<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ page import="cn.js.fan.module.cms.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
int id = ParamUtil.getInt(request, "robotId");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HEAD><TITLE>Forum edit robot</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="default.css" rel="stylesheet" type="text/css">
<script src="../../inc/common.js"></script>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function SelectDateTime(objName) {
	var dt = showModalDialog("../../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		findObj(objName).value = dt;
}

function form2_onsubmit() {

	var t = form2.time.value;
	var ary = t.split(":");
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form2.month_day.value;
	if (weekDay=="" && dayOfMonth=="") {
		alert("请填写每月几号或者星期几！");
		return false;
	}
	if (weekDay=="")
		weekDay = "?";
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	if (dayOfMonth=="")
		dayOfMonth = "?";
	var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * " + weekDay;
	form2.cron.value = cron;
	form2.data_map.value = "<%=id%>";
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
RobotDb rd = new RobotDb();
rd = (RobotDb)rd.getQObjectDb(new Integer(id));
if (op.equals("modify")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, rd, "forum_robot_save")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?robotId=" + id));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}

if (op.equals("addJob")) {
	QObjectMgr qom = new QObjectMgr();
	JobUnitDb ju = new JobUnitDb();
	try {
		if (qom.create(request, ju, "scheduler_add"))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?robotId=" + id ));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}

if (op.equals("editJob")) {
	QObjectMgr qom = new QObjectMgr();
	int jobId = ParamUtil.getInt(request, "id");
	JobUnitDb ju = new JobUnitDb();
	ju = (JobUnitDb)ju.getQObjectDb(new Integer(jobId));
	try {
	if (qom.save(request, ju, "scheduler_edit"))
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?robotId=" + id));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}

if (op.equals("delJob")) {
	JobUnitDb jud = new JobUnitDb();
	int delid = ParamUtil.getInt(request, "jobId");
	JobUnitDb ldb = (JobUnitDb)jud.getQObjectDb(new Integer(delid));
	if (ldb.del())
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?robotId=" + id));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="64%" class="head"><lt:Label res="res.label.forum.admin.robot_list" key="gather_robot"/></td>
      <td width="36%" class="head"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="browse_robot"/></A></TD>
            <TD><A class=add 
            href="robot_add.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="add_robot"/></A></TD>
            <TD><A class=other 
            href="robot_import.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="import_robot"/></A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" summary="" border=0><TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
<FORM id=form1 name=form1 action="?op=modify" method=post>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="thead"><lt:Label res="res.label.forum.admin.robot_add" key="base_config"/></TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="robot_name"/></TD>
    <TD width="65%" bgcolor="#FFFFFF"><INPUT name=name id=name value="<%=StrUtil.getNullString(rd.getString("name"))%>" size=30>
	<input name="id" type="hidden" value="<%=id%>">
	<input name="robotId" type="hidden" value="<%=id%>"></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="gather_num"/>                  
      <P><lt:Label res="res.label.forum.admin.robot_add" key="tips"/></P></TD>
    <TD bgcolor="#FFFFFF"><INPUT name=gather_count id=gather_count value="<%=rd.getInt("gather_count")%>" size=10></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="gather_code"/>                  <P><lt:Label res="res.label.forum.admin.robot_add" key="input_gather_code"/></P></TD>
  <TD bgcolor="#FFFFFF"><INPUT 
name=charset id=charset value="<%=StrUtil.getNullString(rd.getString("charset"))%>" size=10></TD></TR></TBODY></TABLE>
  <br>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="thead"><lt:Label res="res.label.forum.admin.robot_add" key="gather_list_page"/></TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_model"/></TD>
    <TD width="65%" bgcolor="#FFFFFF">
	<INPUT onclick=showUrlType(this.value) type=radio value=0 <%=StrUtil.getNullString(rd.getString("list_url_type")).equals("0")?"checked":""%> name=list_url_type><lt:Label res="res.label.forum.admin.robot_add" key="hand_input"/>&nbsp;&nbsp;
	<INPUT onclick=showUrlType(this.value) type=radio value=1 <%=StrUtil.getNullString(rd.getString("list_url_type")).equals("1")?"checked":""%> name=list_url_type><lt:Label res="res.label.forum.admin.robot_add" key="auto_add"/>&nbsp;&nbsp;</TD></TR>
  <TBODY id=type_manual style="DISPLAY: none">
  <TR id=tr_listurl_manual>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_addr"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="col_per"/></P></TD>
    <TD bgcolor="#FFFFFF"><div id=div_manual><TEXTAREA name=list_url_link rows=6 style="width:98%"><%=StrUtil.getNullString(rd.getString("list_url_link"))%></TEXTAREA></div></TD></TR></TBODY>
  <TBODY id=type_auto>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_addr"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="page_var_of"/></P></TD>
    <TD bgcolor="#FFFFFF"><div id=div_auto><INPUT id=list_url_link size=60 
      name=list_url_link value="<%=StrUtil.getNullString(rd.getString("list_url_link"))%>"></div></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_page"/></TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="begin_page"/>&nbsp;<INPUT id=list_page_begin size=10 name=list_page_begin value="<%=rd.getInt("list_page_begin")%>">&nbsp;~&nbsp;<lt:Label res="res.label.forum.admin.robot_add" key="end_page"/>&nbsp;<INPUT id=list_page_end size=10 name=list_page_end value="<%=StrUtil.getNullString(rd.getString("list_page_end"))%>"> </TD></TR></TBODY>
  <TBODY>
  <TR>
    <TD bgcolor="#FFFFFF">
<script>
var divauto = document.getElementById("div_auto").innerHTML;
var divmanual = document.getElementById("div_manual").innerHTML;
function showUrlType(value) {
	if(value == "0") {
		document.getElementById("type_manual").style.display="";
		document.getElementById("type_auto").style.display="none";
		document.getElementById("div_manual").innerHTML = divmanual;
		document.getElementById("div_auto").innerHTML = "";
	} else {
		document.getElementById("type_manual").style.display="none";
		document.getElementById("type_auto").style.display="";
		document.getElementById("div_manual").innerHTML = "";
		document.getElementById("div_auto").innerHTML = divauto;
	}
}

showUrlType("<%=StrUtil.getNullString(rd.getString("list_url_type"))%>");
</script>	
	<lt:Label res="res.label.forum.admin.robot_add" key="list_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[list]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="example"/>&lt;td&gt;<lt:Label res="res.label.forum.admin.robot_add" key="artitle_list"/>&lt;/td&gt;
      <P><lt:Label res="res.label.forum.admin.robot_add" key="rule_is"/>&lt;td&gt;[list]&lt;/td&gt;</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_field_rule rows=4 id="list_field_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("list_field_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_subjecturllinkrule>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_rule_url"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[url]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_doc_url_rule rows=4 id="list_doc_url_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("list_doc_url_rule")))%></TEXTAREA></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_link_url"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=list_doc_url_prefix size=60 
      name=list_doc_url_prefix value="<%=rd.getString("list_doc_url_prefix")%>"></TD></TR></TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR id=tr_subjectrule>
    <TD colspan="2" align="left" class="thead"><lt:Label res="res.label.forum.admin.robot_add" key="context_config"/></TD>
    </TR>
  <TR id=tr_subjectrule>
    <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_title_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[subject]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P></TD>
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_rule rows=4 id="doc_title_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_title_rule")))%></TEXTAREA></TD></TR>
  
  <TR id=tr_messagerule>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[message]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_rule rows=4 id="doc_content_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_content_rule")))%></TEXTAREA></TD></TR>
  </TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR id=tr_subjectfilter>
    <TD colspan="2" class="thead"><lt:Label res="res.label.forum.admin.robot_add" key="page_check_page"/></TD>
    </TR>
  <TR id=tr_subjectfilter>
    <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="title_check_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide"/></P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="cofig_after"/></P></TD>
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_filter rows=4 id="doc_title_filter" style="width:98%"><%=StrUtil.getNullString(rd.getString("doc_title_filter"))%></TEXTAREA></TD></TR>
  <TR id=tr_subjectreplace_title>
    <TD bgcolor="#FFFFFF"><p><lt:Label res="res.label.forum.admin.robot_add" key="title_replace_words"/></p>
      <p><lt:Label res="res.label.forum.admin.robot_add" key="replace_string_beforestring"/></p></TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="replace_before_char"/>&nbsp;<INPUT id=doc_title_replace_before size=40 
      name=doc_title_replace_before value="<%=StrUtil.getNullString(rd.getString("doc_title_replace_before"))%>">
    <BR><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide_any"/><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_after_char"/>&nbsp;<INPUT name=doc_title_replace_after 
      id=doc_title_replace_after value="<%=StrUtil.getNullString(rd.getString("doc_title_replace_after"))%>" size=40>
    <BR>    <BR></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="title_keywords"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="config_after_gather_keywords"/><</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="keywords_divide"/></P></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_title_key size=60 name=doc_title_key value="<%=StrUtil.getNullString(rd.getString("doc_title_key"))%>"></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="allow_title_repeat"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio <%=StrUtil.getNullString(rd.getString("doc_title_repeat_allow")).equals("1")?"checked":""%> value=1 
      name=doc_title_repeat_allow>
      <lt:Label res="res.label.forum.admin.robot_add" key="allow_repeat"/>&nbsp;&nbsp;
      <INPUT type=radio value=0 <%=StrUtil.getNullString(rd.getString("doc_title_repeat_allow")).equals("0")?"checked":""%> 
      name=doc_title_repeat_allow>
      <lt:Label res="res.label.forum.admin.robot_add" key="not_allow_repeat"/>&nbsp;&nbsp;</TD>
  </TR>
  <TR id=tr_messagefilter>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_filter_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_filter rows=4 id="doc_content_filter" style="width:98%"><%=StrUtil.getNullString(rd.getString("doc_content_filter"))%></TEXTAREA></TD></TR>
  <TR id=tr_messagereplace_title>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_words_replace"/></TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="replace_before_char"/>&nbsp;<INPUT id=doc_content_replace_before size=40 
      name=doc_content_replace_before value="<%=StrUtil.getNullString(rd.getString("doc_content_replace_before"))%>"><BR><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide_any"/><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_after_char"/>&nbsp;<INPUT 
      id=doc_content_replace_after size=40 name=doc_content_replace_after value="<%=StrUtil.getNullString(rd.getString("doc_content_replace_after"))%>"><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_string_beforestring"/><BR></TD></TR>
  <TR id=tr_savepic>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_topic_pic_to_local"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_img <%=rd.getInt("doc_save_img")==0?"checked":""%>><lt:Label res="res.label.forum.admin.robot_add" key="no"/>&nbsp;&nbsp;<INPUT name=doc_save_img 
      type=radio value=1 <%=rd.getInt("doc_save_img")==1?"checked":""%>><lt:Label res="res.label.forum.admin.robot_add" key="yes"/>&nbsp;&nbsp;</TD></TR>
  <TR id=tr_saveflash>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_topic_flash_to_local"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_flash <%=rd.getInt("doc_save_flash")==0?"checked":""%>><lt:Label res="res.label.forum.admin.robot_add" key="no"/>&nbsp;&nbsp;<INPUT name=doc_save_flash 
      type=radio value=1  <%=rd.getInt("doc_save_flash")==1?"checked":""%>><lt:Label res="res.label.forum.admin.robot_add" key="yes"/>&nbsp;&nbsp;</TD></TR>
  <TR id=tr_picurllinkpre>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="pic/flash_link"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_img_flash_prefix size=60 
      name=doc_img_flash_prefix value="<%=rd.getString("doc_img_flash_prefix")%>"></TD></TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_board"/></TD>
    <TD align="left" bgcolor="#FFFFFF">
	<script>
	var bcode = "<%=StrUtil.getNullString(rd.getString("dir_code"))%>";
	</script>
					<select name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' <lt:Label res="res.label.webedit" key="can_not_be_selected"/>'); this.value=bcode; return false;}">
					<option value="" selected><lt:Label res="res.label.webedit" key="select_sort"/></option>
					<%
					Directory dir = new Directory();
					Leaf lf = dir.getLeaf("root");
					DirectoryView dv = new DirectoryView(lf);
					dv.ShowDirectoryAsOptions(request, privilege, out, lf, lf.getLayer());
					%>
					</select>		</TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_check_state"/></TD>
    <TD align="left" bgcolor="#FFFFFF">
	<select name="examine">
	  <option value="0"><lt:Label res="res.label.forum.admin.robot_add" key="no_check"/></option>
	  <option value="1"><lt:Label res="res.label.forum.admin.robot_add" key="has_passed"/></option>
	</select>	
	<script>
	form1.dir_code.value = bcode;
	form1.examine.value = "<%=rd.getInt("examine")%>";
	</script></TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_user"/></TD>
    <TD align="left" bgcolor="#FFFFFF"><INPUT id=topic_user_name size=60 name=topic_user_name value="<%=StrUtil.getNullString(rd.getString("topic_user_name"))%>">
      <lt:Label res="res.label.forum.admin.robot_add" key="users_random"/></TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="face"/><br>
        <iframe src="../iframe_browlist.jsp" height="120"  width="60%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe></TD>
    <TD align="left" bgcolor="#FFFFFF"><img id="browImg" name="browImg" src="../images/brow/25.gif" width="15" height="15">
        <input name="expression" type="" value="<%=StrUtil.getNullString(rd.getString("expression"))%>" size=1>
        <input type="button" name=btnRandom value="<lt:Label res="res.label.forum.admin.robot_add" key="face_random"/>" onClick="setRandomExpression()"></TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD colspan="2" align="center" bgcolor="#FFFFFF"><input name="submit" type="submit" value="<lt:Label key="ok"/>">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input type=reset value=<lt:Label res="res.label.forum.admin.robot_add" key="reset"/>>
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input type=button value=<lt:Label res="res.label.forum.admin.robot_add" key="gather"/> onClick="window.location.href='robot_do.jsp?op=gather&robotId=<%=id%>'">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input name="button" type=button onClick="window.location.href='robot_export.jsp?id=<%=id%>'" value=<lt:Label res="res.label.forum.admin.robot_add" key="output"/>></TD>
    </TR>
  </TBODY></TABLE>
