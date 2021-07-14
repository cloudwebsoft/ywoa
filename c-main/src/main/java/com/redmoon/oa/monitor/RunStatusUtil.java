package com.redmoon.oa.monitor;

import org.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpStatus;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.http.HTTPException;
import org.json.JSONException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.netdisk.UtilTools;
import com.redmoon.oa.person.UserDb;
import com.runqian.report4.dataset.Group;

public class RunStatusUtil {
	/**
	 * 登录次数
	 */
	public static final String loginCount = "loginCount";
	/**
	 * 网盘文件数
	 */
	public static final String netDiskFileCount = "netDiskFileCount";
	/**
	 * 网盘可用空间
	 */
	public static final String netDiskSpace = "netDiskSpace";
	/**
	 * 已使用网盘空间
	 */
	public static final String netDiskUserSpace = "netDiskUserSpace";
	/**
	 * 磁盘可用空间
	 */
	public static final String DiskSpace = "diskSpace";
	/**
	 * 已使用磁盘空间
	 */
	public static final String DiskUseredSpace = "diskUserSpace";
	/**
	 * 文档数
	 */
	public static final String documentCount = "documentCount";
	/**
	 * 流程数
	 */
	public static final String flowCount = "flowCount";
	/**
	 * 短消息数
	 */
	public static final String messageCount = "messageCount";
	/**
	 * 通知数
	 */
	public static final String noticeCount = "noticeCount";
	/**
	 * 手机登录次数
	 */
	public static final String mobileAccessCount = "mobileAccessCount";
	/**
	 * 用户数
	 */
	public static final String userCount = "userCount";
	/**
	 * 论坛帖数
	 */
	public static final String forumCount = "forumCount";
	/**
	 * 公共共享文件夹文件数
	 */
	public static final String shareFileCount = "shareFileCount";
	/**
	 * 日程安排数
	 */
	public static final String scheduleCount = "scheduleCount";
	/**
	 * 项目数
	 */
	public static final String projectCount = "projectCount";
	/**
	 * 工作计划数
	 */
	public static final String workPlanCount = "workPlanCount";
	/**
	 * 工作记事数
	 */
	public static final String workNoteCount = "workNoteCount";
	/**
	 * 部门数
	 */
	public static final String departmentCount = "departmentCount";
	/**
	 * 最后登录时间
	 */
	public static final String lastLoginDate = "lastLoginDate";

	public static final String locationCount = "locationCount";
	/**
	 * 活跃用户数
	 */
	public static final String activeUserCount = "activeUserCount";
	/**
	 * 生成运行状态过程或者远程获取过程中，是否存在错误 
	 */
	public static final String error = "error";
	/**
	 * 生成运行状态过程或者远程获取过程中，错误信息
	 */
	public static final String errorMessage = "errorMessage";
	/**
	 * 日期格式
	 */
	public static final String dateFormatString = "yyyy-MM-dd";
	/**
	 * 时间格式
	 */
	public static final String timeFormatString = "HH:mm:ss";
	/**
	 * 日期时间格式
	 */
	public static final String dateTimeFormatString = dateFormatString + " "
			+ timeFormatString;

