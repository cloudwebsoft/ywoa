<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享目录管理-bottom</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css" />
<script language=JavaScript src='<%=Global.getRootPath(request) %>/netdisk/showDialog/jquery.min.js'></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
	function form1_onsubmit() {
		form1.root_code.value = window.parent.dirmainFrame.getRootCode();
	}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
		
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String op = ParamUtil.get(request, "op");

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "parent_code", parent_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "parent_name", parent_name, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

boolean isHome = false;
String mappingAddress = "";
int type = 0;
if (op.equals("")) {
	op = "AddChild";
}

if (op.equals("AddChild")) {
	PublicLeafPriv lp = new PublicLeafPriv(parent_code);
	if (!lp.canUserAppend(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}else{
		PublicLeaf plParent = new PublicLeaf(parent_code);
		parent_name = plParent.getName();
		
	}	
}

PublicLeaf leaf = null;
if (op.equals("modify")) {
	PublicLeafPriv lp = new PublicLeafPriv(code);
	if (!lp.canUserManage(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}	
	PublicDirectory dir = new PublicDirectory();
	leaf = dir.getLeaf(code);
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	isHome = leaf.getIsHome();
	mappingAddress = leaf.getMappingAddress();
}
%>
<script>	
  $(function(){
	var op = '<%=op%>';
	if(op == 'AddChild'){
		$(".dirTable .curDir td:eq(0)").attr("rowspan",4)
	}else{
		$(".dirTable .curDir td:eq(0)").attr("rowspan",5);
	}

  })
</script>
<form action="netdisk_public_dir_top.jsp?op=<%=op%>" method="post" name="form1" target="dirmainFrame" id="form1" onsubmit="return form1_onsubmit()">
<table  width="80%" class="dirTable" align="center">
  <tr>
    <th colspan="2">目录添加或修改</th>
   
  </tr>
  <tr class="curDir">
    <td rowspan="3">
    	<div>当前节点</div>
        <div class="desc"><%=parent_name.equals("")?"根节点":parent_name%></div>
    </td>
  </tr>
  <tr>
    <td>
    	<span class="colTitle">名称</span>
    	 <input   name="code"  type="hidden" value="<%=code%>"/>
        <input class="colInput"   name="name"  type="input" value="<%=name%>"/>
    </td>
  </tr>
  <tr>
    <td>
    	<span class="colTitle">描述</span>
        <input name="description" value="<%=StrUtil.getNullStr(description)%>"   class="colInput" type="input"/>
        <input type="hidden" name="parent_code" value="<%=parent_code%>" />  
    </td>
  </tr>
  <tr>
    <td>
    	<span class="colTitle">映射地址</span>
        <input class="colInput2" type="input" name="mappingAddress" value="<%=StrUtil.getNullStr(mappingAddress)%>" />
    </td>
  </tr>
	<%if (op.equals("modify")) {%>
        <script>var bcode = "<%=leaf.getCode()%>";</script>
    <%if (op.equals("modify") && !leaf.getCode().equals(PublicLeaf.ROOTCODE)) {%>
	     <tr>
		    <td>
		    	<span class="colTitle">父节点</span>
		    	
		        <!--   <select name="parentCode" class="colSel">
          <--%
				PublicLeaf rootlf = leaf.getLeaf("root");
				PublicDirectoryView dv = new PublicDirectoryView(rootlf);
				dv.ShowDirectoryAsOptionsWithCode(request, out, rootlf, rootlf.getLayer());
			%>
        		</select>-->
        		<span class="colTitle"><%=new PublicLeaf(leaf.getParentCode()).getName()%></span>
        		<input type="hidden" name="parentCode" value="<%=leaf.getParentCode()%>" />
        	</td>
        </tr>
        <script>
			//form1.parentCode.value = "<%=leaf.getParentCode()%>";
		</script>
     <%}else{%>
     	<tr>
    		<td>
      	 		<input type="hidden" name="parentCode" value="<%=leaf.getParentCode()%>" />
    		</td>
       	 </tr>
      <%}%> 
    <%}%>
 
  <tr>
    <td colspan="2" class="colBtn">
    	<input type="submit" value="提交" class="sub"/>
        <input type="reset" value="重置"  class="res"/>
       	<input name="root_code" type="hidden" /> 
    </td>
  </tr>
</table>
</form>

</body>
</html>
