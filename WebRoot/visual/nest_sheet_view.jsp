<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.query.QueryScriptUtil"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowActionDb"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowPredefineDb"%>
<%@ page import="com.redmoon.oa.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="java.util.regex.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%--
- 功能描述：嵌套表格2
- 访问规则：服务器端包含于flow_dispose.jsp中
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2013.11.24
- 修改原因：使支持从模块中选择记录
- 修改点：原控件的默认值为嵌套表单的编码，现改为json格式的描述
--%>

<%
// 因为内外网访问的问题而注释
/*
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

String formCode = ParamUtil.get(request, "formCode");
String moduleCode = "";
// 传过来的formCode有可能是模块编码
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(formCode);
	if (msd == null || !msd.isLoaded()) {
		out.print("表单不存在！");
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

// System.out.println("form_code=" + formCode);
// 因为内外网访问的问题而注释
/*
ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

/*
用于显示嵌套表格于父表单中，由NestTableCtl.converToHTML通过url连接调用，注意需在用到此文件的页面中，置request属性cwsId、pageType、action
*/
String op = ParamUtil.get(request, "op");
%>
<style>
#cwsNestTable{
	font-size: 9pt;
	word-break:break-all;
	cursor: default;
	BORDER: 1px solid #cccccc;
	border-collapse:collapse;
	border-Color:#cccccc;
	align:center;
}
.cwsThead {
/*background-color:#eeeeee;*/
height:20px;
}
#cwsNestTable td {
	border:1px solid #cccccc;
	height:20px;
}
.imgBtn {
	width:22px;
	height:22px;
	filter:alpha(opacity=50);  
	-moz-opacity:0.5;  
	-khtml-opacity: 0.5;  
	opacity: 0.5;  
	cursor:pointer;
}
.imgBtnHover {
	-moz-opacity:1;  
	-khtml-opacity:1;  
	opacity:1;  
}
</style>
<script>
// 删除后重新排序
function refreshSerialNo_<%=formCode%>() {
	var trList = $("#cwsNestTable_<%=formCode%>").find("tr");
    for (var i=1;i<trList.length;i++) {
        var tdArr = trList.eq(i).children("td");
        tdArr.eq(0).html(i);
    }
}
</script>
<div id="dlg<%=formCode%>" style="display:none; padding:0px">
<iframe id="iframeModuleRelate<%=formCode%>" style="width:98%; height:98%; margin:0px; border:0px"></iframe>
</div>
<%
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
// String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

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

// 20131123 fgf 添加nestFieldName，因为其中存储了“选择”按钮需要的配置信息
String nestFieldName = ParamUtil.get(request, "nestFieldName");

// 从模块所关联的父模块的ID
long mainId = ParamUtil.getLong(request, "mainId", -1);

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "parentFormCode", parentFormCode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "nestFieldName", nestFieldName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

JSONObject json = null;
JSONArray mapAry = new JSONArray();
int queryId = -1;
boolean canAdd = true, canEdit = true, canImport = true, canExport=true, canDel = true, canSel = true;
boolean isAutoSel = false;
FormField nestField = null;
if (!nestFieldName.equals("")) {
	FormDb parentFd = new FormDb();
	parentFd = parentFd.getFormDb(parentFormCode);
	nestField = parentFd.getFormField(nestFieldName);
	if (nestField==null) {
		%>
		父表单（<%=parentFormCode%>）中的嵌套表字段：<%=nestFieldName%> 不存在
		<%
		return;
	}
	try {
		// 20131123 fgf 添加
		String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
		json = new JSONObject(defaultVal);
		// System.out.println(getClass() + " json=" + json);
		try {
			canAdd = "true".equals(json.getString("canAdd"));
			canEdit = "true".equals(json.getString("canEdit"));
			canImport = "true".equals(json.getString("canImport"));
			canDel = "true".equals(json.getString("canDel"));
			canSel = "true".equals(json.getString("canSel"));
			if (json.has("canExport")) {
				canExport = "true".equals(json.getString("canExport"));
			}
			if (json.has("isAutoSel")) {
				isAutoSel = "1".equals(json.getString("isAutoSel"));
			}
		}
		catch (Exception e) {
			// e.printStackTrace();
			com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info(e.getMessage());
		}
		if (!json.isNull("maps")) {
			mapAry = (JSONArray)json.get("maps");
		}
		if (!json.isNull("queryId")) {
			queryId = StrUtil.toInt((String)json.get("queryId"));
		}
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
	}	
}

