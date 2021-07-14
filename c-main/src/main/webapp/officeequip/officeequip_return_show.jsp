<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<title>办公用品归还</title>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, "officeequip")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int id = ParamUtil.getInt(request, "id");
OfficeOpDb ood = new OfficeOpDb();
ood = ood.getOfficeOpDb(id);
String officeCode = ood.getOfficeCode();
TreeSelectDb tsd = new TreeSelectDb(officeCode);
UserDb ud = new UserDb(ood.getPerson());
%>
<%      
OfficeOpMgr oom = new OfficeOpMgr();
OfficeMgr om = new OfficeMgr();
String op = ParamUtil.get(request, "op");
boolean re = false;
boolean fe = false;
if (op.equals("return")) {
	try {
		re = oom.returnOfficeEquip(request);
		fe = om.returnChageStorageCount(request);	
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (fe) {
%>			
<script>
window.close();
window.opener.location.reload();
</script>
<%	
	}
}
%>	
<table class="tabStyle_1 percent60">
<form method="post" action="?op=return" name="form1" id="form1">
  <tbody>
    <tr>
      <td colspan="2" align="center" nowrap="nowrap" class="tabStyle_1_title">办公用品归还登记</td>
      </tr>
    <tr>
      <td width="20%"align="center"  nowrap="nowrap">用品名称：</td>
      <td nowrap="nowrap"><%=tsd.getName()%><input id='id' name="id" value="<%=ood.getId()%>" type=hidden></td>
    </tr>
    <tr>
      <td align="center" nowrap="nowrap">数&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;量：</td>
      <td nowrap="nowrap"><%=ood.getCount()%><input id='officeName' name="officeName" value="<%=officeCode%>" type=hidden>
        <input id='count' name="count" value="<%=ood.getCount()%>" type="hidden" /></td>
    </tr>
    <tr>
      <td align="center" nowrap="nowrap">借用时间：</td>
      <td nowrap="nowrap"><%=ood.getOpDate()%></td>
    </tr>
    <tr>
      <td align="center" nowrap="nowrap">借&nbsp;&nbsp;用&nbsp;&nbsp;人：</td>
      <td nowrap="nowrap"><%=ud.getRealName()%></td>
    </tr>
    <tr>
      <td align="center" nowrap="nowrap">归还时间：</td>
      <td nowrap="nowrap"><%
	  Date d = new Date();
	  String dt = DateUtil.format(d, "yyyy-MM-dd");
     %><input name="endDate" type="text" id="endDate" size="20" value = "<%=dt%>"/>
	</td>
    </tr>
    <tr>
      <td align="center">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</td>
      <td><textarea  name="abstracts"  id="abstracts" style="width:98%" rows="5"><%=ood.getRemark()%></textarea></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input name="submit" type="button" class="btn"  value="确定" onclick="mysubmit()" >
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
     <input type="reset" class="btn"  value="重置" ></td>
      </tr>
  </tbody>
</form>
</table>
<script>
$(function(){
	$('#endDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });
})
function mysubmit(){
	$.ajax({
		url: "officeequip_do.jsp?op=op_ret&type=<%=OfficeOpDb.TYPE_RETURN%>&" + $('#form1').serialize(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			//jAlert(data.msg, "提示");
			if(data.ret == 1){
				location.href = "officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_BORROW%>";
			}
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}
</script>
</body>
</html>
