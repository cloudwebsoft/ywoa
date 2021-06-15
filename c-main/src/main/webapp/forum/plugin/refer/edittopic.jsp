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
<%
long msgId = ParamUtil.getLong(request, "editid");
ReferDb rd = new ReferDb();
rd = rd.getReferDb(msgId);
if (!rd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不是咨询贴！"));
}
MsgDb md = new MsgDb();
md = md.getMsgDb(msgId);
%>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
<TBODY>
      <TR>
        <TD height="23" align="left" bgcolor="#F9FAF3">请选择您的贴子谁可以看见：
		  <input name="secretLevel" value="<%=ReferDb.SECRET_LEVEL_PUBLIC%>" type="radio" <%=rd.getSecretLevel()==ReferDb.SECRET_LEVEL_PUBLIC?"checked":""%> />
		  大家
		  <%if (md.isRootMsg()) {%>
		  <input name="secretLevel" value="<%=ReferDb.SECRET_LEVEL_MANAGER%>" type="radio" <%=rd.getSecretLevel()==ReferDb.SECRET_LEVEL_MANAGER?"checked":""%> />
		  版主
		  <%}else{%>
		  <input name="secretLevel" value="<%=ReferDb.SECRET_LEVEL_MSG_OWNER%>" type="radio" <%=rd.getSecretLevel()==ReferDb.SECRET_LEVEL_MSG_OWNER?"checked":""%> />
		  楼主
		  <%}%>
		</TD>
      </TR>
  </TBODY>
</TABLE>
