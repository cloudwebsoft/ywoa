<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.db.ListResult" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.api.ISQLBuilderService" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.flow.query.QueryScriptUtil" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.redmoon.oa.security.SecurityUtil" %>
<%@ page import="cn.js.fan.util.*" %>
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
	response.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
	response.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
	response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
	response.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility

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
StringBuffer requestParamBuf = new StringBuffer();
Enumeration<String> reqParamNames = request.getParameterNames();
while (reqParamNames.hasMoreElements()) {
	String paramName = reqParamNames.nextElement();
	String[] paramValues = request.getParameterValues(paramName);
	if (paramValues.length == 1) {
		String paramValue = ParamUtil.getParam(request, paramName);
		// 过滤掉formCode等
		if ("code".equals(paramName)
				|| "formCode".equals(paramName)
				|| "moduleCode".equals(paramName)
				|| "flowId".equals(paramName)
				|| "parentId".equals(paramName)
				|| "id".equals(paramName)
				|| "op".equals(paramName)
		) {
			;
		}
		else {
			// 传入在定制时，可能带入的其它参数
			StrUtil.concat(requestParamBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
		}
	}
}
%>
<style>
	#cwsNestTable {
		font-size: 9pt;
		word-break: break-all;
		cursor: default;
		BORDER: 1px solid #cccccc;
		border-collapse: collapse;
		border-Color: #cccccc;
		align: center;
	}

	.cwsThead {
		/*background-color:#eeeeee;*/
		height: 20px;
	}

	#cwsNestTable td {
		border: 1px solid #cccccc;
		height: 20px;
	}

	.imgBtn {
		width: 22px;
		height: 22px;
		filter: alpha(opacity=50);
		-moz-opacity: 0.5;
		-khtml-opacity: 0.5;
		opacity: 0.5;
		cursor: pointer;
	}

	.imgBtnHover {
		-moz-opacity: 1;
		-khtml-opacity: 1;
		opacity: 1;
	}

	.paginator {
		margin: 5px 0;
	}

	.search-box input,select {
		vertical-align:middle;
	}
	.search-box input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
		width: 80px;
		line-height: 20px; /*否则输入框的文字会偏下*/
	}
	.search-box {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
	}
	.cond-span {
		display: flex;
		float: left;
		align-items: center;
		text-align: left;
		width: 25%;
		height: 32px;
		margin: 3px 0;
	}
	.cond-title {
		margin: 0;
		padding-right: 3px;
		width: 35%;
		text-align: right;
	}

	.tSearch {
		background: url('src/assets/images/search.png') no-repeat left;
		border: 0;
		padding-left:12px;
		vertical-align: middle;
		cursor:pointer;
		height:21px;
		width:21px;
	}

	.row-add {
		background-color: #FAF0E6;
	}
	.row-pull {

	}
