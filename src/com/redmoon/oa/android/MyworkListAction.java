package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.redmoon.oa.worklog.WorkLogDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class MyworkListAction {
	private String skey = "";
	private String result = "";
	private int pagenum;
	private int pagesize;
	private String op = "";
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getWhat() {
		return what;
	}
	public void setWhat(String what) {
		this.what = what;
	}
	private String what = "";	
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
		if(re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		String userName = privilege.getUserName(getSkey()) ;
		String sql = "select id from work_log where userName="+StrUtil.sqlstr(userName)+" and log_type=" + WorkLogDb.TYPE_NORMAL;
		if (getOp().equals("search")) {
			sql +=" and content like " + StrUtil.sqlstr("%" + getWhat() + "%");
		}
		sql +=" order by myDate desc";

		int curpage = getPagenum();   //第几页
		int pagesize = getPagesize(); //每页显示多少条
		
		WorkLogDb wld = new WorkLogDb();
		try {
			HttpServletRequest request = ServletActionContext.getRequest();

			ListResult lr = wld.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("total",String.valueOf(total));
			Vector v = lr.getResult();
			Iterator ir = null;
			if (v!=null)
				ir = v.iterator();		
			JSONObject result = new JSONObject(); 
			result.put("count",String.valueOf(pagesize));
			JSONArray wldArray  = new JSONArray(); 	
			while (ir!=null && ir.hasNext()) {
				wld = (WorkLogDb)ir.next();
				JSONObject wlds = new JSONObject(); 
				wlds.put("id",String.valueOf(wld.getId()));
				wlds.put("date",DateUtil.format(wld.getMyDate(), "yyyy-MM-dd"));
				// wlds.put("content",privilege.delHTMLTag(StrUtil.getLeft(wld.getContent(), 120)));				
				wlds.put("content", StrUtil.getAbstract(request, wld.getContent(), 20000, "\r\n"));				
				wldArray.put(wlds);			
			}
			result.put("myworks",wldArray);		
			json.put("result",result);		
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
