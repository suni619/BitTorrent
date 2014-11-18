package com.ase.tracker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BencodeUtil {

	public static String bencode(Map<String, Object> responseMap) {
		// TODO Auto-generated method stub
		StringBuilder bencodedResponse = new StringBuilder();
		
		bencodedResponse.append("d");
		bencodedResponse.append("failureReason".length() + ":failureReason");
		bencodedResponse.append(((String)responseMap.get("failureReason")).length() + ":" + (String)responseMap.get("failureReason"));
		bencodedResponse.append("warningMessage".length() + ":warningMessage");
		bencodedResponse.append(((String)responseMap.get("warningMessage")).length() + ":" + (String)responseMap.get("warningMessage"));
		bencodedResponse.append("interval".length() + ":interval");
		bencodedResponse.append(((String)responseMap.get("interval")).length() + ":" + (String)responseMap.get("interval"));
		bencodedResponse.append("complete".length() + ":complete");
		bencodedResponse.append(((String)responseMap.get("complete")).length() + ":" + (String)responseMap.get("complete"));
		bencodedResponse.append("incomplete".length() + ":incomplete");
		bencodedResponse.append(((String)responseMap.get("incomplete")).length() + ":" + (String)responseMap.get("incomplete"));
		
		bencodedResponse.append("peers".length() + ":peers");
		bencodedResponse.append("l");
		List<Object> peersList = (List<Object>) responseMap.get("peers");
		for (Object object : peersList) {
			bencodedResponse.append("d");
			
			Map<String, Object> peerMap = (Map<String, Object>) object;
			bencodedResponse.append("peer_id".length() + ":peer_id");
			bencodedResponse.append(((String)peerMap.get("peer_id")).length() + ":" + (String)peerMap.get("peer_id"));
			bencodedResponse.append("ip".length() + ":ip");
			bencodedResponse.append(((String)peerMap.get("ip")).length() + ":" + (String)peerMap.get("ip"));
			bencodedResponse.append("port".length() + ":port");
			bencodedResponse.append(((String)peerMap.get("port")).length() + ":" + (String)peerMap.get("port"));
			
			bencodedResponse.append("e");
		}
		// end list
		bencodedResponse.append("e");
		// end dictionary
		bencodedResponse.append("e");
		
		return bencodedResponse.toString();
	}

}
