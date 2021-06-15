<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.refer.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
<TBODY>
      <TR>
        <TD height="23" align="left" bgcolor="#F9FAF3">请选择您的贴子谁可以看见：
		  <input name="secretLevel" value="<%=ReferDb.SECRET_LEVEL_PUBLIC%>" type="radio" checked />
		  大家
		  <input name="secretLevel" value="<%=ReferDb.SECRET_LEVEL_MANAGER%>" type="radio" />
		  版主
		</TD>
      </TR>
  </TBODY>
</TABLE>
