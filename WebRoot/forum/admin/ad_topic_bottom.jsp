<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="ad_manage"/></title>
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

function up(id) {
	var ntc = form1.advertise.value;
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
	form1.advertise.value = ntc;
	form1.submit();
}

function down(id) {
	var ntc = form1.advertise.value;
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
	form1.advertise.value = ntc;
	form1.submit();
}

function delAD(id) {
	var ntc = form1.advertise.value;
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
	form1.advertise.value = ntc;
	form1.submit();
}

function selTopic(ids) {
	// 检查在advertise中是否已包含了ids中的id，避免重复加入
	var ary = ids.split(",");
	var ntc = form1.advertise.value;
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
	form1.advertise.value = ntc;
}
//-->
</script>
<link href="../common.css" rel="stylesheet" type="text/css">
<LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<%
ForumDb fd = new ForumDb();
fd = fd.getForumDb();
String op = ParamUtil.get(request, "op");
if (op.equals("setAdvertise")) {
	String ids = ParamUtil.get(request, "advertise");
	fd.setAdTopicBottom(ids);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

fd = ForumDb.getInstance();
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="ad_manage"/></td>
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
<TABLE class="frame_gray"  
cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF" class="thead"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="ad_setup"/></TD>
    </TR>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF"><br>
      <table width="60%" border='0' align="center" cellpadding='0' cellspacing='0' class="tableframe_gray">
        <tr>
          <td height=20 align="left">&nbsp;&nbsp;<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="ad_desc"/></td>
          </tr>
        <tr>
          <td valign="top"><table width="100%" border='0' align="center" cellpadding='0' cellspacing='0'>
              <tr >
                <td width="100%">                    <tr>
                      <FORM METHOD=POST ACTION="?op=setAdvertise" name="form1">
                        <td height="23" colspan=3 align="center"><table width="100%">
                          <tr>
                            <td width="44%" height="22" align="left"><input type=text value="<%=fd.getAdTopicBottom()%>" name="advertise" style='border:1pt solid #636563;font-size:9pt' size=40>                            </td>
                            <td><input type="submit" value="<lt:Label key="ok"/>">
&nbsp;&nbsp;&nbsp;
                            <input type="button" value="<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="sel_topic"/>" onClick="openSelTopicWin()"></td>
                          </tr>
                          <tr>
                            <td colspan="2" align="left"><%
							Vector v = fd.getAllAdTopicBottom();
							int nsize = v.size();
							if (nsize==0)
								out.print(SkinUtil.LoadString(request, "res.label.forum.admin.ad_topic_bottom", "no_ad"));
							else {
								for (int k=0; k<nsize; k++) {
									MsgDb md = (MsgDb)v.get(k);
									String color = StrUtil.getNullString(md.getColor());
									if (color.equals("")) {%>
                              			<img src="../../images/arrow.gif">&nbsp;<a href="../showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>&nbsp;&nbsp;[<a href="javascript:delAD('<%=md.getId()%>')"><lt:Label key="op_del"/></a>]
									  <%if (k!=0) {%>
									  	&nbsp;&nbsp;[<a href="javascript:up('<%=md.getId()%>')"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/></a>]
									  <%}%>
									  <%if (k!=nsize-1) {%>
							  			&nbsp;&nbsp;[<a href="javascript:down('<%=md.getId()%>')"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/></a>]
							  		<%}%>
							  <BR>
                              <%}else{%>
								  <img src="../../images/arrow.gif">&nbsp;<a href="../showtopic.jsp?rootid=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>&nbsp;&nbsp;[<a href="javascript:delAD('<%=md.getId()%>')"><lt:Label key="op_del"/></a>]
								  <%if (k!=0) {%>
									&nbsp;&nbsp;[<a href="javascript:up('<%=md.getId()%>')"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/></a>]
									<%}%>
									<%if (k!=nsize-1) {%>
									&nbsp;&nbsp;[<a href="javascript:down('<%=md.getId()%>')"><lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/></a>]
									<%}%>
									<BR>
								  <%}%>
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
<br>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>  
</html>                            
  