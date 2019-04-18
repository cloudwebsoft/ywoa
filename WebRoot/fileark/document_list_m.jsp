<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*" %>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.redmoon.oa.util.WeekMothUtil" %>
<%@ page import="com.redmoon.oa.netdisk.UtilTools" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String userName1 = privilege.getUser(request);
    String dir_code = ParamUtil.get(request, "dir_code");

    int projectId = ParamUtil.getInt(request, "projectId", 0);
    String prjUrl = "";
    if (projectId == 0 && dir_code.indexOf("cws_prj_") == 0) {
        String project = dir_code.substring(8);
        int p = project.indexOf("_");
        if (p != -1) {
            project = project.substring(0, p);
        }
        projectId = StrUtil.toInt(project, 0);
        prjUrl += "projectId=" + projectId + "&parentId=" + projectId + "&formCode=project";
    }

    if ((dir_code == null || dir_code.equals("")) && projectId != 0) {
        dir_code = "cws_prj_" + projectId;
    }

    String op = StrUtil.getNullString(request.getParameter("op"));
    String searchKind = ParamUtil.get(request, "searchKind");
    String what = ParamUtil.get(request, "what");
    int examine = ParamUtil.getInt(request, "examine", -1);
    int examine1 = ParamUtil.getInt(request, "examine1", -1);
    String kind = ParamUtil.get(request, "kind");
    String kind1 = ParamUtil.get(request, "kind1");
    String title = ParamUtil.get(request, "title");
    String content = ParamUtil.get(request, "content");
    String modifyType = ParamUtil.get(request, "dateType");
    String docSize = ParamUtil.get(request, "docSize");
    String fromDate = ParamUtil.get(request, "fromDate");
    String toDate = ParamUtil.get(request, "toDate");
    String keywords = ParamUtil.get(request, "keywords");
    String keywords1 = ParamUtil.get(request, "keywords1");
    String author = ParamUtil.get(request, "author");
    String ext = ParamUtil.get(request, "ext");
    String checkbox_png = ParamUtil.get(request, "checkbox_png");
    String checkbox_ppt = ParamUtil.get(request, "checkbox_ppt");
    String checkbox_gif = ParamUtil.get(request, "checkbox_gif");
    String checkbox_zip = ParamUtil.get(request, "checkbox_zip");
    String checkbox_pdf = ParamUtil.get(request, "checkbox_pdf");
    String checkbox_doc = ParamUtil.get(request, "checkbox_doc");
    String checkbox_xlsx = ParamUtil.get(request, "checkbox_xlsx");
    String checkbox_txt = ParamUtil.get(request, "checkbox_txt");
    Leaf fileLeaf = new Leaf();
    String filePath = fileLeaf.getFilePath();

    String orderBy = ParamUtil.get(request, "orderBy");
    if (orderBy.equals(""))
        orderBy = "createDate";
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals(""))
        sort = "desc";

    UserSetupDb usd = new UserSetupDb();
    usd = usd.getUserSetupDb(privilege.getUser(request));
//String pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";
    String pageUrl = "fwebedit_new.jsp";

//swfUpload文件上传
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String file_netdisk = cfg.get("file_netdisk");
    HashMap<String, String> explorerFileType = new HashMap<String, String>();
    explorerFileType = UtilTools.uploadFileTypeByExplorer("filearkFileExt");
    String file_size_limit = cfg.get("file_size_limit");
    int file_upload_limit = cfg.getInt("file_upload_limit");
    String upload_file_types = explorerFileType.get("ie_upload_file_types");
    String fixfox_upload_file_types = explorerFileType.get("fixfox_upload_file_types");

    com.redmoon.oa.robot.Config robotCfg = com.redmoon.oa.robot.Config.getInstance();
    boolean isRobotOpen = robotCfg.getBooleanProperty("isRobotOpen");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>文件列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/document_search.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <script type="text/javascript" src="../inc/common.js"></script>
    <script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script src="../js/jquery.xmlext.js"></script>
    <script type="text/javascript" src="../js/flexigrid.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <!-- swfupload 文件上传 -->
    <script language=JavaScript src='showDialog/showDialog.js'></script>
    <link type="text/css" rel="stylesheet" href="showDialog/showDialog.css"/>
    <link type="text/css" rel="stylesheet" href="showDialog/document.css"/>
    <script src="swfupload/swfupload.js"></script>
    <script type="text/javascript" src="swfupload/swfupload.queue.js"></script>
    <script src="js/swfupload.js"></script>
    <script language=JavaScript src='formpost.js'></script>
    <style>
        .loading {
            display: none;
            position: fixed;
            z-index: 1801;
            top: 45%;
            left: 45%;
            width: 100%;
            margin: auto;
            height: 100%;
        }

        .SD_overlayBG2 {
            background: #FFFFFF;
            filter: alpha(opacity=20);
            -moz-opacity: 0.20;
            opacity: 0.20;
            z-index: 1500;
        }

        .treeBackground {
            display: none;
            position: absolute;
            top: -2%;
            left: 0%;
            width: 100%;
            margin: auto;
            height: 200%;
            background-color: #EEEEEE;
            z-index: 1800;
            -moz-opacity: 0.8;
            opacity: .80;
            filter: alpha(opacity=80);
        }
    </style>
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

        var curOrderBy = "<%=orderBy%>";
        var sort = "<%=sort%>";
        function doSort(orderBy) {
            if (orderBy == curOrderBy)
                if (sort == "asc")
                    sort = "desc";
                else
                    sort = "asc";
            window.location.href = "document_list_m.jsp?<%=prjUrl%>&op=<%=op%>&dir_code=<%=dir_code%>&searchKind=<%=searchKind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=prjUrl %>";
        }
        function selAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                // 如果只有一个元素
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = true;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = true;
                }
            }
        }

        function clearAllCheckBox(checkboxname) {
            var checkboxboxs = document.getElementsByName(checkboxname);
            if (checkboxboxs != null) {
                // 如果只有一个元素
                if (checkboxboxs.length == null) {
                    checkboxboxs.checked = false;
                }
                for (i = 0; i < checkboxboxs.length; i++) {
                    checkboxboxs[i].checked = false;
                }
            }
        }
    </script>
</head>
<body>
<%if (projectId != 0) {%>
<%@ include file="../project/prj_inc_menu_top.jsp" %>
<script>
    o("menu5").className = "current";
</script>
<%} %>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'/></div>
<%
    if (!privilege.isUserPrivValid(request, PrivDb.PRIV_READ)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    WeekMothUtil weekMothUtil = new WeekMothUtil();
// 如果dir_code为空，则需检查权限
    LeafPriv lp = new LeafPriv();
    if (dir_code.equals("")) {
        lp.setDirCode(Leaf.ROOTCODE);
        // 如果是管理员或者文章根目录节点上有审核的权限，则允许查看全部的文章列表
        if (privilege.isUserPrivValid(request, "admin") || lp.canUserExamine(privilege.getUser(request))) {
            ;
        } else {
            // out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
            out.print(SkinUtil.makeInfo(request, "请选择目录！"));
            return;
        }
    }

    Leaf leaf = null;
    if (!dir_code.equals(""))
        leaf = dir.getLeaf(dir_code);
    String viewPage = "";
    if (!dir_code.equals("")) {
        if (leaf == null) {
            out.print(SkinUtil.makeErrMsg(request, "目录" + dir_code + "不存在！"));
            return;
        }

        lp.setDirCode(dir_code);
        if (!lp.canUserSee(privilege.getUser(request))) {
            out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }

        PluginMgr pm = new PluginMgr();
        PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
        if (pu != null && pu.getCode().equals(WikiUnit.code)) {
            response.sendRedirect("wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code));
            return;
        }

        if (pu != null) {
            IPluginUI ipu = pu.getUI(request);
            viewPage = request.getContextPath() + "/" + ipu.getViewPage();
        }
    }
    if (viewPage.equals(""))
        viewPage = request.getContextPath() + "/doc_show.jsp";


//if (projectId != 0) {
//	String projectId = dir_code.substring(8);
    // 如果projectId中含有下划线_，则截取出其ID
//	int p = projectId.indexOf("_");
//	if (p!=-1) {
//		projectId = projectId.substring(0, p);
//	}						
//	String pageRedirect = "../project/project_doc_list.jsp?projectId=" + projectId + "&dir_code=" + StrUtil.UrlEncode(dir_code);
//	response.sendRedirect(pageRedirect);
//	return;
//}

    String dir_name = "";
    if (leaf != null)
        dir_name = leaf.getName();
    if (op.equals("del")) {
        int id = ParamUtil.getInt(request, "id");
        try {
            if (docmanager.del(request, id, privilege, true))
                out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "提示", "document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&kind=" + StrUtil.UrlEncode(kind) + "&" + prjUrl));
            else
                out.print(StrUtil.jAlert("删除失败！", "提示"));
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        op = "";
    } else if (op.equals("delBatch")) {
        try {
            docmanager.delBatch(request, true);
            out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "提示", "document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&kind=" + StrUtil.UrlEncode(kind) + "&" + prjUrl));
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }
        op = "";
    } else if (op.equals("passExamine")) {
        try {%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    docmanager.passExamineBatch(request);%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
        out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "提示", "document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&kind=" + StrUtil.UrlEncode(kind) + "&" + prjUrl));
    } catch (ErrMsgException e) {
        out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        return;
    }
    op = "";
} else if (op.equals("setOnTop")) {
    try {%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    int id = ParamUtil.getInt(request, "id");
    Document doc = docmanager.getDocument(id);
    lp = new LeafPriv(doc.getDirCode());
    if (!lp.canUserExamine(privilege.getUser(request))) {
        out.print(StrUtil.jAlert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), "提示"));
        return;
    }
    int level = ParamUtil.getInt(request, "level");
    doc.setLevel(level);
    boolean re = doc.UpdateLevel();
