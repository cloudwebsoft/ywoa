<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="org.jdom.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></title>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.forum.security.flood.FloodConfig myconfig = com.redmoon.forum.security.flood.FloodConfig.getInstance();

String op = ParamUtil.get(request, "op");
%>
<%@ include file="flood_nav.jsp"%>
<script>
$("menu1").className="active";
</script>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="frame_gray" >
  <tr> 
    <td colspan="3" width="100%" height="23" class="thead">
      参数设置    </td>
  </tr>
  <%
Element root = myconfig.getRootElement();

String name="",value = "";
name = request.getParameter("name");
if (name!=null && !name.equals("")) {
	value = ParamUtil.get(request, "value");
	myconfig.put(name,value);
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flood_config.jsp"));
	ForumSchedulerUnit.initParam();
	com.redmoon.forum.MsgDb.initParam();
	ForumPage.init();
}

int k = 0;
Iterator ir = root.getChild("flood").getChildren().iterator();
String desc = "";
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  desc = e.getAttributeValue("desc");
  name = e.getName();
  value = e.getValue();
%>
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='flood_config.jsp'>
          <tr class="highlight"> 
            <td width='36%' height="22"> <INPUT TYPE=hidden name=name value="<%=name%>"> 
              &nbsp;<%=myconfig.getDescription(request, name)%> 
            <td width='41%'>
			<%if (value.equals("true") || value.equals("false")) {%>
				<select name="value"><option value="true"><lt:Label key="yes"/></option><option value="false"><lt:Label key="no"/></option></select>
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
			<%}else{%>
				<input type=text value="<%=value%>" name="value" style='border:1pt solid #636563;font-size:9pt' size=50>
            <%}%>
			<td width="23%" align=center> <INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>            </td>
          </tr>
        </FORM>
<%
  k++;
}
%>
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