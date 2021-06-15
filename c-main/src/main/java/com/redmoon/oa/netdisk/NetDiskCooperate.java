package com.redmoon.oa.netdisk;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
public class NetDiskCooperate {
	//协作动态web中action 代表的含义
	public static final String[] ACTION_DESC = new String[]{"发起协作","加入协作","拒绝加入协作","被取消协作","上传","更新","删除"};
	//协作动态日志web显示的条数
	public static final int LIMT_COUNT = 30;
	public static final int IS_REFUSED = 2;
	public ResultIterator queryMyAttendCooperate(String userName){
		String sql = "SELECT c.id,c.share_user,c.dir_code,c.cooperate_date,d.doc_id,d.name FROM netdisk_cooperate c,netdisk_directory d where c.dir_code = d.code and user_name=? and is_refused=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql,new Object[]{userName,IS_REFUSED});
			return ri;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获得我参与的协作所有根节点的code
	 * @param userName
	 * @return
	 */
	public HashMap<String,Integer> queryMyAttendCooperateRootCode(String userName){
		HashMap<String,Integer> hashMap = new HashMap<String, Integer>();
		String sql = "SELECT c.id,c.share_user,c.dir_code,c.cooperate_date,d.doc_id,d.name FROM netdisk_cooperate c,netdisk_directory d where c.dir_code = d.code and user_name=? and is_refused=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql,new Object[]{userName,IS_REFUSED});
			ResultRecord record = null;
			while(ri.hasNext()){
				record = (ResultRecord)ri.next();
				hashMap.put(record.getString("dir_code"),1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hashMap;
	}
	/**
	 * 获取所有协作动态的数据,web端默认取 30条数据
	 * @param dir_code
	 * @return
	 */
	public List<CooperateLogBean> queryMyAttengCooperateLog(String dir_code){
		List<CooperateLogBean> cooperateLogList = new ArrayList<CooperateLogBean>();
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config.getInstance();
		int limits = cfg.getIntProperty("webShareLogCount");
		if (limits < 0) {
			limits = LIMT_COUNT;
		}
		String sql = "select user_name,action,action_date,action_name from netdisk_cooperate_log where dir_code="
		+ StrUtil.sqlstr(dir_code)
		+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
				+ limits
				: "")
		+ " order by action_date desc"
		+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
				+ limits : "");
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while(ri.hasNext()){
				record = (ResultRecord)ri.next();
				String userName = record.getString("user_name");
				String actionDate = DateUtil.format(record.getDate("action_date"),
				"yyyy-MM-dd HH:mm");
				String realName = "";
				UserDb ud = new UserDb(userName);
				realName = ud.getRealName();
				int action = record.getInt("action");
				String actionName = record.getString("action_name");
				String actionDesc = ACTION_DESC[action];
				CooperateLogBean cooperateLogBean = new CooperateLogBean(userName, actionDesc, actionDate, actionName);
				cooperateLogList.add(cooperateLogBean);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cooperateLogList;
	}
	/**
	 * 返回所有协作动态的 json数据
	 * @param dir_code
	 * @return
	 */
	public JSONObject queryMyAttengCooperateLogByAjax(String dir_code){
		List<CooperateLogBean> list = queryMyAttengCooperateLog(dir_code);
		JSONArray cooperateLogs = JSONArray.fromObject(list);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result",1);
		jsonObject.put("message","成功");
		jsonObject.put("total",list.size());
		jsonObject.put("cooperateLogs", cooperateLogs);
		return jsonObject;
		
	}
}
