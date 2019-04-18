<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.robot.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
int id = ParamUtil.getInt(request, "id");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<TITLE>Robot export</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>
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

function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		findObj(objName).value = dt;
}

function form2_onsubmit() {
	var t = form2.time.value;
	var ary = t.split(":");
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form2.month_day.value;
	if (weekDay=="" && dayOfMonth=="") {
		alert("请填写每月几号或者星期几！");
		return false;
	}
	if (weekDay=="")
		weekDay = "?";
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	if (dayOfMonth=="")
		dayOfMonth = "?";
	var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * " + weekDay;
	form2.cron.value = cron;
	form2.data_map.value = "<%=id%>";
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
RobotDb rd = new RobotDb();
rd = (RobotDb)rd.getQObjectDb(new Integer(id));
if (op.equals("modify")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, rd, "cms_robot_save")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?id=" + id));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="64%" class="tdStyle_1">导出机器人</td>
      <td width="36%" class="tdStyle_1"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp">浏览机器人</A></TD>
            <TD><A class=add 
            href="robot_add.jsp">添加新机器人</A></TD>
            <TD><A class=other 
            href="robot_import.jsp">导入机器人</A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" summary="" border=0><TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class="tabStyle_1 percent98">
  <TBODY>
  <TR>
    <TD align="left" class="tabStyle_1_title"><%=rd.getString("name")%></TD>
    </TR>
  <TR>
    <TD align="center">
	<textarea rows="30" style="width:750px"><%=rd.Export()%></textarea>
	</TD>
    </TR></TBODY></TABLE>
</BODY></HTML>
