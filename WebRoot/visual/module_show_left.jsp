<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String code = ParamUtil.get(request, "code");
if ("".equals(code)) {
	code = ParamUtil.get(request, "formCode");
}
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserView(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = msd.getString("form_code");
if (formCode.equals("")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

int id = ParamUtil.getInt(request, "id");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>列表</title>
<meta http-equiv="X-UA-Compatible" content="IE=Edge;chrome=IE8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link href="../lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet"/>
<link href="../lte/css/plugins/treeview/bootstrap-treeview.css" rel="stylesheet"/>
<script src="../inc/common.js"></script>
<script src="../lte/js/jquery.min.js?v=2.1.4"></script>
<script src="../lte/js/bootstrap.min.js?v=3.3.6"></script>
<script src="../lte/js/plugins/treeview/bootstrap-treeview.js"></script>    
</head>
<body>
<%
String viewList = msd.getString("view_list");
String fieldTreeList = msd.getString("field_tree_list");
FormField ff = fd.getFormField(fieldTreeList);
if (ff==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "字段" + fieldTreeList + "不存在！"));
	return;
}

String fieldName = ff.getName();

String basicCode = ff.getDefaultValueRaw();
TreeSelectDb tsd = new TreeSelectDb();
tsd = tsd.getTreeSelectDb(basicCode);

JSONObject json = new JSONObject();
TreeSelectView tsv = new TreeSelectView(tsd);
tsv.getBootstrapJson(request, tsd, json);
%>
<div style="width:90%; margin:20px auto">
<div id="treeView" class="test"></div>
</div>
</body>
<script>
var data = <%=json.getJSONArray("nodes")%>;
$(function() {
    $('#treeView').treeview({
        // color: "#428bca",
        // enableLinks: true,
        data: data,
        onNodeSelected: function (event, node) {
        	// console.log(node);
            // window.parent.frm.mainModuleFrame.location.href = "../" + node.link;
            if (node.href!="") {
            	var link = "../" + node.href;
            	if (link.indexOf("?")==-1) {
            		link += "?";
            	}
            	else {
            		link += "&";
            	}
            	link += "<%=fieldName%>=" + encodeURI(node.code);
            	window.parent.document.getElementById("mainModuleFrame").contentWindow.location.href = link;
            }
        }        
    }); 
});
</script>
</html>
