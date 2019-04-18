<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="x-ua-compatible" content="ie=7" />
<title>首页管理</title>
<style>
.btn {
border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;
}
.style1 {
	font-size: 14px;
	font-weight: bold;
}
</style>
<script language="JavaScript">
<!--
function openWin(url,width,height,scrollbars)
{
	var newwin = window.open(url,"_blank","scrollbars="+scrollbars+",resizable=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

var curObj;

function openSelHotTopicWin() {
	curObj = form1.hot;
	openWin("../topic_m.jsp?action=sel", 800, 600,"yes");	
}

function openSelFocusTopicWin() {
	curObj = form2.id;
	openWin("../topic_m.jsp?action=sel", 800, 600);	
}

function selTopic(ids) {
	// 检查在notices中是否已包含了ids中的id，避免重复加入
	var ary = ids.split(",");
	var ntc = curObj.value;
	var ary2 = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		var founded = false;
		for (var j=0; j<ary2.length; j++) {
			if (ary[i]==ary2[j]) {
				founded = true;
				break;
			}
		}
		if (!founded) {
			if (ntc=="")
				ntc += ary[i];
			else
				ntc += "," + ary[i];
		}
	}
	curObj.value = ntc;
}

function delHot(id) {
	var ntc = form1.hot.value;
	var ary = ntc.split(",");
	var ary2 = new Array();
	var k = 0;
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			continue;
		}
		else {
			ary2[k] = ary[i];
			k++;
		}
	}
	ntc = "";
	for (i=0; i<ary2.length; i++) {
		if (ntc=="")
			ntc += ary2[i];
		else
			ntc += "," + ary2[i];
	}
	form1.hot.value = ntc;
	form1.submit();
}

function up(id) {
	var ntc = form1.hot.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=0) {
				var tmp = ary[i-1];
				ary[i-1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	form1.hot.value = ntc;
	form1.submit();
}

function down(id) {
	var ntc = form1.hot.value;
	var ary = ntc.split(",");
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			// 往上移动的节点不是第一个节点
			if (i!=ary.length-1) {
				var tmp = ary[i+1];
				ary[i+1] = ary[i];
				ary[i] = tmp;
			}
			else
				return;
			break;
		}
	}
	ntc = "";
	for (i=0; i<ary.length; i++) {
		if (ntc=="")
			ntc += ary[i];
		else
			ntc += "," + ary[i];
	}
	form1.hot.value = ntc;
	form1.submit();
}
function setPerson(userName, userNick){
	if (formStar.star.value=="")
		formStar.star.value = userNick;
	else
		formStar.star.value += "," + userNick;
}
//-->
</script>
<script src="../../inc/common.js"></script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Home home = Home.getInstance();

String op = ParamUtil.get(request, "op");
if (op.equals("setHot")) {
	String hot = ParamUtil.get(request, "hot");
	home.setProperty("hot", hot);
	out.print(StrUtil.Alert_Redirect("操作成功！", "home.jsp"));
	return;
}
else if (op.equals("star")) {
	String star = ParamUtil.get(request, "star");
	// 检查star是否存在
	String[] ary = StrUtil.split(star, ",");
	if (ary!=null) {
		int len = ary.length;
		UserDb ud = new UserDb();
		for (int i=0; i<len; i++) {
			if (ud.getUserDbByNick(ary[i])==null) {
				out.print(StrUtil.Alert_Back("用户" + ary[i] + "不存在！"));
				return;
			}
		}
	}
	ForumDb forum = ForumDb.getInstance();
	
	forum.setStars(star);
	forum.save();
	out.print(StrUtil.Alert_Redirect("操作成功！", "home.jsp"));
	return;
}
%>
<DIV id="tabBar">
  <div class="tabs">
    <ul>
      <li id="menu1"><a href="home.jsp">首页元素</a></li>
      <li id="menu2"><a href="home_sidebar.jsp">边栏配置</a></li>
    </ul>
  </div>
