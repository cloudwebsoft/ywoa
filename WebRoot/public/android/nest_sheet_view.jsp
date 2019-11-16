<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import = "com.redmoon.oa.flow.Directory"%>
<%@ page import = "com.redmoon.oa.flow.Leaf"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.util.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%--
- 功能描述：嵌套表格2列表接口
- 访问规则：手机端 访问 
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：20170212
--%>
<%
String skey = ParamUtil.get(request, "skey");
JSONObject jsonRet = new JSONObject();
boolean re = privilege.Auth(skey);
if (re) {
	try {
		jsonRet.put("res", "-2");
		jsonRet.put("msg", "时间过期");
		out.print(jsonRet.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

String pageType = ParamUtil.get(request, "pageType");

String userName = privilege.getUserName(skey);

String formCode = ParamUtil.get(request, "formCode");
String moduleCode = "";
// 传过来的formCode有可能是模块编码
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(formCode);
	if (msd == null || !msd.isLoaded()) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "模块不存在！");
		out.print(jsonRet.toString());		
		return;
	}
	else {
		moduleCode = formCode;
		formCode = msd.getString("form_code");
		fd = fd.getFormDb(formCode);
	}
}
else {
	moduleCode = formCode;
}

String op = ParamUtil.get(request, "op");

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");

int len = 0;
if (fields!=null)
	len = fields.length;

String ondblclickTitle = "";
String ondblclickScript = "";
MacroCtlMgr mm = new MacroCtlMgr();
com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();
FormDAO fdao = new FormDAO();
// 注意此处不同于nest_table_view控件，需取得相关联的父表单字段的值，nest_table_view控件只会关联cwsId即父表单记录的id
String parentFormCode = ParamUtil.get(request, "parentFormCode");
if ("".equals(parentFormCode)) {
	// 从智能模块中传参
	String parentModuleCode = ParamUtil.get(request, "parentModuleCode");
	ModuleSetupDb msdParent = new ModuleSetupDb();
	msdParent = msdParent.getModuleSetupDb(parentModuleCode);
	parentFormCode = msdParent.getString("form_code");
	
	// System.out.println(getClass() + " parentModuleCode=" + parentModuleCode);
	// System.out.println(getClass() + " parentFormCode=" + parentFormCode);
}

int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
	
if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
        
	if (lf==null) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "流程类型不存在！");
		out.print(jsonRet.toString());			
		return;
	}    	
	parentFormCode = lf.getFormCode();
}

// 20131123 fgf 添加nestFieldName，因为其中存储了“选择”按钮需要的配置信息
String nestFieldName = ParamUtil.get(request, "nestFieldName");

jsonRet.put("res", 0);
JSONObject result = new JSONObject();
jsonRet.put("result", result);

