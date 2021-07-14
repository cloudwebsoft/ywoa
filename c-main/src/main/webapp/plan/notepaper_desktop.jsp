<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

String op = ParamUtil.get(request, "op");
if (op.equals("close")) {
	int id = ParamUtil.getInt(request, "id");
	PlanDb pd = new PlanDb();
	pd = pd.getPlanDb(id);
	pd.setClosed(true);
	pd.save();
	out.print("true");
	return;
}

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();
String kind = ParamUtil.get(request, "kind"); // 是否来自于lte界面
%>
<style>
</style>
<div id="drag_<%=id%>" class="portlet drag_div bor ibox" >
  <div id="drag_<%=id%>_h" class="box ibox-title">
  		<!--<div class="notebg-titlebg">-->
	    <!--    <div class="icobox-1"><span class="notepaperAddBtn" title="添加便笺" onclick="addTab('添加便笺', '<%=request.getContextPath()%>/plan/plan_add.jsp?action=addNotepaper')"><img src="<%=SkinMgr.getSkinPath(request)%>/images/note-add.png" width="19" height="19" /></span> -->     
	    <!--    </div>-->
	    <!--    <span>便签</span>-->
	    <!--    <div class="icobox-3"><span class="notepaperCloseBtn">   -->     
	    <!--    	<img onclick="clo('<%=udsd.getId()%>')" title="关闭" src="<%=SkinMgr.getSkinPath(request)%>/images/note-close.png" align="absmiddle" valign="top" width="19" height="19"/>&nbsp;-->
	    <!--    </span></div>-->
		<!--	<div class="icobox-2">-->
	    <!--    <span class="notepaperMoreBtn" title="日程" onclick="addTab('日程', '<%=request.getContextPath()%>/plan/plan_list.jsp')"><img src="<%=SkinMgr.getSkinPath(request)%>/images/note-more.png" width="19" height="19" /></span>-->
	    <!--    </div>-->
        <!--</div>-->
	  	<%
			if ("lte".equals(kind)) {
		%>
	  	<h5>
			<i class="fa <%=udsd.getIcon()%>"></i>&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/plan/notepaper.jsp"><%=udsd.getTitle()%></a>
		</h5>
	  	<%
			}
			else {
	  	%>
        <div class="titleimg">
        <!--<img src="images/desktop/notepaper.png" width="40" height="40" />-->
        <i class="fa <%=udsd.getIcon()%>"></i>
        &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/plan/notepaper.jsp"><%=udsd.getTitle()%></a></div>
	  	<%
			}
	  	%>
  	</div>
  <div class="article ibox-content">
		<% 
		PlanDb pd = new PlanDb();
		String sql = "select id from " + pd.getTableName() + " where userName=" + StrUtil.sqlstr(privilege.getUser(request)) + " and is_notepaper=1 and is_closed=0 order by myDate desc";
		Iterator ir = pd.listResult(sql, 1, count).getResult().iterator();
    	if(ir.hasNext()){%>
        <table class='article_table'>
  	  <%
  	 	while (ir.hasNext()) {
  	 		pd = (PlanDb)ir.next();%>	
  	 	  <!--<div style="clear:both;padding-left:10px;padding-right:10px;"  id="notepaper<%=pd.getId()%>" onmousemove="o('notepaperImg<%=pd.getId()%>').style.display=''" onmouseout="o('notepaperImg<%=pd.getId()%>').style.display='none';">-->
             <!--<a href="javascript:;" onclick="addTab('便笺', '<%=request.getContextPath()%>/plan/plan_show.jsp?id=<%=pd.getId()%>')"><%=pd.getTitle()%></a>-->
             <!--<img id="notepaperImg<%=pd.getId()%>" title="置为已完成" style="display:none; cursor:pointer" src="<%=request.getContextPath()%>/images/tick.png" onclick="setClosed(<%=pd.getId()%>)" />-->
             <!--<%=DateUtil.format(pd.getMyDate(), "MM-dd HH:mm")%>&nbsp;-->
             <!--</div>-->
            
            <tr>
            <td class='article_content' id="notepaper<%=pd.getId()%>" onmousemove="o('notepaperImg<%=pd.getId()%>').style.display=''" onmouseout="o('notepaperImg<%=pd.getId()%>').style.display='none';">
            	<a href="javascript:;" onclick="addTab('便笺', '<%=request.getContextPath()%>/plan/plan_show.jsp?id=<%=pd.getId()%>')"><%=pd.getTitle()%></a>
            	<img id="notepaperImg<%=pd.getId()%>" title="置为已完成" style="display:none; cursor:pointer" src="<%=request.getContextPath()%>/images/tick.png" onclick="setClosed(<%=pd.getId()%>)" />
            </td>
            <td class='article_time' id="notepaperTime<%=pd.getId()%>">[<%=DateUtil.format(pd.getMyDate(), "yyyy-MM-dd")%>]</td>
            </tr>
  	 	<%}%>
    	</table>	
    	<%}else{%>
    		<div class='no_content'><img title='暂无便笺' src='images/desktop/no_content.jpg'></div>
    	<%}
    %>
		
  </div>
</div>
<script>
var curPlanClosedId;

var errFunc = function(response) {
	alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function doClosed(response){
	var rsp = response.responseText.trim();
	if (rsp.trim()=="true") {
		o("notepaper" + curPlanClosedId).style.display = "none";
		o("notepaperTime" + curPlanClosedId).style.display = "none";
	}
	;
}

function setClosed(planId) {
	curPlanClosedId = planId;
	var params = "op=close&id=" + planId;
	var myAjax = new cwAjax.Request( 
		"<%=request.getContextPath()%>/plan/notepaper_desktop.jsp",
		{
			method:"post",
			parameters:params,
			onComplete:doClosed,
			onError:errFunc
		}
	);
	return;
}
</script>