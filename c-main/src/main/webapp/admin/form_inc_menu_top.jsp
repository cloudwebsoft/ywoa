<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%
String flowTypeCodeTop = ParamUtil.get(request, "flowTypeCode");
if (flowTypeCodeTop.equals(""))
	flowTypeCodeTop = ParamUtil.get(request, "parent_code");
if (flowTypeCodeTop.equals(""))
	flowTypeCodeTop = ParamUtil.get(request, "code");
String isFlowTop = ParamUtil.get(request, "isFlow");
Privilege pvgTop = new Privilege(); 
com.redmoon.oa.flow.LeafPriv lpTop = new com.redmoon.oa.flow.LeafPriv(flowTypeCodeTop);
%>
<div id="tabs1">
  <ul>
    <li id="menu1">
    <%if (!"".equals(flowTypeCodeTop)) { %>
    <a href="form_m.jsp?flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>"><span>流程表单</span></a>
    <%}else{ %>
    <a href="javascript:switchList(1)"><span>流程表单</span></a>
    <%} %>
    </li>
    <!--<li id="menu2"><a href="form_add.jsp?isFlow=<%=isFlowTop%>&flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>"><span>添加表单</span></a></li>-->
<%if (!isFlowTop.equals("0") && !flowTypeCodeTop.equals("")) {%>
	<%if (!flowTypeCodeTop.equals(com.redmoon.oa.flow.Leaf.CODE_ROOT)) {%>	
    <li id="menu5"><a href="flow_predefine_dir.jsp?op=modify&code=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>"><span>属性</span></a></li>	
	<%}%>
    <%if (lpTop.canUserExamine(pvgTop.getUser(request))) {%>
    <li id="menu4"><a href="flow_dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>"><span>权限</span></a></li>
    <%}%>
    <li id="menu3"><a href="flow_predefine_dir.jsp?parent_code=<%=StrUtil.UrlEncode(flowTypeCodeTop)%>&op=AddChild"><span>添加</span></a></li>
<%}%>
<%if ("".equals(flowTypeCodeTop)) {%>
    <li id="menu3"><a href="javascript:switchList(0)"><span>模块表单</span></a></li>
<%}%>
<%if (flowTypeCodeTop.equals(com.redmoon.oa.flow.Leaf.CODE_ROOT) && pvgTop.isUserPrivValid(request, "admin")) {%>
    <li id="menu3"><a href="flow_list.jsp?op=search&by=title"><span>流程列表</span></a></li>
<%}%>
</ul>
</div>
<script>
function switchList(isFlow) {
	o("isFlow").value = isFlow;
	o("searchForm").submit();	
}
</script>

