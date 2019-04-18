<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>选择时间</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta name="viewport" content="width=device-width,minimum-scale=1.0, maximum-scale=2.0"/>
<script src="../../inc/common.js"></script>
<style>
*{
 font:12px;
 letter-spacing:0px;
}
body{
 overflow:hidden;
 margin:0;
 border:0px;
}
#titleYear{
 text-align:center;
 padding-top:3px;
 width:120px;
 height:20px;
 border:solid #E5E9F2;
 border-width:0px 1px 1px 0px;
 background-color:#A4B9D7;
 color:#000;
 cursor:default;
}
#weekNameBox{
 width:282px;
 border-bottom:0;
}
.weekName{
 text-align:center;
 padding-top:4px;
 width:40px;
 height:20px;
 border:solid #E5E9F2;
 border-width:0px 1px 1px 0px;
 background-color:#C0D0E8;
 color:#243F65;
 cursor:default;
}
.controlButton{
 font-family: Webdings;
 font:9px;
 text-align:center;
 padding-top:2px;
 width:40px;
 height:20px;
 border:solid #E5E9F2;
 border-width:0px 1px 1px 0px;
 background-color:#A4B9D7;
 color:#243F65;
 cursor:default;
}

.Ctable{
 width:282px;
 margin-bottom:20px;
}
.Ctable span{
 font:9px verdana;
 font-weight:bold;
 color:#243F65;
 text-align:center;
 padding-top:4px;
 width:40px;
 height:26px;
 border:solid #C0D0E8;
 border-width:0px 1px 1px 0px;
 cursor:default;
}
.Cdate{
 background-color:#E5E9F2;
}
.Ctable span.OtherMonthDate{
 color:#999;
 background-color:#f6f6f6;
}

.selectBox{
 cursor:hand;
 font:9px verdana;
 width:80px;
 position:absolute;
 border:1px solid #425E87;
 overflow-y:scroll;
 overflow-x:hidden;
 background-color:#fff;
 FILTER:progid:DXImageTransform.Microsoft.Shadow(Color=#999999,offX=10,offY=10,direction=120,Strength=5);
 SCROLLBAR-FACE-COLOR: #E5E9F2;
 SCROLLBAR-HIGHLIGHT-COLOR: #E5E9F2;
 SCROLLBAR-SHADOW-COLOR: #A4B9D7; 
 SCROLLBAR-3DLIGHT-COLOR: #A4B9D7; 
 SCROLLBAR-ARROW-COLOR:  #000000; 
 SCROLLBAR-TRACK-COLOR: #eeeee6; 
 SCROLLBAR-DARKSHADOW-COLOR: #ffffff;
}
.selectBox nobr{
 padding:0px 0px 2px 5px;
 width:100%;
 color:#000;
 letter-spacing:2px;
 text-decoration:none;
}
</style>
<body onselectstart="return false">
<table width="260" height="105" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1">
  <thead>
  <tr>
    <td height="24" align="center"><lt:Label res="res.label.util.time" key="please_select_time"/></td>
  </tr>
  </thead>
  <tr>
    <td height="30" align="center"><lt:Label res="res.label.util.time" key="hour"/>
      <select name=hour>
      <script>
	for (var i=0; i<=23; i++) {
		var h = i;
		if (i<10)
			h = "0" + i;
		document.write("<option value='" + h + "'>" + h + "</option>");
	}
	</script></select>	&nbsp;&nbsp;
	<lt:Label res="res.label.util.time" key="minute"/>
      <select name=minute>
    <script>
	for (var i=0; i<=59; i++) {
		var h = i;
		if (i<10)
			h = "0" + i;
		document.write("<option value='" + h + "'>" + h + "</option>");
	}
	</script></select>	&nbsp;&nbsp;
	<lt:Label res="res.label.util.time" key="second"/>
      <select name=second>
    <script>
	for (var i=0; i<=59; i++) {
		var h = i;
		if (i<10)
			h = "0" + i;
		document.write("<option value='" + h + "'>" + h + "</option>");
	}
	o("hour").value = "12";
	o("minute").value = "00";
	o("second").value = "00"; 
	</script></select>
    </td>
  </tr>
  <tr>
    <td height="30" align="center"><input class="btn" type="button" value="确定" onClick="onOk()" >
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <input class="btn" type=button value=<lt:Label key="close"/> onClick="window.close()"></td>
  </tr>
</table>
</body>
<script>
function onOk() {
	if (window.opener) {
	    var time = o("hour").value+':'+o("minute").value+':' +o("second").value;
		window.opener.setDateTime(time);
	}
	else {
		window.returnValue=hour.value+':'+minute.value+':' +second.value;
	}
	window.close();
}
</script>
</HTML>