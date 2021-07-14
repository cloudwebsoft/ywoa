<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.forum_m" key="filter_info"/></title>
<link rel="stylesheet" href="../../common.css">
<script language="JavaScript">
<!--
function openWin(url,width,height)
{
	var newwin = window.open(url,"_blank","scrollbars=yes,resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

function openSelTopicWin() {
	openWin("../topic_m.jsp?action=sel", 640, 480);	
}

function delNotice(id) {
	var ntc = form1.notices.value;
	var ary = ntc.split(",");
	var ary2 = new Array();
	var k = 0;
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			continue;
		}
		else {
			ary2[k] = ary[i];
			k++;
		}
	}
	ntc = "";
	for (i=0; i<ary2.length; i++) {
		if (ntc=="")
			ntc += ary2[i];
		else
			ntc += "," + ary2[i];
	}
	form1.notices.value = ntc;
	form1.submit();
}

function selTopic(ids) {
	// 检查在notices中是否已包含了ids中的id，避免重复加入
	var ary = ids.split(",");
	var ntc = form1.notices.value;
	var ary2 = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		var founded = false;
		for (var j=0; j<ary2.length; j++) {
			if (ary[i]==ary2[j]) {
				founded = true;
				break;
			}
		}
		if (!founded) {
			if (ntc=="")
				ntc += ary[i];
			else
				ntc += "," + ary[i];
		}
	}
	form1.notices.value = ntc;
}

function up(id) {
	var ntc = form1.notices.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=0) {
				var tmp = ary[i-1];
				ary[i-1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	form1.notices.value = ntc;
	form1.submit();
}

function down(id) {
	var ntc = form1.notices.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=ary.length-1) {
				var tmp = ary[i+1];
				ary[i+1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	form1.notices.value = ntc;
	form1.submit();
}
//-->
</script>
<LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<%
ForumDb fd = new ForumDb();
fd = fd.getForumDb();
String op = ParamUtil.get(request, "op");
if (op.equals("setNotice")) {
	String ids = ParamUtil.get(request, "notices");
	fd.setNotices(ids);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
if (op.equals("setFilterName")) {
	String filterName = ParamUtil.get(request, "filterName");
	fd.setFilterUserName(filterName);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
if (op.equals("setFilterMsg")) {
	String filterMsg = ParamUtil.get(request, "filterMsg");
	fd.setFilterMsg(filterMsg);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}

fd = ForumDb.getInstance();
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.forum_m" key="forum_notice"/></td>
  </tr>
</table>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<br>
<TABLE class="frame_gray" cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF" class="thead"><lt:Label res="res.label.forum.admin.forum_m" key="forum_notice"/></TD>
    </TR>
    <TR>
      <TD height=166 valign="top" bgcolor="#FFFBFF"><br>
      <table width="80%" border='0' align="center" cellpadding='5' cellspacing='0' class="tableframe_gray">
        <tr>
          <td height=20 align="left"><lt:Label res="res.label.forum.admin.forum_m" key="notice"/></td>
          </tr>
        <tr>
          <td valign="top"><table width="100%" border='0' align="center" cellpadding='0' cellspacing='0'>
              <tr >
                <td width="100%">                    <tr>
                      <FORM METHOD=POST ACTION="?op=setNotice" name="form1">
                        <td height="23" colspan=3 align="center"><table width="100%">
                          <tr>
                            <td width="32%" height="22"><input type=text value="<%=fd.getNotices()%>" name="notices" style='border:1pt solid #636563;font-size:9pt' size=40>
                            </td>
                            <td width="68%"><input type="submit" value="<lt:Label key="ok"/>">
&nbsp;&nbsp;&nbsp;
                            <input type="button" value="<lt:Label res="res.label.forum.admin.forum_m" key="cancel_notice"/>" onClick="window.location.href='forum_notice.jsp?op=setNotice&noticeMsgId=-1'">
                            &nbsp;&nbsp;
                            <input type="button" value="<lt:Label res="res.label.forum.admin.forum_m" key="sel_topic"/>" onClick="openSelTopicWin()"></td>
                          </tr>
                          <tr>
                            <td height="22" colspan="2" align="left"><%
							Vector v = fd.getAllNotice();
							int nsize = v.size();
							if (nsize==0)
								out.print(SkinUtil.LoadString(request, "res.label.forum.admin.forum_m", "no_notice"));
							else {
								for (int k=0; k<nsize; k++) {
									MsgDb md = (MsgDb)v.get(k);
									String color = StrUtil.getNullString(md.getColor());
									String tp = md.getTitle();
									if (!color.equals(""))
										tp = "<font color='" + color + "'>" + tp + "</font>";
									if (md.isBold())
										tp = "<B>" + tp + "</B>";
									%>
									  <img src="../../images/arrow.gif">&nbsp;<a href="../showtopic.jsp?rootid=<%=md.getId()%>"><%=tp%></a>&nbsp;&nbsp;[<a href="javascript:delNotice('<%=md.getId()%>')"><lt:Label key="op_del"/></a>]
									  <%if (k!=0) {%>
									&nbsp;&nbsp;[<a href="javascript:up('<%=md.getId()%>')">
									<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/>
									</a>]
									<%}%>
									<%if (k!=nsize-1) {%>
									&nbsp;&nbsp;[<a href="javascript:down('<%=md.getId()%>')">
									<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/>
									</a>]
									<%}%>
									<BR>
                            <%	}
							}%></td>
                          </tr>
                        </table></td>
                      </FORM>
                    </tr>
          </TABLE></td>
        </tr>
        </table>
      <br></TD>
    </TR>
  </TBODY>
</TABLE>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>  
<script>
function form5_onsubmit() {
	form5.reason.value = getHtml();
	if (form5.reason.value.length>3000) {
		alert('<lt:Label res="res.label.forum.admin.forum_m" key="too_long"/>' + 3000);
		return false;
	}
}
</script>                                      
</html>                            
  