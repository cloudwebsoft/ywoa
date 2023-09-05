<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %><%@ page import = "com.cloudwebsoft.framework.base.*"%><%@ page import = "com.redmoon.oa.BasicDataMgr"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "cn.js.fan.web.*"%><%@ page import = "java.util.*"%><%@ page import = "com.redmoon.oa.dept.*"%><%@ page import = "com.redmoon.oa.flow.*"%><%@ page import = "com.redmoon.oa.flow.query.*"%><%@ page import = "com.redmoon.oa.visual.*"%><%@ page import = "com.redmoon.oa.dept.DeptDb"%><%@ page import = "com.redmoon.oa.dept.DeptMgr"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><%@ page import="java.io.*"%><%@ page import = "org.json.*"%><%@ page import = "com.cloudwebsoft.framework.db.*"%><%@ page import = "cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %><%
	FormQueryDb aqd = new FormQueryDb();
	int id = ParamUtil.getInt(request, "id", -1);
	aqd = aqd.getFormQueryDb(id);
	
	String colProps = aqd.getColProps();

	if ("".equals(colProps)) {
		out.print("结果不存在，请运行查询并检查查询结果！");
		return;
	}
	
	JSONArray jsons = new JSONArray(colProps);
	
	java.util.Date timePoint = aqd.getTimePoint();
	if(timePoint == null) {
		timePoint = new java.util.Date();
	}

	response.setContentType("application/vnd.ms-excel");
	response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(aqd.getQueryName() + ".xls"));  
	OutputStream os = response.getOutputStream();
	try {
		String fileName = "form_query.xls";
		File file = new File(Global.realPath + "flow/doc_templ/" + fileName);
		Workbook wb = Workbook.getWorkbook(file);
		WritableWorkbook workbook = Workbook.createWorkbook(os, wb);
		WritableSheet sheet = workbook.getSheet(0);

		int k = 0;
		for(int i=0; i<jsons.length();i++){
			JSONObject json = jsons.getJSONObject(i);

			if(json.get("hide").toString().equals("true")) {
				continue;
			}
			
			if (json.get("name").equals(QueryScriptUtil.CWS_OP)) {
				continue;
			}			

			Label label = new Label(k, 0, json.getString("display"));
			sheet.addCell(label);
			k++;
		}

		k = 1;
		
		QueryScriptUtil	qsu = new QueryScriptUtil();		
		ResultIterator ri = null;
		
		String mode = ParamUtil.get(request, "mode");
		if (mode.equals("moduleTag")) {
			int moduleId = ParamUtil.getInt(request, "moduleId");
			String tagName = ParamUtil.get(request, "tagName");
			String moduleFormCode = ParamUtil.get(request, "moduleFormCode");
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDb(moduleFormCode);
			String formCode = msd.getString("form_code");
			FormDb fdModule = new FormDb();
			fdModule = fdModule.getFormDb(formCode);

			com.redmoon.oa.visual.FormDAO moduleFdao = new com.redmoon.oa.visual.FormDAO();
			moduleFdao = moduleFdao.getFormDAO(moduleId, fdModule);
			
			// 取得选项卡中的条件字段映射关系
			String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleFormCode, tagName);
			
			if ("".equals(tagUrl)) {
				LogUtil.getLog(getClass()).info("tagUrl is empty.");
				return;
			}
			
			JSONObject jsonTabSetup = new JSONObject(tagUrl);
			
			// FormSQLBuilder fsb = new FormSQLBuilder(moduleFdao, jsonTabSetup);
			// fsb.setForRelateModule(true);
			
			// sql = fsb.getSmartQuery(request, id);
			ri = qsu.executeQuery(request, aqd, moduleFdao, jsonTabSetup);
		}
		else if (mode.equals("sel")) {
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
		else {
			ri = qsu.executeQuery(request, aqd);
		}
		while(ri.hasNext()){
			ResultRecord rr = (ResultRecord)ri.next();
			
			int col = 0;
						
			for(int i=0; i<jsons.length(); i++){
				JSONObject json = jsons.getJSONObject(i);
				if(json.get("hide").toString().equals("true"))
					continue;
					
				if (json.get("name").equals(QueryScriptUtil.CWS_OP)) {
					continue;
				}
				
				String val = rr.getString(json.get("name").toString());
				if (val!=null && val.indexOf("<")!=-1) {
					val = StrUtil.getAbstract(request, val, 1000, "");
				}
				
				Label label = new Label(col, k, val);
				sheet.addCell(label);
				
				col++;

			}
			k++;
		}
		
		workbook.write();
      	workbook.close();
		wb.close();
    } catch (Exception e) {
		e.printStackTrace();
    } finally {
		os.close();
	}
%>