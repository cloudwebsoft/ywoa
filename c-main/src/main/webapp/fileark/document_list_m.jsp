<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.WikiUnit" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.basic.SelectDb" %>
<%@ page import="com.redmoon.oa.basic.SelectMgr" %>
<%@ page import="com.redmoon.oa.basic.SelectOptionDb" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.PluginMgr" %>
<%@ page import="com.redmoon.oa.fileark.plugin.PluginUnit" %>
<%@ page import="com.redmoon.oa.fileark.plugin.base.IPluginUI" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="com.redmoon.oa.netdisk.UtilTools" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserSetupDb" %>
<%@ page import="com.redmoon.oa.pvg.PrivDb" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String userName1 = privilege.getUser(request);
    String dir_code = ParamUtil.get(request, "dir_code"); // 从目录树上点击的目录

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

    if ((dir_code == null || dir_code.equals("") || !Leaf.CODE_DRAFT.equals(dir_code)) && projectId != 0) {
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
    if (orderBy.equals("")) {
        orderBy = "id"; // "createDate";
    }
    String sort = ParamUtil.get(request, "sort");
    if (sort.equals("")) {
        sort = "desc";
    }

    UserSetupDb usd = new UserSetupDb();
    usd = usd.getUserSetupDb(privilege.getUser(request));
    //String pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";
    String pageUrl = "fwebedit_new.jsp";

    //swfUpload文件上传
    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
    HashMap<String, String> explorerFileType = new HashMap<String, String>();
    String file_size_limit = cfg.get("file_size_limit");
    int file_upload_limit = cfg.getInt("file_upload_limit");

    com.redmoon.oa.robot.Config robotCfg = com.redmoon.oa.robot.Config.getInstance();
    boolean isRobotOpen = robotCfg.getBooleanProperty("isRobotOpen");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>文档列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/document_search.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <style>
        .upfile-image-box {
            display: inline-block;
            width: 160px;
            height: 130px;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            border: 1px dashed #ff0005;
            background: #f8f8f8;
            position: relative;
            overflow: hidden;
            margin: 10px
        }

        .upload-image-box-db {
            border: 1px dashed #00CC00;
        }

        .upfile-cover {
            position: absolute;
            z-index: 1;
            top: 0px;
            width: 158px;
            height: 128px;
            background-color: rgba(0, 0, 0, .1);
            display: none;
            cursor: pointer;
        }

        .upfile-cover .btn-bar {
            width: 100%;
            height: 30px;
            background-color: #eee;
            opacity: 60%;
        }

        .upfile-cover .btn-bar .btn-del {
            color: red;
            padding: 1px 10px;
            /*background-color: #faffbd;
            border: 1px solid #faffbd;
            border-radius: 3px;*/
            font-size: 18px;
            float: right;
        }

        .upfile-image-box:hover .upfile-cover {
            display: block;
        }

        .upfile-image-box img {
            width: 158px;
            height: 128px;
        }

        .upfile-image-desc {
            width: 158px;
            height: 128px;
            text-align: center;
        }

        .upfile-image-desc img {
            margin-top: 40px;
            width: 32px;
            height: 32px;
        }

        .upfile-box {
            display: inline-block;
            width: 158px;
            height: 130px;
            -webkit-box-sizing: border-box;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            border: 1px dashed darkgray;
            background: #f8f8f8;
            position: relative;
            overflow: hidden;
            margin: 10px;
        }

        .upfile-box-tip {
            margin-top: 30px;
            text-align: center;
        }

        .upfile-box-tip span {
            font-size: 40px;
        }

        .upfile-ctrl {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            opacity: 0;
        }

        .sortable-ghost {
            opacity: 0.4;
            background-color: #F4E2C9;
        }

        .file-icon {
            width: 20px;
            height: 20px;
        }

        .ui-dialog-content input[type=file] {
            height: 100% !important;
        }

        .ui-dialog .ui-dialog-content {
            padding-left: 20px !important;
        }

        /*使按钮居中*/
        .ui-dialog .ui-dialog-buttonpane button {
            margin-right: 25% !important;
        }

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
    <script type="text/javascript" src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
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

    <script language=JavaScript src='showDialog/showDialog.js'></script>
    <link type="text/css" rel="stylesheet" href="showDialog/showDialog.css"/>
    <link type="text/css" rel="stylesheet" href="showDialog/document.css"/>

    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script language=JavaScript src='formpost.js'></script>
    <script src="../js/Sortable.js"></script>
    <script>
        var isLeftMenuShow = true;

        function closeLeftMenu() {
            if (isLeftMenuShow) {
                window.parent.setCols("0,*");
                isLeftMenuShow = false;
                btnName.innerHTML = "打开菜单";
            } else {
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

    // 如果dir_code为空，则需检查权限
    LeafPriv lp = new LeafPriv();
    if ("".equals(dir_code) && examine1 != Document.EXAMINE_DRAFT) {
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
    if (!dir_code.equals("") && !Leaf.CODE_DRAFT.equals(dir_code)) {
        leaf = dir.getLeaf(dir_code);
    }
    String viewPage = "";
    if (!dir_code.equals("") && !Leaf.CODE_DRAFT.equals(dir_code)) {
        if (leaf == null) {
            out.print(SkinUtil.makeErrMsg(request, "目录" + dir_code + "不存在！"));
            return;
        }

        lp.setDirCode(dir_code);
        if (!lp.canUserSee(privilege.getUser(request))) {
            out.print(SkinUtil.makeInfo(request, "权限不足"));
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
    if (viewPage.equals("")) {
        viewPage = request.getContextPath() + "/doc_show.jsp";
    }

    String dir_name = "";
    if (leaf != null) {
        dir_name = leaf.getName();
    }

    String parentCode = ParamUtil.get(request, "parentCode");
    String uName = privilege.getUser(request);
    LeafPriv plp = null;
    if (!parentCode.equals("")) {
        plp = new LeafPriv(parentCode);
    }

    Document document = new Document();
    String sql = document.getListSql(request, privilege, lp, plp, uName, examine1, checkbox_png, checkbox_ppt, checkbox_gif,
            checkbox_zip, checkbox_pdf, checkbox_doc, checkbox_xlsx, checkbox_txt,
            ext, docSize, parentCode, dir_code, kind, op, searchKind, what, keywords1,
            fromDate, toDate, examine, title, content, author, kind1, modifyType, orderBy, sort);

    // out.print(sql);

    String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
    if (strcurpage.equals("")) {
        strcurpage = "1";
    }
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
            <li id="menu">
                <a href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&<%=prjUrl %>"><span>全部</span></a>
            </li>
            <%
                Iterator irkind = vkind.iterator();
                while (irkind.hasNext()) {
                    dkd = (DirKindDb) irkind.next();
            %>
            <li id="menu<%=dkd.getKind()%>">
                <a href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&kind=<%=StrUtil.UrlEncode(dkd.getKind())%>&<%=prjUrl %>"><span><%=sod.getOptionName("fileark_kind", dkd.getKind())%></span></a>
            </li>
            <%}%>
        </ul>
    </div>
</div>
<script>
    o("menu<%=kind%>").className = "current";
</script>
<%}%>
<form name="form1" id="form1" action="document_list_m.jsp?<%=prjUrl %>" method="post">
    <div id="search_div" style="display:none;overflow:auto">
        <input type="hidden" name="clickTimes" id="clickTimes" value="0"/>
        <div class="document_cont" id="document_cont">
            <h1 id="doc_h1"><img src="<%=SkinMgr.getSkinPath(request)%>/images/document_icon1.png" width="37" height="25" id="searchImg"/>搜索条件</h1>
            <div class="document_sch" id="document_sch">
                <div class="document_sch_title" id="document_sch_title">请输入搜索条件：</div>
                <ul id="doc_search">
                    <li id="doc_name">文档名称：<input name="title" id="title" type="text" value="<%=title %>"/></li>
                    <li id="doc_author">
                        作&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;者：<input name="author" id="author" type="text" value="<%=author %>"/>
                    </li>
                    <li id="doc_content">文档内容：<input name="content" id="content" type="text" value="<%=content %>"/>
                    </li>
                    <li id="doc_keywords">关&nbsp;键&nbsp;词：&nbsp;<input name="keywords1" id="keywords1" type="text" value="<%=keywords1 %>"/></li>
                    <%
                        boolean isDraftBox = examine1 == Document.EXAMINE_DRAFT;
                        if (!isDraftBox) {
                            if (privilege.isUserPrivValid(request, "admin") || lp.canUserModify(privilege.getUser(request))) {
                    %>
                    <li id="doc_examine">
                        状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：
                        <select id="examine1" name="examine1" style="width:170px;">
                            <option value="-1" selected>不限</option>
                            <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
                            <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
                            <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
                        </select>
                    </li>
                    <%
                    } else {
                    %>
                    <li id="doc_examine">
                        状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：
                        <select id="examine1" name="examine1" style="width:170px;">
                            <option value="-1" selected>不限</option>
                            <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
                        </select>
                    </li>
                    <%
                        }
                    } else {
                    %>
                    <li id="doc_examine">
                        状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：草稿<input name="examine1" value="<%=Document.EXAMINE_DRAFT%>" type="hidden"/>
                    </li>
                    <%
                        }
                    %>
                    <script>
                        o("examine1").value = "<%=examine1%>";
                    </script>
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
                        <%
                            String directory = "";
                            if (isDraftBox) {
                                out.print("草稿箱");
                            } else {
                                if (!dir_code.equals("")) {
                                    Leaf lf = dir.getLeaf(dir_code);
                                    directory = lf.getName();
                                }
                        %>
                        <input id="directory" value="<%=directory %>"/>
                        &nbsp;<a href="javascript:;" onclick="selDept()">选择</a>
                        <%
                            }
                        %>
                    </li>
                </ul>
            </div>
            <div id="displayId">
                <%
                    if (!isDraftBox) {
                %>
                <a id="afBtn" href="javascript:;" style="text-decoration: none"><span style="margin-left:8px;">高级选项</span>
                    &nbsp;<img id="afBtnImg" src="<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png"/></a>
                <%
                    }
                %>
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
                    <div style="display:inline-block"><p id="doc_text">指定后缀：
                        <input name="ext" id="ext" type="text" onclick="clearAlter()" onblur="onBlurEvent()" value="<%=ext %>" style="width:150px;"/></p></div>
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
                    <input type="submit" id="search" class="document_sch_btn" value="搜索" style="margin-right:10px;"/>
                    <input type="button" id="reset" class="document_sch_btn" onclick="reSet()" value="重置"/>
                    <input type="hidden" name="op" value="search"/>
                    <input type="hidden" id="dirCodeSearch" name="dir_code" value="<%=dir_code%>"/>
                </div>
            </div>
        </div>
    </div>
    <div class="upload_sel" id="upload_sel" style="display:none">
        <input type="hidden" name="clickTimes1" id="clickTimes1" value="0"/>
        <ul>
            <li id="liUpload" style="margin-top: 10px">
                <a href="javascript:void(0)">普通</a>
            </li>
            <li class="speed-upload" style="text-align:center;" id="liWebedit">
                <a href="javascript:void(0)">极速</a>
            </li>
        </ul>
    </div>

    <table id="grid" cellSpacing="0" cellPadding="0" width="1028">
        <thead>
        <tr>
            <th width="20"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')"/></th>
            <%if (!parentCode.equals("")) {%>
            <th width="86" noWrap>文件夹</th>
            <%}%>
            <th width="35" align="center">类型</th>
            <th width="397" noWrap>标题</th>
            <th width="110" abbr="author">发布者</th>
            <th width="112" abbr="createDate">创建日期</th>
            <th width="112" abbr="modifiedDate">修改日期</th>
            <th width="60" abbr="examine" noWrap>状态</th>
            <th width="180" noWrap>操作</th>
        </tr>
        </thead>
        <tbody>
        <%
            String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&searchKind=" + searchKind + "&what=" + StrUtil.UrlEncode(what) + "&examine=" + examine;
            querystr += "&examine1=" + examine1 + "&kind=" + StrUtil.UrlEncode(kind) + "&kind1=" + StrUtil.UrlEncode(kind1);
            querystr += "&title=" + StrUtil.UrlEncode(title) + "&content=" + StrUtil.UrlEncode(content) + "&modifyType=" + StrUtil.UrlEncode(modifyType);
            querystr += "&docSize=" + StrUtil.UrlEncode(docSize) + "&fromDate=" + StrUtil.UrlEncode(fromDate) + "&toDate=" + StrUtil.UrlEncode(toDate);
            querystr += "&keywords1=" + StrUtil.UrlEncode(keywords1) + "&author=" + StrUtil.UrlEncode(author);
            querystr += "&ext=" + StrUtil.UrlEncode(ext) + "&checkbox_png=" + StrUtil.UrlEncode(checkbox_png) + "&checkbox_ppt=" + StrUtil.UrlEncode(checkbox_ppt);
            querystr += "&checkbox_gif=" + StrUtil.UrlEncode(checkbox_gif) + "&checkbox_zip=" + StrUtil.UrlEncode(checkbox_zip) + "&checkbox_pdf=" + StrUtil.UrlEncode(checkbox_pdf);
            querystr += "&checkbox_doc=" + StrUtil.UrlEncode(checkbox_doc) + "&checkbox_xlsx=" + StrUtil.UrlEncode(checkbox_xlsx) + "&checkbox_txt=" + StrUtil.UrlEncode(checkbox_txt);

            boolean canExamine = false;
            if (examine1!=Document.EXAMINE_DRAFT) {
                canExamine = lp.canUserExamine(privilege.getUser(request));
            }

            Document doc = new Document();
            DocPriv dp = new DocPriv();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                // boolean isHome = rr.getInt("isHome") == 1 ? true : false;
                String color = StrUtil.getNullStr(rr.getString("color"));
                boolean isBold = rr.getInt("isBold") == 1;
                java.util.Date expireDate = rr.getDate("expire_date");
                int docId = rr.getInt("id");
                doc = doc.getDocument(docId);
                String docTitle = doc.getTitle();

                // 判断是否有浏览文件的权限
                // 如果不是草稿箱
                if (examine1 != Document.EXAMINE_DRAFT) {
                    // 如果不是作者
                    if (!uName.equals(doc.getAuthor())) {
                        if (!canExamine) {
                            if (!dp.canUserSee(request, rr.getInt("id"))) {
                                continue;
                            }
                        }
                    }
                }

                boolean canDownload = isDraftBox || (lp.canUserDownLoad(privilege.getUser(request)) && dp.canUserDownload(privilege.getUser(request), docId));

                int attId = -1;
        %>
        <tr id=<%=rr.getInt("id")%> onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
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
                <img src="images/<%=icon%>" class="file-icon"/>
            </td>
            <td height="24">
                <%if (rr.getInt("type") == Document.TYPE_VOTE) {%>
                <IMG height=15 alt="" src="../forum/skin/bluedream/images/f_poll.gif" width=17 border=0>&nbsp;
                <%}%>
                <%
                    if (!doc.getKind().equals("")) {
                        if (sod == null) {
                            sod = new SelectOptionDb();
                        }
                %>
                <a href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&kind=<%=StrUtil.UrlEncode(doc.getKind())%>&<%=prjUrl %>">[&nbsp;<%=sod.getOptionName("fileark_kind", doc.getKind())%>
                    &nbsp;]</a>
                <%
                    }
                    if (DateUtil.compare(new java.util.Date(), expireDate) == 2) {%>
                        <a href="javascript:" linkType="view" title="ID：<%=rr.getInt("id")%>" data-id="<%=rr.getInt("id")%>" viewPage="<%=viewPage%>?id=<%=rr.getInt("id")%>" doc-title="<%=docTitle%>">
                    <%
                        if (isBold) {
                            out.print("<B>");
                        }
                        if (!color.equals("")) {
                    %>
                    <span style="color:<%=color%>">
                        <%}%>
                        <%=docTitle%>
                        <%if (!color.equals("")) {%>
                    </span>
                    <%}%>
                    <%
                        if (isBold) {
                            out.print("</B>");
                        }
                    %>
                </a>
                <%
                } else {
                    if (rr.getInt("type") == Document.TYPE_FILE) {
                        Attachment am = null;
                        Vector attachments = doc.getAttachments(1);
                        Iterator ir = attachments.iterator();
                        if (ir.hasNext()) {
                            am = (Attachment) ir.next();
                            attId = am.getId();
                        }
                %>
                <input type="hidden" id="isFile<%=docId%>" value="<%=rr.getInt("type")%>"/>
                <input type="hidden" id="hiddenTitle<%=docId%>" value="<%=docTitle%>"/>
                <input type="hidden" id="attachId<%=docId%>" value="<%=attId%>"/>
                <%
                    boolean isImage = false;
                    if (attId != -1) {
                        if (StrUtil.isImage(StrUtil.getFileExt(am.getDiskName()))) {
                            isImage = true;
                        }
                        if (isImage) {
                            if (canDownload) {
                %>
                <a target="_blank" title="ID：<%=docId%>" href="download.do?pageNum=1&id=<%=docId%>&attachId=<%=am.getId()%>"><%=docTitle%></a>
                <%} else {%>
                <%=docTitle%>
                <%
                    }
                } else {
                    String fileExt = StrUtil.getFileExt(am.getDiskName());
                    boolean isOffice = am.getExt().equals("doc")
                            || am.getExt().equals("docx")
                            || am.getExt().equals("xls")
                            || am.getExt().equals("xlsx");
                    if (isOffice) {
                        String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
                        String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
                        File fileExist = new File(htmlfile);
                        if (fileExist.exists()) {

                %>
                <a target="_blank" title="ID：<%=rr.getInt("id")%>" href="../doc_show_preview.jsp?pageNum=<%=1%>&id=<%=docId%>&attachId=<%=am.getId()%>">
                    <%=rr.get("title")%>
                </a>
                <%
                } else {
                    if (canDownload) {
                %>
                <a target="_blank" title="ID：<%=docId%>" href="download.do?pageNum=1&id=<%=docId%>&attachId=<%=am.getId()%>"><%=docTitle%></a>
                <%
                }
                else {
                %>
                <a target="_blank" title="ID：<%=docId%>" href="doc_show.jsp?pageNum=1&docId=<%=rr.getInt("id")%>&attachId=<%=am.getId()%>"><%=docTitle%></a>
                <%
                        }
                    }
                } else if (fileExt.equals("pdf")) {
                %>
                <a target="_blank" title="ID：<%=docId%>" href="pdf_js/viewer.html?file=<%=request.getContextPath()+"/"+am.getVisualPath()+"/"+am.getDiskName()%>"><%=rr.get("title")%>
                </a>
                <%
                } else {
                    if (canDownload) {
                %>
                <a title="ID：<%=docId%>" href="javascript:;" onclick="downLoadDoc(<%=rr.getInt("id")%>, <%=am.getId()%>)"><%=docTitle%>
                </a>
                <%
                    } else {
                        out.print(docTitle);
                    }
                            }
                        }
                    }
                } else {
                %>
                <a href="javascript:" linkType="doc" title="ID：<%=docId%>" data-id="<%=docId%>" doc-title="<%=docTitle%>">
                    <span style="color:<%=color%>">
                        <%if (isBold) { %>
                        <b><%=docTitle%></b>
                        <% } else {%>
                        <%=docTitle%>
                        <%}%>
                    </span>
                </a>
                <%}
                    DocLogDb dldb = new DocLogDb();
                    if (!dldb.isUserReaded(userName1, doc.getId())) {
                %>
                &nbsp;
                <img src="../images/icon_new.gif"/>
                <%
                        }
                    }
                %>
            </td>
            <%
                String userName = "";
                UserDb ud = new UserDb();
                userName = (doc != null) ? doc.getNick() : privilege.getUser(request);
                ud = ud.getUserDb(userName);
                String realName = "";
                if (ud != null && ud.isLoaded()) {
                    realName = StrUtil.getNullStr(ud.getRealName());
                }
            %>
            <td><%=realName.equals("") ? userName : realName %>
                <input type="hidden" id="myName<%=docId%>" value="<%=userName%>"/>
                <input type="hidden" id="myTitle<%=docTitle%>" value="<%=docTitle%>"/>
            </td>
            <td>
                <%
                    java.util.Date d = rr.getDate("createDate");
                    if (d != null) {
                        out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
                    }
                %>
            </td>
            <td>
                <%
                    d = rr.getDate("modifiedDate");
                    if (d != null) {
                        out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
                    }

                    int ex = rr.getInt("examined");
                %>
            </td>
            <td id="tdExamine<%=rr.getInt("id")%>" examine="<%=ex%>" align="center">
                <%
                    if (ex == Document.EXAMINE_NOT) {
                        out.print("<font color='blue'>未审核</font>");
                    } else if (ex == Document.EXAMINE_NOTPASS) {
                        out.print("<font color='red'>未通过</font>");
                    } else if (ex == Document.EXAMINE_PASS) {
                        out.print("已通过");
                    } else if (ex == Document.EXAMINE_DRAFT) {
                        out.print("草稿");
                    }
                %>
            </td>
            <td align="center">
                <%
                    Leaf lf6 = dir.getLeaf(rr.getString("class1"));
                    if (lf6 == null) {
                        out.print("目录不存在</td></tr>");
                        continue;
                    }

                    lp = new LeafPriv(lf6.getCode());
                    if (examine1!=Document.EXAMINE_DRAFT && lp.canUserExamine(privilege.getUser(request))) {
                        if (rr.getInt("doc_level") != Document.LEVEL_TOP) {
                %>
                &nbsp;<a href="javascript:" onclick="isTop(<%=Document.LEVEL_TOP%>,<%=rr.getInt("id")%>)">置顶</a>
                <%} else {%>
                &nbsp;<a href="javascript:" onclick="noTop(<%=rr.getInt("id")%>)">取消置顶</a>
                <%
                        }
                    }
                %>
                <%if (examine1==Document.EXAMINE_DRAFT || lp.canUserExamine(privilege.getUser(request)) || dp.canUserManage(request, rr.getInt("id"))) { %>
                &nbsp;<a href="javascript:" linkType="priv" data-id="<%=rr.getInt("id")%>" title="<%=rr.getString("title")%>" dirCode="<%=dir_code%>" dirName="<%=dir_name%>">权限</a>
                <%if (lf6.isLog()) {%>
                &nbsp;<a href="javascript:" linkType="log" data-id="<%=rr.getInt("id")%>" title="<%=rr.getString("title")%>">日志</a>
                <%
                    }
                    if (canDownload) {
                        if (rr.getInt("type") == Document.TYPE_FILE) {
                %>
                &nbsp;<a href="javascript:" onclick="downLoadDoc(<%=docId%>, <%=attId%>)" title="ID：<%=docTitle%>">下载</a>
                <%
                        }
                    }
                    if (isRobotOpen) {
                %>
                &nbsp;<a href="javascript:" onclick="shareToQQGroups(<%=rr.getInt("id")%>, '<%=rr.getString("title")%>')">群分享</a>
                <%
                    }
                }
            %>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <input id="uploadUrl" type="hidden" value="uploadBatch.do?jsessionid=<%=session.getId()%>?userName=<%=StrUtil.UrlEncode(userName1) %>&dirCode=<%=StrUtil.UrlEncode(dir_code) %>"/>
    <input type="hidden" value="<%=file_size_limit %>" id="fileSizeLimit"/>
    <input type="hidden" value="<%=file_upload_limit %>" id="fileUploadLimit"/>
    <input type="hidden" value="<%=dir_code %>" id="dirCode"/>
    <input type="hidden" id="cooperateId" value=""/>
</form>
<form name="addForm" enctype="multipart/form-data">
    <table width="33%" border="0" cellspacing="0" cellpadding="0" name="ctlTable" id="ctlTable" style="height: 150px; font-size: 15px; position: fixed; top: 200px; left: 200px; display: none; z-index: -100">
        <tbody>
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
                <div id='upload_body' style='height: 150px; width: 600px; overflow: auto;'>
                    <div id='upload_content'>
                        <table border="0" align="center" cellpadding="0" cellspacing="1">
                            <tr>
                                <td>
                                    <div style="width: 100%; height: 95px; margin-top: 10px; border: 0px solid #cccccc">
                                        <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C"
                                                codebase="../activex/cloudym.CAB#version=1,2,0,1" width="410px" height="85px" align="middle" id="webedit">
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
                                            <param name="PostScript" value="<%=Global.virtualPath%>/fileark/uploadBatch.do?uploadType=1&dirCode=<%=dir_code %>"/>
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
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                <div id='SD_button'>
                    <div class='SD_button'>
                        <a onclick="window_remove()">关闭</a>
                    </div>
                </div>
                <a href='javascript:;' id='upload_close' title='关闭'></a>
            </td>
        </tr>
        </tbody>
    </table>
</form>
<div id="uploadDialog" style="display: none">
    <form id="formUpload" type="post">
        <div class="img-preview-box">
            <div class="upfile-box">
                <input type="file" class="upfile-ctrl" id="titleImage" name="titleImage" multiple/>
                <div class="upfile-box-tip">
                    <span>+</span>
                    <p>点击或拖拽到“+”<br/>上传文件</p>
                </div>
            </div>
        </div>
    </form>
</div>
</body>
<script>
    function addEventListeners(ele, type, callback) {
        try {  // Chrome、FireFox、Opera、Safari、IE9.0及其以上版本
            ele.addEventListener(type, callback, false);
        } catch (e) {
            try {  // IE8.0及其以下版本
                ele.attachEvent('on' + type, callback);
            } catch (e) {  // 早期浏览器
                ele['on' + type] = callback;
            }
        }
    }

    $(function () {
        addEventListeners(document, 'click', function(e) {
            e = e || window.event;
            var dom = e.srcElement || e.target;
            try {
                if (dom.id == "" && dom.className != "search" && dom.parentNode.id == "" && dom.parentNode.className.indexOf("daysrow") == -1 && dom.parentNode.className.indexOf("hilite") == -1 && dom.parentNode.className != "button" && dom.parentNode.className != "daynames" && dom.parentNode.className != "footrow" && dom.className != "title" && (dom.parentNode.className != "" && dom.parentNode.className != "xdsoft_mounthpicker" && dom.parentNode.className != "xdsoft_label xdsoft_year" && dom.parentNode.className != "xdsoft_label xdsoft_month" && dom.parentNode.className != "xdsoft_datetimepicker xdsoft_noselect ")) {
                    document.getElementById("search_div").style.display = "none";
                    document.getElementById("clickTimes").value = "0";
                }
            } catch (e) {
            }

            try {
                if (dom.id == "" && dom.parentNode.id == "") {
                    document.getElementById("upload_sel").style.display = "none";
                    document.getElementById("clickTimes1").value = "0";
                }
            } catch (e) {
            }
        })

        /*$(document).bind("click", function (e) {
            e = e || window.event;
            var dom = e.srcElement || e.target;
            try {
                if (dom.id == "" && dom.className != "search" && dom.parentNode.id == "" && dom.parentNode.className.indexOf("daysrow") == -1 && dom.parentNode.className.indexOf("hilite") == -1 && dom.parentNode.className != "button" && dom.parentNode.className != "daynames" && dom.parentNode.className != "footrow" && dom.className != "title" && (dom.parentNode.className != "" && dom.parentNode.className != "xdsoft_mounthpicker" && dom.parentNode.className != "xdsoft_label xdsoft_year" && dom.parentNode.className != "xdsoft_label xdsoft_month" && dom.parentNode.className != "xdsoft_datetimepicker xdsoft_noselect ")) {
                    document.getElementById("search_div").style.display = "none";
                    document.getElementById("clickTimes").value = "0";
                }
            } catch (e) {
            }

            try {
                if (dom.id == "" && dom.parentNode.id == "") {
                    document.getElementById("upload_sel").style.display = "none";
                    document.getElementById("clickTimes1").value = "0";
                }
            } catch (e) {
            }
        });*/
    });

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

        o("radio_1").checked = "checked";
        o("doc_size_1").checked = "checked";
    }

    // 极速上传未开始， 直接点击关闭
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
        $("#webedit").css({"height": "85px"});
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
        if (hasFile) {
            SubmitWithFileThread();
        }
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
        } else {
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
                    window.open("zipFile.do?ids=" + ids);
                } else if (data.ret == 0) {
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                } else {
                    jConfirm(data.msg + '\n您确定要打包下载么？', '提示', function (r) {
                        if (r) {
                            window.open("zipFile.do?ids=" + ids);
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
        if (!isIE()) {
            showUploadDialog();
            return;
        }
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
        <%if (!dir_code.equals("") &&  !Leaf.CODE_DRAFT.equals(dir_code) && leaf.getType()==2) {
            if (lp.canUserAppend(privilege.getUser(request))) {%>
        {name: '添加', bclass: 'add', onpress: action},
        <%	}
        }%>
        <%
        int filearkUserEditDelInterval = cfg.getInt("filearkUserEditDelInterval");
        if (isDraftBox || lp.canUserModify(privilege.getUser(request)) || (lp.canUserAppend(privilege.getUser(request)) && filearkUserEditDelInterval>0)) {
        %>
        {name: '编辑', bclass: 'edit', onpress: action},
        <%}%>
        <%
        if (examine1==Document.EXAMINE_DRAFT || lp.canUserDel(privilege.getUser(request)) || (lp.canUserAppend(privilege.getUser(request)) && filearkUserEditDelInterval>0)) {
        %>
        {name: '删除', bclass: 'delete', onpress: action},
        <%}%>
        <%
        if (examine1!=Document.EXAMINE_DRAFT && lp.canUserExamine(privilege.getUser(request))) {
        %>
        {name: '通过', bclass: 'pass', onpress: action},
        {name: '未过', bclass: 'not-pass', onpress: action},
        {name: '统计', bclass: 'stata', onpress: action},
        <%}%>
        <%if (isDraftBox || lp.canUserDownLoad(privilege.getUser(request))) {%>
        {name: '打包', bclass: 'zip', onpress: action},
        <%}%>
        <%if (isDraftBox) {%>
        {name: '发布', bclass: 'check', onpress: action},
        <%}%>
        {name: '搜索', bclass: 'search', onpress: action, id: 'searchFile'},
        <%if (!"".equals(dir_code) && !Leaf.CODE_DRAFT.equals(dir_code) && (privilege.isUserPrivValid(request, "admin") || lp.canUserExamine(privilege.getUser(request)))) {%>
        {name: '迁移', bclass: 'changeDir', onpress: action},
        <%}%>
        <%if (!dir_code.equals("") && !Leaf.CODE_DRAFT.equals(dir_code) && leaf.getType()==Leaf.TYPE_LIST) {
            if (lp.canUserAppend(privilege.getUser(request))) {%>
        {name: '上传', bclass: 'fileUpload', onpress: action, id: 'dd'},
        <%	}
        }%>
        <%if (cfg.getBooleanProperty("fullTextSearchSupported")) {%>
        {name: '全文检索', bclass: 'search', onpress: action},
        <%}%>
        <%if (leaf!=null && leaf.getChildCount()>0) {%>
        {name: '全部', bclass: 'listIncChild', onpress: action},
        <%}%>
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

            } else {
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
            } else {
                $('#afBtnImg_1')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                $('#doc_time').hide();
            }
        });
        $("#afBtn_2").click(function () {
            if ($(this).html().indexOf("down") != -1) {
                $('#afBtnImg_2')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_up.png";
                $('#doc_size').show();
            } else {
                $('#afBtnImg_2')[0].src = "<%=SkinMgr.getSkinPath(request)%>/images/af_arrow_down.png";
                $('#doc_size').hide();
            }
        });

        $('#liUpload').click(function () {
            showUploadDialog();
        });

        //极速上传
        $('.speed-upload').live("click", function () {
            $("#ctlTable").css({"display": "block", "top": "150px", "z-index": "1800"});
            $("#webedit").css({"height": "85px"});
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

        $.ajax({
            type: "post",
            url: "changeDir.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                ids: idsSelected,
                dirCode: "<%=dir_code%>",
                newDirCode: code
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert(data.msg, '提示', function () {
                        window.location.href = "document_list_m.jsp?dir_code=" + code + "&<%=prjUrl %>";
                    });
                } else if (data.ret == 0) {
                    jAlert(data.msg, '提示');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                $('body').hideLoading();
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    var idsSelected = "";
    function action(com, grid) {
        if (com == '添加') {
            window.location.href = '../<%=pageUrl%>?op=add&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name, "utf-8")%>&kind=<%=StrUtil.UrlEncode(kind)%>';
        } else if (com == '统计') {
            window.location.href = 'fileark_stat_year.jsp?dirCode=<%=StrUtil.UrlEncode(dir_code)%>';
        } else if (com == "目录") {
            // window.location.href='dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>';
            addTab("目录", "<%=request.getContextPath()%>/fileark/dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>");
        } else if (com == '编辑') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert("请选择一条记录！", "提示");
                return;
            }
            if (ids.split(",").length > 1) {
                jAlert("只能选择一条记录！", "提示");
                return;
            }

            <%
            if (!isDraftBox && !lp.canUserModify(privilege.getUser(request))) {
            %>
            if ($('#tdExamine' + ids).attr("examine") == <%=Document.EXAMINE_NOT%>) {
                jAlert("文章正在审核中，不能编辑！", "提示");
                return;
            }
            <%
            }
            %>

            //var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
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
                addTab("编辑文档", "<%=request.getContextPath()%>/<%=pageUrl%>?op=edit&id=" + ids + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>");
            }
        } else if (com == '删除') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert("请选择一条记录！", "提示");
                return;
            }

            jConfirm("您确定要删除么？", "提示", function (r) {
                if (!r) {
                    return
                } else {
                    // window.location.href = "?op=delBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids + "&parentCode=<%=leaf!=null?leaf.getCode():""%>";

                    if (r) {
                        $.ajax({
                            type: "post",
                            url: "delBatch.do",
                            data: {
                                ids: ids
                            },
                            dataType: "html",
                            beforeSend: function (XMLHttpRequest) {
                                $('body').showLoading();
                            },
                            success: function (data, status) {
                                data = $.parseJSON(data);
                                $.toaster({
                                    "priority": "info",
                                    "message": data.msg
                                });
                                if (data.ret == 1) {
                                    var ary = ids.split(",");
                                    for (var i = 0; i < ary.length; i++) {
                                        $('tr[id=' + ary[i] + ']').remove();
                                    }
                                }
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
                }
            })
        } else if (com == '发布') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert("请选择一条记录！", "提示");
                return;
            }

            jConfirm("您确定要发布么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    $.ajax({
                        type: "post",
                        url: "publish.do",
                        data: {
                            ids: ids
                        },
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data, status) {
                            data = $.parseJSON(data);
                            $.toaster({
                                "priority": "info",
                                "message": data.msg
                            });
                            if (data.ret == 1) {
                                var ary = ids.split(",");
                                for (var i = 0; i < ary.length; i++) {
                                    $('tr[id=' + ary[i] + ']').remove();
                                }
                            }
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
            })
        } else if (com == '通过') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }

            jConfirm("您确定要通过么？", "提示", function (r) {
                $.ajax({
                    type: "post",
                    url: "passExamine.do",
                    data: {
                        ids: ids,
                        sendMessage: 1
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        $.toaster({
                            "priority": "info",
                            "message": data.msg
                        });
                        if (data.ret == 1) {
                            var ary = ids.split(",");
                            for (var i = 0; i < ary.length; i++) {
                                $('td[id=tdExamine' + ary[i] + ']').html('<div>已通过</div>');
                            }
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        } else if (com == '未过') {
            var ids = getCheckboxValue("ids");
            if (ids == "") {
                jAlert('请选择记录!', '提示');
                return;
            }

            jConfirm("您确定要不通过么？", "提示", function (r) {
                $.ajax({
                    type: "post",
                    url: "unpassExamine.do",
                    data: {
                        ids: ids,
                        sendMessage: 1
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        $.toaster({
                            "priority": "info",
                            "message": data.msg
                        });
                        if (data.ret == 1) {
                            var ary = ids.split(",");
                            for (var i = 0; i < ary.length; i++) {
                                $('td[id=tdExamine' + ary[i] + ']').html('<div style="color:red">未通过</div>');
                            }
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            });
        } else if (com == '全部') {
            window.location.href = "document_list_m.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>&parentCode=<%=leaf!=null?leaf.getCode():""%>&<%=prjUrl %>";
        } else if (com == "迁移") {
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
        } else if (com == "打包") {
            addTabZip();
        }
    }

    //置顶
    function isTop(level, id) {
        jConfirm("您确实要置顶吗？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "setOnTop.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        id: id,
                        level: level
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert(data.msg, '提示', function () {
                                window.location.reload();
                            });
                        } else if (data.ret == 0) {
                            jAlert(data.msg, '提示');
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        $('body').hideLoading();
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        })
    }

    //取消置顶
    function noTop(id) {
        jConfirm("您确实要取消置顶吗？", "提示", function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "setOnTop.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        id: id,
                        level: 0
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert(data.msg, '提示', function () {
                                window.location.reload();
                            });
                        } else if (data.ret == 0) {
                            jAlert(data.msg, '提示');
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        $('body').hideLoading();
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        })
    }

    //编辑文件
    function editdoc(doc_id, file_id) {
        openWin("fileark_ntko_edit.jsp?docId=" + doc_id + "&attachId=" + file_id + "&isRevise=0", 1024, 768);
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
                    window.open("download.do?pageNum=1&id=" + doc_id + "&attachId=" + file_id + "&op=download");
                } else if (data.ret == 0) {
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                } else {
                    jConfirm(data.msg + '\n您确定要下载么？', '提示', function (r) {
                        if (r) {
                            window.open("download.do?pageNum=1&id=" + doc_id + "&attachId=" + file_id + "&op=download");
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

    $(function () {
        if (!isIE()) {
            $('#liWebedit').hide();
        }
    });


    var dropFiles = [];

    // 实现多图上传预览功能
    $(function () {
        function showImgList(fileList) {
            // 添加时间戳
            var dateTime = new Date().getTime();
            for (var i = 0; i < fileList.length; i++) {
                var picHtml = "<div class='upfile-image-box' title='" + fileList[i].name + "'><img class='upfile-image' id='img_" + dateTime + "_" + fileList[i].name + "'/><div class='upfile-cover'><div class='btn-bar'><span class='btn-del'>×</span></div></div></div>";
                $(".img-preview-box").append(picHtml);

                var imgObjPreview = document.getElementById("img_" + dateTime + "_" + fileList[i].name);
                var $img = $(imgObjPreview);
                if (fileList[i]) {
                    var file = fileList[i];
                    var p = file.name.indexOf(".");
                    var ext = file.name.substr(p + 1).toLowerCase();
                    if (ext == "gif" || ext == "jpg" || ext == "bmp" || ext == "jpeg" || ext == "png") {
                        imgObjPreview.style.display = 'block';
                        imgObjPreview.src = window.URL.createObjectURL(fileList[i]);
                    } else {
                        $img.after("<div class='upfile-image-desc'><img src='../images/file.png'/><div>" + file.name + "</div></div>");
                        $img.hide();
                    }
                    $img.attr('img_type', file.type);
                    $img.attr('img_size', file.size);
                    $img.attr('img_name', file.name);
                    $img.attr('img_lastModified', file.lastModified);
                }
            }
        }

        $('.upfile-box').on('dragover', '.upfile-ctrl', function (e) {
            e.stopPropagation();
            // 阻止浏览器默认打开文件的操作
            e.preventDefault();

            window.event.dataTransfer.dropEffect = 'copy';
        });

        $('.upfile-box').on("drop", '.upfile-ctrl', function (e) {
            e.stopPropagation();
            // 阻止浏览器默认打开文件的操作
            e.preventDefault();

            var fileList = window.event.dataTransfer.files;

            for (var i = 0; i < fileList.length; i++) {
                var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size": fileList[i].size, "type": fileList[i].type};
                dropFiles.push(json);
            }

            showImgList(fileList);
        });

        $(".upfile-ctrl").change(function (e) {
            var fileList = $(this)[0].files;
            for (var i = 0; i < fileList.length; i++) {
                var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size": fileList[i].size, "type": fileList[i].type};
                dropFiles.push(json);
            }
            showImgList(fileList);
        });

        // 拖动排序
        Sortable.create($(".img-preview-box")[0], {
            animation: 150, // 动画参数
            onAdd: function (evt) {   //拖拽时候添加有新的节点的时候发生该事件
                // console.log('onAdd:', [evt.item, evt.from]);
            },
            onUpdate: function (evt) {  //拖拽更新节点位置发生该事件
                // console.log('onUpdate:', [evt.item, evt.from]);
            },
            onRemove: function (evt) {   //删除拖拽节点的时候触发该事件
                // console.log('onRemove:', [evt.item, evt.from]);
            },
            onStart: function (evt) {  //开始拖拽出发该函数
                // console.log('onStart:', [evt.item, evt.from]);
            },
            onSort: function (evt) {  //发生排序发生该事件
                // console.log('onSort:', [evt.item, evt.from]);
            },
            onEnd: function (evt) { //拖拽完毕之后发生该事件
                //  console.log('onEnd:', [evt.item, evt.from]);
                var ary = [];
                $(".upfile-image-box").each(function () {
                    var $img = $(this).find('.upfile-image');
                    for (i = 0; i < dropFiles.length; i++) {
                        var f = dropFiles[i];
                        if (f.name == $img.attr('img_name') && f.type == $img.attr('img_type') && ('' + f.size) == $img.attr('img_size') && ('' + f.lastModified) == $img.attr('img_lastmodified')) {
                            ary.push(f);
                        }
                    }
                });
                dropFiles = ary;
            }
        });

        // 删除
        $(".img-preview-box").on("click", ".btn-del", function () {
            // 删除文件
            var $img = $(this).parents(".upfile-image-box").find('.upfile-image');
            $(this).parents(".upfile-image-box").remove();
            var ary = [];
            for (i = 0; i < dropFiles.length; i++) {
                var f = dropFiles[i];
                if (!(f.name == $img.attr('img_name') && f.type == $img.attr('img_type') && ('' + f.size) == $img.attr('img_size') && ('' + f.lastModified) == $img.attr('img_lastmodified'))) {
                    ary.push(f);
                }
            }
            dropFiles = ary;
            // console.log(dropFiles);
        });
    });

    function showUploadDialog() {
        $("#uploadDialog").dialog({
            modal: true,
            bgiframe: true,
            width: 800,
            height: 600,
            title: "上传文件",
            closeText: "关闭",
            buttons: {
                "上传": function () {
                    $('.ui-button').attr("disabled", true);

                    var f = document.createElement("form");
                    var formData = new FormData(f);
                    for (var i = 0; i < dropFiles.length; i++) {
                        formData.append("file" + i, dropFiles[i].file);
                    }

                    $.ajax({
                        url: 'uploadBatch.do?dirCode=<%=dir_code %>',
                        type: 'post',
                        data: formData,
                        async: true,
                        // 下面三个参数要指定，如果不指定，会报一个JQuery的错误
                        cache: false,
                        contentType: false,
                        processData: false,
                        dataType: "html",
                        beforeSend: function (XMLHttpRequest) {
                            $('body').showLoading();
                        },
                        success: function (data) {
                            var data = $.parseJSON($.trim(data));
                            if (data.ret == "0") {
                                jAlert(data.msg, "提示");
                                $('.ui-button').attr("disabled", false);
                            } else {
                                jAlert(data.msg, "提示", function () {
                                    window.location.reload();
                                });
                            }
                        },
                        complete: function (XMLHttpRequest, status) {
                            $('body').hideLoading();
                        },
                        error: function (returndata) {
                            $('body').hideLoading();
                            $('.ui-button').attr("disabled", false);
                            alert(returndata);
                        }
                    });
                },
                "取消": function () {
                    $(this).dialog("close");
                }
            }
        });
    }

    $(function() {
        // 必须要通过jQuery绑定click，如果直接通过onclick事件addTab，则左侧树形菜单中的链接在addTab后将无法点击
        $("a[linkType='doc']").click(function(e) {
            e.preventDefault();
            addTab($(this).attr('doc-title'), '<%=request.getContextPath()%>/doc_show.jsp?id=' + $(this).data('id'));
        })

        $("a[linkType='priv']").click(function(e) {
            e.preventDefault();
            addTabPriv($(this).data('id'), $(this).attr('title'), $(this).attr('dirCode'), $(this).attr('dirName'));
        })

        $("a[linkType='log']").click(function(e) {
            e.preventDefault();
            addTabLog($(this).data('id'), $(this).attr('title'));
        })

        $("a[linkType='view']").click(function(e) {
            e.preventDefault();
            addTab($(this).attr('doc-title'), $(this).attr('viewPage'));
        })
    })
</script>
</html>