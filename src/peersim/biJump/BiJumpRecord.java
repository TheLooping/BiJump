package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.util.IncrementalStats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/18
 */
public class BiJumpRecord
{
    /** 单例实例 */
    private static BiJumpRecord instance;

    public static BiJumpRecord getInstance() {
        if (instance == null) {
            instance = new BiJumpRecord();
        }
        return instance;
    }

    /**
     * 统计每个消息传递的跳数
     */
    public static IncrementalStats hopStore = new IncrementalStats();

    /**
     * 统计每个消息传递的时间
     */
    public static IncrementalStats timeStore = new IncrementalStats();

    /**
     * 统计消息传递的数量
     */
    public static IncrementalStats msg_deliv = new IncrementalStats();

    /** 观测的协议参数 */
    private static final String PAR_PROT = "protocol";
    /** 输出前缀 */
    public static String prefix = "biJumpRecordFile";

    /** 记录消息转发路径的文件名 */
    private String pathRecordFile;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;

    public BiJumpRecord()
    {
        pathRecordFile = Configuration.getString(prefix);
        System.out.println("Record file path: " + pathRecordFile + "---------------------->>>>>>>>>>>>>>>>>>>>>>>>>>");
        // 初始化文件
        try {
            file = new File(pathRecordFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            // 写入表头：Time, NodeID, Direction, MSGID, ACKID, SrcID, DestID, NextHopID, RealDestID, BodyLength, TTL
            // "Time", "NodeID", "Direction", "MSGID", "ACKID", "SrcID", "DestID", "NextHopID", "RealDestID", "BodyLength", "TTL", "isLast2Hop", "isLastHop"
            bw.write(String.format("%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s%-16s\n",
                    "Time", "NodeID", "Direction", "MSGID", "ACKID", "SrcID", "DestID", "NextHopID", "RealDestID", "BodyLength", "TTL", "isLast2Hop", "isLastHop"));


            // 写入文件
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录消息传递的跳数
     * @param msg 消息
     * @param isLastHop 是否是最后一跳
     */
    public void recordTxHop(MessageAC msg, boolean isLastHop, boolean isLast2Hop, int myPid) {
        hopStore.add(1);
        // 写入文件
        try {
            // String.format("%d, %d, %d, %d, %d, %s, %s, %s, %s, %d, %d, %d, %d\n",
            //                    CommonState.getTime(), msg.srcID, 1,
            //                    msg.id, msg.ackID,
            //                    msg.srcID, msg.destID, msg.nextHopID,
            //                    isLastHop?msg.destID:"",
            //                    msg.body.length, msg.ttl,
            //                    isLast2Hop?1:0, isLastHop?1:0)
//            String record = String.format("%d, %d, %d, %d, %d, %s, %s, %s, %s, %d, %d, %d, %d\n",
//                    CommonState.getTime(), msg.srcID, 1,
//                    msg.id, msg.ackID,
//                    msg.srcID, msg.destID, msg.nextHopID,
//                    isLastHop?msg.destID:"notLastHop",
//                    msg.body.length, msg.ttl,
//                    isLast2Hop?1:0, isLastHop?1:0);
            String record = String.format("%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d%-16d\n",
                    CommonState.getTime(),
                    BiJumpProtocol.nodeIdtoNodeIndex(msg.srcID, myPid),
                    1,
                    msg.id,
                    msg.ackID,
                    BiJumpProtocol.nodeIdtoNodeIndex(msg.srcID, myPid),
                    BiJumpProtocol.nodeIdtoNodeIndex(msg.destID, myPid),
                    BiJumpProtocol.nodeIdtoNodeIndex(msg.nextHopID, myPid),
                    isLastHop ? BiJumpProtocol.nodeIdtoNodeIndex(msg.destID, myPid) : -1,
                    msg.body.length,
                    msg.ttl,
                    isLast2Hop ? 1 : 0,
                    isLastHop ? 1 : 0);
            bw.write(record);

            bw.flush();
//            System.out.println(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