long actionId = ParamUtil.getLong(request, "actionId", -1);
int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;

// 如果是自动拉单，则会有newIds传过来
String newIds = ParamUtil.get(request, "newIds");

String cwsId = "";
// 流程中或者智能模块编辑时，或者查看时
if (op.equals("edit") || op.equals("view")) {
	// cwsId为fdao的id
	cwsId = ParamUtil.get(request, "cwsId");
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "cwsId", cwsId, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;	
	}
		
	if (parentFormCode.equals("")) {
		out.print("嵌套表参数：父模块编码为空！");
		return;
	}
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
	// System.out.println(getClass() + " parentFormCode=" + parentFormCode);
	String relateFieldValue = fdm.getRelateFieldValue(StrUtil.toInt(cwsId), moduleCode);
	if (relateFieldValue==null) {
		out.print(SkinUtil.makeErrMsg(request, "请检查模块" + fd.getName() + "（编码：" + formCode + "）是否相关联"));
		return;
	}

	String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
	boolean isUseFilter = true;
	flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
	
	if (!"".equals(newIds)) {
		String[] idsAry = StrUtil.split(newIds, ",");
		StringBuffer sb = new StringBuffer();
		for (String id : idsAry) {
			StrUtil.concat(sb, ",", StrUtil.sqlstr(id));
		}
		sql = "select id from " + fd.getTableNameByForm() + " where id in (" + sb.toString() + ")";
	}
	else {
		// 流程中指定节点上才可以使用filter
		if (flowId!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
			// 以防止出现嵌套表被多个主表调用时，因为cws_id的重复，出现拉取了重复数据的情况
			sql += " and flowId=" + flowId;
			
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);
			
			WorkflowActionDb wa = new WorkflowActionDb();
			wa = wa.getWorkflowActionDb((int)actionId);
			
			WorkflowPredefineDb wpd = new WorkflowPredefineDb();
			wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());	
			String isModuleFilter = WorkflowActionDb.getActionProperty(wpd, wa.getInternalName(), "isModuleFilter");
			if (!"1".equals(isModuleFilter)) {
				isUseFilter = false;
			}
		}
		else {
			com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
			String strVersionOld = StrUtil.getNullStr(oaCfg.get("versionOld"));
			double versionOld = StrUtil.toDouble(strVersionOld, 4);
			// 如果是3.0之前的系统升级上来的，则不能启用
			if (versionOld>=4.0) {
				sql += " and cws_parent_form=" + StrUtil.sqlstr(parentFormCode);
			}
		}
		
		// 当运用于智能模块中时，始终启用嵌套表对应主模块中的过滤条件
		if (isUseFilter) {
			String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), StrUtil.getNullStr(msd.getString("filter")));
			if (ary!=null) {
				String filter = ary[0];
				if (filter!=null && !"".equals(filter)) {
					sql += " and " + filter;
				}
			}		
		}
	}
		
	sql += " order by id";
		
	// DebugUtil.i(getClass(), "nest_sheet_view.jsp", " formCode=" + formCode + " sql=" + sql);
	
	Vector fdaoV = fdao.list(formCode, sql);
	Iterator ir = fdaoV.iterator();
	%>
	<script>
	function add_row_<%=formCode%>() {
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_add_relate.jsp?isShowNav=0&parentId=<%=cwsId%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>&flowId=<%=flowId%>&actionId=<%=actionId%>";
		openWin(url,"1000","800");
	}

	function edit_row_<%=formCode%>(id) {
	    // console.log("id=" + id);
		<%
		if (!op.equals("edit")) {
			%>
			return;
			<%
		}
		%>
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_edit_relate.jsp?parentId=<%=cwsId%>&id=" + id + "&menuItem=&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&formCode=<%=parentFormCode%>&isShowNav=0&actionId=<%=actionId%>";
		openWin(url,"1000","800");		
	}
	
	function del_row_<%=formCode%>(id) {
		<%
		if (!op.equals("edit")) {
			%>
			return;
			<%
		}
		%>
		
		jConfirm("您确定要删除吗?","提示",function(r){
			if(!r){
				return;
			}
			else{
				try {
					onNestDelRow('<%=formCode%>', id);
				}
				catch(e) {}				
				$.ajax({
					type: "get",
					url: "<%=request.getContextPath()%>/visual/nest_sheet_del_relate.jsp?action=del&id=" + id + "&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&parentId=<%=cwsId%>",
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$(document.body).showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
				            $("#<%=formCode%>_" + id).remove();		
							refreshSerialNo_<%=formCode%>();
                            refreshNestSheetCtl<%=moduleCode%>();
                            
							try {
								onNestAfterDelRow('<%=formCode%>', id, <%=cwsId%>);
							}
							catch(e) {}		                            
							// jAlert(data.msg, "提示");
							// 用于form_js_***.jsp调用						
						}
					},
					complete: function(XMLHttpRequest, status){
						$(document.body).hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						$(document.body).hideLoading();
						alert(XMLHttpRequest.responseText);
					}
				});	  				
            }
		});
	}
	
	function importForSheet_<%=formCode%>(parentId) {
		openWin("<%=request.getContextPath()%>/visual/nest_sheet_import_excel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>&moduleCode=<%=moduleCode%>&flowId=<%=flowId%>&parentId=" + parentId, 480, 80);
	}
	
	function sel_<%=formCode%>(parentId, isQuery) {
		if (isQuery) {
			var fieldParams = "";
			<%
			if (queryId!=-1) {
				// 取得查询脚本中需从request中获取的字段值
				QueryScriptUtil qsu = new QueryScriptUtil();
				Iterator irField = qsu.parseField(queryId, parentFormCode).iterator();
				while (irField.hasNext()) {
					String fName = (String)irField.next();
					%>
					fieldParams += "&<%=fName%>=" + encodeURI(o('<%=fName%>').value);
					<%
				}
			}
			%>
			openWin("<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?op=query&flowId=<%=flowId%>&id=<%=queryId%>&mode=sel&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + fieldParams, 800, 600);
		}
		else {
			openWin("<%=request.getContextPath()%>/visual/module_list_nest_sel.jsp?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + "&mainId=<%=mainId%>", 800, 600);
		}
	}
	
	try {
		// 在Render中生成了此方法，因为通过loadNestCtl，ajax方式来获取嵌套表格2，所以在此需再调用一下以隐藏列
		hideNestCol();
	}
	catch (e) {}
	
	function showModule_<%=formCode%>(visitKey, id) {
		addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/visual/module_show.jsp?visitKey=' + visitKey + '&parentId=' + id + '&id=' + id + '&code=<%=formCode%>');
	}
	</script>
    <table id="cwsNestTable_<%=formCode%>" class="tabStyle_1" style="width:100%;margin:0px" border="0" align="center" cellpadding="2" cellspacing="0">
      <thead>
      <tr ondblclick="<%=ondblclickScript%>" title="<%=ondblclickTitle%>" align="center" class="cwsThead">
        <td style="width:40px;">
		<!--ID-->
		序号</td>
    <%
	String action = ParamUtil.get(request, "action");
	boolean isArchive = "archive".equals(action);
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
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
		
		FormField ff = fd.getFormField(fieldName);
		String macroType = "";
		if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
			macroType = ff.getMacroType();
		}		
	%>
        <td fieldName="<%=fieldName%>" macroType="<%=macroType%>" width="<%=fieldsWidth[i]%>"><%=title%></td>
        <%}%>
        <%
        // 如果一项权限都没有，则不显示操作列，20180615 fgf 还是得显示，因为需要查看
		boolean isOpShow = false;
		if (op.equals("edit") && !isArchive) {
			if (canAdd || canEdit || canImport || canExport || (canSel && queryId!=-1) || (canSel && mapAry.length()>0)) {
				isOpShow = true;
			}
		}		
		// 因为不可写时，仍需要有查看按钮，此时使列宽为50
		if (true || isOpShow) {
		%>
        <td class="tdOperate" style="width:<%=isOpShow?"150":"50"%>px">
        <%if (op.equals("edit") && canAdd) {%>
        <img class="imgBtn" title="增加" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/add.png" style="cursor:pointer" onclick="add_row_<%=formCode%>('<%=formCode%>')" />
        <%}%>
        <%if (op.equals("edit") && canImport) {%>
        <img class="imgBtn" title="导入" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/import.png" style="cursor:pointer" onclick="importForSheet_<%=formCode%>(<%=cwsId%>)" />        
        <%}%>
        <%if (op.equals("edit") && canExport) {%>
        <img class="imgBtn" title="导出" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/export.png" style="cursor:pointer" onclick="openWin('<%=request.getContextPath()%>/visual/module_excel_relate.jsp?parentId=<%=cwsId%>&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>');" />        
        <%}%>        
        <%
		String pageType = ParamUtil.get(request, "pageType");		
		if (op.equals("edit") && canSel && queryId!=-1) {%>
        <img class="imgBtn" title="选择" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/sel.png" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=cwsId%>, true)" />        
        <%}%>      
        <%if (op.equals("edit") && canSel && mapAry.length()>0) {%>
        <img class="imgBtn" title="选择" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/sel.png" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=cwsId%>)" />        
        <%}%>           
        </td>
        <%}%>
      </tr>
      </thead>
      <tbody>
      <%
		com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		String desKey = ssoCfg.get("key");
		
		int k = 0;
		UserMgr um = new UserMgr();

		ondblclickTitle = "";
		ondblclickScript = "";
		if (op.equals("edit")) {
			// ondblclickTitle = "双击本行可以编辑数据";
		}
		
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			RequestUtil.setFormDAO(request, fdao);
			k++;
			long id = fdao.getId();
	  %>
      <tr title="<%=ondblclickTitle%>" align="center" id="<%=formCode%>_<%=fdao.getId()%>">
        <td editable=0 title="编号：<%=fdao.getId()%>">
		<%=k%>
        </td>
        <%
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
		%>
        <td align="left">
		<%if (!fieldName.equals("cws_creator")) {
			if (fieldName.startsWith("main")) {
				String[] ary = StrUtil.split(fieldName, ":");
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
				com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(StrUtil.toInt(cwsId));
				FormField ff = mainFormDb.getFormField(ary[2]);
				if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2])));
					}
				} else {
					out.print(fdmMain.getFieldValueOfMain(StrUtil.toInt(cwsId), ary[2]));
				}
			}
			else if (fieldName.startsWith("other:")) {
				// System.out.println(getClass() + " fieldName=" + fieldName);
				// String[] ary = StrUtil.split(fieldName, ":");
				
				// FormDb otherFormDb = fm.getFormDb(ary[2]);
				// com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
				// out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
				out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
			}
			else{
				FormField ff = fd.getFormField(fieldName);
				if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					// System.out.println(getClass() + " fieldName22=" + fieldName + " ff.getType()=" + ff.getType() + " mu=" + mu);
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}else{%>
					<%=FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName))%>
				<%}
			}%>
        <%}else{
		%>
            <%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
        <%}%>
        </td>
        <%}
		// 以flowId作为值加密
		String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(id));	
		%>
    	<%if (true || isOpShow) {%>
        <td class="tdOperate">
        <%if (op.equals("edit") && canEdit) {%>
        	<img class="imgBtn" title="修改" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/edit.png" style="cursor:pointer" onclick="edit_row_<%=formCode%>(<%=id%>)" />        
        <%}
		else {%>
        	<img class="imgBtn" title="查看" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/info.png" style="cursor:pointer;" onclick="showModule_<%=formCode%>('<%=visitKey%>', <%=id%>)" />        
        <%}
		%>
        <%if (op.equals("edit") && canDel) {%>
        <img class="imgBtn" title="删除" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/del.png" style="cursor:pointer" onclick="del_row_<%=formCode%>(<%=id%>)" />        
        <%}%>
        </td>
        <%}%>
      </tr>
      <%
	  }
	%>
    </tbody>
