<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.cloudweb.oa.utils.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.visual.FormUtil" %>
<%
	com.alibaba.fastjson.JSONArray aryTop = ModuleUtil.renderTabsBack(request);
    if (aryTop.size() > 1) {
%>
<div class="tabs1Box">
	<div id="tabs1">
		<ul>
			<%
				for (Object jsonObject : aryTop) {
					com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject)jsonObject;
					String tagNameTop = "", target = "";
					if (json.containsKey("tagName")) {
                        tagNameTop = json.getString("tagName");
					}
					String tabName = json.getString("tabName");
					if (StrUtil.isEmpty(tabName)) {
					    tabName = json.getString("name");
                    }
					if (json.containsKey("target")) {
					    target = json.getString("target");
                    }
			%>
					<li id="<%=json.getString("id")%>" tagName="<%=tagNameTop%>">
                        <%
                            if ("newTab".equals(target)) {
                        %>
                        <a href="javascript:;" title="<%=tabName%>" onclick="addTab('<%=tabName%>', '<%=json.getString("url")%>')"><span><%=tabName%></span></a>
                        <%
                            }
                            else {
                        %>
						<a href="<%=json.getString("url")%>" title="<%=tabName%>"><span><%=tabName%></span></a>
                        <%
                            }
                        %>
					</li>
			<%
				}
			%>
		</ul>
	</div>
</div>
<%
    }
%>