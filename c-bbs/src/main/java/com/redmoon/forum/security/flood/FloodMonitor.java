package com.redmoon.forum.security.flood;

import java.util.*;
import cn.js.fan.util.DateUtil;

public class FloodMonitor {

	public final static int FLOOD_HTTP_REQUEST = 0;
	public final static int FLOOD_LOGIN_FAIL = 1;
	
	private static Map actionMap = new TreeMap();

	static final long REMOVE_INTERVAL = 2 * DateUtil.MINUTE; // 2 minutes
	
	static {
		setActionMaxOption(new Integer(FLOOD_HTTP_REQUEST), FloodConfig.getInstance().getIntProperty("flood.max_http_request_per_hour"));
	}

	private FloodMonitor() {
	}

	/**
	 * 置动作数量每小时的最大值
	 * @param action Integer 动作
	 * @param actionsPerHour int 动作每小时的最大值
	 */
	public static void setActionMaxOption(Integer action, int actionsMaxPerHour) {
		getAction(action).setActionsPerHour(actionsMaxPerHour);
	}
	
	public static Map getActionMap() {
		return actionMap;
	}

	/**
	 * 取得某个动作每小时的最大访问量
	 * @param action
	 * @return
	 */
	public static int getActionsPerHour(Integer action) {
		return getAction(action).getActionsMaxPerHour();
	}

	/**
	 * 检查某IP的某种动作是否已达到最大值
	 * @param action Integer 动作
	 * @param strIP String IP
	 * @return boolean true
	 */
	public static boolean isReachMax(Integer action, String strIP) {
		return getAction(action).isReachMax(strIP);
	}

	/**
	 * 增长IP的动作数
	 * @param action Integer the action to increase the number of times
	 * @param strIP String the IP to increase the number of times
	 */
	public static void increaseCount(Integer action, String strIP, String uri) {
		getAction(action).increaseCount(strIP, uri);
	}

	/**
	 * 重置IP的动作
	 * @param action Integer
	 * @param strIP String
	 */
	public static void resetAction(Integer action, String strIP) {
		getAction(action).resetAction(strIP);
	}

	/**
	 * 获得某一动作
	 * @param action Integer
	 * @return ControlledAction
	 */
	public static synchronized Action getAction(
			Integer action) {
		Action act = (Action)actionMap.get(action);
		if (act == null) {
			act = new Action();
			actionMap.put(action, act);
		}
		return act;
	}
}
