<%@page import="oracle.jdbc.driver.SQLUtil"%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.project.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

int projectId = ParamUtil.getInt(request, "projectId");
// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + projectId);
// 置嵌套表需要用到的页面类型
request.setAttribute("pageType", "show");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", formCode);

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
String userName = privilege.getUser(request);

UserMgr um = new UserMgr(); 
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-显示内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
<script>
function setradio(myitem,v) {
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}

//--------------展开任务----------------------------
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImg" + t_id);
	var targetTR2 =eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="../forum/images/minus.gif";
			if (targetImg2.getAttribute("loaded")=="no"){
				o("hiddenframe").contentWindow.location.replace("../task_tree.jsp?id="+b_id+getstr);
				// document.frames["hiddenframe"].location.replace("../task_tree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="../forum/images/plus.gif";
		}
	}
}
</script>
</head>
<body>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<br />
<iframe width=300 height=300 src="" id="hiddenframe" style="display:none"></iframe>
<div class="spacerH"></div>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" style="">
  <tr>
    <td width="50%" align="left" valign="top"><table width="99%" border="0" align="center" cellpadding="0" cellspacing="0" >
      <form name="visualForm" id="visualForm">
        <tr>
          <td >
		  <%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, projectId, fd);
			out.print(rd.report());
		  %>		  </td>
        </tr>
      </form>
    </table>
      <%
int curpage = 1;
int pagesize = 5;
int id = 0;	

com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);
Iterator ir = fdao.getAttachments().iterator();
				  while (ir.hasNext()) {
				  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next(); %>
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
            <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a><br />            </td>
          </tr>
        </table>
      <%}%>
      <input name="projectId" value="<%=projectId%>" type="hidden" />
      <table width="100%" style="display:none">
        <tr><td align="center"><input type="button" class="btn" onclick="showFormReport()" value="打印表单"/>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="button" class="btn" onclick="exportToWord()" value="导出至Word"/>
      </td></tr></table>
      <!--
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<%
ModulePrivDb mpd = new ModulePrivDb(formCode);
if (mpd.canUserManage(privilege.getUser(request))) {
%>
<input type="button" class="button1" onclick="window.open('module_edit.jsp?parentId=<%=projectId%>&id=<%=projectId%>&formCode=<%=formCode%>')" value="编 辑" />
<%}%>
	-->
</td>
    <td width="50%" align="left" valign="top">
    <%
	String sql = "select distinct p.id from work_plan p, work_plan_user u where p.project_id=" + projectId + " and u.workPlanId=p.id and u.userName=" + StrUtil.sqlstr(userName) + " order by p.id desc";
	
	// out.print(sql);
	
	WorkPlanDb wpd = new WorkPlanDb();
	ListResult lr = wpd.listResult(sql, curpage, pagesize);
	%>
	<table width="98%" border="0" align="center" cellspacing="0" class="tabStyle_1" style="margin-top:5px;">
	  <tr>
	    <td width="39%" class="tabStyle_1_title"><a href="javascript:;" onclick="addTab('项目计划', '<%=request.getContextPath()%>/project/project_workplan_list.jsp?projectId=<%=projectId%>')"><span style="color:#000">计划</span></a></td>
	    <td width="11%" class="tabStyle_1_title">拟定者        </td>
	    <td width="16%" class="tabStyle_1_title">进度        </td>
	    <td width="16%" class="tabStyle_1_title">开始日期        </td>
	    <td width="18%" class="tabStyle_1_title">结束日期          </td>
	    </tr>
	  <%
	    ir = lr.getResult().iterator();
		while (ir!=null && ir.hasNext()) {
			wpd = (WorkPlanDb)ir.next();
			id = wpd.getId();
			String sbeginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
			String sendDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
		%>
	  <tr class="highlight">
	    <td><a href="javascript:;" onclick="addTab('工作计划', '<%=request.getContextPath()%>/workplan/workplan_show.jsp?id=<%=id%>')"><%=wpd.getTitle()%></a></td>
	    <td align="center"><%=um.getUserDb(wpd.getAuthor()).getRealName()%></td>
	    <td align="center">
          <div class="progressBar">
            <div class="progressBarFore" style="width:<%=wpd.getProgress()%>%;">
              </div>
            <div class="progressText">
              <%=wpd.getProgress()%>%
              </div>
            </div>		
        </td>
	    <td align="center"><%=sbeginDate%></td>
	    <td align="center"><%=sendDate%></td>
	    </tr>
	  <%
		}
%>
    </table>    
    <%
	String title = "",initiator="",mydate="";
	int expression=0;
	int i = 0,type=0,recount=0,isfinish=0;	

	sql = "select distinct id from flow where status<>" + WorkflowDb.STATUS_NONE + " and project_id=" + projectId + " order by id desc"; 
	WorkflowDb wf = new WorkflowDb();

	lr = wf.listResult(sql, curpage, pagesize);
	ir = lr.getResult().iterator();
	%>
      <table width="100%" border="0" align="center" cellspacing="0" class="tabStyle_1 percent98">
        <tbody>
          <tr>
            <td width="58%" class="tabStyle_1_title"><a href="javascript:;" onclick="addTab('项目流程', '<%=request.getContextPath()%>/project/project_flow_list.jsp?projectId=<%=projectId%>')"><span style="color:#000">流程</span></a></td>
            <td width="19%" class="tabStyle_1_title">办理</td>
            <td width="23%" class="tabStyle_1_title">开始时间</td>
          </tr>
          <%
