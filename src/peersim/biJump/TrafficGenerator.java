package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

import java.security.SecureRandom;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class TrafficGenerator implements Control {
    public final static String MSG_AC_PKT_PER_CYCLE = "msgACPerCycle"; // 每个周期发送的消息数量
    public final static String MAX_PKT_PER_SEND = "maxPktPerSend"; // 每次发送的最大消息数量
    private final static String PAR_PROT = "protocol"; // 需要操作的协议
    private int msgAckPktPerCycle; // 每个周期发送的消息数量
    private int maxPktPerSend; // 每次发送的最大消息数量

    private int pid; // 协议ID
    private RandomTextGeneratorFromFile rtg;
    private static final String TEXT_FILE = "src_file";
    private String fileName;
    SecureRandom secureRandom;

    public TrafficGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        fileName = Configuration.getString(prefix + "." + TEXT_FILE);
        System.out.println("TrafficGenerator: " + fileName + "-----------------------------------------");
        rtg = new RandomTextGeneratorFromFile(fileName);
        secureRandom = new SecureRandom();
        msgAckPktPerCycle = Configuration.getInt(prefix + "." + MSG_AC_PKT_PER_CYCLE);
        maxPktPerSend = Configuration.getInt(prefix + "." + MAX_PKT_PER_SEND);
    }

    private MessageAC generateACMessage() {
        MessageAC m = new MessageAC();
        m.timestamp = CommonState.getTime();

        // 随机选择发送端、接收端
        Node src = Network.get(secureRandom.nextInt(Network.size()));
        while (!src.isUp()) {
            src = Network.get(secureRandom.nextInt(Network.size()));
        }
        m.srcID = ((BiJumpProtocol) (src.getProtocol(pid))).nodeId;
        Node dest = Network.get(secureRandom.nextInt(Network.size()));
        while (!dest.isUp() || dest == src) {
            dest = Network.get(secureRandom.nextInt(Network.size()));
        }
        m.destID = ((BiJumpProtocol) (dest.getProtocol(pid))).nodeId;

        // body 随机生成一段文字，长度不固定，范围 200-1000
        m.body = rtg.generateRandomText();
        return m;
    }

    public boolean execute() {
        // 每次随机发送 msgAckPktPerCycle 条消息
        // 每组消息最多发送 maxPktPerSend 条数据包
        for (int i = 0; i < msgAckPktPerCycle; i++) {
            MessageAC m = generateACMessage();
            EDSimulator.add(0, m, BiJumpProtocol.nodeIdtoNode(m.srcID, pid), pid);
            for (int j = 0; j < CommonState.r.nextInt(maxPktPerSend) - 1; j++) {
                MessageAC next = m.clone(rtg.generateRandomText());
                EDSimulator.add(0, next, BiJumpProtocol.nodeIdtoNode(next.srcID, pid), pid);
            }
        }
        return false;
    }
}
