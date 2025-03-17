package blockchaintask1;

import blockchaintask0.BlockChain;
import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Blockchain server that handles multiple client requests over TCP using JSON messages.
 */
public class ServerTCP {
    private static final int PORT = 7777;
    private static final BlockChain blockchain = new BlockChain();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5); // Handles multiple clients

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Blockchain server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nWe have a visitor");
                executor.execute(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Scanner in = new Scanner(socket.getInputStream());
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                while (in.hasNextLine()) {
                    String requestJson = in.nextLine();
                    System.out.println("THE JSON REQUEST MESSAGE IS SHOWN HERE");
                    System.out.println(requestJson);

                    String responseJson = handleRequest(requestJson);

                    System.out.println("THE JSON RESPONSE MESSAGE IS SHOWN HERE");
                    System.out.println(responseJson);
                    System.out.println("Number of Blocks on Chain == " + blockchain.getChainSize());

                    out.println(responseJson);
                }

            } catch (IOException e) {
                System.out.println("Client connection closed: " + e.getMessage());
            }
        }

        private String handleRequest(String requestJson) {
            if (requestJson == null || requestJson.isBlank()) {
                return gson.toJson(new ResponseMessage("Error: Received empty or invalid JSON request."));
            }

            RequestMessage request = gson.fromJson(requestJson, RequestMessage.class);

            if (request == null || request.getCommand() == null) {
                return gson.toJson(new ResponseMessage("Error: Malformed JSON request."));
            }

            switch (request.getCommand()) {
                case "STATUS":
                    return gson.toJson(new ResponseMessage(
                            blockchain.getChainSize(),
                            blockchain.getLatestBlock().getDifficulty(),
                            blockchain.getTotalDifficulty(),
                            blockchain.getExpectedTotalHashes(),
                            blockchain.getLatestBlock().getNonce().longValue(),
                            blockchain.getChainHash()
                    ));
                case "ADD_BLOCK":
                    blockchain.addBlock(request.getTransaction(), request.getDifficulty());
                    return gson.toJson(new ResponseMessage("Block added successfully."));
                case "VERIFY":
                    boolean isValid = blockchain.isChainValid();
                    return gson.toJson(new ResponseMessage("Chain verification: " + isValid));
                case "VIEW_CHAIN":
                    return gson.toJson(new ResponseMessage(blockchain));
                case "CORRUPT":
                    blockchain.corruptBlock(request.getBlockIndex(), request.getNewData());
                    return gson.toJson(new ResponseMessage("Block " + request.getBlockIndex() + " corrupted."));
                case "REPAIR":
                    blockchain.repairChain();
                    return gson.toJson(new ResponseMessage("Blockchain repaired."));
                default:
                    return gson.toJson(new ResponseMessage("Invalid command."));
            }
        }
    }
}
