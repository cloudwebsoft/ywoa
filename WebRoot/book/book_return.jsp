<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "book.all";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String[] ids = request.getParameterValues("ids");
int len = 0;

BookDb bd= new BookDb();
BookTypeDb btdb = new BookTypeDb();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>图书添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="JavaScript" type="text/JavaScript">
<!--
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);
	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}
//-->
</script>
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<%
if (ids!=null)
	len = ids.length;
else
   out.print(StrUtil.jAlert_Redirect("你没有选择要借阅的记录！","提示", "book_query.jsp"))	;
String strids = "";
for (int k=0; k<len; k++) {
if (strids.equals(""))
	strids = ids[k];
else
	strids += "," + ids[k];
}
 %>
<div class="spacerH"></div>
<table class="tabStyle_1 percent98">
  <tr>
    <td width="19%" class="tabStyle_1_title">图书名称</td>
    <td width="10%" class="tabStyle_1_title">图书编号</td>
    <td width="10%" class="tabStyle_1_title">作者</td>
    <td width="11%" class="tabStyle_1_title">所属类别</td>
    <td width="18%" class="tabStyle_1_title">借阅人</td>
    <td width="12%" class="tabStyle_1_title">借阅时间</td>
    <td width="20%" class="tabStyle_1_title">预计归还时间</td>
  </tr>
<%
for (int i=0; i<len; i++) {
	bd = bd.getBookDb(Integer.parseInt(ids[i]));
	int typeId = bd.getTypeId();
	BookTypeDb btd = btdb.getBookTypeDb(typeId);
	UserDb ud = new UserDb(bd.getBorrowPerson());
%>
  <tr>
    <td id="bookName" name="bookName"><%=bd.getBookName()%></td>
    <td id="bookNum" name="bookNum" ><%=bd.getBookNum()%></td>
    <td id="pubHouse" name="pubHouse"><%=bd.getAuthor()%></td>
    <td id="author" name="author"><%=btd.getName()%></td>
    <td id="deptCode" name="deptCode"><%=ud.getRealName()%></td>
    <td align="center" id="deptCode" name="deptCode"><%=bd.getBeginDate()%></td>
    <td align="center" id="deptCode" name="deptCode"><%=bd.getEndDate()%></td>
  </tr>
  <%}%>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0">
<form action="book_return_do.jsp?op=return" name="form1" method="post">
<tr>
  <td align="center">归还时间：<input name="ids" type="hidden" value="<%=strids%>" />
  <%
  Date d = new Date();
  String dt = DateUtil.format(d, "yyyy-MM-dd");
  d = DateUtil.addDate(d, 30);
  String edt = DateUtil.format(d, "yyyy-MM-dd");
  %>
  <input type="text" name="beginDate" id="beginDate" value="<%=dt%>" readOnly >
  &nbsp;&nbsp;&nbsp;&nbsp;<input name="submit" type="submit" class="btn"  value=" 提 交 " ></td>
</tr>
</form>
</table>
</body>
<script>
$(function() {
	$('#beginDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
});

</script>
</html>
