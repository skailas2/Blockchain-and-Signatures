package blockchaintask1;

import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Blockchain Client that communicates with ServerTCP over TCP using JSON messages.
 */
public class ClientTCP {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("0. View basic blockchain status.");
                System.out.println("1. Add a transaction to the blockchain.");
                System.out.println("2. Verify the blockchain.");
                System.out.println("3. View the blockchain.");
                System.out.println("4. Corrupt the chain.");
                System.out.println("5. Hide the corruption by recomputing hashes.");
                System.out.println("6. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();

                String requestJson = "";
                switch (choice) {
                    case 0:
                        requestJson = gson.toJson(new RequestMessage("STATUS"));
                        break;
                    case 1:
                        System.out.println("Enter difficulty > 1");
                        int difficulty = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter transaction:");
                        String transaction = scanner.nextLine();
                        requestJson = gson.toJson(new RequestMessage("ADD_BLOCK", transaction, difficulty));
                        break;
                    case 2:
                        requestJson = gson.toJson(new RequestMessage("VERIFY"));
                        break;
                    case 3:
                        requestJson = gson.toJson(new RequestMessage("VIEW_CHAIN"));
                        break;
                    case 4:
                        System.out.println("Enter block ID of block to corrupt:");
                        int blockIndex = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Enter new data:");
                        String newData = scanner.nextLine();
                        requestJson = gson.toJson(new RequestMessage("CORRUPT", blockIndex, newData));
                        break;
                    case 5:
                        requestJson = gson.toJson(new RequestMessage("REPAIR"));
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                        continue;
                }

                out.println(requestJson);
                out.flush();

                String responseJson = in.readLine();
                System.out.println("Server response: " + responseJson);
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
