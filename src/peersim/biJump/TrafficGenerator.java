package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class TrafficGenerator implements Control {
    public static final int MSG_AC_PKT_PER_CYCLE = 10; // 每个周期发送的消息数量
    public static final int MAX_PKT_PER_SEND = 1; // 每次发送的最大消息数量
    private final static String PAR_PROT = "protocol"; // 需要操作的协议
    private int pid; // 协议ID
    private RandomTextGeneratorFromFile rtg;
    private static final String TEXT_FILE = "src_file";
    private String fileName;

    public TrafficGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        fileName = Configuration.getString(prefix + "." + TEXT_FILE);
        rtg = new RandomTextGeneratorFromFile(fileName);
    }

    private MessageAC generateFindNodeMessage() {
        MessageAC m = new MessageAC();
        m.timestamp = CommonState.getTime();

        // 随机选择发送端、接收端
        Node src = Network.get(CommonState.r.nextInt(Network.size()));
        while (!src.isUp()) {
            src = Network.get(CommonState.r.nextInt(Network.size()));
        }
        m.srcID = ((BiJumpProtocol) (src.getProtocol(pid))).nodeId;
        Node dest = Network.get(CommonState.r.nextInt(Network.size()));
        while (!dest.isUp() || dest == src) {
            dest = Network.get(CommonState.r.nextInt(Network.size()));
        }
        m.destID = ((BiJumpProtocol) (dest.getProtocol(pid))).nodeId;

        // body 随机生成一段文字，长度不固定，范围 200-1000
        m.body = rtg.generateRandomText();
        return m;
    }

    public boolean execute() {
        // 每次随机发送 MSG_AC_PKT_PER_CYCLE 组消息
        // 每组消息最多发送 MAX_PKT_PER_SEND 条数据包
        for (int i = 0; i < MSG_AC_PKT_PER_CYCLE; i++) {
            MessageAC m = generateFindNodeMessage();
            EDSimulator.add(0, m, BiJumpProtocol.nodeIdtoNode(m.srcID, pid), pid);
            for (int j = 0; j < CommonState.r.nextInt(MAX_PKT_PER_SEND) - 1; j++) {
                MessageAC next = m.clone(rtg.generateRandomText());
                EDSimulator.add(0, next, BiJumpProtocol.nodeIdtoNode(next.srcID, pid), pid);
            }
        }
        return false;
    }
}
