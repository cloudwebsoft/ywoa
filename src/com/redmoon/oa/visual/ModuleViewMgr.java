package com.redmoon.oa.visual;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.util.HttpPostFileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.RoleDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2018-1-12上午10:56:00
 */
public class ModuleViewMgr {
	
	public boolean add(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);

		String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
		String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
		String listFieldLink = StrUtil.getNullStr(msd.getString("list_field_link"));

		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("listField", new String[]{listField});
		reqMap.put("listFieldWidth", new String[]{listFieldWidth});
		reqMap.put("listFieldOrder", new String[]{listFieldOrder});
		reqMap.put("listFieldLink", new String[]{listFieldLink});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}
		
		License lic = License.getInstance();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});
		reqMap.put("cwsIp", new String[]{request.getServerName()});
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});

		reqMap.put("version", new String[]{cfg.get("version")});
		
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/add.do";
		
		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");	
			msd.set("list_field", result.getString("listField"));
			msd.set("list_field_width", result.getString("listFieldWidth"));
			msd.set("list_field_order", result.getString("listFieldOrder"));
			msd.set("list_field_link", result.getString("listFieldLink"));			
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("retStr=" + retStr);
			e.printStackTrace();
		}
		return re;
	}	
	
	public boolean del(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String fieldName = ParamUtil.get(request, "fieldName");

		String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
		String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
		String listFieldLink = StrUtil.getNullStr(msd.getString("list_field_link"));
		
		// list_field_link是后来新增的，所以要检查并初始化以兼容之前的版本
		if (!listField.equals("") && listFieldLink.equals("")) {
			String[] fieldAry = StrUtil.split(listField, ",");
			for (int i=0; i<fieldAry.length; i++) {
				if (listFieldLink.equals(""))
					listFieldLink = "#";
				else
					listFieldLink += ",#";
			}
		}
		
		String[] fieldAry = StrUtil.split(listField, ",");
		String[] widthAry = StrUtil.split(listFieldWidth, ",");
		String[] orderAry = StrUtil.split(listFieldOrder, ",");
		String[] linkAry = StrUtil.split(listFieldLink, ",");
		
		listField = "";
		listFieldOrder = "";
		listFieldWidth = "";
		listFieldLink = "";

		int len = fieldAry.length;
		for (int i=0; i<len; i++) {
			if (fieldAry[i].equals(fieldName)) {
				continue;
			}
			if (listField.equals("")) {
				listField = fieldAry[i];
				listFieldWidth = widthAry[i];
				listFieldOrder = orderAry[i];
				listFieldLink = linkAry[i];
			}
			else {
				listField += "," + fieldAry[i];
				listFieldWidth += "," + widthAry[i];
				listFieldOrder += "," + orderAry[i];
				listFieldLink += "," + linkAry[i];			
			}
		}
		msd.set("list_field", listField);
		msd.set("list_field_width", listFieldWidth);
		msd.set("list_field_order", listFieldOrder);
		msd.set("list_field_link", listFieldLink);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;
	}	
		
	public boolean modify(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);

		String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
		String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
		String listFieldLink = StrUtil.getNullStr(msd.getString("list_field_link"));

		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("listField", new String[]{listField});
		reqMap.put("listFieldWidth", new String[]{listFieldWidth});
		reqMap.put("listFieldOrder", new String[]{listFieldOrder});
		reqMap.put("listFieldLink", new String[]{listFieldLink});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}
		
		License lic = License.getInstance();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});
		reqMap.put("cwsIp", new String[]{request.getServerName()});
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});
		
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/modify.do";
		
		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");	
			
			msd.set("list_field", result.getString("listField"));
			msd.set("list_field_width", result.getString("listFieldWidth"));
			msd.set("list_field_order", result.getString("listFieldOrder"));
			msd.set("list_field_link", result.getString("listFieldLink"));		

			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return re;
	}
		
	
	public boolean addTag(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		double tagOrder = ParamUtil.getDouble(request, "tagOrder", -1);
		if (tagOrder==-1) {
			throw new ErrMsgException("请填写顺序号！");
		}
		String tagUrl = ParamUtil.get(request, "tagUrl");
		if (tagUrl.equals(""))
			tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组
			
		String tagType = ParamUtil.get(request, "tagType");
		if (tagType.equals("module")) {
			String tagModuleCode = ParamUtil.get(request, "tagModuleCode");
			JSONObject json = new JSONObject();
			try {
				json.put("moduleCode", tagModuleCode);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tagUrl = json.toString();
		}
			
		String tagName = ParamUtil.get(request, "tagNameAdd");
		
		String tName = StrUtil.getNullStr(msd.getString("nav_tag_name"));	
		String tOrder = StrUtil.getNullStr(msd.getString("nav_tag_order"));
		String tUrl = StrUtil.getNullStr(msd.getString("nav_tag_url"));
		if (tName.equals("")) {
			tName = tagName;
			tUrl = tagUrl;
			tOrder = "" + tagOrder;
		}
		else {
			tName += "," + tagName;
			tUrl += "," + tagUrl;
			tOrder += "," + tagOrder;
		}

		// 根据tagOrder排序
		String[] strOrderAry = StrUtil.split(tOrder, ",");
		int len = strOrderAry.length;
		double[] orderAry = new double[len];
		for (int i=0; i<len; i++) {
			orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
		}
		String[] nameAry = StrUtil.split(tName, ",");
		String[] urlAry = tUrl.split(",");
		
		double temp;
		int size = len;
		String tempStr;
		// 外层循环，控制"冒泡"的最终位置
		for(int i=size-1; i>=1; i--){
			boolean end = true;
			// 内层循环，用于相临元素的比较
			for(int j=0; j < i; j++) {
				if(orderAry[j] > orderAry[j+1]) {
					temp = orderAry[j];
					orderAry[j] = orderAry[j+1];
					orderAry[j+1] = temp;
					end = false;
					
					tempStr = nameAry[j];
					nameAry[j] = nameAry[j+1];
					nameAry[j+1] = tempStr;
					tempStr = urlAry[j];
					urlAry[j] = urlAry[j+1];
					urlAry[j+1] = tempStr;
				}
			}
			if(end == true) {
				break; 
			} 
		}

		tName = "";
		tOrder = "";
		tUrl = "";
		
		for (int i=0; i<len; i++) {
			if (i==0) {
				tName = nameAry[i];
				tOrder = "" + orderAry[i];
				tUrl = urlAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tOrder += "," + orderAry[i];
				tUrl += "," + urlAry[i];
			}
		}

		msd.set("nav_tag_name", tName);
		msd.set("nav_tag_order", tOrder);
		msd.set("nav_tag_url", tUrl);
		
		boolean re = false;;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
			
		return re;
	}		
	
	public boolean delTag(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String tagName = ParamUtil.get(request, "tagName");

		String tName = StrUtil.getNullStr(msd.getString("nav_tag_name"));
		String tUrl = StrUtil.getNullStr(msd.getString("nav_tag_url"));
		String tOrder = StrUtil.getNullStr(msd.getString("nav_tag_order"));
		String[] nameAry = StrUtil.split(tName, ",");
		String[] urlAry = StrUtil.split(tUrl, ",");
		String[] orderAry = StrUtil.split(tOrder, ",");
		
		tName = "";
		tUrl = "";
		tOrder = "";

		int len = nameAry.length;
		for (int i=0; i<len; i++) {
			if (nameAry[i].equals(tagName)) {
				continue;
			}
			if (tName.equals("")) {
				tName = nameAry[i];
				tUrl = urlAry[i];
				tOrder = orderAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tUrl += "," + urlAry[i];
				tOrder += "," + orderAry[i];
			}
		}
		msd.set("nav_tag_name", tName);
		msd.set("nav_tag_url", tUrl);
		msd.set("nav_tag_order", tOrder);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		return re;
	}	
	
	/**
	 * 修改导航标签
	 * @param request
	 * @param code
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean modifyTag(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String tagName = ParamUtil.get(request, "tagName");

		String tagOrder = ParamUtil.get(request, "tagOrder");
		String tagUrl = ParamUtil.get(request, "tagUrl");
		if (tagUrl.equals(""))
			tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组
		String tagType = ParamUtil.get(request, "tagType");
		if (tagType.equals("module")) {
			String tagModuleCode = ParamUtil.get(request, "tagModuleCode");
			JSONObject json = new JSONObject();
			try {
				json.put("moduleCode", tagModuleCode);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tagUrl = json.toString();
		}		

		String tName = StrUtil.getNullStr(msd.getString("nav_tag_name"));
		String tUrl = StrUtil.getNullStr(msd.getString("nav_tag_url"));
		String tOrder = StrUtil.getNullStr(msd.getString("nav_tag_order"));
		String[] nameAry = StrUtil.split(tName, ",");
		String[] urlAry = StrUtil.split(tUrl, ",");
		String[] strOrderAry = StrUtil.split(tOrder, ",");
		
		tName = "";
		tUrl = "";
		tOrder = "";

		int len = nameAry.length;
		for (int i=0; i<len; i++) {
			if (nameAry[i].equals(tagName)) {
				strOrderAry[i] = tagOrder;
				urlAry[i] = tagUrl;
			}
			if (tName.equals("")) {
				tName = nameAry[i];
				tUrl = urlAry[i];
				tOrder = strOrderAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tUrl += "," + urlAry[i];
				tOrder += "," + strOrderAry[i];
			}
		}

		// 根据fieldOrder排序
		double[] orderAry = new double[len];
		for (int i=0; i<len; i++) {
			orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
		}
		
		double temp;
		int size = len;
		String tempStr;
		// 外层循环，控制"冒泡"的最终位置
		for(int i=size-1; i>=1; i--){
			boolean end = true;
			// 内层循环，用于相临元素的比较
			for(int j=0; j < i; j++) {
				if(orderAry[j] > orderAry[j+1]) {
					temp = orderAry[j];
					orderAry[j] = orderAry[j+1];
					orderAry[j+1] = temp;
					end = false;
					
					tempStr = nameAry[j];
					nameAry[j] = nameAry[j+1];
					nameAry[j+1] = tempStr;
					tempStr = urlAry[j];
					urlAry[j] = urlAry[j+1];
					urlAry[j+1] = tempStr;
				}
			}
			if(end == true) {
				break; 
			} 
		}
		
		tName = "";
		tUrl = "";
		tOrder = "";
		
		for (int i=0; i<len; i++) {
			if (i==0) {
				tName = nameAry[i];
				tUrl = urlAry[i];
				tOrder = ""+orderAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tUrl += "," + urlAry[i];
				tOrder += "," + orderAry[i];
			}
		}

		msd.set("nav_tag_name", tName);
		msd.set("nav_tag_url", tUrl);
		msd.set("nav_tag_order", tOrder);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;
	}	
	
	/**
	 * 用于远程调用
	 * @param request
	 * @param code
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean addCond(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		
		String tName = StrUtil.getNullStr(msd.getString("btn_name"));	
		String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
		String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
		String tRole = StrUtil.getNullStr(msd.getString("btn_role"));
		
		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("tName", new String[]{tName});
		reqMap.put("tOrder", new String[]{tOrder});
		reqMap.put("tScript", new String[]{tScript});
		reqMap.put("tBclass", new String[]{tBclass});
		reqMap.put("tRole", new String[]{tRole});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}
		
		License lic = License.getInstance();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});
		reqMap.put("cwsIp", new String[]{request.getServerName()});
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});
		
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/addCond.do";
		
		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");

			msd.set("btn_name", result.getString("name"));
			msd.set("btn_order", result.getString("order"));
			msd.set("btn_script", result.getString("script"));
			msd.set("btn_bclass", result.getString("bclass"));
			msd.set("btn_role", result.getString("role"));			
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}	
	
	public boolean setUse(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		
		int isUse = ParamUtil.getInt(request, "isUse", 0);
		String name = ParamUtil.get(request, "name");
		String urlList = ParamUtil.get(request, "url_list");
		String urlEdit = ParamUtil.get(request, "url_edit");
		String urlShow = ParamUtil.get(request, "url_show");
		int view_show = ParamUtil.getInt(request, "view_show", ModuleSetupDb.VIEW_DEFAULT);
		
		String field_tree_show = ParamUtil.get(request, "field_tree_show");
		if (view_show==ModuleSetupDb.VIEW_SHOW_TREE) {
			if ("".equals(field_tree_show)) {
				throw new ErrMsgException("请选择树形基础数据宏控件字段");
			}
		}
		
		int view_edit = ParamUtil.getInt(request, "view_edit", ModuleSetupDb.VIEW_DEFAULT);
		int is_workLog = ParamUtil.getInt(request, "is_workLog_val", 0);
		
		int view_list = ParamUtil.getInt(request, "view_list", ModuleSetupDb.VIEW_DEFAULT);
		String field_tree_list = ParamUtil.get(request, "field_tree_list");
		if (view_list==ModuleSetupDb.VIEW_LIST_TREE) {
			if ("".equals(field_tree_list)) {
				throw new ErrMsgException("请选择列表视图中对应的树形基础数据宏控件字段");				
			}
		}
		
		String field_begin_date = ParamUtil.get(request, "field_begin_date");
		String field_end_date = ParamUtil.get(request, "field_end_date");
		
		if (view_list==ModuleSetupDb.VIEW_LIST_GANTT) {
			if ("".equals(field_begin_date) || "".equals(field_end_date)) {
				throw new ErrMsgException("请选择开始和结束日期字段");			
			}
		}

		if (view_list==ModuleSetupDb.VIEW_LIST_GANTT && !"".equals(field_begin_date) && field_begin_date.equals(field_end_date)) {
			throw new ErrMsgException("开始日期和结束日期字段不能相同！");			
		}

		if (view_list==ModuleSetupDb.VIEW_LIST_CUSTOM && "".equals(urlList)) {
			throw new ErrMsgException("列表页地址不能为空！");			
		}
		
		if (view_edit==ModuleSetupDb.VIEW_EDIT_CUSTOM && "".equals(urlEdit)) {
			throw new ErrMsgException("编辑页地址不能为空！");			
		}	
		
		if (view_show==ModuleSetupDb.VIEW_SHOW_CUSTOM && "".equals(urlShow)) {
			throw new ErrMsgException("显示页地址不能为空！");			
		}
			
		String field_name = ParamUtil.get(request, "field_name");
		String field_desc = ParamUtil.get(request, "field_desc");
		String field_label = ParamUtil.get(request, "field_label");
		
		String scale_default = ParamUtil.get(request, "scale_default");
		String scale_min = ParamUtil.get(request, "scale_min");
		String scale_max = ParamUtil.get(request, "scale_max");
		
		String validateMsg = ParamUtil.get(request, "validate_msg");
		
		int btn_edit_show = ParamUtil.getInt(request, "btn_edit_show", 1);
		int btn_display_show = ParamUtil.getInt(request, "btn_display_show", 1);
		int btn_add_show = ParamUtil.getInt(request, "btn_add_show", 1);
		String description = ParamUtil.get(request, "description");
		int btn_edit_display = ParamUtil.getInt(request, "btn_edit_display", 0);
		int btn_print_display = ParamUtil.getInt(request, "btn_print_display", 0);
		int btn_flow_show = ParamUtil.getInt(request, "btn_flow_show", 1);
		int btn_log_show = ParamUtil.getInt(request, "btn_log_show", 1);
		int btn_del_show = ParamUtil.getInt(request, "btn_del_show", 1);
		int is_edit_inplace = ParamUtil.getInt(request, "is_edit_inplace", 0); // 在位编辑

		msd.set("is_use", new Integer(isUse));
		msd.set("name", name);
		msd.set("url_list", urlList);
		msd.set("view_show", new Integer(view_show));
		msd.set("view_edit", new Integer(view_edit));
		msd.set("is_workLog", is_workLog);
		msd.set("validate_msg", validateMsg);
		
		msd.set("view_list", view_list);
		msd.set("field_begin_date", field_begin_date);
		msd.set("field_end_date", field_end_date);
		
		msd.set("field_name", field_name);
		msd.set("field_desc", field_desc);
		msd.set("field_label", field_label);

		msd.set("scale_default", scale_default);
		msd.set("scale_min", scale_min);
		msd.set("scale_max", scale_max);
		
		msd.set("url_edit", urlEdit);
		msd.set("url_show", urlShow);
		
		msd.set("btn_edit_show", new Integer(btn_edit_show));
		msd.set("btn_display_show", new Integer(btn_display_show));
		msd.set("btn_add_show", new Integer(btn_add_show));
		
		msd.set("description", description);
		msd.set("btn_edit_display", new Integer(btn_edit_display));
		msd.set("btn_print_display", new Integer(btn_print_display));
		msd.set("btn_flow_show", new Integer(btn_flow_show));
		msd.set("btn_log_show", new Integer(btn_log_show));
		msd.set("btn_del_show", new Integer(btn_del_show));
		
		// 树形显示视图对应的基础数据树形宏控件
		msd.set("field_tree_show", field_tree_show);
		
		// 树形列表视图对应的基础数据树形宏控件	
		msd.set("field_tree_list", field_tree_list);

		msd.set("is_edit_inplace", is_edit_inplace);

		boolean re = true;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		return re;		
	}	
	
	public boolean setFilter(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String filter = ParamUtil.get(request, "filter");
		if (filter.toLowerCase().startsWith("and ")) {
			throw new ErrMsgException("条件不能以and开头！");
		}
		
		String orderby = ParamUtil.get(request, "orderby");
		String sort = ParamUtil.get(request, "sort");

		int cwsStatus = ParamUtil.getInt(request, "cws_status", com.redmoon.oa.flow.FormDAO.STATUS_DONE);
		int isUnitShow = ParamUtil.getInt(request, "isUnitShow", 0);
		String unitCode = ParamUtil.get(request, "unitCode");
		
		msd.set("filter", filter);
		msd.set("orderby", orderby);
		msd.set("sort", sort);
		msd.set("cws_status", cwsStatus);
		msd.set("is_unit_show", isUnitShow);
		msd.set("unit_code", unitCode);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;
	}
	
	public boolean setPromptIcon(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String promptIcon = ParamUtil.get(request, "promptIcon");
		String promptField = ParamUtil.get(request, "promptField");
		String promptCond = ParamUtil.get(request, "promptCond");
		String promptValue = ParamUtil.get(request, "promptValue");
		msd.set("prompt_icon", promptIcon);
		msd.set("prompt_field", promptField);
		msd.set("prompt_cond", promptCond);
		msd.set("prompt_value", promptValue);
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
		return re;
	}	
	
	public boolean setCols(HttpServletRequest request, String code) throws ErrMsgException {
		String cols = ParamUtil.get(request, "cols");
		JSONArray ary = null;
		boolean re = false;
		try {
			ary = new JSONArray(cols);
			// System.out.println(getClass() + " ary=" + ary);
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDb(code);

			String listField = "";
			String listFieldOrder = "";
			String listFieldWidth = "";
			String listFieldLink = "";
			
			for (int j=0; j<ary.length(); j++) {
				JSONObject json = ary.getJSONObject(j);
				
				String fieldName = json.getString("field");
				// 在module_field_inc_preview.jsp中:被替换成了#，以免对jquery的选择器造成问题
				fieldName = fieldName.replaceAll("#", ":");		
				String fieldOrder = String.valueOf(j);
				String fieldWidth = "";
				try {
					fieldWidth = json.getString("width");
				} catch (JSONException e) {
					fieldWidth = String.valueOf(json.getInt("width"));
				}
				String fieldLink = json.getString("link");			
					
				if (listField.equals("")) {
					listField = fieldName;
					listFieldWidth = fieldWidth;
					listFieldOrder = fieldOrder;
					listFieldLink = fieldLink;
				}
				else {
					listField += "," + fieldName;
					listFieldWidth += "," + fieldWidth;
					listFieldOrder += "," + fieldOrder;
					listFieldLink += "," + fieldLink;
				}				
			}			
			
			msd.set("list_field", listField);
			msd.set("list_field_width", listFieldWidth);
			msd.set("list_field_order", listFieldOrder);
			msd.set("list_field_link", listFieldLink);
			
			re = msd.save();			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;
	}	
	
	public boolean addBtn(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		
		String tName = StrUtil.getNullStr(msd.getString("btn_name"));	
		String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
		String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
		String tRole = StrUtil.getNullStr(msd.getString("btn_role"));		

		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("tName", new String[]{tName});
		reqMap.put("tOrder", new String[]{tOrder});
		reqMap.put("tScript", new String[]{tScript});
		reqMap.put("tBclass", new String[]{tBclass});
		reqMap.put("tRole", new String[]{tRole});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}
		
		License lic = License.getInstance();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});
		reqMap.put("cwsIp", new String[]{request.getServerName()});
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});
		
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/addBtn.do";
		
		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");	
			
			msd.set("btn_name", result.getString("name"));
			msd.set("btn_order", result.getString("order"));
			msd.set("btn_script", result.getString("script"));
			msd.set("btn_bclass", result.getString("bclass"));
			msd.set("btn_role", result.getString("role"));	
	
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ErrMsgException("返回数据非法，请检查服务" + cfg.get("cloudUrl") + "是否正常");
		}
		
		return re;
	
	}
	
	public boolean addBtnBatch(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String tName = StrUtil.getNullStr(msd.getString("btn_name"));	
		String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
		String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
		String tRole = StrUtil.getNullStr(msd.getString("btn_role"));
		

		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("tName", new String[]{tName});
		reqMap.put("tOrder", new String[]{tOrder});
		reqMap.put("tScript", new String[]{tScript});
		reqMap.put("tBclass", new String[]{tBclass});
		reqMap.put("tRole", new String[]{tRole});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}
		
		License lic = License.getInstance();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});
		reqMap.put("cwsIp", new String[]{request.getServerName()});
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});
		
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/addBtnBatch.do";
		
		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");	
			
			msd.set("btn_name", result.getString("name"));
			msd.set("btn_order", result.getString("order"));
			msd.set("btn_script", result.getString("script"));
			msd.set("btn_bclass", result.getString("bclass"));
			msd.set("btn_role", result.getString("role"));				
						
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ErrMsgException(e.getMessage(request));			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return re;
	}	

	public boolean delBtn(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String btnName = ParamUtil.get(request, "btnName");

		String tName = StrUtil.getNullStr(msd.getString("btn_name"));
		String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
		String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
		String tRole = StrUtil.getNullStr(msd.getString("btn_role"));
		
		String[] nameAry = StrUtil.split(tName, ",");
		String[] scriptAry = StrUtil.split(tScript, "#");
		String[] orderAry = StrUtil.split(tOrder, ",");
		String[] bclassAry = StrUtil.split(tBclass, ",");
		String[] roleAry = StrUtil.split(tRole, ",");
		
		if (nameAry!=null) {
			if (bclassAry==null || bclassAry.length!=nameAry.length) {
				bclassAry = new String[nameAry.length];
				for (int i=0; i<nameAry.length; i++)
					bclassAry[i] = "";
			}	
			if (roleAry==null || roleAry.length!=nameAry.length) {
				roleAry = new String[nameAry.length];
				for (int i=0; i<nameAry.length; i++)
					roleAry[i] = "";
			}		
		}
		
		tName = "";
		tScript = "";
		tOrder = "";
		tBclass = "";
		tRole = "";

		int len = nameAry.length;
		for (int i=0; i<len; i++) {
			if (nameAry[i].equals(btnName)) {
				continue;
			}
			if (tName.equals("")) {
				tName = nameAry[i];
				tScript = scriptAry[i];
				tOrder = orderAry[i];
				tBclass = bclassAry[i];
				tRole = roleAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tScript += "#" + scriptAry[i];
				tOrder += "," + orderAry[i];
				tBclass += "," + bclassAry[i];
				tRole += "#" + roleAry[i];		
			}
		}
		msd.set("btn_name", tName);
		msd.set("btn_script", tScript);
		msd.set("btn_order", tOrder);
		msd.set("btn_bclass", tBclass);
		msd.set("btn_role", tRole);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;	
	}

	/**
	 * 修改按钮及查询
	 * @param request
	 * @param code
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean modifyBtn(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String btnName = ParamUtil.get(request, "btnName");

		String btnOrder = ParamUtil.get(request, "btnOrder");
		String btnScript = ParamUtil.get(request, "btnScript");
		if (btnScript.equals(""))
			btnScript = "/**/";
		String btnBclass = ParamUtil.get(request, "btnBclass");
		if (btnBclass.equals(""))
			btnBclass = "none";
					
		String[] queryFields = ParamUtil.getParameters(request, "queryFields");
		if (queryFields!=null) {
			JSONObject json = new JSONObject();
			try {
				json.put("btnType", "queryFields");
				// 因为json是无序的，所以需要用fields记录条件字段的顺序
				String fields = "";
				for (int i=0; i<queryFields.length; i++) {
					String token = ParamUtil.get(request, queryFields[i] + "_cond");
					json.put(queryFields[i], token);
					if (fields.equals(""))
						fields = queryFields[i];
					else
						fields += "," + queryFields[i];
				}
				json.put("fields", fields);
				
				int isToolbar = ParamUtil.getInt(request, "isToolbar", 0);
				json.put("isToolbar", isToolbar);				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			btnScript = json.toString();		
		}		
		
		String batchField = ParamUtil.get(request, "batchField");
		String batchValue = ParamUtil.get(request, "batchValue");
		
		boolean isBtnBatch = !batchField.equals("");
		
		// System.out.println(getClass() + " batchValue=" + batchValue);
		
		if ("".equals(batchValue) && queryFields==null && isBtnBatch) {
			throw new ErrMsgException("批处理的值不能为空！");
		}	
		if (!"".equals(batchField) && isBtnBatch) {
			JSONObject json = new JSONObject();
			try {
				json.put("btnType", "batchBtn");
				json.put("batchField", batchField);
				json.put("batchValue", batchValue);				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			btnScript = json.toString();		
		}

		String btnRole = ParamUtil.get(request, "roleCodes");

		String tName = StrUtil.getNullStr(msd.getString("btn_name"));
		String tScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String tOrder = StrUtil.getNullStr(msd.getString("btn_order"));
		String tBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
		String tRole = StrUtil.getNullStr(msd.getString("btn_role"));
		
		String[] nameAry = StrUtil.split(tName, ",");
		String[] scriptAry = StrUtil.split(tScript, "#");
		String[] strOrderAry = StrUtil.split(tOrder, ",");
		String[] bclassAry = StrUtil.split(tBclass, ",");
		String[] roleAry = StrUtil.split(tRole, "#");
		
		// 初始化bclass
		if (nameAry!=null) {
			if (bclassAry==null || bclassAry.length!=nameAry.length) {
				bclassAry = new String[nameAry.length];
				for (int i=0; i<nameAry.length; i++)
					bclassAry[i] = "";
			}
		}

		// 初始化role
		if (nameAry!=null) {
			if (roleAry==null || roleAry.length!=nameAry.length) {
				roleAry = new String[nameAry.length];
				for (int i=0; i<nameAry.length; i++)
					roleAry[i] = "";
			}
		}

		tName = "";
		tScript = "";
		tOrder = "";
		tBclass = "";
		tRole = "";

		int len = nameAry.length;
		for (int i=0; i<len; i++) {
			if (nameAry[i].equals(btnName)) {
				strOrderAry[i] = btnOrder;
				scriptAry[i] = btnScript;
				bclassAry[i] = btnBclass;
				roleAry[i] = btnRole;
			}
			if (tName.equals("")) {
				tName = nameAry[i];
				tScript = scriptAry[i];
				tOrder = strOrderAry[i];
				tBclass = bclassAry[i];
				tRole = roleAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tScript += "#" + scriptAry[i];
				tOrder += "," + strOrderAry[i];
				tBclass += "," + bclassAry[i];
				tRole += "#" + roleAry[i];
			}
		}

		// 根据fieldOrder排序
		double[] orderAry = new double[len];
		for (int i=0; i<len; i++) {
			orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
		}
		
		double temp;
		int size = len;
		String tempStr;
		// 外层循环，控制"冒泡"的最终位置
		for(int i=size-1; i>=1; i--){
			boolean end = true;
			// 内层循环，用于相临元素的比较
			for(int j=0; j < i; j++) {
				if(orderAry[j] > orderAry[j+1]) {
					temp = orderAry[j];
					orderAry[j] = orderAry[j+1];
					orderAry[j+1] = temp;
					end = false;
					
					tempStr = nameAry[j];
					nameAry[j] = nameAry[j+1];
					nameAry[j+1] = tempStr;
					tempStr = scriptAry[j];
					scriptAry[j] = scriptAry[j+1];
					scriptAry[j+1] = tempStr;
					
					tempStr = bclassAry[j];
					bclassAry[j] = bclassAry[j+1];
					bclassAry[j+1] = tempStr;				

					tempStr = roleAry[j];
					roleAry[j] = roleAry[j+1];
					roleAry[j+1] = tempStr;
				}
			}
			if(end == true) {
				break; 
			} 
		}
		
		tName = "";
		tScript = "";
		tOrder = "";
		tRole = "";
		
		for (int i=0; i<len; i++) {
			if (i==0) {
				tName = nameAry[i];
				tScript = scriptAry[i];
				tOrder = ""+orderAry[i];
				tBclass = bclassAry[i];
				tRole = roleAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tScript += "#" + scriptAry[i];
				tOrder += "," + orderAry[i];
				tBclass += "," + bclassAry[i];
				tRole += "#" + roleAry[i];
			}
		}

		msd.set("btn_name", tName);
		msd.set("btn_script", tScript);
		msd.set("btn_order", tOrder);
		msd.set("btn_bclass", tBclass);
		msd.set("btn_role", tRole);
		
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;
	}
	
	/**
	 * 增加操作列链接
	 * @param request
	 * @param code
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean addLink(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		
		String tName = StrUtil.getNullStr(msd.getString("op_link_name"));	
		String tOrder = StrUtil.getNullStr(msd.getString("op_link_order"));
		String tUrl = StrUtil.getNullStr(msd.getString("op_link_url"));
		String tField = StrUtil.getNullStr(msd.getString("op_link_field"));
		String tCond = StrUtil.getNullStr(msd.getString("op_link_cond"));
		String tValue = StrUtil.getNullStr(msd.getString("op_link_value"));
		String tEvent = StrUtil.getNullStr(msd.getString("op_link_event"));
		String tRole = StrUtil.getNullStr(msd.getString("op_link_role"));
		
		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("tName", new String[]{tName});
		reqMap.put("tOrder", new String[]{tOrder});
		reqMap.put("tUrl", new String[]{tUrl});
		reqMap.put("tField", new String[]{tField});
		reqMap.put("tCond", new String[]{tCond});
		reqMap.put("tValue", new String[]{tValue});
		reqMap.put("tEvent", new String[]{tEvent});
		reqMap.put("tRole", new String[]{tRole});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			// System.out.println(getClass() + " key=" + ok + " value=" + value[0]);
			reqMap.put((String)me.getKey(), value);
		}

		reqMap.put("cwsIp", new String[]{request.getServerName()});
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		reqMap.put("cwsVersion", new String[]{cfg.get("version")});

		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/addLink.do";

/*		License lic = License.getInstance();
		reqMap.put("licNum", new String[]{lic.getEnterpriseNum()});
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});

		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}*/

		String retStr = "";
		File file = new File(Global.getRealPath() + "/WEB-INF/license.dat");
		HttpPostFileUtil post = null;
		try {
			post = new HttpPostFileUtil(cloudUrl);
			post.addParameter("license", file);

			Set set = reqMap.entrySet();
			for (Iterator itr = set.iterator(); itr.hasNext();) {
				Map.Entry me = (Map.Entry) itr.next();
				Object ok = me.getKey();
				Object ov = me.getValue();
				if (ov instanceof String[]) {
					String[] ary = (String[])ov;
					for (String val : ary) {
						post.addParameter((String)ok, (String)val);
						// System.out.println(getClass() + " " + ok + "=" + val);
					}
				} else {
					post.addParameter((String)ok, (String)ov);
				}
			}
			retStr = post.send();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (retStr==null || "".equals(retStr)) {
			// DebugUtil.log(getClass(), "FormParser", "网络连接错误！");
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}

		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");				
			msd.set("op_link_name", result.getString("tName"));
			msd.set("op_link_order", result.getString("tOrder"));
			msd.set("op_link_url", result.getString("tUrl"));
			msd.set("op_link_field", result.getString("tField"));
			msd.set("op_link_cond", result.getString("tCond"));
			msd.set("op_link_value", result.getString("tValue"));
			msd.set("op_link_event", result.getString("tEvent"));
			msd.set("op_link_role", result.getString("tRole"));			
			re = msd.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return re;			
	}	

	public boolean modifyLink(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		
		String tName = StrUtil.getNullStr(msd.getString("op_link_name"));
		String tUrl = StrUtil.getNullStr(msd.getString("op_link_url"));
		String tOrder = StrUtil.getNullStr(msd.getString("op_link_order"));
		String tField = StrUtil.getNullStr(msd.getString("op_link_field"));
		String tCond = StrUtil.getNullStr(msd.getString("op_link_cond"));
		String tValue = StrUtil.getNullStr(msd.getString("op_link_value"));
		String tEvent = StrUtil.getNullStr(msd.getString("op_link_event"));
		String tRole = StrUtil.getNullStr(msd.getString("op_link_role"));

		HashMap<String, String[]> reqMap = new HashMap<String, String[]>();
		reqMap.put("tName", new String[]{tName});
		reqMap.put("tOrder", new String[]{tOrder});
		reqMap.put("tUrl", new String[]{tUrl});
		reqMap.put("tField", new String[]{tField});
		reqMap.put("tCond", new String[]{tCond});
		reqMap.put("tValue", new String[]{tValue});
		reqMap.put("tEvent", new String[]{tEvent});
		reqMap.put("tRole", new String[]{tRole});
		
		Map map = request.getParameterMap();
		Set keSet = map.entrySet();
		for (Iterator itr = keSet.iterator(); itr.hasNext();) {
			Map.Entry me = (Map.Entry) itr.next();
			Object ok = me.getKey();
			Object ov = me.getValue();
			String[] value = new String[1];
			if (ov instanceof String[]) {
				value = ParamUtil.getParameters(request, (String)ok);
			} else {
				value[0] = ParamUtil.get(request, (String)ok);
			}
			reqMap.put((String)me.getKey(), value);
		}

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String cloudUrl = cfg.get("cloudUrl");
		cloudUrl += "/public/module/modifyLink.do";

		reqMap.put("cwsVersion", new String[]{cfg.get("version")});
		reqMap.put("cwsIp", new String[]{request.getServerName()});

/*		License lic = License.getInstance();
		reqMap.put("licNum", new String[]{lic.getEnterpriseNum()});
		reqMap.put("licName", new String[]{lic.getName()});
		reqMap.put("licCompany", new String[]{lic.getCompany()});
		reqMap.put("licType", new String[]{lic.getType()});
		reqMap.put("licKind", new String[]{lic.getKind()});
		reqMap.put("licDomain", new String[]{lic.getDomain()});
		reqMap.put("licVersion", new String[]{lic.getVersion()});

		String retStr = NetUtil.post(cloudUrl, reqMap);
		if ("".equals(retStr)) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}*/

		String retStr = "";
		File file = new File(Global.getRealPath() + "/WEB-INF/license.dat");
		HttpPostFileUtil post = null;
		try {
			post = new HttpPostFileUtil(cloudUrl);
			post.addParameter("license", file);

			Set set = reqMap.entrySet();
			for (Iterator itr = set.iterator(); itr.hasNext();) {
				Map.Entry me = (Map.Entry) itr.next();
				Object ok = me.getKey();
				Object ov = me.getValue();
				if (ov instanceof String[]) {
					String[] ary = (String[])ov;
					for (String val : ary) {
						post.addParameter((String)ok, (String)val);
						// System.out.println(getClass() + " " + ok + "=" + val);
					}
				} else {
					post.addParameter((String)ok, (String)ov);
				}
			}
			retStr = post.send();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (retStr==null || "".equals(retStr)) {
			// DebugUtil.log(getClass(), "FormParser", "网络连接错误！");
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_network"));
		}
		
		boolean re = false;
		try {
			JSONObject json = new JSONObject(retStr);
			JSONObject result = json.getJSONObject("result");				
			msd.set("op_link_name", result.getString("tName"));
			msd.set("op_link_order", result.getString("tOrder"));
			msd.set("op_link_url", result.getString("tUrl"));
			msd.set("op_link_field", result.getString("tField"));
			msd.set("op_link_cond", result.getString("tCond"));
			msd.set("op_link_value", result.getString("tValue"));
			msd.set("op_link_event", result.getString("tEvent"));
			msd.set("op_link_role", result.getString("tRole"));					
			
			re = msd.save();
		} catch (ResKeyException e) {
			e.printStackTrace();
			throw new ErrMsgException(e.getMessage(request));
		} catch (JSONException e) {
			System.out.println(getClass() + " retStr=" + retStr);
			e.printStackTrace();
		}
		
		return re;				
	}

	public boolean delLink(HttpServletRequest request, String code) throws ErrMsgException {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		String linkName = ParamUtil.get(request, "linkName");

		String tName = StrUtil.getNullStr(msd.getString("op_link_name"));
		String tUrl = StrUtil.getNullStr(msd.getString("op_link_url"));
		String tOrder = StrUtil.getNullStr(msd.getString("op_link_order"));

		String tField = StrUtil.getNullStr(msd.getString("op_link_field"));
		String tCond = StrUtil.getNullStr(msd.getString("op_link_cond"));
		String tValue = StrUtil.getNullStr(msd.getString("op_link_value"));
		String tEvent = StrUtil.getNullStr(msd.getString("op_link_event"));
		String tRole = StrUtil.getNullStr(msd.getString("op_link_role"));
			
		String[] nameAry = StrUtil.split(tName, ",");
		String[] urlAry = StrUtil.split(tUrl, ",");
		String[] orderAry = StrUtil.split(tOrder, ",");
		
		String[] fieldAry = StrUtil.split(tField, ",");
		String[] condAry = StrUtil.split(tCond, ",");
		String[] valueAry = StrUtil.split(tValue, ",");
		String[] eventAry = StrUtil.split(tEvent, ",");	
		String[] roleAry = StrUtil.split(tRole, "#");	
		
		tName = "";
		tUrl = "";
		tOrder = "";

		int len = nameAry.length;
		for (int i=0; i<len; i++) {
			if (nameAry[i].equals(linkName)) {
				continue;
			}
			if (tName.equals("")) {
				tName = nameAry[i];
				tUrl = urlAry[i];
				tOrder = orderAry[i];
				
				tField = fieldAry[i];
				tCond = condAry[i];
				tValue = valueAry[i];
				tEvent = eventAry[i];	
				tRole = roleAry[i];
			}
			else {
				tName += "," + nameAry[i];
				tUrl += "," + urlAry[i];
				tOrder += "," + orderAry[i];
				
				tField += "," + fieldAry[i];
				tCond += "," + condAry[i];
				tValue += "," + valueAry[i];
				tEvent += "," + eventAry[i];
				tRole += "#" + roleAry[i];
			}
		}
		msd.set("op_link_name", tName);
		msd.set("op_link_url", tUrl);
		msd.set("op_link_order", tOrder);
		
		msd.set("op_link_field", tField);
		msd.set("op_link_cond", tCond);
		msd.set("op_link_value", tValue);
		msd.set("op_link_event", tEvent);
		msd.set("op_link_role", tRole);
		boolean re = false;
		try {
			re = msd.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ErrMsgException(e.getMessage(request));
		}
		
		return re;			
	}
}
