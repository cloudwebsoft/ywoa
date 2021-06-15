<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.debate.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<table width="98%">
  <tr>
    <td>选择支持哪一方：
	  <select name="viewpoint_type">
	    <option value="<%=DebateViewpointDb.TYPE_SUPPORT%>">正方</option>
	    <option value="<%=DebateViewpointDb.TYPE_OPPOSE%>">反方</option>
	    <option value="<%=DebateViewpointDb.TYPE_OTHERS%>">第三方</option>
        </select>
	  <input type="hidden" name="pluginCode" value="<%=DebateUnit.code%>" /></td>
  </tr>
</table>
<%
String viewpoint_type = ParamUtil.get(request, "viewpoint_type");
if (!viewpoint_type.equals("")) {
%>
<script>
frmAnnounce.viewpoint_type.value = "<%=viewpoint_type%>";
</script>
<%
}
%>
