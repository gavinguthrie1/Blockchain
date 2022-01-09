import com.google.common.hash.Hashing;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * This is a class for a simple block created in Java.
 * @author Gavin Guthrie
 * @see <a href="https://github.com/gavinguthrie1/Blockchain">Github</a>
 */

public class Block{

    private final int index;
    private String hash;
    private final Block lastBlock;
    private final DateTime timestamp;
    private ArrayList<String> blockData;
    private int nonce = 0;
    private int difficulty;

    public Block(Block previousBlock, ArrayList<String> blockData) throws BlockError {
        //If not genesis and cannot verify previous block, then error in chain
        if(!this.checkGenesisBlock() && !previousBlock.verifyHash()){
            throw new BlockError(String.format("Failed to verify hash of block %s to generate %s", previousBlock.hash, this.hash));
        }

        //If not genesis increment index, otherwise set to zero
        if(!this.checkGenesisBlock()){
            this.index = (previousBlock.getIndex() + 1);

        }else{
            this.index = 0;
        }

        this.lastBlock = previousBlock;
        //Set timestamp to current time
        this.timestamp = new DateTime();
        this.blockData = blockData;
    }


    /**
     * Generates SHA256 hash of block
     * @return SHA256 Hash String
     * @throws BlockError
     */
    public String generateHash() throws BlockError {
        if(hash != null){
            throw new BlockError("Unable to update hash after initial calculation");
        }

        //Concatenate block data into string
        String blockDataFullString = String.valueOf(this.index);
        if(!this.checkGenesisBlock()){
            blockDataFullString += this.lastBlock.hash;
        }
        blockDataFullString += this.timestamp.toString();

        for(String data : this.blockData){
            blockDataFullString += data;
        }

        blockDataFullString += String.valueOf(difficulty);

        //Hash concatenated string
        this.hash = Hashing.sha256().hashString(blockDataFullString, StandardCharsets.UTF_8).toString();

        return this.hash;
    }


    /**
     * Generates SHA256 hash of block with nonce
     * @param nonce Nonce to include in hash
     * @return SHA256 Hash String
     * @throws BlockError
     */
    public String generateNonceHash(int nonce) throws BlockError {
        String blockDataFullString = Integer.toString(this.index);
        if(!this.checkGenesisBlock()){
            blockDataFullString += this.lastBlock.hash;
        }
        blockDataFullString += this.timestamp.toString();

        for(String data : this.blockData){
            blockDataFullString += data;
        }
        blockDataFullString += String.valueOf(nonce);

        String nonceHashHex = Hashing.sha256().hashString(blockDataFullString, StandardCharsets.UTF_8).toString();

        return hex_to_binary(nonceHashHex);
    }


    /**
     * Verifies hash of block
     * @return True if valid, false otherwise
     * @throws BlockError
     */
    public boolean verifyHash() throws BlockError {
        //Hash has not been generated, return false
        if(this.hash == null){
            return false;
        }

        //Temp store hash
        String tempHash = this.hash;
        this.hash = null;

        //Recalculate hash
        this.generateHash();

        return this.hash.equals(tempHash);

    }

    public String getHash(){
        return this.hash;
    }

    public int getIndex() throws BlockError {
        return this.index;
    }

    /**
     * Checks if block is genesis
     * @return True if genesis, false otherwise.
     */
    public Boolean checkGenesisBlock(){
        return this.index == 0 && this.lastBlock == null;
    }

    public void setNonce(int nonce) throws BlockError {
        if(this.nonce != 0){
            throw new BlockError("Unable to update nonce twice!");
        }

        this.nonce = nonce;
    }

    public static String hex_to_binary(String hex) {
        int len = hex.length() * 4;
        String bin = new BigInteger(hex, 16).toString(2);

        if(bin.length() < len){
            int diff = len - bin.length();
            String pad = "";
            for(int i = 0; i < diff; ++i){
                pad = pad.concat("0");
            }
            bin = pad.concat(bin);
        }
        return bin;
    }

    public int getNonce(){
        return this.nonce;
    }

    public DateTime getTimestamp(){
        return this.timestamp;
    }

    public void setDifficulty(int difficulty) throws BlockError {
        if(this.hash != null){
            throw new BlockError("Unable to update difficulty after generating hash");
        }

        this.difficulty = difficulty;
    }

    public int getDifficulty(){
        return this.difficulty;
    }
}
