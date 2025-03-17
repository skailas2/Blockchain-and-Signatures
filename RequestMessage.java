package blockchaintask1;

/**
 * Represents a JSON-formatted request sent from the client to the server.
 */
public class RequestMessage {
    private String command;
    private String transaction;
    private int difficulty;
    private int blockIndex;
    private String newData;

    // Constructors for different types of requests
    public RequestMessage(String command) {
        this.command = command;
    }

    public RequestMessage(String command, String transaction, int difficulty) {
        this.command = command;
        this.transaction = transaction;
        this.difficulty = difficulty;
    }

    public RequestMessage(String command, int blockIndex, String newData) {
        this.command = command;
        this.blockIndex = blockIndex;
        this.newData = newData;
    }

    // Getters for JSON serialization
    public String getCommand() {
        return command;
    }

    public String getTransaction() {
        return transaction;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public String getNewData() {
        return newData;
    }
}
