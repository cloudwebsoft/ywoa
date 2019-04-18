<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);

String userName = privilege.getUser(request);
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor" >
<div class="portlet_content" style="margin:0px; padding:0px">

    <div id="drag_<%=id%>_h" class="box">
    	<span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="cms/plugin/wiki/admin/wiki_doc_edit_rank.jsp">wiki编辑排行</a></span>
        
        <div class="opbut-1"><img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>
        <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>
        <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>
       </div>   

<table id="drag_<%=udsd.getId()%>_c" class="tabStyle_1" style="width:100%" cellSpacing="0"  align="center">
  <thead>
    <tr>
      <td noWrap width="68%">文章</td>
      <td noWrap width="21%">作者</td>
      <td width="11%" noWrap>编辑次数</td>
      </tr>
  </thead>
  <tbody>
<%
java.util.Date date = new java.util.Date();
WikiStatistic st = new WikiStatistic();
int[][] ary = st.getRankDocEditCountMonth(date, udsd.getCount());

com.redmoon.oa.fileark.DocumentMgr dm = new com.redmoon.oa.fileark.DocumentMgr();
com.redmoon.oa.fileark.Document doc = null;
for (int i = 0; i < ary.length; i++) {
	int docId = ary[i][0];
	if (docId == 0) // 行数不足row
		break;
	doc = dm.getDocument(docId);
	if (doc==null)
		continue;
	int count = ary[i][1];
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td>&nbsp;<a href="<%=request.getContextPath()%>/wiki_show.jsp?id=<%=docId%>" target="_blank"><%=doc.getTitle()%></a></td>
      <td align="center"><%=doc.getNick()%></td>
      <td align="center"><%=ary[i][1]%></td>
      </tr>
<%}%>
  </tbody>
</table>

</div>
</div>
