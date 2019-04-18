<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
long flowId = ParamUtil.getLong(request, "flowId");
long actionId = ParamUtil.getLong(request, "actionId");

String sql = "select id from flow_my_action where flow_id=" + flowId + " and is_checked<>" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date desc";
MyActionDb mad = new MyActionDb();
Vector v = mad.list(sql);
Iterator ir = v.iterator();
Map map = new HashMap();
WorkflowActionDb wa = new WorkflowActionDb();

wa = wa.getWorkflowActionDb((int)actionId);
Vector vcfrom = wa.getLinkFromActions();

// 取得第一个对应的节点处理记录
long firstMyActionId = -1;
while (ir.hasNext()) {
	mad = (MyActionDb)ir.next();
	if (mad.getActionId()==actionId)
		firstMyActionId = mad.getId();
}

boolean isFound = false;

ir = v.iterator();
String checked = "";
if (v.size()==1)
	checked = "checked";
while (ir.hasNext()) {
	mad = (MyActionDb)ir.next();

	long aId = mad.getActionId();
	wa = wa.getWorkflowActionDb((int)aId);
	
	// 是否为兄弟节点
	boolean isSibling = false;
	Vector vtfrom = wa.getLinkFromActions();
	for (int i = 0; i < vcfrom.size(); i++) {
		WorkflowActionDb action1 = (WorkflowActionDb) vcfrom.get(i);
		for (int j = 0; j < vtfrom.size(); j++) {
			WorkflowActionDb action2 = (WorkflowActionDb) vtfrom.get(j);
			if (action1.getId() == action2.getId()) {
				isSibling = true;
				break;
			}
		}
	}
	
	if (isSibling) {
		// 如果是兄弟节点，但还需再判断一下从该节点至当前节点有没有连线，如果有，则应允许返回至该兄弟节点
		// 比如从节点2与节点3是兄弟节点，但节点2至节点3月连线，则节点3返回时，应可返回至节点2
		// ----[ 节点1  ]-|-------------------|---[ 节点3  ]-----
		//                |-----[ 节点2  ]----|
		boolean hasLinkFromSibling = false;
		for (int i = 0; i < vcfrom.size(); i++) {
			WorkflowActionDb action1 = (WorkflowActionDb) vcfrom.get(i);
			if (action1.getId()==wa.getId()) {
				hasLinkFromSibling = true;
				break;
			}
		}
		if (!hasLinkFromSibling) {
			continue; 
		}
	}
	
	// 20180109 注释掉，不再防止用户重复
	/*
	if (map.get(mad.getUserName())==null) {
		map.put(mad.getUserName(), mad.getUserName());
	}
	else
		continue;
	*/
	
	if (map.get("" + aId)!=null)
		continue;
		
	/*
	if (mad.getId()==firstMyActionId) {
		isFound = true;
		continue;
	}
	// 在本节点之前处理的节点才能被返回
	if (!isFound)
		continue;
	*/
	map.put("" + aId, "" + aId);
	
	// System.out.println(getClass() + " v.size()=" + v.size() + " checked=" + checked);
%>
<div>
   <input type="radio" name="returnId" value="<%=wa.getId()%>" <%=checked%> />
   <!--
   <%if (!wa.getJobName().equals("")) {%>
   <%=wa.getJobName()%>：
   <%}%>
   -->
   <%=wa.getTitle()%>：
   <%=wa.getUserRealName()%>
   &nbsp;&nbsp;<%=DateUtil.format(mad.getCheckDate(), "yyyy-MM-dd HH:mm")%>
</div>   
<%	
}
%>
