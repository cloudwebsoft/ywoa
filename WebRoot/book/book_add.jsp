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
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="book_do.jsp?op=add" name="form1" method="post">
<table align="center" class="tabStyle_1 percent80">
	<thead>
    <tr>
      <td colspan="4" class="tabStyle_1_title">添加图书</td>
    </tr>
    </thead>
    <tr>
      <td width="68">图书编号：</td>
      <td width="171"><input type="text" name="bookNum" id="bookNum" value="" maxlength="110">
        <font color=red>*</font></td>
      <td width="70">书名： </td>
      <td width="167"><input type="text" name="bookName" id="bookName" value="" maxlength="100">
        <font color=red>*</font></td>
    </tr>
    <tr>
      <td>图书类别：</td>
      <td><%
	  BookTypeDb btd = new BookTypeDb();
	  String opts = "";
	  Iterator ir = btd.list().iterator();
	  while (ir.hasNext()) {
	  	 btd = (BookTypeDb)ir.next();
	  	 opts += "<option value='" + btd.getId() + "'>" + btd.getName() + "</option>";
	  }
	  %>
        <select name="typeId" id="typeId" >
          <option selected>-----请选择-----</option>
          <%=opts%>
        </select>
        <font color=red>*</font></td>
      <td>图书归属：</td>
      <td width="167"><select name="deptCode">
          <%
	DeptMgr dm = new DeptMgr();
	DeptDb lf = dm.getDeptDb(DeptDb.ROOTCODE);
	DeptView dv = new DeptView(lf);
	dv.ShowDeptAsOptions(out, lf, lf.getLayer()); 
 %>
        </select></td>
    </tr>
    <tr>
      <td>作者： </td>
      <td><input type="text" name="author" id="author" value="" maxlength="100"></td>
      <td>价格：(￥)</td>
      <td><input type="text" name="price" id="price" maxlength="100">
        <font color=red>*</font></td>
    </tr>
    <tr>
      <td>出版社：</td>
      <td><input type="text" name="pubHouse" id="pubHouse"value="" maxlength="100"></td>
      <td>出版日期： </td>
      <td><input type="text" name="pubDate" id="pubDate" readOnly>
    </tr>
    <tr>
      <td align="left" nowrap>内容简介：</td>
      <td colspan="3"><textarea  name="brief"  id="brief" style="width:98%" rows="8"></textarea>
      </td>
    </tr>
    <tr>
      <td colspan="4" align="center"><input name="submit" type="submit" class="btn"  value=" 确  定 " >
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input type="reset" class="btn"  value=" 重  置 " >
      </td>
    </tr>
</table>
</form>
</body>
<script>
$(function() {
	$('#pubDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
});

</script>
</html>
