package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.map.LocationDb;

public class LocationListAction  {
	private String skey = "";
	private String result = "";
	private int pagenum;
	private int pagesize;
	private String op = "";
	private int flag;
	
	/**
	 * @return the flag
	 */
	public int getFlag() {
		return flag;
	}
	/**
	 * @param flag the flag to set
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String userName = privilege.getUserName(getSkey()) ;
		String sql = "select id from oa_location where user_name="+StrUtil.sqlstr(userName)+" and type = "+flag;
		if (getOp().equals("search")) {
			sql +=" and remark like " + StrUtil.sqlstr("%" + getWhat() + "%");
		}
		sql +=" order by create_date desc";
		
		int curpage = getPagenum();   //第几页
		int pagesize = getPagesize(); //每页显示多少条
		
		LocationDb wld = new LocationDb();
		try {
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
				wld = (LocationDb)ir.next();
				JSONObject wlds = new JSONObject(); 
				wlds.put("id",String.valueOf(wld.getLong("id")));
				wlds.put("date",DateUtil.format(wld.getDate("create_date"), "yyyy-MM-dd"));
				wlds.put("address", StrUtil.getNullStr(wld.getString("address")));				
				wlds.put("remark", wld.getString("remark"));
				wlds.put("fileSize", wld.getLong("file_size"));
				wlds.put("lontitude", wld.getDouble("lontitude"));
				wlds.put("latitude", wld.getDouble("latitude"));
				String diskName = StrUtil.getNullStr(wld.getString("file_path"));
				int p = diskName.lastIndexOf("/");
				if (p!=-1)
					diskName = diskName.substring(p+1);
				wlds.put("diskName", diskName);
				
				wldArray.put(wlds);			
			}
			result.put("locations",wldArray);		
			json.put("result",result);		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}	
}