</style>
<script>
// 删除后重新排序
function refreshSerialNo_<%=formCode%>() {
	var trList = $("#cwsNestTable_<%=formCode%>").find("tr");
    for (var i=1;i<trList.length;i++) {
        var tdArr = trList.eq(i).children("td");
        tdArr.eq(1).html(i);
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
String[] fieldsAlign = msd.getColAry(false, "list_field_align");

int len = 0;
if (fields!=null) {
	len = fields.length;
}

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
int isPage = 0, isSearchable = 0, isAddHighlight = 0, isShowFlow = 0, isNoShow = 1;
int pageSize = ParamUtil.getInt(request, "pageSize", -1);
String selWinUrl = "";
FormField nestField = null;
boolean isPropStat = false;
JSONObject jsonPropStat = null;
String nestFilter = "";
FormDb sourceFd = new FormDb();
FormDb parentFd = new FormDb();
String sourceMoudleCode = "";
if (!"".equals(nestFieldName)) {
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

		sourceMoudleCode = json.getString("sourceForm");
		if (!"".equals(sourceMoudleCode)) {
			sourceFd = sourceFd.getFormDb(sourceMoudleCode);
		}

		nestFilter = json.getString("filter");

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
		if (json.has("propStat")) {
			jsonPropStat = json.getJSONObject("propStat");
			/*if (StringUtils.isNotEmpty(propStat)) {
				if ("".equals(propStat)) {
					propStat = "{}";
				}
				jsonPropStat = new JSONObject(propStat);
				if (jsonPropStat.length()>0) {
					isPropStat = true;
				}
			}*/
			if (jsonPropStat.length()>0) {
				isPropStat = true;
			}
		}
		if (json.has("isPage")) {
			isPage = json.getInt("isPage");
			if (pageSize == -1) {
				pageSize = json.getInt("pageSize");
			}
		}
		if (json.has("isSearchable")) {
			isSearchable = json.getInt("isSearchable");
		}
		if (json.has("isAddHighlight")) {
			isAddHighlight = json.getInt("isAddHighlight");
		}
		if (!json.isNull("maps")) {
			mapAry = (JSONArray)json.get("maps");
		}
		if (!json.isNull("queryId")) {
			queryId = StrUtil.toInt((String)json.get("queryId"));
		}
		if (!json.isNull("selWinUrl")) {
			selWinUrl = json.getString("selWinUrl");
		}
		/*if (!json.isNull("isUseModuleFilter")) {
			isUseFilter = json.getBoolean("isUseModuleFilter");
		}*/
		if (!json.isNull("isShowFlow")) {
			isShowFlow = json.getInt("isShowFlow");
		}
		if (!json.isNull("isNoShow")) {
			isNoShow = json.getInt("isNoShow");
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}
}

long actionId = ParamUtil.getLong(request, "actionId", -1);
int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;

// 如果是自动拉单，则会有newIds传过来
String newIds = ParamUtil.get(request, "newIds");
boolean isQuery = false;
String action = ParamUtil.get(request, "action");

int curPage = ParamUtil.getInt(request, "curPage", 1);
ListResult lr = null;

String cwsId = "";

// String url = request.getRequestURI() + "?" + request.getQueryString();
String url = "/visual/nest_sheet_view.jsp?" + request.getQueryString();
boolean isShowChk = ("edit".equals(op) || "add".equals(op)) && canDel;
String formName = ParamUtil.get(request, "cwsFormName");
%>
<script>
	function selBatchInNestSheet(formCode, fieldName, obj) {
		if (obj.checked) {
			$('#cwsNestTable_' + formCode).find('.row-chk').prop('checked', true);
		} else {
			$('#cwsNestTable_' + formCode).find('.row-chk').prop('checked', false);
		}
	}

	function delBatch<%=formCode%>() {
		var ids = '';
		$('#cwsNestTable_<%=formCode%>').find('input[id=rowId]:checked').each(function (k) {
			if (ids == '') {
				ids = $(this).val();
			} else {
				ids += ',' + $(this).val();
			}
		});
		console.log('delBatch ids', ids);
		if (ids == '') {
			myMsg('请选择记录', 'warn');
			return;
		}

		myConfirm('提示', '您确定要删除么', delBatchCallBack_<%=formCode%>, ids);
	}

	function delBatchCallBack_<%=formCode%>(ids) {
		var url = "/flow/delNestSheetRelated.do?ids=" + ids + "&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&parentId=<%=StrUtil.UrlEncode(cwsId)%>";
		var params = {};
		ajaxPost(url, params).then((data) => {
			console.log('data', data);
			myMsg(data.msg);
			if (data.ret == "1") {
				// 如果是编辑状态，则刷新嵌套表
				<%if ("edit".equals(op)) {%>
				refreshNestSheetCtl<%=formCode%>();
				<%}else{%>
				var ary = ids.split(',');
				for (var i in ary) {
					console.log('delBatchCallBack ary[' + i + ']=', ary[i]);
					$("#<%=formCode%>_" + ary[i]).remove();
					// 移除可能在智能模块添加页插入的tempCwsId_中的id值
					$("input[name='tempCwsId_<%=formCode%>'][value=" + ary[i] + "]").remove();
				}
				<%
				}%>

				refreshSerialNo_<%=formCode%>();

				callCalculateOnload();

				eventTarget.fireEvent({
					type: EVENT_TYPE.NEST_DEL,
					moduleCode: "<%=moduleCode%>"
				});

				try {
					onNestAfterDelRow('<%=formCode%>', ids, "<%=StrUtil.UrlEncode(cwsId)%>");
				}
				catch(e) {}
				// 用于form_js_***.jsp调用
				try {
					onNestSheetChange("<%=moduleCode%>", "del", id);
				} catch (e) {};
			}
		});
	}

	// 将单元格的paddin置为0，以使得嵌套表格2占满全部单元格
	$('#nestsheet_<%=nestFieldName%>').closest('td').css('padding', '0px');
</script>
<%
// 流程中或者智能模块编辑时，或者查看时
if ("edit".equals(op) || "view".equals(op)) {
	// cwsId为fdao的id
	cwsId = ParamUtil.get(request, "cwsId");
	if ("".equals(parentFormCode)) {
		out.print("嵌套表参数：父模块编码为空！");
		return;
	}
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
	String relateFieldValue = fdm.getRelateFieldValue(StrUtil.toLong(cwsId), moduleCode);
	if (relateFieldValue==null) {
		out.print(SkinUtil.makeErrMsg(request, "请检查模块" + fd.getName() + "（编码：" + formCode + "）是否相关联"));
		return;
	}

	String orderBy = "id", sort = "asc";

	String sql = "select id from " + fd.getTableNameByForm() + " t1 where cws_id=" + StrUtil.sqlstr(relateFieldValue);
	boolean isUseFilter = true;
	flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);

	String mainPageType = ParamUtil.get(request, "mainPageType");

	if (!"".equals(newIds)) {
		String[] idsAry = StrUtil.split(newIds, ",");
		StringBuffer sb = new StringBuffer();
		for (String id : idsAry) {
			StrUtil.concat(sb, ",", StrUtil.sqlstr(id));
		}
		sql = "select id from " + fd.getTableNameByForm() + " t1 where id in (" + sb.toString() + ")";
	}
	else {
		// 流程中使用filter
		// if (flowId != com.redmoon.oa.visual.FormDAO.NONEFLOWID && mainPageType.contains("flow")) {
		if (flowId != com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
			// 以防止出现嵌套表被多个主表调用时，因为cws_id的重复，出现拉取了重复数据的情况
			sql += " and flowId=" + flowId;
			
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);
			if (!wf.isLoaded()) {
				out.print(SkinUtil.makeErrMsg(request, "流程：" + flowId + "不存在"));
				return;
			}
			
			WorkflowPredefineDb wpd = new WorkflowPredefineDb();
			wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());	
			if (!wpd.isModuleFilter()) {
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

		if ("search".equals(action)) {
			ISQLBuilderService sqlBuilderService = SpringUtil.getBean(ISQLBuilderService.class);
			Object[] aryCondAndUrlStr = sqlBuilderService.fitCondAndUrlStr(request, msd, fd);
			String cond = (String) aryCondAndUrlStr[0];
			if (!"1=1".equals(cond)) {
				sql += " and " + cond;
			}
		}
		
		// 当运用于智能模块中时，始终启用嵌套表对应主模块中的过滤条件
		if (isUseFilter) {
			String userName = privilege.getUser(request);
			String[] ary = ModuleUtil.parseFilter(request, msd.getString("form_code"), StrUtil.getNullStr(msd.getFilter(userName)));
			if (ary!=null) {
				String filter = ary[0];
				if (filter!=null && !"".equals(filter)) {
					if (filter.toLowerCase().startsWith("select ")) {
						sql = filter;
					}
					else {
						sql += " and " + filter;
					}
				}
			}

			// 20200701 使可根据模块配置中的排序方式进行排序
			String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
			boolean isComb = filter.startsWith("<items>") || "".equals(filter);
			// 如果是组合条件，则赋予后台设置的排序字段
			if (isComb) {
				orderBy = StrUtil.getNullStr(msd.getString("orderby"));
				sort = StrUtil.getNullStr(msd.getString("sort"));
			}
		}
	}

	// 模块中可能未设置排序orderBy就会为空
	if ("".equals(orderBy)) {
		orderBy = "id";
		sort = "asc";
	}

	// 如果sql语句中含有order by，则说明sql中脚本条件中含有order by，如： select field from table order by dt asc;
	int p = sql.lastIndexOf("order by ");
	if (p==-1) {
		sql += " order by " + orderBy + " " + sort;
	}

	DebugUtil.i(getClass(), "nest_sheet_view.jsp", " formCode=" + formCode + " sql=" + sql);

	Vector fdaoV;
	if (isPage == 1) {
		try {
			lr = fdao.listResult(formCode, sql, curPage, pageSize);
		} catch (ErrMsgException e) {
			e.printStackTrace();
			out.print(e.getMessage());
			return;
		}
		fdaoV = lr.getResult();
	}
	else {
		fdaoV = fdao.list(formCode, sql);
	}
	Iterator ir = fdaoV.iterator();

	com.alibaba.fastjson.JSONObject jsonSums = com.alibaba.fastjson.JSONObject.parseObject(FormUtil.getSums(fd, parentFd, cwsId).toString());
	%>
	<script>
	// 计算控件回调
	function callByNestSheet(nestSheetSums, formCode) {
		console.log('macro_js_nestsheet callByNestSheet nestSheetSums', JSON.stringify(nestSheetSums));
		if (nestSheetSums != null) {
			if (typeof (nestSheetSums) == 'object') {
				var keys = '';
				// 20220730 将o由原来的sum(nest.je)中的je改为计算控件的字段名
				for (var o in nestSheetSums) {
					if (keys.indexOf(',' + o + ',') != -1) {
						// 跳过已正常取得的字段，因为可能在sum时两个嵌套表中都含有同名的字段，而其中一个是有formCode属性的
						continue;
					}
					console.log('keys', keys);
					var $ctl = $("input[name='" + o + "'][formCode='" + formCode + "']");
					if (!$ctl[0]) {
						// 向下兼容会带来问题，如果在sum时两个嵌套表中都含有同名的字段，会导致出现问题，故需带有formCode属性的计算控件字段记住
						// 向下兼容，旧版的sum型计算控件中没有formCode
						$ctl = $("input[name='" + o + "']");
					} else {
						if (keys == '') {
							keys = ',' + o + ',';
						} else {
							keys += o + ',';
						}
					}
					$ctl.val(nestSheetSums[o]);
				}
			}
		}
	}

	// $(function() {
		// console.log('<%=jsonSums.toString()%>');
	<%
	// 注意仅可编辑时才调用，否则例如：当事务所内资审核时，查看项目详情，切换选项卡，看以往审核记录，查看记录的详情，
	// 再回到审核流程处理界面时，callByNestSheet会将以往审核记录的值赋予给流程表单中的同名字段，
	// 即认定栏投资方上方的那些字段（均为计算控件，但实际上已无法计算，因为投资方嵌套表中没有对应的列）
	if ("edit".equals(op)) {
	%>
		callByNestSheet(<%=jsonSums.toString()%>, '<%=formCode%>');
	<%
	}
	%>
	// })

	function fireEventSelect_<%=moduleCode%>() {
		eventTarget.fireEvent({
			type: EVENT_TYPE.NEST_SELECT,
			moduleCode: "<%=moduleCode%>"
		});
	}

	function add_row_<%=formCode%>() {
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_add_relate.jsp?isShowNav=0&parentId=<%=StrUtil.UrlEncode(cwsId)%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>&flowId=<%=flowId%>&actionId=<%=actionId%>";
		// openWin(url,"1000","800");

		openSmartModuleRelateTableDrawer(1, {parentId: <%=cwsId%>, moduleCode: '<%=parentFormCode%>', moduleCodeRelated: '<%=moduleCode%>', flowId: <%=flowId%>, pageType: 'add_relate'});
	}

	function edit_row_<%=formCode%>(id) {
	    // console.log("id=" + id);
		<%
		/*if (!"edit".equals(op)) {
			%>
			// return;
			<%
		}*/
		%>
		var url = "<%=request.getContextPath()%>/visual/nest_sheet_edit_relate.jsp?parentId=<%=StrUtil.UrlEncode(cwsId)%>&id=" + id + "&menuItem=&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&formCode=<%=parentFormCode%>&isShowNav=0&actionId=<%=actionId%>";
		// openWin(url,"1000","800");
		openSmartModuleRelateTableDrawer(2, {parentId: <%=cwsId%>, id: id, moduleCode: '<%=parentFormCode%>', moduleCodeRelated: '<%=moduleCode%>', flowId: <%=flowId%>, pageType: 'edit_relate'});
	}

	function delCallBack_<%=formCode%>(id) {
		var url = "/flow/delNestSheetRelated.do?ids=" + id + "&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&parentId=<%=StrUtil.UrlEncode(cwsId)%>";
		var params = {};
		ajaxPost(url, params).then((data) => {
			console.log('data', data);
			myMsg(data.msg);
			if (data.ret == "1") {
				$("#<%=formCode%>_" + id).remove();
				refreshSerialNo_<%=formCode%>();

				loadNestCtl('<%=url%>', 'nestsheet_<%=nestFieldName%>', <%=curPage%>, <%=pageSize%>, getConds(), '<%=formName%>');

				eventTarget.fireEvent({
					type: EVENT_TYPE.NEST_DEL,
					moduleCode: "<%=moduleCode%>"
				});

				// 移除可能在智能模块添加页插入的tempCwsId_中的id值
				$("input[name='tempCwsId_<%=formCode%>'][value=" + id + "]").remove();

				// callCalculateOnload();
				try {
					onNestAfterDelRow('<%=formCode%>', id, "<%=StrUtil.UrlEncode(cwsId)%>");
				}
				catch(e) {}
				// 用于form_js_***.jsp调用
				try {
					onNestSheetChange("<%=moduleCode%>", "del", id);
				} catch (e) {};
			}
		});
	}
	
	function del_row_<%=formCode%>(id) {
		myConfirm('提示', '您确定要删除么', delCallBack_<%=formCode%>, id);
	}
	
	function importForSheet_<%=formCode%>(parentId) {
		openImportExcelModal(parentId, "<%=moduleCode%>", "<%=parentFormCode%>", <%=flowId%>, "<%=nestFieldName%>", "nest_sheet");
		// openWin("<%=request.getContextPath()%>/visual/nest_sheet_import_excel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>&moduleCode=<%=moduleCode%>&flowId=<%=flowId%>&parentId=" + parentId + "&<%=requestParamBuf.toString()%>", 480, 80);
	}

	function doExportExcelRelate() {
		// nestType=<%=MacroCtlUnit.NEST_TYPE_NORMAIL%>&parentId=<%=StrUtil.UrlEncode(cwsId)%>&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>
		exportExcelRelate(
				'<%=MacroCtlUnit.NEST_TYPE_NORMAIL%>',
				'<%=cwsId%>',
				'<%=parentFormCode%>',
				'<%=formCode%>',
				'<%=fd.getName()%>',
		)
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
			<%
			if ("".equals(selWinUrl)) {
				String condFields = String.join("|", ModuleUtil.getModuleListNestSelCondFields(nestFilter));
			%>
			// openWin("<%=request.getContextPath()%>/visual/moduleListNestSel.do?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + "&mainId=<%=mainId%>", 800, 600);
			openWinModuleListNest("<%=parentFormCode%>", "<%=formCode%>", "<%=nestFieldName%>", "nest_sheet", parentId, <%=mainId%>, "<%=condFields%>");
			<%
			} else {
			%>
			openWin("<%=request.getContextPath()%>/<%=selWinUrl%>?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + "&mainId=<%=mainId%>", 800, 600);
			<%
			}
			%>
		}
	}
	
	try {
		// 在Render中生成了此方法，因为通过loadNestCtl，ajax方式来获取嵌套表格2，所以在此需再调用一下以隐藏列
		hideNestCol();
	}
	catch (e) {}
	
	function showModule_<%=formCode%>(visitKey, id, quoteFlowId, quoteId, quoteModuleCode) {
		var isShowFlow = <%=isShowFlow%>;
		if (isShowFlow ==1 && quoteFlowId != -1) {
			// addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/flowShowPage.do?flowId=' + quoteFlowId + '&isNav=false&visitKey=' + visitKey);
			openWinFlowShow(quoteFlowId, visitKey);
		}
		else if (quoteId != -1) {
			// addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/visual/moduleShowPage.do?moduleCode=' + quoteModuleCode + '&id=' + quoteId + '&visitKey=' + visitKey);
			openWinModuleShow(quoteModuleCode, quoteId, visitKey);
		}
		else {
			// 需用moduleShowRelatePage，否则当用moduleShowPage查看时点击编辑按钮，可能就有问题，比如：预算阶段需取父表单中的记录，这样就取不到了
			// addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/visual/moduleShowRelatePage.do?visitKey=' + visitKey + '&isNav=false&parentId=<%=cwsId%>&id=' + id + '&code=<%=parentFormCode%>&moduleCodeRelated=<%=moduleCode%>');

			// 暂时先用openWinModuleShow替代实现
			// openWinModuleRelateShow('<%=parentFormCode%>', <%=moduleCode%>, <%=cwsId%>, id, visitKey);
			openWinModuleShow('<%=moduleCode%>', id, visitKey);
		}
	}
	</script>

<%
		if (isSearchable == 1) {
	%>
	<div class="search-box">
		<%
			// 显示查询条件
			String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
			String[] btnNames = StrUtil.split(btnName, ",");
			String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
			String[] btnScripts = StrUtil.split(btnScript, "#");
			ArrayList<String> dateFieldNamelist = new ArrayList<String>();
			if (btnNames != null) {
				for (int i = 0; i < btnNames.length; i++) {
					if (btnScripts[i].startsWith("{")) {
						// 用于表单域宏控件中convertToHTMLCtlForQuery方法中调用getOptions时
						request.setAttribute("isNestSheetQuery", "y");
						Map<String, String> checkboxGroupMap = new HashMap<>();
						JSONObject queryJson = new JSONObject(btnScripts[i]);
						if (queryJson.get("btnType").equals("queryFields")) {
							String condFields = (String) queryJson.get("fields");
							String condTitles = "";
							if (queryJson.has("titles")) {
								condTitles = (String) queryJson.get("titles");
							}
							String[] fieldAry = StrUtil.split(condFields, ",");
							String[] titleAry = StrUtil.split(condTitles, ",");
							if (fieldAry.length > 0) {
								isQuery = true;
							}
							for (int j = 0; j < fieldAry.length; j++) {
								String fieldName = fieldAry[j];
								String fieldTitle = "#";
								if (titleAry!=null) {
									fieldTitle = titleAry[j];
									if ("".equals(fieldTitle)) {
										fieldTitle = "#";
									}
								}

								String condType = (String) queryJson.get(fieldName);
								CondUnit condUnit = CondUtil.getCondUnit(request, msd, fd, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist);
								out.print("<span class=\"cond-span\">");
								out.print("<span class=\"cond-title\">");
								out.print(condUnit.getFieldTitle());
								out.print("</span>");
								out.print(condUnit.getHtml());
								out.print("</span>");
								out.print("<script>");
								out.print(condUnit.getScript());
								out.print("</script>");
							}
						}
					%>
						<span class="cond-span">
						<input type="hidden" name="code" value="<%=moduleCode%>" />
						<input type="hidden" name="mainCode" value="<%=ParamUtil.get(request, "mainCode")%>" />
						<%if (isQuery) { %>
							<input id="btn<%=nestFieldName%>" class="tSearch" type="button" value="" />
						<%}%>
						</span>
						<script>
							$(function() {
								$('#btn<%=nestFieldName%>').click(function(e) {
									e.preventDefault();

									var conds = getConds();
									loadNestCtl('<%=url%>', 'nestsheet_<%=nestFieldName%>', 1, <%=pageSize%>, conds, '<%=formName%>');
								})
							});
						</script>
					<%
					}
				}
			}
		%>
	</div>
<%
	}
%>
    <table id="cwsNestTable_<%=formCode%>" formCode="<%=formCode%>" class="tabStyle_1" style="width:100%;margin:0px" border="0" align="center" cellpadding="2" cellspacing="0">
      <thead>
      <tr ondblclick="<%=ondblclickScript%>" title="<%=ondblclickTitle%>" align="center" class="cwsThead">
		  <td class="td-chk" align="center" style="width: 35px; display: <%=isShowChk?"":"none"%>">
			  <input type="checkbox" onclick="selBatchInNestSheet('<%=formCode%>', '<%=nestFieldName%>', this)" />
		  </td>
        <td style="width:50px;display:<%=isNoShow==1?"":"none"%>">
		<!--ID-->
		序号
		</td>
    <%
	boolean isArchive = "archive".equals(action);
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title = "创建者";

		// 跳过列表中添加的操作列
		if ("colOperate".equals(fieldName)) {
			continue;
		}

		if (!"cws_creator".equals(fieldName)) {
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
			else if ("cws_creator".equals(fieldName)) {
				title = "创建者";
			} else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
				title = "ID";
			} else if ("cws_status".equals(fieldName)) {
				title = "状态";
			} else if ("cws_flag".equals(fieldName)) {
				title = "冲抵状态";
			} else if ("flowId".equalsIgnoreCase(fieldName)) {
				title = "流程号";
			} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
				title = "流程开始时间";
			} else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
				title = "流程结束时间";
			} else if ("cws_id".equals(fieldName)) {
				title = "关联ID";
			}
			else if ("cws_visited".equals(fieldName)) {
				title = "是否已读";
			}
			else if ("colPrompt".equals(fieldName)) {
				title = "colPrompt"; //
			}
			else if ("cws_create_date".equals(fieldName)) {
				title = "创建时间";
			} else if ("cws_modify_date".equals(fieldName)) {
				title = "修改时间";
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
		boolean isOpShow = true;
		if (op.equals("edit") && !isArchive) {
			if (canAdd || canEdit || canImport || canExport || (canSel && queryId!=-1) || (canSel/* && mapAry.length()>0*/)) {
				isOpShow = true;
			}
		}		
		// 因为不可写时，仍需要有查看按钮，此时使列宽为50
		if (true || isOpShow) {
		%>
        <td class="tdOperate" style="width:<%=isOpShow?"150":"50"%>px">
        <%if ("edit".equals(op) && canAdd) {%>
			<a class="link-btn" title="增加" style="cursor:pointer" onclick="add_row_<%=formCode%>('<%=formCode%>')">
				<i class="fa fa-plus-circle link-icon link-icon-add"></i>
			</a>
		<%}%>
        <%if ("edit".equals(op) && canImport) {%>
			<a class="link-btn" title="导入" style="cursor:pointer" onclick="importForSheet_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>)"><i class="fa fa-arrow-circle-o-down link-icon link-icon-edit"></i></a>
        <%}%>
        <%if ("edit".equals(op) && canExport) {%>
<%--
			<a class="link-btn" title="导出" style="cursor:pointer" onclick="openWin('<%=request.getContextPath()%>/visual/exportExcelRelate.do?nestType=<%=MacroCtlUnit.NEST_TYPE_NORMAIL%>&parentId=<%=StrUtil.UrlEncode(cwsId)%>&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>');"><i class="fa fa-arrow-circle-o-up link-icon link-icon-edit"></i></a>
--%>
			<a class="link-btn" title="导出" style="cursor:pointer" onclick="doExportExcelRelate()"><i class="fa fa-arrow-circle-o-up link-icon link-icon-edit"></i></a>
        <%}%>
        <%
		if ("edit".equals(op) && canSel && queryId!=-1) {%>
			<a class="link-btn" title="选择" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>, true)"><i class="fa fa-check-square-o link-icon link-icon-show"></i></a>
        <%}%>
        <%if ("edit".equals(op) && canSel/* && mapAry.length()>0*/) {%>
			<a class="link-btn" title="选择" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>)"><i class="fa fa-check-square-o link-icon link-icon-show"></i></a>
        <%}%>
		<%if ("edit".equals(op) && canDel) {%>
		<a class="link-btn" title="批量删除" style="cursor:pointer" onclick="delBatch<%=formCode%>()"><i class="fa fa-trash-o link-icon link-icon-del"></i></a>
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
		WorkflowDb wf = new WorkflowDb();
		ondblclickTitle = "";
		ondblclickScript = "";
		if ("edit".equals(op)) {
			// ondblclickTitle = "双击本行可以编辑数据";
		}

		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			RequestUtil.setFormDAO(request, fdao);
			k++;
			long id = fdao.getId();
			String cls = "";
			if (isAddHighlight==1) {
				if (fdao.getCwsQuoteId() == 0) {
					cls = "row-add";
				} else {
					cls = "row-pull";
				}
			}
			int quoteFlowId = -1;
			long quoteId = -1;
			if (isShowFlow == 1) {
				quoteId = fdao.getCwsQuoteId();
				FormDAO daoQuote = new FormDAO();
				daoQuote = daoQuote.getFormDAO(quoteId, sourceFd);
				quoteFlowId = daoQuote.getFlowId();
			}
	  %>
      <tr title="<%=ondblclickTitle%>" align="center" id="<%=formCode%>_<%=fdao.getId()%>" class="<%=cls%>">
		  <td class="td-chk" align="center" style="display: <%=isShowChk?"":"none"%>">
			<input id="rowId" value="<%=fdao.getId()%>" class="row-chk" type="checkbox"/>
		  </td>
        <td editable="0" style="display: <%=isNoShow==1?"":"none"%>" title="ID：<%=fdao.getId()%>">
		<%=k%>
        </td>
        <%
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			// 跳过列表中添加的操作列
			if ("colOperate".equals(fieldName)) {
				continue;
			}
		%>
        <td align="<%=fieldsAlign[i]%>">
		<%if (!"cws_creator".equals(fieldName)) {
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
				out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
			}
			else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
				out.print(String.valueOf(fdao.getId()));
			} else if ("cws_flag".equals(fieldName)) {
				out.print(String.valueOf(fdao.getCwsFlag()));
			} else if ("cws_creator".equals(fieldName)) {
				out.print(StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName()));
			} else if ("cws_status".equals(fieldName)) {
				out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
			} else if ("flowId".equalsIgnoreCase(fieldName)) {
				out.print(String.valueOf(fdao.getFlowId()));
			} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
				if (fdao.getFlowId() != -1) {
					wf = wf.getWorkflowDb(fdao.getFlowId());
					out.print(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss"));
				}
			} else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
				if (fdao.getFlowId() != -1) {
					wf = wf.getWorkflowDb(fdao.getFlowId());
					out.print(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
				}
			} else if ("cws_id".equals(fieldName)) {
				out.print(String.valueOf(fdao.getCwsId()));
			}
			else if ("cws_visited".equals(fieldName)) {
				out.print(fdao.isCwsVisited()?"是":"否");
			}
			else if ("colPrompt".equals(fieldName)) {
				continue;
			} else if ("cws_create_date".equals(fieldName)) {
				out.print(DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss"));
			} else if ("cws_modify_date".equals(fieldName)) {
				out.print(DateUtil.format(fdao.getCwsModifyDate(), "yyyy-MM-dd HH:mm:ss"));
			}
			else{
				FormField ff = fd.getFormField(fieldName);
				if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
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
		// 以id作为值加密
		// String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(id));
		String visitKey = SecurityUtil.makeVisitKey(String.valueOf(id));
		if (isShowFlow == 1 && quoteFlowId != -1) {
			visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(quoteFlowId));
		}
		%>
    	<%if (true || isOpShow) {%>
        <td class="tdOperate">
        <%if ("edit".equals(op) && canEdit) {%>
			<a class="link-btn" title="修改" style="cursor:pointer" onclick="edit_row_<%=formCode%>(<%=id%>)"><i class="fa fa-edit link-icon link-icon-edit"></i></a>
        <%}
		else {%>
			<a class="link-btn" title="查看" style="cursor:pointer" onclick="showModule_<%=formCode%>('<%=visitKey%>', <%=id%>, <%=quoteFlowId%>, <%=quoteId%>, '<%=sourceMoudleCode%>')"><i class="fa fa-file-text-o link-icon link-icon-show"></i></a>
        <%}
		%>
        <%if ("edit".equals(op) && canDel) {%>
			<a class="link-btn" title="删除" style="cursor:pointer" onclick="del_row_<%=formCode%>(<%=id%>)"><i class="fa fa-trash-o link-icon link-icon-del"></i></a>
        <%}%>
        </td>
        <%}%>
      </tr>
      <%
		  }

		  StringBuffer trStatHtml = new StringBuffer();
		  if (isPropStat) {
			  int n = 0;
			  for (int m = 0; m < fields.length; m++) {
				  FormField formField = fd.getFormField(fields[m]);
				  if (formField == null) {
					  continue;
				  }

				  Iterator ir3 = jsonPropStat.keys();
				  while (ir3.hasNext()) {
					  String fieldName = (String) ir3.next();

					  if (!formField.getName().equals(fieldName)) {
						  trStatHtml.append("<td></td>");
						  continue;
					  }

					  String modeStat = jsonPropStat.getString(fieldName);

					  FormField ff = fd.getFormField(fieldName);
					  if (ff == null) {
						  DebugUtil.e(getClass(), "合计字段", "field " + fieldName + " is not exist");
					  }
					  int fieldType = ff.getFieldType();

					  double sumVal = 0;
					  if (k > 0) {
						  // 通过FormUtil.getSums(fd, pForm, cwsId)也可以获取到合计值，但只能取到字段为计算控件的值
						  sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
					  }
					  if ("0".equals(modeStat)) {
						  if (fieldType == FormField.FIELD_TYPE_INT
								  || fieldType == FormField.FIELD_TYPE_LONG) {
							  if ("view".equals(op)) {
								  trStatHtml.append("<td><span id='cws_stat_" + fieldName + "' title='合计'>" + (long) sumVal + "</span></td>");
							  } else {
								  trStatHtml.append("<td><input id='cws_stat_" + fieldName + "' title='合计' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='" + (long) sumVal + "'/></td>");
							  }
						  } else {
							  if ("view".equals(op)) {
								  trStatHtml.append("<td><span id='cws_stat_" + fieldName + "' title='合计'>" + NumberUtil.round(sumVal, 2) + "</span></td>");
							  } else {
								  trStatHtml.append("<td><input id='cws_stat_" + fieldName + "' title='合计' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='" + NumberUtil.round(sumVal, 2) + "'/></td>");
							  }
						  }
					  } else if (modeStat.equals("1")) {
						  if ("view".equals(op)) {
							  trStatHtml.append("<td><span id='cws_stat_" + fieldName + "' title='合计'>" + NumberUtil.round(sumVal / k, 2) + "</span></td>");
						  } else {
							  trStatHtml.append("<td><input id='cws_stat_" + fieldName + "' title='平均' readonly class='input-stat' value='" + NumberUtil.round(sumVal / k, 2) + "'/></td>");
						  }
					  }
					  n++;
				  }
			  }

			  trStatHtml.insert(0, "<tr id='trStat'><td align='center'>合计</td>");
			  trStatHtml.append("<td class='tdOperate'></td></tr>"); // 加上操作列，注意得加上class='tdOperate'，因为在打印页面print_preview.jsp中会根据class去除掉操作列

			  out.print(trStatHtml.toString());
		  }
	  %>
    </tbody>
