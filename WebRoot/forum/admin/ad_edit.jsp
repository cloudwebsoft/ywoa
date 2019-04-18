<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="com.redmoon.forum.ad.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Advertisement Manage</title>
<LINK href="default.css" type=text/css rel=stylesheet>
<LINK href="../../common.css" type=text/css rel=stylesheet>
<script src="../../inc/common.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
<script>
function ad_kind_onchange() {
	var kinds, key;
	kinds = new Array('0','1','2','3');
	for(key in kinds) {
		var obj=$('kind_'+kinds[key]);
		var isShow = kinds[key]==form1.ad_kind.value;
		if (isShow)
			$("kind").innerHTML = obj.innerHTML;
	}
}

function getBoards() {
	return form1.boardcodes.value;
}

function openWinBoards() {
	var ret = showModalDialog('board_sel_multi.jsp',window.self,'dialogWidth:520px;dialogHeight:350px;status:no;help:no;')
	if (ret==null)
		return;
	form1.boardNames.value = "";
	form1.boardcodes.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.boardNames.value=="") {
			form1.boardcodes.value += ret[i][0];
			form1.boardNames.value += ret[i][1];
		}
		else {
			form1.boardcodes.value += "," + ret[i][0];
			form1.boardNames.value += "," + ret[i][1];
		}
	}
}

function window_onload() {
  ad_kind_onchange();
}
</script>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style></head>
<body onload="window_onload()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

AdDb ad = new AdDb();
ad = (AdDb)ad.getQObjectDb(new Integer(id));
int ad_type = ad.getInt("ad_type");
int ad_kind = ad.getInt("ad_kind");

String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, ad, "sq_ad_save"))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "ad_edit.jsp?id=" + id));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));	
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

String[] types = new String[] {SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "top_banner"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "footer_banner"),  SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "inner_words"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "float_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_footer"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "door_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_inner_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "door_ad_right"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_outer_ad")};
%>
<table width='100%' cellpadding='0' cellspacing='0'>
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.ad_list" key="edit"/>&nbsp;</td>
  </tr>
