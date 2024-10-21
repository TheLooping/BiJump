package peersim.biJump;

import java.math.BigInteger;
import java.util.Random;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class UniformRandomGenerator {
    private final Random rnd;
    private final int bits;
    static long seed = 0;


    public UniformRandomGenerator(int aBits, Random r) {
        bits = aBits;
        rnd = r;
    }
    public UniformRandomGenerator(int aBits, long aSeed) {
        this(aBits, new Random(aSeed));
    }


    private final BigInteger nextRand() {
        return new BigInteger(bits, rnd);
    }
    public final BigInteger generate() {
        return nextRand();
    }


}
