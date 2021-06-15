<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.robot.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HEAD><TITLE>Fileark add robot</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
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
if (!privilege.isUserPrivValid(request, "admin")) {	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	RobotDb rd = new RobotDb();
	try {
		if (qom.create(request, rd, "oa_document_robot_create")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.waitJump(SkinUtil.LoadString(request, "info_op_success"), 1, "robot_list.jsp"));
		}
		else {
			out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
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
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" summary="" border=0><TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
<FORM id=form1 name=form1 action="robot_add.jsp?op=add" method=post>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="tabStyle_1_title">基本设置</TD>
    </TR>
  <TR>
    <TD width="35%">机器人名</TD>
    <TD width="65%"><INPUT id=name size=30 name=name></TD></TR>
  <TR>
    <TD>采集个数                  
      <P>视网速而定，建议设置小一些，以免超时</P></TD>
    <TD><INPUT id=gather_count size=10 name=gather_count></TD></TR>
  <TR>
    <TD>采集页面编码                  <P>请输入要采集页面的编码。比如：gbk、utf-8、big5。为空则不进行编码转换</P></TD>
  <TD><INPUT id=charset size=10 
name=charset></TD></TR></TBODY></TABLE>
  <br>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR>
    <TD colspan="2" align="left" class="tabStyle_1_title">列表页面采集设</TD>
    </TR>
  <TR>
    <TD width="35%">索引页面URL地址方式</TD>
    <TD width="65%"><INPUT onclick=showUrlType(this.value) type=radio value=0 
      name=list_url_type>手工输入&nbsp;&nbsp;<INPUT onclick=showUrlType(this.value) 
      type=radio CHECKED value=1 name=list_url_type>自动增长&nbsp;&nbsp;</TD></TR>
  <TBODY id=type_manual style="DISPLAY: none">
  <TR id=tr_listurl_manual>
    <TD>索引页面URL地址
      <P>每行一个</P></TD>
    <TD><div id=div_manual><TEXTAREA name=list_url_link rows=6 style="width:98%"></TEXTAREA></div></TD></TR></TBODY>
  <TBODY id=type_auto>
  <TR>
    <TD>索引页面URL地址
      <P>分页变量用[page]代替</P></TD>
    <TD>
	<div id=div_auto><INPUT id=list_url_link size=60 name=list_url_link></div></TD></TR>
  <TR>
    <TD>索引页面页码
	</TD>
    <TD>页码开始数&nbsp;<INPUT id=list_page_begin size=10 value=1 
      name=list_page_begin>&nbsp;~&nbsp;页码结束数&nbsp;<INPUT id=list_page_end size=10 name=list_page_end> </TD></TR></TBODY>
  <TBODY>
  <TR>
    <TD>
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
	列表区域识别规则
      <P>截取的地方加上[list]</P>
      <P>如&lt;td&gt;文章列表&lt;/td&gt;
      <P>规则就是&lt;td&gt;[list]&lt;/td&gt;</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD><TEXTAREA name=list_field_rule rows=4 id="list_field_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_subjecturllinkrule>
    <TD>文章链接URL识别规则
      <P>截取的地方加上[url]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD><TEXTAREA name=list_doc_url_rule rows=4 id="list_doc_url_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR>
    <TD>文章链接URL补充前缀</TD>
  <TD><INPUT id=list_doc_url_prefix size=60 
      name=list_doc_url_prefix></TD></TR></TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR id=tr_subjectrule>
    <TD colspan="2" align="left" class="tabStyle_1_title">内容页面采集设置</TD>
    </TR>
  <TR id=tr_subjectrule>
    <TD width="35%">文章标题识别规则
      <P>截取的地方加上[subject]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD width="65%"><TEXTAREA name=doc_title_rule rows=4 id="doc_title_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_fromrule>
    <TD>信息来源识别规则
      <P>截取的地方加上[from]</P>
      <P>用 * 来代替任意字符、换行、回车<br>
      如果以#开头，表示以#之后的文字作为来源 </P></TD>
    <TD><TEXTAREA name=doc_source_rule rows=4 id="doc_source_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_authorrule>
    <TD>作者识别规则
      <P>截取的地方加上[author]</P>
      <P>用 * 来代替任意字符、换行、回车<br>
        如果以#开头，表示以#之后的文字作为作者 <br>
      </P></TD>
    <TD><TEXTAREA name=doc_author_rule rows=4 style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagerule>
    <TD>文章内容识别规则
      <P>截取的地方加上[message]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD><TEXTAREA name=doc_content_rule rows=4 id="doc_content_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagepagetype>
    <TD>文章内容分页模式</TD>
    <TD><INPUT type=radio CHECKED value=0 
      name=doc_page_mode>
    页码导航&nbsp;&nbsp;<INPUT type=radio value=1 
      name=doc_page_mode>
    上下页导航&nbsp;&nbsp;</TD></TR>
  <TR id=tr_messagepagerule>
    <TD>文章内容分页区域识别规则
      <P>截取的地方加上[pagearea]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD><TEXTAREA name=doc_page_rule rows=4 id="doc_page_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagepageurlrule>
    <TD>文章内容分页链接识别规则
      <P>截取的地方加上[page]</P>
      <P>用 * 来代替任意字符、换行、回车</P></TD>
    <TD><TEXTAREA name=doc_page_url_rule rows=4 id="doc_page_url_rule" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagepageurllinkpre>
    <TD>文章内容分页链接URL补充前缀</TD>
  <TD><INPUT 
  name=doc_page_url_prefix id=doc_page_url_prefix value="" size=60></TD></TR>
  <TR id=tr_messagepageurllinkpre>
    <TD><p>文章发布时间识别规则</p>
      <p>截取的地方加上[date]</p>
      <p>用 * 来代替任意字符、换行、回车</p></TD>
    <TD><INPUT 
  name="doc_date" id="doc_date" value="" size=60></TD>
  </TR>
  <TR id=tr_messagepageurllinkpre>
    <TD>文章发布时间格式</TD>
    <TD><INPUT 
  name="doc_date_format" id="doc_date_format" size=60></TD>
  </TR>
  </TBODY></TABLE>
  <br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=0 class="tabStyle_1 percent98">
  <TBODY>
  <TR id=tr_subjectfilter>
    <TD colspan="2" class="tabStyle_1_title">内容页面整理设置</TD>
    </TR>
  <TR id=tr_subjectfilter>
    <TD width="35%">文章标题过滤规则
      <P>用 * 来代替任意字符、换行、回车</P>
      <P>多个规则之间用 | 隔开</P>
      <P>设置该选项后，则不会采集标题符合过滤规则的文章</P></TD>
    <TD width="65%"><TEXTAREA name=doc_title_filter rows=4 id="doc_title_filter" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_subjectreplace_title>
    <TD><p>文章标题文字替换</p>
      </TD>
    <TD>替换前的字符&nbsp;<INPUT id=doc_title_replace_before size=40 
      name=doc_title_replace_before>
    <BR>(多个规则用 | 隔开，用*代替任意符号)<BR>替换后的字符&nbsp;<INPUT 
      id=doc_title_replace_after size=40 name=doc_title_replace_after>
    <BR>    <BR></TD></TR>
  <TR id=tr_subjectkey>
    <TD>文章标题包含关键字
      <P>设置该选项后，则只采集标题包含关键字的文章</P>
      <P>多个关键字之间用 | 隔开</P></TD>
    <TD><INPUT id=doc_title_key size=60 name=doc_title_key></TD></TR>
  <TR id=tr_subjectkey>
    <TD>允许文章标题重复</TD>
    <TD><INPUT type=radio CHECKED value=1 
      name=doc_title_repeat_allow>
      允许重复&nbsp;&nbsp;
      <INPUT type=radio value=0 
      name=doc_title_repeat_allow>
      不允许重复&nbsp;&nbsp;</TD>
  </TR>
  
  <TR id=tr_messagefilter>
    <TD>文章内容过滤规则
      <P>用 * 来代替任意字符、换行、回车</P>
      <P>多个规则之间用 | 隔开</P></TD>
    <TD><TEXTAREA name=doc_content_filter rows=4 id="doc_content_filter" style="width:98%"></TEXTAREA></TD></TR>
  <TR id=tr_messagereplace_title>
    <TD>文章内容文字替换</TD>
    <TD>替换前的字符&nbsp;<INPUT id=doc_content_replace_before size=40 
      name=doc_content_replace_before><BR>(多个用 | 隔开，用*代替任意符号)<BR>替换后的字符&nbsp;<INPUT 
      id=doc_content_replace_after size=40 name=doc_content_replace_after><BR>可以用[string]来表示替换前的字符<BR></TD></TR>
  <TR id=tr_savepic>
    <TD>保存内容中的图片到本地</TD>
    <TD><INPUT type=radio value=0 name=doc_save_img>否&nbsp;&nbsp;<INPUT name=doc_save_img 
      type=radio value=1 checked>是&nbsp;&nbsp;</TD></TR>
  <TR id=tr_saveflash>
    <TD>保存内容中的FLASH到本地</TD>
    <TD><INPUT type=radio value=0 name=doc_save_flash>否&nbsp;&nbsp;<INPUT name=doc_save_flash 
      type=radio value=1 checked>是&nbsp;&nbsp;</TD></TR>
  <TR id=tr_picurllinkpre>
    <TD>图片/FLASH链接的URL补充前缀</TD>
    <TD><INPUT id=doc_img_flash_prefix size=60 
      name=doc_img_flash_prefix></TD></TR>
  <TR id=tr_picurllinkpre>
    <TD align="left">存入目录</TD>
    <TD align="left">
			<input name="dir_code" type="hidden">
			<span id="dir_name">请选择目录</span>&nbsp;[<a href="javascript:openWin('dir_sel.jsp', 480, 360)">选择</a>]
		</TD>
  </TR>
  <TR id=tr_picurllinkpre>
    <TD align="left">存入时审核状态</TD>
    <TD align="left">
	<select name="examine">
	  <option value="0"><lt:Label res="res.label.webedit" key="has_not_checked"/></option>
	  <option value="2"><lt:Label res="res.label.webedit" key="has_passed"/></option>
	</select>	</TD>
  </TR>	  
  <TR id=tr_picurllinkpre>
    <TD colspan="2" align="center"><input class=submit type=submit value=提交保存 name=thevaluesubmit>
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input type=reset value=重置 name=thevaluereset></TD>
    </TR>
  </TBODY></TABLE>
<INPUT type=hidden 
value=yes name=valuesubmit><INPUT type=hidden value=1 name=robotid></FORM>
</BODY></HTML>