	/**
	 * 封装用于页面显示的JSON对象，似乎无用
	 * 
	 * @param params
	 * @return
	 */
	public JSONObject createReturnJson(Map<String, Object> params) {
		JSONObject returnJson = new JSONObject();

		try {
			returnJson.put(RunStatusUtil.loginCount, params
					.get(RunStatusUtil.loginCount));
			returnJson.put(RunStatusUtil.netDiskFileCount, params
					.get(RunStatusUtil.netDiskFileCount));
			returnJson.put(RunStatusUtil.netDiskSpace, params
					.get(RunStatusUtil.netDiskSpace));
			returnJson.put(RunStatusUtil.netDiskUserSpace, params
					.get(RunStatusUtil.netDiskUserSpace));
			returnJson.put(RunStatusUtil.DiskSpace, params
					.get(RunStatusUtil.DiskSpace));
			returnJson.put(RunStatusUtil.DiskUseredSpace, params
					.get(RunStatusUtil.DiskUseredSpace));
			returnJson.put(RunStatusUtil.documentCount, params
					.get(RunStatusUtil.documentCount));
			returnJson.put(RunStatusUtil.flowCount, params
					.get(RunStatusUtil.flowCount));
			returnJson.put(RunStatusUtil.messageCount, params
					.get(RunStatusUtil.messageCount));
			returnJson.put(RunStatusUtil.noticeCount, params
					.get(RunStatusUtil.noticeCount));
			returnJson.put(RunStatusUtil.mobileAccessCount, params
					.get(RunStatusUtil.mobileAccessCount));
			returnJson.put(RunStatusUtil.userCount, params
					.get(RunStatusUtil.userCount));
			returnJson
					.put(RunStatusUtil.error, params.get(RunStatusUtil.error));
			returnJson.put(RunStatusUtil.errorMessage, params
					.get(RunStatusUtil.errorMessage));
			returnJson.put(RunStatusUtil.forumCount, params
					.get(RunStatusUtil.forumCount));
			returnJson.put(RunStatusUtil.shareFileCount, params
					.get(RunStatusUtil.shareFileCount));
			returnJson.put(RunStatusUtil.scheduleCount, params
					.get(RunStatusUtil.scheduleCount));
			returnJson.put(RunStatusUtil.workPlanCount, params
					.get(RunStatusUtil.workPlanCount));
			returnJson.put(RunStatusUtil.projectCount, params
					.get(RunStatusUtil.projectCount));
			returnJson.put(RunStatusUtil.workNoteCount, params
					.get(RunStatusUtil.workNoteCount));
			returnJson.put(RunStatusUtil.departmentCount, params
					.get(RunStatusUtil.departmentCount));
			returnJson.put(RunStatusUtil.lastLoginDate, DateUtil.format(
					(Date) params.get(RunStatusUtil.lastLoginDate),
					dateTimeFormatString));
			returnJson.put(RunStatusUtil.locationCount, params
					.get(RunStatusUtil.locationCount));
			returnJson.put(RunStatusUtil.activeUserCount, params
					.get(RunStatusUtil.activeUserCount));
			
			return returnJson;
		} catch (JSONException e) {
			return null;
		}
	}

	/**
	 * 创建默认的统计信息 --- 该信息所有统计给过数 都为-1
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createDefaultJSONObject() throws JSONException {
		JSONObject returnJson = new JSONObject();
		returnJson.put(RunStatusUtil.loginCount, -1);
		returnJson.put(RunStatusUtil.netDiskFileCount, -1);
		returnJson.put(RunStatusUtil.netDiskSpace, "");
		returnJson.put(RunStatusUtil.netDiskUserSpace, "");
		returnJson.put(RunStatusUtil.DiskSpace, "");
		returnJson.put(RunStatusUtil.DiskUseredSpace, "");
		returnJson.put(RunStatusUtil.documentCount, -1);
		returnJson.put(RunStatusUtil.flowCount, -1);
		returnJson.put(RunStatusUtil.messageCount, -1);
		returnJson.put(RunStatusUtil.noticeCount, -1);
		returnJson.put(RunStatusUtil.mobileAccessCount, -1);
		returnJson.put(RunStatusUtil.userCount, -1);
		returnJson.put(RunStatusUtil.forumCount, -1);
		returnJson.put(RunStatusUtil.shareFileCount, -1);
		returnJson.put(RunStatusUtil.scheduleCount, -1);
		returnJson.put(RunStatusUtil.workPlanCount, -1);
		returnJson.put(RunStatusUtil.projectCount, -1);
		returnJson.put(RunStatusUtil.workNoteCount, -1);
		returnJson.put(RunStatusUtil.departmentCount, -1);
		returnJson.put(RunStatusUtil.lastLoginDate, new Date(0));
		returnJson.put(RunStatusUtil.locationCount, -1);
		returnJson.put(RunStatusUtil.activeUserCount, -1);
		return returnJson;
	}

	/**
	 * 执行查询SQL
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	private int calCount(String sql) throws SQLException {
		int returnValue = -1;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;

		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			returnValue = rr.getInt(1);
		}

		return returnValue;
	}

	/**
	 * 统计论坛帖数
	 * 
	 * @return
	 * @throws SQLException
	 */
	private int statForumCount(Date startDate, Date endDate)
			throws SQLException {
		// 计算登录次数
		StringBuilder loginSql = new StringBuilder(
				"select count(*) from sq_message where 1=1 ");
		if (startDate != null) {
			loginSql.append(" and lydate>=" + startDate.getTime());
		}
		if (endDate != null) {
			loginSql.append(" and lydate<=" + endDate.getTime());
		}

		int returnValue = calCount(loginSql.toString());
		return returnValue;
	}