%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
        out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "提示", "document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&kind=" + StrUtil.UrlEncode(kind) + "&" + prjUrl));
    } catch (ErrMsgException e) {
        out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        return;
    }
    return;
} else if (op.equals("changeDir")) {
    String strIds = ParamUtil.get(request, "ids");
    String[] ids = StrUtil.split(strIds, ",");
    if (ids == null) {
        out.print(StrUtil.jAlert_Back("请选择文件！", "提示"));
        return;
    }
    String newDirCode = ParamUtil.get(request, "newDirCode");
    // 检查权限
    if (!lp.canUserExamine(privilege.getUser(request))) {
        out.print(StrUtil.jAlert_Back(privilege.MSG_INVALID, "提示"));
        return;
    }
%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    DocumentMgr dm = new DocumentMgr();
    for (int i = 0; i < ids.length; i++) {
        Document doc = dm.getDocument(StrUtil.toInt(ids[i]));
        doc.UpdateDir(newDirCode);
    }%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
        out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(newDirCode) + "&" + prjUrl));
        return;
    }

    boolean isProject = false;
    if (dir_code.indexOf("cws_prj_") == 0) {

    }
    DateUtil dateUtil = new DateUtil();
    String parentCode = ParamUtil.get(request, "parentCode");

    try {
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "parentCode", parentCode, getClass().getName());

        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "parentCode", parentCode, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "searchKind", searchKind, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
    } catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }
    String uName = privilege.getUser(request);
    LeafPriv plp = null;
    if (!parentCode.equals(""))
        plp = new LeafPriv(parentCode);

    String sql = "select distinct d.id,class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate,keywords from document as d, doc_content as c ";

    if (!checkbox_png.equals("") || !checkbox_ppt.equals("") || !checkbox_gif.equals("") || !checkbox_zip.equals("") || !checkbox_pdf.equals("") || !checkbox_doc.equals("") || !checkbox_xlsx.equals("") || !checkbox_txt.equals("") || (!ext.equals("多个用逗号分隔") && !ext.equals("")) || docSize.equals("2") || docSize.equals("3") || docSize.equals("4")) {
        sql += ",document_attach as a where a.doc_id = d.id and d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN + " and ((1=1";
    } else {
        sql += " where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN + " and ((1=1";
    }
    if (lp.canUserSee(privilege.getUser(request)) && !lp.canUserExamine(privilege.getUser(request))) {
        sql += " and d.id not in (";
        sql += "select doc_id from doc_priv ";
        sql += "where see= 0 and (";
        //当个用户
        sql += "name=" + StrUtil.sqlstr(uName);
        //角色
        sql += " or name in(select roleCode from user_of_role where userName =" + StrUtil.sqlstr(uName) + ")";
        //用户角色组
        sql += " or name in(select code from user_group g,user_of_group ug where g.code = ug.group_code and  user_name =" + StrUtil.sqlstr(uName) + ")";
        sql += " or name in( select code from user_group_of_role rg,user_group g,user_of_role r   where rg.userGroupCode = g.code and r.roleCode = rg.roleCode and r.userName =" + StrUtil.sqlstr(uName) + ")";
        sql += ")";
        sql += ")";

    }

    if (!parentCode.equals("")) {
        if (!plp.canUserModify(privilege.getUser(request))) {
            sql += " and examine=" + Document.EXAMINE_PASS;
        }
    } else if (!dir_code.equals("")) {
        if (!lp.canUserModify(privilege.getUser(request))) {
            sql += " and examine=" + Document.EXAMINE_PASS;
        }
    }

    sql += ") or (author=" + StrUtil.sqlstr(uName) + "))";

    if (!kind.equals("")) {
        sql += " and kind=" + StrUtil.sqlstr(kind);
    }

    if (op.equals("search")) {

        if (searchKind.equals("title")) {
            sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
        } else if (searchKind.equals("content")) {
            //sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate from document as d, doc_content as c where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN;
            sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
        } else {
            sql += " and keywords like " + StrUtil.sqlstr("%" + what + "%");
        }

        if (examine != -1) {
            sql += " and examine=" + examine;
        }

        if (!title.equals("")) {
            sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
        }
        if (!content.equals("")) {
            sql += " and c.content like " + StrUtil.sqlstr("%" + content + "%");
        }
        if (!keywords1.equals("")) {
            sql += " and d.keywords like " + StrUtil.sqlstr("%" + keywords1 + "%");
        }
        if (!author.equals("")) {
            sql += " and d.author like " + StrUtil.sqlstr("%" + author + "%");
        }

        if (!kind1.equals("")) {
            sql += " and kind=" + StrUtil.sqlstr(kind1);
        }

        if (!keywords.equals("not") && !keywords.equals("")) {
            Directory directory = new Directory();
            Leaf leaf1 = directory.getLeaf(keywords);
            sql += " and d.class1 = " + StrUtil.sqlstr(leaf1.getCode());
        }

        if (examine1 != -1) {
            sql += " and examine=" + examine1;
        }
        //上一周
        if (modifyType.equals("2")) {
            sql += " and modifiedDate >=" + StrUtil.sqlstr(weekMothUtil.getPreviousWeekday()) + " and modifiedDate<=" + StrUtil.sqlstr(weekMothUtil.getPreviousWeekSunday());
        }
        //上一个月
        if (modifyType.equals("3")) {
            sql += " and modifiedDate >=" + StrUtil.sqlstr(weekMothUtil.getPreviousMonthFirst()) + " and modifiedDate<=" + StrUtil.sqlstr(weekMothUtil.getPreviousMonthEnd());
        }
        //上一年一年内
        if (modifyType.equals("4")) {
            Calendar curr = Calendar.getInstance();
            curr.set(Calendar.YEAR, curr.get(Calendar.YEAR) - 1);
            curr.set(Calendar.MONTH, 0);
            curr.set(Calendar.DAY_OF_MONTH, 1);
            curr.set(Calendar.HOUR_OF_DAY, 0);
            curr.set(Calendar.MINUTE, 0);
            curr.set(Calendar.SECOND, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar curr1 = Calendar.getInstance();
            curr1.set(Calendar.YEAR, curr1.get(Calendar.YEAR) - 1);
            curr1.set(Calendar.MONTH, 11);
            curr1.set(Calendar.DAY_OF_MONTH, 31);
            curr1.set(Calendar.HOUR_OF_DAY, 23);
            curr1.set(Calendar.MINUTE, 59);
            curr1.set(Calendar.SECOND, 59);

            sql += " and modifiedDate >=" + StrUtil.sqlstr(sdf.format(curr.getTime())) + " and modifiedDate <=" + StrUtil.sqlstr(sdf.format(curr1.getTime()));
        }
        if (modifyType.equals("5")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sql += " and modifiedDate >=" + StrUtil.sqlstr(sdf.format(sdf.parse(fromDate + " 00:00:00"))) + " and modifiedDate <=" + StrUtil.sqlstr(sdf.format(sdf.parse(toDate + " 23:59:59")));
        }
        if (!checkbox_png.equals("") || !checkbox_ppt.equals("") || !checkbox_gif.equals("") || !checkbox_zip.equals("") || !checkbox_pdf.equals("") || !checkbox_doc.equals("") || !checkbox_xlsx.equals("") || !checkbox_txt.equals("") || (!ext.equals("多个用逗号分隔") && !ext.equals(""))) {
            String toghterExt = "'aa'";
            if (!checkbox_png.equals("")) {
                toghterExt += ",'" + checkbox_png + "'";
            }
            if (!checkbox_ppt.equals("")) {
                toghterExt += ",'" + checkbox_ppt + "'";
            }
            if (!checkbox_gif.equals("")) {
                toghterExt += ",'" + checkbox_gif + "'";
            }
            if (!checkbox_zip.equals("")) {
                toghterExt += ",'" + checkbox_zip + "'";
            }
            if (!checkbox_pdf.equals("")) {
                toghterExt += ",'" + checkbox_pdf + "'";
            }
            if (!checkbox_doc.equals("")) {
                toghterExt += ",'" + checkbox_doc + "'";
            }
            if (!checkbox_xlsx.equals("")) {
                toghterExt += ",'" + checkbox_xlsx + "'";
            }
            if (!checkbox_txt.equals("")) {
                toghterExt += ",'" + checkbox_txt + "'";
            }

            String[] extArr = ext.split(",");
            for (int i = 0; i < extArr.length; i++) {
                if (!extArr[i].equals("多个用逗号分隔")) {
                    if ((i + 1) == extArr.length) {
                        toghterExt += ",'" + extArr[i] + "'";
                    } else {
                        toghterExt += ",'" + extArr[i] + "'";
                    }
                }
            }
            sql += " and a.ext in (" + toghterExt + ")";
        }

        if (docSize.equals("2")) {
            sql += " and a.file_size<=" + 10 * 1024 * 1024;
        }
        if (docSize.equals("3")) {
            sql += " and a.file_size>" + 10 * 1024 * 1024 + " and a.file_size<=" + 50 * 1024 * 1024;
        }
        if (docSize.equals("4")) {
            sql += " and a.file_size>" + 50 * 1024 * 1024;
        }

    }
    if (!parentCode.equals("")) {
        sql += " and (parent_code=" + StrUtil.sqlstr(parentCode) + " or class1=" + StrUtil.sqlstr(parentCode) + ")";
    } else if (!dir_code.equals("")) {
        if (!dir_code.equalsIgnoreCase("root")) {
            if (keywords.equals("") || keywords.equals("not")) {
                sql += " and class1=" + StrUtil.sqlstr(dir_code);
            }

        }
    }

    sql += " order by doc_level desc, examine asc, " + orderBy + " " + sort;

// out.print(sql);
    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if (strcurpage.equals(""))
        strcurpage = "1";
    if (!StrUtil.isNumeric(strcurpage)) {
        out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
        return;
    }
    int pagesize = ParamUtil.getInt(request, "pageSize", 20);
    int curpage = Integer.parseInt(strcurpage);
    JdbcTemplate jt = new JdbcTemplate();
    ResultIterator ri = jt.executeQuery(sql, Integer.parseInt(strcurpage), pagesize);
    ResultRecord rr = null;

    long total = jt.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
//设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    DirKindDb dkd = new DirKindDb();
    Vector vkind = dkd.listOfDir(dir_code);
    SelectOptionDb sod = new SelectOptionDb();
    if (vkind.size() > 0) {
%>
<div class="tabs1Box">
    <div id="tabs1">
        <ul>
            <li id="menu"><a
                    href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&<%=prjUrl %>"><span>全部</span></a>
            </li>
            <%
                Iterator irkind = vkind.iterator();
                while (irkind.hasNext()) {
                    dkd = (DirKindDb) irkind.next();
            %>
            <li id="menu<%=dkd.getKind()%>"><a
                    href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&kind=<%=StrUtil.UrlEncode(dkd.getKind())%>&<%=prjUrl %>"><span><%=sod.getOptionName("fileark_kind", dkd.getKind())%></span></a>
            </li>
            <%}%>
        </ul>
    </div>
</div>
<script>
    o("menu<%=kind%>").className = "current";
</script>
<%}%>
<form name="form" id="form1" action="document_list_m.jsp?<%=prjUrl %>" method="post">
    <table id="searchTable" width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98"
           style="position:relative;display:none;">
        <tr>
            <td align="left">
                <script>
                    if (typeof(window.parent.leftFileFrame) == "object") {
                        // document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">关闭菜单</span></a>");
                    }
                </script>
                &nbsp;
                <!-- <select id="searchKind" name="searchKind">
		          <option value="title">标题</option>
				  <%if (!Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {%>
		          <option value="content">内容</option>
				  <%}%>
		          <option value="keywords">关键字</option>
		        </select> -->
                <%if (privilege.isUserPrivValid(request, "admin") || lp.canUserModify(privilege.getUser(request))) {%>
                <!-- 审核
			        <select id="examine" name="examine">
			          <option value="-1" selected>不限</option>
			          <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
			          <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
			          <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
			        </select> -->
                <%}%>
                <%if (dir_code.equals("")) {%>
                <!--  类别
		            <select id="kind" name="kind">
		            <option value="">无</option>
		            <%
					SelectMgr sm = new SelectMgr();
					SelectDb sd = sm.getSelect("fileark_kind");
					Vector vsd = sd.getOptions();
					Iterator irsd = vsd.iterator();
		            while (irsd.hasNext()) {
		                  sod = (SelectOptionDb)irsd.next();
		              %>
		                <option value="<%=sod.getValue()%>" <%=kind.equals(sod.getValue())?"selected":""%>><%=sod.getName()%></option>
		              <%
		            }%>
		            </select> -->
                <%} else {%>
                <!-- <input id="kind" name="kind" type="hidden" value="<%=kind%>"/> -->
                <%}%>
                &nbsp;
                <!-- <input name=what size=20 value="<%=what%>"/> -->
                &nbsp;
                <!-- <input class="tSearch" name="submit" type=submit value="搜索"/> -->
                <input name="dir_code" type="hidden" value="<%=dir_code%>"/>
                <input name="parentCode" type="hidden" value="<%=parentCode%>"/>
                <input name="op" type="hidden" value="search"/>

            </td>
        </tr>
    </table>

    <table>

        <div id="search_div" style="display:none;overflow:auto">
            <input type="hidden" name="clickTimes" id="clickTimes" value="0"/>
            <div class="document_cont" id="document_cont">
                <h1 id="doc_h1"><img src="<%=SkinMgr.getSkinPath(request)%>/images/document_icon1.png" width="37"
                                     height="25" id="searchImg"/>搜索条件</h1>
                <div class="document_sch" id="document_sch">
                    <div class="document_sch_title" id="document_sch_title">文档名称内容：</div>
                    <ul id="doc_search">
                        <li id="doc_name">文档名称：<input name="title" id="title" type="text" value="<%=title %>"/></li>
                        <li id="doc_author">作&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;者：&nbsp;<input name="author"
                                                                                                id="author" type="text"
                                                                                                value="<%=author %>"/>
                        </li>
                        <li id="doc_content">文档内容：<input name="content" id="content" type="text" value="<%=content %>"/>
                        </li>
                        <li id="doc_keywords">关 键 字：&nbsp;<input name="keywords1" id="keywords1" type="text"
                                                                 value="<%=keywords1 %>"/></li>
                        <%if (privilege.isUserPrivValid(request, "admin") || lp.canUserModify(privilege.getUser(request))) {%>
                        <li id="doc_examine">
                            审&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;核：
                            <select id="examine1" name="examine1" style="width:170px;">
                                <option value="-1" selected>不限</option>
                                <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
                                <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
                                <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
                            </select>
                        </li>
                        <script>
                            o("examine1").value = "<%=examine1%>";
                        </script>
                        <%}%>


                        <%if (dir_code.equals("")) {%>
                        <li id="doc_kind">类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;别：
                            <select id="kind1" name="kind1" style="width:170px;">
                                <option value="">无</option>
                                <%
                                    SelectMgr sm = new SelectMgr();
                                    SelectDb sd = sm.getSelect("fileark_kind");
                                    Vector vsd = sd.getOptions();
                                    Iterator irsd = vsd.iterator();
                                    while (irsd.hasNext()) {
                                        sod = (SelectOptionDb) irsd.next();
                                %>
                                <option value="<%=sod.getValue()%>" <%=kind.equals(sod.getValue()) ? "selected" : ""%>><%=sod.getName()%>
                                </option>
                                <%
                                    }%>
                            </select>
                        </li>
                        <%} else {%>
                        <input id="kind1" name="kind1" type="hidden" value="<%=kind1%>"/>
                        <%}%>
                        <script>
                            o("kind1").value = "<%=kind1%>";
                        </script>
                        <li id="doc_directory">所在目录：
                            <!--  <select id="keywords" name="keywords" style="width:170px">
					<option value="not" selected>请选择目录</option>
					<%
					//Leaf lf = dir.getLeaf("root");
					//DirectoryView dv = new DirectoryView(request, lf);
					//dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
					String directory = "";
					if(!dir_code.equals("")){
						Leaf lf = dir.getLeaf(dir_code);
						directory = lf.getName();
					}
					
					%>
					
					</select>-->
                            <input value="<%=directory %>" id="directory"/>
                            &nbsp;<a href="javascript:;" onclick="selDept()">选择</a>

                        </li>
                    </ul>

                </div>
                <div id="displayId"><span style="margin-left:8px;">高级选项</span><a id="afBtn" href="javascript:;">
                    &nbsp;<img id="afBtnImg" src="<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png"/></a>
                    <div class="document_sch" id="document_sch_1" style="display:none;">
                        <div class="document_sch_title" id="document_sch_title_1">附件类型：</div>
                        <div id="doc_file">
                            <ul class="document_sch_ul" id="document_sch_ul">
                                <li id="doc_png">
                                    <input type="checkbox" name="checkbox_png" id="checkbox_png"
                                           value="png" <%=checkbox_png.equals("png") ? "checked" : "" %>/>&nbsp;PNG
                                </li>
                                <li id="doc_gif">
                                    <input type="checkbox" name="checkbox_gif" id="checkbox_gif"
                                           value="gif" <%=checkbox_gif.equals("gif") ? "checked" : "" %>/>&nbsp;GIF
                                </li>
                                <li id="doc_pdf">
                                    <input type="checkbox" name="checkbox_pdf" id="checkbox_pdf"
                                           value="pdf" <%=checkbox_pdf.equals("pdf") ? "checked" : "" %>/>&nbsp;PDF
                                </li>
                                <li id="doc_xlsx">
                                    <input type="checkbox" name="checkbox_xlsx" id="checkbox_xlsx"
                                           value="xlsx" <%=checkbox_xlsx.equals("xlsx") ? "checked" : "" %>/>&nbsp;XLSX
                                </li>
                            </ul>
                            <ul class="document_sch_ul" id="document_sch_ul_1">
                                <li id="doc_ppt">
                                    <input type="checkbox" name="checkbox_ppt" id="checkbox_ppt"
                                           value="ppt" <%=checkbox_ppt.equals("ppt") ? "checked" : "" %>/>&nbsp;PPT
                                </li>
                                <li id="doc_zip">
                                    <input type="checkbox" name="checkbox_zip" id="checkbox_zip"
                                           value="zip" <%=checkbox_zip.equals("zip") ? "checked" : "" %>/>&nbsp;ZIP
                                </li>
                                <li id="doc_doc">
                                    <input type="checkbox" name="checkbox_doc" id="checkbox_doc"
                                           value="doc" <%=checkbox_doc.equals("doc") ? "checked" : "" %>/>&nbsp;DOC
                                </li>
                                <li id="doc_txt">
                                    <input type="checkbox" name="checkbox_txt" id="checkbox_txt"
                                           value="txt" <%=checkbox_txt.equals("txt") ? "checked" : "" %>/>&nbsp;TXT
                                </li>
                            </ul>
                        </div>
                        <div style="display:inline-block"><p id="doc_text">指定后缀： <input name="ext" id="ext" type="text"
                                                                                        onclick="clearAlter()"
                                                                                        onblur="onBlurEvent()"
                                                                                        value="<%=ext %>"
                                                                                        style="width:150px;"/></p></div>
                    </div>
                    <div class="document_sch" id="document_sch_2" style="display:none;">
                        <div class="document_sch_title" id="document_sch_title_2">修改时间：</div>
                        <ul id="doc_time">
                            <li id="doc_radio_1">
                                <input type="radio" name="dateType" id="radio_1" value="1" checked/>&nbsp;不记得
                            </li>
                            <li id="doc_radio_2">
                                <input type="radio" name="dateType" id="radio_2" value="2"/>&nbsp;上个星期
                            </li>
                            <li id="doc_radio_3">
                                <input type="radio" name="dateType" id="radio_3" value="3"/>&nbsp;上个月
                            </li>
                            <li id="doc_radio_4">
                                <input type="radio" name="dateType" id="radio_4" value="4"/>&nbsp;去年一年内
                            </li>
                            <li id="doc_radio_5">
                                <input type="radio" name="dateType" id="radio_5" value="5"/>&nbsp;指定时间<br/>
                                从 <input type="text" id="fromDate" name="fromDate" size="10" style="width:100px;"
                                         value="<%=fromDate %>"/>

                                到 <input type="text" id="toDate" name="toDate" size="10" style="width:100px;"
                                         value="<%=toDate %>"/>

                            </li>
                        </ul>
                        <script>
                            setRadioValue("dateType", "<%=modifyType%>");
                        </script>
                    </div>
                    <div class="document_sch" id="document_sch_3" style="display:none;">
                        <div class="document_sch_title" id="document_sch_title_3">附件大小：</div>
                        <ul id="doc_size">
                            <li id="doc_size1">
                                <input type="radio" name="docSize" id="doc_size_1" value="1" checked/>&nbsp;不记得
                            </li>
                            <li id="doc_size2">
                                <input type="radio" name="docSize" id="doc_size_2" value="2"/>&nbsp;小于等于10M
                            </li>
                            <li id="doc_size3">
                                <input type="radio" name="docSize" id="doc_size_3" value="3"/>&nbsp;大于10M并且小于等于50M
                            </li>
                            <li id="doc_size4">
                                <input type="radio" name="docSize" id="doc_size_4" value="4"/>&nbsp;大于50M
                            </li>
                        </ul>
                        <script>
                            setRadioValue("docSize", "<%=docSize%>");
                        </script>
                    </div>
                    <div class="document_sch_p" id="document_sch_p">
                        <input type="submit" name="submit" id="search" class="document_sch_btn" value="搜索"
                               style="margin-right:10px;"/>
                        <input type="button" name="submit" id="reset" class="document_sch_btn" onclick="reSet()"
                               value="重置"/>
                    </div>
                </div>
            </div>
            <script>
                $(function () {
                    $(document).bind("click", function (e) {
                        e = e || window.event;
                        var dom = e.srcElement || e.target;
                        try {
                            if (dom.id == "" && dom.className != "search" && dom.parentNode.id == "" && dom.parentNode.className.indexOf("daysrow") == -1 && dom.parentNode.className.indexOf("hilite") == -1 && dom.parentNode.className != "button" && dom.parentNode.className != "daynames" && dom.parentNode.className != "footrow" && dom.className != "title" && (dom.parentNode.className != "" && dom.parentNode.className != "xdsoft_mounthpicker" && dom.parentNode.className != "xdsoft_label xdsoft_year" && dom.parentNode.className != "xdsoft_label xdsoft_month" && dom.parentNode.className != "xdsoft_datetimepicker xdsoft_noselect ")) {
                                document.getElementById("search_div").style.display = "none";
                                document.getElementById("clickTimes").value = "0";
                            }
                        } catch (e) {
                        }
                    });
                });
            </script>
        </div>
    </table>
    <table>
        <div class="upload_sel" id="upload_sel" style="display:none">
            <input type="hidden" name="clickTimes1" id="clickTimes1" value="0"/>
            <ul id="aa">
                <li id="bb">
                    <a><span class="TextStyle" id="spanButtonPlaceholder"></span></a>
                </li>
                <li class="clfT" style="text-align:center;" id="cc">
                    <a href="javascript:void(0)">极速</a>
                </li>
            </ul>
        </div>
        <script>
            $(function () {
                $(document).bind("click", function (e) {
                    e = e || window.event;
                    var dom = e.srcElement || e.target;
                    try {
                        if (dom.id == "" && dom.parentNode.id == "") {
                            document.getElementById("upload_sel").style.display = "none";
                            document.getElementById("clickTimes1").value = "0";
                        }
                    } catch (e) {
                    }
                });
            });
        </script>
    </table>

    <table id="grid" cellSpacing="0" cellPadding="0" width="1028">
        <thead>
        <tr>
            <th width="20"><input type="checkbox"
                                  onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')"/></th>
            <%if (!parentCode.equals("")) {%>
            <th width="86" noWrap>文件夹</th>
            <%}%>
            <th width="35" align="center">类型</th>
            <th width="397" noWrap>标题</th>
            <th width="110" abbr="author">发布者</th>
            <th width="112" abbr="modifiedDate">修改日期</th>
            <th width="60" abbr="examine" noWrap>审核</th>
            <th width="180" noWrap>操作</th>
        </tr>
        </thead>
        <tbody>
        <%
            String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&searchKind=" + searchKind + "&what=" + StrUtil.UrlEncode(what) + "&examine=" + examine;
            querystr += "&examine1=" + examine1 + "&kind=" + StrUtil.UrlEncode(kind) + "&kind1=" + StrUtil.UrlEncode(kind1);
            querystr += "&title=" + StrUtil.UrlEncode(title) + "&content=" + StrUtil.UrlEncode(content) + "&modifyType=" + StrUtil.UrlEncode(modifyType);
            querystr += "&docSize=" + StrUtil.UrlEncode(docSize) + "&fromDate=" + StrUtil.UrlEncode(fromDate) + "&toDate=" + StrUtil.UrlEncode(toDate);
            querystr += "&keywords=" + StrUtil.UrlEncode(keywords) + "&keywords1=" + StrUtil.UrlEncode(keywords1) + "&author=" + StrUtil.UrlEncode(author);
            querystr += "&ext=" + StrUtil.UrlEncode(ext) + "&checkbox_png=" + StrUtil.UrlEncode(checkbox_png) + "&checkbox_ppt=" + StrUtil.UrlEncode(checkbox_ppt);
            querystr += "&checkbox_gif=" + StrUtil.UrlEncode(checkbox_gif) + "&checkbox_zip=" + StrUtil.UrlEncode(checkbox_zip) + "&checkbox_pdf=" + StrUtil.UrlEncode(checkbox_pdf);
            querystr += "&checkbox_doc=" + StrUtil.UrlEncode(checkbox_doc) + "&checkbox_xlsx=" + StrUtil.UrlEncode(checkbox_xlsx) + "&checkbox_txt=" + StrUtil.UrlEncode(checkbox_txt);

            Document doc = new Document();
            DocPriv dp = new DocPriv();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                boolean isHome = rr.getInt("isHome") == 1 ? true : false;
                String color = StrUtil.getNullStr(rr.getString("color"));
                boolean isBold = rr.getInt("isBold") == 1;
                java.util.Date expireDate = rr.getDate("expire_date");
                doc = doc.getDocument(rr.getInt("id"));
        %>
        <tr id=<%=rr.getInt("id")%> onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'"
            class="tbg1">
            <td><input type="checkbox" name="ids" value="<%=rr.getInt("id")%>"/></td>
            <%
                if (!parentCode.equals("")) {
                    Leaf clf = new Leaf();
                    clf = clf.getLeaf(doc.getDirCode());
            %>

            <td noWrap>
                <a href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=prjUrl %>"><%=clf.getName()%>
                </a>
            </td>
            <%}%>
            <td>
                <%
                    String icon = doc.getFileIcon();
                %>
                <img src="images/<%=icon%>" style="width:20px;height:20px;"/>
            </td>
            <td height="24">
                <%if (rr.getInt("type") == Document.TYPE_VOTE) {%>
                <IMG height=15 alt="" src="../forum/skin/bluedream/images/f_poll.gif" width=17 border=0>&nbsp;
                <%}%>
                <%
                    if (!doc.getKind().equals("")) {
                        if (sod == null)
                            sod = new SelectOptionDb();
                %>
                <a href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&kind=<%=StrUtil.UrlEncode(doc.getKind())%>&<%=prjUrl %>">[&nbsp;<%=sod.getOptionName("fileark_kind", doc.getKind())%>
                    &nbsp;]</a>
                <%
                    }
                    if (DateUtil.compare(new java.util.Date(), expireDate) == 2) {%>
                <a href="javascript:;" onclick="addTab('<%=rr.getString(2)%>', '<%=viewPage%>?id=<%=rr.getInt("id")%>')"
                   title="<%=rr.getString(2)%>">
                    <%
                        if (isBold)
                            out.print("<B>");
                        if (!color.equals("")) {
                    %>
                    <font color="<%=color%>">
                        <%}%>
                        <%=rr.getString("title")%>
                        <%if (!color.equals("")) {%>
                    </font>
                    <%}%>
                    <%
                        if (isBold)
                            out.print("</B>");
                    %>
                </a>
                <%
                } else {
                    if (rr.getInt("type") == 2) {
                        Vector attachments = doc.getAttachments(1);
                        Iterator ir = attachments.iterator();
                        boolean isImage = false;
                        while (ir.hasNext()) {
                            Attachment am = (Attachment) ir.next();
                            if (StrUtil.isImage(StrUtil.getFileExt(am.getDiskName()))) {
                                isImage = true;
                            }
                            if (isImage) {
                                if (lp.canUserDownLoad(privilege.getUser(request)) && dp.canUserDownload(privilege.getUser(request), rr.getInt("id"))) {
                %>
                <a target=_blank
                   href="doc_getfile.jsp?pageNum=1&id=<%=rr.getInt("id")%>&attachId=<%=am.getId()%>"><%=rr.get("title")%>
                </a>
                <%} else {%>
                <%=rr.get("title")%>
                <%
                    }
                } else {
                    String fileExt = StrUtil.getFileExt(am.getDiskName());
                    if (fileExt.equals("doc") || fileExt.equals("docx") || fileExt.equals("xls") || fileExt.equals("xlsx") || fileExt.equals("ppt") || fileExt.equals("pptx")) {
                %>
                <a target=_blank
                   href="fileark_ntko_show.jsp?pageNum=1&docId=<%=rr.getInt("id")%>&attachId=<%=am.getId()%>"><%=rr.get("title")%>
                </a>
                <%
                } else if (fileExt.equals("pdf")) {
                    String pdfSql = "select visualpath,diskname from document_attach where doc_id = " + StrUtil.sqlstr(String.valueOf(doc.getId()));
                    String fullPath = "";
                    String diskName = "";
                    JdbcTemplate pdfjt = new JdbcTemplate();
                    ResultIterator pdfri = pdfjt.executeQuery(pdfSql);
                    while (pdfri.hasNext()) {
                        ResultRecord pdfrd = (ResultRecord) pdfri.next();
                        fullPath = pdfrd.getString(1);
                        diskName = pdfrd.getString(2);
                    }%>
                <a target=_blank
                   href="pdf_js/viewer.html?file=<%=request.getContextPath()+"/"+fullPath+"/"+diskName%>"><%=rr.get("title")%>
                </a>
                <%
                } else {
                    if (lp.canUserDownLoad(privilege.getUser(request)) && dp.canUserDownload(privilege.getUser(request), rr.getInt("id"))) {
                %>
                <a href="javascript:;" onclick="downLoadDoc(<%=rr.getInt("id")%>, <%=am.getId()%>)"><%=rr.get("title")%>
                </a>
                <%
                } else {%>
                <%=rr.get("title")%>
                <%
                    }
                %>
                <%
                            }
                        }
                    }
                } else {
                %>
                <a href="javascript:;"
                   onclick="addTab('<%=rr.getString("title")%>', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=rr.getInt("id")%>')"
                   title="<%=rr.getString("title")%>">
                    <font color="<%=color%>">
                        <%if (isBold) { %>
                        <b><%=rr.get("title")%>
                        </b>
                        <% } else {%>
                        <%=rr.get("title")%>
                        <%}%>
                    </font>
                </a>
                <%} %>
                <%
                    DocLogDb dldb = new DocLogDb();
                    if (!dldb.isUserReaded(userName1, doc.getId())) {
                %>
                &nbsp;
                <img src="../images/icon_new.gif"/>
                <%
                    }
                %>
                <%}%></td>
            <%
                String userName = "";
                UserDb ud = new UserDb();
                userName = (doc != null) ? doc.getNick() : privilege.getUser(request);
                ud = ud.getUserDb(userName);
                String realName = "";
                if (ud != null && ud.isLoaded())
                    realName = StrUtil.getNullStr(ud.getRealName());
            %>
            <td><%=realName.equals("") ? userName : realName %>
                <input type="hidden" id="myName<%=rr.getInt("id")%>" value="<%=userName%>"/>
                <input type="hidden" id="myTitle<%=rr.getInt("id")%>" value="<%=rr.get("title")%>"/>
            </td>
            <td><%
                java.util.Date d = rr.getDate("modifiedDate");
                if (d != null)
                    out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
            %>
            </td>
            <td align="center">
                <%
                    int ex = rr.getInt("examine");
                    if (ex == 0)
                        out.print("<font color='blue'>未审核</font>");
                    else if (ex == 1)
                        out.print("<font color='red'>未通过</font>");
                    else
                        out.print("已通过");
                %>
            </td>
            <td align="center">
                <%
                    Attachment am = null;
                    if (rr.getInt("type") == Document.TYPE_FILE) {
                        Vector attachments = doc.getAttachments(1);
                        Iterator ir = attachments.iterator();
                        if (ir.hasNext()) {
                            am = (Attachment) ir.next();
                        }
                %>
                <input type="hidden" id="isFile<%=rr.getInt("id")%>" value="<%=rr.getInt("type")%>"/>
                <input type="hidden" id="hiddenTitle<%=rr.getInt("id")%>" value="<%=rr.getString("title")%>"/>
                <input type="hidden" id="attachId<%=rr.getInt("id")%>" value="<%=am.getId()%>"/>
                <%
                    }

                    Leaf lf6 = dir.getLeaf(rr.getString("class1"));
                    if (lf6 == null) {
                        out.print("</td></tr>");
                        continue;
                    }
                    lp = new LeafPriv(lf6.getCode());
                    if (lp.canUserDownLoad(privilege.getUser(request)) && dp.canUserDownload(request, doc.getId())) {
                        if (rr.getInt("type") == Document.TYPE_FILE) {
                %>
                &nbsp;<a href="javascript:;" onclick="downLoadDoc(<%=rr.getInt("id")%>, <%=am.getId()%>)">下载</a>
                <%
                } else { %>
                <!-- <a href="../<%=pageUrl%>?op=edit&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(rr.getString("class1"))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">编辑</a>&nbsp; -->
                <%
                        }
                    }
                    if (lp.canUserDel(privilege.getUser(request)) && dp.canUserSee(request, doc.getId())) {
                %>
                <!-- <a onClick="return confirm('您确定要删除吗？')" href="document_list_m.jsp?op=del&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&<%=prjUrl %>">删除</a>&nbsp; -->
                <%
                    }
                    if (lp.canUserExamine(privilege.getUser(request))) {
                %>
                <!-- <a href="document_list_m.jsp?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=<%=rr.getInt("id")%>&<%=prjUrl %>">通过</a> -->
                <%if (rr.getInt("doc_level") != Document.LEVEL_TOP) {%>
                &nbsp;<a href="javascript:;" onclick="isTop(<%=Document.LEVEL_TOP%>,<%=rr.getInt("id")%>)">置顶</a>
                <%} else {%>
                &nbsp;<a href="javascript:;" onclick="noTop(<%=rr.getInt("id")%>)">取消置顶</a>
                <%}%>
                <%}%>
                <%if (lp.canUserExamine(privilege.getUser(request)) || dp.canUserManage(request, rr.getInt("id"))) { %>
                &nbsp;<a href="javascript:;"
                         onclick="addTabPriv(<%=rr.getInt("id") %>,'<%=rr.getString("title") %>','<%=dir_code %>','<%=dir_name %>')">权限</a>
                <%if (lf6.isLog()) {%>
                &nbsp;<a href="javascript:;"
                         onclick="addTabLog(<%=rr.getInt("id") %>,'<%=rr.getString("title") %>')">日志</a>
                <%}%>
                <%if (isRobotOpen) {%>
                &nbsp;<a href="javascript:;"
                         onclick="shareToQQGroups(<%=rr.getInt("id")%>, '<%=rr.getString("title")%>')">群分享</a>
                <%}%>
                <%}%>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <input type="hidden"
           value="swfupload.jsp;jsessionid=<%=session.getId() %>?userName=<%=StrUtil.UrlEncode(userName1) %>&dirCode=<%=StrUtil.UrlEncode(dir_code) %>"
           id="uploadUrl"/>
    <input type="hidden" value="<%=file_size_limit %>" id="fileSizeLimit"/>
    <input type="hidden" value="<%=file_upload_limit %>" id="fileUploadLimit"/>
    <input type="hidden" value="<%=upload_file_types %>" id="uploadFileType"/>
    <input type="hidden" value="<%=fixfox_upload_file_types %>" id="FixfoxUploadFileType"/>
    <input type="hidden" value="<%=dir_code %>" id="dirCode"/>
    <input type="hidden" id="cooperateId" value=""/>
</form>
<form name="addForm" action="swfupload.jsp" enctype="multipart/form-data">
    <table width="33%" border="0" cellspacing="0" cellpadding="0" name="ctlTable" id="ctlTable"
           style="height: 150px; font-size: 15px; position: fixed; top: 200px; left: 200px; display: none; z-index: -100">
        <tr>
            <td>
                <tbody class="window_upload">
                <tr>
                    <td class='SD_upload'></td>
                    <td class='SD_upload'></td>
                    <td class='SD_upload'></td>
                </tr>
                <tr>
                    <td class='SD_upload'></td>
                    <td id='upload_container'>
                        <h3 id='upload_title'>
                            极速上传
                        </h3>
                        <div id='upload_body'
                             style='height: 100px; width: 400px; overflow: auto;'>
                            <div id='upload_content'>
                                <table border="0" align="center" cellpadding="0"
                                       cellspacing="1">
                                    <tr>
                                        <td>
                                            <div
                                                    style="width: 100%; height: 75px; margin-top: 10px; border: 0px solid #cccccc">
                                                <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C"
                                                        codebase="../activex/cloudym.CAB#version=1,2,0,1" width="400"
                                                        style="height:75px;" align="middle" id="webedit">
                                                    <param name="Encode" value="utf-8"/>
                                                    <param name="MaxSize" value="<%=Global.MaxSize%>"/>
                                                    <!--上传字节-->
                                                    <param name="ForeColor" value="(0,0,0)"/>
                                                    <param name="BgColor" value="(255,255,255)"/>
                                                    <param name="ForeColorBar" value="(255,255,255)"/>
                                                    <param name="BgColorBar" value="(104,181,200)"/>
                                                    <param name="ForeColorBarPre" value="(0,0,0)"/>
                                                    <param name="BgColorBarPre" value="(230,230,230)"/>
                                                    <param name="FilePath" value="<%=filePath%>"/>
                                                    <param name="Relative" value="1"/>
                                                    <!--上传后的文件需放在服务器上的路径-->
                                                    <param name="Server" value="<%=request.getServerName()%>"/>
                                                    <param name="Port" value="<%=request.getServerPort()%>"/>
                                                    <param name="VirtualPath" value="<%=Global.virtualPath%>"/>
                                                    <param name="PostScript"
                                                           value="<%=Global.virtualPath%>/fileark/swfupload.jsp?type=1&dirCode=<%=dir_code %>"/>
                                                    <param name="PostScriptDdxc" value=""/>
                                                    <param name="SegmentLen" value="204800"/>
                                                    <param name="info" value="文件拖放区"/>
                                                    <%
                                                        License license = License.getInstance();
                                                    %>
                                                    <param name="Organization" value="<%=license.getCompany()%>">
                                                    <param name="Key" value="<%=license.getKey()%>">
                                                </object>
                                            </div>
                                            <script>//initUpload()</script>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                        <div id='SD_button'>
                            <div class='SD_button'>
                                <!-- <a
                                    onclick="addForm.webedit.OpenFolderDlg();if (addForm.webedit.GetFiles()=='') return false; else SubmitWithFileThread()">上传文件</a> -->
                                <a onclick="window_remove()">关闭</a>
                            </div>
                        </div>
                        <a href='javascript:;' id='upload_close' title='关闭'></a>
                    </td>
                </tr>
                </tbody>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    //重置数据
    function reSet() {
        o("title").value = "";
        o("author").value = "";
        o("content").value = "";
        o("directory").value = "";
        o("ext").value = "多个用逗号分隔";
        o("fromDate").value = "";
        o("toDate").value = "";
        o("keywords1").value = "";
        var objects = document.getElementsByTagName("input");
        for (var i = 0; i < objects.length; i++) {
            if (objects[i].type == 'radio' || objects[i].type == 'checkbox') {
                objects[i].checked = ""
            }
        }

        document.getElementById("radio_1").checked = "checked";
        document.getElementById("doc_size_1").checked = "checked";
    }

    //极速上传未开始， 直接点击关闭
    function window_remove() {
        $("#ctlTable").hide();
        $(".loading").css({"display": "none"});
        $(".treeBackground").css({"display": "none"});
        $(".treeBackground").removeClass("SD_overlayBG2");
        //parent.leftFileFrame.location.href="fileark_left.jsp";
        //window.location.href = "document_list_m.jsp?dir_code=<%=dir_code%>&<%=prjUrl %>";
    }
    function onAddFile(index, fileName, filePath, fileSize, modifyDate) {

    }
    function upload_common() {
        $("#ctlTable").css({"z-index": "1000", "top": "150px"});
        $("#webedit").css({"height": "75px"});
    }

    function SubmitWithFileThread() {
        upload_common();

        loadDataToWebeditCtrl(addForm, addForm.webedit);
        addForm.webedit.Upload();

        // 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
        // 原因是此时服务器的返回信息还没收到
        // alert("ReturnMessage=" + addform.webedit.ReturnMessage);
        window.setTimeout("checkResult()", 200);
    }

    function onDropFile(filePaths) {
        var ary = filePaths.split(",");
        var hasFile = false;
        for (var i = 0; i < ary.length; i++) {
            var filePath = ary[i].trim();
            if (filePath != "") {
                hasFile = true;
                addForm.webedit.InsertFileToList(filePath);
            }
        }
        if (hasFile)
            SubmitWithFileThread();
    }

    /**
     * 检查结果
     * @return
     */
    function checkResult() {
        $(".loading").css({"display": "none"});
        $(".treeBackground").css({"display": "none"});
        $(".treeBackground").removeClass("SD_overlayBG2");
        if (addForm.webedit.ReturnMessage.trim() == "上传成功!") {
            doAfter(true, '<%=dir_code%>');
        } else {
            addForm.webedit.RemoveAllFile();
            window.setTimeout("checkResult()", 200);
        }
    }

    /**
     * 刷新界面
     * @param isSucceed
     * @param dirCode
     * @return
     */
    function doAfter(isSucceed, dirCode) {
        var url = '';
        //if(pageNo == 1){
        url = "document_list_m.jsp&<%=prjUrl %>";
        //}else{
        //	url = "document_list_m.jsp";
        //}
        if (isSucceed) {
            window.location.href = url + "?dir_code=" + dirCode;
            parent.leftFileFrame.location.href = "fileark_left.jsp";
            window.location.href = "document_list_m.jsp?dir_code=<%=dir_code%>&<%=prjUrl %>";
        }
        else {
            jAlert(webedit.ReturnMessage, "提示");
        }
    }

    function clearAlter() {
        var ext = o("ext").value;
        if (ext == "多个用逗号分隔") {
            o("ext").value = "";
        }
    }
    function onBlurEvent() {
        var ext = o("ext").value;
        if (ext == "") {
            o("ext").value = "多个用逗号分隔";
        }
    }

    function searchSubmit() {
        form.submit();
    }

    function addTabLog(id, title) {
        addTab(title + " 日志", "fileark/doc_log.jsp?id=" + id + "&title=" + encodeURI(title));
    }

    function addTabPriv(id, title, dir_code, dir_name) {
        addTab("管理权限", "fileark/doc_priv_m.jsp?doc_id=" + id + "&title=" + encodeURI(title) + "&dir_code=" + encodeURI(dir_code) + "&dir_name=" + encodeURI(dir_name));
    }

    function addTabZip() {
        var ids = getCheckboxValue("ids");
        if (ids == "") {
            jAlert("请选择一条记录！", "提示");
            return;
        }

        $.ajax({
            type: "post",
            url: "downloadValidate.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                ids: ids
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 2) {
                    window.open("../zip_getfile.jsp?ids=" + ids);
                }
                else if (data.ret == 0) {
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                }
                else {
                    jConfirm(data.msg + '\n您确定要打包下载么？', '提示', function (r) {
                        if (r) {
                            window.open("../zip_getfile.jsp?ids=" + ids);
                        }
                    });
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    var flex;

    function changeSort(sortname, sortorder) {
        window.location.href = "document_list_m.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&parentCode=<%=parentCode%>&<%=prjUrl %>";
    }

    function changePage(newp) {
        if (newp)
            window.location.href = "document_list_m.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>" + "&parentCode=<%=parentCode%>&<%=prjUrl %>";
    }

    function rpChange(pageSize) {
        window.location.href = "document_list_m.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>" + "&parentCode=<%=parentCode%>&<%=prjUrl %>";
    }

    function onReload() {
        window.location.reload();
    }

    function showDiv() {
        var times = document.getElementById("clickTimes").value;
        if (times == 0) {
            //document.getElementById("search_div").style.display="block";
            $("#search_div").show();
            document.getElementById("search_div").style.left = ($("#searchFile").offset().left - 6) + "px";
            document.getElementById("clickTimes").value = "1";
        } else {
            //document.getElementById("search_div").style.display="none";
            $("#search_div").hide();
            document.getElementById("clickTimes").value = "0";
        }
    }

    function showDivUpload() {
        var times1 = document.getElementById("clickTimes1").value;
        if (times1 == 0) {
            //document.getElementById("search_div").style.display="block";
            $("#upload_sel").show();
            document.getElementById("upload_sel").style.left = $("#dd").offset().left + "px";
            document.getElementById("clickTimes1").value = "1";
        } else {
            //document.getElementById("search_div").style.display="none";
            $("#upload_sel").hide();
            document.getElementById("clickTimes1").value = "0";
        }
    }

    var buttonObj;
    buttonObj = [
        <%if (!dir_code.equals("") && leaf.getType()==2) {
            if (lp.canUserAppend(privilege.getUser(request))) {%>
        {name: '添加', bclass: 'add', onpress: action},
        <%	}
        }%>
        <%
        if (lp.canUserModify(privilege.getUser(request))) {
        %>
        {name: '编辑', bclass: 'edit', onpress: action},
        <%}%>
        <%
        if (lp.canUserDel(privilege.getUser(request))) {
        %>
        {name: '删除', bclass: 'delete', onpress: action},
        <%}%>
        <%
        if (lp.canUserExamine(privilege.getUser(request))) {
        %>
        {name: '通过', bclass: 'pass', onpress: action},
        {name: '统计', bclass: 'stata', onpress: action},
        <%}%>
        <%if (lp.canUserDownLoad(privilege.getUser(request))) {%>
        {name: '打包', bclass: 'zip', onpress: action},
        <%}%>
        <%if (privilege.isUserPrivValid(request, "admin") || (!dir_code.equals("") && lp.canUserExamine(privilege.getUser(request)))) {%>
        /*{name: '目录', bclass: 'directory', onpress : action},*/
        <%}%>
        {name: '搜索', bclass: 'search', onpress: action, id: 'searchFile'},
        <%if (!dir_code.equals("") && (privilege.isUserPrivValid(request, "admin") || lp.canUserExamine(privilege.getUser(request)))) {%>
        {name: '迁移', bclass: 'changeDir', onpress: action},
        <%}%>
        <%if (!dir_code.equals("") && leaf.getType()==2) {
            if (lp.canUserAppend(privilege.getUser(request))) {%>
        {name: '上传', bclass: 'add', onpress: action, id: 'dd'},
        <%	}
        }%>
        <%if (cfg.getBooleanProperty("fullTextSearchSupported")) {%>
        {name: '全文检索', bclass: 'search', onpress: action},
        <%}%>
        <%if (leaf!=null && leaf.getChildCount()>0) {%>
        {name: '全部', bclass: 'listIncChild', onpress: action},
        <%}%>
        {name: '条件', bclass: '', type: 'include', id: 'searchTable'}
    ];


    $(function () {
        flex = $("#grid").flexigrid
        (
                {
                    buttons: buttonObj,
                    /*
                     searchitems : [
                     {display: 'ISO', name : 'iso'},
                     {display: 'Name', name : 'name', isdefault: true}
                     ],
                     */
                    sortname: "<%=orderBy%>",
                    sortorder: "<%=sort%>",
                    url: false,
                    usepager: true,
                    checkbox: false,
                    page: <%=curpage%>,
                    total: <%=total%>,
                    useRp: true,
                    rp: <%=pagesize%>,

                    // title: "通知",
                    singleSelect: true,
                    resizable: false,
                    showTableToggleBtn: true,
                    showToggleBtn: false,

                    onChangeSort: changeSort,

                    onChangePage: changePage,
                    onRpChange: rpChange,
                    onReload: onReload,
                    /*
                     onRowDblclick: rowDbClick,
                     onColSwitch: colSwitch,
                     onColResize: colResize,
                     onToggleCol: toggleCol,
                     */
                    autoHeight: true,
                    width: document.documentElement.clientWidth,
                    height: document.documentElement.clientHeight - 80
                }
        );

        <%if (op.equals("search")) {%>
        // o("searchKind").value = "<%=searchKind%>";
        //if (o("examine")) {
        //o("examine").value = "<%=examine%>";
        // }
        <%}%>

        var ext = o("ext").value;
        if (ext == "") {
            o("ext").value = "多个用逗号分隔";
        }

        $("#afBtn").click(function () {
            if ($(this).html().indexOf("down") == -1) {
                $('#afBtnImg')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                $('#document_sch_3').hide();
                $('#document_sch_2').hide();
                $('#document_sch_1').hide();
                document.getElementById("search_div").style.height = "355px";

            }
            else {
                $('#afBtnImg')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_up.png";
                $('#document_sch_1').show();
                $('#document_sch_2').show();
                $('#document_sch_3').show();
                document.getElementById("search_div").style.height = "470px";
            }
        });
        $("#afBtn_1").click(function () {
            if ($(this).html().indexOf("down") != -1) {
                $('#afBtnImg_1')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_up.png";
                $('#doc_time').show();
            }
            else {
                $('#afBtnImg_1')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                $('#doc_time').hide();
            }
        });
        $("#afBtn_2").click(function () {
            if ($(this).html().indexOf("down") != -1) {
                $('#afBtnImg_2')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_up.png";
                $('#doc_size').show();
            }
            else {
                $('#afBtnImg_2')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                $('#doc_size').hide();
            }
        });

        var butObject = document.getElementById("spanButtonPlaceholder");
        if (butObject != null) {
            var browserFixfox = navigator.userAgent.indexOf("Firefox") != -1;
            var uploadUrl = $("#uploadUrl").val();
            var fileSizeLimit = $("#fileSizeLimit").val();
            var fileUploadLimit = $("#fileUploadLimit").val();
            var uploadFileType = $("#uploadFileType").val();
            var dirCode = $("#dirCode").val();
            try {
                initSwfUpload({
                    "upload_url": uploadUrl,
                    "post_params": {
                        "type": 0
                    },
                    "file_types": uploadFileType,
                    "file_upload_limit": fileUploadLimit,
                    "file_size_limit": fileSizeLimit,
                    "dirCode": dirCode
                });
            } catch (ex) {
                jAlert(ex, "提示");
            }
        }

        //极速上传
        $('.clfT').live("click", function () {
            $("#ctlTable").css({"display": "block", "top": "150px", "z-index": "1800"});
            $("#webedit").css({"height": "75px"});
            $(".treeBackground").addClass("SD_overlayBG2");
            $("#treeBackground").css({"display": "block"});
        });
        //极速上传取消按钮
        $("#SD_cancel,#upload_close").bind("click", function () {
            window_remove();
            //sd_closeWindow();
        });


        $('#toDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });

        $('#fromDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
    });

    function selectNode(code, name) {
        if (code == '0') {
            jAlert("请选择目录!", "提示");
            return;
        }
        if (code == '<%=dir_code%>') {
            jAlert("请选择其他目录!", "提示");
            return;
        }
        window.location.href = "fileark/document_list_m.jsp?op=changeDir&ids=" + idsSelected + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&newDirCode=" + code + "&<%=prjUrl %>";
    }

    var idsSelected = "";

    function action(com, grid) {
        if (com == '添加') {
            window.location.href = '../<%=pageUrl%>?op=add&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name, "utf-8")%>&kind=<%=StrUtil.UrlEncode(kind)%>';
        }
        else if (com == '统计') {
            window.location.href = 'fileark_stat_year.jsp?dirCode=<%=StrUtil.UrlEncode(dir_code)%>';
        }
        else if (com == "目录") {
            // window.location.href='dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>';
            addTab("目录", "<%=request.getContextPath()%>/fileark/dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>");
        }
        else if (com == '编辑') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert("请选择一条记录！", "提示");
                return;
            }
            if (ids.split(",").length > 1) {
                jAlert("只能选择一条记录！", "提示");
                return;
            }

            //var id = $(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).val();
            var attachId = $("#attachId" + ids).val();
            var isFile = $("#isFile" + ids).val();
            var hiddenTitle = $("#hiddenTitle" + ids).val();

            if (isFile == "2") {
                if (hiddenTitle.indexOf(".xls") != -1 || hiddenTitle.indexOf(".xlsx") != -1 || hiddenTitle.indexOf(".doc") != -1 || hiddenTitle.indexOf(".docx") != -1 || hiddenTitle.indexOf(".ppt") != -1 || hiddenTitle.indexOf(".pptx") != -1 || hiddenTitle.indexOf(".wps") != -1) {
                    editdoc(ids, attachId);
                } else {
                    jAlert("该文件不能编辑!", "提示");
                }
            } else {
                window.location.href = "../<%=pageUrl%>?op=edit&id=" + ids + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>";
            }
        }
        else if (com == '删除') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert("请选择一条记录！", "提示");
                return;
            }

            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return
                }
                else {
                    window.location.href = "?op=delBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids + "&parentCode=<%=leaf!=null?leaf.getCode():""%>";
                }
            })
        }
        else if (com == '通过') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }
            //var ids = "";
            var userNames = "";
            var titles = "";

            var idss = ids.split(",");
            for (var i = 0; i < idss.length; i++) {
                var tempName = $("#myName" + idss[i]).val();
                var tempTitle = $("#myTitle" + idss[i]).val();
                if (userNames == "") {
                    userNames = tempName;
                    titles = tempTitle;
                } else {
                    userNames += "," + tempName;
                    titles += "," + tempTitle;
                }
            }
            jConfirm("您确定要通过么？", "提示", function (r) {
                if (r) {
                    window.location.href = "?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids + "&parentCode=<%=leaf!=null?leaf.getCode():""%>&userNames=" + userNames + "&titles=" + titles + "&sendMessage=1";
                }
            });
        }
        else if (com == '全部') {
            window.location.href = "document_list_m.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>&parentCode=<%=leaf!=null?leaf.getCode():""%>&<%=prjUrl %>";
        }
        else if (com == "迁移") {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }

            idsSelected = ids;

            //openWin("dir_sel.jsp", 640, 480);
            openWin("../fwebedit_left_choose.jsp?op=changeDir", 640, 480);
        } else if (com == "搜索") {
            showDiv();
        } else if (com == "全文检索") {
            window.location.href = "full_text_search_list.jsp";
        } else if (com == "上传") {
            showDivUpload();
            //document.getElementById("upload_sel").style.display="block";
        } else if (com == "打包") {
            addTabZip();
        }
    }
    //置顶
    function isTop(level, id) {
        jConfirm("您确实要置顶吗？", "提示", function (r) {
            if (!r) {
                return;
            }
            else {
                window.location.href = "document_list_m.jsp?op=setOnTop&level=" + level + "&id=" + id + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&<%=prjUrl %>";
            }
        })
    }
    //取消置顶
    function noTop(id) {
        jConfirm("您确实要取消置顶吗？", "提示", function (r) {
            if (!r) {
                return;
            }
            else {
                window.location.href = "document_list_m.jsp?op=setOnTop&level=0&id=" + id + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&<%=prjUrl %>";
            }
        })
    }
    //编辑文件
    function editdoc(doc_id, file_id) {
        <%if (cfg.get("isUseNTKO").equals("true")) {%>
        openWin("fileark_ntko_edit.jsp?docId=" + doc_id + "&attachId=" + file_id + "&isRevise=0", 1024, 768);
        <%}else{%>
        rmofficeTable.style.display = "";
        addform.redmoonoffice.AddField("doc_id", doc_id);
        addform.redmoonoffice.AddField("file_id", file_id);
        addform.redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/fileark/getfile.jsp?docId=" + doc_id + "&attachId=" + file_id);
        <%}%>
    }
    // 下载文件
    function downLoadDoc(doc_id, file_id) {
        $.ajax({
            type: "post",
            url: "downloadValidate.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                ids: doc_id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 2) {
                    window.open("doc_getfile.jsp?pageNum=1&id=" + doc_id + "&attachId=" + file_id + "&op=download");
                }
                else if (data.ret == 0) {
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                }
                else {
                    jConfirm(data.msg + '\n您确定要下载么？', '提示', function (r) {
                        if (r) {
                            window.open("doc_getfile.jsp?pageNum=1&id=" + doc_id + "&attachId=" + file_id + "&op=download");
                        }
                    });
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }
    function selDept() {
        openWin("../fwebedit_left_choose.jsp?op=search", 300, 400, "yes");
    }

    function shareToQQGroups(id, title) {
        jConfirm('您确定要分享至QQ群么？', '提示', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "../public/robot/shareToQQGroups.do",
                    data: {
                        id: id
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        jAlert(data.msg, "提示");
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }
</script>
</html>