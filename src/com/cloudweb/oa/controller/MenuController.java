package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import com.redmoon.oa.pvg.PrivDb;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RolePrivDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-12-3下午05:20:45
 */
@Controller
@RequestMapping("/admin")
public class MenuController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/setMenuPriv", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
	public String setMenuPriv() {
		boolean re = false;
		
		String roleCode = ParamUtil.get(request, "roleCode");
		RolePrivDb rpd = new RolePrivDb();
		
		boolean isPriv = ParamUtil.getBoolean(request, "isPriv", false);
		String priv = ParamUtil.get(request, "priv");
		if (isPriv) {
			String[] ary = StrUtil.split(priv, ",");
			if (ary!=null) {
				for (int k = 0; k < ary.length; k++) {
					rpd.setPriv(ary[k]);
					rpd.setRoleCode(roleCode);
					try {
						re = rpd.create();
					} catch (ErrMsgException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			String[] ary = StrUtil.split(priv, ",");
			if (ary!=null) {
				for (int k = 0; k < ary.length; k++) {
					try {
						rpd = rpd.getRolePrivDb(roleCode, ary[k]);
						if (rpd.isLoaded()) {
							re = rpd.del();
						}
					} catch (ErrMsgException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		JSONObject json = new JSONObject();
		try {
			if (re) {
				json.put("ret", 1);
				json.put("msg", "操作成功！");
			} else {
				json.put("ret", 0);
				json.put("msg", "操作失败");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();		
	}
}