	/**
	 * 统计登录次数
	 * 
	 * @return
	 * @throws SQLException
	 */
	private int statLogin(Date startDate, Date endDate) throws SQLException {
		// 计算登录次数
		StringBuilder loginSql = new StringBuilder(
				"select count(*) from log where action='登录系统' ");
		if (startDate != null) {
			loginSql.append(" and log_date>=" + startDate.getTime());
		}
		if (endDate != null) {
			loginSql.append(" and log_date<=" + endDate.getTime());
		}

		int returnValue = calCount(loginSql.toString());
		return returnValue;
	}

	/**
	 * 统计系统使用状态 返回JSON 对象
	 * 
	 * @return
	 */
	public Map<String, Object> statUsedStatusToMap(Date startDate,
			Date endDate, String userName) {

		int loginCountValue = -1;
		int netDiskFileCountValue = -1;
		String netDiskSpaceValue = "";
		String netDiskUserSpaceValue = "";
		String diskSpaceValue = "";
		String diskUserdSpaceValue = "";
		int documentCountValue = -1;
		int flowCountValue = -1;
		int messageCountValue = -1;
		int noticeCountValue = -1;
		int mobileAccessCountValue = -1;
		int userCountValue = -1;
		int forumCount = -1;
		int shareFileCount = -1;
		int scheduleCount = -1;
		int workPlanCount = -1;
		int projectCount = -1;
		int workNoteCount = -1;
		int departmentCount = -1;
		int activeUserCount = -1;
		Date lastLoginDate = new Date(0);
		boolean errorValue = false;
		String errorMessageValue = "";

		int locationCount = -1;

		try {
			loginCountValue = statLogin(startDate, endDate);
			netDiskFileCountValue = statNetDiskFile(startDate, endDate);
			netDiskSpaceValue = statNetDiskSpace(userName);
			netDiskUserSpaceValue = statNetDiskUserSpace(userName);
			diskSpaceValue = statDiskSpace();
			diskUserdSpaceValue = statDiskUsedSpace();
			documentCountValue = statDocument(startDate, endDate);
			flowCountValue = statflow(startDate, endDate);
			messageCountValue = statMessage(startDate, endDate);
			noticeCountValue = statNotice(startDate, endDate);
			mobileAccessCountValue = statMobileAccess(startDate, endDate);
			userCountValue = statUser(startDate, endDate);
			forumCount = statForumCount(startDate, endDate);
			shareFileCount = statShareFileCount(startDate, endDate);
			scheduleCount = statScheduleCount(startDate, endDate);
			workPlanCount = statWorkPlanCount(startDate, endDate);
			projectCount = statProjectCount(startDate, endDate);
			workNoteCount = statWorkNoteCount(startDate, endDate);
			departmentCount = statDepartmentCount(startDate, endDate);
			lastLoginDate = getLastLoginDate();
			locationCount = statLocationCount(startDate, endDate);
			activeUserCount = statActiveUserCount(startDate, endDate);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorValue = true;
			errorMessageValue = "数据库错误：" + e.getMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errorValue = true;
			errorMessageValue = "统计错误：" + e.getMessage();
		}
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put(RunStatusUtil.loginCount, loginCountValue);
		jsonMap.put(RunStatusUtil.netDiskFileCount, netDiskFileCountValue);
		jsonMap.put(RunStatusUtil.netDiskSpace, netDiskSpaceValue);
		jsonMap.put(RunStatusUtil.netDiskUserSpace, netDiskUserSpaceValue);
		jsonMap.put(RunStatusUtil.DiskSpace, diskSpaceValue);
		jsonMap.put(RunStatusUtil.DiskUseredSpace, diskUserdSpaceValue);
		jsonMap.put(RunStatusUtil.documentCount, documentCountValue);
		jsonMap.put(RunStatusUtil.flowCount, flowCountValue);
		jsonMap.put(RunStatusUtil.messageCount, messageCountValue);
		jsonMap.put(RunStatusUtil.noticeCount, noticeCountValue);
		jsonMap.put(RunStatusUtil.mobileAccessCount, mobileAccessCountValue);
		jsonMap.put(RunStatusUtil.userCount, userCountValue);
		jsonMap.put(RunStatusUtil.forumCount, forumCount);
		jsonMap.put(RunStatusUtil.shareFileCount, shareFileCount);
		jsonMap.put(RunStatusUtil.scheduleCount, scheduleCount);
		jsonMap.put(RunStatusUtil.workPlanCount, workPlanCount);
		jsonMap.put(RunStatusUtil.projectCount, projectCount);
		jsonMap.put(RunStatusUtil.workNoteCount, workNoteCount);
		jsonMap.put(RunStatusUtil.departmentCount, departmentCount);
		jsonMap.put(RunStatusUtil.lastLoginDate, lastLoginDate);
		jsonMap.put(RunStatusUtil.locationCount, locationCount);
		jsonMap.put(RunStatusUtil.error, errorValue);
		jsonMap.put(RunStatusUtil.errorMessage, errorMessageValue);
		jsonMap.put(RunStatusUtil.activeUserCount, activeUserCount);
		return jsonMap;
	}

