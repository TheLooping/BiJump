package peersim.biJump;

import java.math.BigInteger;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class Timeout extends SimpleEvent {
    public static final int TIMEOUT = 100;
    public BigInteger node;
    public long msgID;
    public long opID;
    public Timeout(BigInteger node, long msgID, long opID) {
        super(TIMEOUT);
        this.node = node;
        this.msgID = msgID;
        this.opID = opID;
    }
}