<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.sql.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("")) {
	response.setContentType("application/x-json");

	String mode = ParamUtil.get(request, "mode");

	int id = ParamUtil.getInt(request, "id", -1);

	if (!mode.equals("moduleTag") && !mode.equals("selField")) {
		if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
			FormQueryPrivilegeMgr aqpm = new FormQueryPrivilegeMgr();
			if (!aqpm.canUserQuery(request, id)) {
				out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
				return;
			}
		}
	}

	FormQueryDb aqd = new FormQueryDb();

	if (id==-1) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "id不能为空！"));
		return;
	}
	aqd = aqd.getFormQueryDb(id);

	String formCode = aqd.getTableCode();

	// @task:权限检查
	if (!mode.equals("moduleTag") && !mode.equals("selField")) {
		FormQueryPrivilegeMgr aqpm = new FormQueryPrivilegeMgr();
		if (!aqpm.canUserQuery(request, id)) {
			out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
			out.print(SkinUtil.makeErrMsg(request, "您没有被授权该查询！"));
			return;
		}
	}

	java.util.Date timePoint = aqd.getTimePoint();
	String sTimePoint = "";
	boolean isTimePoint = false; // 是否使用时间点
	if(timePoint != null) {
		isTimePoint = true;
		sTimePoint = DateUtil.format(timePoint, "yyyy-MM-dd");
	}

	String sql;
	ResultIterator ri = null;
	QueryScriptUtil	qsu = new QueryScriptUtil();

	JSONArray mapAry = new JSONArray();

	if (mode.equals("moduleTag")) { // 关联选项卡
		int moduleId = ParamUtil.getInt(request, "moduleId");
		String tagName = ParamUtil.get(request, "tagName");
		String moduleFormCode = ParamUtil.get(request, "moduleFormCode");

		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(moduleFormCode);
		String moduleForm = msd.getString("form_code");

		FormDb fdModule = new FormDb();
		fdModule = fdModule.getFormDb(moduleForm);

		com.redmoon.oa.visual.FormDAO moduleFdao = new com.redmoon.oa.visual.FormDAO();
		moduleFdao = moduleFdao.getFormDAO(moduleId, fdModule);

		// 取得选项卡中的条件字段映射关系
		String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleFormCode, tagName);

		if (tagUrl.equals("")) {
			JSONObject jobject = new JSONObject();
			jobject.put("page", 1);
			jobject.put("total", 0);

			out.print(jobject);
			return;
		}

		JSONObject jsonTabSetup = new JSONObject(tagUrl);

		// FormSQLBuilder fsb = new FormSQLBuilder(moduleFdao, jsonTabSetup);
		// fsb.setForRelateModule(true);

		// sql = fsb.getSmartQuery(request, id);
		ri = qsu.executeQuery(request, aqd, moduleFdao, jsonTabSetup);
	}
	else if (mode.equals("sel")) { // 选择拉单
		String nestFormCode = ParamUtil.get(request, "nestFormCode");
		String parentFormCode = ParamUtil.get(request, "parentFormCode");
		String nestFieldName = ParamUtil.get(request, "nestFieldName");

		FormDb pForm = new FormDb();
		pForm = pForm.getFormDb(parentFormCode);
		FormField nestField = pForm.getFormField(nestFieldName);

		JSONObject json = null;
		// JSONArray mapsNest = new JSONArray();
		JSONArray mapsCond = new JSONArray();
		try {
			// 得到父窗口条件字段并置于map中
			String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
			json = new JSONObject(defaultVal);
			// nestFormCode = json.getString("destForm");
			mapsCond = (JSONArray)json.get("mapsCond");
			HashMap mapValue = new HashMap();
			for (int i=0; i<mapsCond.length(); i++) {
				JSONObject j = null;
				try {
					j = mapsCond.getJSONObject(i);
					// sourceField为条件字段
					String condField = (String)j.get("sourceField");
					String parentWinField = (String) j.get("destField");
					mapValue.put(condField, ParamUtil.get(request, parentWinField));
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			// mapsNest = (JSONArray)json.get("mapsNest");

			ri = qsu.executeQuery(request, aqd, pForm, mapsCond, mapValue);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
			return;
		}

	}
	else if (mode.equals("selField")) { // 选择查询字段
		String openerFormCode = ParamUtil.get(request, "openerFormCode");
		String openerFieldName = ParamUtil.get(request, "openerFieldName");

		FormDb openerFd = new FormDb();
		openerFd = openerFd.getFormDb(openerFormCode);
		if (!openerFd.isLoaded()) {
			out.print(StrUtil.Alert_Back("表单不存在！"));
			return;
		}

		FormField ff = openerFd.getFormField(openerFieldName);
		String strDesc = ff.getDescription();
		JSONObject json = null;

		try {
			json = new JSONObject(strDesc);
			formCode = json.getString("formCode");
			int queryId = -1;
			try {
				queryId = StrUtil.toInt(json.getString("queryId"));
			}
			catch (JSONException e) {
				queryId = json.getInt("queryId");
			}
			// 解码，替换%sq %dq，即单引号、双引号
			// String filter = StrUtil.decodeJSON(json.getString("filter"));
			JSONArray mapsCond = (JSONArray)json.get("mapsCond");

			FormQueryDb fqd = new FormQueryDb();
			fqd = fqd.getFormQueryDb(queryId);

			mapsCond = (JSONArray)json.get("mapsCond");
			HashMap mapValue = new HashMap();
			for (int i=0; i<mapsCond.length(); i++) {
				JSONObject j = null;
				try {
					j = mapsCond.getJSONObject(i);
					// sourceField为条件字段
					String condField = (String)j.get("sourceField");
					String parentWinField = (String) j.get("destField");
					mapValue.put(condField, ParamUtil.get(request, parentWinField));
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			// mapsNest = (JSONArray)json.get("mapsNest");

			mapAry = (JSONArray)json.get("maps");

			ri = qsu.executeQuery(request, fqd, openerFd, mapsCond, mapValue);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "json格式非法！"));
			return;
		}
	}
	else if (mode.equals("changeCondValue")) { // 在查询结果中更改条件继续搜索
		ri = qsu.executeQueryOnChangCondValue(request, aqd);
	}
	else { // 查询
		ri = qsu.executeQuery(request, aqd);
	}

	/*
	String tableName = "";
	sql = qsu.getSql();
	int p = sql.indexOf("ft_");
	if (p!=-1) {
		int q = sql.indexOf(" ", p);
		if (q==-1) {
			tableName = sql.substring(p).toLowerCase();;
		}
		else {
			tableName = sql.substring(p, q).toLowerCase();
		}
	}

	FormDb fd = null;
	String scriptFormCode = "";
	if (tableName.startsWith("ft_")) {
		scriptFormCode = tableName.substring("ft_".length());
	}
	*/

	if (ri==null) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "脚本运行错误！"));
		return;
	}

	JSONArray rows = new JSONArray();
	JSONObject jobject = new JSONObject();
	jobject.put("rows", rows);

	// String sortname=request.getParameter("sortname");
	// String sortorder=request.getParameter("sortorder");

	jobject.put("page", qsu.getPage());
	jobject.put("total", qsu.getTotal());

	String queryFormCode = qsu.getFormCode();

	String formName = "";
	FormDb fd = new FormDb();
	boolean isFormTable = queryFormCode!=null;
	if (isFormTable) {
		fd = fd.getFormDb(queryFormCode);
		formName = fd.getName();
		// 避免大小写问题，仅管可能无此问题
		queryFormCode = fd.getCode();
	}

	MacroCtlMgr mm = new MacroCtlMgr();

	int row = 0;
	while(ri.hasNext()){
		ResultRecord rr = (ResultRecord)ri.next();

		JSONObject jo = new JSONObject();
		HashMap mapIndex = qsu.getMapIndex();
		Iterator irMap = mapIndex.keySet().iterator();
		int k = 0;
		while (irMap.hasNext()) {
			String keyName = (String) irMap.next();

			String val = StrUtil.getNullStr(rr.getString(keyName));

			if (isFormTable) {
				FormField ff = fd.getFormField(keyName);
				if (ff!=null) {
					if(ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu!=null) {
							val = mu.getIFormMacroCtl().converToHtml(request, ff, val);
						}
					}
					else {
						if (ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
							val = DateUtil.format(rr.getDate(keyName), "yyyy-MM-dd");
						}
						else if (ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
							val = DateUtil.format(rr.getDate(keyName), "yyyy-MM-dd HH:mm:ss");
						}
					}
				}
			}

			jo.put(keyName, val);

			if (isFormTable) {
				String mid = "";
				try {
					// 可能id在sql中没有写
					mid = rr.getString("id");
				}
				catch (IllegalArgumentException e) {
				}
				if (!"".equals(mid)) {
					jo.put(QueryScriptUtil.CWS_OP, "<a href=\"javascript:;\" onclick=\"addTab('" + formName + "', '" + request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + mid + "&id=" + mid + "&code=" + queryFormCode + "')\">查看</a>");
				}
				else {
					jo.put(QueryScriptUtil.CWS_OP, "");
				}
			}
			else {
				jo.put(QueryScriptUtil.CWS_OP, "");
			}

			k++;
		}

		// 因为flexigrid需要用到id作为checkbox的值，其值为"row"+id
		if (!jo.has("id")) {
			jo.put("id", String.valueOf(row));
		}
		rows.put(jo);
		row++;
	}

	if (qsu.getTotal()>0 && !mode.equals("sel")) {
		String statDesc = aqd.getStatDesc();
		if (statDesc.equals("")) {
			statDesc = "{}";
		}
		JSONObject json = new JSONObject(statDesc);
		JSONObject jo = new JSONObject();
		Iterator ir3 = json.keys();
		int n = 0;
		Map mapFieldType = ri.getMapType();
		while (ir3.hasNext()) {
			String key = (String) ir3.next();
			String modeStat = json.getString(key);

			Integer iType = (Integer)mapFieldType.get(key.toUpperCase());
			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());

			double sumVal = FormSQLBuilder.getSUMOfSQL(qsu.getSql(), key);
			if (modeStat.equals("0")) {
				if (fieldType==FormField.FIELD_TYPE_INT
				  || fieldType==FormField.FIELD_TYPE_LONG) {
					jo.put(key, (long)sumVal);
				}
				else {
					jo.put(key, NumberUtil.round(sumVal, 2));
				}
			}
			else if (modeStat.equals("1")) {
				jo.put(key, "平均：" + NumberUtil.round(sumVal/qsu.getTotal(), 2));
			}
			n++;
		}
		if (n>0) {
			jo.put("flowId", "合计");
			rows.put(jo);
		}
	}
	out.print(jobject);
}
%>