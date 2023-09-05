package com.cloudweb.oa.visual;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FuncUtil;
import com.redmoon.oa.visual.ModuleImportTemplateDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-9-10上午11:41:38
 */

@Controller
@RequestMapping("/visual")
public class FuncController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/getFuncVal", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;","application/json;"})
	public String getFuncVal(@RequestParam String formCode, @RequestParam String fieldName, @RequestParam String fieldNames) {		
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		
		String[] ary = StrUtil.split(fieldNames, ",");
		int len = 0;
		if (ary!=null) {
			len = ary.length;
		}
		
		FormDAO fdao = new FormDAO(fd);
		for (int i=0; i<len; i++) {
			String fName = ary[i];
			String fValue = ParamUtil.get(request, fName);
			fdao.setFieldValue(fName, fValue);			
		}
		
		String val = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
		JSONObject json = new JSONObject();
		try {
			json.put("ret", 1);
			json.put("msg", "");
			json.put("val", val);
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return json.toString();
	}
}
