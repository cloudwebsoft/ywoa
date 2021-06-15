<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="java.util.Iterator"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查看工作记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}
//-->
</script>
<script language=javascript>
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
</head>
<body>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
String userName = ParamUtil.get(request, "userName");
boolean nav = ParamUtil.get(request, "isNav").equals("1");

String content="",mydate="",appraise="";
String sql = "";

WorkLogMgr wlm = new WorkLogMgr();
WorkLogDb wld = null;
try {
	wld = wlm.getWorkLogDb(request, id);
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<% if(!nav){%>
<%@ include file="mywork_nav.jsp"%>
<%}%>


<%
if (wld!=null && wld.isLoaded()) {
	content = wld.getContent();
	appraise = wld.getAppraise();
	mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd");
}
%>
  <br/>    
      <table width="98%" height="93%" border="0" align="center" cellpadding="3" cellspacing="0" class="tabStyle_1 percent80" style="margin-top:30px;">
        
        <form name="form1" action="?op=edit" method="post" onSubmit="">
          <tr>
            <td height="29" align="left" class="tabStyle_1_title">
      <%if (wld.getLogType()==WorkLogDb.TYPE_NORMAL) {%>
      日期：<%=mydate%>
      <%}else if (wld.getLogType()==WorkLogDb.TYPE_WEEK) {%>
      第<%=wld.getLogItem()%>周
      <%}else{%>
      <%=wld.getLogItem()%>月
      <%}%>
            <input type=hidden name="id" value="<%=id%>"></td>
          </tr>
          <tr> 
            <td align="left" valign="top">&nbsp;
            <% 
            	String str = "";
            	for (int i = 0; i < content.length(); i += 110){
                  if(content.length()>(i+110)){
                        str += content.substring(i, i + 110)+"<br/>";
                  }else{
                        str +=content.substring(i, content.length());
                  }
           		}
            %>
            <%=str %>
			<div style="border-bottom:1px dashed #cccccc"></div>
			<p><%=StrUtil.getNullStr(appraise)%></td>
          </tr>
          <tr>
          
          	<td>
          		<div align="left" id="div<%=wld.getId() %>" name="div<%=wld.getId() %>">
					<%
					WorkLogAttachmentDb workLogAttachmentDb = new WorkLogAttachmentDb();
					Iterator ir = workLogAttachmentDb.getAttachsOfMyWork(wld.getId()).iterator();
					while (ir.hasNext()) {
						WorkLogAttachmentDb att = (WorkLogAttachmentDb)ir.next();
					%>
		              <img src="../images/attach2.gif" align="absmiddle" /> <a href="getfile.jsp?attachId=<%=att.getId()%>&wldId=<%=wld.getId()%>" target="_blank"><%=att.getName()%></a>
					  <br/>
		              <%}%>
				</div>
          	</td>
          </tr>
          <tr>
            <td align="center" valign="top">
		<%if (privilege.canAdminUser(request, userName)) {%>
      	&nbsp; <input type="button" class="btn" onclick="window.location.href = 'mywork_appraise.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>';" value="点评" />
	  	<%}%>            
            </td>
          </tr>
        </form>
    </table>    
</body>
</html>
