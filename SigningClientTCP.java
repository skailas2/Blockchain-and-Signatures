package blockchaintask2;

import com.google.gson.Gson;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

/**
 * Blockchain client that signs requests using RSA.
 */
public class SigningClientTCP {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final Gson gson = new Gson();
    private static BigInteger e, d, n; // RSA keys
    private static String clientID;

    public static void main(String[] args) throws Exception {
        generateKeyPair();
        computeClientID();

        System.out.println("Client ID: " + clientID);
        System.out.println("Public Key (e, n): " + e + ", " + n);
        System.out.println("Private Key (d, n): " + d + ", " + n);

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("0. View blockchain status.");
                System.out.println("1. Add a transaction.");
                System.out.println("2. Verify blockchain.");
                System.out.println("3. View blockchain.");
                System.out.println("4. Corrupt the chain.");
                System.out.println("5. Repair the chain.");
                System.out.println("6. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 6) {
                    System.out.println("Exiting...");
                    return;
                }

                RequestMessage requestMessage = createSignedRequest(choice, scanner);
                String requestJson = gson.toJson(requestMessage);

                System.out.println("Sending request: " + requestJson);
                out.println(requestJson);
                out.flush();

                String responseJson = in.readLine();
                System.out.println("Server response: " + responseJson);
            }
        }
    }

    private static void generateKeyPair() throws Exception {
        SecureRandom rnd = new SecureRandom();
        BigInteger p = new BigInteger(400, 100, rnd);
        BigInteger q = new BigInteger(400, 100, rnd);
        n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("65537");
        d = e.modInverse(phi);
    }

    private static void computeClientID() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((e.toString() + n.toString()).getBytes());
        clientID = Base64.getEncoder().encodeToString(hash).substring(0, 20);
    }

    private static String signMessage(String message) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes());
        BigInteger hashInt = new BigInteger(1, hash);
        BigInteger signature = hashInt.modPow(d, n);
        return signature.toString();
    }

    private static RequestMessage createSignedRequest(int choice, Scanner scanner) throws Exception {
        String requestType = switch (choice) {
            case 0 -> "STATUS";
            case 1 -> {
                System.out.println("Enter difficulty > 1:");
                int difficulty = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Enter transaction:");
                String transaction = scanner.nextLine();
                yield "ADD_BLOCK:" + transaction + ":" + difficulty;
            }
            case 2 -> "VERIFY";
            case 3 -> "VIEW_CHAIN";
            case 4 -> {
                System.out.println("Enter block ID to corrupt:");
                int blockIndex = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Enter new data:");
                String newData = scanner.nextLine();
                yield "CORRUPT:" + blockIndex + ":" + newData;
            }
            case 5 -> "REPAIR";
            default -> "INVALID";
        };

        String signature = signMessage(requestType);
        return new RequestMessage(clientID, e.toString(), n.toString(), requestType, "", signature);
    }
}
