package blockchaintask0;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;

public class Block {
    private int index;
    private Timestamp timestamp;
    private String data;
    private String previousHash;
    private BigInteger nonce;
    private int difficulty;

    /**
     * Constructor for Block.
     *
     * @param index      Block position in the chain (Genesis is 0)
     * @param timestamp  Block creation time
     * @param data       Transaction details
     * @param difficulty Number of leading hex zeroes required in hash
     */
    public Block(int index, Timestamp timestamp, String data, int difficulty) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.difficulty = difficulty;
        this.previousHash = "";
        this.nonce = BigInteger.ZERO;
    }

    /**
     * Computes the SHA-256 hash of the block's contents.
     *
     * @return The hash as a hexadecimal string
     */
    public String calculateHash() {
        String input = index + timestamp.toString() + data + previousHash + nonce + difficulty;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Proof-of-work algorithm: finds a valid hash by incrementing nonce.
     *
     * @return A valid hash with the required number of leading zeroes
     */
    public String proofOfWork() {
        String hash = calculateHash();
        String target = new String(new char[difficulty]).replace("\0", "0");
        while (!hash.startsWith(target)) {
            nonce = nonce.add(BigInteger.ONE);
            hash = calculateHash();
        }
        return hash;
    }

    // Getters and setters

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public BigInteger getNonce() { return nonce; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    /**
     * Returns a JSON-like representation of the block.
     */
    @Override
    public String toString() {
        return "{ \"index\" : " + index +
                ", \"timestamp\" : \"" + timestamp.toString() + "\"" +
                ", \"transaction\" : \"" + data + "\"" +
                ", \"previousHash\" : \"" + previousHash + "\"" +
                ", \"nonce\" : " + nonce +
                ", \"difficulty\": " + difficulty +
                " }";
    }

    /**
     * A simple test for the Block class.
     */
    public static void main(String[] args) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Block genesis = new Block(0, now, "Genesis Block", 2);
        genesis.setPreviousHash("");
        String hash = genesis.proofOfWork();

        System.out.println("Genesis Block:");
        System.out.println(genesis);
        System.out.println("Valid Hash: " + hash);
    }
}
