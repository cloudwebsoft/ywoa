<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>

<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />

<link type="text/css" rel="stylesheet" href="../js/appendGrid/jquery.appendGrid-1.5.1.css" />
<script type="text/javascript" src="../js/appendGrid/jquery.appendGrid-1.5.1.js"></script>

<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>

</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("add")) {
	try {
		if (privmgr.add(request)) {
			out.print(StrUtil.jAlert_Redirect("添加成功！", "提示", "priv_m.jsp"));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！", "提示"));		
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	return;
}
else if (op.equals("del")) {
	try {
		if (privmgr.del(request)) {
			out.print(StrUtil.jAlert_Redirect("删除成功！", "提示", "priv_m.jsp"));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！", "提示"));		
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	return;
}
/*
else if (op.equals("moveLeft")) {
	String priv = ParamUtil.get(request, "priv");
	PrivDb pd = new PrivDb();
	pd = pd.getPrivDb(priv);
	if (pd.getLayer()==2) {
		pd.setLayer(1);
		pd.save();
	}
	return;
}
else if (op.equals("moveRight")) {
	String priv = ParamUtil.get(request, "priv");
	PrivDb pd = new PrivDb();
	pd = pd.getPrivDb(priv);
	if (pd.getLayer()==1) {
		pd.setLayer(2);
		pd.save();
	}
	return;
}
*/
else if (op.equals("edit")) {
   	String rowOrder = ParamUtil.get(request, "tblPrivs_rowOrder");
   	String[] uniqueIndexes = null;
   	if (rowOrder==null) {
   		uniqueIndexes = new String[0];
   	}
   	else {
   		uniqueIndexes = rowOrder.split(",");
   	}

	boolean re = false;
	
	// 找出被删除的项
	StringBuffer privsToDel = new StringBuffer();
	
	Vector privToDelV = new Vector();
	String oldPrivs = ParamUtil.get(request, "oldPrivs");
	String[] ary = StrUtil.split(oldPrivs, ",");
	for (int k=0; k<ary.length; k++) {
		String oldPriv = ary[k];
		boolean isFound = false;
		for (int i = 0; i < uniqueIndexes.length; i++) {
	    	String priv = ParamUtil.get(request, "tblPrivs_priv_" + uniqueIndexes[i]);
			if (oldPriv.equals(priv)) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			privToDelV.addElement(oldPriv);
    		StrUtil.concat(privsToDel, ",", StrUtil.sqlstr(oldPriv));			
		}
	}	
		
	for (int i = 0; i < uniqueIndexes.length; i++) {
	  	String desc = ParamUtil.get(request, "tblPrivs_desc_" + uniqueIndexes[i]);
	   	String desc2 = ParamUtil.get(request, "tblPrivs_desc2_" + uniqueIndexes[i]);
	   	if (!"".equals(desc) && !"".equals(desc2)) {
	   		out.print(StrUtil.jAlert_Back(desc + " 只能填写一级或二级，不能都填写！", "提示"));
	   		return;
	   	}
	   	String priv = ParamUtil.get(request, "tblPrivs_priv_" + uniqueIndexes[i]);
	   	int layer = 1;
		if ("".equals(desc)) {
			desc = desc2;
			layer = 2;
		}

		PrivDb pd = new PrivDb();
		pd = pd.getPrivDb(priv);
		pd.setDesc(desc);
		pd.setOrders(i + 1);
		pd.setLayer(layer);
		pd.save();
	}
    	
	if (re) {
		PrivCache rc = new PrivCache(new PrivDb());
        rc.refreshList();
		if (privsToDel.length()>0) {
			Iterator irToDel = privToDelV.iterator();
			while (irToDel.hasNext()) {
				String privDel = (String)irToDel.next();
				PrivDb pd = new PrivDb();
				pd = pd.getPrivDb(privDel);
				if (pd.isLoaded()) {
					pd.del();
				}
			}
		}	
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "priv_m.jsp"));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
%>
<%@ include file="priv_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
PrivDb pd = new PrivDb();
String sql = pd.getSqlList();
// out.print(sql);
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
String priv;
int isSystem = 0;

StringBuffer oldPrivs = new StringBuffer();
while (ri.hasNext()) {
 	ResultRecord rr = (ResultRecord)ri.next();
	priv = rr.getString(1);
	StrUtil.concat(oldPrivs, ",", priv);
}
%>
<form id=form1 name=form1 action="priv_m.jsp?op=edit" method=post>
<table id="tblPrivs" align="center">
</table>
<input name="oldPrivs" value="<%=oldPrivs %>" type="hidden" />
</form>
<div style="text-align:center; margin:10px">
<input type="button" class="btn" id="btnSubmit" value="确定" />
</div>
</body>
<script>
$(function () {
    // Initialize appendGrid
    $('#tblPrivs').appendGrid({
        // caption: '',
        i18n: {
            append: '添加新行',
            rowDrag: '拖动',
            removeLast: '删除最后一行',
            insert: '添加一行在上方',
            moveUp: '上移',
            moveDown: '下移',
            remove: '删除'
        },        
        initRows: 0,
        rowDragging: true,      
        hideButtons: {
            append: true,
        	// remove: true,
        	removeLast: true,        	
            insert: true
        },      
        columns: [
 			//  宽度隐藏比较字段操作
            { name: 'desc', display: '类别', type: 'text', ctrlAttr: { maxlength: 100 }, ctrlCss: { width: '150px', border:'0'} },
            { name: 'desc2', display: '名称', type: 'text', ctrlAttr: { maxlength: 100 }, ctrlCss: { width: '200px', border:'0'} },
            { name: 'privX', display: '编码', type: 'text', ctrlAttr: { maxlength: 40, readonly: 'true', disabled:true }, ctrlCss: { width: '200px', border:'0'} },
            { name: 'priv', display: '编码', type: 'hidden', ctrlAttr: { maxlength: 100 }, ctrlCss: { width: '30px'} },            
            { name: 'isSystem', display: '系统保留', type: 'text', ctrlAttr: { maxlength: 100, readonly: 'true', disabled:true }, ctrlCss: { width: '60px', border:'0'} }
        ],
        customRowButtons: [
                { uiButton: { icons: { primary: 'ui-icon-arrowthick-1-w' }, text: false }, click: moveLeft, btnCss: { 'max-width': '20px' }, btnAttr: { title: '左移' }, atTheFront: true },
            	{ uiButton: { icons: { primary: 'ui-icon-arrowthick-1-e' }, text: false }, click: moveRight, btnCss: { 'max-width': '20px' }, btnAttr: { title: '右移' }, atTheFront: true },
            	{ uiButton: { icons: { primary: 'ui-icon-newwin' }, text: false }, click: privView, btnCss: { 'max-width': '20px' }, btnAttr: { title: '查看' } }
            ]   
    });
    
	function moveLeft(evtObj, uniqueIndex, rowData) {
		var rowIndex = $('#tblPrivs').appendGrid('getRowIndex', uniqueIndex);	
		if (rowData.desc=="") {			
			$('#tblPrivs').appendGrid('setCtrlValue', 'desc', rowIndex, rowData.desc2);
			$('#tblPrivs').appendGrid('setCtrlValue', 'desc2', rowIndex, "");
		}
		else {
			jAlert("不能向左移动", "提示");
		}
	}

	function moveRight(evtObj, uniqueIndex, rowData) {
		var rowIndex = $('#tblPrivs').appendGrid('getRowIndex', uniqueIndex);
		if (rowData.desc2=="") {			
			$('#tblPrivs').appendGrid('setCtrlValue', 'desc2', rowIndex, rowData.desc);
			$('#tblPrivs').appendGrid('setCtrlValue', 'desc', rowIndex, "");
		}
		else {
			jAlert("不能向右移动", "提示");
		}		
	}
	
	function privView(evtObj, uniqueIndex, rowData) {
		window.location.href = "priv_show.jsp?priv=" + rowData.priv;
	}   
<%  
ri.beforeFirst();
JSONArray arr = new JSONArray();
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord) ri.next();
	int layer = rr.getInt(4);
	String desc = rr.getString("description");
	String desc2 = "";
	if (layer==2) {
		desc2 = desc;
		desc = "";
	}
	
	JSONObject json = new JSONObject();
	json.put("desc", desc);	
	json.put("desc2", desc2);
	json.put("privX", rr.getString("priv"));
	json.put("priv", rr.getString("priv"));
	json.put("isSystem", rr.getInt("isSystem")==1?"是":"否");
	arr.put(json);
}
%>	    

    // Handle `Load` button click
    $(function () {
        $('#tblPrivs').appendGrid('load', <%=arr%>);
    });
    // Handle `Serialize` button click
    $('#btnSubmit').button().click(function () {
        $(document.forms[0]).serialize();
        $(document.forms[0]).submit();
    });
});

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>