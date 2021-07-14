package com.cloudweb.oa.controller;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.sys.DebugUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOLog;
import com.redmoon.oa.visual.FormulaResult;
import com.redmoon.oa.visual.FormulaUtil;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

@Controller
@RequestMapping("/visual/formula")
public class FormulaController {
	@Autowired  
	private HttpServletRequest request;
	
	@ResponseBody
	@RequestMapping(value = "/getParams", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})
	public String getParams(@RequestParam(value = "code", required = true) String code) {		
		String params = "";
		JSONObject json = new JSONObject();		
		boolean re = false;
		try {
			String sql = "select params from form_table_formula where code=?";
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code});
			if (ri.hasNext()) {
				re = true;
				ResultRecord rr = (ResultRecord)ri.next();
				params = StrUtil.getNullStr(rr.getString(1));
			}
			
			if (re) {
		    	json.put("ret", "1");
				json.put("msg", "操作成功！");		
				json.put("params", params);		
			}
			else {
		    	json.put("ret", "0");
				json.put("msg", "操作失败！");
			}
		}
		catch (JSONException e) {
			try {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} 
		catch (SQLException e) {
			try {
				json.put("ret", "0");
				json.put("msg", "操作失败！");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/doFormula", method = RequestMethod.POST, produces={"text/html;charset=UTF-8;", "application/json;"})
	public String doFormula() {	
		String formula = ParamUtil.get(request, "formula");
    	String val = "";
		JSONObject json = new JSONObject();
    	try {
			FormulaResult fr = FormulaUtil.render(formula);
			val = fr.getValue();
		} catch (ErrMsgException e) {
    		// e.printStackTrace();
			DebugUtil.e(getClass(), "doFormula formula", formula + " error: " + e.getMessage());
			try {
				json.put("ret", "0");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return json.toString();
		}		
		try {
			json.put("ret", "1");
			json.put("msg", "操作成功！");
			json.put("value", val);
		}
		catch (JSONException e) {
			e.printStackTrace();
		} 
		return json.toString();
	}	
}

