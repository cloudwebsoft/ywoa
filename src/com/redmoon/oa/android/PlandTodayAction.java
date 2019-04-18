package com.redmoon.oa.android;

import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.person.PlanDb;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

public class PlandTodayAction {
	private String skey = "";
	private String result = "";

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
		Calendar cc = Calendar.getInstance();
		int year = cc.get(Calendar.YEAR);
		int week = cc.get(Calendar.WEEK_OF_YEAR);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		String date1 = format.format(cc.getTime()) + " 00:00:00";
		String date2 = format.format(cc.getTime()) + " 23:59:59";
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			String sql = "select id from user_plan where userName="
					+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
					+ " and ((remindDate>="
					+ SQLFilter.getDateStr(date1, "yyyy-MM-dd HH:mm:ss")
					+ " and remindDate<="
					+ SQLFilter.getDateStr(date2, "yyyy-MM-dd HH:mm:ss")
					+ ")) order by remindDate";
	
			PlanDb pd = new PlanDb();
			Vector v = pd.list(sql);
			Iterator ir = null;
			if (v != null)
				ir = v.iterator();
			JSONArray result = new JSONArray();
			while (ir != null && ir.hasNext()) {
				pd = (PlanDb) ir.next();
				JSONObject plan = new JSONObject();
				plan.put("id", String.valueOf(pd.getId()));
				plan.put("title", pd.getTitle());
				plan.put("content", pd.getContent());
				plan.put("is_closed",String.valueOf(pd.isClosed()));		
				plan.put("startDate", DateUtil.format(pd.getMyDate(),
						"yyyy-MM-dd HH:mm:ss"));
				plan.put("endDate", DateUtil.format(pd.getEndDate(),
						"yyyy-MM-dd HH:mm:ss"));
				plan.put("is_remind",String.valueOf(pd.isRemind()));		
				plan.put("remindDate",String.valueOf(pd.getRemindDate()));		
				result.put(plan);
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
