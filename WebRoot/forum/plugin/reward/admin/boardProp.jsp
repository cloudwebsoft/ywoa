<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../../../inc/inc.jsp" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.SkinUtil"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.reward.*"%>
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
<script language="JavaScript">
<!--

//-->
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String boardCode = ParamUtil.get(request, "boardCode");
if (boardCode.equals("")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "want") + "boardCode"));
	return;
}
String code = RewardUnit.code;
BoardDb sb = new BoardDb();
sb = (BoardDb)sb.getBoardDb(code, boardCode);

Leaf leaf = new Leaf();
leaf = leaf.getLeaf(boardCode);

PluginMgr pm = new PluginMgr();
PluginUnit pu = pm.getPluginUnit(code);

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	RewardSkin sv = new RewardSkin();
	String boardRule = ParamUtil.get(request, "boardRule");
	try {
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
		com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, pvg, "boardRule", boardRule, getClass().getName());
		// 防XSS
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "boardRule", boardRule, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	if (!cn.js.fan.db.SQLFilter.isValidSqlParam(boardRule)) {
		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ forum/plugin/project/admin/boardProp.jsp boardRule=" + boardRule);
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
		return;
	}	
	
	sb.setBoardRule(boardRule);
	if (sb.save())
		out.print(StrUtil.Alert(sv.LoadString(request, "modifySucceed")));
	else
		out.print(StrUtil.Alert(sv.LoadString(request, "modifyFail")));
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理插件-<a href=manager.jsp><%=RewardSkin.LoadString(request, "name")%></a>的规则</td>
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
      <td width="75%" height="22" align="left"><%=leaf.getCode()%><input type="hidden" name="boardCode" value="<%=boardCode%>"></td>
        </tr>
      <tr align="center">
        <td height="22">版面名称</td>
      <td height="22" align="left"><%=leaf.getName()%></td>
        </tr>
      <tr align="center">
        <td height="22">版面规则</td>
        <td height="22" align="left">
        <textarea name="boardRule" cols="78" rows="10">
		<%=com.cloudwebsoft.framework.security.AntiXSS.clean(sb.getBoardRule())%>
        </textarea>
        </td>
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