	/**
	 * 获取当天硬盘已使用空间
	 * 
	 * @return
	 */
	private String statDiskUsedSpace() {
		com.redmoon.oa.android.CloudConfig ccfg = com.redmoon.oa.android.CloudConfig
				.getInstance();

		long diskSpace = ccfg.getIntProperty("diskSpace");
		if (diskSpace != -1) {
			// return
			// NumberUtil.round((double)ccfg.getIntProperty("diskSpaceUsed") /
			// 1024, 2) + "G";
			return UtilTools
					.getFileSize(ccfg.getIntProperty("diskSpaceUsed") * 1024 * 1024);
		}

		String realPath = Global.getRealPath();
		String returnValue = "";
		if (realPath.indexOf(":") == 1) {
			String disk = realPath.substring(0, 2);
			File file = new File(disk);
			// double freeSpace = (double)file.getFreeSpace()/1024000000;
			// double totalSpace = (double)file.getTotalSpace()/1024000000;
			// double usedSpace = totalSpace-freeSpace;
			// returnValue = NumberUtil.round(usedSpace, 2) + "G";
			returnValue = UtilTools.getFileSize(file.getTotalSpace()
					- file.getFreeSpace());
		}
		return returnValue;
	}

	/**
	 * 获取当前硬盘空间
	 * 
	 * @return
	 */
	private String statDiskSpace() {
		com.redmoon.oa.android.CloudConfig ccfg = com.redmoon.oa.android.CloudConfig
				.getInstance();

		long diskSpace = ccfg.getIntProperty("diskSpace");
		if (diskSpace != -1) {
			// return diskSpace / 1024 + "G";
			return UtilTools.getFileSize(diskSpace * 1024 * 1024);
		}

		String realPath = Global.getRealPath();
		String returnValue = "";
		if (realPath.indexOf(":") == 1) {
			String disk = realPath.substring(0, 2);
			File file = new File(disk);
			// double totalSpace = (double)file.getTotalSpace()/1024000000;
			// returnValue = NumberUtil.round(totalSpace, 2) + "G";
			returnValue = UtilTools.getFileSize(file.getTotalSpace());
		}
		return returnValue;
	}

