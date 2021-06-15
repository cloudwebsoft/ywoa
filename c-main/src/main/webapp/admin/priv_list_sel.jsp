<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>权限选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/
%>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<%
License license = License.getInstance();
String sql = "select priv,description,isSystem from privilege order by isSystem desc, priv asc";
if (license.isGov()) {
	sql = "select priv,description,isSystem from privilege where kind=" + PrivDb.KIND_DEFAULT + " or kind=" + PrivDb.KIND_GOV + " order by isSystem desc, priv asc";
}
else {
	sql = "select priv,description,isSystem from privilege where kind=" + PrivDb.KIND_DEFAULT + " order by isSystem desc, priv asc";
}
// out.print(sql);
JdbcTemplate rmconn = new JdbcTemplate();
ResultIterator ri = rmconn.executeQuery(sql);
ResultRecord rr = null;
String priv;
String desc;
int isSystem = 0;
%>
<table width="98%" align="center" class="percent98"><tr><td align="left"><input class="btn" type="button" value="选择" onclick="sel()" /></td></tr></table>
<table id="mainTable" class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td width="6%" align="center" noWrap class="tabStyle_1_title">
        <input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('code'); else deSelAllCheckBox('code')" />
      </td>
      <td class="tabStyle_1_title" noWrap width="44%">编码</td>
      <td class="tabStyle_1_title" noWrap width="50%">描述</td>
    </tr>
<%
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next();
	priv = rr.getString(1);
	desc = rr.getString(2);
	isSystem = rr.getInt(3);
	%>
    <tr class="highlight">
      <td align="center"><input type="checkbox" id="code" name="code" value="<%=priv%>" desc="<%=desc%>" /></td>
      <td><%=priv%></td>
      <td><%=desc%></td>
    </tr>
<%}%>
  </tbody>
</table>
<br />
</body>
<script>
function getCheckboxDesc(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	var CheckboxValue = '';
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked) {
				return checkboxboxs.getAttribute("desc");
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].type=="checkbox" && checkboxboxs[i].checked)
			{
				if (CheckboxValue==''){
					CheckboxValue += checkboxboxs[i].getAttribute("desc");
				}
				else{
					CheckboxValue += ","+ checkboxboxs[i].getAttribute("desc");
				}
			}
		}
	}
	return CheckboxValue;
}

function sel() {
	var code = getCheckboxValue("code");
  	var desc = getCheckboxDesc("code");
  	var dlg = window.opener ? window.opener : dialogArguments;
	dlg.setIntpuObjValue(code, desc);
	window.close();
}

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

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

</script>
</html>