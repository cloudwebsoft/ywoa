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
	String oldboardcode = ParamUtil.get(request, "oldboardcode");

	if (op.equals("change")) {
		boolean re = false;
		try {
			String[] idsary = StrUtil.split(strIds, ",");
			int len = idsary.length;
			for (int i=0; i<len; i++) {
				re = topic.ChangeBoard(request,StrUtil.toLong(idsary[i]),boardcode);
			}		
			if (re) {
				out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_operate_success"), privurl));
			}
			else {
				out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")));
				boardcode = oldboardcode;
			}
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
			boardcode = oldboardcode;
		}
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><lt:Label res="res.label.forum.manager" key="move_board"/> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<br>
<FORM action="changeboard.jsp?op=change" method="post" name="form1">
<TABLE class="tableCommon60" width="55%" height=144 align="center" cellPadding=3 cellSpacing=0>
	  <thead>
        <TR>
          <TD height=22 colSpan=2 align="center" class="td_title"><lt:Label res="res.label.forum.manager" key="move_board"/></TD>
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
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="move_to"/></TD>
          <TD height=22 align="left"><select name="boardcode" onChange="if(this.options[this.selectedIndex].value=='not'){alert('<lt:Label res="res.label.forum.manager" key="you_selected_board_not"/>'); this.selectedIndex=0;}">
                <option value="not" selected><lt:Label res="res.label.forum.manager" key="select_board"/></option>
			<%
				Directory dir = new Directory();
				Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
			%>
          </select>
		  <input type=hidden name=ids value="<%=strIds%>">
		  <input type=hidden name=privurl value="<%=privurl%>"></TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="reason"/></TD>
          <TD height=22 align="left"><select name="changeBoardReason" onChange="form1.reason.value+=this.value">
              <option value="">
              <lt:Label key="wu"/>
              </option>
              <%
			BasicDataMgr bdm = new BasicDataMgr();
			%>
              <%=bdm.getOptionsStr("changeBoardReason", false)%>
          </select></TD>
        </TR>
        <TR align="center">
          <TD width="16%" height=22 align="left"><lt:Label res="res.label.forum.manager" key="desc"/></TD>
          <TD width="84%" height=22 align="left"><textarea name="reason" cols="40" rows="6" ></textarea></TD>
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
