package com.redmoon.oa.tigase;

import java.util.*;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * @Description:
 * @author:古月圣
 * @Date: 2016-6-9下午08:14:52
 */

public class SyncProvider implements IQProvider {
	
	public final static String SET_MAP_KEY = "$set_item$";

	public SyncProvider() {
	}

	public IQ parseIQ(XmlPullParser parser) throws Exception {
		SyncPacket dp = new SyncPacket();
		// dp.addExtension(new DefaultPacketExtension(parser.getName(),
		// parser.getNamespace()));
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		boolean done = false;
		do {
			if (done) {
				break;
			}
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("item")) {
					Map<String, String> ret = parseItem(parser);
					if (ret != null && ret.containsKey("_id")) {
						map.put(ret.get("_id"), ret);
					}
				}
			} else if (eventType == XmlPullParser.END_TAG
					&& parser.getName().equals("query")) {
				done = true;
			}
		} while (true);
		map.put(SET_MAP_KEY, null);
		dp.setAttributes(map);
		return dp;
	}

	public Map<String, String> parseItem(XmlPullParser parser) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		boolean done = false;
		do {
			if (done) {
				break;
			}
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("_id")
						|| parser.getName().equals("name")
						|| parser.getName().equals("parent_id")
						|| parser.getName().equals("layer")
						|| parser.getName().equals("order")) {
					map.put(parser.getName(), parser.nextText());
				}
			} else if (eventType == XmlPullParser.END_TAG
					&& parser.getName().equals("item")) {
				done = true;
			}
		} while (true);
		return map;
	}
}
