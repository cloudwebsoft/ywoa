<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int id = ParamUtil.getInt(request, "id", -1);
String dirCode = ParamUtil.get(request, "dir_code");

com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

LeafPriv lp = new LeafPriv(dirCode);

boolean isDirArticle = false;
Leaf lf = new Leaf();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			id = lf.getDocID();
			isDirArticle = true;
		}
	}
}

if (id==-1) {
	out.print(SkinUtil.makeErrMsg(request, "id格式错误！"));
	return;
}

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
doc = docmgr.getDocument(id);
if (doc==null || !doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}

if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

int pageNum = ParamUtil.getInt(request, "pageNum", -1);
if (pageNum==-1) {
	WikiDocumentDb wdd = new WikiDocumentDb();
	wdd = wdd.getWikiDocumentDb(id);
	pageNum = wdd.getBestPageNum();
}
else {
	// 检查审核是否已通过
	WikiDocUpdateDb wdud = new WikiDocUpdateDb();
	wdud = wdud.getWikiDocUpdateDb(id, pageNum);
	if (wdud==null) {
		out.print(SkinUtil.makeErrMsg(request, "文章不存在！", true));
		return;
	}
	if (wdud.getInt("check_status")!=WikiDocUpdateDb.CHECK_STATUS_PASSED) {
		if (!lp.canUserExamine(privilege.getUser(request))) {
			out.print(SkinUtil.makeErrMsg(request, "文章审核未通过！"));
			return;
		}
	}
}

// System.out.println(getClass() + " pageNum=" + pageNum);
if (pageNum==-1) {
	out.print(SkinUtil.makeErrMsg(request, "文章页不存在或审核未通过！"));
	return;
}