</FORM><br>
<%
JobUnitDb ju = new JobUnitDb();
int jobId = ju.getJobId("com.redmoon.forum.job.RobotJob", "" + id);
if (jobId!=-1) {
	ju = (JobUnitDb)ju.getQObjectDb(new Integer(jobId));
%>
<table width="98%" border="0" align="center" bgcolor="#FFFFFF" class="main" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <form name="form2" action="robot_edit.jsp?op=editJob" method="post" onSubmit="return form2_onsubmit()">
  <tr>
    <td align="left"><strong><lt:Label res="res.label.forum.admin.robot_add" key="attemper_scheme"/>&nbsp;&nbsp;<a href="robot_edit.jsp?op=delJob&jobId=<%=ju.get("id")%>&robotId=<%=id%>"><lt:Label res="res.label.forum.admin.robot_add" key="del"/></a></strong></td>
  </tr>
  <tr>
    <td align="left"><input name="job_class" type="hidden" value="com.redmoon.forum.job.RobotJob">
      <input name="map_data" type="hidden" value="<%=id%>">
      <lt:Label res="res.label.forum.admin.robot_add" key="name"/>：
      <input name="job_name" value="<%=ju.getString("job_name")%>">
      &nbsp;<lt:Label res="res.label.forum.admin.robot_add" key="each_month"/>：
      <input name="month_day" size="2" value="<%=StrUtil.getNullString(ju.getString("month_day"))%>">
      <lt:Label res="res.label.forum.admin.robot_add" key="date"/></td>
  </tr>
  <tr>
    <td align="left"> <lt:Label res="res.label.forum.admin.robot_add" key="begin_date"/>
<%
String cron = ju.getString("cron");
String[] ary = cron.split(" ");
if (ary[0].length()==1)
	ary[0] = "0" + ary[0];
if (ary[1].length()==1)
	ary[1] = "0" + ary[1];
if (ary[2].length()==1)
	ary[2] = "0" + ary[2];
String t = ary[2] + ":" + ary[1] + ":" + ary[0];
%>
        <input style="WIDTH: 50px" name="time" size="20" value="<%=t%>">
      &nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('time')" src="../../images/form/clock.gif" align="absMiddle" width="18" height="18"> <lt:Label res="res.label.forum.admin.robot_add" key="at"/>
      <input name="weekDay" type="checkbox" value="1">
      <lt:Label res="res.label.forum.admin.robot_add" key="Sun"/>
      <input name="weekDay" type="checkbox" value="2">
      <lt:Label res="res.label.forum.admin.robot_add" key="Mon"/>
      <input name="weekDay" type="checkbox" value="3">
      <lt:Label res="res.label.forum.admin.robot_add" key="Tues"/>
      <input name="weekDay" type="checkbox" value="4">
      <lt:Label res="res.label.forum.admin.robot_add" key="Wed"/>
      <input name="weekDay" type="checkbox" value="5">
      <lt:Label res="res.label.forum.admin.robot_add" key="Thurs"/>
      <input name="weekDay" type="checkbox" value="6">
      <lt:Label res="res.label.forum.admin.robot_add" key="Friday"/>
      <input name="weekDay" type="checkbox" value="7">
      <lt:Label res="res.label.forum.admin.robot_add" key="Sat"/>
      <input name="submit" type="submit" value="<lt:Label key="ok"/>">
      <input name="robotId" type="hidden" value="<%=id%>">
      <input name="id" type="hidden" value="<%=ju.getInt("id")%>">
      <input name="cron" type="hidden">
      <input name="data_map" type="hidden">
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden">
      <%
String[] w = ary[5].split(",");
for (int i=0; i<w.length; i++) {
%>
<script>
setCheckboxChecked("weekDay", "<%=w[i]%>");
</script>
      <%
}
%>
    </td>
  </tr>
</form>  
</table>
<%}
else {
%>
<table width="98%" border="0" align="center" bgcolor="#FFFFFF" class="main" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
<form name="form2" action="robot_edit.jsp?op=addJob" method="post" onSubmit="return form2_onsubmit()">
  <tr>
    <td align="left"><strong><lt:Label res="res.label.forum.admin.robot_add" key="attemper_scheme"/></strong></td>
  </tr>
  <tr>
    <td align="left"><input name="job_class" type="hidden" value="com.redmoon.forum.job.RobotJob">
      <lt:Label res="res.label.forum.admin.robot_add" key="each_month"/>：
      <input name="month_day" size="2">
      <lt:Label res="res.label.forum.admin.robot_add" key="date"/>
      <input name="job_name" type="hidden" value="<%=StrUtil.getNullString(rd.getString("name"))%>"></td>
  </tr>
  <tr>
    <td align="left"> <lt:Label res="res.label.forum.admin.robot_add" key="begin_date"/>
      <input style="WIDTH: 50px" value="12:00:00" name="time" size="20">
      &nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('time')" src="../../images/form/clock.gif" align="absMiddle" width="18" height="18"> 在
      <input name="weekDay" type="checkbox" value="1">
      <lt:Label res="res.label.forum.admin.robot_add" key="Sun"/>
      <input name="weekDay" type="checkbox" value="2">
      <lt:Label res="res.label.forum.admin.robot_add" key="Mon"/>
      <input name="weekDay" type="checkbox" value="3">
      <lt:Label res="res.label.forum.admin.robot_add" key="Tues"/>
      <input name="weekDay" type="checkbox" value="4">
      <lt:Label res="res.label.forum.admin.robot_add" key="Wed"/>
      <input name="weekDay" type="checkbox" value="5">
      <lt:Label res="res.label.forum.admin.robot_add" key="Thurs"/>
      <input name="weekDay" type="checkbox" value="6">
      <lt:Label res="res.label.forum.admin.robot_add" key="Friday"/>
      <input name="weekDay" type="checkbox" value="7">
      <lt:Label res="res.label.forum.admin.robot_add" key="Sat"/>
      <input name="submit" type="submit" value="<lt:Label key="ok"/>">
      <input name="robotId" type="hidden" value="<%=id%>">
      <input name="cron" type="hidden">
      <input name="data_map" type="hidden">
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"></td>
  </tr>
</form>  
</table>
<%}%>
</BODY>
<script>
function setBrow(browId) {
	form1.expression.value = browId;
	form1.browImg.src = "../images/brow/" + browId + ".gif";
}

function setRandomExpression() {
	if (form1.expression.value!='-1') {
		form1.expression.value = "-1";
		form1.btnRandom.value='<lt:Label res="res.label.forum.admin.robot_add" key="cacle_random"/>'
	}
	else {
		form1.expression.value = "15";
		form1.btnRandom.value='<lt:Label res="res.label.forum.admin.robot_add" key="face_random"/>'	
	}
}
</script>
</HTML>
