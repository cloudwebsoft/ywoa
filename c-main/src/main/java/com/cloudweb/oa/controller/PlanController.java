package com.cloudweb.oa.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.android.Privilege;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.person.PlanMgr;

@Controller
@RequestMapping("/public/plan")
public class PlanController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/getPlans", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String getPlans(@RequestParam int year, @RequestParam int month) {
		// month开始于1
		month -= 1;
		java.util.Date dtBegin = DateUtil.getDate(year, month, 1);
		java.util.Date dtEnd = DateUtil.getDate(year, month, DateUtil.getDayCount(year, month));
		// 往前往后各推7天
		dtBegin = DateUtil.addDate(dtBegin, -7);
		dtEnd = DateUtil.addDate(dtEnd, 7);
		
		String f = "yyyy-MM-dd";
		String strB = DateUtil.format(dtBegin, f);
		String strE = DateUtil.format(dtEnd, f);
		
		String skey = ParamUtil.get(request, "skey");
		Privilege pvg = new Privilege();
		String userName = pvg.getUserName(skey);
		
		String sql = "select id from user_plan where mydate >=" + SQLFilter.getDateStr(strB, f) + " and mydate <=" + SQLFilter.getDateStr(strE, f);
		boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
		if (isShared) {
			sql += " and is_shared=1";
		}
		else {
			sql += " and userName=" + StrUtil.sqlstr(userName);
		}
		sql += " order by mydate desc, enddate desc";

		JSONObject json = new JSONObject();
		try {
			PlanDb pd = new PlanDb();
			Vector v = pd.list(sql);
			Iterator ir = v.iterator();
			JSONObject result = new JSONObject();
			while (ir.hasNext()) {
			   pd = (PlanDb)ir.next();
			   JSONObject plan = new JSONObject();
			   plan.put("id", pd.getId());
			   plan.put("title", pd.getTitle());				   
			   plan.put("startTime", DateUtil.format(pd.getMyDate(),"yyyy-MM-dd HH:mm:ss"));				   
			   plan.put("endTime", DateUtil.format(pd.getEndDate(),"yyyy-MM-dd HH:mm:ss"));				   
			   plan.put("isClosed",String.valueOf(pd.isClosed()));
			   
			   String d = DateUtil.format(pd.getMyDate(), "yyyy-M-d");
			   if (result.has(d)){
				   JSONArray day = result.getJSONArray(d);
				   day.put(plan);
			   }
			   else {
					JSONArray day = new JSONArray();
					day.put(plan);
					result.put(d, day);
			   }
			}
			
			json.put("ret", 1);
			json.put("msg", "操作成功！");
			json.put("result", result);						
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return json.toString();				
	}
	
	@ResponseBody
	@RequestMapping(value = "/getNotepapers", produces={"text/html;","application/json;charset=UTF-8;"})		
	public String getNotepapers() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		String skey = ParamUtil.get(request, "skey");
		try {
			privilege.doLogin(request, skey);
			String userName = privilege.getUserName(skey);
			int curpage = ParamUtil.getInt(request, "pagenum", 1);
			int pagesize = ParamUtil.getInt(request, "pagesize", 20);			
			String sql = "select id from user_plan where userName="
				+ StrUtil.sqlstr(userName) + " and is_notepaper=1";
			String cond = ParamUtil.get(request, "cond");
			if ("title".equals(cond)){ 
				String title = request.getParameter("what");
				title = StrUtil.UnicodeToUTF8(title);
				if (!"".equals(title)){
					sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
				}
			}
			sql += " order by mydate desc, enddate desc";
			
			// LogUtil.getLog(getClass()).info("getNotepapers: sql=" + sql);

			PlanDb pd = new PlanDb();
			ListResult lr = pd.listResult(sql, curpage, pagesize);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			json.put("res", "0");
			json.put("msg", "操作成功");
			json.put("total", String.valueOf(lr.getTotal()));
			JSONObject result = new JSONObject();
			result.put("count", String.valueOf(pagesize));
			JSONArray ary = new JSONArray();
			while (ri.hasNext()) {
				pd = (PlanDb) ri.next();
				JSONObject plan = new JSONObject();
				plan.put("id", pd.getId());
				plan.put("title", pd.getTitle());				   
				plan.put("startTime", DateUtil.format(pd.getMyDate(),"yyyy-MM-dd HH:mm"));				   
				plan.put("endTime", DateUtil.format(pd.getEndDate(),"yyyy-MM-dd HH:mm"));				   
				plan.put("isClosed",String.valueOf(pd.isClosed()));
				ary.put(plan);
			}
			result.put("notepapers", ary);
			json.put("result", result);
		} catch (JSONException | ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/openPlan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String openPlan(@RequestParam int id) {
		JSONObject json = new JSONObject();
		try {
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			pd.setClosed(false);
			boolean re = pd.save();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();				
	}		
	
	@ResponseBody
	@RequestMapping(value = "/closePlan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String closePlan(@RequestParam int id) {
		JSONObject json = new JSONObject();
		try {
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			pd.setClosed(true);
			boolean re = pd.save();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();				
	}	
	
	@ResponseBody
	@RequestMapping(value = "/addPlan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String addPlan() {
		boolean re = true;
		String title, content, beginDate, endDate;
		String errmsg = "";
		boolean isRemind = false;

		title = request.getParameter("title");
		if (title.equals("")) {
			errmsg += "标题不能为空！\n";
		}
		content = request.getParameter("content");
		if (content.equals("")) {
			errmsg += "内容不能为空！\n";
		}
		beginDate = ParamUtil.get(request, "beginDate");
		endDate = ParamUtil.get(request, "endDate");

		if (endDate.equals("")) {
			endDate = beginDate;
		}

		int before = ParamUtil.getInt(request, "before", 0);
		if (before>0) {
			isRemind = true;
		}
		
		boolean isNotepaper = ParamUtil.getInt(request, "isNotepaper", 0)==1;
		boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
		boolean isClosed = ParamUtil.getInt(request, "isClosed", 0) == 1;
		
		boolean shared = ParamUtil.getInt(request, "shared", 0)==1;
		
		java.util.Date d = null;
		java.util.Date end = null;
		try {
			d = DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm");
			end = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm");
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
		}
		if (d == null || end == null)
			errmsg += "日期未填写或格式错误！\n";
		
		JSONObject json = new JSONObject();	
		
		if (!"".equals(errmsg)){ 
			try {
				json.put("ret", 0);
				json.put("msg", errmsg);				
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			return json.toString();			
		}
		
		String skey = ParamUtil.get(request, "skey");
		Privilege privilege = new Privilege();
		String userName = privilege.getUserName(skey);

		PlanDb pd = new PlanDb();
		pd = pd.getLastNotepaper(userName);
		int x = PlanDb.DEFAULT_X;
		int y = PlanDb.DEFAULT_y;
		if (pd!=null) {
			x = pd.getX() + 30;
			y = pd.getY() + 30;
		}
		else {
			pd = new PlanDb();
		}
		
		pd.setTitle(title);
		pd.setContent(content);
		pd.setMyDate(d);
		pd.setEndDate(end);
		pd.setUserName(userName);
		pd.setNotepaper(isNotepaper);
		pd.setMaker(userName);
		if (isRemind) {
			pd.setRemind(isRemind);
			java.util.Date dt = DateUtil.addMinuteDate(d, -before);
			pd.setRemindDate(dt);
		} else {
			pd.setRemind(isRemind);
		}
		
		pd.setRemindBySMS(isRemindBySMS);
		pd.setClosed(isClosed);
		pd.setShared(shared);
		pd.setX(x);
		pd.setY(y);
		try {
			re = pd.create();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (ErrMsgException | JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return json.toString();				
	}    	
	
	@ResponseBody
	@RequestMapping(value = "/editPlan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String editPlan(@RequestParam int id) {
		PlanDb pd = new PlanDb();
		pd = pd.getPlanDb(id);
		
		boolean re = true;
		String title, content, beginDate, endDate;
		String errmsg = "";
		boolean isRemind = false;

		title = request.getParameter("title");
		if (title.equals("")) {
			errmsg += "标题不能为空！\n";
		}
		content = request.getParameter("content");
		if (content.equals("")) {
			errmsg += "内容不能为空！\n";
		}
		beginDate = ParamUtil.get(request, "beginDate");
		endDate = ParamUtil.get(request, "endDate");

		if (endDate.equals("")) {
			endDate = beginDate;
		}

		boolean isNotepaper = ParamUtil.getInt(request, "isNotepaper", 0)==1;
		
		boolean shared = ParamUtil.getInt(request, "shared", 0)==1;

		int before = ParamUtil.getInt(request, "before", 0);
		if (before>0) {
			isRemind = true;
		}
		boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
		// boolean isClosed = ParamUtil.getInt(request, "isClosed", 0) == 1;
		java.util.Date d = null;
		java.util.Date end = null;
		try {
			d = DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm");
			end = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm");
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
		}
		if (d == null || end == null) {
			errmsg += "日期未填写或格式错误！\n";
		}
		
		JSONObject json = new JSONObject();	
		
		if (!"".equals(errmsg)){ 
			try {
				json.put("ret", 0);
				json.put("msg", errmsg);				
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			return json.toString();			
		}
		
		String skey = ParamUtil.get(request, "skey");
		Privilege privilege = new Privilege();
		String userName = privilege.getUserName(skey);

		pd.setTitle(title);
		pd.setContent(content);
		pd.setMyDate(d);
		pd.setEndDate(end);
		pd.setUserName(userName);
		if (isRemind) {
			pd.setRemind(isRemind);
			java.util.Date dt = DateUtil.addMinuteDate(d, -before);
			pd.setRemindDate(dt);
		} else {
			pd.setRemind(isRemind);
		}
		pd.setNotepaper(isNotepaper);
		pd.setRemindBySMS(isRemindBySMS);
		// pd.setClosed(isClosed);
		pd.setShared(shared);
		try {
			re = pd.save();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (ErrMsgException | JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return json.toString();				
	}    	
	
	@ResponseBody
	@RequestMapping(value = "/delPlan", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String delPlan(@RequestParam int id) {
		JSONObject json = new JSONObject();
		try {
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			boolean re = pd.del();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();				
	}	
	
	@ResponseBody
	@RequestMapping(value = "/delPlanBatch", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})	
	public String delPlanBatch() {
		JSONObject json = new JSONObject();
		try {
			PlanMgr pm = new PlanMgr();
			pm.delBatch(request);

			json.put("ret", 1);
			json.put("msg", "操作成功！");
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();				
	}		
	
	@ResponseBody
	@RequestMapping(value = "/saveNotepaperPos", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})		
	public String saveNotepaperPos() {
		int id = ParamUtil.getInt(request, "id", -1);
		int left = ParamUtil.getInt(request, "left", -1);
		int top = ParamUtil.getInt(request, "top", -1);
		JSONObject json = new JSONObject();
		try {
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			pd.setX(left);
			pd.setY(top);
			boolean re = pd.save();
			if (re) {
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();			
	}
	
	@ResponseBody
	@RequestMapping(value = "/shareNotepaper", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})		
	public String shareNotepaper() {
		int id = ParamUtil.getInt(request, "id", -1);
		JSONObject json = new JSONObject();
		try {
			String op = ParamUtil.get(request, "op");
			boolean isShare;
			if ("share".equals(op)) {
				isShare = true;
			}
			else {
				isShare = false;
			}
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			pd.setShared(isShare);
			boolean re = pd.save();
			if (re) {
				String msg;
				if (isShare) {
					msg = "共享成功";
				}
				else {
					msg = "取消共享成功";
				}				
				json.put("ret", 1);
				json.put("msg", msg);
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();			
	}	
	
	@ResponseBody
	@RequestMapping(value = "/saveNotepaperContent", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})		
	public String saveNotepaperContent() {
		int id = ParamUtil.getInt(request, "id", -1);
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
		String userName = pvg.getUser(request);
		String content = ParamUtil.get(request, "content");
		String startTime = ParamUtil.get(request, "startTime");
		int x = ParamUtil.getInt(request, "left", 30);
		int y = ParamUtil.getInt(request, "top", 30);
		JSONObject json = new JSONObject();
		try {
			boolean re = false;
			PlanDb pd = new PlanDb();		
			// 创建
			if (id<0) {
				pd.setTitle(content);
				pd.setContent(content);
				Date d = DateUtil.parse(startTime, "yyyy-MM-dd HH:mm");
				pd.setMyDate(d);
				pd.setUserName(userName);
				pd.setMaker(userName);
				pd.setEndDate(d);
				pd.setZdrq(d);
				pd.setNotepaper(true);
				Date remindDate = DateUtil.addMinuteDate(d, -10);
				pd.setRemindDate(remindDate);
				pd.setX(x);
				pd.setY(y);
				re = pd.create();
				json.put("id", pd.getId());
			}
			else {
				pd = pd.getPlanDb(id);
				pd.setContent(content);
				Date d = DateUtil.parse(startTime, "yyyy-MM-dd HH:mm");
				pd.setMyDate(d);
				pd.setX(x);
				pd.setY(y);				
				re = pd.save();
			}
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();			
	}	
	
	@ResponseBody
	@RequestMapping(value = "/closeNotepaper", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})		
	public String closeNotepaper() {
		int id = ParamUtil.getInt(request, "id", -1);
		JSONObject json = new JSONObject();
		try {
			if (id<0) { // 防止新建了一个空的，但还没有保存过
				json.put("ret", 1);
				json.put("msg", "操作成功！");
				return json.toString();
			}
			PlanDb pd = new PlanDb();
			pd = pd.getPlanDb(id);
			pd.setClosed(true);
			boolean re = pd.save();
			if (re) { 
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			}
			else {
				json.put("ret", 0);
				json.put("msg", "操作失败！");				
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			try {
				json.put("ret", 0);
				json.put("msg", e.getMessage());				
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return json.toString();			
	}		
}
