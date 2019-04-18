package com.redmoon.oa.android;

import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.UserDb;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;

public class NoticeListAction extends ActionSupport {
	private String skey = "";
	private String op = "";
	private String what = ""; // 查询内容

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

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

	private String cond = ""; // 查询列表值

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	private String result = "";
	private int pagenum;
	private int pagesize;

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
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}

			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			boolean canAdd = false;
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			
			boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
			boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
			if (isNoticeAll|| isNoticeMgr) {
				canAdd = true;
			}
			String userName = privilege.getUserName(getSkey());
			String unitCode = privilege.getUserUnitCode(skey);
			String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
			String sql = "";
			if(isNoticeAll) {
				sql = "select id from oa_notice o where 1=1";
			}else if(isNoticeMgr){
				sql = "select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = "+StrUtil.sqlstr(userName)+" and o.unit_code =" +StrUtil.sqlstr(unitCode);
			}else{
				sql = "select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = "+StrUtil.sqlstr(userName)+" and o.begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (o.end_date is null or o.end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ")";
			}
			if (getOp().equals("search")) {
				sql += " and o." + getCond() + " like "
						+ StrUtil.sqlstr("%" + getWhat() + "%") + "";
			}
			sql += " order by id desc ";

			int curpage = getPagenum();
			int pagesize = getPagesize();
			NoticeDb nd = new NoticeDb();
			ListResult lr = nd.listResult(sql, curpage, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("total", String.valueOf(lr.getTotal()));
			json.put("canAdd", "" + canAdd);
			JSONObject result = new JSONObject();
			result.put("count", String.valueOf(pagesize));
			JSONArray notices = new JSONArray();
			UserDb user = new UserDb();
			while (ri.hasNext()) {
				NoticeDb rr = (NoticeDb) ri.next();
				JSONObject notice = new JSONObject();
				notice.put("id", String.valueOf(rr.getId()));
				notice.put("title", rr.getTitle());
				notice.put("sender", user.getUserDb(rr.getUserName())
						.getRealName());
				notice.put("expirydate", DateUtil.format(rr.getEndDate(),
						"yyyy-MM-dd HH:mm"));
				notice.put("createdate",DateUtil.parseDate(DateUtil.format(rr.getCreateDate(), DateUtil.DATE_TIME_FORMAT)));
				notices.put(notice);
			}

			result.put("notices", notices);

			json.put("result", result);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setResult(json.toString());
		return "SUCCESS";
	}
}
