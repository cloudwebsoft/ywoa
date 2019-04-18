package com.redmoon.oa.tigase;

import java.util.*;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;

import cn.js.fan.web.Global;

/**
 * @Description:
 * @author:
 * @Date: 2016-6-7上午11:24:45
 */
public class SyncManager {

	private Connection connection;
	private SyncPacket sync;

	public SyncManager(Connection connection) {
		sync = new SyncPacket();
		this.connection = connection;
	}

	public void syncDept(Map<String, String> map) throws XMPPException {
		sync.setType(IQ.Type.SET);
		sync.setAttributes(map);
		sendPacket();
	}

	public String getAllDepts() throws XMPPException {
		ProviderManager.getInstance().addIQProvider(
				SyncPacket.ELEMENTNAME, SyncPacket.NAMESPACE,
				new SyncProvider());
		sync.setType(IQ.Type.GET);
		sync.setTo(connection.getServiceName());
		IQ result = sendPacket();
		String res = result.getChildElementXML();
		System.out.println(res);
		return res;
	}

	public void removeDept(String code) throws XMPPException {
		sync.setType(IQ.Type.SET);
		Map<String, String> map = new HashMap<String, String>();
		map.put("remove", "");
		map.put("_id", code);
		sync.setAttributes(map);
		sendPacket();
	}

	public void removeAllDepts() throws XMPPException {
		sendSyncPacket("removeAll", "");
	}
	
	public void makeFriend(String realName) throws XMPPException {
		sendSyncPacket("friend", realName);
	}
	
	public void makeFriends() throws XMPPException {
		sendSyncPacket("friends", "");
	}
	
	public void setOAInfo() throws XMPPException {
		sendSyncPacket("url", Global.getFullRootPath());
	}
	
	private void sendSyncPacket(String key, String value) throws XMPPException {
		sync.setType(IQ.Type.SET);
		Map<String, String> map = new HashMap<String, String>();
		map.put(key, value);
		sync.setAttributes(map);
		sendPacket();
	}
	
	private IQ sendPacket() throws XMPPException {
		sync.setTo(connection.getServiceName());
		PacketFilter filter = new AndFilter(new PacketFilter[] {
				new PacketIDFilter(sync.getPacketID()),
				new PacketTypeFilter(IQ.class) });
		PacketCollector collector = connection.createPacketCollector(filter);
		connection.sendPacket(sync);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		if (result == null) {
			throw new XMPPException("No response from server.");
		}
		if (result.getType() == IQ.Type.ERROR) {
			throw new XMPPException(result.getError());
		}
		return result;
	}
}