</DIV>
<script>
$("menu1").className="active"; 
</script>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理</td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <br>
      <table width="73%" align="center" cellspacing="0" class="frame_gray">
      <form id=form1 name=form1 action="?op=setHot" method=post>
        <tr>
          <td height="22" class="thead"><strong><a name="hot">推荐贴子</a></strong>&nbsp;( 编号之间用，分隔 )</td>
        </tr>
        <tr>
          <td height="22"><input type=text value="<%=StrUtil.getNullString(home.getProperty("hot"))%>" name="hot" size=60>
            <input name="button" type="button" class="btn" onClick="openSelHotTopicWin()" value="选 择">
            <input type="submit" class="btn" value="确 定"></td>
          </tr>
        <tr>
          <td height="22">
		  <%
		  					MsgMgr mm = new MsgMgr();
							MsgDb md = null;
							int[] v = home.getHotIds();
							int hotlen = v.length;
							if (hotlen==0)
								out.print("无热点话题！");
							else {
								for (int k=0; k<hotlen; k++) {
									md = mm.getMsgDb(v[k]);
									if (md.isLoaded()) {
										String color = StrUtil.getNullString(md.getColor());
										if (color.equals("")) {%>
											<%=md.getId()%>&nbsp;<img src="images/arrow.gif">&nbsp;<a target="_blank" href="../showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>
											<%}else{%>
											<%=md.getId()%>&nbsp;<img src="images/arrow.gif">&nbsp;<a target="_blank" href="../showtopic.jsp?rootid=<%=md.getId()%>"><font color="<%=color%>"><%=md.getTitle()%></font></a>
											<%}%>
											&nbsp;&nbsp;[<a href="javascript:delHot('<%=md.getId()%>')">
											<lt:Label key="op_del"/>
											</a>]
											<%if (k!=0) {%>
											&nbsp;&nbsp;[<a href="javascript:up('<%=md.getId()%>')">
											<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="up"/>
											</a>]
											<%}%>
											<%if (k!=hotlen-1) {%>
											&nbsp;&nbsp;[<a href="javascript:down('<%=md.getId()%>')">
											<lt:Label res="res.label.forum.admin.ad_topic_bottom" key="down"/>
											</a>]
											<%}%>
											<br>
											<%}
										else {%>
											<%=v[k]%>&nbsp;<font color=red><img src="images/arrow.gif">&nbsp;贴子不存在</font> &nbsp;&nbsp;[<a href="javascript:delHot('<%=v[k]%>')">
											<lt:Label key="op_del"/>
											</a>]<BR>
										<%}
								}
							}%>			</td>
        </tr>
      </form>
</table>
      <br>
      <table width="73%" border='0' align="center" cellpadding='5' cellspacing='0' class="frame_gray">
        <tr>
          <td height=20 align="center" class="thead style1">社区之星</td>
        </tr>
        <tr>
          <td valign="top"><%
	ForumDb forum = ForumDb.getInstance();
	String star = forum.getStars();
	UserDb user = new UserDb();
	UserMgr um = new UserMgr();
	if (!star.equals("")) {
		// start 中存储的是用户的name
		String[] stars = StrUtil.split(star, ",");
		for (int i=0; i<stars.length; i++) { 
			user = um.getUserDbByNick(stars[i]);
			if (user!=null) {
			%>
              <table width="130" height="150" border="0" cellspacing="0" cellpadding="0" style="float:left">
                <tr>
                  <td><font color="#FF0000">
                    <%
				String myface = user.getMyface();
				String RealPic = user.getRealPic(); 
				if (myface.equals("")) {%>
                    <img src="../images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>"/>
                <%}else{%>
                    <img src="<%=user.getMyfaceUrl(request)%>" name="tus" id="tus" />
                <%}%>
                  </font></td>
                </tr>
                <tr>
                  <td><a href="../../userinfo.jsp?username=<%=user.getName()%>" target="_blank"><%=user.getNick()%></a></td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                </tr>
              </table>
            <%
			}
			else
				out.print("<table width='130' height='150' style='float:left'><tr><td>" + stars[i] + "不存在！</td></tr></table>");
		}
	}
	%>
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <form name="formStar" action="?op=star" method="post">
                  <tr>
                    <td><input type="text" name="star" value="<%=star%>">
                        <a href="javascript:openWin('forum_user_sel.jsp', 800, 600)">
                          <lt:Label res="res.label.forum.admin.manager_list" key="select"/>
                      </a>
                        <input name="submit" type="submit" value="<lt:Label key="commit"/>">
                        <lt:Label res="res.label.blog.admin.home" key="desc_star"/></td>
                  </tr>
                </form>
            </table></td>
        </tr>
      </table>
      <br>
    <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
</body>                                        
</html>                            
  