	/**
	 * 得到最后登录时间
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Date getLastLoginDate() throws SQLException {
		String sql = "select LOG_DATE from log order by LOG_DATE desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql, 1, 1);
		long time = 0;
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			time = rr.getLong("LOG_DATE");
		}
		return new Date(time);
	}

	/**
	 * 统计部门数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statDepartmentCount(Date startDate, Date endDate)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				"select count(*) from department where 1=1 ");
		if (startDate != null) {
			sql.append(" and addDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));

		}
		if (endDate != null) {
			sql.append(" and addDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));

		}
		int returnValue = calCount(sql.toString());
		return returnValue;
	}

	/**
	 * 统计工作记事数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statWorkNoteCount(Date startDate, Date endDate)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				"select count(*) from work_log where 1=1 ");
		if (startDate != null) {
			sql.append(" and myDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));

		}
		if (endDate != null) {
			sql.append(" and myDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));

		}
		int returnValue = calCount(sql.toString());
		return returnValue;
	}

	/**
	 * 统计项目数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statProjectCount(Date startDate, Date endDate)
			throws SQLException {

		StringBuilder sql = new StringBuilder(
				"select count(*) from form_table_project where 1=1 ");
		if (startDate != null) {
			sql.append(" and (begin_date>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
			sql.append(" or end_date>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)) + ")");
		}
		if (endDate != null) {
			sql.append(" and (begin_date<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
			sql.append(" or end_date<="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)) + ")");
		}
		int returnValue = calCount(sql.toString());

		return returnValue;
	}

	/**
	 * 统计工作计划数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statWorkPlanCount(Date startDate, Date endDate)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				"select count(*) from work_plan where 1=1 ");
		if (startDate != null) {
			sql.append(" and (beginDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
			sql.append(" or endDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)) + ")");
		}
		if (endDate != null) {
			sql.append(" and (beginDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
			sql.append(" or endDate<="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)) + ")");
		}
		int returnValue = calCount(sql.toString());
		return returnValue;
	}

	/**
	 * 统计日程安排数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statScheduleCount(Date startDate, Date endDate)
			throws SQLException {
		StringBuilder sql = new StringBuilder(
				"select count(*) from user_plan where 1=1 ");
		if (startDate != null) {
			sql.append(" and myDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			sql.append(" and myDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(sql.toString());
		return returnValue;
	}

	/**
	 * 统计公共共享文件数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statShareFileCount(Date startDate, Date endDate)
			throws SQLException {
		// TODO Auto-generated method stub
		StringBuilder sql = new StringBuilder(
				"select count(*) from netdisk_public_attach where 1=1 ");
		if (startDate != null) {
			sql.append(" and create_date>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			sql.append(" and create_date<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(sql.toString());
		return returnValue;
	}

	/**
	 * 统计系统使用状态 返回JSON 对象
	 * 
	 * @return
	 */
	public JSONObject statUsedStatusTOJSONObject(Date startDate, Date endDate,
			String userName) {
		JSONObject returnValue = new JSONObject();
		returnValue = createReturnJson(statUsedStatusToMap(startDate, endDate,
				userName));
		return returnValue;
	}

