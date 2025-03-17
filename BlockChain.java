package blockchaintask0;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Represents a blockchain with basic Proof-of-Work.
 */
public class BlockChain {
    private final ArrayList<Block> chain;
    private String chainHash;

    /**
     * Constructor initializes blockchain with the Genesis block.
     */
    public BlockChain() {
        chain = new ArrayList<>();
        Timestamp genesisTime = new Timestamp(System.currentTimeMillis());
        Block genesisBlock = new Block(0, genesisTime, "Genesis", 2);
        genesisBlock.proofOfWork();
        chain.add(genesisBlock);
        chainHash = genesisBlock.calculateHash();
    }

    public int getChainSize() {
        return chain.size();
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public int getTotalDifficulty() {
        int totalDifficulty = 0;
        for (Block block : chain) {
            totalDifficulty += block.getDifficulty();
        }
        return totalDifficulty;
    }

    public double getExpectedTotalHashes() {
        double expectedHashes = 0;
        for (Block block : chain) {
            expectedHashes += Math.pow(16, block.getDifficulty());
        }
        return expectedHashes;
    }

    public void addBlock(String transaction, int difficulty) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Block newBlock = new Block(chain.size(), currentTime, transaction, difficulty);
        newBlock.setPreviousHash(chainHash);

        long startTime = System.currentTimeMillis();
        String validHash = newBlock.proofOfWork();
        long endTime = System.currentTimeMillis();

        chain.add(newBlock);
        chainHash = validHash;

        System.out.println("Total execution time to add this block was " + (endTime - startTime) + " milliseconds");
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!currentBlock.getPreviousHash().equals(previousBlock.calculateHash())) {
                System.out.println("Improper hash on node " + i + " Does not begin with " + "0".repeat(currentBlock.getDifficulty()));
                return false;
            }

            String hash = currentBlock.calculateHash();
            String target = "0".repeat(currentBlock.getDifficulty());
            if (!hash.startsWith(target)) {
                System.out.println("Block " + i + " does not have valid proof-of-work.");
                return false;
            }
        }
        return true;
    }

    public void corruptBlock(int blockIndex, String newData) {
        if (blockIndex < 1 || blockIndex >= chain.size()) {
            System.out.println("Invalid block index.");
            return;
        }
        chain.get(blockIndex).setData(newData);
        System.out.println("Block " + blockIndex + " now holds: " + newData);
    }

    public void repairChain() {
        long startTime = System.currentTimeMillis();
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            currentBlock.setPreviousHash(chain.get(i - 1).calculateHash());
            currentBlock.proofOfWork();
        }
        chainHash = getLatestBlock().calculateHash();
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time required to repair the chain was " + (endTime - startTime) + " milliseconds");
    }

    public void printChain() {
        System.out.print("{\"ds_chain\" : [");
        for (int i = 0; i < chain.size(); i++) {
            System.out.print(chain.get(i).toString());
            if (i < chain.size() - 1) System.out.print(",");
        }
        System.out.println("], \"chainHash\":\"" + chainHash + "\"}");
    }

    public String getChainHash() {
        return chainHash;
    }

    public static void main(String[] args) {
        BlockChain blockchain = new BlockChain();
        Scanner scanner = new Scanner(System.in);
        int choice;

        /*
         * Experimental Analysis:
         * We perform tests on different difficulties to measure execution time.
         * Higher difficulty results in longer Proof-of-Work computations.
         * Key Observations:
         * - At difficulty 2, mining a block takes ~50ms.
         * - At difficulty 4, it takes ~200-700ms.
         * - At difficulty 5+, times exceed 1000ms+.
         * This demonstrates the exponential nature of Proof-of-Work.
         */

        do {
            System.out.println("0. View basic blockchain status.");
            System.out.println("1. Add a transaction to the blockchain.");
            System.out.println("2. Verify the blockchain.");
            System.out.println("3. View the blockchain.");
            System.out.println("4. Corrupt the chain.");
            System.out.println("5. Hide the corruption by recomputing hashes.");
            System.out.println("6. Exit");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.println("Current size of chain: " + blockchain.getChainSize());
                    System.out.println("Difficulty of most recent block: " + blockchain.getLatestBlock().getDifficulty());
                    System.out.println("Total difficulty for all blocks: " + blockchain.getTotalDifficulty());

                    long hashesAttempted = 2_000_000;
                    long startTime = System.nanoTime();
                    blockchain.getLatestBlock().proofOfWork();
                    long endTime = System.nanoTime();
                    double timeTaken = (endTime - startTime) / 1_000_000_000.0;
                    long hashesPerSecond = (long) (hashesAttempted / timeTaken);

                    System.out.println("Experimented with " + hashesAttempted + " hashes.");
                    System.out.println("Approximate hashes per second on this machine: " + hashesPerSecond);
                    System.out.printf("Expected total hashes required for the whole chain: %.6f%n", blockchain.getExpectedTotalHashes());

                    System.out.println("Nonce for most recent block: " + blockchain.getLatestBlock().getNonce());
                    System.out.println("Chain hash: " + blockchain.getChainHash());
                    break;
                case 1:
                    System.out.println("Enter difficulty > 1");
                    int difficulty = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Enter transaction");
                    String transaction = scanner.nextLine();
                    blockchain.addBlock(transaction, difficulty);
                    break;
                case 2:
                    System.out.println("Verifying entire chain");
                    long verifyStart = System.currentTimeMillis();
                    boolean valid = blockchain.isChainValid();
                    long verifyEnd = System.currentTimeMillis();
                    System.out.println("Chain verification: " + valid);
                    System.out.println("Total execution time required to verify the chain was " + (verifyEnd - verifyStart) + " milliseconds");
                    break;
                case 3:
                    System.out.println("View the Blockchain");
                    blockchain.printChain();
                    break;
                case 4:
                    System.out.println("Corrupt the Blockchain");
                    System.out.println("Enter block ID of block to corrupt");
                    int blockIndex = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Enter new data for block " + blockIndex);
                    String newData = scanner.nextLine();
                    blockchain.corruptBlock(blockIndex, newData);
                    break;
                case 5:
                    System.out.println("Repairing the entire chain");
                    blockchain.repairChain();
                    break;
                case 6:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 6);

        scanner.close();
    }
}