</table>
<%
} else {
	// 添加
	cwsId = String.valueOf(FormDAO.TEMP_CWS_ID);
	%>
<script>
	function updateRow(formCode, fdaoId, tds, token) {
		console.log('updateRow tds', tds, 'token', token);
		var ary = tds.split(token);
		$('#' + formCode + '_' + fdaoId).children().each(function (k) {
			if (k > ary.length)
				return;
			// 跳过 复选框、序号
			if (k == 0 || k == 1) {
				return;
			}

			$(this).html(ary[k - 2]);
		});
		callCalculateOnload();

		eventTarget.fireEvent({
			type: EVENT_TYPE.NEST_EDIT,
			moduleCode: "<%=moduleCode%>"
		});

		try {
			onNestSheetChange("<%=moduleCode%>", "update", fdaoId);
		} catch (e) {}
	}

	function add_row_<%=formCode%>() {
		// var url = "<%=request.getContextPath()%>/visual/nest_sheet_add_relate.jsp?isShowNav=0&parentId=<%=StrUtil.UrlEncode(cwsId)%>&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>";
		// openWin(url,"800","600");
		openSmartModuleRelateTableDrawer(1, {parentId: <%=cwsId%>, moduleCode: '<%=parentFormCode%>', moduleCodeRelated: '<%=moduleCode%>', flowId: -1, pageType: 'add_relate'});
	}
	
	function edit_row_<%=formCode%>(id) {
		// var url = "<%=request.getContextPath()%>/visual/nest_sheet_edit_relate.jsp?parentId=<%=StrUtil.UrlEncode(cwsId)%>&id=" + id + "&menuItem=&moduleCode=<%=moduleCode%>&formCodeRelated=<%=formCode%>&formCode=<%=parentFormCode%>&isShowNav=0&actionId=<%=actionId%>";
		// openWin(url,"800","600");
		openSmartModuleRelateTableDrawer(2, {parentId: <%=cwsId%>, id: id, moduleCode: '<%=parentFormCode%>', moduleCodeRelated: '<%=moduleCode%>', flowId: <%=flowId%>, pageType: 'edit_relate'});
	}
	
	function del_row_<%=formCode%>(id) {
		myConfirm('提示', '您确定要删除么', delCallBack_<%=formCode%>, id);
	}

	function delCallBack_<%=formCode%>(id) {
		var url = "/flow/delNestSheetRelated.do?ids=" + id + "&formCode=<%=parentFormCode%>&formCodeRelated=<%=formCode%>&moduleCode=<%=moduleCode%>&parentId=<%=StrUtil.UrlEncode(cwsId)%>";
		var params = {};
		ajaxPost(url, params).then((data) => {
			console.log('data', data);
			myMsg(data.msg);
			if (data.ret == "1") {
				$("#<%=formCode%>_" + id).remove();
				refreshSerialNo_<%=formCode%>();

				callCalculateOnload();

				// 移除可能在智能模块添加页插入的tempCwsId_中的id值
				$("input[name='tempCwsId_<%=formCode%>'][value=" + id + "]").remove();

				eventTarget.fireEvent({
					type: EVENT_TYPE.NEST_DEL,
					moduleCode: "<%=moduleCode%>"
				});

				try {
					onNestAfterDelRow('<%=formCode%>', id, "<%=StrUtil.UrlEncode(cwsId)%>");
				}
				catch(e) {}
				// 用于form_js_***.jsp调用
				try {
					onNestSheetChange("<%=moduleCode%>", "del", id);
				} catch (e) {};
			}
		});
	}
	
	function importForSheet_<%=formCode%>(parentId) {
		// openWin("<%=request.getContextPath()%>/visual/nest_sheet_import_excel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>&moduleCode=<%=moduleCode%>&parentId=" + parentId + "&<%=requestParamBuf.toString()%>", 480, 80);
		openImportExcelModal(parentId, "<%=moduleCode%>", "<%=parentFormCode%>", <%=flowId%>, "<%=nestFieldName%>", "nest_sheet");
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
			<%
				String condFields = String.join("|", ModuleUtil.getModuleListNestSelCondFields(nestFilter));
			%>
			// openWin("<%=request.getContextPath()%>/visual/moduleListNestSel.do?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_sheet&parentId=" + parentId + "&mainId=<%=mainId%>", 800, 600);
			openWinModuleListNest("<%=parentFormCode%>", "<%=formCode%>", "<%=nestFieldName%>", "nest_sheet", parentId, <%=mainId%>, "<%=condFields%>");
		}
	}

	function showModule_<%=formCode%>(visitKey, id) {
		// addTab('<%=fd.getName()%>', '<%=request.getContextPath()%>/visual/moduleShowPage.do?visitKey=' + visitKey + '&parentId=' + id + '&id=' + id + '&code=<%=moduleCode%>');
		openWinModuleShow('<%=moduleCode%>', id, visitKey);
	}
