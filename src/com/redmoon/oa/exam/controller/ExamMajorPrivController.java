package com.redmoon.oa.exam.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.exam.MajorPriv;
import com.redmoon.oa.exam.MajorView;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

/**
 * @Description: 考试专业权限controller
 * @author: sht
 * @Date: 2018-4-27下午02:10:23
 */
@Controller
@RequestMapping("/majorPriv")
public class ExamMajorPrivController {

	@Autowired
	private HttpServletRequest request;
	
	/**
	 * 
	 * @Description: 专业用户权限添加
	 * @return 
	 * @throws JSONException 
	 * @throws ErrMsgException 
	 */
	@ResponseBody
	@RequestMapping(value="majorUserAdd" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String majorUserAdd() throws JSONException, ErrMsgException{
		
		String majorCode = ParamUtil.get(request, "majorCode");
		String name = ParamUtil.get(request, "name");
		JSONObject json = new JSONObject();
		MajorPriv majorPriv = new MajorPriv();
		if (name.equals("")) {
			json.put("ret", "0");
			json.put("msg", "名称不能为空！");
			return json.toString();
		}
		int type = ParamUtil.getInt(request, "type");
		String[] names = name.split("\\,");
		boolean re = false;
		for (String um : names) {
			//检测用户是否存在
			if (type == MajorPriv.TYPE_USER) {
				UserDb user = new UserDb();
				user = user.getUserDb(um);
				if (!user.isLoaded()) {
					continue;
				}
			}
			try {
				majorPriv.setMajorCode(majorCode);
				re =majorPriv.add(um,type);
			} catch (ErrMsgException e) {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
				return json.toString();
			}
		}
		if (re) {
			json.put("ret", "1");
			json.put("msg", "添加成功！");
		} else {
			json.put("ret", "0");
			json.put("msg", "添加失败！");
		}
		return json.toString();
	}
	
	/**
	 * 
	 * @Description: 专业角色权限添加
	 * @return
	 * @throws JSONException 
	 */
	@ResponseBody
	@RequestMapping(value="majorRoleAdd" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String majorRoleAdd() throws JSONException{
		
		String majorCode = ParamUtil.get(request, "majorCode");
		String roleCodes = ParamUtil.get(request, "roleCodes");
		JSONObject json = new JSONObject();
		boolean re;
		try {
			MajorPriv mp = new MajorPriv(majorCode);
			re = mp.setRoles(majorCode, roleCodes);
			if(re){
				json.put("ret", "1");
				json.put("msg", "操作成功！");
			}else{
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		}
		catch (Exception e) {
			json.put("ret", "0");
			json.put("msg", e.getMessage());
		}
		return json.toString();
		
	}
	
	/**
	 * 
	 * @Description: 专业权限删除
	 * @return
	 * @throws ErrMsgException 
	 * @throws JSONException 
	 */
	@ResponseBody
	@RequestMapping(value="majorDel" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String majorDel() throws ErrMsgException, JSONException{
		
		int id = ParamUtil.getInt(request, "id");
		MajorPriv mp = new MajorPriv();
		JSONObject json = new JSONObject();
		mp = mp.getMajorPriv(id);
		if (mp.del()){
			json.put("ret", "1");
			json.put("msg", "删除成功！");
		}else{
			json.put("ret", "0");
			json.put("msg", "删除失败！");
		}
		return json.toString();
	}
	
	/*
	 * 专业子节点添加controller
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="majorChildAdd" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String questionKindAdd() throws JSONException{
		
		String code = ParamUtil.get(request, "code");
		String name = ParamUtil.get(request, "name");
		String parentCode = ParamUtil.get(request, "parent_code").trim();
		String discription = ParamUtil.get(request, "subject");
		JSONObject json = new JSONObject();
		TreeSelectMgr tm = new TreeSelectMgr();
		boolean re = false;
		try {
			//判断名称是否有重复
			Vector children = tm.getChildren(parentCode);
			Iterator ri = children.iterator();
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				String oldName = childlf.getName();
				if (oldName.equals(name)) {
					json.put("ret", "2");
					json.put("msg", "请检查名称是否有重复!");
					return json.toString();
				}
			}
			//增加节点
			TreeSelectDb tsd = new TreeSelectDb(parentCode);
			TreeSelectDb ctsd = new TreeSelectDb();
			ctsd = ctsd.getTreeSelectDb(code);
			ctsd.setName(name);
			ctsd.setCode(code);
			ctsd.setDescription(discription);
			ctsd.setParentCode(parentCode);
			re = tsd.AddChild(ctsd);
			if (re) {
				json.put("ret", "1");
				json.put("msg", "添加成功！");
			} else {
				json.put("ret", "0");
				json.put("msg", "添加失败!");
			}
		} catch (ErrMsgException e) {
			
		}
		return json.toString();
	}
	
	/**
	 * 
	 * @Description: 专业权限修改
	 * @return
	 * @throws ErrMsgException 
	 * @throws JSONException 
	 */
	@ResponseBody
	@RequestMapping(value="majorModify" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String majorModify() throws ErrMsgException, JSONException{
		
		int id = ParamUtil.getInt(request, "id");
		int manage = ParamUtil.getInt(request, "manage");
		int invigilate = ParamUtil.getInt(request, "invigilate");
		MajorPriv mp = new MajorPriv();
		JSONObject json = new JSONObject();
		mp = mp.getMajorPriv(id);
		mp.setCanManage(manage);
		mp.setInvigilate(invigilate);
		if (mp.save()){
			json.put("ret", "1");
			json.put("msg", "修改成功！");
		}else{
			json.put("ret", "0");
			json.put("msg", "修改失败！");
		}
		return json.toString();
	}
}
