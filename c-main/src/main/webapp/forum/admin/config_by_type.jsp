<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="org.jdom.*"%>
<%@ include file="../inc/inc.jsp" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></title>
<%@ include file="../inc/nocache.jsp" %>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style><body style="background-color:#ffffff">
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.forum.Config myconfig = com.redmoon.forum.Config.getInstance();

ForumDb fd = new ForumDb();
fd = fd.getForumDb();

String op = ParamUtil.get(request, "op");

fd = ForumDb.getInstance();
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr>
    <td width="100%" height="23" class="thead">&nbsp;
        <lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></td>
  </tr>
  <tr>
    <td valign="top" bgcolor="#FFFFFF"><%
Element root = myconfig.getRootElement();
String type = ParamUtil.get(request, "type");

String name="",value = "";
name = request.getParameter("name");
if (name!=null && !name.equals("")) {
	value = ParamUtil.get(request, "value");
	if (name.equals("fullTextSearchTime")) {
		value = ParamUtil.get(request, "search_date") + " " + ParamUtil.get(request, "search_time");
	}
	myconfig.put(name,value);
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_by_type.jsp?type=" + type));
	ForumSchedulerUnit.initParam();
	com.redmoon.forum.MsgDb.initParam();
	ForumPage.init();
}

int k = 0;
Iterator ir = root.getChild("forum").getChildren().iterator();
String desc = "";
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  if (!e.getAttributeValue("type").equals(type))
  	continue;
  desc = e.getAttributeValue("desc");
  name = e.getName();
  value = e.getValue();
