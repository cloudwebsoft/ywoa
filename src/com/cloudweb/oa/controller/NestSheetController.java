package com.cloudweb.oa.controller;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.NestSheetCtl;
import com.redmoon.oa.visual.FormDAO;

/**
 * @Description: wm
 * @author: 
 * @Date: 2017-10-17下午04:19:01
 */
@Controller
@RequestMapping("/nestsheetctl")
public class NestSheetController {
	
	@Autowired
	private HttpServletRequest request;
		
	/**
	 * 调用自动嵌套表的autoSel进行拉单操作
	 * @Description: 
	 * @return
	 * @throws JSONException
	 * @throws ErrMsgException 
	 * @throws JSONException 
	 * @throws SQLException 
	 */
	@ResponseBody
	@RequestMapping(value="autoSel" ,produces={"text/html;charset=UTF-8;","application/json;"})
	public String autoSel() throws ErrMsgException, JSONException, SQLException{
		JSONObject json = new JSONObject();
		long parentId = ParamUtil.getLong(request, "parentId", -1);
		// 当为-1时，有可能是在模块中添加时拉单
		if (parentId==-1) {
			/*
			json.put("ret", "1");
			json.put("msg", "");
			return json.toString();
			*/
		}
		String ret = "1";
		String msg = "成功";
		String parentFormCode = ParamUtil.get(request, "parentFormCode");
		String nestFieldName = ParamUtil.get(request, "nestFieldName");
		FormDb parentFd = new FormDb();
		parentFd = parentFd.getFormDb(parentFormCode);
		FormField nestField = parentFd.getFormField(nestFieldName);
		// 插入表单之前删除之前自动拉单产生的垃圾数据 ,根据cws_id = parentId--武蒙
		String nestFormCode = "";
		try {
			String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
			JSONObject j = new JSONObject(defaultVal);
			nestFormCode = j.getString("destForm");
		} catch (JSONException e) {
			throw new ErrMsgException("JSON解析失败！");
		}
		// 存在了刷新反复自动拉单导致覆盖的bug判断是否是首次计入,如果是判断嵌套表中是否有数据,如果没有进行拉单操作,如果有不进行拉单
		boolean isFirst = ParamUtil.getBoolean(request, "isFirst", false);
		if(isFirst){ // 第一次,拉单
			String selSel = "select id from form_table_"+nestFormCode+" where cws_id = " +StrUtil.sqlstr(String.valueOf(parentId))+";";
			FormDAO fdao = new FormDAO();
			java.util.Vector v = fdao.list(nestFormCode, selSel);
			if(v.size() == 0){ // 没有数据进行拉单
				boolean boo = NestSheetCtl.autoSel(request, parentId, nestField);
				// end of nestsheetctl
				if(!boo){
					ret = "0";
					msg = "自动拉单失败";
				}
			}
		}else{ // 不是首次进入的第一次,要进行拉单操作
			String delSql = "delete from form_table_" + nestFormCode + " where cws_id = " +StrUtil.sqlstr(String.valueOf(parentId)) + ";";
			JdbcTemplate jt = new JdbcTemplate();
			jt.executeUpdate(delSql);
			// 删除结束
			// nestsheetctl request 设置了filter和 flowId
			boolean boo = NestSheetCtl.autoSel(request, parentId, nestField);
			// end of nestsheetctl
			if(!boo){
				ret = "0";
				msg = "自动拉单失败";
			}
		}
		json.put("ret", ret);
		json.put("msg", msg);
		json.put("nestformCode", nestFormCode);
		json.put("newIds", StrUtil.getNullStr((String)request.getAttribute("newIds")));
		return json.toString();
	}
	
}
