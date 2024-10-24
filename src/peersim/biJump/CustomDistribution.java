package peersim.biJump;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;

/**
 * This control initializes the whole network (that was already created by peersim) assigning a unique NodeId, randomly generated,
 * to every node.
 * 
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class CustomDistribution implements Control {

	private static final String PAR_PROT = "protocol";

	private int protocolID;
	private UniformRandomGenerator urg;

	public CustomDistribution(String prefix) {
		protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
//		System.out.println("protocolID: " + protocolID);
		urg = new UniformRandomGenerator(BiJumpProtocol.NODE_ID_BITS, CommonState.r);
	}


	public boolean execute() {
		BigInteger tmp;


		for (int i = 0; i < Network.size(); ++i) {
			tmp = urg.generate();
			((BiJumpProtocol) (Network.get(i).getProtocol(protocolID))).biJump_pid = protocolID;
			((BiJumpProtocol) (Network.get(i).getProtocol(protocolID))).nodeId = tmp;
		}

		return false;
	}

}
