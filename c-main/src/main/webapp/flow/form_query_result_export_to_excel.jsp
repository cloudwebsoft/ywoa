<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %><%@ page import = "com.cloudwebsoft.framework.base.*"%><%@ page import = "com.redmoon.oa.BasicDataMgr"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "cn.js.fan.web.*"%><%@ page import = "java.util.*"%><%@ page import = "com.redmoon.oa.dept.*"%><%@ page import = "com.redmoon.oa.flow.*"%><%@ page import = "com.redmoon.oa.flow.macroctl.*"%><%@ page import = "com.redmoon.oa.dept.DeptDb"%><%@ page import = "com.redmoon.oa.dept.DeptMgr"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><%@ page import="java.io.*"%><%@ page import = "org.json.*"%><%@ page import = "com.cloudwebsoft.framework.db.*"%><%
	FormQueryDb aqd = new FormQueryDb();
	int id = ParamUtil.getInt(request, "id", -1);
	aqd = aqd.getFormQueryDb(id);
	
	String formCode = aqd.getTableCode();
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	
	FormDb fdRelated = null;
	String queryRelated = aqd.getQueryRelated();
	int queryRelatedId = StrUtil.toInt(queryRelated, -1);
	if (queryRelatedId!=-1) {
		FormQueryDb aqdRelated = aqd.getFormQueryDb(queryRelatedId);
		fdRelated = fd.getFormDb(aqdRelated.getTableCode());
	}
	
	String colProps = aqd.getColProps();
	if (colProps.equals("") || colProps.equals("[]")) {
		Iterator ir = fd.getFields().iterator();
		colProps = "";
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (!ff.isCanList())
				continue;
					
			if (colProps.equals(""))
				colProps = "{display: '" + ff.getTitle() + "', name : '" + ff.getName() + "', width : " + ff.getWidth() + ", sortable : false, align: 'center', hide: false}";
			else
				colProps += ",{display: '" + ff.getTitle() + "', name : '" + ff.getName() + "', width : " + ff.getWidth() + ", sortable : false, align: 'center', hide: false}";
		}
		
		if (!queryRelated.equals("")) {
			// 取得关联查询中默认的colProps			
			ir = fdRelated.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
				if (!ff.isCanList())
					continue;
				
				if (colProps.equals(""))
					colProps = "{display: '" + ff.getTitle() + "', name : 'rel." + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
				else
					colProps += ",{display: '" + ff.getTitle() + "', name : 'rel." + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
			}
		}		
		
		colProps = "[" + colProps + "]";
	}

	JSONArray jsons = new JSONArray(colProps);
	
	java.util.Date timePoint = aqd.getTimePoint();
	if(timePoint == null) {
		timePoint = new java.util.Date();
	}

   	FormSQLBuilder fsb = new FormSQLBuilder();
	String sql = fsb.getSmartQuery(request, id);

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
		
		Label label = new Label(k, 0, "ID");
		sheet.addCell(label);
		k++;
		label = new Label(k, 0, "标题");
		sheet.addCell(label);
		k++;
		label = new Label(k, 0, "发起人");
		sheet.addCell(label);
		k++;
		label = new Label(k, 0, "开始日期");
		sheet.addCell(label);
		k++;				
		label = new Label(k, 0, "状态");
		sheet.addCell(label);
		k++;				
		
		for(int i=0; i<jsons.length();i++){
			JSONObject json = jsons.getJSONObject(i);

			if(json.get("hide").toString().equals("true")) {
				continue;
			}

			String fcode = json.getString("name");
			if (!fcode.startsWith("rel.")) {
				FormField ff = fd.getFormField(fcode);
				if (ff!=null) {
					label = new Label(k, 0, json.getString("display"));
					sheet.addCell(label);
					k++;
				}
			}
			else {
				if (queryRelatedId!=-1) {
					FormField ff = fdRelated.getFormField(fcode.substring("rel.".length()));
					if (ff!=null) {
						label = new Label(k, 0, json.getString("display"));
						sheet.addCell(label);
						k++;
					}
				}
			}
		}

		String fieldType = "";
		String value = "";
		k = 1;
		
		// com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
		com.redmoon.oa.flow.FormDAO flowDao = new com.redmoon.oa.flow.FormDAO();
		WorkflowDb wf = new WorkflowDb();

        MacroCtlUnit mu = null;
        MacroCtlMgr mm = new MacroCtlMgr();
		DeptMgr dm = new DeptMgr();

		Vector vt = fdao.list(formCode, sql);
		Iterator ir = vt.iterator();
		
		while(ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
			
			long flowId = fdao.getFlowId();
			if (flowId==-1) {
				flowId = StrUtil.toInt(fdao.getCwsId(), -1);
			}
			
			int col = 0;
			
			String flowCreateDate = "";
			String flowTitle = "";
			String flowStarter = "";
			String flowStatus = "";			
			if (flowId!=-1) {
				wf = wf.getWorkflowDb((int)flowId);
				flowCreateDate = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm");
				flowTitle = wf.getTitle();
				flowStarter = wf.getUserName();
				flowStatus = wf.getStatusDesc();				
			}
			label = new Label(col, k, "" + flowId);
			sheet.addCell(label);
			col ++;
			label = new Label(col, k, flowTitle);
			sheet.addCell(label);
			col ++;
			label = new Label(col, k, flowStarter);
			sheet.addCell(label);
			col ++;
			label = new Label(col, k, flowCreateDate);
			sheet.addCell(label);			
			col ++;
			label = new Label(col, k, flowStatus);
			sheet.addCell(label);			
			col ++;
			
			value = "";
			
			for(int i =0; i<jsons.length(); i++){
				JSONObject json = jsons.getJSONObject(i);
				if(json.get("hide").toString().equals("true"))
					continue;

				String fcode = json.getString("name");
				
				if (!fcode.startsWith("rel.")) {
					FormField ff = fd.getFormField(fcode);
					if (ff!=null) {
						
						if (ff.getType().equals(FormField.TYPE_MACRO)) {
							mu = mm.getMacroCtlUnit(ff.getMacroType());
							String macroCode = mu.getCode();
							if (macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {
								DeptDb dd = dm.getDeptDb(fdao.getFieldValue(ff.getName()));
								value = dd.getName();
							}
							else
								value = fdao.getFieldValue(fcode);
						}
						else		
							value = fdao.getFieldValue(fcode);

						label = new Label(col, k, value);
						sheet.addCell(label);
						
						col++;
					}
				}
				else {
					if (queryRelatedId!=-1) {
						FormField ff = fdRelated.getFormField(fcode.substring("rel.".length()));
						if (ff!=null) {
							flowDao = flowDao.getFormDAO((int)flowId, fdRelated);

							label = new Label(col, k, flowDao.getFieldValue(ff.getName()));
							sheet.addCell(label);
							
							col++;
						}
					}
				}
			}
			k++;
		}
		
		if (vt.size()>0) {
			String statDesc = aqd.getStatDesc();
			if (statDesc.equals("")) {
				statDesc = "{}";
			}
			JSONObject json = new JSONObject(statDesc);
			Iterator ir3 = json.keys();
			int n = 0;
			while (ir3.hasNext()) {
				String key = (String) ir3.next();
				String mode = json.getString(key);
				
				String cellVal = "";
				double sumVal = FormSQLBuilder.getSUMOfSQL(sql, key);
				if (mode.equals("0")) {
					cellVal = NumberUtil.round(sumVal, 2);
				}
				else if (mode.equals("1")) {
					cellVal = "平均：" + NumberUtil.round(sumVal/vt.size(), 2);
				}
				for(int i=0; i<jsons.length(); i++) {
					JSONObject jsonObj = jsons.getJSONObject(i);
					if(jsonObj.get("hide").toString().equals("true")) {
						continue;
					}
					String fcode = jsonObj.getString("name");
					if (fcode.equals(key)) {
						label = new Label(i, k, cellVal);
						sheet.addCell(label);
						break;
					}
				}				
				n++;
			}
			if (n>0) {
				label = new Label(0, k, "合计");
				sheet.addCell(label);
			}
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