com.redmoon.oa.flow.Leaf ft = new com.redmoon.oa.flow.Leaf();
MyActionDb mad = new MyActionDb();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next(); 
	UserDb user = null;
	if (wfd.getUserName()!=null)
		user = um.getUserDb(wfd.getUserName());
	String userRealName = "";
	if (user!=null)
		userRealName = user.getRealName();	
	%>
          <tr class="highlight">
            <td><a href="javascript:;" onclick="addTab('<%=StrUtil.getLeft(wfd.getTitle(), 10)%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><%=StrUtil.getLeft(wfd.getTitle(), 40)%></a></td>
            <td align="center"><%
		sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " order by receive_date desc";
		Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
	  	if (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
		%>
                <%=um.getUserDb(mad.getUserName()).getRealName()%>
                <%
		}
	  %>            </td>
            <td align="center"><%=DateUtil.format(wfd.getBeginDate(), "yy-MM-dd HH:mm")%></td>
          </tr>
          <%}%>
        </tbody>
      </table>
      <%
	sql = "select class1,title,id,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,author from document where examine<>" + com.redmoon.oa.fileark.Document.EXAMINE_DUSTBIN;
	sql += " and (parent_code=" + StrUtil.sqlstr(ProjectChecker.CODE_PREFIX + projectId) + " or class1=" + StrUtil.sqlstr(ProjectChecker.CODE_PREFIX + projectId) + ")";
	sql += " order by doc_level desc, examine asc, modifiedDate desc";
	PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
	ResultIterator ri = pageconn.getResultIterator(sql);
	  %>
      <table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="0" width="100%" align="center">
        <tbody>
          <tr>
            <td class="tabStyle_1_title" width="58%" height="28" noWrap><a href="javascript:;" onclick="addTab('项目文档', '<%=request.getContextPath()%>/project/project_doc_list.jsp?projectId=<%=projectId%>')"><span style="color:#000">文档</span></a></td>
            <td class="tabStyle_1_title" width="19%" noWrap>作者</td>
        <td class="tabStyle_1_title" width="23%" noWrap>修改时间</td>
        </tr>
          <%
com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();		
com.redmoon.oa.fileark.Directory dir = new com.redmoon.oa.fileark.Directory();
while (ri.hasNext()) {
 	ResultRecord rr = (ResultRecord)ri.next(); 
	boolean isHome = rr.getInt("isHome")==1?true:false;
	String color = StrUtil.getNullStr(rr.getString("color"));
	boolean isBold = rr.getInt("isBold")==1;
	java.util.Date expireDate = rr.getDate("expire_date");
	doc = doc.getDocument(rr.getInt("id"));	
	UserDb ud = new UserDb(rr.getString("author"));
	%>
          <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
            <td height="24"><%if (rr.getInt("type")==1) {%>
              <IMG height=15 alt="" src="../forum/skin/bluedream/images/f_poll.gif" width=17 border=0>&nbsp;
              <%}%>
              <%if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
              <a href="javascript:;" onclick="addTab('项目文档', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=rr.getInt("id")%>')" title="<%=rr.getString(2)%>">
              <%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
		%>
              <font color="<%=color%>">
              <%}%>
              <%=rr.getString("title")%>
              <%if (!color.equals("")) {%>
              </font>
              <%}%>
              <%
		if (isBold)
			out.print("</B>");
		%>
              </a>
              <%}else{%>
              <a href="javascript:;" onclick="addTab('项目文档', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=rr.getInt("id")%>')" title="<%=rr.getString("title")%>"><%=rr.get("title")%></a>
            <%}%></td>
        <td align="center"><%=ud.getRealName()%></td>
        <td align="center"><%
	  java.util.Date d = rr.getDate("modifiedDate");
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yyyy-MM-dd HH:mm"));
	  %></td>
        </tr>
          <%}%>
        </tbody>
      </table>
    </td>
  </tr>
</table>
<form name="formWord" target="_blank" action="../visual/module_show_word.jsp" method="post">
<textarea name="cont" style="display:none"></textarea>
</form>
</body>
<script>
function showFormReport() {
	var preWin=window.open('preview','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
	preWin.document.open();
	preWin.document.write("<style>TD{ TABLE-LAYOUT: fixed; FONT-SIZE: 12px; WORD-BREAK: break-all; FONT-FAMILY:}</style>" + formDiv.innerHTML);
	preWin.document.close();
	preWin.document.title="表单";
	preWin.document.charset="UTF-8";
}

function exportToWord() {
	formWord.cont.value = formDiv.innerHTML;
	formWord.submit();
}

// 展开帖子
function loadForumThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImgForum" + t_id);
	var targetTR2 =eval("document.all.followForum" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="../forum/images/minus.gif";
			if (targetImg2.getAttribute("loaded")=="no"){
				isIE = (document.all?true:false);
				
				if (isIE)
					document.frames["hiddenframe"].location.replace("../forum/listtree.jsp?id="+b_id+getstr);
				else {
					var frm = document.getElementById("hiddenframe");
					frm.contentWindow.location.replace("../forum/listtree.jsp?id="+b_id+getstr);
				}
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="../forum/images/plus.gif";
		}
	}
}
</script>
</html>