</table>
<%}else{
	cwsId = String.valueOf(FormDAO.TEMP_CWS_ID);
	%>
<script>
	function updateRow(formCode, fdaoId, tds, token) {
		var ary = tds.split(token);
		$('#' + formCode + '_' + fdaoId).children().each(function(k) {
            if (k>ary.length)
				return;
            // 跳过第一列（序号）
			if (k==0) {
			    return;
			}
			$(this).html(ary[k-1]);
		});
		callCalculateOnload();
	}

	function add_row_<%=formCode%>() {
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_add_relate.jsp?isShowNav=0&parentId=<%=cwsId%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>";
		openWin(url,"800","600");						
	}
	
	function edit_row_<%=formCode%>(id) {
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_edit_relate.jsp?parentId=<%=cwsId%>&id=" + id + "&menuItem=&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>&isShowNav=0&actionId=<%=actionId%>";
		openWin(url,"800","600");
	}
	
	function del_row_<%=formCode%>(id) {
		jConfirm("您确定要删除吗?","提示",function(r){
			if(!r){return;}
			else{
			    // o("iframeModuleRelate<%=formCode%>").src = "<%=request.getContextPath()%>/visual/nest_sheet_del_relate.jsp?action=del&id=" + id + "&mode=delForTmpAdd&formCode=<%=parentFormCode%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&parentId=<%=cwsId%>";
	            // $("#<%=formCode%>_" + id).remove();
				try {
					onNestDelRow('<%=formCode%>', id);
				}
				catch(e) {}									
				$.ajax({
					type: "get",
					url: "<%=request.getContextPath()%>/visual/nest_sheet_del_relate.jsp?action=del&id=" + id + "&mode=delForTmpAdd&formCode=<%=parentFormCode%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&parentId=<%=cwsId%>",
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$(document.body).showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
				            $("#<%=formCode%>_" + id).remove();
                            refreshSerialNo_<%=formCode%>();
							// 模块添加的时候不能刷新，否则因页面reload，表单中的数据会全部丢失
							//if("<%=newIds%>" !== ""){
                                //refreshNestSheetCtl<%=moduleCode%>("<%=newIds%>");
                            //}
                            //移除tempCwsId_中的id值
                            $("input[name='tempCwsId_<%=formCode%>'][value=" + id + "]").remove();
							// jAlert(data.msg, "提示");
							// 用于form_js_***.jsp调用			
							try {
								onNestAfterDelRow('<%=formCode%>', id, <%=cwsId%>);
							}
							catch(e) {}											
						}
					},
					complete: function(XMLHttpRequest, status){
						$(document.body).hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						$(document.body).hideLoading();
						alert(XMLHttpRequest.responseText);
					}
				});					
            }
		});
	}
	
	function importForSheet_<%=formCode%>(parentId) {
		openWin("<%=request.getContextPath()%>/visual/nest_sheet_import_excel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>&moduleCode=<%=moduleCode%>&parentId=" + parentId, 480, 80);
	}
	
	function sel_<%=formCode%>(parentId, isQuery) {
		if (isQuery) {
			var fieldParams = "";		
			<%
			if (queryId!=-1) {
				// 取得查询脚本中需从request中获取的字段值
				QueryScriptUtil qsu = new QueryScriptUtil();
				Iterator irField = qsu.parseField(queryId, parentFormCode).iterator();
				while (irField.hasNext()) {
					String fName = (String)irField.next();
					%>
					fieldParams += "&<%=fName%>=" + encodeURI(o('<%=fName%>').value);
					<%
				}
			}
			%>		
			openWin("<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?op=query&id=<%=queryId%>&mode=sel&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + fieldParams, 800, 600);
		}
		else {
			openWin("<%=request.getContextPath()%>/visual/module_list_nest_sel.jsp?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + "&mainId=<%=mainId%>", 800, 600);
		}
	}
	
</script>    
    <table id="cwsNestTable_<%=formCode%>" class="tabStyle_1" style="width:100%;margin:0px" border="0" align="center" cellpadding="2" cellspacing="0">
      <thead>
      <tr ondblclick="<%=ondblclickScript%>" title="<%=ondblclickTitle%>" align="center" class="cwsThead">
        <td style="width:40px;">序号</td>
        <%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
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
		
		FormField ff = fd.getFormField(fieldName);
		String macroType = "";
		if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
			macroType = ff.getMacroType();
		}
	%>
        <td fieldName="<%=fieldName%>" macroType="<%=macroType%>" width="<%=fieldsWidth[i]%>"><%=title%></td>
        <%}%>
		<%
		  // 如果一项权限都没有，则不显示操作列
		  boolean isOpShow = false;
		  if (canAdd || canEdit || canImport || canExport || (canSel && queryId!=-1) || (canSel && mapAry.length()>0)) {
			  isOpShow = true;
		  }
		  if (isOpShow) {
		%>
        <td style="width:150px">
        <%if (canAdd) {%>
        <img class="imgBtn" title="增加" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/add.png" style="cursor:pointer" onclick="add_row_<%=formCode%>('<%=formCode%>')" />
        <%}%>
        <%if (canImport) {%>
        <input class="imgBtn" style="display:none" type="button" onclick="importForSheet_<%=formCode%>(<%=cwsId%>)" value="导入" />
        <%}%>
		<%if (canSel && queryId!=-1) {%>
        <img class="imgBtn" title="选择" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/sel.png" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=cwsId%>, true)" />        
        <%}%>
        <%if (canSel && mapAry.length()>0) {%>
        <img class="imgBtn" title="选择" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/sel.png" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=cwsId%>)" />        
        <%}%>        
        </td>
		  <%}%>
      </tr>
      </thead>
      <tbody>
      <%
		  if (!"".equals(newIds)) {
			  String[] idsAry = StrUtil.split(newIds, ",");
			  StringBuffer sb = new StringBuffer();
			  for (String id : idsAry) {
				  StrUtil.concat(sb, ",", StrUtil.sqlstr(id));
			  }
			  String sql = "select id from " + fd.getTableNameByForm() + " where id in (" + sb.toString() + ")";
			  Vector fdaoV = fdao.list(formCode, sql);
			  Iterator ir = fdaoV.iterator();

			  int k = 0;
			  UserMgr um = new UserMgr();

			  ondblclickTitle = "";
			  ondblclickScript = "";
			  if (op.equals("edit")) {
				  // ondblclickTitle = "双击本行可以编辑数据";
			  }

			  while (ir!=null && ir.hasNext()) {
				  fdao = (FormDAO)ir.next();
				  RequestUtil.setFormDAO(request, fdao);
				  k++;
				  long id = fdao.getId();
		  %>
		  <tr title="<%=ondblclickTitle%>" align="center" id="<%=formCode%>_<%=fdao.getId()%>">
			  <td editable=0 title="编号：<%=fdao.getId()%>">
				  <%=k%>
			  </td>
			  <%
				  for (int i=0; i<len; i++) {
					  String fieldName = fields[i];
			  %>
			  <td align="left">
				  <%if (!fieldName.equals("cws_creator")) {
					  if (fieldName.startsWith("main")) {
						  String[] ary = StrUtil.split(fieldName, ":");
						  FormDb mainFormDb = fm.getFormDb(ary[1]);
						  com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
						  com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(StrUtil.toInt(cwsId));
						  FormField ff = mainFormDb.getFormField(ary[2]);
						  if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
							  MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
							  if (mu != null) {
								  out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2])));
							  }
						  } else {
							  out.print(fdmMain.getFieldValueOfMain(StrUtil.toInt(cwsId), ary[2]));
						  }
					  }
					  else if (fieldName.startsWith("other:")) {
						  // System.out.println(getClass() + " fieldName=" + fieldName);
						  // String[] ary = StrUtil.split(fieldName, ":");

						  // FormDb otherFormDb = fm.getFormDb(ary[2]);
						  // com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
						  // out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
						  out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
					  }
					  else{
						  FormField ff = fd.getFormField(fieldName);
						  if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
							  MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
							  // System.out.println(getClass() + " fieldName22=" + fieldName + " ff.getType()=" + ff.getType() + " mu=" + mu);
							  if (mu != null) {
								  out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
							  }
						  }else{%>
				  <%=FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName))%>
				  <%}
				  }%>
				  <%}else{
				  %>
				  <%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
				  <%}%>
			  </td>
			  <%}%>
			  <%if (isOpShow) {%>
			  <td>
				  <%if (canEdit) {%>
				  <img class="imgBtn" title="修改" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/edit.png" style="cursor:pointer" onclick="edit_row_<%=formCode%>(<%=id%>)" />
				  <%}%>
				  <%if (canDel) {%>
				  <img class="imgBtn" title="删除" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/del.png" style="cursor:pointer" onclick="del_row_<%=formCode%>(<%=id%>)" />
				  <%}%>
			  </td>
			  <%}%>
		  </tr>
		  <%
			  }
		  }
	  %>
      </tbody>