%>
        <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1">
          <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_by_type.jsp?type=<%=type%>'>
            <tr>
              <td bgcolor=#F6F6F6 width='52%'><INPUT TYPE=hidden name=name value="<%=name%>">
                &nbsp;<%=myconfig.getDescription(request, name)%>
              <td bgcolor=#F6F6F6 width='34%'><%
			if (name.equals("waterMarkPos")) {%>
                  <select name="value">
                    <option value="<%=cn.js.fan.util.file.image.WaterMarkUtil.POS_LEFT_TOP%>">
                    <lt:Label res="res.label.forum.admin.config_m" key="left_top"/>
                    </option>
                    <option value="<%=cn.js.fan.util.file.image.WaterMarkUtil.POS_LEFT_BOTTOM%>">
                    <lt:Label res="res.label.forum.admin.config_m" key="left_bottom"/>
                    </option>
                    <option value="<%=cn.js.fan.util.file.image.WaterMarkUtil.POS_RIGHT_TOP%>">
                    <lt:Label res="res.label.forum.admin.config_m" key="right_top"/>
                    </option>
                    <option value="<%=cn.js.fan.util.file.image.WaterMarkUtil.POS_RIGHT_BOTTOM%>">
                    <lt:Label res="res.label.forum.admin.config_m" key="right_bottom"/>
                    </option>
                  </select>
                  <script>
			form<%=k%>.value.value = "<%=value%>";
			</script>
            <%} else if (name.equals("loginSaveDate")) {%>
                <select name="value">
                <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_NONE%>" selected="selected">
                  <lt:Label res="res.label.door" key="not_save"/>
                  </option>
                <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_DAY%>">
                  <lt:Label res="res.label.door" key="save_one_day"/>
                  </option>
                <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_MONTH%>">
                  <lt:Label res="res.label.door" key="save_one_month"/>
                  </option>
                <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_YEAR%>">
                  <lt:Label res="res.label.door" key="save_one_year"/>
                  </option>
                </select>
            <script>
			form<%=k%>.value.value = "<%=value%>";
			</script> 				  
			<%} else if (value.equals("true") || value.equals("false")) {%>
                  <select name="value">
                    <option value="true">
                    <lt:Label key="yes"/>
                    </option>
                    <option value="false">
                    <lt:Label key="no"/>
                    </option>
                  </select>
                  <script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
                  <%}else if(name.equals("userLevel")){%>
                  <select name="value">
                    <option value="levelCredit">
                    <lt:Label res="res.label.forum.admin.config_m" key="credit"/>
                    </option>
                    <option value="levelExperience">
                    <lt:Label res="res.label.forum.admin.config_m" key="experience"/>
                    </option>
                    <option value="levelGold">
                    <lt:Label res="res.label.forum.admin.config_m" key="gold"/>
                    </option>
                    <option value="levelTopticCount">
                    <lt:Label res="res.label.forum.admin.config_m" key="least_topic"/>
                    </option>
                  </select>
                  <script>
					form<%=k%>.value.value = "<%=value%>";
				</script>
                  <%}else if (name.equals("fullTextSearchTime")) {
				java.util.Date d = DateUtil.parse(value, "yyyy-MM-dd HH:mm:ss");
				String dstr = DateUtil.format(d, "yyyy-MM-dd");
				String hstr = DateUtil.format(d, "HH:mm:ss");
			%>
                  <input name="vlaue" size="10" type="hidden">
                  <input name="search_date" size=10 readonly value="<%=dstr%>">
                &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" onClick="SelectDate('search_date','yyyy-mm-dd')">
                <input style="WIDTH: 80px" value="<%=hstr%>" name="search_time" size="30">
                &nbsp; <img style="CURSOR: hand" onClick="SelectDateTime(form<%=k%>.search_time)" src="../images/clock.gif" align="absMiddle" width="18" height="18">
                <%}else if (name.equals("signUserLevel")) {%>
                <select name="value">
                  <%
	UserLevelDb uld = new UserLevelDb();
	Vector vlevel = uld.getAllLevel();
	Iterator irlevel = vlevel.iterator();
	int i = 0;
	while (irlevel.hasNext()) {
		i ++;
		uld = (UserLevelDb)irlevel.next();
	%>
                  <option value="<%=uld.getLevel()%>"><%=uld.getDesc()%></option>
                  <%}				
				%>
                </select>
                <script>
				form<%=k%>.value.value = "<%=uld.getUserLevelDbByLevel(StrUtil.toInt(value, 0)).getLevel()%>";
				</script>
                <%}else if (name.equals("faceUserLevel")) {%>
                <select name="value">
                  <%
				UserLevelDb uld = new UserLevelDb();
				Vector vlevel = uld.getAllLevel();
				Iterator irlevel = vlevel.iterator();
				int i = 0;
				while (irlevel.hasNext()) {
					i ++;
					uld = (UserLevelDb)irlevel.next();
				%>
                  <option value="<%=uld.getLevel()%>"><%=uld.getDesc()%></option>
                  <%}%>
                </select>
                <script>
				form<%=k%>.value.value = "<%=uld.getUserLevelDbByLevel(StrUtil.toInt(value, 0)).getLevel()%>";
				</script>
                <%}else if (name.equals("UBBTopicTitleUserLevel")){%>
                <select name="value">
                  <%
				UserLevelDb uld = new UserLevelDb();
				Vector vlevel = uld.getAllLevel();
				Iterator irlevel = vlevel.iterator();
				int i = 0;
				while (irlevel.hasNext()) {
					i ++;
					uld = (UserLevelDb)irlevel.next();
				%>
                  <option value="<%=uld.getLevel()%>"><%=uld.getDesc()%></option>
                  <%}%>
                </select>
                <script>
				form<%=k%>.value.value = "<%=uld.getUserLevelDbByLevel(StrUtil.toInt(value, 0)).getLevel()%>";
				</script>
                <%}else{%>
                <input type=text value="<%=value%>" name="value" style='border:1pt solid #636563;font-size:9pt' size=30>
                <%}%>
              <td width="14%" align=center bgcolor=#F6F6F6><INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>
              </td>
            </tr>
          </FORM>
        </table>
      <%
  k++;
}
%></td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
    <td valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table>
</body>
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

var GetDate=""; 
function SelectDate(ObjName,FormatDate) {
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("../../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
} 

function SelectDateTime(obj) {
	var dt = showModalDialog("../../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		obj.value = dt;
}
</script>                                       
</html>