<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<title><lt:Label res="res.forum.plugin.group" key="group_create"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("create")) {
	GroupDb gd = new GroupDb();
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.create(request, gd, "plugin_group_create")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.waitJump(SkinUtil.LoadString(request, "info_op_success"), 3, "group.jsp?id=" + gd.getLong("id")));
			%>
			<br />
			<br />
			<br /><br />
			<br />
			<br />
			</div>
			<%@ include file="../../inc/footer.jsp"%>
			</div>
			<%
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
  <TABLE cellSpacing=0 cellPadding=1 rules=rows width="98%" class="tableCommon">
  <TBODY>
  <TR>
        <TD height=25>&nbsp;<img src="../../images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/>
        </a>&nbsp;<B>&raquo;</B> 
        <a href="group_list.jsp">朋友圈</a>&nbsp;</TD>
        </TR>
  </TBODY></TABLE>
  <br>
  <form name="form1" action="group_create.jsp?op=create" method="post">
  <table class="tableCommon" width="98%" border="1" cellpadding="0" cellspacing="0">
    <thead>
      <td colspan="2" align="center" class="td_title"><a href="<%=request.getContextPath()%>/forum/index.jsp">
        <lt:Label res="res.forum.plugin.group" key="group_create"/>
      </a></td>
    </thead>
    <tr>
      <td height="24" colspan="2"><%
	  String scoreDesc = SkinUtil.LoadString(request, "res.forum.plugin.group", "group_create_score");
	  GroupConfig gc = GroupConfig.getInstance();
	  String scoreCode = gc.getProperty("create_money_code");
	  if (!scoreCode.equals("")) {
	  	ScoreMgr sm = new ScoreMgr();
		ScoreUnit su = sm.getScoreUnit(scoreCode);
		out.print(StrUtil.format(scoreDesc, new Object[] {gc.getProperty("create_money_sum"), su.getName(request)}));
	  }%>
	  &nbsp;用户最多能创建<%=gc.getProperty("max_group_create")%>个圈子
	  </td>
      </tr>
    <tr>
      <td width="11%">名称</td>
      <td width="89%"><input name="name"></td>
    </tr>
    <tr>
      <td>描述</td>
      <td><textarea name="description" rows="5" cols="50"></textarea></td>
    </tr>
    <tr>
      <td>分类</td>
      <td>
	  <select name="catalog_code">
	  	  <%
		  com.redmoon.forum.plugin.group.LeafChildrenCacheMgr lcc = new com.redmoon.forum.plugin.group.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
		  Iterator ir = lcc.getDirList().iterator();
		  while (ir.hasNext()) {
		  	com.redmoon.forum.plugin.group.Leaf lf = (com.redmoon.forum.plugin.group.Leaf)ir.next();
		  %>
		  	<option value="<%=lf.getCode()%>"><%=lf.getName()%></option>
		  <%
		  }
		  %>	  
	  </select>
	  <input name="creator" type="hidden" value="<%=privilege.getUser(request)%>">
	  </td>
    </tr>
    <tr>
      <td height="28" colspan="2" align="center"><input type="submit" value="确定"></td>
    </tr>
  </table></form>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</BODY></HTML>
