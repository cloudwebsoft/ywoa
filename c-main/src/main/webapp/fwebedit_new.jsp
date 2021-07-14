<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.security.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.clouddisk.Config" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.dept.DeptMgr" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
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
            width: 160px;
            height: 130px;
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

        .upfile-cover .btn-bar .btn-del,.btn-cancel {
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
            width: 160px;
            height: 130px;
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
            font-size: 35px;
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
    </style>
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src="js/Sortable.js"></script>
    <link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="inc/map.js"></script>
    <script src="inc/livevalidation_standalone.js"></script>
    <script src="js/jquery.form.js"></script>
    <link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
    <script src="js/datepicker/jquery.datetimepicker.js"></script>
    <script src="js/jquery.toaster.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <!--[if lt IE 9]>
    <script src="js/json2.js"></script>
    <![endif]-->
    <script id='uploadJs' src="inc/upload.js" local="zh"></script>
    <script>
        var isLeftMenuShow = true;
        function closeLeftMenu() {
            if (typeof(window.parent.setCols) != 'function') {
                return;
            }
            if (isLeftMenuShow) {
                window.parent.setCols("0,*");
                isLeftMenuShow = false;
                $('#btnMenu').attr('title', "打开菜单");
            }
            else {
                window.parent.setCols("200,*");
                isLeftMenuShow = true;
                $('#btnMenu').attr('title', "关闭菜单");
            }
        }

        function onAddFile() {
        }
    </script>
    <%
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    %>
    <jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
    <jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
    <jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
    <%
        String dir_code = ParamUtil.get(request, "dir_code");
        String dir_name = ParamUtil.get(request, "dir_name");
        int id = 0;

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        Privilege privilege = new Privilege();
        String op = ParamUtil.get(request, "op");
        String action = ParamUtil.get(request, "action");

        String correct_result = "操作成功！";
        Document doc = null;
        Document template = null;

        if (op.equals("edit")) {
            id = ParamUtil.getInt(request, "id");
            doc = docmanager.getDocument(id);
            if (!doc.isLoaded()) {
                out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
                return;
            }
            dir_code = doc.getDirCode();
        }

        Leaf leaf = dir.getLeaf(dir_code);
        if (leaf == null) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "目录:" + dir_code + "不存在"));
            return;
        }

        String strtemplateId = ParamUtil.get(request, "templateId");
        int templateId = Document.NOTEMPLATE;
        if (!strtemplateId.trim().equals("")) {
            if (StrUtil.isNumeric(strtemplateId)) {
                templateId = Integer.parseInt(strtemplateId);
            }
        }
        if (templateId == Document.NOTEMPLATE) {
            templateId = leaf.getTemplateId();
        }

        if (templateId != Document.NOTEMPLATE) {
            template = docmanager.getDocument(templateId);
        }

        if (op.equals("add")) {
            LeafPriv lp = new LeafPriv();
            lp.setDirCode(dir_code);
            if (!lp.canUserAppend(privilege.getUser(request))) {
                // out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
                out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
                return;
            }

            if (action.equals("selTemplate")) {
                int tid = ParamUtil.getInt(request, "templateId");
                template = docmanager.getDocument(tid);
            }
        } else if (op.equals("edit")) {
            try {
                LeafPriv lp = new LeafPriv(doc.getDirCode());
                if (!lp.canUserModify(privilege.getUser(request))) {
                    if (doc.getExamine() == Document.EXAMINE_NOT) {
                        if (!lp.canUserModify(privilege.getUser(request))) {
                            out.print(StrUtil.jAlert_Back("文章正在审核中，不能编辑", "提示"));
                            return;
                        }
                    }

                    // 判断是否本人编辑自己的文章
                    boolean canModify = false;
                    if (doc.getAuthor().equals(privilege.getUser(request))) {
                        double filearkUserDelIntervalH = StrUtil.toDouble(cfg.get("filearkUserEditDelInterval"), 0);
                        double intervalMinute = filearkUserDelIntervalH * 60;
                        if (DateUtil.datediffMinute(new Date(), doc.getCreateDate()) < intervalMinute) {
                            canModify = true;
                        }
                        else {
                            out.print(SkinUtil.makeInfo(request, "已超时，发布后" + filearkUserDelIntervalH + "小时内可修改"));
                            return;
                        }
                    }
                    if (!canModify) {
                        out.print(SkinUtil.makeErrMsg(request, Privilege.MSG_INVALID));
                        return;
                    }
                }

                if (action.equals("selTemplate")) {
                    int tid = ParamUtil.getInt(request, "templateId");
                    doc.setTemplateId(tid);
                    doc.updateTemplateId();
                }
                if (doc != null) {
                    template = doc.getTemplate();
                }
            } catch (ErrMsgException e) {
                out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
                return;
            }
        }
        if (op.equals("editarticle")) {
            op = "edit";
            try {
                doc = docmanager.getDocumentByCode(request, dir_code, privilege);
                dir_code = doc.getDirCode();

                LeafPriv lp = new LeafPriv();
                lp.setDirCode(doc.getDirCode());
                if (!lp.canUserModify(privilege.getUser(request))) {
                    out.print(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
                    return;
                }

            } catch (ErrMsgException e) {
                out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
                return;
            }
        }

        if (doc != null) {
            id = doc.getID();
            Leaf lfn = new Leaf();
            lfn = lfn.getLeaf(doc.getDirCode());
            dir_name = lfn.getName();
        }
    %>
    <title><%=doc != null ? doc.getTitle() : ""%></title>
    <style type="text/css">
        .head1 {
            background-color: #daeaf8;
            height: 25px;
            font-size: 14px;
            font-weight: bold;
            color: #666666;
            padding: 8px 0px 0px 5px;
            border-bottom: 1px solid #92b4d2;
        }

        input[type=text] {
            border: 1px solid #d6d6d6;
            height: 22px
        }

        .mybtn {
            background-color: #87c3f1 !important;
            font-weight: bold;
            text-align: center;
            line-height: 35px;
            height: 35px;
            width: 120px;
            padding-right: 8px;
            padding-left: 8px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            behavior: url(skin/common/ie-css3.htc);
            cursor: pointer;
            color: #fff;
            border-top-width: 0px;
            border-right-width: 0px;
            border-bottom-width: 0px;
            border-left-width: 0px;
            border-top-style: none;
            border-right-style: none;
            border-bottom-style: none;
            border-left-style: none;
        }
    </style>
    <script type="text/javascript" charset="utf-8" src="ueditor/js/ueditor/ueditor.config.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="ueditor/js/ueditor/ueditor.all.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
    <script language="JavaScript">
        <!--
        <%
        if (doc!=null) {
            out.println("var id=" + doc.getID() + ";");
        }
        %>
        var op = "<%=op%>";
        function selectNode(code, name) {
            o("dir_code").value = code;
            $("dirNameSpan").innerHTML = name;
        }
        //-->
    </script>
</head>
<body>
<table width="100%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
    <tr valign="top" bgcolor="#FFFFFF">
        <td width="" height="430" colspan="2" style="background-attachment: fixed; background-repeat: no-repeat">
            <table cellSpacing=0 cellPadding=0 width="100%">
                <tbody>
                <tr>
                    <td width="86%" class="head1">
                        <a href="javascript:;" onclick="closeLeftMenu()"><span id="btnMenu" title="关闭菜单"><i class="fa fa-bars"></i></span></a>
                        <script>
                            $(function() {
                               if (window.parent) {
                                   if (!window.parent.leftFileFrame) {
                                       $('#btnMenu').hide();
                                   }
                               }
                               else {
                                   $('#btnMenu').hide();
                               }
                            });
                        </script>
                        &nbsp;&nbsp;
                        <%
                            String pageUrl = "fileark/document_list_m.jsp?";
                            if (op.equals("add")) {
                        %>
                        <%=dir_name%>&nbsp;-&nbsp;添加
                        <!-- <a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">&nbsp;<%=dir_name%></a> -->
                        <%
                        } else {
                            Leaf dlf = new Leaf();
                            if (doc != null) {
                                dlf = dlf.getLeaf(doc.getDirCode());
                            }
                            if (doc != null && dlf.getType() == 2) {
                        %>
                        <!-- <a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>"><%=dlf.getName()%></a> -->
                        <%=dlf.getName()%>
                        <%} else {%>
                        <%=dir_name%>
                        <%}%>
                        &nbsp;-&nbsp;修改
                        <%}%>
                    </td>
                    <td width="14%" align="right" class="head1">
                        <span id="webeditLink">
                        <%if (cfg.getBooleanProperty("canWebedit")) {%>
                            <a href="fwebedit.jsp?op=<%=op%>&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">高级方式</a>&nbsp;
                        <%} %>
                        </span>
                    </td>
                </tr>
                </tbody>
            </table>
            <form id="addform" name="addform" action="fwebedit_do.jsp?action=fckwebedit_new&dir_code=<%=StrUtil.UrlEncode(dir_code) %>" method="post" enctype="multipart/form-data">
                <table border="0" cellspacing="1" width="100%" cellpadding="5" align="center" bgcolor="#f2f2f2">
                    <%if (doc != null) {%>
                    <tr align="center" bgcolor="#F2F2F2">
                        <td height="20" colspan=2 align=center><b><%=doc != null ? doc.getTitle() : ""%>
                        </b>&nbsp;<input type="hidden" name=isuploadfile value="true"/>
                            <input type="hidden" name=id value="<%=doc!=null?""+doc.getID():""%>"/>
                            <%=doc != null ? "(id:" + doc.getID() + ")" : ""%>
                        </td>
                    </tr>
                    <%}%>
                    <tr>
                        <td colspan="2" align="left" valign="middle">
                            <%
                                // 如果是加入新文章
                                if (doc == null) {
                                    PluginMgr pm = new PluginMgr();
                                    PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                                    if (pu != null) {
                                        IPluginUI ipu = pu.getUI(request);
                                        IPluginViewAddDocument pv = ipu.getViewAddDocument(dir_code);
                                        if (!pu.getAddPage().equals("")) {
                            %>
                            <jsp:include page="<%=pu.getAddPage()%>" flush="true"/>
                            <% } else {
                                out.print(pu.getName(request) + ":&nbsp;" + pv.render(UIAddDocument.POS_TITLE) + "<BR>");
                                out.print(pv.render(UIAddDocument.POS_FORM_ELEMENT) + "<BR>");
                            }
                            }
                            } else {
                                PluginMgr pm = new PluginMgr();
                                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                                if (pu != null) {
                                    IPluginUI ipu = pu.getUI(request);
                                    IPluginViewEditDocument pv = ipu.getViewEditDocument(doc);
                                    if (!pu.getEditPage().equals("")) {
                            %>
                            <jsp:include page="<%=pu.getEditPage()%>" flush="true">
                                <jsp:param name="id" value="<%=doc.getID()%>"/>
                            </jsp:include>
                            <% } else {
                                out.print(pu.getName(request) + ":&nbsp;" + pv.render(UIAddDocument.POS_TITLE) + "<BR>");
                                out.print(pv.render(UIAddDocument.POS_FORM_ELEMENT) + "<BR>");
                            }
                            }
                            }
                            %></td>
                    </tr>
                    <tr>
                        <td colspan="2" align="left" valign="middle">标题：
                            <input name="title" id=me type="text" size=50 maxlength=100
                                   value="<%=doc!=null?doc.getTitle():""%>">
                            <script>
                                var title = new LiveValidation('title');
                                title.add(Validate.Presence);
                            </script>
                            作者：
                            <%
                                String author = "";
                                if (doc != null) {
                                    author = doc.getAuthor();
                                } else {
                                    author = privilege.getUser(request);
                                }
                            %>
                            <input id="author" name="author" value="<%=author%>" size=10 maxlength=100/>
                            <input type="hidden" id="op" name="op" value="<%=op%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="left" valign="middle">关键词：
                            <input title="请用&quot;，&quot;号分隔" name="keywords" id=keywords type="text" size=20
                                   maxlength=100 value="<%=StrUtil.getNullStr(doc==null?dir_name:doc.getKeywords())%>">
                            <!-- 类&nbsp;&nbsp;别： -->
                            <select style="display:none;" id="kind" name="kind">
                                <option value="">无</option>
                                <%
                                    DirKindDb dkd = new DirKindDb();
                                    Vector vkind = dkd.listOfDir(dir_code);
                                    SelectOptionDb sod = new SelectOptionDb();
                                    if (vkind.size() > 0) {
                                        Iterator irkind = vkind.iterator();
                                        while (irkind.hasNext()) {
                                            dkd = (DirKindDb) irkind.next();
                                %>
                                <option value="<%=dkd.getKind()%>"><%=sod.getOptionName("fileark_kind", dkd.getKind())%>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                            <%if (doc != null) {%>
                            <script>
                                o("kind").value = "<%=doc.getKind()%>";
                            </script>
                            <%}%>
                            <input type="hidden" name="isRelateShow" value="1">
                            <%
                                String strChecked = "";
                                if (doc != null) {
                                    if (doc.getCanComment()) {
                                        strChecked = "checked";
                                    }
                                } else {
                                    strChecked = "checked";
                                }

                                boolean filearkDocCanComment = cfg.getBooleanProperty("filearkDocCanComment");
                                String cmtCls = "";
                                if (!filearkDocCanComment) {
                                    cmtCls = " style='display:none'";
                                }
                            %>
                            <span <%=cmtCls%>>
                                <input type="checkbox" name="canComment" value="1" <%=strChecked%>>
                                允许评论
                                <%if (doc != null) {%>
                                [<a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a>]
                                <%}%>
                            </span>
                            <%
                                LeafPriv lp = new LeafPriv(dir_code);
                                if (!leaf.isExamine()) {
                            %>
                            <input type="hidden" name="examine" value="<%=Document.EXAMINE_PASS%>">
                            <%
                                } else {
                                    if (doc!=null && doc.getExamine()!=Document.EXAMINE_DRAFT && lp.canUserExamine(privilege.getUser(request))) {
                            %>
                            &nbsp;&nbsp; <span>审核：</span>
                            <select id="examine" name="examine">
                                <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
                                <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
                                <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
                                <option value="<%=Document.EXAMINE_DUSTBIN%>">回收站</option>
                                <option value="<%=Document.EXAMINE_DRAFT%>">草稿</option>
                            </select>
                            <script>
                                o("examine").value = "<%=doc.getExamine()%>";
                            </script>
                            <%
                                    } else {
                                        int examine = Document.EXAMINE_NOT;
                                        if (doc==null) {
                                            if (lp.canUserExamine(privilege.getUser(request))) {
                                                examine = Document.EXAMINE_PASS;
                                            }
                                        }
                                        else {
                                            examine = doc.getExamine();
                                        }
                            %>
                                        <input type="hidden" name="examine" value="<%=examine%>"/>
                            <%
                                    }
                                }

                                String checknew = "";
                                if (doc != null && doc.getIsNew() == 1) {
                                    checknew = "checked";
                                }
                            %>
                            &nbsp;&nbsp;排序号：
                            <input name="level" value="<%=doc!=null?doc.getLevel():"0"%>" size="2"/>
                            (<a href="javascript:;" onclick="o('level').value=100">置顶</a>)
                        </td>
                    </tr>
                    <tr align="left">
                        <td colspan="2" valign="middle">
                            <%if (doc != null) {%>
                            <script>
                                var bcode = "<%=doc.getDirCode()%>";
                            </script>
                            目录：
                            <%
                                if (leaf.getType() == Leaf.TYPE_DOCUMENT) {
                                    out.print("<input name=dir_code type=hidden value='" + doc.getDirCode() + "'>" + leaf.getName());
                                } else {

                                Leaf lf = dir.getLeaf(doc.getDirCode());
                            %>
                            <input value="<%=lf.getName() %>" id="directory" readOnly/>
                            &nbsp;<a href="javascript:;" onclick="selDept()" style="color:#666">选择</a>
                            &nbsp;( <span class="style3">蓝色</span>表示可选 )
                            <%}%>
                            <%} else {%>
                            <input type=hidden name="dir_code" value="<%=dir_code%>">
                            <%}%>
                            <input name="templateId" class="btn" value="<%=templateId%>" type=hidden>
                            <%
                                if ("edit".equals(op)) {
                                    // LeafPriv lp = new LeafPriv(doc.getDirCode());
                                    if (lp.canUserExamine(privilege.getUser(request))) {
                            %>
                            &nbsp;&nbsp;创建日期：
                            <input id="createDate" name="createDate" value="<%=doc!=null ? DateUtil.format(doc.getCreateDate(), "yyyy-MM-dd HH:mm:ss") : ""%>"/>
                            <%
                                    }
                                }
                            %>
                        </td>
                    </tr>
                    <tr align="left">
                        <td colspan="2" valign="middle">
                            <div class="img-preview-box">
                                <div class="upfile-box">
                                    <input type="file" class="upfile-ctrl" id="titleImage" name="titleImage" multiple="" accept="image/png, image/jpeg, image/gif, image/jpg"/>
                                    <div class="upfile-box-tip">
                                        <span>+</span>
                                        <p>点击或拖拽到“+”<br/>上传标题图片</p>
                                    </div>
                                </div>
                                <%
                                    if ("edit".equals(op)) {
                                        Vector titleImages = doc.getTitleImages(1);
                                        Iterator irT = titleImages.iterator();
                                        while (irT.hasNext()) {
                                            Attachment att = (Attachment)irT.next();
                                    %>
                                <div class="upfile-image-box upload-image-box-db" data-id="<%=att.getId()%>"><img class="upfile-image" id="img_<%=att.getDiskName()%>" src="<%=request.getContextPath()%>/<%=att.getVisualPath()%>/<%=att.getDiskName()%>"/>
                                    <div class='upfile-cover'>
                                        <div class='btn-bar'>
                                            <span class='btn-del' title="删除">
                                                <i class="fa fa-trash"></i>
                                            </span>
                                            <span class='btn-cancel' title="取消标题图片，置为附件">
                                                <i class="fa fa-minus-circle"></i>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                    <%
                                        }
                                    }
                                %>
                            </div>
                        </td>
                    </tr>
                    <tr align="left">
                        <td colspan="2" valign="middle">
                            摘要：<br/>
                            <textarea id="summary" name="summary" rows="4" style="width: 95%; margin-top: 5px"><%=doc!=null?doc.getSummary():""%></textarea>
                        </td>
                    </tr>
                    <tr align="left">
                        <td colspan="2" valign="middle">颜色：
                            <select name="color" id="color">
                                <option value="" style="COLOR: black" selected>显示颜色</option>
                                <option style="BACKGROUND: #000088" value="#000088">显示颜色</option>
                                <option style="BACKGROUND: #0000ff" value="#0000ff">显示颜色</option>
                                <option style="BACKGROUND: #008800" value="#008800">显示颜色</option>
                                <option style="BACKGROUND: #008888" value="#008888">显示颜色</option>
                                <option style="BACKGROUND: #0088ff" value="#0088ff">显示颜色</option>
                                <option style="BACKGROUND: #00a010" value="#00a010">显示颜色</option>
                                <option style="BACKGROUND: #1100ff" value="#1100ff">显示颜色</option>
                                <option style="BACKGROUND: #111111" value="#111111">显示颜色</option>
                                <option style="BACKGROUND: #333333" value="#333333">显示颜色</option>
                                <option style="BACKGROUND: #50b000" value="#50b000">显示颜色</option>
                                <option style="BACKGROUND: #880000" value="#880000">显示颜色</option>
                                <option style="BACKGROUND: #8800ff" value="#8800ff">显示颜色</option>
                                <option style="BACKGROUND: #888800" value="#888800">显示颜色</option>
                                <option style="BACKGROUND: #888888" value="#888888">显示颜色</option>
                                <option style="BACKGROUND: #8888ff" value="#8888ff">显示颜色</option>
                                <option style="BACKGROUND: #aa00cc" value="#aa00cc">显示颜色</option>
                                <option style="BACKGROUND: #aaaa00" value="#aaaa00">显示颜色</option>
                                <option style="BACKGROUND: #ccaa00" value="#ccaa00">显示颜色</option>
                                <option style="BACKGROUND: #ff0000" value="#ff0000">显示颜色</option>
                                <option style="BACKGROUND: #ff0088" value="#ff0088">显示颜色</option>
                                <option style="BACKGROUND: #ff00ff" value="#ff00ff">显示颜色</option>
                                <option style="BACKGROUND: #ff8800" value="#ff8800">显示颜色</option>
                                <option style="BACKGROUND: #ff0005" value="#ff0005">显示颜色</option>
                                <option style="BACKGROUND: #ff88ff" value="#ff88ff">显示颜色</option>
                                <option style="BACKGROUND: #ee0005" value="#ee0005">显示颜色</option>
                                <option style="BACKGROUND: #ee01ff" value="#ee01ff">显示颜色</option>
                                <option style="BACKGROUND: #3388aa" value="#3388aa">显示颜色</option>
                                <option style="BACKGROUND: #000000" value="#000000">显示颜色</option>
                            </select>
                            <%
                                if (doc != null) {
                            %>
                            <script>
                                addform.color.value = "<%=StrUtil.getNullStr(doc.getColor())%>";
                                $('#color').css("color", addform.color.value);
                            </script>
                            <%
                                }
                            %>
                            <script>
                                $('#color').change(function () {
                                    $('#color').css("color", $(this).val());
                                });
                            </script>
                            <%
                                String strExpireDate = "";
                                if (doc != null) {
                                    strExpireDate = DateUtil.format(doc.getExpireDate(), "yyyy-MM-dd");
                            %>
                            <input type="checkbox" id="isBold" name="isBold"
                                   value="true" <%=doc.isBold()?"checked":""%> >
                            <%} else {%>
                            <input type="checkbox" id="isBold" name="isBold" value="true">
                            <%}%>
                            标题加粗
                            &nbsp;到期时间：
                            <input type="text" id="expireDate" name="expireDate" size="10" value="<%=strExpireDate%>">
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" valign="top">
                            <div style="clear:both">
                                <script id="htmlcode" name="htmlcode" type="text/plain">
                                    <%
                                    if (template != null) {
                                        out.print(template.getContent(1));
                                    } else if (!op.equals("add")) {
                                        out.print(doc.getDocContent(1).getContent());
                                    }
                                %>
                                </script>
                                <%--<textarea id="htmlcode" name="htmlcode">
                                </textarea>--%>
                            </div>
                            <script>
                                var uEditor;
                                $(function () {
                                    uEditor = UE.getEditor('htmlcode', {
                                        //allowDivTransToP: false,//阻止转换div 为p
                                        toolleipi: true,//是否显示，设计器的 toolbars
                                        textarea: 'htmlcode',
                                        enableAutoSave: false,
                                        toolbars: [[
                                            'fullscreen', 'source', '|', 'undo', 'redo', '|',
                                            'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'selectall', 'cleardoc', '|',
                                            'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                                            'paragraph', 'fontfamily', 'fontsize', '|',
                                            'directionalityltr', 'directionalityrtl', 'indent', '|',
                                            'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                                            'link', 'unlink', 'anchor', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                                            'simpleupload', 'insertimage', 'insertvideo', 'emotion', 'map', 'insertframe', 'insertcode', 'pagebreak', 'template', '|',
                                            'horizontal', 'date', /*'time'*/, 'spechars', '|',
                                            'inserttable', 'deletetable', 'insertparagraphbeforetable', 'insertrow', 'deleterow', 'insertcol', 'deletecol', 'mergecells', 'mergeright', 'mergedown', 'splittocells', 'splittorows', 'splittocols', '|',
                                            'print', 'preview', 'searchreplace', 'help'
                                        ]],
                                        //focus时自动清空初始化时的内容
                                        //autoClearinitialContent:true,
                                        //关闭字数统计
                                        wordCount: false,
                                        //关闭elementPath
                                        elementPathEnabled: false,
                                        //默认的编辑区域高度
                                        initialFrameHeight: 300,
                                        disabledTableInTable: false
                                        ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
                                        //更多其他参数，请参考ueditor.config.js中的配置项
                                    });

                                    UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
                                    UE.Editor.prototype.getActionUrl = function (action) {
                                        if (action == 'uploadimage' || action == 'uploadscrawl') {
                                            return '<%=request.getContextPath()%>/ueditor/UploadFile?op=fileark';
                                        } else if (action == 'uploadvideo') {
                                            return '<%=request.getContextPath()%>/ueditor/UploadFile?op=fileark';
                                        } else {
                                            return this._bkGetActionUrl.call(this, action);
                                        }
                                    }
                                });
                            </script>
                        </td>
                    </tr>
                    <%
                        com.redmoon.clouddisk.Config cloudCfg = com.redmoon.clouddisk.Config.getInstance();
                        if (cloudCfg.getBooleanProperty("isUsed")) {
                    %>
                    <tr>
                        <td style="width:5%" align="left" valign="top">网盘：</td>
                        <td>
                            <a href="javascript:;"
                               onClick="openWin('netdisk/clouddisk_list.jsp?mode=select', 850, 600)">选择文件</a>
                            <div id="netdiskFilesDiv" style="line-height:1.5"></div>
                        </td>
                    </tr>
                    <%
                        }

                        String trFilePrivDis = (lp.canUserExamine(privilege.getUser(request)) || cfg.getBooleanProperty("filearkCanAuthorSetFilePriv")) ? "" : "none";
                    %>
                    <tr style="display: <%=trFilePrivDis%>">
                        <td>
                            浏览范围：
                            <a href="javascript:;" onclick="openWin('admin/dept_role_group_sel.jsp', 800, 600)">设置权限</a>
                            &nbsp;<img src="admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:none;" id="imgGou"/>
                            <%
                                // 如果是编辑，则根据权限，取出其所有具有读权限的部门、角色、组
                                /**
                                 * 	public static final int TYPE_USERGROUP = 0;
                                 *     public static final int TYPE_USER = 1;
                                 *     public static final int TYPE_ROLE = 2;
                                 *     public static final int TYPE_DEPT = 3;
                                 */
                                JSONArray arr = new JSONArray();
                                if (doc != null) {
                                    DeptMgr deptMgr = new DeptMgr();
                                    DocPriv docPriv = new DocPriv();
                                    Iterator<DocPriv> ir = docPriv.listByDocId(doc.getId()).iterator();
                                    while (ir.hasNext()) {
                                        docPriv = ir.next();
                                        if (docPriv.getSee()!=1) {
                                            continue;
                                        }
                                        int type = docPriv.getType();
                                        JSONObject json = new JSONObject();
                                        switch (type) {
                                            case DocPriv.TYPE_DEPT:
                                                DeptDb dd = deptMgr.getDeptDb(docPriv.getName());
                                                if (dd!=null) {
                                                    json.put("kind", type);
                                                    json.put("code", dd.getCode());
                                                    json.put("name", dd.getName());
                                                    arr.add(json);
                                                }
                                                break;
                                            case DocPriv.TYPE_ROLE:
                                                RoleDb rd = new RoleDb();
                                                rd = rd.getRoleDb(docPriv.getName());
                                                json.put("kind", type);
                                                json.put("code", rd.getCode());
                                                json.put("name", rd.getDesc());
                                                arr.add(json);
                                                break;
                                            case DocPriv.TYPE_USERGROUP:
                                                UserGroupDb ug = new UserGroupDb();
                                                ug = ug.getUserGroupDb(docPriv.getName());
                                                json.put("kind", type);
                                                json.put("code", ug.getCode());
                                                json.put("name", ug.getDesc());
                                                arr.add(json);
                                                break;
                                            case DocPriv.TYPE_USER:
                                                UserDb ud = new UserDb();
                                                ud = ud.getUserDb(docPriv.getName());
                                                json.put("kind", type);
                                                json.put("code", ud.getName());
                                                json.put("name", ud.getRealName());
                                                arr.add(json);
                                                break;
                                        }
                                    }
                                }
                                if (arr.size()>0) {
                            %>
                                <script>
                                    $('#imgGou').show();
                                </script>
                            <%
                                }
                            %>
                            <textarea id="deptRoleGroup" name="deptRoleGroup" style="display: none"><%=arr.toString()%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:5%" align="left" valign="top">
                            附件：<br/>
                            <%
                                Calendar cal = Calendar.getInstance();
                                String year = "" + (cal.get(Calendar.YEAR));
                                String month = "" + (cal.get(Calendar.MONTH) + 1);
                                String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
                            %>
                            <input type="hidden" name="filepath" value="<%=filepath%>"/>
                            <script>initUpload();</script>
                        </td>
                    </tr>
                    <%
                        if (doc != null) {
                            Vector attachments = doc.getAttachments(1);
                            Iterator ir = attachments.iterator();
                            int[] ary = new int[attachments.size()];
                            int k = 0;
                            while (ir.hasNext()) {
                                Attachment am = (Attachment)ir.next();
                                ary[k] = am.getId();
                                k++;
                            }

                            ir = attachments.iterator();
                            if (ir.hasNext()) {
                    %>
                    <tr>
                        <td height="25" colspan=2 align="center">
                            <table id="tableAtt" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <%
                                while (ir.hasNext()) {
                                    Attachment am = (Attachment) ir.next();
                            %>
                                <tr id="row<%=am.getId()%>">
                                    <td width="8%" align="center"><img src=images/attach.gif></td>
                                    <td width="92%" align="left">
                                        <input id="attachName<%=am.getId()%>" name="attachName" value="<%=am.getName()%>" size="30">
                                        <a href="javascript:;" onclick="changeAttachName('<%=am.getId()%>', '<%=doc.getID()%>', '<%="attachName"+am.getId()%>')">重命名</a>&nbsp;&nbsp;
                                        <a href="javascript:;" onclick="delAttach(<%=am.getId()%>)">删除</a>&nbsp;&nbsp;
                                        <a target=_blank href="fileark/download.do?docId=<%=doc.getID()%>&attachId=<%=am.getId()%>">下载</a>&nbsp;&nbsp;
                                        <%if (StrUtil.getFileExt(am.getDiskName()).equals("doc") || StrUtil.getFileExt(am.getDiskName()).equals("docx") || StrUtil.getFileExt(am.getDiskName()).equals("xls") || StrUtil.getFileExt(am.getDiskName()).equals("xlsx")) {%>
                                        <a href="javascript:;" onClick="editdoc(<%=doc.getID()%>, <%=am.getId()%>)">编辑</a>&nbsp;&nbsp;
                                        <%}%>
                                        <%
                                            if (StrUtil.isImage(StrUtil.getFileExt(am.getName()))) {
                                        %>
                                        <a href="javascript:;" onclick="changeTitleImage(<%=am.getId()%>, true)" title="置为标题图片">标题图片</a>&nbsp;&nbsp;
                                        <%
                                            }
                                        %>
                                        <a href="javascript:;" onclick="move('up', <%=am.getId()%>)"><img src="images/arrow_up.gif" alt="往上" width="16" height="20" border="0" align="absmiddle"></a>&nbsp;
                                        <a href="javascript:;" onclick="move('down', <%=am.getId()%>)"><img src="images/arrow_down.gif" alt="往下" width="16" height="20" border="0" align="absmiddle"></a>
                                    </td>
                                </tr>
                            <%
                            }%>
                            </table>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    <tr>
                        <td height="30" colspan=2 align=center>
                            <%
                                if (op.equals("add")) {
                            %>
                            <input id="btnDraft" type="button" class="mybtn" value="保存草稿">
                            <%
                                }
                                else {
                                    if (doc.getExamine()==Document.EXAMINE_DRAFT) {
                            %>
                            <input id="btnDraft" type="button" class="mybtn" value="保存草稿">
                            <%
                                    }
                                }

                                if (op.equals("add")) {
                                    action = "添 加";
                                }
                                else {
                                    if (doc.getExamine()==Document.EXAMINE_DRAFT) {
                                        action = "发 布";
                                    }
                                    else {
                                        action = "修 改";
                                    }
                                }
                            %>
                            <script>
                                $('#btnDraft').click(function(e) {
                                    o("examine").value = "<%=Document.EXAMINE_DRAFT%>";
                                    submitForm();
                                });
                            </script>
                            <input id="btnOK" type="button" class="mybtn" value=" <%=action%> ">
                            <%
                                String prjUrl = "";
                                if (dir_code.indexOf("cws_prj_") == 0) {
                                    String project = dir_code.substring(8);
                                    int p = project.indexOf("_");
                                    if (p != -1) {
                                        project = project.substring(0, p);
                                    }
                                    prjUrl += "projectId=" + project + "&parentId=" + project + "&formCode=project";
                                }

                                if (!"edit".equals(op)) {
                            %>
                            &nbsp;<input name="remsg" type="button" class="mybtn" onClick='location.href="fileark/document_list_m.jsp?dir_code=<%=dir_code %>&<%=prjUrl %>"' value=" 返 回 ">
                            <%
                                }
                            %>
                            &nbsp;<input type="button" class="mybtn" onClick='window.location.reload()' value=" 刷 新 ">
                            <%
                                if (op.equals("edit")) {
                                    String viewPage = "doc_show.jsp";
                                    PluginMgr pm = new PluginMgr();
                                    PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                                    if (pu != null) {
                                        IPluginUI ipu = pu.getUI(request);
                                        viewPage = pu.getViewPage();
                                    }
                            %>
                            &nbsp;<input name="remsg" type="button" class="mybtn" onclick='addTab("<%=doc.getTitle()%>", "<%=viewPage%>?id=<%=id%>")' value=" 查 看 ">
                            <%
                                }
                                else {
                            %>
                            &nbsp;<input type="button" class="mybtn" onclick='preview()' value=" 预 览 ">
                            <%
                                }
                            %>
                        </td>
                    </tr>
                </table>
            </form>
        </td>
    </tr>
</table>
</body>
<script>
    function preview() {
        var tabIdOpener = getActiveTabId();
        addTab('预览', '<%=request.getContextPath()%>/doc_preview.jsp?tabIdOpener=' + tabIdOpener);
    }

    function getTitle() {
        return o("title").value;
    }

    function getSummary() {
        return o("summary").value;
    }

    function getContent() {
        return uEditor.getContent();
    }

    $(function () {
        $('#createDate').datetimepicker({
            lang: 'ch',
            timepicker: true,
            format:'Y-m-d H:i:00',
            step:10
        });
        $('#expireDate').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
        $('#expire_date').datetimepicker({
            lang: 'ch',
            timepicker: false,
            format: 'Y-m-d',
            formatDate: 'Y/m/d'
        });
    });

    function changeAttachName(attach_id, doc_id, nm) {
        $.ajax({
            type: "post",
            url: "fileark/changeAttachName.do",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                attachId: attach_id,
                newName: $('#' + nm).val()
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
                $('body').showLoading();
            },
            success: function(data, status){
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function doDelAttach(attachId, isTitleImage) {
        var re = false;
        $.ajax({
            async: false,
            type: "post",
            url: "fileark/delAttach.do",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                attachId: parseInt(attachId)
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
                $('body').showLoading();
            },
            success: function(data, status){
                data = $.parseJSON(data);
                if (data.ret==1) {
                    re = true;
                }
                if (!isTitleImage) {
                    jAlert(data.msg, "提示");
                }
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
        return re;
    }

    function delAttach(attachId) {
        jConfirm('您确定要删除么？', '提示', function(r) {
            if (r) {
                if (doDelAttach(attachId)) {
                    $('#row' + attachId).remove();
                }
            }
        });
    }

    function selDept() {
        openWin("fwebedit_left_choose.jsp", 300, 400, "yes");
    }

    function setNetdiskFiles(ids) {
        getNetdiskFiles(ids);
    }

    function doGetNetdiskFiles(response) {
        var rsp = response.responseText.trim();
        o("netdiskFilesDiv").innerHTML += rsp;
    }

    var errFunc = function (response) {
        // alert('Error ' + response.status + ' - ' + response.statusText);
        jAlert(response.responseText, '提示');
    };

    function getNetdiskFiles(ids) {
        var str = "ids=" + ids;
        var myAjax = new cwAjax.Request(
                "<%=cn.js.fan.web.Global.getFullRootPath(request)%>/netdisk/ajax_getfile.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doGetNetdiskFiles,
                    onError: errFunc
                }
        );
    }

    $(function() {
        $('#btnOK').click(function() {
            if (o("examine").value=="<%=Document.EXAMINE_DRAFT%>") {
                <%
                    // 当由草稿状态转发布时的状态
                    int examineWhenPublish = Document.EXAMINE_NOT;
                    if (!leaf.isExamine() || lp.canUserExamine(privilege.getUser(request))) {
                        examineWhenPublish = Document.EXAMINE_PASS;
                    }
                %>
                o("examine").value = "<%=examineWhenPublish%>";
            }
            submitForm();
        });
    });

    function submitForm() {
        if (!LiveValidation.massValidate(title.formObj.fields)) {
            jAlert("请检查表单中的内容填写是否正常！","提示");
            return;
        }
        $('#btnOK').attr("disabled", true);

        var formData = new FormData($('#addform')[0]);
        for (var i = 0; i < dropFiles.length; i++) {
            formData.append("titleImage" + i, dropFiles[i].file);
        }

        // 取得顺序号
        var imgOrders = "";
        $('.img-preview-box .upfile-image').each(function() {
            var imgName = $(this).attr('img_name');
            if (imgName==null) {
                imgName = $(this).parent().data('id');
            }
            if (""==imgOrders) {
                imgOrders = imgName;
            }
            else {
                imgOrders += "," + imgName;
            }
        });
        formData.append("imgOrders", imgOrders);

        $.ajax({
            url: 'fileark/operate.do?action=fckwebedit_new&dir_code=<%=StrUtil.UrlEncode(dir_code) %>' ,
            type: 'post',
            data: formData,
            async: true,
            // 下面三个参数要指定，如果不指定，会报一个JQuery的错误
            cache: false,
            contentType: false,
            processData: false,
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
                $('body').showLoading();
            },
            success: function (data) {
                var data = $.parseJSON($.trim(data));
                if (data.ret=="0") {
                    jAlert(data.msg, "提示");
                    $('#btnOK').attr("disabled", false);
                }
                else {
                    if ($('#op').val()=="add") {
                        if (data.redirectUri != "") {
                            jAlert_Redirect(data.msg, "提示", data.redirectUri);
                        }
                        else {
                            if (o("examine").value==<%=Document.EXAMINE_DRAFT%>) {
                                jAlert_Redirect(data.msg, "提示", "fwebedit_new.jsp?op=edit&id=" + data.docId);
                            }
                            else {
                                var msg = data.msg;
                                if (data.examineFlowId!=null && data.examineFlowId!=<%=DocumentMgr.EXAMINE_FLOW_ID_NONE%>) {
                                    msg = "操作成功，文章正在流程审核中...";
                                }
                                jAlert_Redirect(msg, "提示", "fileark/document_list_m.jsp?dir_code=<%=dir_code%>");
                            }
                        }
                    }
                    else {
                        var msg = data.msg;
                        if (data.examineFlowId!=null && data.examineFlowId!=<%=DocumentMgr.EXAMINE_FLOW_ID_NONE%>) {
                            msg = "操作成功，文章正在流程审核中...";
                        }
                        $.toaster({priority: 'info', message: msg});
                        $('#btnOK').attr("disabled", false);
                        /*jAlert(msg, "提示", function() {
                            // 可能会上传新的附件，所以需reload才能看到
                            window.location.reload();
                        });*/
                    }
                }
            },
            complete: function(XMLHttpRequest, status){
                $('body').hideLoading();
            },
            error: function (returndata) {
                $('body').hideLoading();
                $('#btnOK').attr("disabled", false);
                alert(returndata);
            }
        });
    }

    function move(direction, attachId) {
        $tr = $("#row" + attachId);
        if (direction == "up") {
            if ($tr.index() == 0) {
                jAlert("无法上移", "提示");
                return;
            }
        }
        else {
            var trLen = $("#tableAtt").find("tr").length;
            if ($tr.index() == trLen - 1) {
                jAlert("无法下移", "提示");
                return;
            }
        }
        $.ajax({
            type: "post",
            url: "fileark/move.do",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                attachId: attachId,
                direction: direction
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
                if (data.ret == 1) {
                    if (direction == "up") {
                        if ($tr.index() != 0) {
                            $tr.prev().before($tr);
                        }
                    }
                    else {
                        var trLen = $("#tableAtt").find("tr").length;
                        if ($tr.index() != trLen - 1) {
                            $tr.next().after($tr);
                        }
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

    function setDeptRoleGroup(jsonArr) {
        if (jsonArr.length>0) {
            $('#imgGou').show();
        }
        else {
            $('#imgGou').hide();
        }
        $('#deptRoleGroup').val(JSON.stringify(jsonArr));
    }

    function getDeptRoleGroup() {
        return $('#deptRoleGroup').val();
    }

    var dropFiles = [];

    // 实现多图上传预览功能
    $(function () {
        function showImgList(fileList) {
            // 添加时间戳
            var dateTime = new Date().getTime();
            for (var i = 0; i < fileList.length; i++) {
                var picHtml = "<div class='upfile-image-box' ><img class='upfile-image' id='img_" + dateTime + "_" + fileList[i].name + "'/><div class='upfile-cover'><div class='btn-bar'><span class='btn-del'>×</span></div></div></div>";
                $(".img-preview-box").append(picHtml);

                var imgObjPreview = document.getElementById("img_" + dateTime + "_" + fileList[i].name);
                if (fileList[i]) {
                    var file = fileList[i];
                    imgObjPreview.style.display = 'block';
                    imgObjPreview.src = window.URL.createObjectURL(fileList[i]);

                    var $img = $(imgObjPreview);
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

            // var formData = new FormData($('#addform')[0]);
            for (var i = 0; i < fileList.length; i++) {
                // formData.append("titleImage" + i, fileList[i]);
                var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size" : fileList[i].size, "type" : fileList[i].type};
                dropFiles.push(json);
            }

            showImgList(fileList);
        });

        $(".upfile-ctrl").change(function(e) {
            var fileList = $(this)[0].files;
            for (var i = 0; i < fileList.length; i++) {
                var json = {"name": fileList[i].name, "file": fileList[i], "lastModified": fileList[i].lastModified, "size" : fileList[i].size, "type" : fileList[i].type};
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
                // console.log('onEnd:', [evt.item, evt.from]);
            }
        });

        // 删除
        $(".img-preview-box").on("click", ".btn-del", function () {
            var id = $(this).parents(".upfile-image-box").data('id');
            if (id != null) {
                // 删除已存文件
                var self = this;
                jConfirm("您确定要删除吗？", "提示", function (r) {
                    if (!r) {
                        return;
                    } else {
                        var re = doDelAttach(id, true);
                        if (re) {
                            $(self).parents(".upfile-image-box").remove();
                        }
                    }
                })
            }
            else {
                // 删除新增文件
                var $img = $(this).parents(".upfile-image-box").find('img');
                $(this).parents(".upfile-image-box").remove();
                var ary = [];
                for (i = 0; i < dropFiles.length; i++) {
                    var f = dropFiles[i];

                    // IE中无法取到文件的lastModified
                    // console.log(f.name + '--' + $img.attr('img_name') + "," + f.type + '--' + $img.attr('img_type') + "," + f.size + '--' + $img.attr('img_size') + ',' + f.lastModified + '--' + $img.attr('img_lastmodified'));
                    if (isIE()) {
                        if (!(f.name == $img.attr('img_name') && f.type == $img.attr('img_type') && (''+f.size) == $img.attr('img_size'))) {
                            ary.push(f);
                        }
                    }
                    else {
                        if (!(f.name == $img.attr('img_name') && f.type == $img.attr('img_type') && (''+f.size) == $img.attr('img_size') && (''+f.lastModified) == $img.attr('img_lastmodified'))) {
                            ary.push(f);
                        }
                    }
                }
                dropFiles = ary;
                // console.log(dropFiles);
            }
        });
    });

    $(function() {
        if (!isIE()) {
            $('#webeditLink').hide();
        }
    });

    function changeTitleImage(attachId, isTitleImage) {
        var t = '';
        if (isTitleImage) {
            t = '您确定要置为标题图片么？';
        }
        else {
            t = '您确定要取消标题图片么？';
        }
        jConfirm(t, '提示', function(r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "fileark/changeTitleImage.do",
                    contentType:"application/x-www-form-urlencoded; charset=utf-8",
                    data: {
                        attachId: attachId,
                        isTitleImage: isTitleImage
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                        $('body').showLoading();
                    },
                    success: function(data, status){
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            jAlert(data.msg, "提示", function() {
                                window.location.reload();
                            });
                        }
                        else {
                            jAlert(data.msg, "提示");
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('body').hideLoading();
                    },
                    error: function(XMLHttpRequest, textStatus){
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    $(".img-preview-box").on("click", ".btn-cancel", function () {
        var id = $(this).parents(".upfile-image-box").data('id');
        changeTitleImage(id, false);
    });
</script>
</html>