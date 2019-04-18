package com.redmoon.oa.robot;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.sys.DebugUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.CityCtl;
import com.redmoon.oa.flow.macroctl.ProvinceCtl;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.util.WeatherUtil;
import com.redmoon.oa.visual.FormDAO;

public class RobotUtil {
	/**
	 * 积分票
	 * */
	public final static String TICKET_SCORE = "0";
	/**
	 * 米票
	 */
	public final static String TICKET_RICE = "1";
	/**
	 * 菜票
	 */
	public final static String TICKET_DISH = "2";
	
	public static String getKey() {
		Config cfg = Config.getInstance();		
		return cfg.getProperty("key");
	}
	
	public static String getRobotId() {
		Config cfg = Config.getInstance();		
		return cfg.getProperty("robotId");		
	}

	public static String getTickName(String kind) {
		if ("0".equals(kind)) {
			return "积分";
		}
		else if ("1".equals(kind)) {
			return "米票";
		}
		else if ("2".equals(kind)) {
			return "菜票";
		}
		else
			return "类型不存在";
	}
	
	/**
	 * 关闭帐户
	 */
	public static void closeAccount(String groupId, String qq) {
		UserMgr um = new UserMgr();
		try {
			um.leaveOffice(null, qq, UserDb.ADMIN);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 发送临时会话，可能会被屏蔽
	 * @param request
	 * @param qq
	 * @param msg
	 * @return
	 */
	public static String sendTmpMsg(HttpServletRequest request, String qq, String msg) {
		Config cfg = Config.getInstance();		
		String urlRobot = cfg.getProperty("urlRobot");		
		String robotId = cfg.getProperty("robotId");
		String key = cfg.getProperty("key");
		String url = urlRobot + "/SendTempIM.do?RobotQQ=" + robotId + "&Key=" + key + "&QQ=" + qq + "&Message=" + StrUtil.UrlEncode(msg);
		return NetUtil.gather(request, "utf-8", url);
	}
	
	/**
	 * 向好友发送消息
	 * @param request
	 * @param qq
	 * @param msg
	 * @return
	 */
	public static String sendMsg(HttpServletRequest request, String qq, String msg) {
		Config cfg = Config.getInstance();
		String urlRobot = cfg.getProperty("urlRobot");		
		String robotId = cfg.getProperty("robotId");
		String key = cfg.getProperty("key");		
		String url = urlRobot + "/SendIM.do?RobotQQ=" + robotId + "&Key=" + key + "&QQ=" + qq + "&Message=" + StrUtil.UrlEncode(msg);
		return NetUtil.gather(request, "utf-8", url);
	}
	
	/**
	 * 发送群消息
	 * @param request
	 * @param groupId
	 * @param msg
	 * @return
	 */
	public static String sendClusterMsg(HttpServletRequest request, String groupId, String msg) {
		Config cfg = Config.getInstance();
		String urlRobot = cfg.getProperty("urlRobot");
		String robotId = cfg.getProperty("robotId");
		String key = cfg.getProperty("key");		
		String url = urlRobot + "/SendClusterIM.do?RobotQQ=" + robotId + "&Key=" + key + "&GroupId=" + groupId + "&Message=" + StrUtil.UrlEncode(msg);
		return NetUtil.gather(request, "utf-8", url);
	}
	
	/**
	 * 记录得分明细
	 * @param userName
	 * @param code
	 * @param score
	 * @param relateId
	 * @param desc
	 * @return
	 */
	public static boolean logScoreDetail(String userName, String code, int score, String relateId, String relateUser, String desc) {
		String formCode = "score_detail";
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		FormDAO fdao = new FormDAO(fd);
		fdao.setFieldValue("user_name", userName);
		fdao.setFieldValue("kind", code);
		fdao.setFieldValue("create_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		fdao.setFieldValue("score", String.valueOf(score));
		fdao.setFieldValue("relate_id", relateId);
		fdao.setFieldValue("description", desc);
		fdao.setFieldValue("relate_user", relateUser);
		return fdao.create();
	}
	
	/**
	 * 找到最后一条签到记录
	 * @param qq
	 * @return
	 */
	public static FormDAO getLastSign(String groupId, String qq) {
		String sql = "select id from " + FormDb.getTableName("qqgroup_sign") + " where qq=" + StrUtil.sqlstr(qq);
		FormDAO fdao = new FormDAO();
		Vector v;
		try {
			v = fdao.list("qqgroup_sign", sql);
			Iterator ir = v.iterator();
			if (ir.hasNext()) {
				return (FormDAO)ir.next();
			}			
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 从积分规则中取出规则
	 * @param code
	 * @return
	 */
	public static ScoreRule getScoreRule(String code) {
		Config cfg = Config.getInstance();
		Map map = cfg.getScoreRules();
		return (ScoreRule)map.get(code);
	}
	
	/**
	 * 签到
	 * @param request
	 * @param groupId
	 * @param qq
	 * @param senderName
	 * @return
	 */
	public static String sign(HttpServletRequest request, String groupId, String qq, String senderName) {
		UserDb ud = new UserDb();	
		ud = ud.getUserDb(qq);
		if (!ud.isLoaded()) {
			com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();				  
			String pwd = scfg.getInitPassword();
			ud.create(qq, senderName, pwd, "", DeptDb.ROOTCODE);
			ud = ud.getUserDb(qq);
			// 存入QQ群号
			ud.setParty(groupId);
			ud.save();
		}
		else {
			// 判断帐户如已关闭，则开启
			/*
			if (!ud.isValid()) {
				ud.setValid(UserDb.VALID_WORKING);
				ud.save();
			}
			*/
		}
		
		Config cfg = Config.getInstance();	
		
		// 判断是否在签到时间范围内
		Date dt = new Date();
		String signTimeFrameStart1 = cfg.getProperty("signTimeFrameStart1");
		String signTimeFrameEnd1 = cfg.getProperty("signTimeFrameEnd1");
		String signTimeFrameStart2 = cfg.getProperty("signTimeFrameStart2");
		String signTimeFrameEnd2 = cfg.getProperty("signTimeFrameEnd2");
		
		String today = DateUtil.format(dt, "yyyy-MM-dd");
		Date start1 = DateUtil.parse(today + " " + signTimeFrameStart1, "yyyy-MM-dd HH:mm");
		Date end1 = DateUtil.parse(today + " " + signTimeFrameEnd1, "yyyy-MM-dd HH:mm");
		Date start2 = DateUtil.parse(today + " " + signTimeFrameStart2, "yyyy-MM-dd HH:mm");
		Date end2 = DateUtil.parse(today + " " + signTimeFrameEnd2, "yyyy-MM-dd HH:mm");

		FormDAO fdao = getLastSign(groupId, qq);
		if (DateUtil.compare(dt, start1)==1 && DateUtil.compare(dt, end1)==2) {
			;
		}
		else if (DateUtil.compare(dt, start2)==1 && DateUtil.compare(dt, end2)==2) {
			;
		}
		else {
			// 如非首次签到，需在规定的时间范围内
			if (fdao!=null) {
				String msg = "签到未成功，有效时间 " + signTimeFrameStart1 + "-" + signTimeFrameEnd1 + " ~ " + signTimeFrameStart2 + "-" + signTimeFrameEnd2;
				return msg;
			}
		}
		
		int times=1, days = 1, rank=0, surpass = 0;
		String formCode = "qqgroup_sign";

		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		int maxOneDay = cfg.getIntProperty("signMaxOneDay");

		boolean isExceed = false; // 当天签到是否超过了最大次数
		ScoreRule sr = getScoreRule("sign");
		int scoreDlt = sr.getValue();
		String socreName = sr.getName();
		int score, scoreRemained;
		
		if (fdao==null) {
			fdao = new FormDAO(fd);
			fdao.setFieldValue("qq", qq);
			fdao.setFieldValue("nick", senderName);
			fdao.setFieldValue("days", "1");
			fdao.setFieldValue("times", "1");
			fdao.setFieldValue("times_today", "1");
			fdao.setFieldValue("last_date", DateUtil.format(new Date(), "yyyy-MM-dd"));
			score = scoreDlt;
			fdao.setFieldValue("score", String.valueOf(score));
			fdao.setFieldValue("score_remained", String.valueOf(score));
			fdao.create();
		}
		else {
			Date lastDate = DateUtil.parse(fdao.getFieldValue("last_date"), "yyyy-MM-dd");
			Date now = new Date();
			
			times = StrUtil.toInt(fdao.getFieldValue("times"));
			days = StrUtil.toInt(fdao.getFieldValue("days"));		
			score = StrUtil.toInt(fdao.getFieldValue("score"), 0);
			scoreRemained = StrUtil.toInt(fdao.getFieldValue("score_remained"), 0);
			
			// 如果为今天
			if (DateUtil.isSameDay(lastDate, now)) {
				// 如果只有1次，则可以再签到
				int timesToday = StrUtil.toInt(fdao.getFieldValue("times_today"));
				if (timesToday < maxOneDay) {
					times++;
					timesToday++;
					fdao.setFieldValue("times", String.valueOf(times));					
					fdao.setFieldValue("times_today", String.valueOf(timesToday));
					score += scoreDlt;
					scoreRemained += scoreDlt;
					fdao.setFieldValue("score", String.valueOf(score));					
					fdao.setFieldValue("score_remained", String.valueOf(scoreRemained));					
				}
				else {
					isExceed = true;			
				}
			}
			else {	
				times += 1;
				fdao.setFieldValue("times", String.valueOf(times));
				score += scoreDlt;
				scoreRemained += scoreDlt;				
				fdao.setFieldValue("score", String.valueOf(score));
				fdao.setFieldValue("score_remained", String.valueOf(scoreRemained));					
				fdao.setFieldValue("last_date", DateUtil.format(new Date(), "yyyy-MM-dd"));				
				// 如果最后一次记录不是今天，则判断是否连续签到
				// 判断是否为昨天
				if (DateUtil.isSameDay(DateUtil.addDate(now, -1),lastDate)) {
					fdao.setFieldValue("days", String.valueOf(++days));
				}
				
				fdao.setFieldValue("times_today", String.valueOf(1));
			}
			try {
				fdao.setFieldValue("nick", senderName);				
				fdao.save();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JdbcTemplate jt = new JdbcTemplate();
		// 取得签到次数排行
		String sql = "select u.rowNo from (select qq,(@rowNum:=@rowNum+1) as rowNo from form_table_" + formCode + " s,(select (@rowNum :=0) ) b order by s.times desc ) u where u.qq=";
		sql += StrUtil.sqlstr(qq);
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				rank = rr.getInt(1);
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String msg;
		if (isExceed) {
			msg = cfg.getProperty("signInfo");
			msg = StrUtil.format(msg, new Object[]{qq, times, days, rank, score});

			// 超过2次则提示
			String str = cfg.getProperty("signExceed");
			msg += "\n" +  StrUtil.format(str, new Object[]{maxOneDay});	
		}
		else {
			// 记录得分明细
			logScoreDetail(qq, "sign", scoreDlt, "", "", "");
			
			msg = cfg.getProperty("signMsg");
			msg = StrUtil.format(msg, new Object[]{qq, times, days, rank, score});
		}
		return msg;
	}
	
	/**
	 * 取得签到记录
	 * @param userName
	 * @return
	 */
	public static FormDAO getSign(String userName) {
		String sql = "select id from form_table_qqgroup_sign where qq=" + StrUtil.sqlstr(userName);
		FormDAO fdao = new FormDAO();
		Iterator ir;
		try {
			ir = fdao.list("qqgroup_sign", sql).iterator();
			if (ir.hasNext()) {
				fdao = (FormDAO)ir.next();
				return fdao;
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 检查群名片是否合法
	 * @param request
	 * @param qq
	 * @param senderName
	 * @return
	 */
	public static String checkCard(HttpServletRequest request, String qq, String senderName) {
		// Pattern pat = Pattern.compile("([\\u4e00-\\u9fa5\\xa1-\\xff]+)-([A-Z0-9a-z/_\\u4e00-\\u9fa5\\xa1-\\xff]+)-(.*?)",
		//		Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Pattern pat = Pattern.compile("([\\u4e00-\\u9fa5\\xa1-\\xff]+)-(.*?)",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher mat = pat.matcher(senderName);
		if (!mat.find()) {
			Config cfg = Config.getInstance();		
			String str = cfg.getProperty("cardNotValid");
			return StrUtil.format(str, new Object[]{qq});
		}
		return "";
	}

	/**
	 * 登录群OA
	 * @param request
	 * @param groupId
	 * @param qq
	 * @return
	 */
	public static String login(HttpServletRequest request, String groupId, String qq) {
		com.redmoon.forum.Config cfgForum = com.redmoon.forum.Config.getInstance();
		String myKey = cfgForum.getKey();
		String authKey = qq + "|"
				+ DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");						
		authKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(myKey, authKey);	

		Config cfg = Config.getInstance();
		String loginUrlAuto = cfg.getGroup(groupId).getLoginUrlAuto();

		String str = sendMsg(request, qq, loginUrlAuto + "&authKey=" + authKey);
		JSONObject json = null;
		try {
			json = new JSONObject(str);
			if (json.has("nFlag")) {
				// 发送失败，因未加米宝为好友
				if (json.getInt("nFlag")==0) {
					String loginUrl = cfg.getGroup(groupId).getLoginUrl();					
					// out.print("[@" + qq + "]" + " 登录指令失败"); // json.getString("strError"));
					return StrUtil.format(loginUrl, new Object[]{qq});
				}
			}
		}
		catch(JSONException e) {
			System.out.println(RobotUtil.class.getName() + " login:" + str);
			e.printStackTrace();
			String apiNotOpen = StrUtil.format(cfg.getProperty("apiNotOpen"), new Object[]{qq});
			return apiNotOpen;
		}
		
		String loginRemind = cfg.getProperty("loginRemind");
		return StrUtil.format(loginRemind, new Object[]{qq});
	}

	/**
	 * 找回密码
	 * @param request
	 * @param groupId
	 * @param qq
	 * @return
	 */
	public static String getBackPwd(HttpServletRequest request, String groupId, String qq) {
		UserDb user = new UserDb();
		user = user.getUserDb(qq);

		Config cfg = Config.getInstance();
		String getBackPwd = cfg.getProperty("getBackPwd");
		String str = sendMsg(request, qq, StrUtil.format(getBackPwd, new Object[]{user.getPwdRaw()}));
		JSONObject json = null;
		try {
			json = new JSONObject(str);
			if (json.has("nFlag")) {
				// 发送失败，因未加米宝为好友
				if (json.getInt("nFlag")==0) {
					String getBackPwdAddFriend = cfg.getProperty("getBackPwdAddFriend");
					return StrUtil.format(getBackPwdAddFriend, new Object[]{qq});
				}
				else {
					// json的内容是 {"Info":"已经给该QQ发送信息","nFlag":1}
					// System.out.println(RobotUtil.class.getName() + " getBackPwd1:" + json.toString());
					// 找回密码成功
					String getBackPwdSuccess = cfg.getProperty("getBackPwdSuccess");
					return StrUtil.format(getBackPwdSuccess, new Object[]{qq});
				}
			}
			else {
				System.out.println(RobotUtil.class.getName() + " getBackPwd2:" + json.toString());
			}
		}
		catch(JSONException e) {
			System.out.println(RobotUtil.class.getName() + " login:" + str);
			e.printStackTrace();
			String apiNotOpen = StrUtil.format(cfg.getProperty("apiNotOpen"), new Object[]{qq});
			return apiNotOpen;
		}
		return "";
	}

	public static String getWeather(HttpServletRequest request, String qq, String city) {
		if (!city.endsWith("市")) {
			city += "市";
		}
		StringBuffer sb = new StringBuffer();
		JSONObject json = WeatherUtil.get(city);
		if (json!=null) {
			try { 
				if (json.getString("desc").equals("invilad-citykey")) {
					return "[@" + qq + "]城市 “" + city + "” 名称错误！";
				}
				sb.append("[@" + qq + "] " + city + " 天气：\n");
				JSONObject data = json.getJSONObject("data");
				JSONArray ary = data.getJSONArray("forecast");
				for (int i=0; i<ary.length(); i++) {
					JSONObject jo = ary.getJSONObject(i);
					String date = jo.getString("date");
					String high = jo.getString("high");
					String low = jo.getString("low");
					String type = jo.getString("type");
					String fx = jo.getString("fengxiang");
					String fl = jo.getString("fengli");
					fl = fl.replace("<![CDATA[", "");
					fl = fl.replace("]]>", "");
					sb.append(date + "， " + high + " " + low + "，" + type + "，" + fx + " " + fl + "\n");	
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
				return json.toString();
			}
		}
		return sb.toString();
	}
	
	public static boolean startRedbag(long id) throws ErrMsgException {
		String formCode = "robot_red_bag";
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(id, fd);
		
		String name = fdao.getFieldValue("name");
		String remark = fdao.getFieldValue("remark");
		String max_count = fdao.getFieldValue("max_count");
		float expireHour = StrUtil.toFloat(fdao.getFieldValue("expire_hour"), 0);
		int expireMin = (int)(60 * expireHour);
					
		// 生成批次
		int batchNo = 1;
		String sql = "select max(batch_no) from form_table_robot_red_bag_bat";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			batchNo = rr.getInt(1) + 1;
		}
		FormDb fdBat = new FormDb();
		fdBat = fdBat.getFormDb("robot_red_bag_bat");
		FormDAO fdaoBat = new FormDAO(fdBat);
		fdaoBat.setFieldValue("batch_no", String.valueOf(batchNo));
		fdaoBat.setFieldValue("red_bag", String.valueOf(id));
		Date startTime = new Date();
		Date endTime = DateUtil.addMinuteDate(startTime, expireMin);
		fdaoBat.setFieldValue("start_time", DateUtil.format(startTime, "yyyy-MM-dd HH:mm:ss"));
		fdaoBat.setFieldValue("end_time", DateUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
		fdaoBat.setFieldValue("max_count", max_count);
		fdaoBat.create();
		
		// 生成獲獎結果
		FormDb fdRes = new FormDb();
		fdRes = fdRes.getFormDb("robot_red_bag_res");	
		sql = "select id from form_table_robot_red_bag_nest where cws_id='" + id + "'";
		Iterator ir = fdao.list("robot_red_bag_nest", sql).iterator();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			
			String tip = fdao.getFieldValue("tip");
			String counts = fdao.getFieldValue("counts");
			tip = StrUtil.format(tip, new Object[]{counts});
			String kind = fdao.getFieldValue("kind");
			int copies = StrUtil.toInt(fdao.getFieldValue("copies"), 1);
			
			for (int k=0; k<copies; k++) {
				FormDAO fdaoRes = new FormDAO(fdRes);
				fdaoRes.setFieldValue("batch_no", String.valueOf(batchNo));
				fdaoRes.setFieldValue("item", String.valueOf(fdao.getId()));
				fdaoRes.setFieldValue("tip", tip);
				fdaoRes.setFieldValue("kind", kind);
				fdaoRes.setFieldValue("my_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
				fdaoRes.create();
			}
		}
		
		// 發送消息
		com.redmoon.oa.robot.Config cfg = com.redmoon.oa.robot.Config.getInstance();
		String redBagNotice = cfg.getProperty("redBagNotice");
		redBagNotice = StrUtil.format(redBagNotice, new Object[]{name, String.valueOf(expireMin), remark});
		
		String redBagNoticeUrl = cfg.getProperty("redBagNoticeUrl") + batchNo;
		redBagNotice += redBagNoticeUrl;
		
		Map groups = cfg.getGroups();
		ir = groups.keySet().iterator();
		while (ir.hasNext()) {
			String gid = (String)ir.next();
			Group group = (Group)groups.get(gid);
			if (group.isRedbagOpen()) {
				HttpServletRequest request = null;
				RobotUtil.sendClusterMsg(request, group.getId(), redBagNotice + "&groupId=" + gid);
			}
		}
		return true;	
	}
	
	/**
	 * 当被@时，播报人脉资源
	 * @param request
	 * @param groupId
	 * @param user
	 * @throws ErrMsgException
	 */
	public static void announceConnectionOnAt(HttpServletRequest request, String groupId, UserDb user) throws ErrMsgException {
		if (user==null)
			return;
		
		String sql = "select id from form_table_connection where user_name=" + StrUtil.sqlstr(user.getName()) + " and isShowOnAt=1 order by orders desc";
		FormDb fd = new FormDb();
		String formCode = "connection";
		fd = fd.getFormDb(formCode);
		UserDb ud = new UserDb();
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		// 取前3条
		StringBuffer connMsg = new StringBuffer();
		ListResult lr = fdao.listResult(formCode, sql, 1, 3);
		Iterator irPro = lr.getResult().iterator();
		while (irPro.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)irPro.next();
			String name = fdao.getFieldValue("name");
			
		    String province = fdao.getFieldValue("province");
		    String city = fdao.getFieldValue("city");
		    
		    FormField ff = fdao.getFormField("province");
			ProvinceCtl pc = new ProvinceCtl();
			province = pc.converToHtml(request, ff, province);
			
		    ff = fdao.getFormField("city");
			CityCtl cc = new CityCtl();
			city = cc.converToHtml(request, ff, city);
		
			StrUtil.concat(connMsg, "\n", user.getRealName() + "：" + province + city + " " + name + "，" + StrUtil.getAbstract(request, fdao.getFieldValue("remark"), 60, ""));
		}
		if (connMsg.length()>0) {
			RobotUtil.sendClusterMsg(request, groupId, "【播报】" + connMsg);
		}		
	}
	
	/**
	 * 解析命令
	 * @param request
	 * @param groupId
	 * @param user 被赠分人的UserDb实例
	 * @param userRealName 被赠分人的呢称
	 * @param cmdMsg
	 * @param senderQq 赠分人的QQ号
	 * @param senderName 赠分人的呢称
	 * @throws ErrMsgException 
	 */
	public static void parseCmdMsg(HttpServletRequest request, String groupId, UserDb user, String userRealName, String cmdMsg, String senderQq, String senderName) throws ErrMsgException {
		if (cmdMsg.startsWith("赠分") || cmdMsg.startsWith("送分")) {
			if (user==null || !user.isLoaded()) {
				RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]赠分未成功，" + userRealName + " 尚未创建帐户，需签到以后才能创建帐户");						
				return;		
			}
			
			// 一般不会出现自己赠自己的情况，因为@的时候，QQ本身会过滤掉自己
			if (senderQq.contentEquals(user.getName())) {
				RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]赠分未成功，您不能给自己赠分！");						
				return;					
			}
			
			String fenShu = cmdMsg.substring(2).trim();
			fenShu = fenShu.replaceAll("　", ""); // 去掉全角的空格
			if (fenShu.endsWith("分")) {
				// 分值
				fenShu = fenShu.substring(0, fenShu.length()-1);
			}
			
			DebugUtil.i(RobotUtil.class, "parseCmdMsg", "userName=" + user.getName() + " fenShu=" + fenShu);
				
			if (StrUtil.isNumeric(fenShu)) {
				// 取得赠分人
				FormDAO fdao = getLastSign(groupId, senderQq);
				if (fdao==null) {
					RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]赠分失败，您还没有创建帐户，请先签到！");
					return;					
				}
				
				int score = StrUtil.toInt(fdao.getFieldValue("score"), 0);
				int scoreRemained = StrUtil.toInt(fdao.getFieldValue("score_remained"), 0);
				
				int dlt = StrUtil.toInt(fenShu);
				if (dlt<0) {
					RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]赠分的分值需大于0");
					return;
				}
				if (scoreRemained < dlt) {
					RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]积分不足，操作失败，您当前剩余积分为" + scoreRemained + "，低于赠分值" + dlt);
					return;						
				}
				
				// 给受赠人加分
				FormDAO fdaoGift = getLastSign(groupId, user.getName());
				if (fdaoGift!=null) {
					fdaoGift.setFieldValue("score", String.valueOf(score + dlt));
					fdaoGift.setFieldValue("score_remained", String.valueOf(scoreRemained + dlt));
					fdaoGift.save();		
					RobotUtil.logScoreDetail(user.getName(), "score_gift", dlt, "", senderQq, "");							

					// 减去赠分人的分数
					fdao.setFieldValue("score", String.valueOf(score - dlt));
					fdao.setFieldValue("score_remained", String.valueOf(scoreRemained - dlt));
					fdao.save();
					
					RobotUtil.logScoreDetail(senderQq, "score_gift", -dlt, "", user.getName(), "");	
					
					RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]赠分成功，赠予[@" + user.getName() + "]" + dlt + "分！");											
				}
				else {
					RobotUtil.sendClusterMsg(request, groupId, "[@" + senderQq + "]" + userRealName + " 没有签到记录，无法赠分！");
				}
			}

		}
	}
	
	/**
	 * 解析@消息
	 * @param request
	 * @param groupId
	 * @param Message
	 * @param senderQq 发送者的QQ号
	 * @param senderName 发送者的呢称
	 */
	public static void parseAtMsg(HttpServletRequest request, String groupId, String Message, String senderQq, String senderName) {
		Pattern p = Pattern.compile(
                "@([A-Z0-9a-z-_/\\u4e00-\\u9fa5\\xa1-\\xff\\.\\(\\)]+) (.+)", // 前为utf8中文范围，后为gb2312中文范围
                // "@([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.\\(\\)]+) ([^\\s]+)", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(Message);
        while (m.find()) {
            String realName = m.group(1);	
            
            String userName = "";
            if (realName.endsWith(")")) {
            	// 两种类型：@老范-OA-办公(2530476560) 123 或  @蓝风 123
            	int k = realName.indexOf("(");
            	if (k!=-1) {
            		userName = realName.substring(k+1, realName.length()-1);
            	}
            }
            
            String cmdMsg = StrUtil.getNullStr(m.group(2));
            cmdMsg = cmdMsg.trim(); // @用户 后面的消息
            
			System.out.println(RobotUtil.class.getName() + " @user userName=" + userName + " realName=" + realName + " cmdMsg=" + cmdMsg);		

			UserDb user = new UserDb();	 
			if (!"".equals(userName)) {
				user = user.getUserDb(userName);
			}
			else {
				user = user.getUserDbByRealName(realName);
			}
			
			// 当被@时赠分， @蓝风 赠分30
			try {
				parseCmdMsg(request, groupId, user, realName, cmdMsg, senderQq, senderName);
			} catch (ErrMsgException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// 当被@时，自动播报人脉
			/*
			try {
				RobotUtil.announceConnectionOnAt(request, groupId, user);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
        }			
	}
}
