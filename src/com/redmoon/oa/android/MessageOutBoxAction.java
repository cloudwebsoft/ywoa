package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.message.MessageDb;

public class MessageOutBoxAction {
	private String skey = "";
	private String result = "";
	private String op = "";
	private String cond = ""; //查询列表值
	private String what = "";

	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getCond() {
		return cond;
	}
	public void setCond(String cond) {
		this.cond = cond;
	}
	public String getWhat() {
		return what;
	}
	public void setWhat(String what) {
		this.what = what;
	}

	private int pagenum;
	private int pagesize;
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
		
		try {
			MessageDb md = new MessageDb();
			String sql = "select id from oa_message where is_sent=1 and sender="+StrUtil.sqlstr(privilege.getUserName(getSkey()))+" and box=" + MessageDb.OUTBOX +" and is_sender_dustbin = 0";
			if (getOp().equals("search")) {
				if (getCond().equals("title"))
					sql += " and title like " + StrUtil.sqlstr("%" + getWhat() + "%");
				else
					sql += " and content like " + StrUtil.sqlstr("%" + getWhat()  + "%");
			}	
			sql += " order by isreaded asc,rq desc";
			int curpage = getPagenum();
			int pagesize = getPagesize();
			ListResult lr = md.listResult(sql, curpage, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			//JdbcTemplate jt = new JdbcTemplate();
			//HttpServletRequest request = ServletActionContext.getRequest();
			//Paginator paginator = new Paginator(request);
			//ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
			//long total = ri.getTotal();
			//paginator.init(total, pagesize);
			// 设置当前页数和总页数
			//int totalpages = paginator.getTotalPages();
			
					
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("total",String.valueOf(lr.getTotal()));
			
			JSONObject result = new JSONObject(); 
			result.put("count",String.valueOf(pagesize));
			
			JSONArray messages = new JSONArray(); 		
			
			while (ri.hasNext()) {
				MessageDb rr = (MessageDb)ri.next();	
				
				JSONObject message = new JSONObject();
				message.put("id",String.valueOf(rr.getId()));
				message.put("title",rr.getTitle());
				message.put("sender",rr.getSenderRealName());
				message.put("createdate",String.valueOf(rr.getSendTime()));
				
				messages.put(message);
			}	
			result.put("messages",messages);		
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

