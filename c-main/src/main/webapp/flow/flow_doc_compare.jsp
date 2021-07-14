<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int flowId = ParamUtil.getInt(request, "flowId", -1);
if (flowId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "flowId格式错误！"));
	return;
}

String ids = ParamUtil.get(request, "ids");
String[] ary = StrUtil.split(ids, ",");
if (ary==null || ary.length<2) {
	out.print(SkinUtil.makeErrMsg(request, "请选择两条记录！"));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=Global.AppName%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../js/compare.js"></script>
<script type="text/javascript">
//滚动条同步
function DriveRightScroll() {
	var RightDivObj = document.getElementById('contrastt_right');
	var LeftDivObj = document.getElementById('contrastt_left');
	if (LeftDivObj.scrollTop < (RightDivObj.scrollHeight - RightDivObj.clientHeight))
	{
	  RightDivObj.scrollTop = LeftDivObj.scrollTop;
	  RightDivObj.scrollLeft = LeftDivObj.scrollLeft;
	}
}
function DriveLeftScroll() {
	var RightDivObj = document.getElementById('contrastt_right');
	var LeftDivObj = document.getElementById('contrastt_left');
	if (RightDivObj.scrollTop < (LeftDivObj.scrollHeight - LeftDivObj.clientHeight))
	{
	  LeftDivObj.scrollTop = RightDivObj.scrollTop;
	  LeftDivObj.scrollLeft = RightDivObj.scrollLeft;     
	}
}
</script>
</head>
<body>
<div class="content" style="width:100%">
  <div align="center" style="text-align:left; clear:both; padding:10px">
    <div id="contentDiv">
      <div id="contrast">
		<% 
		Document doc = new Document(); 
		doc = doc.getDocument(StrUtil.toInt(ary[0]));   
		String content = doc.getContent(1); 
		Document doc2 = doc.getDocument(StrUtil.toInt(ary[1])); 
		String content2 = doc2.getContent(1); 
		%>      
		<div style="width:50%;float:left;text-align:left">
		&nbsp;<%=doc.getAuthor() %>&nbsp;&nbsp;<%=DateUtil.format(doc.getCreateDate(), "yyyy-MM-dd HH:mm:ss") %>
		</div>
		<div style="width:50%;float:left;text-align:left">
		&nbsp;<%=doc2.getAuthor() %>&nbsp;&nbsp;<%=DateUtil.format(doc2.getCreateDate(), "yyyy-MM-dd HH:mm:ss") %>
		</div>
      </div>
      <div id="contrastt_left" style="width:50%;height:540px; overflow:auto; float:left" onscroll="DriveRightScroll()">
		<%=StrUtil.getNullStr(content)%>
	  </div>
      <div id="contrastt_right" style="width:50%;height:540px; overflow:auto; float:left" onscroll="DriveLeftScroll()" > <%=StrUtil.getNullStr(content2)%> </div>
    </div>
    <div style="clear:both"></div>
    <div style="margin-left:10px; margin-top: 10px">
      <ul id="contrast_left" style="list-style-type: none;">
        <li><strong>注:</strong></li>
        <li>1、浅绿色 表示一个范围</li>
        <li>2、浅紫色 存在差异</li>
      </ul>
    </div>
  </div>
</div>
</div>
</body>
<script type="text/javascript">
CompareById('contrastt_left', 'contrastt_right');
</script>
</html>