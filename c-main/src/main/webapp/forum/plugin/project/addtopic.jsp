<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.project.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.reward.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
<TBODY>
    <TR>
  <TD height="23" align="left" bgcolor="#F9FAF3">项目：
    <%
  String boardcode = ParamUtil.get(request, "boardcode");
  
  long projectId = -1;
  String projectName = "";
  if (boardcode.startsWith(ProjectChecker.CODE_PREFIX)) {
  	projectId = StrUtil.toLong(boardcode.substring(ProjectChecker.CODE_PREFIX.length()));
	
	com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();
	com.redmoon.oa.flow.FormDb fd = fm.getFormDb("project");
	
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);
	projectName = fdao.getFieldValue("name");
  }
  %>
    <%=projectName%>
    <input name="projectId" value="<%=projectId%>" type="hidden" />  </TD>
  </TR>
  </TBODY>
</TABLE>
