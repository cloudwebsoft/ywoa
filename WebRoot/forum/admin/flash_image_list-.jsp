<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.cms.*"%>
<%@ page import="cn.js.fan.module.cms.site.*"%>
<%@ page import="cn.js.fan.module.cms.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String siteCode = "cws_forum";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Flash图片管理</title>
<style>
.btn {
border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;
}
</style>
<script language="JavaScript">
<!--
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
var urlObj;
function SelectImage(urlObject) {
	urlObj = urlObject;
	openWin("media_frame.jsp?action=selectImage", 800, 600);
}
function SetUrl(visualPath) {
	urlObj.value = visualPath;
}
//-->
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
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
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.common", "info_op_success"), "flash_image_list.jsp?siteCode=" + siteCode));
	else
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.common", "info_op_fail"), "flash_image_list.jsp?siteCode=" + siteCode));
	return;
}

if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	SiteFlashImageDb sad = new SiteFlashImageDb();
	try {
		if (qom.create(request, sad, "site_flash_image_create")) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flash_image_list.jsp?siteCode=" + siteCode));
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理Flash图片</td>
  </tr>
</table>
<br>
<%
int pagesize = 20;

SiteFlashImageDb pd = new SiteFlashImageDb();

String sql = "select id from " + pd.getTable().getName() + " where site_code=" + StrUtil.sqlstr(siteCode) + " order by id desc";
%>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead">管理</td>
  </tr>
  <tr> 
    <td valign="top"><br>
<%
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
      <table width="98%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="right"><%=paginator.getPageStatics(request)%> </td>
        </tr>
      </table>
      <table width="98%"  border="0" align="center" cellpadding="3" cellspacing="1" bgcolor="#CCCCCC">
        <tr align="center" bgcolor="#F1EDF3">
          <td width="9%" class="thead">ID</td>
          <td width="49%" height="24" class="thead"><strong>标题</strong></td>
          <td width="13%" class="thead"><strong>
            <lt:Label res="res.label.blog.admin.blog" key="operate"/>
          </strong></td>
        </tr>
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
            <td align="left" bgcolor="#FFFFFF"><%=pd.getLong("id")%></td>
            <td align="left" bgcolor="#FFFFFF"><%=pd.getString("name")%> </td>
            <td height="22" bgcolor="#FFFFFF"><a href="flash_image_edit.jsp?siteCode=<%=siteCode%>&id=<%=pd.getLong("id")%>">编辑</a>&nbsp;&nbsp;
			<a href="#" onClick="if (confirm('您确定要删除吗？')) window.location.href='flash_image_list.jsp?op=del&id=<%=pd.getLong("id")%>&siteCode=<%=StrUtil.UrlEncode(siteCode)%>&CPages=<%=curpage%>'"><lt:Label res="res.label.blog.admin.blog" key="del"/>
            </a> </td>
          </tr>
        </form>
        <%}%>
      </table>
      <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23"><div align="right">
              <%
	String querystr = "";
    out.print(paginator.getPageBlock(request, "site_flash_image_list.jsp?"+querystr));
%>
          </div></td>
        </tr>
      </table>
      <br>
      <br>
      <table width="92%" align="center" class="frame_gray">
        <form id=form1 name=form1 action="?op=add&siteCode=<%=siteCode%>" method=post>
          <tr>
            <td height="22" colspan="4" class="thead">添加Flash图片</td>
          </tr>
          <tr>
            <td height="22">名称</td>
            <td height="22" colspan="3"><input name="name"></td>
          </tr>
          <tr>
            <td height="22">Flash图片设置</td>
            <td height="22">地址</td>
            <td height="22">链接</td>
            <td height="22">文字</td>
          </tr>
          <tr>
            <td height="22">图片1              
            <input name="site_code" value="<%=siteCode%>" type=hidden></td>
            <td><input name="url1">
            <input name="button" type="button" onclick="SelectImage(form1.url1)" value="选择" /></td>
            <td><input name="link1"></td>
            <td><input name="title1"></td>
          </tr>
          <tr>
            <td height="22">图片2              </td>
            <td><input name="url2">
            <input name="button2" type="button" onclick="SelectImage(form1.url2)" value="选择" /></td>
            <td><input name="link2"></td>
            <td><input name="title2"></td>
          </tr>
          <tr>
            <td height="22">图片3 </td>
            <td><input name="url3">
                <input name="button5" type="button" onclick="SelectImage(form1.url3)" value="选择" /></td>
            <td><input name="link3"></td>
            <td><input name="title3"></td>
          </tr>
          
          <tr>
            <td height="22">图片4              </td>
            <td><input name="url4">
            <input name="button3" type="button" onclick="SelectImage(form1.url4)" value="选择" /></td>
            <td><input name="link4"></td>
            <td><input name="title4"></td>
          </tr>
          <tr>
            <td width="17%" height="22">图片5  			  </td>
            <td width="32%"><input name="url5">
            <input name="button4" type="button" onclick="SelectImage(form1.url5)" value="选择" /></td>
            <td width="28%"><input name="link5"></td>
            <td width="23%"><input name="title5"></td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="center"><input name="submit32" type="submit" style="border:1pt solid #636563;font-size:9pt; LINE-HEIGHT: normal;HEIGHT: 18px;" value=" 确 定 ">	</td>
          </tr>
          <tr>
            <td height="22" colspan="4" align="left">帮助：<br>
1、Flash图片根据ID进行提取<br>
2、在模板中提取的方法是：<span class="flash_img">{$cms.flashImage(id=23,w=249,h=165)} (表示提取ID为23的Flash图片广告，宽为249，高为165) <br>
3、用JS提取的方法是：&lt;script src=&quot;js.jsp?var=flashImg&amp;id=23&quot;&gt;&lt;/script&gt;</span></td>
          </tr>
        </form>
      </table>
      <br>
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
  