	/**
	 * 统计用户数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statUser(Date startDate, Date endDate) throws SQLException {
		StringBuilder userSql = new StringBuilder(
				"select count(*) from users where 1=1 ");
		if (startDate != null) {
			userSql.append(" and regDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			userSql.append(" and regDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(userSql.toString());
		return returnValue;
	}

	/**
	 * 统计手机登录次数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statMobileAccess(Date startDate, Date endDate)
			throws SQLException {
		// 计算手机登录数
		StringBuilder mobileAccessSql = new StringBuilder(
				"select count(*) from log where action='登录系统' and device=100");
		if (startDate != null) {
			mobileAccessSql.append(" and log_date>=" + startDate.getTime());
		}
		if (endDate != null) {
			mobileAccessSql.append(" and log_date<=" + endDate.getTime());
		}
		int returnValue = calCount(mobileAccessSql.toString());
		return returnValue;
	}

	/**
	 * 统计通知数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statNotice(Date startDate, Date endDate) throws SQLException {
		StringBuilder noticeSql = new StringBuilder(
				"select count(*) from oa_notice where 1=1  ");
		if (startDate != null) {
			noticeSql.append(" and create_date>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			noticeSql.append(" and create_date<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(noticeSql.toString());
		return returnValue;
	}

	/**
	 * 统计短消息数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statMessage(Date startDate, Date endDate) throws SQLException {
		StringBuilder messageSql = new StringBuilder(
				"select count(*) from oa_message where 1=1   ");
		if (startDate != null) {
			messageSql.append(" and send_time>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			messageSql.append(" and send_time<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(messageSql.toString());
		return returnValue;
	}

	/**
	 * 统计流程数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statflow(Date startDate, Date endDate) throws SQLException {
		// 计算流程数
		StringBuilder flowSql = new StringBuilder(
				"select count(*) from flow where 1=1 and status<>-10  ");
		if (startDate != null) {
			flowSql.append(" and mydate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			flowSql.append(" and mydate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(flowSql.toString());
		return returnValue;
	}

	/**
	 * 统计文件柜数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statDocument(Date startDate, Date endDate) throws SQLException {
		StringBuilder documentSql = new StringBuilder(
				"select count(*) from document where 1=1 ");
		if (startDate != null) {
			documentSql.append(" and createDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			documentSql.append(" and createDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(documentSql.toString());
		return returnValue;
	}

	/**
	 * 统计已使用网盘大小
	 * 
	 * @param userName
	 * @return
	 */
	private String statNetDiskUserSpace(String userName) {
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		// String returnValue = NumberUtil.round((double) (ud
		// .getDiskSpaceUsed()) / 1024000, 1)
		// + "M";
		String returnValue = UtilTools.getFileSize(ud.getDiskSpaceUsed());
		return returnValue;
	}

	/**
	 * 统计网盘分配大小
	 * 
	 * @param userName
	 * @return
	 */
	private String statNetDiskSpace(String userName) {
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		String returnValue = UtilTools.getFileSize(ud.getDiskSpaceAllowed());
		return returnValue;
	}

	/**
	 * 统计网盘文件数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statNetDiskFile(Date startDate, Date endDate)
			throws SQLException {
		// 计算网盘文件数
		StringBuilder netDiskSql = new StringBuilder(
				"select count(*) from netdisk_document_attach where 1=1 ");
		if (startDate != null) {
			netDiskSql.append(" and uploadDate>="
					+ StrUtil.sqlstr(DateUtil.format(startDate,
							dateTimeFormatString)));
		}
		if (endDate != null) {
			netDiskSql.append(" and uploadDate<="
					+ StrUtil.sqlstr(DateUtil.format(endDate,
							dateTimeFormatString)));
		}
		int returnValue = calCount(netDiskSql.toString());
		return returnValue;
	}
	/**
	 * 活跃用户数
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	private int statActiveUserCount(Date startDate, Date endDate)
			throws SQLException {
		// 计算网盘文件数
		StringBuilder activeUserCountSql = new StringBuilder(
				"select   count( distinct user_name) from log where action='登录系统' and device=0" );
		if (startDate != null) {
			activeUserCountSql.append(" and log_date>=" + startDate.getTime());
		}
		if (endDate != null) {
			activeUserCountSql.append(" and log_date<=" + endDate.getTime());
		}
		int returnValue = calCount(activeUserCountSql.toString());
		System.out.println(getClass() + "===================" + activeUserCountSql.toString());
		return returnValue;
	}
	/**
	 * 通过HTTPClient 获取OA 运行状态
	 * 
	 * @param fdao
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public JSONObject getYOAStatus(com.redmoon.oa.visual.FormDAO fdao,
			Date startDate, Date endDate) {
		JSONObject returnValue = null;
		final String path = fdao.getFieldValue("access_path");
		final String sso = fdao.getFieldValue("sso_pwd");
		final String uri = "/public/use_status.jsp";
		final String url = path
				+ uri
				+ "?"
				+ ssoParm
				+ "="
				+ StrUtil.UrlEncode(sso)
				+ "&preDate="
				+ StrUtil.UrlEncode("*")
				+ "&"
				+ beginDateParm
				+ "="
				+ StrUtil.UrlEncode(DateUtil
						.format(startDate, dateFormatString)) + "&"
				+ endDateParm + "="
				+ StrUtil.UrlEncode(DateUtil.format(endDate, dateFormatString));
		System.out.println(getClass() + "url=" + url);
		boolean errorValue = false;
		String errorMessageValue = "";
		try {
			HttpClient httpClient = new HttpClient();
			GetMethod getAddrMethod = new GetMethod(url);
			final int status = httpClient.executeMethod(getAddrMethod);

			if (status != HttpStatus.SC_OK) {
				errorValue = true;
				errorMessageValue += "获取地址失败: " + getAddrMethod.getStatusLine();
			} else {
				String responseMessage = new String(getAddrMethod
						.getResponseBody(), "utf-8");
				returnValue = new JSONObject(responseMessage);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			errorValue = true;
			errorMessageValue = "获取JSON 无法解析：" + e.getMessage();

		} catch (HTTPException e) {
			e.printStackTrace();
			errorValue = true;
			errorMessageValue = "HTTP访问异常：" + e.getMessage() + " 状态："
					+ e.getStatusCode();

		} catch (IOException e) {
			e.printStackTrace();
			errorValue = true;
			errorMessageValue = "IO异常：" + e.getMessage();
		}

		if (errorValue) {
			try {
				returnValue = createDefaultJSONObject();
				returnValue.put(error, errorValue);
				returnValue.put(errorMessage, errorMessageValue);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return returnValue;
	}

	/**
	 * 预定义日期开始与结束的分割符
	 */
	public static String preDateSplitString = "\\|";
	/**
	 * 预定义日期参数
	 */
	public static String preDateParm = "preDate";
	/**
	 * SSO参数
	 */
	public static String ssoParm = "sso";
	/**
	 * 自定义开始日期参数
	 */
	public static String beginDateParm = "beginDate";
	/**
	 * 自定义结束日期参数
	 */
	public static String endDateParm = "endDate";
	/**
	 * 自定义日期参数标识
	 */
	public static String customDateIden = "*";
	/**
	 * 时间补零
	 */
	public static String fillingTimeZero = " 00:00:00";

