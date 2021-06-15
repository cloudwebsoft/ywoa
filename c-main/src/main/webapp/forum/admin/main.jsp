<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 cn.js.fan.cache.jcs.*"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String priv="class";
String op = ParamUtil.get(request, "op");
RMCache rmcache = RMCache.getInstance();
if (op.equals("startcache")) {
	rmcache.setCanCache(true);
}
if (op.equals("stopcache")) {
	rmcache.setCanCache(false);
}
%>
<%!	// global variables

	// decimal formatter for cache values
	static final DecimalFormat mbFormat = new DecimalFormat("#0.00");
	static final DecimalFormat percentFormat = new DecimalFormat("#0.0");
    // variable for the VM memory monitor box
    static final int NUM_BLOCKS = 50;
%>
<HTML><HEAD><TITLE>main</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="images/default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR></HEAD>
<BODY text=#000000 bgColor=#eeeeee leftMargin=0 topMargin=0><!-- ACP Page Header Start -->
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
  <TR>
    <TD class=head><span class="tab">
      <lt:Label res="res.label.forum.admin.index" key="forum_manage"/>
    </span></TD>
  </TR></TBODY></TABLE><BR><!-- ACP Page Header End -->
<STYLE type=text/css>.tab {
	PADDING-RIGHT: 30px; PADDING-LEFT: 10px; FONT-SIZE: 12px; PADDING-BOTTOM: 1px; CURSOR: hand; PADDING-TOP: 5px; LETTER-SPACING: 1px
}
</STYLE>

<SCRIPT language=JavaScript>
function tabClick( idx ) {

  for ( i = 0; i < 2; i++ ) {
    if ( i == idx ) {
      var tabImgLeft = eval("document.all.tabImgLeft__" + idx );
      var tabImgRight = eval("document.all.tabImgRight__" + idx );
      var tabLabel = eval("document.all.tabLabel__" + idx );
      var tabContent = eval("document.all.tabContent__" + idx );

      tabImgLeft.src = "images/tab_active_left.gif";
      tabImgRight.src = "images/tab_active_right.gif";
      tabLabel.background = "images/tab_active_bg.gif";
      tabContent.style.visibility = "visible";
      tabContent.style.display = "block";
      continue;
    }
    var tabImgLeft = eval("document.all.tabImgLeft__" + i );
    var tabImgRight = eval("document.all.tabImgRight__" + i );
    var tabLabel = eval("document.all.tabLabel__" + i );
    var tabContent = eval("document.all.tabContent__" + i );

    tabImgLeft.src = "images/tab_unactive_left.gif";
    tabImgRight.src = "images/tab_unactive_right.gif";
    tabLabel.background = "images/tab_unactive_bg.gif";
    tabContent.style.visibility = "hidden";
    tabContent.style.display = "none";
  }
}
</SCRIPT>

