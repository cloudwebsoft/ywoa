package com.redmoon.oa.netdisk;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

public class NetDiskSideBar {
	/**
	 * 获取侧边栏图片属性数据
	 * 
	 * @return
	 */
	transient Logger logger = Logger.getLogger(NetDiskSideBar.class.getName());

	/**
	 * 获取消息主题信息
	 * 
	 * @return
	 */
	public JSONArray getMsgNoticeTopic(String userName, int count) {
		JSONArray messageArr = new JSONArray();
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id,title,send_time from oa_message where receiver = "
				+ StrUtil.sqlstr(userName)
				+ " and box="
				+ MessageDb.INBOX
				+ " and isreaded = 0 and is_dustbin=0  and type<>10 "
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
						+ count
						: "")
				+ " order by send_time desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
						+ count : "");
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int id = record.getInt("id");
				String title = record.getString("title");
				Date date = record.getDate("send_time");
				String authKey = userName + "|"
						+ DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
				// com.redmoon.oa.sso.Config cfg = new
				// com.redmoon.oa.sso.Config();
				// authKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg
				// .get("key"), authKey);
				authKey = cn.js.fan.security.ThreeDesUtil
						.encrypt2hex(
								com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY,
								authKey);
				JSONObject messageObj = new JSONObject();
				messageObj.put("id", id);
				messageObj.put("title", title);
				messageObj.put("date", DateUtil.format(date, "MM-dd HH:mm"));
				messageObj.put("authKey", authKey);
				messageArr.add(messageObj);
			}
		} catch (SQLException e) {
			logger.error("getMsgNoticeTopic:" + e.getMessage());
		}
		return messageArr;
	}

	/**
	 * 获取消息主题信息 静态页面使用
	 * 
	 * @return
	 */
	public Vector<Integer> getMsgNoticeTopicByStatic(String userName, int count) {
		Vector<Integer> messageArr = new Vector<Integer>();
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id,title,send_time from oa_message where receiver = "
				+ StrUtil.sqlstr(userName)
				+ " and box="
				+ MessageDb.INBOX
				+ " and isreaded = 0 and is_dustbin=0  and type<>10 "
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
						+ count
						: "")
				+ " order by send_time desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
						+ count : "");
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int id = record.getInt("id");
				messageArr.add(id);
			}
		} catch (SQLException e) {
			logger.error("getMsgNoticeTopic:" + e.getMessage());
		}
		return messageArr;
	}

	/**
	 * 获取流程主题信息
	 * 
	 * @return
	 */
	public JSONArray getFlowNoticeTopic(String userName, int count) {
		JSONArray flowArr = new JSONArray();
		JdbcTemplate jt = new JdbcTemplate();
		int id = 0;
		JSONObject flowObj = new JSONObject();
		String sql = "select m.id,f.title,m.receive_date from flow_my_action m, flow f where m.flow_id=f.id and f.status<>"
				+ WorkflowDb.STATUS_NONE
				+ " and (user_name="
				+ StrUtil.sqlstr(userName)
				+ " or proxy="
				+ StrUtil.sqlstr(userName)
				+ ") and (is_checked=0 or is_checked=2) and sub_my_action_id="
				+ MyActionDb.SUB_MYACTION_ID_NONE
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
						+ count
						: "")
				+ " order by receive_date desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
						+ count : "");
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				id = record.getInt("id");
				String title = record.getString("title");
				Date date = record.getDate("receive_date");
				String authKey = userName + "|"
						+ DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
				// com.redmoon.oa.sso.Config cfg = new
				// com.redmoon.oa.sso.Config();
				// authKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg
				// .get("key"), authKey);
				authKey = cn.js.fan.security.ThreeDesUtil
						.encrypt2hex(
								com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY,
								authKey);
				flowObj.put("id", id);
				flowObj.put("title", title);
				flowObj.put("date", DateUtil.format(date, "MM-dd HH:mm"));
				flowObj.put("authKey", authKey);
				flowArr.add(flowObj);
			}
		} catch (SQLException e) {
			logger.error("getFlowNoticeTopic:" + e.getMessage());
		}
		return flowArr;
	}

	/**
	 * 获取流程主题信息 静态页面使用
	 * 
	 * @return
	 */
	public Vector<Integer> getFlowNoticeTopicByStatic(String userName, int count) {
		Vector<Integer> flowArr = new Vector<Integer>();
		JdbcTemplate jt = new JdbcTemplate();
		int id = 0;
		String sql = "select f.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>"
				+ WorkflowDb.STATUS_NONE
				+ " and (user_name="
				+ StrUtil.sqlstr(userName)
				+ " or proxy="
				+ StrUtil.sqlstr(userName)
				+ ") and (is_checked=0 or is_checked=2) and sub_my_action_id="
				+ MyActionDb.SUB_MYACTION_ID_NONE
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
						+ count
						: "")
				+ " order by receive_date desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
						+ count : "");
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				id = record.getInt(1);
				flowArr.add(id);
			}
		} catch (SQLException e) {
			logger.error("getFlowNoticeTopic:" + e.getMessage());
		}
		return flowArr;
	}

	/**
	 * 返回所有协作动态的 json数据
	 * 
	 * @return
	 */
	public JSONObject querySideBarLogByAjax(String userName) {
		SideBarMgr sbMgr = new SideBarMgr();
		List<SideBarBean> list = sbMgr.querySideBar(userName);
		String topic = sbMgr.getTopicString(userName);

		String[] topics = null;

		if (topic.equals(",")) {
			topics = null;
		} else if (topic.startsWith(",")) {
			topics = new String[] { topic.substring(1) };
		} else if (topic.endsWith(",")) {
			topics = new String[] { topic.substring(0, topic.length() - 1) };
		} else {
			topics = StrUtil.split(topic, ",");
		}

		JSONArray sideBarArray = JSONArray.fromObject(list);
		JSONArray sideBarMsgTopic = null;
		JSONArray sideBarFlowTopic = null;

		if (topics == null) {
			sideBarMsgTopic = new JSONArray();
			sideBarFlowTopic = new JSONArray();
		} else {
			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			int flowCount = cfg.getIntProperty("sidebar_flow_count");
			int msgCount = cfg.getIntProperty("sidebar_msg_count");
			int maxCount = flowCount + msgCount;

			if (topics.length == 1) {
				if (topics[0].equals("flowNotice")) {
					sideBarFlowTopic = getFlowNoticeTopic(userName, maxCount);
					sideBarMsgTopic = new JSONArray();
				} else if (topics[0].equals("msgNotice")) {
					sideBarMsgTopic = getMsgNoticeTopic(userName, maxCount);
					sideBarFlowTopic = new JSONArray();
				}
			} else if (topics.length == 2) {
				sideBarFlowTopic = getFlowNoticeTopic(userName, flowCount);
				sideBarMsgTopic = getMsgNoticeTopic(userName, msgCount);
			} else {
				sideBarMsgTopic = new JSONArray();
				sideBarFlowTopic = new JSONArray();
			}
		}

		// 取得待办流程的条数
		int flowWaitCount = WorkflowDb.getWaitCount(userName);
		// 内部邮件的数目
		MessageDb md = new MessageDb();
		int msgNewCount = md.getNewInnerMsgCount(userName);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ret", "1");
		// jsonObject.put("total",list.size());
		jsonObject.put("num", sideBarFlowTopic.size());
		jsonObject.put("sideBarArray", sideBarArray);
		jsonObject.put("sideBarMsgTopic", sideBarMsgTopic);
		jsonObject.put("sideBarFlowTopic", sideBarFlowTopic);
		jsonObject.put("flowWaitCount", flowWaitCount);
		jsonObject.put("innerMsgCount", msgNewCount);

		return jsonObject;

	}
}
