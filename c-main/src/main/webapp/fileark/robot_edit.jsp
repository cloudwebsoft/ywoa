<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.robot.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%
int id = ParamUtil.getInt(request, "robotId");
%>
<!DOCTYPE HTML>
<html>
<HEAD><TITLE>CMS edit robot</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
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
	var dt = openWin("../util/calendar/time.jsp", 226, 125);
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

function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function selectNode(code, name) {
	form1.dir_code.value = code;
	$("dir_name").innerHTML = name;
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
RobotDb rd = new RobotDb();
rd = (RobotDb)rd.getQObjectDb(new Integer(id));
if (op.equals("modify")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, rd, "oa_document_robot_save")) {
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
      <td width="64%" class="tdStyle_1">采集机器人</td>
      <td width="36%" class="tdStyle_1"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp">浏览机器人</A></TD>
            <TD><A class=add 
            href="robot_add.jsp">添加新机器人</A></TD>
            <TD><A class=other 
            href="robot_import.jsp">导入机器人</A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<FORM id=form1 name=form1 action="?op=modify" method=post>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="35" align="center"><input class="btn" name="submit" type=submit value=" 修  改 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="button2" type=button class="btn" onClick="window.location.href='robot_do.jsp?op=gather&robotId=<%=id%>'" value=" 采  集 ">
&nbsp;&nbsp;&nbsp;&nbsp;
<input name="button2" type=button class="btn" onClick="window.location.href='robot_export.jsp?id=<%=id%>'" value=" 导  出 "></td>
  </tr>
</table>

  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="tabStyle_1_title">基本设置</TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF">机器人名</TD>
    <TD width="65%" bgcolor="#FFFFFF"><INPUT name=name id=name value="<%=StrUtil.getNullString(rd.getString("name"))%>" size=30>
	<input name="robotId" type="hidden" value="<%=id%>">
	<input name="id" type="hidden" value="<%=id%>"></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF">采集个数                  
      <P>视网速而定，建议设置小一些，以免超时</P></TD>
    <TD bgcolor="#FFFFFF"><INPUT name=gather_count id=gather_count value="<%=rd.getInt("gather_count")%>" size=10></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF">采集页面编码                  <P>请输入要采集页面的编码。比如：gbk、utf-8、big5。为空则不进行编码转换</P></TD>
  <TD bgcolor="#FFFFFF"><INPUT 
name=charset id=charset value="<%=StrUtil.getNullString(rd.getString("charset"))%>" size=10></TD></TR></TBODY></TABLE>
  <br>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="tabStyle_1_title">列表页面采集设</TD>
    </TR>
  <TR>
    <TD width="35%" bgcolor="#FFFFFF">索引页面URL地址方式</TD>
    <TD width="65%" bgcolor="#FFFFFF">
	<INPUT onclick=showUrlType(this.value) type=radio value=0 <%=StrUtil.getNullString(rd.getString("list_url_type")).equals("0")?"checked":""%> name=list_url_type>手工输入&nbsp;&nbsp;
	<INPUT onclick=showUrlType(this.value) type=radio value=1 <%=StrUtil.getNullString(rd.getString("list_url_type")).equals("1")?"checked":""%> name=list_url_type>自动增长&nbsp;&nbsp;</TD></TR>
  <TBODY id=type_manual style="DISPLAY: none">
  <TR id=tr_listurl_manual>
    <TD bgcolor="#FFFFFF">索引页面URL地址
      <P>每行一个</P></TD>
    <TD bgcolor="#FFFFFF"><div id=div_manual><TEXTAREA name=list_url_link rows=6 style="width:98%"><%=StrUtil.getNullString(rd.getString("list_url_link"))%></TEXTAREA></div></TD></TR></TBODY>
  <TBODY id=type_auto>
  <TR>
    <TD bgcolor="#FFFFFF">索引页面URL地址
      <P>分页变量用[page]代替</P></TD>
    <TD bgcolor="#FFFFFF"><div id=div_auto><INPUT id=list_url_link size=60 
      name=list_url_link value="<%=StrUtil.getNullString(rd.getString("list_url_link"))%>"></div></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF">索引页面页码</TD>
    <TD bgcolor="#FFFFFF">页码开始数&nbsp;<INPUT id=list_page_begin size=10 name=list_page_begin value="<%=rd.getInt("list_page_begin")%>">&nbsp;~&nbsp;页码结束数&nbsp;<INPUT id=list_page_end size=10 name=list_page_end value="<%=StrUtil.getNullString(rd.getString("list_page_end"))%>"> </TD></TR></TBODY>
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
	列表区域识别规则
      <P>截取的地方加上[list]</P>
      <P>如&lt;td&gt;文章列表&lt;/td&gt;
      <P>规则就是&lt;td&gt;[list]&lt;/td&gt;</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_field_rule rows=4 id="list_field_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("list_field_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_subjecturllinkrule>
    <TD bgcolor="#FFFFFF">文章链接URL识别规则
      <P>截取的地方加上[url]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=list_doc_url_rule rows=4 id="list_doc_url_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("list_doc_url_rule")))%></TEXTAREA></TD></TR>
  <TR>
    <TD bgcolor="#FFFFFF">文章链接URL补充前缀</TD>
  <TD bgcolor="#FFFFFF"><INPUT id=list_doc_url_prefix size=60 
      name=list_doc_url_prefix value="<%=StrUtil.getNullString(rd.getString("list_doc_url_prefix"))%>"></TD></TR></TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR id=tr_subjectrule>
    <TD colspan="2" align="left" class="tabStyle_1_title">内容页面采集设置</TD>
    </TR>
  <TR id=tr_subjectrule>
    <TD width="35%" bgcolor="#FFFFFF">文章标题识别规则
      <P>截取的地方加上[subject]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_rule rows=4 id="doc_title_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_title_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_fromrule>
    <TD bgcolor="#FFFFFF">信息来源识别规则
      <P>截取的地方加上[from]</P>
      <P>用 * 来代替任意字符、换行、回车<br>
      如果以#开头，表示以#之后的文字作为来源</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_source_rule rows=4 id="doc_source_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_source_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_authorrule>
    <TD bgcolor="#FFFFFF">作者识别规则
      <P>截取的地方加上[author]</P>
      <P>用 * 来代替任意字符、换行、回车<br>
      如果以#开头，表示以#之后的文字作为作者</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_author_rule rows=4 style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_author_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_messagerule>
    <TD bgcolor="#FFFFFF">文章内容识别规则
      <P>截取的地方加上[message]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_rule rows=4 id="doc_content_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_content_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_messagepagetype>
    <TD bgcolor="#FFFFFF">文章内容分页模式</TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio <%=rd.getInt("doc_page_mode")==0?"CHECKED":""%> value="0" name=doc_page_mode>
    页码导航&nbsp;&nbsp;<INPUT type=radio value="1"  <%=rd.getInt("doc_page_mode")==1?"CHECKED":""%>
      name=doc_page_mode>
    上下页导航&nbsp;&nbsp;</TD></TR>
  <TR id=tr_messagepagerule>
    <TD bgcolor="#FFFFFF">文章内容分页区域识别规则
      <P>截取的地方加上[pagearea]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_page_rule rows=4 id="doc_page_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_page_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_messagepageurlrule>
    <TD bgcolor="#FFFFFF">文章内容分页链接识别规则
      <P>截取的地方加上[page]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_page_url_rule rows=4 id="doc_page_url_rule" style="width:98%"><%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_page_url_rule")))%></TEXTAREA></TD></TR>
  <TR id=tr_messagepageurllinkpre>
    <TD bgcolor="#FFFFFF">文章内容分页链接URL补充前缀</TD>
  <TD bgcolor="#FFFFFF"><INPUT 
  name=doc_page_url_prefix id=doc_page_url_prefix size=60 value="<%=StrUtil.getNullString(rd.getString("doc_page_url_prefix"))%>"></TD></TR>
  <TR id=tr_messagepageurllinkpre>
    <TD bgcolor="#FFFFFF"><p>文章发布时间识别规则</p>
        <p>截取的地方加上[date]</p>
      <p>用 * 来代替任意字符、换行、回车</p></TD>
    <TD bgcolor="#FFFFFF"><INPUT 
  name="doc_date" id="doc_date" value="<%=StrUtil.HtmlEncode(StrUtil.getNullString(rd.getString("doc_date")))%>" size=60></TD>
  </TR>
  <TR id=tr_messagepageurllinkpre>
    <TD bgcolor="#FFFFFF">文章发布时间格式</TD>
    <TD bgcolor="#FFFFFF"><INPUT 
  name="doc_date_format" id="doc_date_format" value="<%=StrUtil.getNullString(rd.getString("doc_date_format"))%>" size=60></TD>
  </TR>
  </TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR id=tr_subjectfilter>
    <TD colspan="2" class="tabStyle_1_title">内容页面整理设置</TD>
    </TR>
  <TR id=tr_subjectfilter>
    <TD width="35%" bgcolor="#FFFFFF">文章标题过滤规则
      <P>用 * 来代替任意字符、换行、回车</P>
      <P>多个规则之间用 | 隔开</P>
      <P>设置该选项后，则不会采集标题符合过滤规则的文章</P></TD>
    <TD width="65%" bgcolor="#FFFFFF"><TEXTAREA name=doc_title_filter rows=4 id="doc_title_filter" style="width:98%"><%=StrUtil.getNullString(rd.getString("doc_title_filter"))%></TEXTAREA></TD></TR>
  <TR id=tr_subjectreplace_title>
    <TD bgcolor="#FFFFFF"><p>文章标题文字替换</p>
      </TD>
    <TD bgcolor="#FFFFFF">替换前的字符&nbsp;<INPUT id=doc_title_replace_before size=40 
      name=doc_title_replace_before value="<%=StrUtil.getNullString(rd.getString("doc_title_replace_before"))%>">
    <BR>(多个规则用 | 隔开，用*代替任意符号)<BR>替换后的字符&nbsp;<INPUT name=doc_title_replace_after 
      id=doc_title_replace_after value="<%=StrUtil.getNullString(rd.getString("doc_title_replace_after"))%>" size=40>
    <BR>    <BR></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF">文章标题包含关键字
      <P>设置该选项后，则只采集标题包含关键字的文章</P>
      <P>多个关键字之间用 | 隔开</P></TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_title_key size=60 name=doc_title_key value="<%=StrUtil.getNullString(rd.getString("doc_title_key"))%>"></TD></TR>
  <TR id=tr_subjectkey>
    <TD bgcolor="#FFFFFF">允许文章标题重复</TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio <%=StrUtil.getNullString(rd.getString("doc_title_repeat_allow")).equals("1")?"checked":""%> value=1 
      name=doc_title_repeat_allow>
      允许重复&nbsp;&nbsp;
      <INPUT type=radio value=0 <%=StrUtil.getNullString(rd.getString("doc_title_repeat_allow")).equals("0")?"checked":""%> 
      name=doc_title_repeat_allow>
      不允许重复&nbsp;&nbsp;</TD>
  </TR>
  <TR id=tr_messagefilter>
    <TD bgcolor="#FFFFFF">文章内容过滤规则
      <P>用 * 来代替任意字符、换行、回车</P>
      <P>多个规则之间用 | 隔开</P></TD>
    <TD bgcolor="#FFFFFF"><TEXTAREA name=doc_content_filter rows=4 id="doc_content_filter" style="width:98%"><%=StrUtil.getNullString(rd.getString("doc_content_filter"))%></TEXTAREA></TD></TR>
  <TR id=tr_messagereplace_title>
    <TD bgcolor="#FFFFFF">文章内容文字替换</TD>
    <TD bgcolor="#FFFFFF">替换前的字符&nbsp;<INPUT id=doc_content_replace_before size=40 
      name=doc_content_replace_before value="<%=StrUtil.getNullString(rd.getString("doc_content_replace_before"))%>"><BR>(多个用 | 隔开，用*代替任意符号)<BR>替换后的字符&nbsp;<INPUT 
      id=doc_content_replace_after size=40 name=doc_content_replace_after value="<%=StrUtil.getNullString(rd.getString("doc_content_replace_after"))%>"><BR>可以用[string]来表示替换前的字符<BR></TD></TR>
  <TR id=tr_savepic>
    <TD bgcolor="#FFFFFF">保存内容中的图片到本地</TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_img <%=rd.getInt("doc_save_img")==0?"checked":""%>>否&nbsp;&nbsp;<INPUT name=doc_save_img 
      type=radio value=1 <%=rd.getInt("doc_save_img")==1?"checked":""%>>是&nbsp;&nbsp;</TD></TR>
  <TR id=tr_saveflash>
    <TD bgcolor="#FFFFFF">保存内容中的FLASH到本地</TD>
    <TD bgcolor="#FFFFFF"><INPUT type=radio value=0 name=doc_save_flash <%=rd.getInt("doc_save_flash")==0?"checked":""%>>否&nbsp;&nbsp;<INPUT name=doc_save_flash 
      type=radio value=1  <%=rd.getInt("doc_save_flash")==1?"checked":""%>>是&nbsp;&nbsp;</TD></TR>
  <TR id=tr_picurllinkpre>
    <TD bgcolor="#FFFFFF">图片/FLASH链接的URL补充前缀</TD>
    <TD bgcolor="#FFFFFF"><INPUT id=doc_img_flash_prefix size=60 
      name=doc_img_flash_prefix value="<%=StrUtil.getNullString(rd.getString("doc_img_flash_prefix"))%>"></TD></TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF">存入目录</TD>
    <TD align="left" bgcolor="#FFFFFF">
	<%
	Directory dir = new Directory();
	Leaf lf = dir.getLeaf(StrUtil.getNullString(rd.getString("dir_code")));
	String dirName = "";
	if (lf!=null)
		dirName = lf.getName();
	%>
	<input name="dir_code" type="hidden" value="<%=StrUtil.getNullString(rd.getString("dir_code"))%>">
	<span id="dir_name"><%=dirName%></span>&nbsp;[<a href="javascript:openWin('dir_sel.jsp', 480, 360)">选择</a>] </TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left" bgcolor="#FFFFFF">存入时审核状态</TD>
    <TD align="left" bgcolor="#FFFFFF">
	<select name="examine">
	  <option value="0">未审核</option>
	  <option value="2">已通过</option>
	</select>	
	<script>
	form1.examine.value = "<%=rd.getInt("examine")%>";
	</script></TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD colspan="2" align="center" bgcolor="#FFFFFF"><input class="btn" type=submit value="修  改">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" type=reset value="重  置">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" type=button value="采  集" onClick="window.location.href='robot_do.jsp?op=gather&robotId=<%=id%>'">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" name="button" type=button onClick="window.location.href='robot_export.jsp?id=<%=id%>'" value="导  出"></TD>
    </TR>
  </TBODY></TABLE>
</FORM><br>
<%
JobUnitDb ju = new JobUnitDb();
int jobId = ju.getJobId("com.redmoon.oa.fileark.robot.RobotJob", "" + id);
if (jobId!=-1) {
	ju = (JobUnitDb)ju.getQObjectDb(new Integer(jobId));
%>
<table width="98%" border="0" align="center" bgcolor="#FFFFFF" class="main">
  <form name="form2" action="robot_edit.jsp?op=editJob" method="post" onSubmit="return form2_onsubmit()">
  <tr>
    <td align="left">调度计划&nbsp;&nbsp;<a href="robot_edit.jsp?op=delJob&jobId=<%=ju.get("id")%>&robotId=<%=id%>">删除</a></td>
  </tr>
  <tr>
    <td align="left"><input name="job_class" type="hidden" value="com.redmoon.oa.fileark.robot.RobotJob">
      <input name="map_data" type="hidden" value="<%=id%>">
      名称：
      <input name="job_name" value="<%=StrUtil.getNullString(ju.getString("job_name"))%>">
      &nbsp;每月：
      <input name="month_day" size="2" value="<%=StrUtil.getNullString(ju.getString("month_day"))%>">
      号</td>
  </tr>
  <tr>
    <td align="left"> 开始时间
<%
String cron = StrUtil.getNullString(ju.getString("cron"));
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
      &nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('time')" src="../images/form/clock.gif" align="absMiddle" width="18" height="18"> 在
      <input name="weekDay" type="checkbox" value="1">
      星期日
      <input name="weekDay" type="checkbox" value="2">
      星期一
      <input name="weekDay" type="checkbox" value="3">
      星期二
      <input name="weekDay" type="checkbox" value="4">
      星期三
      <input name="weekDay" type="checkbox" value="5">
      星期四
      <input name="weekDay" type="checkbox" value="6">
      星期五
      <input name="weekDay" type="checkbox" value="7">
      星期六
      <input class="btn" name="submit3" type="submit" value=" 确 定 ">
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
<table width="98%" border="0" align="center" bgcolor="#FFFFFF" class="tabStyle_1 percent98">
<form name="form2" action="robot_edit.jsp?op=addJob" method="post" onSubmit="return form2_onsubmit()">
  <tr>
    <td align="left" class="tabStyle_1_title">调度计划</td>
  </tr>
  <tr>
    <td align="left"><input name="job_class" type="hidden" value="com.redmoon.oa.fileark.robot.RobotJob">
      每月：
        <input name="month_day" size="2">
      号
      <input name="job_name" type="hidden" value="<%=StrUtil.getNullString(rd.getString("name"))%>"></td>
  </tr>
  <tr>
    <td align="left"> 开始时间
      <input style="WIDTH: 50px" value="12:00:00" name="time" size="20">
      &nbsp;<img style="CURSOR: hand" onClick="SelectDateTime('time')" src="../images/form/clock.gif" align="absMiddle" width="18" height="18"> 在
      <input name="weekDay" type="checkbox" value="1">
      星期日
      <input name="weekDay" type="checkbox" value="2">
      星期一
      <input name="weekDay" type="checkbox" value="3">
      星期二
      <input name="weekDay" type="checkbox" value="4">
      星期三
      <input name="weekDay" type="checkbox" value="5">
      星期四
      <input name="weekDay" type="checkbox" value="6">
      星期五
      <input name="weekDay" type="checkbox" value="7">
      星期六
      <input class="btn" name="submit2" type="submit" value=" 确 定 ">
      <input name="robotId" type="hidden" value="<%=id%>">
      <input name="cron" type="hidden">
      <input name="data_map" type="hidden">
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"></td>
  </tr>
</form>  
</table>
<%}%>
</BODY>
</HTML>
