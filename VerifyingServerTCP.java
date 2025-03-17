package blockchaintask2;

import com.google.gson.Gson;
import blockchaintask0.BlockChain;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;

public class VerifyingServerTCP {
    private static final int PORT = 7777;
    private static final Gson gson = new Gson();
    private static final BlockChain blockchain = new BlockChain();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Blockchain server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String requestJson;
                while ((requestJson = in.readLine()) != null) {
                    System.out.println("Received request: " + requestJson);
                    RequestMessage request = gson.fromJson(requestJson, RequestMessage.class);

                    if (!verifyClient(request) || !verifySignature(request)) {
                        out.println(gson.toJson(new ResponseMessage("Error: Invalid authentication")));
                        continue;
                    }

                    String response;
                    if (request.getCommand().startsWith("ADD_BLOCK")) {
                        String[] parts = request.getCommand().split(":");
                        blockchain.addBlock(parts[1], Integer.parseInt(parts[2]));
                        response = "Block added successfully.";
                    } else if ("VIEW_CHAIN".equals(request.getCommand())) {
                        response = blockchain.toString();
                    } else if ("VERIFY".equals(request.getCommand())) {
                        response = "Blockchain verification: " + blockchain.isChainValid();
                    } else {
                        response = "Unknown request.";
                    }

                    out.println(gson.toJson(new ResponseMessage(response)));
                }
            } catch (Exception e) {
                System.out.println("Verification error: " + e.getMessage());
            }
        }

        private boolean verifyClient(RequestMessage request) throws Exception {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((request.getE() + request.getN()).getBytes());
            String expectedID = Base64.getEncoder().encodeToString(hash).substring(0, 20);
            return expectedID.equals(request.getId());
        }

        private boolean verifySignature(RequestMessage request) throws Exception {
            String dataToVerify = request.getCommand();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToVerify.getBytes());
            BigInteger expectedHash = new BigInteger(1, hash);
            BigInteger receivedSignature = new BigInteger(request.getSignature());
            BigInteger decryptedHash = receivedSignature.modPow(new BigInteger(request.getE()), new BigInteger(request.getN()));
            return expectedHash.equals(decryptedHash);
        }
    }
}
