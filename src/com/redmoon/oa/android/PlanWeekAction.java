package com.redmoon.oa.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.person.PlanDb;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.StrUtil;

public class PlanWeekAction {
	private String y = "";
	private String w = "";

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getW() {
		return w;
	}

	public void setW(String w) {
		this.w = w;
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

	private String skey = "";
	private String result = "";

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

		Calendar cc = Calendar.getInstance();
		int year = cc.get(Calendar.YEAR);
		cc.setFirstDayOfWeek(Calendar.MONDAY);
		if (getY().equals("")) {
			setY(String.valueOf(year));
		}
		if (getW().equals("")) {
			w = String.valueOf(cc.get(Calendar.WEEK_OF_YEAR));
		}
		int yy = StrUtil.toInt(getY());
		int ww = StrUtil.toInt(getW());
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.set(Calendar.YEAR, yy);
		cal.set(Calendar.WEEK_OF_YEAR, ww);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String[] date_week = new String[7];
		date_week[0] = format.format(cal.getTime());
		String date1 = date_week[0] + " 00:00:00";
		String date2 = date_week[0] + " 23:59:59";

		PlanDb pd = new PlanDb();
		Vector v = null;
		Iterator ir = null;

		int size = 0;

		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONArray result = new JSONArray();
			JSONObject dates1 = new JSONObject();
			String sql = "select id from user_plan where userName="
					+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
					+ " and ((mydate>="
					+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
					+ " and mydate<="
					+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
					+ ") or (enddate>="
					+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
					+ " and enddate<="
					+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
					+ ") or (mydate<="
					+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
					+ " and enddate>="
					+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
					+ ")) order by mydate,enddate";

			v = pd.list(sql);
			if (v != null)
				size = v.size();
			dates1.put("count", String.valueOf(size));
			dates1.put("y", String.valueOf(date_week[0]).substring(0, 4));
			dates1.put("m", String.valueOf(date_week[0]).substring(5, 7));
			dates1.put("d", String.valueOf(date_week[0]).substring(8, 10));
			result.put(dates1);
			for (int i = 1; i < 7; i++) {
				long temp = cal.getTimeInMillis() + 24 * 60 * 60 * 1000;
				cal.setTimeInMillis(temp);
				date_week[i] = format.format(cal.getTime());

				date1 = date_week[i] + " 00:00:00";
				date2 = date_week[i] + " 23:59:59";
				sql = "select id from user_plan where userName="
						+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
						+ " and ((mydate>="
						+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
						+ " and mydate<="
						+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
						+ ") or (enddate>="
						+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
						+ " and enddate<="
						+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
						+ ") or (mydate<="
						+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
						+ " and enddate>="
						+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
						+ ")) order by mydate,enddate";

				v = pd.list(sql);
				if (v != null)
					size = v.size();
				JSONObject dates = new JSONObject();
				dates.put("count", String.valueOf(size));
				dates.put("y", String.valueOf(date_week[i]).substring(0, 4));
				dates.put("m", String.valueOf(date_week[i]).substring(5, 7));
				dates.put("d", String.valueOf(date_week[i]).substring(8, 10));
				result.put(dates);
			}
			json.put("result", result);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setResult(json.toString());
		return "SUCCESS";
	}
}
