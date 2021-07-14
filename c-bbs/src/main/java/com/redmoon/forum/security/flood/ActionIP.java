package com.redmoon.forum.security.flood;

import java.util.ArrayList;
import java.util.Iterator;

import cn.js.fan.util.DateUtil;

public class ActionIP {
	private int actionsMaxPerHour = 0; // 缺省不作限制。

	private long lastRemoveTime = 0; // 最后一次删除的时间

	private long lastIncrementTime = 0; // 最后一次增长的时间

	private ArrayList actionHistoryList = new ArrayList();
	
	private String ip;
	
	ActionIP(String ip, int actionsMaxPerHour) {
		this.ip = ip;
		if (actionsMaxPerHour >= 0) {
			this.actionsMaxPerHour = actionsMaxPerHour;
		}
	}

	void setActionsPerHour(int actionsMaxPerHour) {
		if (actionsMaxPerHour >= 0) {
			this.actionsMaxPerHour = actionsMaxPerHour;
		}
	}

	public long getLastIncrementTime() {
		return lastIncrementTime;
	}
	
	public ArrayList getActionHistoryList() {
		return actionHistoryList;
	}

	void increaseCount(String ip, String uri) {
		long now = System.currentTimeMillis();
		lastIncrementTime = now;
		ActionHistory ah = new ActionHistory();
		ah.setTime(now);
		ah.setUri(uri);
		
		actionHistoryList.add(0, ah);
	}

	void resetAction() {
		lastRemoveTime = 0;
		lastIncrementTime = 0;
		actionHistoryList.clear();
	}

	boolean isReachMax() {
		if (actionsMaxPerHour == 0) {//unlimited 没有限制。
			return false;
		}
		if (actionHistoryList.size() < actionsMaxPerHour) {
			return false;
		}
		// now try to remove timeout actions
		removeTimeoutActions();
		return (actionHistoryList.size() >= actionsMaxPerHour);
	}

	private synchronized void removeTimeoutActions() {
		long now = System.currentTimeMillis();
		if (now - lastRemoveTime > FloodMonitor.REMOVE_INTERVAL) {
			lastRemoveTime = now;
			for (Iterator ir = actionHistoryList.iterator(); ir.hasNext();) {
				ActionHistory ah = (ActionHistory) ir.next();
				// 删除超出一个小时的动作历史记录
				if ((now - ah.getTime()) > DateUtil.HOUR) {
					ir.remove();
				}
			}
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
