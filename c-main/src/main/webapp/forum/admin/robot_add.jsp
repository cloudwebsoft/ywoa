<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HEAD><TITLE>Forum add robot</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="default.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	RobotDb rd = new RobotDb();
	try {
		if (qom.create(request, rd, "forum_robot_create")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.waitJump(SkinUtil.LoadString(request, "info_op_success"), 1, "robot_list.jsp"));
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
<FORM id=form1 name=form1 action="?op=add" method=post>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="thead">
      <lt:Label res="res.label.forum.admin.robot_add" key="base_config"/>
    </TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF">
      <lt:Label res="res.label.forum.admin.robot_add" key="robot_name"/>
    </TD>
    <TD width="65%" bgcolor="#FFFFFF"><INPUT id=name size=30 name=name></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF">
      <lt:Label res="res.label.forum.admin.robot_add" key="gather_num"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="tips"/></P></TD><TD bgcolor="#FFFFFF"><INPUT id=gather_count size=10 name=gather_count></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="gather_code"/>                  <P><lt:Label res="res.label.forum.admin.robot_add" key="input_gather_code"/> </P></TD>
  <TD bgcolor="#FFFFFF"><INPUT id=charset size=10 
name=charset></TD></TR></TBODY></TABLE>
  <br>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="thead"><lt:Label res="res.label.forum.admin.robot_add" key="gather_list_page"/></TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_model"/></TD>
    <TD width="65%" bgcolor="#FFFFFF"><INPUT onclick=showUrlType(this.value) type=radio value=0 
      name=list_url_type><lt:Label res="res.label.forum.admin.robot_add" key="hand_input"/>&nbsp;&nbsp;<INPUT onclick=showUrlType(this.value) 
      type=radio CHECKED value=1 name=list_url_type><lt:Label res="res.label.forum.admin.robot_add" key="auto_add"/>&nbsp;&nbsp;</TD></TR>
  <TBODY id=type_manual style="DISPLAY: none">
  <TR id=tr_listurl_manual>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_addr"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="col_per"/></P></TD>
    <TD bgcolor="#FFFFFF"><div id=div_manual><TEXTAREA name=list_url_link rows=6 style="width:98%"></TEXTAREA></div></TD></TR></TBODY>
  <TBODY id=type_auto>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_url_addr"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="page_var_of"/></P></TD>
    <TD bgcolor="#FFFFFF">
	<div id=div_auto><INPUT id=list_url_link size=60 name=list_url_link></div></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="index_page"/>
	</TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="begin_page"/>&nbsp;<INPUT id=list_page_begin size=10 value=1 
      name=list_page_begin>&nbsp;~&nbsp;<lt:Label res="res.label.forum.admin.robot_add" key="end_page"/>&nbsp;<INPUT id=list_page_end size=10 name=list_page_end> </TD></TR></TBODY>
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

showUrlType("1");
</script>
	<lt:Label res="res.label.forum.admin.robot_add" key="list_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[list]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="example"/>&lt;td&gt;<lt:Label res="res.label.forum.admin.robot_add" key="artitle_list"/>&lt;/td&gt;
      <P><lt:Label res="res.label.forum.admin.robot_add" key="rule_is"/>&lt;td&gt;[list]&lt;/td&gt;</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/>
</TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_field_rule rows=4 id="list_field_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_subjecturllinkrule>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_rule_url"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[url]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_doc_url_rule rows=4 id="list_doc_url_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_link_url"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=list_doc_url_prefix size=60 
      name=list_doc_url_prefix></TD></TR></TBODY></TABLE>
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
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_rule rows=4 id="doc_title_rule" style="width:98%"></TEXTAREA></TD></TR>
  
  <TR id=tr_messagerule>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="Interception_add"/>[message]</P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_rule rows=4 id="doc_content_rule" style="width:98%"></TEXTAREA></TD></TR>
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
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_filter rows=4 id="doc_title_filter" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_subjectreplace_title>
    <TD bgcolor="#FFFFFF"><p><lt:Label res="res.label.forum.admin.robot_add" key="title_replace_words"/></p>
      <p><lt:Label res="res.label.forum.admin.robot_add" key="replace_string_beforestring"/></p></TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="replace_before_char"/>&nbsp;<INPUT id=doc_title_replace_before size=40 
      name=doc_title_replace_before>
    <BR><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide_any"/><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_after_char"/>&nbsp;<INPUT 
      id=doc_title_replace_after size=40 name=doc_title_replace_after>
    <BR>    <BR></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="title_keywords"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="config_after_gather_keywords"/></P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="keywords_divide"/></P></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_title_key size=60 name=doc_title_key></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="allow_title_repeat"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio CHECKED value=1 
      name=doc_title_repeat_allow>
      <lt:Label res="res.label.forum.admin.robot_add" key="allow_repeat"/>      &nbsp;&nbsp;
      <INPUT type=radio value=0 
      name=doc_title_repeat_allow>
      <lt:Label res="res.label.forum.admin.robot_add" key="not_allow_repeat"/>      &nbsp;&nbsp;</TD>
  </TR>
  
  <TR id=tr_messagefilter>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_filter_rule"/>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="replace_enter_so"/></P>
      <P><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide"/></P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_filter rows=4 id="doc_content_filter" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagereplace_title>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_context_words_replace"/></TD>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="replace_before_char"/>&nbsp;<INPUT id=doc_content_replace_before size=40 
      name=doc_content_replace_before><BR><lt:Label res="res.label.forum.admin.robot_add" key="roles_divide_any"/><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_after_char"/>&nbsp;<INPUT 
      id=doc_content_replace_after size=40 name=doc_content_replace_after><BR><lt:Label res="res.label.forum.admin.robot_add" key="replace_string_beforestring"/><BR></TD></TR>
  <TR id=tr_savepic>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_topic_pic_to_local"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_img><lt:Label res="res.label.forum.admin.robot_add" key="no"/>&nbsp;&nbsp;<INPUT name=doc_save_img 
      type=radio value=1 checked><lt:Label res="res.label.forum.admin.robot_add" key="yes"/>&nbsp;&nbsp;</TD></TR>
  <TR id=tr_saveflash>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_topic_flash_to_local"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_flash><lt:Label res="res.label.forum.admin.robot_add" key="no"/>&nbsp;&nbsp;<INPUT name=doc_save_flash 
      type=radio value=1 checked><lt:Label res="res.label.forum.admin.robot_add" key="yes"/>&nbsp;&nbsp;</TD></TR>
  <TR id=tr_picurllinkpre>
    <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="pic/flash_link"/></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_img_flash_prefix size=60 
      name=doc_img_flash_prefix></TD></TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_board"/></TD>
    <TD align="left" bgcolor="#FFFFFF">
					<select name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){alert('<lt:Label res="res.label.forum.admin.robot_add" key="your_select_area"/>'); this.selectedIndex=0;}">
            <option value="" selected><lt:Label res="res.label.forum.admin.robot_add" key="select_board"/></option>
			<%
				Directory dir = new Directory();
				Leaf leaf = dir.getLeaf(Leaf.CODE_ROOT);
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(leaf);
				dv.ShowDirectoryAsOptions(request, privilege, out, leaf, leaf.getLayer());
			%>
          </select>		</TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="save_check_state"/></TD>
    <TD align="left" bgcolor="#FFFFFF">
	<select name="examine">
	  <option value="0"><lt:Label res="res.label.forum.admin.robot_add" key="no_check"/></option>
	  <option value="1"><lt:Label res="res.label.forum.admin.robot_add" key="has_passed"/></option>
	</select>	</TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="topic_user"/></TD>
    <TD align="left" bgcolor="#FFFFFF"><INPUT id=topic_user_name size=60 
      name=topic_user_name>
      <lt:Label res="res.label.forum.admin.robot_add" key="users_random"/></TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_add" key="face"/><br>
      <iframe src="../iframe_browlist.jsp" height="120"  width="60%" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe></TD>
    <TD align="left" bgcolor="#FFFFFF"><img id="browImg" name="browImg" src="../images/brow/25.gif" width="15" height="15">
	  <input name="expression" type="" value="15" size=1>
       <input type="button" name=btnRandom value="<lt:Label res="res.label.forum.admin.robot_add" key="face_random"/>" onClick="setRandomExpression()"></TD>
  </TR>	  
  <TR id=tr_picurllinkpre>
    <TD colspan="2" align="center" bgcolor="#FFFFFF"><input class=submit type=submit value=<lt:Label res="res.label.forum.admin.robot_add" key="submit_save"/> name=thevaluesubmit>
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input type=reset value=<lt:Label res="res.label.forum.admin.robot_add" key="reset"/> name=thevaluereset></TD>
    </TR>
  </TBODY></TABLE>
<INPUT type=hidden 
value=yes name=valuesubmit><INPUT type=hidden value=1 name=robotid></FORM>
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
