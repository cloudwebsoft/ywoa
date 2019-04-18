<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="java.io.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="pvg" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
/*
- 功能描述：查看文件
- 访问规则：来自手机客户端
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-6-28
==================
- 修改者：
- 修改时间：
- 修改原因
- 修改点：
*/
String skey = ParamUtil.get(request, "skey");
JSONObject json = new JSONObject();
boolean ret = pvg.Auth(skey);
if(ret){
	try {
		json.put("res","-2");
		json.put("msg","时间过期");
		out.print(json.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

pvg.doLogin(request, skey);

com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

int id = 0;
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
UserMgr um = new UserMgr();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			// id = lf.getDocID();
			doc = docmgr.getDocumentByCode(request, dirCode, privilege);
			id = doc.getID();
			isDirArticle = true;
		}
	}
}

if (id==0) {
	try {
		id = ParamUtil.getInt(request, "id");
		doc = docmgr.getDocument(id);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (!doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}
if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

String CPages = ParamUtil.get(request, "CPages");
int pageNum = 1;
if (StrUtil.isNumeric(CPages))
	pageNum = Integer.parseInt(CPages);

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
	out.print(StrUtil.Alert_Redirect("操作成功！", "doc_show.jsp?id=" + id));
}

if (op.equals("vote")) {
	try {
		docmgr.vote(request,id);
		out.print(StrUtil.Alert_Redirect("投票成功！", "doc_show.jsp?op=view&id=" + id));
		return;		
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

if (op.equals("delCmt")) {
	try {
		int cmtId = ParamUtil.getInt(request, "cmtId");
		if (cm.del(request, privilege, cmtId))
			out.print(StrUtil.Alert_Redirect("操作成功！", "doc_show.jsp?id=" + id));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta name="viewport" content="initial-scale=1.0, maximum-scale=3.0, minimum-scale=1.0" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Global.AppName%> - <%=doc.getTitle()%></title>
<style>
body,div,ul,li,p,span {
	font-size:22px;
	color: #a1a1a1;
}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<script tyle="text/javascript" language="javascript" src="spwhitepad/createShapes.js"></script>
<script>
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "关闭菜单";		
	}
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "read"))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

    LeafPriv lp = new LeafPriv();
	lp.setDirCode(doc.getDirCode());
    if (!lp.canUserSee(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	else {
		if (doc!=null && pageNum==1) {
			// 使点击量增1
			doc.increaseHit();
		}
%>
<%if (doc.isLoaded()) {%>
	<table width="100%" align="center" style="display:">
    <tr>
      <td height="22" align="left" style="padding-left:5px">
	  <%
	  if (!doc.getKind().equals("")) {
		SelectOptionDb sod = new SelectOptionDb();
		%>
		类别：<%=sod.getOptionName("fileark_kind", doc.getKind())%>&nbsp;&nbsp;
		<%
	  }
	  %>
	  <%if (!doc.getAuthor().equals("")){%>
        作者：<%=doc.getAuthor()%>&nbsp;
        <%}%>
        &nbsp;&nbsp;&nbsp;访问次数：<%=doc.getHit()%>
        <%}else{%>
        未找到该文章！
        <%}%>
      &nbsp;&nbsp;&nbsp;&nbsp;</td>
    </tr>
</table>
<%if (doc.isLoaded()) {%>
<table width="98%" align="center" class="percent98">
<tr><td style="line-height:150%;">
<%=doc.getContent(pageNum)%>
</td></tr></table>
<%}%>
<br>
<%
         String str = "";
		if (doc.getType()==1) {
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

                    str = "";
                    str += "<table>";
                    str += "<form action='" + request.getContextPath() +
                            "/doc_show.jsp?op=vote&id=" + doc.getId() +
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
                            "<td colspan='2' align=center><input class='btn' type='submit' value=' 投  票 '>";
                    str += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    str +=
                            "<input class='btn' name='btn' type='button' value='查看结果' onClick=\"window.location.href='" +
                            request.getContextPath() + "/doc_show.jsp?id=" +
                            doc.getId() + "&op=view'\"></td>";
                    str += "</tr>";
                    str += "</form>";
                    str += "</table>";
					out.print(str);
				}
        }%>
<br>
<%if (op.equals("view")) {
                DocPollDb mpd = new DocPollDb();
                mpd = (DocPollDb)mpd.getQObjectDb(new Integer(doc.getId()));
                java.util.Vector options = mpd.getOptions(doc.getId());
                int len = options.size();

                int[] re = new int[len];
                int[] bfb = new int[len];
                int total = 0;
                int k = 0;
                for (k=0; k<len; k++) {
                        DocPollOptionDb opt = (DocPollOptionDb)options.elementAt(k);					
                        re[k] = opt.getInt("vote_count");
                        total += re[k];
                }
				
					if (total!=0) {
						for (k=0; k<len; k++) {
							bfb[k] = (int)Math.round((double)re[k]/total*100);
						}
					}
		%>
<table class="tabStyle_1 percent80">
  <tr>
    <td class="tabStyle_1_title" colspan="4">投票结果：</td>
  </tr>
  <% 
				int barId = 0;
				String showVoteUser = ParamUtil.get(request, "showVoteUser");
				for (k=0; k<len; k++) { %>
  <tr>
    <td width="5%"><%=k+1%>、</td>
    <td width="59%"><img src="forum/images/vote/bar<%=barId%>.gif" width=<%=bfb[k]*2%> height=10>
    <%
						if (showVoteUser.equals("1")) {
							out.print("<div>");
							String[] userAry = StrUtil.split(((DocPollOptionDb)options.elementAt(k)).getString("vote_user"), ",");
							if (userAry!=null) {
								int userLen = userAry.length;
								String userNames = "";
								for (int n=0; n<userLen; n++) {
									UserDb ud = um.getUserDb(userAry[n]);
									if (userNames.equals(""))
										userNames = ud.getRealName();
									else
										userNames += ",&nbsp;" + ud.getRealName();
								}
								out.print(userNames);
							}
							out.print("</div>");
						}
						%></td>
    <td width="17%" align="right"><%=re[k]%>人</td>
    <td width="19%" align="right"><%=bfb[k]%>%</td>
  </tr>
  <%
					barId ++;
					if (barId==10)
						barId = 0;				
				}%>
  <tr>
    <td colspan="4" align="center">共有<%=total%>人参加调查&nbsp;&nbsp;
        <input class="btn" name="button" type="button" onClick="window.location.href='doc_show.jsp?id=<%=doc.getId()%>&op=view&showVoteUser=1'" value="查看投票人员"></td>
  </tr>
</table>
<%}%>
<%}%>
</body>
</html>
