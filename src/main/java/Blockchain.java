import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a class for a simple blockchain created in Java.
 * @author Gavin Guthrie
 * @see <a href="https://github.com/gavinguthrie1/Blockchain">Github</a>
 */

public class Blockchain {
    //Create array to store blocks
    private Block[] blockchain;
    //Initialise int to store number of blocks
    private int blockHeight = 0;
    //Initialise difficulty to 1
    private int difficulty = 0;
    //Increase difficulty every 30 blocks
    public final int blockInterval = 10;
    //Create 10 new block every 40s
    public final int blockTime = 40;

    public Blockchain() throws BlockError {
        //Create an array of size 1
        this.blockchain = new Block[1];

        //Create the first block
        Block genesisBlock = new Block(null, new ArrayList<String>());

        //Add to blockchain
        this.blockchain[0] = genesisBlock;

        //Calculate difficulty for genesis block
        this.getCurrentDifficulty();

        //Generate Hash
        genesisBlock.generateHash();

        //Mine Genesis block
        this.mineLatestBlock();

        //Print hash of genesis block
        System.out.println(String.format("Genesis Block Hash: %s", genesisBlock.getHash()));

    }


    /**
     * Creates a the next block on a chain
     * @param blockData Data to be passed into the block
     * @throws BlockError
     */
    public void createNextBlock(ArrayList<String> blockData) throws BlockError {
        Block nextBlock = new Block(this.getLatestBlock(), blockData);

        blockHeight++;

        //Create array 1 larger than previous
        Block[] updatedBlockchain = Arrays.copyOf(this.blockchain, blockHeight + 1);
        updatedBlockchain[blockHeight] = nextBlock;

        blockchain = updatedBlockchain;

        //Calculate difficulty for block
        this.getCurrentDifficulty();

        nextBlock.generateHash();

        System.out.println(String.format("Added Block %s, Hash %s", blockHeight, nextBlock.getHash()));
    }

    /**
     * Gets the latest block
     * @return Latest block
     */
    public Block getLatestBlock(){
        return blockchain[blockHeight];
    }

    /**
     * Checks the validity of a chain
     * @return True if valid, false otherwise
     * @throws BlockError
     */
    public boolean verifyChain() throws BlockError {
        //If first block is not the genesis, then invalid.
        if(!blockchain[0].checkGenesisBlock()){
            return false;
        }

        //Verify hash of each block
        for(int i = 1; i < blockchain.length; i++){
            if(!blockchain[i].verifyHash()){
                return false;
            }
        }

        return true;
    }


    /**
     * Checks if block has been mined.
     * @return True if mined, false otherwise
     * @throws BlockError
     */
    public boolean verifyBlockMinedBlock(Block checkBlock) throws BlockError {
        //Generate hash for block with nonce
        String binaryHash = checkBlock.generateNonceHash(checkBlock.getNonce());

        //Split number dictated by difficulty
        String difficultyRemovedHash = binaryHash.substring(0, checkBlock.getDifficulty());

        //Checks if split has number of zeros required by difficulty
        return difficultyRemovedHash.equals(new String(new char[checkBlock.getDifficulty()]).replace("\0", "0"));
    }


    /**
     * Mines the latest block in the chain
     * @return
     * @throws BlockError
     */
    public int mineLatestBlock() throws BlockError {
        //Loop from zero to when nonce is found
        for(int nonce = 0; true; nonce++){

            int difficulty = blockchain[blockHeight].getDifficulty();

            //Generate hash for block with nonce
            String binaryHash = blockchain[blockHeight].generateNonceHash(nonce);

            //Split number dictated by difficulty
            String difficultyRemovedHash = binaryHash.substring(0, difficulty);

            //Checks if split has number of zeros required by difficulty
            if(difficultyRemovedHash.equals(new String(new char[difficulty]).replace("\0", "0"))){
                //Sets nonce and returns
                blockchain[blockHeight].setNonce(nonce);
                System.out.println(String.format("Mined block %s with nonce %s", blockchain[blockHeight].getHash(), nonce));
                return nonce;
            }
        }
    }


    /**
     * Calculated + Returns the correct difficulty to match blockInterval * blockTime
     * @return New difficulty
     * @throws BlockError
     */
    public int getCurrentDifficulty() throws BlockError {
        //If block interval has been reached and not genesis block
        if(blockHeight % blockInterval == 0 & !blockchain[blockHeight].checkGenesisBlock()){
            Block previousDifficultyUpdate = blockchain[blockHeight - blockInterval];

            //Expected difference = blockInterval * blockTime.
            Duration expectedBlockDifference  = new Duration((blockInterval * blockTime) * 100);

            //Calculate actual difference
            DateTime actualBlockTime = previousDifficultyUpdate.getTimestamp();
            Duration actualBlockDifference  = new Duration(actualBlockTime, blockchain[blockHeight].getTimestamp());


            if(actualBlockDifference.isShorterThan(expectedBlockDifference.multipliedBy(2))){
                //If the time taken is more than double the allowed increase the difficultly
                this.difficulty++;
                System.out.println(String.format("Increased difficulty to %s", this.difficulty));
            }else if(actualBlockDifference.isLongerThan(expectedBlockDifference.dividedBy(2))){
                //If the time taken is more than double the allowed increase the difficultly
                this.difficulty--;
                System.out.println(String.format("Decreased difficulty to %s", this.difficulty));
            }
        }

        blockchain[blockHeight].setDifficulty(this.difficulty);

        return this.difficulty;
    }

    /**
     * Calculates difficulty of blockchain (with formula for each block 2^difficulty) to determine which POW chain to use
     * @return difficulty
     */
    public int getBlockchainDifficulty(){
        int difficulty = 0;

        for(Block block : blockchain){
            difficulty += Math.pow(2, block.getDifficulty());
        }

        return difficulty;
    }



}
