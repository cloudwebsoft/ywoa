<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectDb"%>
<%@ page import = "com.redmoon.oa.basic.TreeSelectView"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="officeequip";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String strTypeId = ParamUtil.get(request, "typeId");
String strEquipId =ParamUtil.get(request, "equipId");
String equipId1 = "";
String officeN = "";
String officeName = ParamUtil.get(request, "officeName");
if ("".equals(officeName)) {
	officeName = "office_equipment";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>办公用品入库登记</title>
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
<script>
function selectEquip() {
	$.ajax({
		url: "officeequip_do.jsp?op=equip_check&officeName=" + $('#officeName').val(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#tip_info').html("");
				if (data.isFind) {
					$('#measureUnit').val(data.unit);
					$('#price').val(data.price);
					$('#buyPerson').val(data.buyPerson);
				}
			} else {
				$('#tip_info').html(data.msg);
			}
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}
</script>
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
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%@ include file="officeequip_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<table align="center" class="tabStyle_1 percent60">
<form action="officeequip_do.jsp" name="form1" id="form1" method="post">
<tr>
  <td colspan="4" class="tabStyle_1_title">办公用品入库登记</td>
</tr>
<tr>
 <td align="center" width=20%>用品名称：</td>
 <td><%
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
   <select name="officeName" id="officeName" onChange="selectEquip()">
     <%=sb%>
   </select>
   <script>
   o("officeName").value = "<%=officeName%>";
   </script>
   <font color=red>  *  </font>&nbsp;&nbsp;&nbsp;&nbsp;<b id="tip_info" style="color: red;width: 150px"><%=tsd.isLoaded() ? "" : "请先添加办公用品！" %></b>
   </td>    	  
</tr>
<tr>
  <td align="center">计量单位：</td>
  <td><font color=red>

  <input name="measureUnit" type="text" id="measureUnit" value="" size="10" maxlength="100">
  *  </font></td>
  </tr>
<tr>
  <td align="center">用品数量：</td>
  <td><font color=red>
    <input name="storageCount" type="text" id="storageCount"value="" size="10" maxlength="100">
  *</font></td>
</tr>
<tr>
  <td align="center">价格&nbsp;&nbsp;(￥)：</td>
  <td><input name="price" type="text" id="price" value="" size="10" maxlength="100">
    <font color=red>*</font></td>
    </tr>
<tr>
 <td align="center">供&nbsp;&nbsp;应&nbsp;&nbsp;商：</td>
  <td><input name="buyPerson" type="text" id="buyPerson"value="" size="20" maxlength="100"></td>
</tr>
<tr>
  <td align="center">购置时间：</td>
  <td>
  <%
   Date d = new Date();
   String dt = DateUtil.format(d, "yyyy-MM-dd");
  %>
    <input name="buyDate" type="text" id="buyDate" value="<%=dt%>" size="10" maxlength="100">
        </td>
        </tr>
<tr style="display:none">
 <td align="center">单&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;位：</td>
  <td>
        <select id="unitCode" name="unitCode">
        <%
        Iterator ir = privilege.getUserAdminUnits(request).iterator();
        if (ir.hasNext()) {
	        while (ir.hasNext()) {
	          DeptDb dd = (DeptDb)ir.next();
	        %>
	          <option value="<%=dd.getCode()%>"><%=dd.getName()%></option>
	        <%
	        }
        } else {
        	%>
	          <option value="<%=privilege.getUserUnitCode(request)%>"><%=new DeptDb(privilege.getUserUnitCode(request)).getName()%></option>
	        <%
        }
	        %>
        </select>          
  </td>
</tr><tr>
  <td colspan=2 align="center">
     <input name="submit" type="button" class="btn"  value="确定" onclick="mysubmit()" > 
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
     <input type="button" class="btn"  value="返回" onclick="window.location.href='officeequip_add_list.jsp'" ></td>
</tr>
</form>
</table>
<script>
$(function(){
	$('#buyDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
function mysubmit(){
	$.ajax({
		url: "officeequip_do.jsp?op=equip_add&" + $('#form1').serialize(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			jAlert(data.msg, "提示");
			if(data.ret == 1){
				$('#officeName').val("office_equipment");
				$('#measureUnit').val("");
				$('#storageCount').val("");
				$('#price').val("");
				$('#buyPerson').val("");
				$('#buyDate').val("<%=dt %>");
				jAlert_Redirect(data.msg, "提示", "officeequip_add_list.jsp");				
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
