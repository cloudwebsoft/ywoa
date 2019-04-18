package com.redmoon.oa.android;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.PlanDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class PlandDetailAction {
	private String y = "";
	private String m = "";
	private String d = "";
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

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
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
	
	private int pagenum = 1;
	private int pagesize = 20;
		
	public int getPagenum() {
		return pagenum;
	}

	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
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
		
		boolean isListAll= false;
		
		if (getY().equals("")) {
			isListAll = true;			
			setY(String.valueOf(year));
		}
		if (getM().equals("")) {
			setM(String.valueOf(c1.get(Calendar.MONTH) + 1));
		}
		if (getD().equals("")) {
			setD(String.valueOf(c1.get(Calendar.DATE)));
		}
		
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			String sql = "select id from user_plan where (("
				+ SQLFilter.year("mydate") + "=" + y + " and "
				+ SQLFilter.month("mydate") + "=" + m + " and "
				+ SQLFilter.day("mydate") + "=" + d + ") or ("
				+ SQLFilter.year("enddate") + "=" + y + " and "
				+ SQLFilter.month("enddate") + "=" + m + " and "
				+ SQLFilter.day("enddate") + "=" + d
				+ ")) and userName="
				+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
				+ " order by mydate,enddate";
			
			if (isListAll) {
				sql = "select id from user_plan where userName="
					+ StrUtil.sqlstr(privilege.getUserName(getSkey()))
					+ " order by mydate desc, enddate desc";		
			}		
			
			PlanDb pd = new PlanDb();
			Vector v = null;
			int total = 0;
			if (isListAll) {
				int curpage = getPagenum();
				int pagesize = getPagesize();
				ListResult lr = pd.listResult(sql, curpage, pagesize);
				v = lr.getResult();
				total = lr.getTotal();
			}
			else {
				v = pd.list(sql);
				total = v.size();
			}
			json.put("total", String.valueOf(total));
			Iterator ir = null;
			if(v!=null)
				ir = v.iterator(); 
			
			JSONArray result = new JSONArray();
			while (ir!=null && ir.hasNext()) {
				   pd = (PlanDb)ir.next();
				   JSONObject plan = new JSONObject();
				   plan.put("id", String.valueOf(pd.getId()));	
				   plan.put("title", pd.getTitle());				   
				   plan.put("content", pd.getContent());				   
				   plan.put("startDate", DateUtil.format(pd.getMyDate(),"yyyy-MM-dd HH:mm:ss"));				   
				   plan.put("endDate", DateUtil.format(pd.getEndDate(),"yyyy-MM-dd HH:mm:ss"));				   
				   plan.put("is_closed",String.valueOf(pd.isClosed()));	
				   plan.put("is_remind",String.valueOf(pd.isRemind()));		
				   plan.put("remindDate",String.valueOf(pd.getRemindDate()));
				   plan.put("isToMobile",String.valueOf(pd.isRemindBySMS()));
				   result.put(plan);
			}
			json.put("result", result);		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		setResult(json.toString());
		return "SUCCESS";
	}
}