JSONObject json = null;
JSONArray mapAry = new JSONArray();
int queryId = -1;
boolean canAdd = false, canEdit = false, canImport = false, canExport=false, canDel = false, canSel = false;
boolean isAutoSel = false;
FormField nestField = null;
if (!nestFieldName.equals("")) {
	FormDb parentFd = new FormDb();
	parentFd = parentFd.getFormDb(parentFormCode);
	nestField = parentFd.getFormField(nestFieldName);
	if (nestField==null) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "父表单（" + parentFormCode + "中的嵌套表字段：" + nestFieldName + "不存在！");
		out.print(jsonRet.toString());		
		return;
	}
	
	if (!"".equals(moduleCode) && !"flow".equals(pageType)) { // pageType.equals("add") || pageType.equals("edit")) {
		ModulePrivDb mpd = new ModulePrivDb(moduleCode);
		canAdd = mpd.canUserAppend(userName);
		canEdit = mpd.canUserModify(userName);
		canDel = mpd.canUserManage(userName);
		
		canSel = canEdit;
						
		result.put("canAdd", canAdd);
		result.put("canEdit", canEdit);
		result.put("canDel", canDel);		
		result.put("canSel", canSel);		
	}
	else {
		try {
			// 20131123 fgf 添加
			String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
			json = new JSONObject(defaultVal);
			// System.out.println(getClass() + " json=" + json);
			try {
				if (json.has("canAdd")) {
					canAdd = "true".equals(json.getString("canAdd"));
				}
				else {
					// 向下兼容
					canAdd = true;
					canEdit = true;
					canImport = true;
					canExport = true;
					canDel = true;
				}
				if (json.has("canEdit")) {
					canEdit = "true".equals(json.getString("canEdit"));
				}
				if (json.has("canImport")) {			
					canImport = "true".equals(json.getString("canImport"));
				}
				if (json.has("canDel")) {			
					canDel = "true".equals(json.getString("canDel"));
				}
				if (json.has("canSel")) {
					canSel = "true".equals(json.getString("canSel"));
				}
				if (json.has("isAutoSel")) {			
					isAutoSel = "1".equals(json.getString("isAutoSel"));
				}
				if (json.has("canExport")) {
					canExport = "true".equals(json.getString("canExport"));
				}
				
				result.put("canAdd", canAdd);
				result.put("canEdit", canEdit);
				result.put("canImport", canImport);
				result.put("canDel", canDel);
				result.put("canSel", canSel);
				result.put("isAutoSel", isAutoSel);
				result.put("canExport", canExport);
			}
			catch (Exception e) {
			}
			if (!json.isNull("maps")) {
				mapAry = (JSONArray)json.get("maps");
			}
			if (!json.isNull("queryId"))
				queryId = StrUtil.toInt((String)json.get("queryId"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
		}
	}	
}

long actionId = ParamUtil.getLong(request, "actionId", -1);

boolean isEditable = ParamUtil.getBoolean(request, "isEditable", true);
if (isEditable) {
	op = "edit";
}
else {
	op = "view";
}

// 流程中或者智能模块编辑时，或者查看时
if (op.equals("edit") || op.equals("view")) {	
	// cwsId为fdao的id
	String cwsId = "";
	if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
        FormDb flowFd = new FormDb();
        flowFd = flowFd.getFormDb(parentFormCode);
        com.redmoon.oa.flow.FormDAO fdaoFlow = new com.redmoon.oa.flow.FormDAO();
        fdaoFlow = fdaoFlow.getFormDAO(flowId, flowFd);
        cwsId = String.valueOf(fdaoFlow.getId());
	}
	else {
		cwsId = ParamUtil.get(request, "cwsId");
	}
	if (parentFormCode.equals("")) {
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "嵌套表参数：父模块编码为空！");
		out.print(jsonRet.toString());			
		return;
	}
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
	// System.out.println(getClass() + " parentFormCode=" + parentFormCode);
	String relateFieldValue = fdm.getRelateFieldValue(StrUtil.toInt(cwsId), moduleCode);
	if (relateFieldValue==null) {		
		jsonRet.put("res", "-1");
		jsonRet.put("msg", "请检查模块" + fd.getName() + "（编码：" + formCode + "）是否相关联");
		out.print(jsonRet.toString());	
		return;
	}
		
	String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
	sql += " order by cws_order";
				
	Vector fdaoV = fdao.list(formCode, sql);
		
	if (isAutoSel) {
		if (fdaoV.size()==0) {
			// 如果嵌套表中没有记录，则说明是正在发起流程
			if (op.equals("edit")) {
				re = NestSheetCtl.autoSel(request, StrUtil.toInt(cwsId), nestField);
				if (re) {
					fdaoV = fdao.list(formCode, sql);
				}
			}
		}
	}
	
	result.put("totalCount", fdaoV.size());
	
	result.put("parentId", cwsId);
	
	Iterator ir = fdaoV.iterator();

	int k = 0;
	UserMgr um = new UserMgr();

	JSONArray datas = new JSONArray();	
	result.put("datas", datas);

	while (ir!=null && ir.hasNext()) {
		fdao = (FormDAO)ir.next();
		k++;
		long id = fdao.getId();
		
		JSONObject row = new JSONObject();
		datas.put(row);
		JSONArray fieldAry = new JSONArray();
		row.put("rId", id);
		row.put("fields", fieldAry);		
	  
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
		
			JSONObject fjo = new JSONObject();
		
			String title = "创建者";
			if (!fieldName.equals("cws_creator")) {
				if (fieldName.startsWith("main")) {
					String[] ary = StrUtil.split(fieldName, ":");
					FormDb mainFormDb = fm.getFormDb(ary[1]);
					title = mainFormDb.getFieldTitle(ary[2]);			
				}
				else if (fieldName.startsWith("other")) {
					String[] ary = StrUtil.split(fieldName, ":");
					if (ary.length>=8) {
						FormDb oFormDb = fm.getFormDb(ary[5]);
						title = oFormDb.getFieldTitle(ary[7]);
					}
					else {
						FormDb otherFormDb = fm.getFormDb(ary[2]);
						title = otherFormDb.getFieldTitle(ary[4]);
					}				
				}
				else {
					title = fd.getFieldTitle(fieldName);
				}
			}		
		
			fjo.put("title", title);
		
			if (!fieldName.equals("cws_creator")) {
				if (fieldName.startsWith("main")) {
					String[] ary = StrUtil.split(fieldName, ":");
					FormDb mainFormDb = fm.getFormDb(ary[1]);
					com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
					com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(StrUtil.toInt(cwsId));
					FormField ff = mainFormDb.getFormField(ary[2]);
					String val = "", text = "";
					if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							val = fdaoMain.getFieldValue(ary[2]);
							RequestUtil.setFormDAO(request, fdaoMain);							
							text = mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2]));
						}
					} else {
						val = fdmMain.getFieldValueOfMain(StrUtil.toInt(cwsId), ary[2]);
						text = val;
					}
					
					fjo.put("name", ff.getName());
					fjo.put("value", val);
					fjo.put("text", text);				
				}
				else if (fieldName.startsWith("other:")) {
					String val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
					fjo.put("name", fieldName);
					fjo.put("value", val);
					fjo.put("text", val);					
				}
				else{
					FormField ff = fd.getFormField(fieldName);
					String val = "", text = "";
					if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						// System.out.println(getClass() + " fieldName22=" + fieldName + " ff.getType()=" + ff.getType() + " mu=" + mu);
						if (mu != null) {
							val = fdao.getFieldValue(fieldName);
							RequestUtil.setFormDAO(request, fdao);														
							text = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
						}
					}else{
						val = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
						text = val;
					}
					
					fjo.put("name", ff.getName());
					fjo.put("value", val);
					fjo.put("text", text);						
				}
	        }else{
				fjo.put("name", "cws_creator");
				fjo.put("value", fdao.getCreator());
				fjo.put("text", um.getUserDb(fdao.getCreator()).getRealName());	        
	        }
	        
			fieldAry.put(fjo);	        
    	}
	}
}

out.print(jsonRet);
%>