</script>    
    <table id="cwsNestTable_<%=formCode%>" formCode="<%=formCode%>" class="tabStyle_1" style="width:100%;margin:0px" border="0" align="center" cellpadding="2" cellspacing="0">
      <thead>
      <tr ondblclick="<%=ondblclickScript%>" title="<%=ondblclickTitle%>" align="center" class="cwsThead">
		  <td class="td-chk" align="center" style="width: 35px; display: <%=isShowChk?"":"none"%>">
			  <input type="checkbox" onclick="selBatchInNestSheet('<%=formCode%>', '<%=nestFieldName%>', this)" />
		  </td>
        <td style="width:50px;display: <%=isNoShow==1?"":"none"%>">序号</td>
        <%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		// 跳过列表中添加的操作列
		if ("colOperate".equals(fieldName)) {
			continue;
		}

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
			else if ("cws_creator".equals(fieldName)) {
				title = "创建者";
			} else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
				title = "ID";
			} else if ("cws_status".equals(fieldName)) {
				title = "状态";
			} else if ("cws_flag".equals(fieldName)) {
				title = "冲抵状态";
			} else if ("flowId".equalsIgnoreCase(fieldName)) {
				title = "流程号";
			} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
				title = "流程开始时间";
			} else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
				title = "流程结束时间";
			} else if ("cws_id".equals(fieldName)) {
				title = "关联ID";
			}
			else if ("cws_visited".equals(fieldName)) {
				title = "是否已读";
			}
			else if ("colPrompt".equals(fieldName)) {
				title = "colPrompt"; //
			}
			else if ("cws_create_date".equals(fieldName)) {
				title = "创建时间";
			} else if ("cws_modify_date".equals(fieldName)) {
				title = "修改时间";
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
		  // 如果一项权限都没有，则不显示操作列，还是得显示，因为需要查看详情
		  boolean isOpShow = true;
		  if (canAdd || canEdit || canImport || canExport || (canSel && queryId!=-1) || (canSel && mapAry.length()>0)) {
			  isOpShow = true;
		  }
		  if (isOpShow) {
		%>
        <td class="tdOperate" style="width:150px">
        <%if (canAdd) {%>
			<a class="link-btn" title="增加" style="cursor:pointer" onclick="add_row_<%=formCode%>('<%=formCode%>')"><i class="fa fa-plus-circle link-icon link-icon-add"></i></a>
		<%}%>
        <%if (canImport) {%>
			<a class="link-btn" title="导入" style="cursor:pointer" onclick="importForSheet_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>)"><i class="fa fa-arrow-circle-o-down link-icon link-icon-edit"></i></a>
        <%}%>
		<%if (canSel && queryId!=-1) {%>
			<a class="link-btn" title="选择" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>, true)"><i class="fa fa-check-square-o link-icon link-icon-show"></i></a>
        <%}%>
        <%if (canSel/* && mapAry.length()>0*/) {%>
			<a class="link-btn" title="选择" style="cursor:pointer" onclick="sel_<%=formCode%>(<%=StrUtil.toHtml(cwsId)%>)"><i class="fa fa-check-square-o link-icon link-icon-show"></i></a>
        <%}%>
		<%if (canDel) {%>
			<a class="link-btn" title="批量删除" style="cursor:pointer" onclick="delBatch<%=formCode%>()"><i class="fa fa-trash-o link-icon link-icon-del"></i></a>
		<%}%>
        </td>
		  <%}%>
      </tr>
      </thead>
      <tbody>
      <%
		  // 自动拉单
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
			  WorkflowDb wf = new WorkflowDb();
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
			  <td class="td-chk" align="center" style="display: <%=isShowChk?"":"none"%>">
				  <input id="rowId" value="<%=fdao.getId()%>" class="row-chk" type="checkbox"/>
			  </td>
			  <td editable=0 title="ID：<%=fdao.getId()%>" style="display: <%=isNoShow==1?"":"none"%>">
				  <%=k%>
			  </td>
			  <%
				  for (int i=0; i<len; i++) {
					  String fieldName = fields[i];
					  // 跳过列表中添加的操作列
					  if ("colOperate".equals(fieldName)) {
						  continue;
					  }
			  %>
			  <td align="<%=fieldsAlign[i]%>">
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
					  else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
						  out.print(String.valueOf(fdao.getId()));
					  } else if ("cws_flag".equals(fieldName)) {
						  out.print(String.valueOf(fdao.getCwsFlag()));
					  } else if ("cws_creator".equals(fieldName)) {
						  out.print(StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName()));
					  } else if ("cws_status".equals(fieldName)) {
						  out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
					  } else if ("flowId".equalsIgnoreCase(fieldName)) {
						  out.print(String.valueOf(fdao.getFlowId()));
					  } else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
						  if (fdao.getFlowId() != -1) {
							  wf = wf.getWorkflowDb(fdao.getFlowId());
							  out.print(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss"));
						  }
					  } else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
						  if (fdao.getFlowId() != -1) {
							  wf = wf.getWorkflowDb(fdao.getFlowId());
							  out.print(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
						  }
					  } else if ("cws_id".equals(fieldName)) {
						  out.print(String.valueOf(fdao.getCwsId()));
					  }
					  else if ("cws_visited".equals(fieldName)) {
						  out.print(fdao.isCwsVisited()?"是":"否");
					  }
					  else if ("colPrompt".equals(fieldName)) {
						  continue;
					  } else if ("cws_create_date".equals(fieldName)) {
						  out.print(DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd HH:mm:ss"));
					  } else if ("cws_modify_date".equals(fieldName)) {
						  out.print(DateUtil.format(fdao.getCwsModifyDate(), "yyyy-MM-dd HH:mm:ss"));
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
				  <%
					  if (canEdit) {
				  %>
				  <a class="link-btn" title="修改" style="cursor:pointer" onclick="edit_row_<%=formCode%>(<%=id%>)"><i class="fa fa-edit link-icon link-icon-edit"></i></a>
				  <%
				  } else {
				  %>
				  <a class="link-btn" title="查看" style="cursor:pointer" onclick="showModule_<%=formCode%>('', <%=id%>)"><i class="fa fa-file-text-o link-icon link-icon-show"></i></a>
				  <%
					  }
					
					  if (canDel) {
				  %>
				  <a class="link-btn" title="删除" style="cursor:pointer" onclick="del_row_<%=formCode%>(<%=id%>)"><i class="fa fa-trash-o link-icon link-icon-del"></i></a>
				  <%
					  }
				  %>
			  </td>
			  <%}%>
		  </tr>
		  <%
			  }
		  }
	  %>
      </tbody>
