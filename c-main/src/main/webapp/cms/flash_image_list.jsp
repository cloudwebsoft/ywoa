<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.module.cms.site.*" %>
<%
    String siteCode = Leaf.ROOTCODE;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>图片轮播管理</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css"/>
    <style>
        .label {
            border: 1px solid #ccc;
            margin: 5px 5px;
            padding: 5px 5px;
            border-radius: 5px;
            display: block;
            float: left;
        }

        .close {
            margin-left: 5px;
            cursor: pointer;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../inc/map.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script language="JavaScript">
        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
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
        ld = (SiteFlashImageDb) ld.getQObjectDb(new Long(id));
        boolean re = ld.del();
        if (re) {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "res.common", "info_op_success"), "提示", "flash_image_list.jsp?siteCode=" + siteCode));
        } else {
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "res.common", "info_op_fail"), "提示", "flash_image_list.jsp?siteCode=" + siteCode));
        }
        return;
    } else if (op.equals("add")) {
        QObjectMgr qom = new QObjectMgr();
        SiteFlashImageDb sad = new SiteFlashImageDb();
        try {
            if (qom.create(request, sad, "site_flash_image_create")) {
                out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "flash_image_list.jsp?siteCode=" + siteCode));
            } else {
                out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"), "提示"));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
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
    long total = lr.getTotal();

    Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }
%>
<table class="percent80" width="98%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
        <td align="left" style="padding-bottom: 5px">
            <input class="btn" type="button" onclick="window.location.href='flash_image_add.jsp'" value="添加"/>
        </td>
        <td align="right"><%=paginator.getPageStatics(request)%>
        </td>
    </tr>
</table>
<table width="98%" border="0" align="center" cellpadding="3" cellspacing="1" class="tabStyle_1 percent80">
    <thead>
    <tr align="center">
        <td width="9%" class="tabStyle_1_title">ID</td>
        <td width="35%" height="24" class="tabStyle_1_title">标题</td>
        <td width="8%" class="tabStyle_1_title">自动提取</td>
        <td width="35%" class="tabStyle_1_title">目录</td>
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
            i++;
            pd = (SiteFlashImageDb) ir.next();
            Leaf lf = dir.getLeaf(pd.getString("site_code"));
            String siteName = "";
            if (lf != null)
                siteName = lf.getName();
            else
                siteName = "已删除";
    %>
    <form id="frm<%=i%>" name="frm<%=i%>" action="?op=modify" method="post">
        <tr>
            <td align="center"><%=pd.getLong("id")%>
            </td>
            <td align="left"><%=pd.getString("name")%>
            </td>
            <td align="center">
                <%=pd.getInt("is_auto")==1?"是":"否"%>
            </td>
            <td align="left">
                <%
                    String dirs = pd.getString("dir_code");
                    String[] ary = StrUtil.split(dirs, ",");
                    if (ary!=null) {
                        StringBuffer sb = new StringBuffer();
                        for (String code : ary) {
                            Leaf leaf = dir.getLeaf(code);
                            StrUtil.concat(sb, "，", leaf.getName());
                        }
                        out.print(sb.toString());
                    }
                %>
            </td>
            <td height="22"><a href="flash_image_edit.jsp?siteCode=<%=siteCode%>&id=<%=pd.getLong("id")%>">编辑</a>&nbsp;&nbsp;
                <a href="javascript:;" onClick="jConfirm('您确定要删除吗？','提示',function(r){if(!r){return;}else{window.location.href='flash_image_list.jsp?op=del&id=<%=pd.getLong("id")%>&siteCode=<%=StrUtil.UrlEncode(siteCode)%>&CPages=<%=curpage%>'}}) " style="cursor:pointer">删除
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
                out.print(paginator.getPageBlock(request, "site_flash_image_list.jsp?" + querystr));
            %>
        </td>
    </tr>
</table>
</body>
</html>