String op = ParamUtil.get(request, "op");
String view = ParamUtil.get(request, "view");
CommentMgr cm = new CommentMgr();
if (op.equals("addcomment")) {
	try {
		cm.insert(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

if (op.equals("vote")) {
	try {
		docmgr.vote(request,id);
		response.sendRedirect("doc_show.jsp?id=" + id);
		return;		
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title><%=doc.getTitle()%> - <%=Global.AppName%></title>
<link rel="stylesheet" href="js/treeview/jquery.treeview.css" />
<link href="wiki.css" type="text/css" rel="stylesheet" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.tab-bar {
	border-bottom: 1px dashed #cccccc;
	height: 40px;
	margin-bottom: 10px;
}
.tab {
	cursor:pointer;
	margin-top:8px;
	display:block;
	float:left;
	border: 1px solid #ccc;
	padding: 3px 10px;
	margin-left:5px;
}
.tab-active {
	background-color:#1D7BD3;
}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>

<script type="text/javascript" src="js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css" />

<script src="js/jquery.cookie.js" type="text/javascript"></script>
<script src="js/treeview/jquery.treeview.js" type="text/javascript"></script>
<script>
function window_onload() {
	var content = $("#content").html();
	parseDir(content);
	parseHighlight(content);
}

function parseHighlight(content) {
	var ul = document.createElement("ul");
	ul.setAttribute("id", "tabHighCont");
	$(ul).css("display", "none");
		
	var highCount = 0;
	var reg = new RegExp('<span(.*?) style="(.*?)background-color:(.*?)">(.*?)<\/span>', "ig");
	while((result=reg.exec(content))!=null) {
		var t = RegExp.$4;
		var $t = $('<div>' + t + '</div>');
		var st = $t.text();
		var at = st;
		if (st.length>15) {
			st = st.substring(0, 15);
		}

		var li = document.createElement("li");
		li.innerHTML = "<a title='" + at + "'>" + st + "</a>";
		ul.appendChild(li);
		highCount ++;
	}
	
	$("#dir").append(ul);
	
	$("#tabHighCont li a").bind("click", function() {
		var text = this.innerText;
		var span = $("#content").find("span");
		span.each(function(){
			var html = $(this).prop('outerHTML');
			if (html.indexOf("background-color")!=-1) {
				if (html.indexOf(text)!=-1) {
					// lte界面中，滚动会导致iframe整体左移				
					// $(this)[0].scrollIntoView(true);
					showSearch($(this)[0]);					
				}
			}
		});
	});	
	
	$('#tabHigh').click(function() {
		$('#tabDirCont').hide();
		$('#tabHighCont').show();
		$('#tabDir').removeClass('tab-active');
		$('#tabHigh').addClass('tab-active');
	});
	
	$('#tabDir').click(function() {
		$('#tabDirCont').show();
		$('#tabHighCont').hide();
		$('#tabDir').addClass('tab-active');
		$('#tabHigh').removeClass('tab-active');		
	});		
}

function showSearch(spanObj){   
	var oDiv = spanObj;
	var t = document.createElement("input");
	t.type="text";
	oDiv.insertBefore(t, oDiv.firstChild);
	t.focus();
	oDiv.removeChild(t);
}

function parseDir(content) {
	// 查找H2
	var ul = document.createElement("ul");
	ul.setAttribute("id", "tabDirCont");

	var reg = new RegExp("<H2>(.*?)<\/H2>", "ig");
	var reg3 = new RegExp("<H3>(.*?)<\/H3>", "ig");
	var result;
	var begin = 0;
	var end = 0;
	var k = 0;
	var oldli;
	var imgS = "<img class=s src=\"images/wiki/s.gif\">";
	while((result=reg.exec(content))!=null){
		var li = document.createElement("li");
		li.innerHTML = imgS + "<a>" + RegExp.$1 + "</a>";
		li.setAttribute("layer", 1);
		ul.appendChild(li);
		
		if (k==0) {
			begin = result.index + result[0].length;
			k++;
		}
		else {
			end = result.index - 1;
			
			var str = content.substring(begin, end);
			// alert("Matched'"+result[0]+"'"+" at position "+result.index+"; next search begins at "+ reg.lastIndex);
			// 查找H3
			var reg3 = new RegExp("<H3>(.*?)<\/H3>", "ig");
			var result3;
			var ul3 = document.createElement("ul");
			var isFound = false;
			while((result3=reg3.exec(str))!=null){
				var li3 = document.createElement("li");
				li3.innerHTML = imgS + "<a>" + RegExp.$1 + "</a>";
				li3.className = "Child";
				li3.setAttribute("layer", 2);
				ul3.appendChild(li3);
				isFound = true;
			}
			if (isFound) {
				oldli.appendChild(ul3);
				oldli.className = "Opened";
			}
			
			begin = result.index + result[0].length;
		}
		oldli = li;
	}
	
	if (begin<content.length - 1) {
		var str = content.substring(begin);
		var reg3 = new RegExp("<H3>(.*?)<\/H3>", "ig");
		var result3;
		var ul3 = document.createElement("ul");
		var isFound = false;
		while((result3=reg3.exec(str))!=null){
			var li3 = document.createElement("li");
			li3.innerHTML = imgS + "<a>" + RegExp.$1 + "</a>";
			li3.className = "Child";
			li3.setAttribute("layer", 2);					
			ul3.appendChild(li3);
			isFound = true;
		}
		if (isFound) {
			oldli.appendChild(ul3);
			oldli.className = "Opened";
		}
	}

	var t = document.createElement("div");
	$(t).addClass("tab-bar");
	t.innerHTML = "<span id='tabDir' class='tab'>目录</span>&nbsp;&nbsp;<span id='tabHigh' class='tab'>高亮</span>";
	$("#dir").append(t);
	$("#dir").append(ul);
	
	$('#tabDir').addClass('tab-active');

	$("li.Opened img").bind("click", function(){
		if (this.parentNode){
			if (this.parentNode.className == "Opened")	{
				this.parentNode.className = "Closed";
			}
			else {
				this.parentNode.className = "Opened";
			}
		}
	});
	
	$("#dir li a").bind("click", function() {
		var layer = this.parentNode.getAttribute("layer");
		var text = this.innerHTML;
		if (layer==1) {
			var h2 = $("#content").find("h2");
			h2.each(function(){
				if ($(this).html()==text) {
					// $(this)[0].scrollIntoView(true);
					showSearch($(this)[0]);					
				}
			});
		}
		else {
			var h3 = $("#content").find("h3");
			h3.each(function(){
				if ($(this).html()==text) {
					// $(this)[0].scrollIntoView(true);
					showSearch($(this)[0]);					
				}
			});
		}
	});
}
</script>
</head>
<body onLoad="window_onload()">
<TABLE cellSpacing=0 cellPadding=0 width="100%">
<TBODY>
<TR>
<TD class="tdStyle_1"><div style="font-family:'宋体'">
<%
	String navstr = "";
	String parentcode = lf.getParentCode();
	Leaf plf = new Leaf();
	while (!parentcode.equals("root")) {
		plf = plf.getLeaf(parentcode);
		if (plf.getType()==Leaf.TYPE_LIST)
			navstr = "<a href='fileark/wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
		else
			navstr = plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
		
		parentcode = plf.getParentCode();
		// System.out.println(parentcode + ":" + plf.getName() + " leaf name=" + lf.getName());
	}
	if (lf.getType()==Leaf.TYPE_LIST) {
		out.print(navstr + "<a href='fileark/wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a>");
	}
	else
		out.print(navstr + lf.getName());
%>
</div></TD>
</TR>
</TBODY>
</TABLE>
<div class="content">
<div align="center" style="text-align:left">
  <table width="100%" height="401" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td height="317" colspan="40" valign="top" style="padding:5px"><table cellSpacing="0" cellPadding="5" width="100%" align="center" border="0">
        <tbody>
          <tr>
            <td height="39" align="center">
              <b><font size="3"> <%=doc.getTitle()%></font></b></td>
            </tr>
          <tr>
            <td height="20" align="right" bgcolor="#eeeeee">&nbsp;
              创建日期：<%=cn.js.fan.util.DateUtil.format(doc.getModifiedDate(), "yyyy-MM-dd")%>&nbsp;&nbsp;&nbsp;&nbsp;
              <%if (doc.isCanComment()) {%>
              <A href="doc_comment.jsp?id=<%=doc.getId()%>">【发表评论】</A>
              <%}%>
              <%if (privilege.isUserPrivValid(request, "read")) {%>
              <a href="wiki_post.jsp?dir_code=<%=doc.getDirCode()%>">【创建】</a>&nbsp;&nbsp;
              <a href="wiki_edit.jsp?id=<%=doc.getId()%>">【编辑】</a>
              <%}%>
              </td>
            </tr>
          </tbody>
        </table>
        <%
		if (doc!=null && pageNum==1) {
			// 使点击量增1
			doc.increaseHit();
		}
		%>
        <div id="dir"></div>
        <div class="clear"></div>
        <%
		if (doc.isLoaded()) {%>
        <div id="content">
          <%=doc.getContent(pageNum)%>
          <%}%>
          </div>
        <br>
        <%
		/*
		java.util.Vector attachments = doc.getAttachments(pageNum);
		java.util.Iterator ir = attachments.iterator();
		while (ir.hasNext()) {
		  Attachment am = (Attachment) ir.next(); %>
        <table width="569"  border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td width="91" height="26" align="right"><img src=images/attach.gif></td>
            <td>&nbsp; <a target=_blank href="download.do?pageNum=<%=pageNum%>&id=<%=doc.getID()%>&attachId=<%=am.getId()%>"><%=am.getName()%></a> &nbsp;下载次数&nbsp;<%=am.getDownloadCount()%></td>
            </tr>
          </table>
        <%
		}
		*/
		if (doc.getType()==1 && (op.equals("") || !op.equals("vote"))) {
                DocPollDb mpd = new DocPollDb();
                mpd = (DocPollDb)mpd.getQObjectDb(new Integer(doc.getId()));
                if (mpd!=null) {
                    String ctlType = "radio";
                    if (mpd.getInt("max_choice") > 1)
                        ctlType = "checkbox";
                    java.util.Vector options = mpd.getOptions(doc.getId());
                    int len = options.size();

                    int[] re = new int[len];
                    int[] bfb = new int[len];
                    int total = 0;
                    int k = 0;
                    for (k = 0; k < len; k++) {
                        DocPollOptionDb opt = (DocPollOptionDb) options.
                                              elementAt(k);
                        re[k] = opt.getInt("vote_count");
                        total += re[k];
                    }
                    if (total != 0) {
                        for (k = 0; k < len; k++) {
                            bfb[k] = (int) Math.round((double) re[k] / total *
                                    100);
                        }
                    }

                    String str = "";
                    str += "<table>";
                    str += "<form action='" + request.getContextPath() +
                            "/doc_vote.jsp?op=vote&id=" + doc.getId() +
                            "' name=formvote method='post'>";
                    str += "<tr><td colspan='2'>";
                    java.util.Date epDate = mpd.getDate("expire_date");
                    if (epDate != null) {
                        str += "到期时间：" + DateUtil.format(epDate, "yyyy-MM-dd");
                    }
                    str += "</td><tr>";
                    for (k = 0; k < len; k++) {
                        DocPollOptionDb opt = (DocPollOptionDb) options.
                                              elementAt(k);

                        str += "<tr>";
                        str += "<td width=26>" + (k + 1) + "、</td>";
                        str +=
                                "<td width=720><input class='n' type=" +
                                ctlType + " name=votesel value='" +
                                k + "'>";
                        str += opt.getString("content") + "</td>";
                        str += "</tr>";
                    }
                    str += "<tr>";
                    str +=
                            "<td colspan='2' align=center><input type='submit' value=' 投  票 '>";
                    str += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    str +=
                            "<input name='btn' type='button' value='查看结果' onClick=\"window.location.href='" +
                            request.getContextPath() + "/doc_vote.jsp?id=" +
                            doc.getId() + "&op=view'\"></td>";
                    str += "</tr>";
                    str += "</form>";
                    str += "</table>";
					out.print(str);
				}
        }%>
        <br>
        </td>
      <td width="206" height="317" valign="top" style="padding:5px; border-left:1px dashed #cccccc">
        <%
	  if (privilege.isUserLogin(request)) {
		  UserDb user = new UserDb();
		  user = user.getUserDb(privilege.getUser(request));
	  %>
        <div><a href="javascript:;" onclick="addTab('用户信息', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=user.getName()%>')"><%=user.getRealName()%></a></div>
        <%}%>
        <p style="line-height:1.5"><strong>词条统计</strong><br>
          浏览次数：<%=doc.getHit()%>&nbsp;次 <BR>
          编辑次数：<%=doc.getPageCount()%>&nbsp;次&nbsp;&nbsp;<a href="wiki_update_list.jsp?id=<%=doc.getId()%>">历史版本</a><BR>
          最近更新：
          <%
		  int pageCount = doc.getPageCount();
		  WikiDocumentDb wdd = new WikiDocumentDb();
		  wdd = wdd.getWikiDocumentDb(doc.getId());
		  java.util.Date lastEditDate = doc.getModifiedDate();
		  if (wdd.getLastEditDate()!=null)
		  	lastEditDate = wdd.getLastEditDate();
		  out.print(DateUtil.format(doc.getModifiedDate(), "yyyy-MM-dd HH:mm"));
		  %>
          <BR>
          创建者：<A title="查看用户资料" href="javascript:;" onclick="addTab('用户信息', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(doc.getNick())%>')"><%=doc.getAuthor()%></A><br>
          </p>
        <ul id="wikiTree" class="filetree treeview-famfamfam">
          <%
		Leaf lfWiki = new Leaf();
		lfWiki = lfWiki.getLeaf(Leaf.CODE_WIKI);
		DirectoryView dv = new DirectoryView(request, lfWiki);
		dv.ListUl(out, "wiki_show.jsp", true);
		%>
          </ul>
        
        </td>
    </tr>
  </table>
</div>
</div>
</body>
<script>
$(document).ready(function(){
	$("#wikiTree").treeview({
		persist: "cookie",
		collapsed: false,
		unique: true,
		cookieId: "navigationtree"
	});
	
	$(window).goToTop({
		showHeight : 1,//设置滚动高度时显示
		speed : 500 //返回顶部的速度以毫秒为单位
	});	
});
</script>
</html>