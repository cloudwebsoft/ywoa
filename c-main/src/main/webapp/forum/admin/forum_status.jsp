<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.Date"%>
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
if (op.equals("setStatus")) {
	int status = ParamUtil.getInt(request, "status");
	String reason = ParamUtil.get(request, "reason");
	fd.setStatus(status);
	fd.setReason(reason);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}

fd = ForumDb.getInstance();
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.forum_m" key="status"/></td>
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
      <TD valign="top" bgcolor="#FFFBFF" class="thead"><lt:Label res="res.label.forum.admin.forum_m" key="status"/></TD>
    </TR>
    <TR>
      <TD height=200 valign="top" bgcolor="#FFFBFF"><br>
        <br>
        <FORM METHOD=POST ACTION="?op=setStatus" name="form5" onSubmit="return form5_onsubmit()">
          <table width="67%" align="center" class="tableframe_gray">
            <tr>
              <td width="43%" height="26"><lt:Label res="res.label.forum.admin.forum_m" key="status"/>
                  <select name="status">
                    <option value="<%=ForumDb.STATUS_NORMAL%>">
                    <lt:Label res="res.label.forum.admin.forum_m" key="start"/>
                    </option>
                    <option value="<%=ForumDb.STATUS_STOP%>">
                    <lt:Label res="res.label.forum.admin.forum_m" key="stop"/>
                    </option>
                  </select>
                  <script>
						form5.status.value = "<%=fd.getStatus()%>"
						</script>
                  <textarea id="reason" name="reason" style="display:none"><%=fd.getReason().replaceAll("\"","'")%></textarea></td>
            </tr>
            <tr>
              <td height="22"><%
String rpath = request.getContextPath();
%>
                  <link rel="stylesheet" href="<%=rpath%>/editor/edit.css">
                  <script src="<%=rpath%>/editor/DhtmlEdit.js"></script>
                  <script src="<%=rpath%>/editor/editjs.jsp"></script>
                  <script src="<%=rpath%>/editor/editor_s.jsp"></script>
                  <script>
setHtml(form5.reason);
                          </script></td>
            </tr>
            <tr>
              <td height="22" align="center"><input name="submit2" type="submit" value="<lt:Label key="ok"/>"></td>
            </tr>
          </table>
        </FORM>
        <br>
      <br>
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
  