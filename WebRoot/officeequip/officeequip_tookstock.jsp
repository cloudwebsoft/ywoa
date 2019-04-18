<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
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
String officecode="";
String officename="";
int rukun=0;
int ly=0;
int jc=0;
int gh=0;
int kucun=0;
if (op.equals("tookstock")) {
	officecode =ParamUtil.get(request, "officeName");
	OfficeDb officeDb = new OfficeDb();
	rukun=officeDb.querySumByCode(officecode);
	
	OfficeOpDb officeOpDb = new OfficeOpDb();
	ly=officeOpDb.queryNumByCode(officecode,OfficeOpDb.TYPE_RECEIVE);
	jc=officeOpDb.queryNumByCode(officecode,OfficeOpDb.TYPE_BORROW);
	gh=officeOpDb.queryNumByCode(officecode,OfficeOpDb.TYPE_RETURN);

  OfficeStocktakingDb officeStocktakingDb = new OfficeStocktakingDb();
  kucun=officeStocktakingDb.queryNumByCode(officecode);
  
  TreeSelectDb treeSelectDb = new TreeSelectDb(officecode);
  officename = treeSelectDb.getName();
  
}

Date d = new Date();
String dt = DateUtil.format(d, "yyyy-MM-dd");
int realNum = 0;

String officeName = ParamUtil.get(request, "officeName");
if ("".equals(officeName)) {
	officeName = "office_equipment";
}

//String strTypeId = ParamUtil.get(request, "typeId");
//String strEquipId =ParamUtil.get(request, "officeName");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>盘点</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<script>
function selectEquip() {
	$.ajax({
		url: "officeequip_do.jsp?op=equip_storecount&officeName=" + $('#officeName').val(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			if(data.ret == 1){
				$('#tip_info').html("");
				$('#inCount').html(data.inCount);
				$('#receiveCount').html(data.receiveCount);
				$('#borrowCount').html(data.borrowCount);
				$('#returnCount').html(data.returnCount);
				$('#storeCount').html(data.storeCount);
			} else {
				$('#tip_info').html(data.msg);
				$('#inCount').html("");
				$('#receiveCount').html("");
				$('#borrowCount').html("");
				$('#returnCount').html("");
				$('#storeCount').html("");
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
<%@ include file="officeequip_inc_menu_top.jsp"%>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div> 
<%
	if (op.equals("edit")) {
		if(ParamUtil.get(request, "realNum") == null || "".equals(ParamUtil.get(request, "realNum")))
		{
			out.print(StrUtil.jAlert("请输入当前库存！","提示"));
			
		}else
		{
			OfficeMgr officeMgr = new OfficeMgr();
			boolean re = false;
			try {%>
			<script>
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			</script>
			<%
			  re = officeMgr.tookstock(request);
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
			if(re)
			{
				out.print(StrUtil.jAlert("操作成功！","提示"));
			}
		}
  
}
 %>
<script>
o("menu9").className="current";
</script>
<div class="spacerH"></div>
<form action="?op=tookstock" name="form1" id="form1" method="post">
<table class="tabStyle_1 percent60">
    <tr>
      <td class="tabStyle_1_title" colspan="2">办公用品盘点</td>
    </tr>

    <tr>
      <td width=20% align="center">用品名称：</td>
      <td align="left"><%
      	    int total =0 ;
      	
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb("office_equipment");
		TreeSelectView tsv = new TreeSelectView(tsd);
		StringBuffer sb = new StringBuffer();
		tsv.getTreeSelectAsOptions(sb, tsd, 1);
	  %>
        <select name="officeName" id="officeName" onChange="selectEquip()">
        	<%=sb%>
        </select>
        <font color=red>  *  </font>&nbsp;&nbsp;&nbsp;&nbsp;<b id="tip_info" style="color: red;width: 150px"></b>
	   <script>
       o("officeName").value = "<%=officeName%>";
       </script>        
        </td>
    </tr>
    <tr>
      <td align="center">入库总数：</td>
      <td id="inCount" align="left"></td>
    </tr>
    <tr>
      <td align="center">领用总数：</td>
      <td id="receiveCount" align="left"></td>
    </tr>
    <tr>
      <td align="center">借用总数：</td>
      <td id="borrowCount" align="left"></td>
    </tr>
    <tr>
      <td align="center">归还总数：</td>
      <td id="returnCount" align="left"></td>
    </tr>
    <tr>
      <td align="center">当前库存：</td>
      <td id="storeCount" align="left"></td>
    </tr>
    <tr>
      <td align="center">实际库存：</td>
      <td align="left">
      	<input name="realNum" type="text" id="realNum" value="" size="10" maxlength="100">
      	<font color=red>  (*注：不填写则默认实际库存与当前库存一致)  </font>
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center">
      <input name="submit" type="button" class="btn"  value="确定" onclick="mysubmit()" >
      &nbsp;&nbsp;
	  <input type="button" class="btn"  value="返回" onclick="window.location.href='officeequip_tookstock_list.jsp'" >      
      </td>
    </tr>
</table>
</form>
<script>
$(function() {
	selectEquip();
});

function mysubmit(){
	if ($('#realNum').val() == '') {
		$('#realNum').val($('#storeCount').html());
	}
	$.ajax({
		url: "officeequip_do.jsp?op=equip_tookstore&" + $('#form1').serialize(),
		type: "post",
		dataType: "json",
		success: function(data, status){
			jAlert(data.msg, "提示");
			if(data.ret == 1){
				$('#officeName').val("office_equipment");
				$('#inCount').html("");
				$('#receiveCount').html("");
				$('#borrowCount').html("");
				$('#returnCount').html("");
				$('#storeCount').html("");
				$('#realNum').val("");
				jAlert_Redirect(data.msg, "提示", "officeequip_tookstock_list.jsp");				
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
