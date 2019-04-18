package com.redmoon.oa.android;

import java.sql.SQLException;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

public class PlanMonthAction {
	private String y = "";
	private String m = "";
	private String skey = "";
	private String result = "";

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONObject json = new JSONObject();

		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Calendar c1 = Calendar.getInstance();
		int year = c1.get(Calendar.YEAR);
		if (getY().equals("")) {
			setY(String.valueOf(year));
		}
		if (getM().equals("")) {
			setM(String.valueOf(c1.get(Calendar.MONTH) + 1));
		}

		int dd = DateUtil.getDayCount(StrUtil.toInt(getY()), StrUtil.toInt(getM()) - 1);
		JdbcTemplate rmconn = new JdbcTemplate();
		ResultIterator ri;
		int count = 0;
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONArray result = new JSONArray();
			String sql ="";
			for (int i = 1; i <= dd; i++) {
				sql = "select count(id) from user_plan where (("
						+ SQLFilter.year("mydate") + "=" + y + " and "
						+ SQLFilter.month("mydate") + "=" + m + " and "
						+ SQLFilter.day("mydate") + "=" + i + ") or ("
						+ SQLFilter.year("enddate") + "=" + y + " and "
						+ SQLFilter.month("enddate") + "=" + m + " and "
						+ SQLFilter.day("enddate") + "=" + i
						+ ")) and userName="
						+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
						+ " order by mydate,enddate";
				ri = rmconn.executeQuery(sql);
				ResultRecord rr = null;
				while (ri.hasNext()) {
					rr = (ResultRecord) ri.next();
					JSONObject dates = new JSONObject();
					count = rr.getInt(1);
					dates.put("y", String.valueOf(y));
					dates.put("m", String.valueOf(m));
					dates.put("d", String.valueOf(i));
					dates.put("count", String.valueOf(count));
					result.put(dates);
				}
				json.put("result", result);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
