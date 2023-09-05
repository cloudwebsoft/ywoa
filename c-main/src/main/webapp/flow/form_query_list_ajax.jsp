<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.sql.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("")) {
		response.setContentType("application/x-json"); 

		String mode = ParamUtil.get(request, "mode");
		int id = ParamUtil.getInt(request, "id", -1);
				
		if (id==-1) {
			out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
			String str = LocalUtil.LoadString(request,"res.flow.Flow","idNotEmpty");
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, str));
			return;
		}

		// @task:权限检查
		if (!mode.equals("moduleTag") && !mode.equals("selField")) {
			if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
				FormQueryPrivilegeMgr aqpm = new FormQueryPrivilegeMgr();
				if (!aqpm.canUserQuery(request, id)) {
					out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
					String str = LocalUtil.LoadString(request,"res.flow.Flow","notAuthorizedQuery");
					out.print(SkinUtil.makeErrMsg(request, str));
					return;
				}
			}
		}

		FormQueryDb aqd = new FormQueryDb();
		aqd = aqd.getFormQueryDb(id);
		
		String formCode = aqd.getTableCode();
		
		java.util.Date timePoint = aqd.getTimePoint();
		String sTimePoint = "";
		boolean isTimePoint = false; // 是否使用时间点
		if(timePoint != null) {
			isTimePoint = true;
			sTimePoint = DateUtil.format(timePoint, "yyyy-MM-dd");
		}
				
		String sql;
				
		if (mode.equals("moduleTag")) {
			int moduleId = ParamUtil.getInt(request, "moduleId");
			String tagName = ParamUtil.get(request, "tagName");
			String moduleFormCode = ParamUtil.get(request, "moduleFormCode");
			
			FormDb fdModule = new FormDb();
			fdModule = fdModule.getFormDb(moduleFormCode);

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
			
			FormSQLBuilder fsb = new FormSQLBuilder(moduleFdao, jsonTabSetup);
			fsb.setForRelateModule(true);
			
			sql = fsb.getSmartQuery(request, id);
		}
		else if (mode.equals("selField")) { // 选择查询字段
			String openerFormCode = ParamUtil.get(request, "openerFormCode");
			String openerFieldName = ParamUtil.get(request, "openerFieldName");
			
			FormDb openerFd = new FormDb();
			openerFd = openerFd.getFormDb(openerFormCode);
			if (!openerFd.isLoaded()) {
				String str = LocalUtil.LoadString(request,"res.flow.Flow","formNotExist");
				out.print(StrUtil.Alert_Back(str));
				return;
			}
			
			FormField ff = openerFd.getFormField(openerFieldName);
			String strDesc = ff.getDescription();
			JSONObject json = null;
			
			try {
				json = new JSONObject(strDesc);
				int queryId = StrUtil.toInt(json.getString("queryId"));
				// 解码，替换%sq %dq，即单引号、双引号
				// String filter = StrUtil.decodeJSON(json.getString("filter"));
				JSONArray mapsCond = (JSONArray)json.get("mapsCond");
	
				FormQueryDb fqd = new FormQueryDb();
				fqd = fqd.getFormQueryDb(queryId);
				
				mapsCond = (JSONArray)json.get("mapsCond");
				Map mapValue = new HashMap();
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

				FormSQLBuilder fsb = new FormSQLBuilder();
				fsb.setMapConds(mapValue);
				fsb.setForSelField(true);
				sql = fsb.getSmartQuery(request, id);
		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String str = LocalUtil.LoadString(request,"res.flow.Flow","illegalFormat");
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, str));
				return;
			}		
		}		
		else {
			FormSQLBuilder fsb = new FormSQLBuilder();
			sql = fsb.getSmartQuery(request, id);
		}

		// System.out.println(getClass() + " sql=" + sql);
		
		JSONArray rows = new JSONArray();
		JSONObject jobject = new JSONObject();
		jobject.put("rows", rows);

		int pagesize = ParamUtil.getInt(request, "rp", 20);
		// System.out.println(getClass() + " rp=" + pagesize);
		Paginator paginator = new Paginator(request);
		int curpage = ParamUtil.getInt(request, "page", 1);
		ListResult lr = null;
		
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		com.redmoon.oa.visual.FormDAO pfdao = new com.redmoon.oa.visual.FormDAO();
		
		String queryRelated = aqd.getQueryRelated();
		FormDb fdRelated = new FormDb();
		int queryRelatedId = -1;
		if (!queryRelated.equals("")) {
			queryRelatedId = StrUtil.toInt(queryRelated, -1);
			FormQueryDb aqdRelated = aqd.getFormQueryDb(queryRelatedId);
			fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
		}
		
		// System.out.println(getClass() + " sql=" + sql);
		
		try {
			lr = fdao.listResult(formCode, sql, curpage, pagesize);		
		}
		catch (Exception e) {
			e.printStackTrace();
			// out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
			// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查询结果为空，请检查您配置的查询条件！"));
			jobject.put("page", 1);
			jobject.put("total", 0);
			
			out.print(jobject);
			return;
		}
		
		// String sortname=request.getParameter("sortname");
		// String sortorder=request.getParameter("sortorder");
		
		long total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (!v.isEmpty()) {
			ir = v.iterator();
		}
		
		jobject.put("page", curpage);
		jobject.put("total", total);

		WorkflowDb wf = new WorkflowDb();
        MacroCtlUnit mu = null;
        MacroCtlMgr mm = new MacroCtlMgr();
		DeptMgr dm = new DeptMgr();
		UserDb user = new UserDb();
		
		while(ir!=null && ir.hasNext()){
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
		
			JSONObject jo = new JSONObject();
			
			Iterator ffir = fdao.getFields().iterator();
			while (ffir.hasNext()) {
				FormField ff = (FormField)ffir.next();
				
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					mu = mm.getMacroCtlUnit(ff.getMacroType());
					String macroCode = mu.getCode();
					if (macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {
						DeptDb dd = dm.getDeptDb(fdao.getFieldValue(ff.getName()));
						jo.put(ff.getName(), dd.getName());
					}
					else {
						// jo.put(ff.getName(), fdao.getFieldValue(ff.getName()));
						jo.put(ff.getName(), mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName())));
					}
				}
				else		
					jo.put(ff.getName(), fdao.getFieldValue(ff.getName()));
			}
			// 写入id是用于flexigrid置于tr的id中，以便于生成checkbox
			jo.put("id", "" + fdao.getId());
			
			// 取得所关联的查询中的数据
			if (queryRelatedId!=-1) {
				int flowId = StrUtil.toInt(fdao.getCwsId());
				pfdao = pfdao.getFormDAO(flowId, fdRelated);
				ffir = pfdao.getFields().iterator();
				while (ffir.hasNext()) {
					FormField ff = (FormField)ffir.next();

					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						mu = mm.getMacroCtlUnit(ff.getMacroType());
						String macroCode = mu.getCode();
						if (macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {
							DeptDb dd = dm.getDeptDb(pfdao.getFieldValue(ff.getName()));
							jo.put("rel." + ff.getName(), dd.getName());
						}
						else {
							// jo.put(ff.getName(), fdao.getFieldValue(ff.getName()));
							jo.put("rel." + ff.getName(), mu.getIFormMacroCtl().converToHtml(request, ff, pfdao.getFieldValue(ff.getName())));
						}
					}
					else {
						jo.put("rel." + ff.getName(), pfdao.getFieldValue(ff.getName()));
					}
				}
			}
			
			long flowId = fdao.getFlowId();
			if (flowId==-1) {
				flowId = StrUtil.toLong(fdao.getCwsId(), -1);
			}
			String flowCreateDate = "";
			String flowTitle = "";
			String flowStarter = "";
			String flowStatus = "";
			if (flowId!=-1 && flowId!=0) {
				wf = wf.getWorkflowDb((int)flowId);
				flowCreateDate = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm");
				flowTitle = wf.getTitle();
				flowStarter = wf.getUserName();
				if (flowStarter!=null)
					flowStarter = user.getUserDb(flowStarter).getRealName();
				flowStatus = wf.getStatusDesc();
			}
			
			jo.put("flowId", "<a href='javascript:;' onclick=\"addTab('" + flowTitle + "', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + flowId + "')\">" + flowId + "</a>");
			jo.put("flowBeginDate", flowCreateDate);
			jo.put("flowTitle", "<a href='javascript:;' onclick=\"addTab('" + flowTitle + "', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + flowId + "')\">" + flowTitle + "</a>");
			jo.put("flowStarter", flowStarter);
			jo.put("flowStatus", flowStatus);
			
			long mid = fdao.getId();
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			String formName = fd.getName();
			jo.put(QueryScriptUtil.CWS_OP, "<a href=\"javascript:;\" onclick=\"addTab('" + formName + "', '" + request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + mid + "&id=" + mid + "&code=" + formCode + "')\">查看</a>");

			rows.put(jo);
		}
		
		if (total>0) {
			String statDesc = aqd.getStatDesc();
			if (statDesc.equals(""))
				statDesc = "{}";
			JSONObject json = new JSONObject(statDesc);
			JSONObject jo = new JSONObject();
			Iterator ir3 = json.keys();
			int n = 0;
			while (ir3.hasNext()) {
				String key = (String) ir3.next();
				String modeStat = json.getString(key);
				
				double sumVal = FormSQLBuilder.getSUMOfSQL(sql, key);
				if (modeStat.equals("0")) {
					jo.put(key, NumberUtil.round(sumVal, 2));
				}
				else if (modeStat.equals("1")) {
					String str = LocalUtil.LoadString(request,"res.flow.Flow","average");
					jo.put(key, str + NumberUtil.round(sumVal/total, 2));
				}
				n++;
			}
			if (n>0) {
				String str = LocalUtil.LoadString(request,"res.flow.Flow","total");
				jo.put("flowId", str);
				// rows.put(jo);
			}
		}

		out.print(jobject);
}
else if (op.equals("modifyColProps")) {
	FormQueryDb aqd = new FormQueryDb();
	int id = ParamUtil.getInt(request, "id", -1);
	aqd = aqd.getFormQueryDb(id);
	
	String colProps = ParamUtil.get(request, "colProps");
	aqd.setColProps(colProps);
	boolean re = aqd.save();
	
	JSONObject jo = new JSONObject();
	jo.put("re", re);
	out.print(jo);
}
%>