</table>
<%
}

if (isPage == 1 && ("edit".equals(op) || "view".equals(op))) {
%>
<div class="paginator" id="paginator<%=nestFieldName%>"></div>
<%
	}
%>
<script>
	var aryAlign = new Array();
	<%
	int k = 0;
	for (String align : fieldsAlign) {
	%>
		aryAlign[<%=k%>] = '<%=align%>';
	<%
		k++;
	}
	%>

	// 记录添加的嵌套表格2记录的ID
	function addTempCwsId(formCode, cwsId) {
		console.log('addTempCwsId formCode', formCode);
		console.log('addTempCwsId cwsId', cwsId);
		var name = "<%=com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS%>_" + formCode;
		var inp;
		try {
			inp = document.createElement('<input type="hidden" name="' + name + '" />');
		} catch(e) {
			inp = document.createElement("input");
			inp.type = "hidden";
			inp.name = name;
		}
		inp.value = cwsId;

		spanTempCwsIds.appendChild(inp);
	}

// 新增后插入数据
// 或从module_list_nest_sel.jsp选择数据后调用
var idNum = 1000;
function insertRow_<%=moduleCode%>(formCode, fdaoId, tds, token, isPull, flowId) {
	var ary = tds.split(token);
	var cls = '';
	<%
	if (isAddHighlight == 1) {
	%>
	// 如果是来自于拉单
	if (isPull) {
		cls = 'row-pull';
	}
	else {
		cls = 'row-add';
	}
	<%
	}
	%>
	var trHTML = "<tr id='" + formCode + "_" + fdaoId + "' class='" + cls + "'>";

	// 插入复选框
	trHTML += "<td align='center' class='td-chk' style=\"display: <%=isShowChk?"":"none"%>\"><input id='rowId' type='checkbox' value='" + fdaoId + "' class='row-chk'></td>";

	// 插入序号列
	var trList = $("#cwsNestTable_" + formCode).find("tr");
    var trLen = trList.length;
	trHTML += "<td title='ID：" + fdaoId + "' align='center' style=\"display: <%=isNoShow==1?"":"none"%>\">" + trLen + "</td>";
	
	for (var i=0; i<ary.length; i++) {
		trHTML += "<td align='" + aryAlign[i] + "'>" + ary[i] + "</td>";
	}
	
	trHTML += "<td align='center'>";
	idNum++;
	<%if (canEdit) {%>
	trHTML += '<a class="link-btn" title="修改" style="cursor:pointer" onclick="edit_row_' + formCode + '(' + fdaoId + ')"><i class="fa fa-edit link-icon link-icon-edit"></i></a>';
	<%}
	else {
		%>
		trHTML += '<a class="link-btn" title="查看" style="cursor:pointer" onclick="showModule_<%=formCode%>(\'\', ' + fdaoId + ',' + flowId + ')"><i class="fa fa-file-text-o link-icon link-icon-show"></i></a>';
		<%
	}%>
	<%if (canDel) {%>
	trHTML += '<a class="link-btn" title="删除" style="cursor:pointer" onclick="del_row_' + formCode + '(' + fdaoId + ')"><i class="fa fa-trash-o link-icon link-icon-del"></i></a>';
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
	// 流程中、智能模块编辑时或者查看时，否则在智能模块添加时是没有主模块id号的
	<%if ("edit".equals(op) || "view".equals(op)) {%>
		// 20210521 注释掉，因为插入后似乎没必要reload，虽然刷新后，可以重新分页、排序
		// reloadNestSheetCtl<%=moduleCode%>();
	<%}%>

	eventTarget.fireEvent({
		type: EVENT_TYPE.NEST_ADD,
		moduleCode: "<%=moduleCode%>"
	});
	
	// 用于form_js_***.jsp调用
	try {
		onNestInsertRow('<%=moduleCode%>', formCode, fdaoId, tds, token, <%=StrUtil.toHtml(cwsId)%>);
	} catch (e) {
	}
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

<%
// 防止内资审核点开项目，在选项卡中看内资审核以往记录，查看详情时覆盖了流程处理页面上的投资方明细表中的同名方法
if ("edit".equals(op)) {
%>
// 带分页重新加载
function reloadNestSheetCtl<%=moduleCode%>() {
	loadNestCtl('<%=url%>', 'nestsheet_<%=nestFieldName%>', <%=curPage%>, <%=pageSize%>, getConds(), '<%=formName%>').then(() => {
		console.log('reloadNestSheetCtl fireEvent');
		eventTarget.fireEvent({
			type: EVENT_TYPE.NEST_EDIT,
			moduleCode: "<%=moduleCode%>"
		});
	});
}
<%
}
if (isPage == 1 && ("edit".equals(op) || "view".equals(op))) {
%>
// 初始化分页
var paging<%=nestFieldName%> = new MyPaging('#paginator<%=nestFieldName%>', {
	size: <%=pageSize%>,
	total: <%=lr.getTotal()%>,
	current: <%=curPage%>,
	prevHtml: '上一页',
	nextHtml: '下一页',
	layout: 'total, totalPage, sizes, prev, pager, next, jumper',
	jump: function() {
		var _this = this; // 模拟ajax获取数据
		_this.setTotal(<%=lr.getTotal()%>);

		// 如果当前分页不等于点击的页码，才可以loadNestCtl，否则会陷入循环
		if (_this.current != <%=curPage%> || _this.size != <%=pageSize%>) {
			var conds = getConds();
			loadNestCtl('<%=url%>', 'nestsheet_<%=nestFieldName%>', _this.current, _this.size, conds, '<%=formName%>');
		}
	}
});

$('.jumpBtn').on('click', function() {
	paging<%=nestFieldName%>.setCurrent(1);
});
<%
}
%>

	function getConds() {
		var conds = "";
		var fields = $('.search-box').find('input,select,textarea');
		$.each(fields, function (i, field) {
			if ('' == conds) {
				conds = field.name + '=' + encodeURI(field.value);
			} else {
				conds += "&" + field.name + '=' + encodeURI(field.value);
			}
		});
		if ('' != conds) {
			conds += "&action=search";
		}
		return conds;
	}

	function bindNestTableMouseEvent() {
		$("table[id^='cwsNestTable_'] td").mouseout(function () {
			if ($(this).parent().parent().get(0).tagName != "THEAD")
				$(this).parent().find("td").each(function (i) {
					$(this).removeClass("tdOver");
				});
		});

		$("table[id^='cwsNestTable_'] td").mouseover(function () {
			if ($(this).parent().parent().get(0).tagName != "THEAD")
				$(this).parent().find("td").each(function (i) {
					$(this).addClass("tdOver");
				});
		});
	}

	bindNestTableMouseEvent();

	// 注意如果在此初始化event，则主表单中有N个嵌套表格2，就会被初始化N个对应的事件
	// 所以应放在form_js中
	/*function onNestChange(event) {
		console.log(event);
	}

	$(function() {
		eventTarget.addEvent(EVENT_TYPE.NEST_ADD, onNestChange);
		eventTarget.addEvent(EVENT_TYPE.NEST_EDIT, onNestChange);
		eventTarget.addEvent(EVENT_TYPE.NEST_DEL, onNestChange);
	});*/
</script>