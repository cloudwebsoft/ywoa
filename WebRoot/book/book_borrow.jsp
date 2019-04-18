<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
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
<title>图书借阅</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
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

function setPerson(deptCode, deptName, user, userRealName)
{
	form1.borrowPerson.value = user;
	form1.userRealName.value = userRealName;
}
//-->
</script>
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%
if (ids!=null)
	len = ids.length;
else {
	out.print(StrUtil.jAlert_Redirect("你没有选择要借阅的记录！","提示", "book_list.jsp"));
	return;
}
String strids = "";
for (int k=0; k<len; k++) {
	if (strids.equals(""))
		strids = ids[k];
	else
		strids += "," + ids[k];
}
%>
<div class="spacerH"></div>
<table class="tabStyle_1 percent80">
  <tr>
    <td class="tabStyle_1_title"align="center">图书名称</td>
    <td class="tabStyle_1_title">图书编号</td>
    <td class="tabStyle_1_title">出版社</td>
    <td class="tabStyle_1_title">作者</td>
    <td class="tabStyle_1_title">所属类别</td>
  </tr>
<%
	for (int i=0; i<len; i++) {
		bd = bd.getBookDb(Integer.parseInt(ids[i]));
	    int typeId = bd.getTypeId();
		BookTypeDb btd = btdb.getBookTypeDb(typeId);
%>
  <tr>
    <td id="bookName" name="bookName"><%=bd.getBookName()%></td>
    <td id="bookNum" name="bookNum" ><%=bd.getBookNum()%></td>
    <td id="pubHouse" name="pubHouse"><%=bd.getPubHouse()%></td>
    <td id="author" name="author"><%=bd.getAuthor()%></td>
    <td id="deptCode" name="deptCode"><%=btd.getName()%></td>
  </tr>
<%
	}
%>
</table>
<table class="tabStyle_1 percent80">
  <form action="book_do.jsp?op=borrow" name="form1" method="post">
    <tr>
      <td align="center">借阅时间：<input name="ids" type="hidden" value="<%=strids%>" /></td>
      <td>
<%
	Date d = new Date();
	String dt = DateUtil.format(d, "yyyy-MM-dd");
	d = DateUtil.addDate(d, 30);
	String edt = DateUtil.format(d, "yyyy-MM-dd");
%>
	<input type="text" name="beginDate" id="beginDate" value="<%=dt%>" maxlength="100" >
	  </td>
      <td align="center">预计归还时间： </td>
      <td><input type="text" name="endDate" id="endDate" value="<%=edt%>" maxlength="100" readOnly>
        </td>
    </tr>
    <tr>
      <td align="center" nowrap>借&nbsp;&nbsp;阅&nbsp;&nbsp;人：</td>
      <td colspan="3"><input type="hidden" name="borrowPerson" id="borrowPerson" value="">
        <input name="userRealName" type="text" id="userRealName" value="" readonly>
        <a href="#" onClick="javascript:showModalDialog('../user_sel.jsp?unitCode=<%=StrUtil.UrlEncode(privilege.getUserUnitCode(request))%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a></td>
    </tr>
    <tr>
      <td align="center" nowrap>备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注：</td>
      <td colspan="3"><textarea  name="brief"  id="brief" style="width:98%" rows="8"></textarea></td>
    </tr>
    <tr>
      <td colspan="4" align="center"><input name="submit" type="submit" class="btn"  value="提  交" ></td>
    </tr>
  </form>
</table>
</body>
<script>
$(function() {
	$('#endDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
});
</script>
</html>