<TABLE cellSpacing=0 cellPadding=0 width="95%" align=center border=0>
  <TBODY>
  <TR>
    <TD style="PADDING-LEFT: 2px; HEIGHT: 22px" 
    background=images/tab_top_bg.gif>
      <TABLE cellSpacing=0 cellPadding=0 border=0>
        <TBODY>
        <TR>
          <TD>
            <TABLE height=22 cellSpacing=0 cellPadding=0 border=0>
              <TBODY>
              <TR>
                <TD width=3><IMG id=tabImgLeft__0 height=22 
                  src="images/tab_active_left.gif" width=3></TD>
                <TD class=tab id=tabLabel__0 onClick="tabClick( 0 )" 
                background=images/tab_active_bg.gif 
                  UNSELECTABLE="on"><lt:Label res="res.label.blog.admin.index" key="system_info"/></TD>
                <TD width=3><IMG id=tabImgRight__0 height=22 
                  src="images/tab_active_right.gif" 
              width=3></TD></TR></TBODY></TABLE></TD>
          <TD>&nbsp;</TD>
        </TR></TBODY></TABLE></TD></TR>
  <TR>
    <TD bgColor=#ffffff>
      <TABLE cellSpacing=0 cellPadding=0 width="100%" border=0>
        <TBODY>
        <TR>
          <TD width=1 background=images/tab_bg.gif><IMG height=1 
            src="images/tab_bg.gif" width=1></TD>
          <TD 
          style="PADDING-RIGHT: 15px; PADDING-LEFT: 15px; PADDING-BOTTOM: 15px; PADDING-TOP: 15px; HEIGHT: 350px" 
          vAlign=top>
            <DIV id=tabContent__0 style="DISPLAY: block; VISIBILITY: visible"><!-- Main Table Start -->
		  <TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
            <!-- Table Head Start-->
            <TBODY>
              <TR>
                <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><font size="-1"><b>
                  <lt:Label res="res.label.blog.admin.cache" key="java_memory"/>
                </b></font></TD>
              </TR>
              <TR class=row style="BACKGROUND-COLOR: #fafafa">
                <TD height="175" align="center" style="PADDING-LEFT: 10px"><p>
                    <ul>
                      <%	// The java runtime
	Runtime runtime = Runtime.getRuntime();

    double freeMemory = (double)runtime.freeMemory()/(1024*1024);
	double totalMemory = (double)runtime.totalMemory()/(1024*1024);
	double usedMemory = totalMemory - freeMemory;
	double percentFree = ((double)freeMemory/(double)totalMemory)*100.0;
    int free = 100-(int)Math.round(percentFree);
%>
                      <table border=0>
                        <tr>
                          <td><font size="-1">
                            <lt:Label res="res.label.blog.admin.cache" key="used_memory"/></font></td>
                          <td><font size="-1"><%= mbFormat.format(usedMemory) %> MB</font></td>
                        </tr>
                        <tr>
                          <td><font size="-1">
                            <lt:Label res="res.label.blog.admin.cache" key="memory_total"/>
                            :</font></td>
                          <td><font size="-1"><%= mbFormat.format(totalMemory) %> MB</font></td>
                        </tr>
                      </table>
                      <br>
                      <table border=0>
            <td><table bgcolor="#000000" cellpadding="1" cellspacing="0" border="0" width="200" align=left>
                  <td><table bgcolor="#000000" cellpadding="1" cellspacing="1" border="0" width="100%">
                        <%    for (int i=0; i<NUM_BLOCKS; i++) {
        if ((i*(100/NUM_BLOCKS)) < free) {
    %>
                        <td bgcolor="#00ff00" width="<%= (100/NUM_BLOCKS) %>%"><img src="images/blank.gif" width="1" height="15" border="0"></td>
                            <%		} else { %>
                            <td bgcolor="#006600" width="<%= (100/NUM_BLOCKS) %>%"><img src="images/blank.gif" width="1" height="15" border="0"></td>
                            <%		}
    }
%>
                    </table></td>
              </table></td>
                <td><font size="-1"> &nbsp;<b><%= percentFormat.format(percentFree) %>% 
                  <lt:Label res="res.label.blog.admin.cache" key="free"/>
                </b> </font> </td>
                      </table>
                      <br>
                      <br>
                      <br>
                      <br>
                      <br>
                      <br>
                </TD>
              </TR>
              <!-- Table Body End -->
              <!-- Table Foot -->
              <TR>
                <TD class=tfoot align=right><DIV align=right> </DIV></TD>
              </TR>
              <!-- Table Foot -->
            </TBODY>
          </TABLE></div>
            </TD>
          <TD width=1 background=images/tab_bg.gif><IMG height=1 
            src="images/tab_bg.gif" width=1></TD></TR></TBODY></TABLE></TD></TR>
  <TR>
    <TD background=images/tab_bg.gif bgColor=#ffffff><IMG height=1 
      src="images/tab_bg.gif" width=1></TD></TR></TBODY></TABLE>
</BODY></HTML>