</table>
<br>
<TABLE class="frame_gray" cellSpacing=0 cellPadding=5 width="95%" align=center>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF">
	  <form method="post" name="form1" action="?op=edit">
        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableborder">
          <tr class="header">
            <td colspan="2"><lt:Label res="res.label.forum.admin.ad_list" key="edit_ad"/> - <strong><%=types[ad_type]%></strong></td>
          </tr>
          <tbody style="display: yes">
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="display_style"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="select_display_style"/></span>
			  </td>
              <td>
			  <select name="ad_kind" onchange="ad_kind_onchange()">
                <option value="<%=AdDb.KIND_HTML%>" selected><lt:Label res="res.label.forum.admin.ad_list" key="code"/></option>
                <option value="<%=AdDb.KIND_TEXT%>"><lt:Label res="res.label.forum.admin.ad_list" key="word"/></option>
                <option value="<%=AdDb.KIND_IMAGE%>"><lt:Label res="res.label.forum.admin.ad_list" key="pic"/></option>
                <option value="<%=AdDb.KIND_FLASH%>">Flash</option>
              </select>
			  <input name="ad_type" value="<%=ad_type%>" type="hidden">
			  <script>
			  form1.ad_kind.value = "<%=ad.getInt("ad_kind")%>";
			  </script>
			  <input name="id" value="<%=id%>" type="hidden"></td>
            </tr>
            <tr>
              <td width="60%">
			  <b><lt:Label res="res.label.forum.admin.ad_list" key="ad_title"/>:</b><br><span ><lt:Label res="res.label.forum.admin.ad_list" key="notice"/></span>
			  </td>
              <td ><input type="text" size="30" name="title" value="<%=ad.getString("title")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="ad_area"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="config_area"/></span></td>
              <td><span class="TableData">
			    <input name="boardcodes" type="hidden" value="<%=ad.getString("boardcodes")%>">
				<%
				Directory dir = new Directory();
				String[] ary = StrUtil.split(ad.getString("boardcodes"), ",");
				String boardNames = "";
				if (ary!=null) {
					int arylen = ary.length;
					for (int i=0; i<arylen; i++) {
						Leaf lf = dir.getLeaf(ary[i]);
						if (lf!=null) {
							if (boardNames.equals(""))
								boardNames = lf.getName();
							else
								boardNames += "," + lf.getName();
						}	
					}
				}
				%>
                <textarea name="boardNames" cols="50" rows="5" readOnly wrap="yes" id="boardNames"><%=boardNames%></textarea>
                <br>&nbsp;
				<input class="SmallButton" title="<%=SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "add_department")%>" onClick="openWinBoards()" type="button" value="<%=SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "add")%>" name="button">
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input class="SmallButton" title="<%=SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "clear_department")%>" onClick="form1.boardNames.value='';form1.boardcodes.value=''" type="button" value="<%=SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "clear_all")%>" name="button">
              </span></td>
            </tr>
            <tr>
              <td width="60%"  ><b><lt:Label res="res.label.forum.admin.ad_list" key="ad_begin_date"/>:</b><br>
                  <span ><lt:Label res="res.label.forum.admin.ad_list" key="config_ad_effective_date"/></span></td>
              <td>
			  <input size="30" id="begin_date" name="begin_date" value="<%=DateUtil.format(ad.getDate("begin_date"), "yyyy-MM-dd")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "begin_date",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "B1",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
              </td>
            </tr>
            <tr>
              <td width="60%"  ><b><lt:Label res="res.label.forum.admin.ad_list" key="ad_end_date"/>:</b><br>
                  <span ><lt:Label res="res.label.forum.admin.ad_list" key="config_ad_end_date"/></span></td>
              <td>
			  <input size="30" id="end_date" name="end_date" value="<%=DateUtil.format(ad.getDate("end_date"), "yyyy-MM-dd")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "end_date",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "B1",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
			  
              </td>
            </tr>
        </table>
		<div id="kind"></div>
        <center>
          <input class="button" type="submit" name="advsubmit" value="<%=SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "submit")%>">
        </center>
      </form>	 
        <div id="kind_0" style="display:none" >
          <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableborder">
            <tr class="header">
              <td colspan="2"><lt:Label res="res.label.forum.admin.ad_list" key="html_code"/></td>
            </tr>
            <tr>
              <td width="60%"  valign="top"><b><lt:Label res="res.label.forum.admin.ad_list" key="ad_html_code"/>:</b><br>
                <lt:Label res="res.label.forum.admin.ad_list" key="input_display_code"/></td>
              <td><textarea rows="5" name="content" id="content" cols="50"><%=ad_kind==ad.KIND_HTML?ad.getString("content"):""%></textarea></td>
            </tr>
          </table>
        </div>
        <div id="kind_1" style="display: none" >
          <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableborder">
            <tr class="header">
              <td colspan="2"><lt:Label res="res.label.forum.admin.ad_list" key="word_ad"/></td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="words_content"/>:</b><br>
                  <span ><lt:Label res="res.label.forum.admin.ad_list" key="input_words_content"/></span></td>
              <td><input type="text" size="30" name="content" value="<%=ad_kind==ad.KIND_TEXT?ad.getString("content"):""%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="words_link"/>:</b><br>
                  <span ><lt:Label res="res.label.forum.admin.ad_list" key="words_link_url"/></span></td>
              <td ><input type="text" size="30" name="url" value="<%=ad.getString("url")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="words_size"/>:</b><br>
                  <span ><lt:Label res="res.label.forum.admin.ad_list" key="input_words_unit"/></span></td>
              <td ><input type="text" size="30" name="font_size" value="<%=ad.getString("font_size")%>">
              </td>
            </tr>
          </table>
        </div>
        <br>
        <div id="kind_2" style="display: none" >
          <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableborder">
            <tr class="header">
              <td colspan="2"><lt:Label res="res.label.forum.admin.ad_list" key="pic_ad"/></td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="pic_http"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="pic_http_src"/></span></td>
              <td ><input type="text" size="30" name="content" value="<%=ad_kind==ad.KIND_IMAGE?ad.getString("content"):""%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="pic_link"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="pic_url"/></span></td>
              <td ><input type="text" size="30" name="url" value="<%=ad.getString("url")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="pic_width"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="pic_ad_width"/></span></td>
              <td ><input type="text" size="30" name="width" value="<%=ad.getInt("width")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"  ><b><lt:Label res="res.label.forum.admin.ad_list" key="pic_height"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="pic_ad_height"/></span></td>
              <td ><input type="text" size="30" name="height" value="<%=ad.getInt("height")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"  ><b><lt:Label res="res.label.forum.admin.ad_list" key="pic_replace_words"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="pic_mouse_info"/></span></td>
              <td ><input type="text" size="30" name="image_alt" value="<%=ad.getString("image_alt")%>">
              </td>
            </tr>
          </table>
        </div>
        <br>
        <div id="kind_3" style="display: none" >
          <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableborder">
            <tr class="header">
              <td colspan="2"><lt:Label res="res.label.forum.admin.ad_list" key="flash_ad"/></td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="flash_http"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="flash_src"/></span></td>
              <td><input type="text" size="30" name="content" value="<%=ad_kind==ad.KIND_FLASH?ad.getString("content"):""%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="flash_width"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="input_flash_width"/></span></td>
              <td><input type="text" size="30" name="width" value="<%=ad.getInt("width")%>">
              </td>
            </tr>
            <tr>
              <td width="60%"><b><lt:Label res="res.label.forum.admin.ad_list" key="flash_height"/>:</b><br>
                  <span><lt:Label res="res.label.forum.admin.ad_list" key="input_flash_height"/></span></td>
              <td ><input type="text" size="30" name="height" value="<%=ad.getInt("height")%>">
              </td>
            </tr>
          </table>
        </div>	  
	   </TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<br>
<br>
</body>
</html>
