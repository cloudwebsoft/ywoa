<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectView"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "officeequip")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
Date d = new Date();
String dt = DateUtil.format(d, "yyyy-MM-dd");

//String strTypeId = ParamUtil.get(request, "typeId");
String strEquipId =ParamUtil.get(request, "officeName");
String officeName = ParamUtil.get(request, "officeName");
if ("".equals(officeName)) {
	officeName = "office_equipment";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用品领用</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<script language="JavaScript" type="text/JavaScript">
function setPerson(deptCode, deptName, user, userRealName) {
	form1.person.value = user;
	form1.person_real.value = userRealName;
}

function selectEquip() {
	$.ajax({
		url: "officeequip_do.jsp?op=equip_total&officeName=" + $('#officeName').val(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#store_count').html(data.msg);
				$('#tip_info').html("");
			} else {
				$('#store_count').html("");
				$('#tip_info').html(data.msg);
			}
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}
</script>
</head>
<body>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("chageStorageCount")) {
	OfficeMgr om = new OfficeMgr();
	OfficeOpMgr oo = new OfficeOpMgr();
	boolean re = false;
	boolean fe = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		  re = om.chageStorageCount(request);
		  fe = oo.create(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (fe && re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "officeequip_receive.jsp"));
}
 %>
<script>
o("menu6").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
  <form action="?op=chageStorageCount" name="form1" id="form1" method="post">
    <tr>
      <td class="tabStyle_1_title" colspan="2">办公用品借用登记</td>
    </tr>

    <tr>
      <td width=20%  align="center">用品名称：</td>
      <input name=type type="hidden" value="<%=1%>">
      <td align="left"><%
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
        <select name="officeName" id="officeName" onchange="selectEquip()">
        	<%=sb%>
        </select><font color=red>  *  </font>&nbsp;&nbsp;&nbsp;&nbsp;<b id="tip_info" style="color: red;width: 150px"></b>
	   	<script>
   		o("officeName").value = "<%=officeName%>";
	   	</script>              
        </td>
    </tr>
    <tr>
      <td align="center">库存数量：</td>
      <td id="store_count" align="left"></td>
    </tr>
    <tr>
      <td align="center"><script>
	  </script>
        借用数量：</td>
      <td align="left">
        <input name="storageCount" type="text" id="storageCount" size="20"><font color=red>  *  </font></td>
    </tr>
    <tr>
      <td align="center">借&nbsp;&nbsp;用&nbsp;&nbsp;人：</td>
      <td align="left">
      <%if (privilege.isUserPrivValid(request, "officeequip")) {%>
      <input name="person" type="hidden" id="person">
      <input id="person_real" name="person_real" type="text" size="20" readOnly>
      <font color=red>  *  </font>
        <a href="#" onClick="javascript:showModalDialog('../user_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a>
      <%} else { 
      UserDb ud = new UserDb(privilege.getUser(request));
      %>
      <input name="person" type="hidden" id="person" value="<%=ud.getName() %>">
      <input id="person_real" name="person_real" type="text" size="20" value="<%=ud.getRealName() %>" readOnly>
      <%} %>
	  </td>
    </tr>
    <tr>
      <td align="center">借用时间：</td>
      <td align="left"><input id="opDate" name="opDate" type="text" value="<%=dt%>" size="20">
        </td>
    </tr>
    <tr>
      <td align="center">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</td>
      <td align="left"><textarea  name="abstracts"  id="abstracts" style="width:90%" rows="5"></textarea></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input name="submit" type="button" class="btn"  value="确定" onclick="mysubmit()" > 
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
     <input type="button" class="btn"  value="返回" onclick="window.location.href='officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_BORROW%>'" ></td>
    </tr>
  </form>
</table>
<script>
$(function(){
	$('#opDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
	
	selectEquip();
})
function mysubmit(){
	$.ajax({
		url: "officeequip_do.jsp?op=op_add&type=<%=OfficeOpDb.TYPE_BORROW%>&" + $('#form1').serialize(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#officeName').val("office_equipment");
				$('#store_count').html("");
				$('#storageCount').val("");
				$('#person').val("");
				$('#person_real').val("");
				$('#abstracts').val("");
				$('#opDate').val("<%=dt %>");
				jAlert_Redirect(data.msg, "提示", "officeequip_return_list.jsp?opType=<%=OfficeOpDb.TYPE_BORROW%>");
			}
			else {
				jAlert(data.msg, "提示");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}
</script>
</body>
</html>
