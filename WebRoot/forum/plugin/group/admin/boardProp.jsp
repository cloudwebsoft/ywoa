<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../inc/inc.jsp" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.SkinUtil"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.Leaf"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../../common.css">
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>版块规则管理</title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String boardCode = ParamUtil.get(request, "boardCode");
if (boardCode.equals("")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "want") + "boardCode"));
	return;
}

String code = ParamUtil.get(request, "code");

BoardDb sb = new BoardDb();
sb = sb.getBoardDb(code, boardCode);

Leaf leaf = new Leaf();
leaf = leaf.getLeaf(boardCode);

PluginMgr pm = new PluginMgr();
PluginUnit pu = pm.getPluginUnit(code);

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	String boardRule = ParamUtil.get(request, "boardRule");
	sb.setBoardRule(boardRule);
	if (sb.save())
		out.print(StrUtil.Alert("修改成功！"));
	else
		out.print(StrUtil.Alert("修改失败！"));
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理插件-<a href=manager.jsp><%=SkinUtil.LoadString(request, "res.forum.plugin.group", "name")%></a>的规则</td>
  </tr>
</table>
<br>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">&nbsp;</td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="90%"  border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFBFF" class="tableframe_gray">
	  <form action="?op=modify" method="post">
      <tr align="center">
        <td width="25%" height="22">版面编码</td>
      <td width="75%" height="22" align="left"><%=leaf.getCode()%>
	  <input type="hidden" name="boardCode" value="<%=boardCode%>">
	  <input type="hidden" name="code" value="<%=code%>">
	  </td>
        </tr>
      <tr align="center">
        <td height="22">版面名称</td>
      <td height="22" align="left"><%=leaf.getName()%></td>
        </tr>
      <tr align="center">
        <td height="22">版面规则</td>
        <td height="22" align="left"><textarea name="boardRule" cols="78" rows="20"><%=sb.getBoardRule()%></textarea></td>
        </tr>
      <tr align="center">
        <td height="22" colspan="2"><input type="submit" name="Submit" value="提交">
&nbsp;&nbsp;
          <input type="reset" name="Submit" value="重置"></td>
        </tr></form>
    </table>
      <br>
    </td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