	/**
	 * 获取查询的时间参数
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Date> getQueryDateCondition(HttpServletRequest request) {
		Map<String, Date> returnValue = new HashMap<String, Date>();
		Date startDate = null;
		Date endDate = null;
		String preDate = ParamUtil.get(request, RunStatusUtil.preDateParm);

		if (preDate == null || preDate.isEmpty()) {

		} else if (preDate.equals(RunStatusUtil.customDateIden)) {
			String startDateString = ParamUtil.get(request,
					RunStatusUtil.beginDateParm);
			String endDateString = ParamUtil.get(request,
					RunStatusUtil.endDateParm);
			startDate = DateUtil.parse(startDateString
					+ RunStatusUtil.fillingTimeZero,
					RunStatusUtil.dateTimeFormatString);
			endDate = DateUtil.parse(endDateString
					+ RunStatusUtil.fillingTimeZero,
					RunStatusUtil.dateTimeFormatString);
		} else {
			try {
				String[] dates = preDate
						.split(RunStatusUtil.preDateSplitString);
				startDate = DateUtil.parse(dates[0]
						+ RunStatusUtil.fillingTimeZero,
						RunStatusUtil.dateTimeFormatString);
				endDate = DateUtil.parse(dates[1]
						+ RunStatusUtil.fillingTimeZero,
						RunStatusUtil.dateTimeFormatString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		returnValue.put(RunStatusUtil.beginDateParm, startDate);
		returnValue.put(RunStatusUtil.endDateParm, endDate);
		return returnValue;
	}

	private int statLocationCount(Date startDate, Date endDate)
			throws SQLException {
		// 计算登录次数
		StringBuilder loginSql = new StringBuilder(
				"select count(*) from oa_location where 1=1 ");
		if (startDate != null) {
			loginSql.append(" and create_date>="
					+ SQLFilter.getDateStr(DateUtil.format(startDate,
							"yyyy-MM-dd"), "yyyy-MM-dd"));
		}
		if (endDate != null) {
			loginSql.append(" and create_date<="
					+ SQLFilter.getDateStr(DateUtil.format(endDate,
							"yyyy-MM-dd"), "yyyy-MM-dd"));
		}

		int returnValue = calCount(loginSql.toString());
		return returnValue;
	}

}