</table>
<%}%>
<script>
// 从nest_sheet_add_relate.jsp中调用，嵌套表格2提交后
// 或从module_list_nest_sel.jsp选择数据后调用
var idNum = 1000;
function insertRow_<%=moduleCode%>(formCode, fdaoId, tds, token) {
	var ary = tds.split(token);
	var trHTML = "<tr id='" + formCode + "_" + fdaoId + "'>";
	// 插入序号列
	var trList = $("#cwsNestTable_" + formCode).find("tr");
    var trLen = trList.length;	
	trHTML += "<td title='编号：" + fdaoId + "' align='center'>" + trLen + "</td>";
	
	for (var i=0; i<ary.length; i++) {
		trHTML += "<td align='left'>" + ary[i] + "</td>";
	}
	
	trHTML += "<td align='center'>";
	idNum++;
	<%if (canEdit) {%>
    trHTML += '<img id="imgBtnEdit' + idNum + '" class="imgBtn" title="修改" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/edit.png" onclick="edit_row_' + formCode + '(' + fdaoId + ')" />';
	<%}%>
	<%if (canDel) {%>
	trHTML += "&nbsp;";
    trHTML += '<img id="imgBtnDel' + idNum + '"class="imgBtn" title="删除" align="absmiddle" src="<%=request.getContextPath()%>/images/buttons/del.png" onclick="del_row_' + formCode + '(' + fdaoId + ')" />';
	<%}%>
	trHTML += "</td>";
	trHTML += "</tr>";
		
	$("#cwsNestTable_" + formCode).append(trHTML);
	<%if (canEdit) {%>	
	$("#imgBtnEdit" + idNum).hover(
	  function () { 
		$(this).toggleClass( "imgBtnHover" );
	  },
	  function () {//mouseout
		$(this).toggleClass( "imgBtnHover" );	  
	  }
	);	
	<%}%>
	<%if (canDel) {%>	
	$("#imgBtnDel" + idNum).hover(
	  function () { 
		$(this).toggleClass( "imgBtnHover" );	
	  },
	  function () {//mouseout
		$(this).toggleClass( "imgBtnHover" );	  
	  }
	);
	<%}%>
	// 流程中或者智能模块编辑时，或者查看时，否则在智能模块添加时是没有主模块id号的
	<%if (op.equals("edit") || op.equals("view")) {%>
		refreshNestSheetCtl<%=moduleCode%>();
	<%}%>
	
	// 用于form_js_***.jsp调用
	try {
		onNestInsertRow('<%=moduleCode%>', formCode, fdaoId, tds, token, <%=cwsId%>);	}
	catch(e) {}
}

$(function () {
	initImgBtn();
});

function initImgBtn() {
	$(".imgBtn").hover(
	  function () { 
		$(this).toggleClass( "imgBtnHover" );	
	  },
	  function () {//mouseout
		$(this).toggleClass( "imgBtnHover" );	  
	  }
	);
}
</script>