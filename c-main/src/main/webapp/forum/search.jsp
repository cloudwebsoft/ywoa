<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>
<lt:Label res="res.label.forum.search" key="search"/> - <%=Global.AppName%></title>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script src="../inc/common.js"></script>
<script language="javascript">
<!--
function form1_onsubmit(){
	if (form1.selboard.value=="")
	{
		alert('<lt:Label res="res.label.forum.search" key="alert_board"/>');
		return false;
	}
}
function form2_onsubmit(){
	if ($("queryString").value.trim()==""){
		alert("请填写关键字！");
		return false;
	}
}
//-->
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
if (!privilege.canUserDo(request, "", "search")) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 检查是否处于可搜索时间段
TimeConfig tcsearch = new TimeConfig();
if (tcsearch.isSearchForbidden(request)) {
    out.print(SkinUtil.makeErrMsg(request, StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.search", "time_forbid_search"), 
           new Object[] {tc.getProperty("forbidSearchTime")})));
	return;
}
			
String boardcode = ParamUtil.get(request, "boardcode");
%>
<FORM name="form1" action="search_do.jsp" method="get" onSubmit="return form1_onsubmit()">
<TABLE width="98%" align="center" class="tableCommon">
	<thead>
      <TR>
        <TD height=22 colSpan=2 align="center"><lt:Label res="res.label.forum.search" key="input_keywards"/></TD>
      </TR>
	</thead>
    <TBODY>
      <TR>
        <TD width="210" height=26 align="right"><lt:Label res="res.label.forum.search" key="search_content"/></TD>
        <TD vAlign=top height=24><input size=40 name=searchwhat>
          <input name=boardcode value="<%=StrUtil.toHtml(boardcode)%>" type=hidden>
        </TD>
      </TR>
      <TR>
        <TD height=26 align="right">
            <lt:Label res="res.label.forum.search" key="search_author"/>
          <INPUT type=radio value=byauthor name=searchtype>        </TD>
        <TD vAlign=top height=24><SELECT size=1 name=selauthor>
            <OPTION value=topicname selected>
            <lt:Label res="res.label.forum.search" key="topic_author"/>
            </OPTION>
            <OPTION value=replyname>
            <lt:Label res="res.label.forum.search" key="reply_author"/>
            </OPTION>
          </SELECT>
        </TD>
      </TR>
      <TR>
        <TD height=26 align="right">
            <lt:Label res="res.label.forum.search" key="search_keywords"/>
            <INPUT type=radio CHECKED value=bykey name=searchtype>
        </TD>
        <TD vAlign=top height=22><SELECT size=1 name=searchItem>
            <OPTION value=topic selected>
            <lt:Label res="res.label.forum.search" key="search_topic_keywards"/>
            </OPTION>
            <OPTION value=content><lt:Label res="res.label.forum.search" key="search_content_keywards"/></OPTION>			
          </SELECT>
        </TD>
      </TR>
      <TR>
        <TD height=26 align="right">
          <lt:Label res="res.label.forum.search" key="scope_date"/>        </TD>
        <TD vAlign=top height=23><SELECT size=1 name=timelimit>
            <OPTION value="all">
            <lt:Label res="res.label.forum.search" key="all_date"/>
            </OPTION>
            <OPTION value=1>
            <lt:Label res="res.label.forum.search" key="after_yestoday"/>
            </OPTION>
            <OPTION value=5 selected>
            <lt:Label res="res.label.forum.search" key="after_five_today"/>
            </OPTION>
            <OPTION value=10>
            <lt:Label res="res.label.forum.search" key="after_ten_today"/>
            </OPTION>
            <OPTION value=30>
            <lt:Label res="res.label.forum.search" key="after_30_today"/>
            </OPTION>
          </SELECT>
        </TD>
      </TR>
      <TR>
        <TD align=right height=26>
          <lt:Label res="res.label.forum.search" key="sel_board"/></TD>
        <TD vAlign=center height=26><select name="selboard" onChange="if(this.options[this.selectedIndex].value=='not'){alert('<lt:Label res="res.label.forum.manager" key="you_selected_board_not"/>'); this.selectedIndex=0;}">
            <option value="allboard" selected>
            <lt:Label res="res.label.forum.search" key="all_board"/>
            </option>
            <%
				Directory dir = new Directory();
				Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
			%>
          </select>
          <script language=javascript>
		<!--
		var v = "<%=StrUtil.toHtml(boardcode)%>";
		if (v!="")
			form1.selboard.value = v;
		//-->
		</script>
          &nbsp;
          <INPUT type=submit value=<lt:Label res="res.label.forum.search" key="begin_search"/> name=submit1>
        </TD>
      </TR>
</TABLE>
</FORM>
<br>
<FORM name=form2 action=search_full_text_do.jsp method="get" onSubmit="return form2_onsubmit()">
<TABLE width="98%" align="center" class="tableCommon" id=AutoNumber1>
	<thead>
      <TR>
        <TD height=22 colSpan=2><P align=center>
          <lt:Label res="res.label.forum.search" key="search_fulltext"/>
        </P></TD>
      </TR>
	</thead>
    <TBODY>
      <TR>
        <TD height=26 align="right" ><lt:Label res="res.label.forum.search" key="search_content"/>
        </TD>
        <TD vAlign=top height=23><input size=40 name="queryString"></TD>
      </TR>
      <TR>
        <TD width=210 height=26 align="right">
            <lt:Label res="res.label.forum.search" key="search_keywords"/>        </TD>
        <TD vAlign=top height=24>
          <SELECT size=1 name="fieldName">
            <OPTION value="content" selected>
            <lt:Label res="res.label.forum.search" key="search_content_keywards"/>
            </OPTION>
            <OPTION value="title">
            <lt:Label res="res.label.forum.search" key="search_topic_keywards"/>
            </OPTION>
            <OPTION value="nick">
            <lt:Label res="res.label.forum.search" key="search_nick_keywards"/>
            </OPTION>
          </SELECT>
        </TD>
      </TR>
      <TR>
        <TD align=right width=210 height=26>&nbsp;</TD>
        <TD vAlign=center height=26><INPUT type=submit value="<lt:Label res="res.label.forum.search" key="begin_search"/>" name="submit1">
        </TD>
      </TR>
  </TBODY>
</TABLE>
</FORM>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
