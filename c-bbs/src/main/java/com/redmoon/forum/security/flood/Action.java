package com.redmoon.forum.security.flood;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import cn.js.fan.util.DateUtil;

public class Action {
	private int actionsMaxPerHour = 0; // 缺省情况下，对每小时的动作数不作限制。

	private Map ipMap = new TreeMap();

	private long lastRemoveTime = 0;

	void setActionsPerHour(int actionsMaxPerHour) {
		if (actionsMaxPerHour >= 0) {
			this.actionsMaxPerHour = actionsMaxPerHour;
		}
	}
	
	public Map getIPMap() {
		return ipMap;
	}

	public int getActionsMaxPerHour() {
		return actionsMaxPerHour;
	}

	boolean isReachMax(String strIP) {
		removeTimeoutIP();
		return getActionIP(strIP).isReachMax();
	}

	void increaseCount(String ip, String uri) {
		removeTimeoutIP();
		getActionIP(ip).increaseCount(ip, uri);
	}

	void resetAction(String ip) {
		removeTimeoutIP();
		getActionIP(ip).resetAction();
	}

	private synchronized ActionIP getActionIP(String ip) {
		ActionIP actionIP = (ActionIP) ipMap.get(ip);
		if (actionIP == null) {
			actionIP = new ActionIP(ip, actionsMaxPerHour);
			ipMap.put(ip, actionIP);
		} else {
			// there is a ControlledIP, update the actionsPerHour
			actionIP.setActionsPerHour(actionsMaxPerHour);
		}
		return actionIP;
	}

	/**
	 * 删除最近一小时没有访问的IP
	 *
	 */
	private synchronized void removeTimeoutIP() {
		long now = System.currentTimeMillis();
		if ((now - lastRemoveTime) > FloodMonitor.REMOVE_INTERVAL) {
			lastRemoveTime = now;
			Collection ipList = ipMap.values();
			for (Iterator ir = ipList.iterator(); ir.hasNext();) {
				ActionIP curIP = (ActionIP) ir.next();
				if (now - curIP.getLastIncrementTime() > DateUtil.HOUR) {
					ir.remove();
				}
			}
		}
	}
}
