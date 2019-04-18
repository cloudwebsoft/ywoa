<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.module.cms.site.*"%>
<%
String siteCode = Leaf.ROOTCODE;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<title>图片轮播管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script language="JavaScript">
<!--
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
}
var urlObj;
function SelectImage(urlObject) {
	urlObj = urlObject;
	openWin("flash_image_sel.jsp", 800, 600);
}
function setImgUrl(visualPath, id, title) {
	o("url" + urlObj).value = visualPath;
	o("link" + urlObj).value = "<%=request.getContextPath()%>/doc_show.jsp?id=" + id;
	o("title" + urlObj).value = title;
}
//-->
</script>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	SiteFlashImageDb ld = new SiteFlashImageDb();
    ld = (SiteFlashImageDb)ld.getQObjectDb(new Long(id));
    boolean re = ld.del();
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request,"res.common", "info_op_success"),"提示", "flash_image_list.jsp?siteCode=" + siteCode));
	else
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request,"res.common", "info_op_fail"),"提示", "flash_image_list.jsp?siteCode=" + siteCode));
	return;
}
else if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	SiteFlashImageDb sad = new SiteFlashImageDb();
	try {
		if (qom.create(request, sad, "site_flash_image_create")) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "flash_image_list.jsp?siteCode=" + siteCode));
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
      <td class="tdStyle_1">图片轮播</td>
    </tr>
  </tbody>
</table>
<br>
<%
int pagesize = 20;

SiteFlashImageDb pd = new SiteFlashImageDb();
String sql = "select id from " + pd.getTable().getName() + " where site_code=" + StrUtil.sqlstr(siteCode) + " order by id desc";
int curpage = ParamUtil.getInt(request, "CPages", 1);
ListResult lr = pd.listResult(sql, curpage, pagesize);
int total = lr.getTotal();

Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
      <table class="percent80" width="98%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="right"><%=paginator.getPageStatics(request)%> </td>
        </tr>
      </table>
      <table width="98%" border="0" align="center" cellpadding="3" cellspacing="1" class="tabStyle_1 percent80">
        <thead>
        <tr align="center">
          <td width="9%" class="tabStyle_1_title">ID</td>
          <td width="49%" height="24" class="tabStyle_1_title">标题</td>
          <td width="14%" class="tabStyle_1_title">类别</td>
          <td width="13%" class="tabStyle_1_title">
            操作
          </td>
        </tr>
        </thead>
        <%
Iterator ir = lr.getResult().iterator();
int i = 0;
Directory dir = new Directory();
while (ir.hasNext()) {
	i ++;
	pd = (SiteFlashImageDb) ir.next();
	Leaf lf = dir.getLeaf(pd.getString("site_code"));
	String siteName = "";
	if (lf!=null)
		siteName = lf.getName();
	else
		siteName = "已删除";
%>
        <form id="frm<%=i%>" name="frm<%=i%>" action="?op=modify" method="post">
          <tr align="center">
            <td align="left"><%=pd.getLong("id")%></td>
            <td align="left"><%=pd.getString("name")%> </td>
            <td align="left"><%=siteName%></td>
            <td height="22"><a href="flash_image_edit.jsp?siteCode=<%=siteCode%>&id=<%=pd.getLong("id")%>">编辑</a>&nbsp;&nbsp;
			<a href="#" onClick="jConfirm('您确定要删除吗？','提示',function(r){if(!r){return;}else{window.location.href='flash_image_list.jsp?op=del&id=<%=pd.getLong("id")%>&siteCode=<%=StrUtil.UrlEncode(siteCode)%>&CPages=<%=curpage%>'}}) " style="cursor:pointer">删除
            </a>
            </td>
          </tr>
        </form>
        <%}%>
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent80">
        <tr>
          <td height="23" align="right">
              <%
	String querystr = "";
    out.print(paginator.getPageBlock(request, "site_flash_image_list.jsp?"+querystr));
%>
          </td>
        </tr>
      </table>
      <br>
      <table width="92%" align="center" class="tabStyle_1 percent80">
        <form id=form1 name=form1 action="?op=add&siteCode=<%=siteCode%>" method=post>
          <thead>
          <tr>
            <td height="22" colspan="4" class="tabStyle_1_title">添加图片</td>
          </tr>
          </thead>
          <tr>
            <td height="22">名称</td>
            <td height="22" colspan="3"><input id="name" name="name">
			<script>
            var name = new LiveValidation('name');
            name.add(Validate.Presence);	
            </script>            
            </td>
          </tr>
          <tr>
            <td height="22">图片设置</td>
            <td height="22">地址</td>
            <td height="22">链接</td>
            <td height="22">文字</td>
          </tr>
          <tr>
            <td height="22">图片1              
            <input name="site_code" value="<%=siteCode%>" type=hidden></td>
            <td><input name="url1">
            <input name="button" type="button" onclick="SelectImage(1)" value="选择" /></td>
            <td><input name="link1"></td>
            <td><input name="title1"></td>
          </tr>
          <tr>
            <td height="22">图片2              </td>
            <td><input name="url2">
            <input name="button2" type="button" onclick="SelectImage(2)" value="选择" /></td>
            <td><input name="link2"></td>
            <td><input name="title2"></td>
          </tr>
          <tr>
            <td height="22">图片3 </td>
            <td><input name="url3">
                <input name="button5" type="button" onclick="SelectImage(3)" value="选择" /></td>
            <td><input name="link3"></td>
            <td><input name="title3"></td>
          </tr>
          <tr>
            <td height="22">图片4</td>
            <td><input name="url4">
            <input name="button3" type="button" onclick="SelectImage(4)" value="选择" /></td>
            <td><input name="link4"></td>
            <td><input name="title4"></td>
          </tr>
          <tr>
            <td width="17%" height="22">图片5</td>
            <td width="32%"><input name="url5">
            <input name="button4" type="button" onclick="SelectImage(5)" value="选择" /></td>
            <td width="28%"><input name="link5"></td>
            <td width="23%"><input name="title5"></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center"><input name="submit32" type="submit" class="btn" value=" 确 定 ">	</td>
          </tr>
        </form>
      </table>
                                      
</body>                                        
</html>                            
  