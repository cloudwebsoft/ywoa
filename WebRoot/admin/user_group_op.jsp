<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.db.SequenceManager" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理用户组-添加/修改</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
UserGroupDb ug = null;
boolean isEdit = false;
if (op.equals("edit")) {
	isEdit = true;
	String code = ParamUtil.get(request, "code");
	if (code.equals("")) {
		StrUtil.Alert_Back("编码不能为空！");
		return;
	}
	ug = new UserGroupDb(code);
}
else if (op.equals("editdo")) {
	isEdit = true;
	String code = ParamUtil.get(request, "code");
	
	UserGroupMgr usergroupmgr = new UserGroupMgr();
	try {
		if (usergroupmgr.update(request))
			out.print(StrUtil.Alert_Redirect("修改成功！", "user_group_op.jsp?op=edit&code=" + StrUtil.UrlEncode(code)));
		else {
			out.print(StrUtil.Alert_Back("修改失败！"));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}	
	if (code.equals("")) {
		StrUtil.Alert_Back("编码不能为空！");
		return;
	}
	ug = usergroupmgr.getUserGroupDb(code);
}
%>
<%if (op.equals("edit")) {%>
<%@ include file="user_group_op_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<%} else {%>
<%@ include file="user_group_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%} %>
<div class="spacerH"></div>
<form action="<%=isEdit?"user_group_op.jsp?op=editdo":"user_group_m.jsp?op=add"%>" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
  <table class="tabStyle_1 percent80" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" height="31" colspan="2" align="center">
        <%if (isEdit) {%>
        修改用户组
        <%}else{%>
        添加用户组
        <%}%>      </td>
    </tr>
    <tr style="display:none">
      <td width="105" height="31" align="center">编码</td>
      <td align="left">
      <%
      	String code = isEdit?ug.getCode():String.valueOf(SequenceManager.nextID(SequenceManager.OA_USER_GROUP));
      %>
	  <input type="hidden" id="code" name="code" value="<%=code%>" />
	  <%=code%></td>
    </tr>
    <tr>
      <td height="32" align="center">名称</td>
      <td align="left"><input name="desc" value="<%=isEdit?ug.getDesc():""%>" />
      <font color="red">*</font></td>
    </tr>    
    <tr>
      <td height="31" align="center">部门</td>
      <td align="left">
	  <select id="deptCode" name="deptCode" onchange="form1.isDept.value='1'; if (this.value=='<%=DeptDb.ROOTCODE%>') {o('isIncludeSubDept').checked=true;}" <%=isEdit?(ug.isDept()?"":"disabled"):""%>>
        <%
		String unitCode;
		if (privilege.isUserPrivValid(request, "admin")) {
			unitCode = DeptDb.ROOTCODE;
		}
		else {
			unitCode = privilege.getUserUnitCode(request);
		}	
		
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(unitCode);
		DeptView dv = new DeptView(dd);
		dv.ShowDeptAsOptions(out, dd, 1);
		%>
      </select>
	  <%if (isEdit && ug.isDept()) {%>
	  <script>
	  o("deptCode").value = "<%=ug.getDeptCode()%>";
	  </script>
	  <%}%>
	  </td>
    </tr>
    <tr>
      <td height="32" align="center">是否为部门组</td>
      <td align="left">
	  <select id="isDept" name="isDept" onchange="if (this.value=='0') {o('isIncludeSubDept').checked=false;o('deptCode').disabled=true;} else {o('isIncludeSubDept').disabled=false;o('deptCode').disabled=false;}">
        <option value="1">是</option>
        <option value="0" selected="selected">否</option>
      </select>
	  <%if (isEdit && ug.isDept()) {%>
	  <script>
	  o("isDept").value = "<%=ug.isDept()?1:0%>";
	  </script>
	  <%}%>	  
	  </td>
    </tr>
    <tr>
      <td height="32" align="center">含子部门</td>
      <td align="left"><input type="checkbox" id="isIncludeSubDept" name="isIncludeSubDept" value="1" checked <%=isEdit?(ug.isDept()?"":"disabled"):""%> />
	  <%if (isEdit) {%>
	  <script>
	  <%if (ug.isIncludeSubDept()) {%>
	  o("isIncludeSubDept").checked = true;
	  <%}else{%>
	  o("isIncludeSubDept").checked = false;
	  <%}%>
	  </script>
	  <%}%>
	  </td>
    </tr>
    <tr>
      <td height="32" align="center">单位</td>
      <td align="left"><select name="unitCode" <%=isEdit?"disabled":""%>>
          <%
		  Iterator ir = privilege.getUserAdminUnits(request).iterator();
		  while (ir.hasNext()) {
		  	dd = (DeptDb)ir.next();
		  %>
          <option value="<%=dd.getCode()%>"><%=dd.getName()%></option>
          <%
		  }
		  %>
        </select>
		<%if (isEdit) {%>
          <script>
		  form1.unitCode.value = "<%=ug.getUnitCode()%>";
		  </script>
		<%}%>
      </td>
    </tr>    
    <tr>
      <td height="43" colspan="2" align="center">
      	<input name="Submit" type="submit" class="btn" value="确定" />
        <%if (op.equals("")) {%>
        &nbsp;&nbsp;&nbsp;
        <input name="Submit" type="button" onclick="window.location.href='user_group_m.jsp'" class="btn" value="返回" />
        <%}%>
        </td>
    </tr>
  </table>
</form>
</body>
<script>
function form1_onsubmit() {
}
</script>
</html>