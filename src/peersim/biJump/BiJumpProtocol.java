package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.UnreliableTransport;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;


/**
 * @author <a href="kw_wang_@outlook.com">Kaiwen Wang</a>
 * @version v1.0
 * @since 2024/10/12
 */
public class BiJumpProtocol implements EDProtocol {
    public static int num = 0;
    public static final int NODE_ID_BITS = 160;
    public static final String PAR_TRANSPORT = "transport";
    public static final double FORWARD_PROBABILITY = 0.2;
    public static String prefix = null;
    public UnreliableTransport transport;
    public int transport_pid; // protocol identifier
    public int biJump_pid;
    private SecureRandom secureRandom = new SecureRandom();

    private static boolean _ALREADY_INSTALLED = false;

    public BigInteger nodeId;
    public Cryption cryption;
    public BiJumpRecord record = BiJumpRecord.getInstance();

    public BiJumpProtocol(String prefix) {
        this.prefix = prefix;
        this.transport_pid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
//        this.bijump_pid = Configuration.lookupPid("bijump");
//        this.nodeId = new BigInteger(NODE_ID_BITS, CommonState.r);
        try {
            this.cryption = new Cryption();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKeyOfNode (int node_index) {
        Node node = Network.get(node_index);
        BiJumpProtocol p = (BiJumpProtocol) node.getProtocol(biJump_pid);
        return p.cryption.getPublicKey();
    }
    static public Node nodeIdtoNode(BigInteger searchNodeId, int pid) {
        if (searchNodeId == null)
            return null;

        int inf = 0;
        int sup = Network.size() - 1;
        int m;

        while (inf <= sup) {
            m = (inf + sup) / 2;

            BigInteger mId = ((BiJumpProtocol) Network.get(m).getProtocol(pid)).nodeId;

            if (mId.equals(searchNodeId))
                return Network.get(m);

            if (mId.compareTo(searchNodeId) < 0)
                inf = m + 1;
            else
                sup = m - 1;
        }

        // perform a traditional search for more reliability (maybe the network is not ordered)
        BigInteger mId;
        for (int i = Network.size() - 1; i >= 0; i--) {
            mId = ((BiJumpProtocol) Network.get(i).getProtocol(pid)).nodeId;
            if (mId.equals(searchNodeId))
                return Network.get(i);
        }

        return null;
    }
    static public int nodeIdtoNodeIndex(BigInteger searchNodeId, int pid) {
        if (isLastHop(searchNodeId))
            return -1;
        Node n = nodeIdtoNode(searchNodeId, pid);
        if (n != null)
            return n.getIndex();
        else
            return -2;
    }

    private BigInteger getNodeIdFromNode(Node node) {
        return ((BiJumpProtocol) node.getProtocol(biJump_pid)).nodeId;
    }

    public Object clone() {
        BiJumpProtocol bp = new BiJumpProtocol(BiJumpProtocol.prefix);
        return bp;
    }


    public boolean nextCycle() {
        return false;
    }

    // 构造aesKey，标志是接收端，并不使用
    private byte[] generateRealDestAesKey() {
        byte[] aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
        Arrays.fill(aesKey, (byte) 1);
        return aesKey;
    }
    private Boolean isRealDestAesKey(byte[] aesKey) {
        byte[] realDestAesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
        Arrays.fill(realDestAesKey, (byte) 1);
        return Arrays.equals(aesKey, realDestAesKey);
    }

    // 构造nextHopID，标志最后一跳，并不使用
    private static BigInteger generateLastHopID() {
        byte[] lastHopID = new byte[NODE_ID_BITS / 8];
        Arrays.fill(lastHopID, (byte) 1);
        return new BigInteger(lastHopID);
    }
    private static Boolean isLastHop(BigInteger nextHopID) {
        byte[] lastHopID = new byte[NODE_ID_BITS / 8];
        Arrays.fill(lastHopID, (byte) 1);
        return Arrays.equals(nextHopID.toByteArray(), lastHopID);
    }

    // 根据ttl、概率是否结束随机转发过程
    private boolean shouldContinueForwarding(int ttl, double probability) {
        // 如果TTL已经为0，直接终止转发
        if (ttl <= 2) {
            return false;
        }
        // 生成0到1之间的随机数，若小于概率，则终止转发
        double randomValue = secureRandom.nextDouble();
        return (randomValue < probability);
    }

    // 根据随机收敛变化的转发参数结束转发过程
    private boolean shouldContinueForwardingbyRFP(MessageAC msg) {
        double newRndFwdPara = ForwardingParameterCalculator.iterate(msg.rndFwdPara);
//        System.out.println("msg.rndFwdPara: " + msg.rndFwdPara + " -> " + newRndFwdPara);
        // 使用 g 函数判断是否停止
        msg.rndFwdPara = newRndFwdPara;
        double r = secureRandom.nextDouble();
        if (r > ForwardingProbabilityCalculator.g(newRndFwdPara, ForwardingParameterCalculator.epsilon)) {
            return false;
        }
        return true;
    }

    private int getRandNodeIndex(Node node) {
        while (true) {
            int next2HopIndex = secureRandom.nextInt(Network.size());
            if (next2HopIndex != node.getIndex()) {
                return next2HopIndex;
            }
        }
    }
    private BigInteger nodeIndexToId(int index) {
        return ((BiJumpProtocol) Network.get(index).getProtocol(biJump_pid)).nodeId;
    }

    private void processInitPkt(Node node, MessageAC msg) {
        try {
            // 加密真实源（使用真实目的地的公钥）
            msg.isInitiator = false;
            msg.encryptedRealSrc = msg.srcID.toByteArray();
            int realDestIndex = nodeIdtoNode(msg.destID, biJump_pid).getIndex();
            msg.encryptedRealDest = msg.destID.toByteArray();

            int nextHopIndex = getRandNodeIndex(node);
            int next2HopIndex = getRandNodeIndex(node);

            msg.srcID = nodeId;
            msg.destID = nodeIndexToId(nextHopIndex);
            msg.nextHopID = nodeIndexToId(next2HopIndex);


            // 下一跳aesKey 加密： 真实接收端, body
            byte[] next_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(next_aesKey); // 生成随机 aesKey + IV
            msg.encryptedRealDest = cryption.aesCTREncryptProcess(msg.encryptedRealDest, next_aesKey); // 加密真实目的地
            msg.body = cryption.aesCTREncryptProcess(msg.body, next_aesKey); // 加密 body
            msg.encryptedNextHopKey = cryption.encryptWithPublicKey(getPublicKeyOfNode(nextHopIndex), next_aesKey);

            // 下下跳aesKey 加密： 真实接收端, body
            byte[] next2_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(next2_aesKey);
            msg.encryptedRealDest = cryption.aesCTREncryptProcess(msg.encryptedRealDest, next2_aesKey); // 加密真实目的地
            msg.body = cryption.aesCTREncryptProcess(msg.body, next2_aesKey); // 加密 body
            msg.encryptedNext2HopKey = cryption.encryptWithPublicKey(getPublicKeyOfNode(next2HopIndex), next2_aesKey);

            // 真实接收端aesKey 加密：真实源身份
            byte[] realDest_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(realDest_aesKey);
            msg.encryptedRealSrc = cryption.aesCTREncryptProcess(msg.encryptedRealSrc, realDest_aesKey);
            msg.encryptedRealDestKey = cryption.encryptWithPublicKey(getPublicKeyOfNode(realDestIndex), realDest_aesKey);

            sendMessage(msg, msg.destID, biJump_pid, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processPkt(Node node, MessageAC msg) {
        try {
            if (msg.isInitiator) {
                processInitPkt(node, msg);
                return;
            }
            msg.ttl--;

            // 判断是否是倒数第一跳
            if (isLastHop(msg.nextHopID)) {
                processLastHopPkt(node, msg);
                return;
            }

            // 判断是否是接收端
            byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedNextHopKey);
            if (isRealDestAesKey(aesKey)) {
                processRequestPkt(node, msg);
                return;
            }

            // 判断是否是 响应第一跳
            if (msg.isResponseFirstHop) {
                processResponseFirstHopPkt(node, msg);
                return;
            }

            // 判断是否结束转发
//            if (!shouldContinueForwarding(msg.ttl, FORWARD_PROBABILITY)) {
//                processLast2HopPkt(node, msg);
//                return;
//            }

            // 判断是否结束转发
            if (!shouldContinueForwardingbyRFP(msg)) {
                processLast2HopPkt(node, msg);
                return;
            }

            // 普通中继节点跳转：解密，加密
            processRelayPkt(node, msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 普通中继节点跳转：解密，只加密
    private void processRelayPkt(Node node, MessageAC msg) {
        try {
            // 解密
            byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedNextHopKey);
            msg.body = cryption.aesCTRDecryptProcess(msg.body, aesKey);
            msg.encryptedRealDest = cryption.aesCTRDecryptProcess(msg.encryptedRealDest, aesKey);
            msg.encryptedNextHopKey = msg.encryptedNext2HopKey;

            // 随机选一个下下跳, 并生成新的aesKey, 加密msg.body 和 msg.encryptedRealDest
            int next2HopIndex = secureRandom.nextInt(Network.size());
            PublicKey next2HopPublicKey = getPublicKeyOfNode(next2HopIndex);
            byte[] next_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(next_aesKey);
            msg.encryptedNext2HopKey = cryption.encryptWithPublicKey(next2HopPublicKey, next_aesKey);
            msg.encryptedRealDest = cryption.aesCTREncryptProcess(msg.encryptedRealDest, next_aesKey);
            msg.body = cryption.aesCTREncryptProcess(msg.body, next_aesKey);

            // 发送
            msg.srcID = nodeId;
            msg.destID = msg.nextHopID;
            msg.nextHopID = nodeIndexToId(next2HopIndex);

            sendMessage(msg, msg.destID, biJump_pid, false, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 倒数第二跳：只解密，不加密
    private void processLast2HopPkt(Node node, MessageAC msg) {
        try {
            byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedNextHopKey);
            msg.body = cryption.aesCTRDecryptProcess(msg.body, aesKey);
            msg.encryptedRealDest = cryption.aesCTRDecryptProcess(msg.encryptedRealDest, aesKey);

            msg.srcID = nodeId;
            msg.destID = msg.nextHopID;

            msg.nextHopID = generateLastHopID();

            msg.encryptedNextHopKey = msg.encryptedNext2HopKey;

            // 下下跳aesKey随便填，用不到
            msg.encryptedNext2HopKey = new byte[msg.encryptedNextHopKey.length];
            secureRandom.nextBytes(msg.encryptedNext2HopKey);

            // 发送
            sendMessage(msg, msg.destID, biJump_pid, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 倒数第一跳：解密得到真实接收端，转发；选择下下跳（响应阶段第一跳） 加密真实源
    private void processLastHopPkt(Node node, MessageAC msg) {
        try {
            // 解密
            byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedNextHopKey);
            msg.body = cryption.aesCTRDecryptProcess(msg.body, aesKey);
            msg.encryptedRealDest = cryption.aesCTRDecryptProcess(msg.encryptedRealDest, aesKey);
            msg.destID = new BigInteger(msg.encryptedRealDest);
            msg.srcID = nodeId;

            // 将下一跳的aesKey置1 并加密
            byte[] next_aesKey = generateRealDestAesKey();
            int destIndex = nodeIdtoNodeIndex(msg.destID, biJump_pid);
            if (destIndex < 0) {
                System.out.println("<<<<<<<<<<<<<<<<<<<< RealDestID: " + msg.destID + " Index: " + destIndex + ">>>>>>>>>>>>>>>>>>>>>>>");
                System.out.printf("time:%-16d; msg.id:%-16d; msg.ackID:%-16d; srcID:%-16d; destID:%-16d; nextHopID:%-16d\n",
                        CommonState.getTime(),
                        msg.id,
                        msg.ackID,
                        nodeIdtoNodeIndex(msg.srcID, biJump_pid),
                        nodeIdtoNodeIndex(msg.destID, biJump_pid),
                        nodeIdtoNodeIndex(msg.nextHopID, biJump_pid));
            }
            msg.encryptedNextHopKey = cryption.encryptWithPublicKey(getPublicKeyOfNode(nodeIdtoNodeIndex(msg.destID, biJump_pid)), next_aesKey);


            // 选择下下跳，加密真实源
            int next2HopIndex = getRandNodeIndex(node);
            PublicKey next2HopPublicKey = getPublicKeyOfNode(next2HopIndex);
            byte[] next2_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(next_aesKey);
            msg.encryptedRealSrc = cryption.aesCTREncryptProcess(msg.encryptedRealSrc, next2_aesKey);
            msg.encryptedNext2HopKey = cryption.encryptWithPublicKey(next2HopPublicKey, next2_aesKey);
            msg.nextHopID = nodeIndexToId(next2HopIndex);
            // 发送
            sendMessage(msg, msg.destID, biJump_pid, true, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 处理 body，请求 -> 响应：逆序body
    // 请求：1 + body； 响应：2 + body
    private byte[] processBody(MessageAC msg) {
        if (msg.body[0] == 2) {
            // 刷新时间 + 打印ackID
//            System.out.print("\r\033[K" + String.format("%.1f", CommonState.getTime()/ 1000.0) + "s, ACKID: " + msg.ackID);
            // 打印
//            System.out.println("Body: " + new String(msg.body, StandardCharsets.UTF_8));
            return null;
        } else if (msg.body[0] == 1) {
            byte[] reversedBody = new byte[msg.body.length];
            for (int i = 1; i < msg.body.length; i++) {
                reversedBody[i] = msg.body[msg.body.length - i - 1];
            }
            reversedBody[0] = 2;
            return reversedBody;
        }
        else {
            System.out.println("Invalid body");
        }
        return null;
    }

    // 根据body判断是请求还是响应
    private boolean isRequest(byte[] body) {
        return body[0] == 1;
    }
    private boolean isResponse(byte[] body) {
        return body[0] == 2;
    }

    // 接收端：响应，且切换源和目的（加密的目的）
    private void processRequestPkt(Node node, MessageAC msg) {
        try {
            if (isResponse(msg.body)) {
                processBody(msg);
            }
            if (isRequest(msg.body)) {
//                System.out.println(msg.toString());

                // 新建响应消息
                MessageAC responseMsg = new MessageAC();
                responseMsg.ackID = msg.id;
                responseMsg.isInitiator = false;

                // 解密一次真实源
                byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedRealDestKey);
                msg.encryptedRealSrc = cryption.aesCTRDecryptProcess(msg.encryptedRealSrc, aesKey);
//                System.out.println(msg.toString());
                responseMsg.encryptedRealDest = msg.encryptedRealSrc;

                // 随机生成加密的真实源和真实目的地，并不使用
                responseMsg.encryptedRealSrc = new byte[msg.encryptedRealDest.length];
                responseMsg.encryptedRealDestKey = new byte[msg.encryptedNextHopKey.length];
                secureRandom.nextBytes(responseMsg.encryptedRealSrc);
                secureRandom.nextBytes(responseMsg.encryptedRealDestKey);


                // 更新下一跳密钥
                responseMsg.encryptedNextHopKey = msg.encryptedNext2HopKey;

                int next2HopIndex = getRandNodeIndex(node);
                PublicKey next2HopPublicKey = getPublicKeyOfNode(next2HopIndex);
                byte[] next2_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
                secureRandom.nextBytes(next2_aesKey);
//                System.out.println("responseMsg encryptedRealDest: " + MessageAC.bytesToHex(responseMsg.encryptedRealDest));
                responseMsg.encryptedRealDest = cryption.aesCTREncryptProcess(responseMsg.encryptedRealDest, next2_aesKey);

                responseMsg.encryptedRealDestKey = cryption.aesCTREncryptProcess(msg.encryptedRealDest, next2_aesKey);
                responseMsg.encryptedNext2HopKey = cryption.encryptWithPublicKey(next2HopPublicKey, next2_aesKey);

                // 其他字段赋值
                responseMsg.srcID = nodeId;
                responseMsg.destID = msg.nextHopID;
                responseMsg.nextHopID = nodeIndexToId(next2HopIndex);
                // 消息体处理
                responseMsg.body = processBody(msg);
                if (responseMsg.body == null) {
                    System.out.println("Request body is invalid");
                    return;
                }
                responseMsg.body = cryption.aesCTREncryptProcess(responseMsg.body, next2_aesKey);
                responseMsg.isResponseFirstHop = true;
                // 发送
//                System.out.println(responseMsg.toString());

                sendMessage(responseMsg, responseMsg.destID, biJump_pid, false, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 响应第一跳：不解密body，加密
    private void processResponseFirstHopPkt(Node node, MessageAC msg) {
        try {
            msg.isResponseFirstHop = false;
            // 解密一次realdest
            byte[] aesKey = cryption.decryptWithPrivateKey(msg.encryptedNextHopKey);
            msg.encryptedRealDest = cryption.aesCTRDecryptProcess(msg.encryptedRealDest, aesKey);

            // 更新下一跳密钥
            msg.encryptedNextHopKey = msg.encryptedNext2HopKey;

            // 下下跳aesKey 加密 msg.body、msg.encryptedRealDest
            int next2HopIndex = getRandNodeIndex(node);
            PublicKey next2HopPublicKey = getPublicKeyOfNode(next2HopIndex);
            byte[] next2_aesKey = new byte[Cryption.AES_KEY_SIZE + Cryption.AES_CTR_IV_SIZE];
            secureRandom.nextBytes(next2_aesKey);
            msg.encryptedRealDest = cryption.aesCTREncryptProcess(msg.encryptedRealDest, next2_aesKey);
            msg.body = cryption.aesCTREncryptProcess(msg.body, next2_aesKey);
            msg.encryptedNext2HopKey = cryption.encryptWithPublicKey(next2HopPublicKey, next2_aesKey);

            // 其他字段赋值
            msg.srcID = nodeId;
            msg.destID = msg.nextHopID;
            msg.nextHopID = nodeIndexToId(next2HopIndex);
            // 发送
            sendMessage(msg, msg.destID, biJump_pid, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /* 核心方法，处理事件 */
    public void processEvent (Node node, int pid, Object event) {
        if (event instanceof MessageAC msg) {
            processPkt(node, msg);
        }
    }

    private void sendMessage(MessageAC msg, BigInteger destId, int myPid, boolean isLastHop, boolean isLast2Hop) {
        Node srcNode = nodeIdtoNode(nodeId, myPid);
        Node destNode = nodeIdtoNode(destId, myPid);
        transport = (UnreliableTransport) srcNode.getProtocol(transport_pid);
        transport.send(srcNode, destNode, msg, myPid);
        record.recordTxHop(msg, isLastHop, isLast2Hop, myPid);
    }


}
