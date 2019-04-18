<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<TITLE>选择用户</TITLE>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <script>
  function setUserRoles() {
	window.returnValue = getTargets();
  	window.close();
  }

  function initUserRoles() {
  	setTargets();
  }

function setTargets() {
   var depts = dialogArguments.getUserRoles();
   var ary = depts.split(",");
   for(var i=0; i<form1.elements.length; i++) {
   		if (form1.elements[i].type=="checkbox"){
			for (var j=0; j<ary.length; j++) {
				if (form1.elements[i].name==ary[j]) {
					form1.elements[i].checked = true;
					break;
				}
			}
   		}
   }
	
}

function getTargets(){
   var ary = new Array();
   var j = 0;
   for(var i=0; i<form1.elements.length; i++) {
   		if (form1.elements[i].type=="checkbox"){
			if (form1.elements[i].checked) {
				ary[j] = new Array();
				ary[j][0] = form1.elements[i].name;
				ary[j][1] = form1.elements[i].value;
				j ++;
			}
   		}
   }
   return ary;
}
</script>
</HEAD>
<BODY bgColor=#FBFAF0 leftMargin=4 topMargin=8 rightMargin=0 class=menubar onLoad="initUserRoles()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="460" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
  <thead>
  <tr> 
    <td height="24" colspan="2" align="center"><span>用户角色</span></td>
  </tr>
  </thead>
  <tr> 
    <td height="87" colspan="2" valign="top">
  <%
RoleDb urole = new RoleDb();
Vector result;
String unitCode = privilege.getUserUnitCode(request);
if (unitCode.equals(DeptDb.ROOTCODE))
	result = urole.list();
else
	result = urole.getRolesOfUnit(unitCode);

Iterator ir = result.iterator();
%>
      <br>
      <table width="95%" align="center" class="tabStyle_1_sub">
        <form name="form1">
          <tbody>
  <%
while (ir.hasNext()) {
 	RoleDb ug = (RoleDb)ir.next();
%>
            <tr>
              <td width="31%">
              <input type="checkbox" name="<%=ug.getCode()%>" value="<%=ug.getDesc()%>">&nbsp;<%=ug.getDesc()%>			</td>
            </tr>
  <%}%>
          </tbody>
        </form>
      </table>	</td>
  </tr>
  <tr align="center">
    <td height="28" colspan="2">
<input class="btn" type="button" name="okbtn" value="确定" onClick="setUserRoles()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
      <input class="btn" type="button" name="cancelbtn" value="取消" onClick="window.close()">    </td>
  </tr>
</table>
</BODY></HTML>
