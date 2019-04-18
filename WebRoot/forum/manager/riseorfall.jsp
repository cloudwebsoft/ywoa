<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
	String querystring = StrUtil.getNullString(request.getQueryString());
	String privurl = ParamUtil.get(request, "privurl");
	
	String op = ParamUtil.get(request, "op");
	String strIds = ParamUtil.get(request, "ids");
	try {
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
		// 防XSS
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "ids", strIds, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	String boardcode = ParamUtil.get(request, "boardcode");
	
	if (op.equals("do")) {
		boolean re = false;
		try {
			String[] idsary = StrUtil.split(strIds, ",");
			int len = idsary.length;
			for (int i=0; i<len; i++) {
				re = topic.riseOrFallTopic(request,StrUtil.toLong(idsary[i]));
			}		
			if (re) {
				out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_operate_success"), privurl));
			}
			else {
				out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
			}
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><lt:Label res="res.label.forum.manager" key="riseorfall"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<br>
<FORM action="riseorfall.jsp?op=do" method="post" name="form1">
<TABLE class="tableCommon60" width="55%" align="center" cellPadding=3 cellSpacing=0>
      <thead>
        <TR>
          <TD height=22 colSpan=2 align="center"><lt:Label res="res.label.forum.manager" key="riseorfall"/></TD>
        </TR>
 	  </thead>
      <TBODY>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="topic"/></TD>
          <TD height=22 align="left">
            <%
		MsgMgr mm = new MsgMgr();
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary==null) {
			return;
		}
		int len = idsary.length;
		for (int i=0; i<len; i++) {
			MsgDb md = mm.getMsgDb(StrUtil.toLong(idsary[i]));
		%>
            <a href="../showtopic.jsp?rootid=<%=md.getRootid()%>"><%=StrUtil.toHtml(md.getTitle())%></a><BR>
		<%}%>		</TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="to_date"/></TD>
          <TD height=22 align="left"><input type=hidden name=ids value="<%=strIds%>">
		  <input type=hidden name=privurl value="<%=privurl%>">
					<input readonly type="text" id="redate" name="redate" size="20">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "redate",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>		  </TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="reason"/></TD>
          <TD height=22 align="left"><select name="riseOrFallReason" onChange="form1.reason.value+=this.value">
              <option value="">
              <lt:Label key="wu"/>
              </option>
              <%
			BasicDataMgr bdm = new BasicDataMgr();
			%>
              <%=bdm.getOptionsStr("riseOrFallReason", false)%>
          </select></TD>
        </TR>
        <TR align="center">
          <TD width="18%" height=22 align="left"><lt:Label res="res.label.forum.manager" key="desc"/></TD>
          <TD width="82%" height=22 align="left"><textarea name="reason" cols="40" rows="6" ></textarea></TD>
        </TR>
        <TR align="center">
          <TD colSpan=2 height=32><input type="submit" name="Submit2" value="<%=SkinUtil.LoadString(request,"ok")%>"></TD>
        </TR>
      </TBODY>
</TABLE>
</FORM>
<br>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</BODY></HTML>
