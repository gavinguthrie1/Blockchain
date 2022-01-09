import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String args[]) throws BlockError, InterruptedException {
        Blockchain b = new Blockchain();

        for(int i = 0; i < 100; i++) {
            b.createNextBlock(new ArrayList<String>());

            b.mineLatestBlock();

        }

        System.out.println(b.getBlockchainDifficulty());
    }
}
