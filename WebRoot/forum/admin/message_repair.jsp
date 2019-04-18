<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>修复</title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("repair")) {
	long id = ParamUtil.getLong(request, "msgId", -1);
	if (id==-1) {
		out.print(StrUtil.Alert_Back("请输入贴子编号！"));
		return;
	}
	MsgDb md = new MsgDb();
	md = md.getMsgDb(id);
	if (!md.isLoaded()) {
		out.print(StrUtil.Alert_Back("贴子不存在！"));
		return;
	}
	if (md.isRootMsg()) {
		String sql = "select count(*) from sq_message where rootid=" + id + " and check_status=" + MsgDb.CHECK_STATUS_PASS;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			int c = rr.getInt(1)-1;
			md.setRecount(c);
			if (md.save()) {
				out.print(StrUtil.Alert_Redirect("操作成功，回复数为" + c + "！", "message_repair.jsp"));
				return;
			}
			else {
				out.print(StrUtil.Alert_Back("操作失败！"));
			}
		}
	}
	else {
		out.print(StrUtil.Alert_Back("您输入的贴子编号对应的不是主题贴！"));
		return;
	}
}
else if (op.equals("repairTag")) {
	long id = ParamUtil.getLong(request, "msgId", -1);
	if (id==-1) {
		out.print(StrUtil.Alert_Back("请输入贴子编号！"));
		return;
	}	
	MsgDb md = new MsgDb();
	md = md.getMsgDb(id);
	if (!md.isLoaded()) {
		out.print(StrUtil.Alert_Back("贴子不存在！"));
		return;
	}
	md.setContent(StrUtil.fillHtmlTag(md.getContent()));
	md.save();
	out.print(StrUtil.Alert_Redirect("操作成功！", "message_repair.jsp"));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">修复</td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">修复贴子</td>
  </tr>  
  <tr> 
    <td valign="top"><br>
      <table width="99%"  border="0" align="center" cellpadding="3" cellspacing="1">
      <form name="form1" method="post" action="message_repair.jsp?op=repair">
	  <tr align="center">
        <td height="24">修复主题贴的回复数，请输入贴子编号ID：&nbsp;&nbsp;
          <input name="msgId"><input type=submit value="确定"></td>
      	</tr></form>
    </table>
      <br>
      <table width="99%"  border="0" align="center" cellpadding="3" cellspacing="1">
        <form name="form1" method="post" action="message_repair.jsp?op=repairTag">
          <tr align="center">
            <td height="24">补齐贴子中的内容标签，请输入贴子编号ID：&nbsp;&nbsp;
                <input name="msgId">
            <input name="submit" type=submit value="确定"></td>
          </tr>
        </form>
      </table>
      <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  