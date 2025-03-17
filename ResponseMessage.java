package blockchaintask1;

import blockchaintask0.BlockChain;

/**
 * Represents a JSON-formatted response sent from the server to the client.
 */
public class ResponseMessage {
    private String message;
    private int chainSize;
    private int difficulty;
    private int totalDifficulty;
    private double expectedHashes;
    private long nonce;
    private String chainHash;
    private BlockChain blockchain;

    // Constructor for status responses
    public ResponseMessage(int chainSize, int difficulty, int totalDifficulty, double expectedHashes, long nonce, String chainHash) {
        this.chainSize = chainSize;
        this.difficulty = difficulty;
        this.totalDifficulty = totalDifficulty;
        this.expectedHashes = expectedHashes;
        this.nonce = nonce;
        this.chainHash = chainHash;
    }

    // Constructor for simple success/error messages
    public ResponseMessage(String message) {
        this.message = message;
    }

    // Constructor for returning the entire blockchain
    public ResponseMessage(BlockChain blockchain) {
        this.blockchain = blockchain;
    }

    // Getters for JSON serialization
    public String getMessage() {
        return message;
    }

    public int getChainSize() {
        return chainSize;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getTotalDifficulty() {
        return totalDifficulty;
    }

    public double getExpectedHashes() {
        return expectedHashes;
    }

    public long getNonce() {
        return nonce;
    }

    public String getChainHash() {
        return chainHash;
    }

    public BlockChain getBlockchain() {
        return blockchain;
    }
}
