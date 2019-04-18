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
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.clouddisk.Config" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="inc/common.js"></script>
    <script type="text/javascript" src="js/jquery1.7.2.min.js"></script>
    <link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="inc/map.js"></script>
    <script src="inc/livevalidation_standalone.js"></script>
    <script src="js/jquery.form.js"></script>
    <link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
    <script src="js/datepicker/jquery.datetimepicker.js"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
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

        Privilege privilege = new Privilege();

        String correct_result = "操作成功！";
        Document doc = null;
        Document template = null;

        String op = ParamUtil.get(request, "op");
        if (op.equals("edit")) {
            id = ParamUtil.getInt(request, "id");
            doc = docmanager.getDocument(id);
            dir_code = doc.getDirCode();
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        Leaf leaf = dir.getLeaf(dir_code);

        String strtemplateId = ParamUtil.get(request, "templateId");
        int templateId = Document.NOTEMPLATE;
        if (!strtemplateId.trim().equals("")) {
            if (StrUtil.isNumeric(strtemplateId))
                templateId = Integer.parseInt(strtemplateId);
        }
        if (templateId == Document.NOTEMPLATE) {
            templateId = leaf.getTemplateId();
        }

        if (templateId != Document.NOTEMPLATE) {
            template = docmanager.getDocument(templateId);
        }

        String action = ParamUtil.get(request, "action");
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
                    out.print(SkinUtil.makeErrMsg(request, privilege.MSG_INVALID));
                    return;
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
    <title><%=doc != null ? doc.getTitle() : ""%>
    </title>
    <style type="text/css">
        .head1 {
            background-color: #daeaf8;
            height: 23px;
            font-size: 14px;
            font-weight: bold;
            color: #666666;
            padding: 8px 0px 0px 5px;
            border-bottom: 2px solid #92b4d2;
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

        .mybtn1 {
            border: 1px solid #d4d4d4;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            behavior: url(skin/common/ie-css3.htc);
            cursor: pointer;
            padding-right: 5px;
            padding-left: 5px;
            height: 30px;
            width: 50px;
            line-height: 27px;
            background-color: #FFF !important;
            cursor: pointer;
            font-weight: bold;
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

        function showvote(isshow) {
            if (addform.isvote.checked) {
                divVote.style.display = "";
            }
            else {
                divVote.style.display = "none";
            }
        }

        var attachCount = 1;

        function AddAttach() {
            updiv.insertAdjacentHTML("BeforeEnd", "<div><input type='file' name='attachment" + attachCount + "'></div>");
            attachCount += 1;
        }

        function selectNode(code, name) {
            addform.dir_code.value = code;
            $("dirNameSpan").innerHTML = name;
        }
        //-->
    </script>
</head>
<body>
<TABLE width="98%" BORDER=0 align="center" CELLPADDING=0 CELLSPACING=0>
    <TR valign="top" bgcolor="#FFFFFF">
        <TD width="" height="430" colspan="2"
            style="background-attachment: fixed; background-repeat: no-repeat">
            <TABLE cellSpacing=0 cellPadding=0 width="100%">
                <TBODY>
                <TR>
                    <TD width="86%" class="head1">
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
                        <script>
                            if (typeof(window.parent.leftFileFrame) == "object") {
                                var btnN = "关闭菜单";
                                if (window.parent.getCols() != "200,*") {
                                    btnN = "打开菜单";
                                    isLeftMenuShow = false;
                                }
                                //document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">");
                                //document.write(btnN);
                                //document.write("</span></a>");
                            }
                        </script>
                    </TD>
                    <td width="14%" align="right" class="head1">
                <span id="webeditLink">
                <%if (cfg.getBooleanProperty("canWebedit")) {%>
                    <a href="fwebedit.jsp?op=<%=op%>&id=<%=id%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">高级方式</a>&nbsp;            
                <%} %>
                </span>
                    </td>
                </TR>
                </TBODY>
            </TABLE>
            <form id="addform" name="addform" action="fwebedit_do.jsp?action=fckwebedit_new&dir_code=<%=StrUtil.UrlEncode(dir_code) %>" method="post"
                  enctype="multipart/form-data">
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
                        <td colspan="2" align="left" valign="middle">关键字：
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
                                    if (doc.getCanComment())
                                        strChecked = "checked";
                                } else
                                    strChecked = "checked";
                            %>
                            <input type="checkbox" name="canComment" value="1" <%=strChecked%>>
                            允许评论
                            <%if (doc != null) {%>
                            [<a href="fileark/comment_m.jsp?doc_id=<%=doc.getID()%>">管理评论</a>]
                            <%}%>
                            <%
                                if (false && !leaf.isExamine()) {
                            %>
                            <input type="hidden" name="examine" value="<%=Document.EXAMINE_PASS%>">
                            <%
                            } else {
                                LeafPriv lp = new LeafPriv(dir_code);
                                if (lp.canUserExamine(privilege.getUser(request))) {
                            %>&nbsp;&nbsp; <span class="style2">审核</span>
                            <select id="examine" name="examine">
                                <option value="<%=Document.EXAMINE_PASS%>">已通过</option>
                                <option value="<%=Document.EXAMINE_NOT%>">未审核</option>
                                <option value="<%=Document.EXAMINE_NOTPASS%>">未通过</option>
                                <option value="<%=Document.EXAMINE_DUSTBIN%>">回收站</option>
                            </select>
                            <%if (doc != null) {%>
                            <script>
                                addform.examine.value = "<%=doc.getExamine()%>";
                            </script>
                            <%}%>
                            <%} else {%>
                            <input type="hidden" name="examine" value="<%=(doc!=null)?""+doc.getExamine():"0"%>">
                            <%
                                    }
                                }
                            %>
                            <%
                                String checknew = "";
                                if (doc != null && doc.getIsNew() == 1)
                                    checknew = "checked";
                            %>
                            <!--
<input type="checkbox" name="isNew" value="1" <%=checknew%>>
<img src="images/i_new.gif" width="18" height="7">
--></td>
                    </tr>
                    <tr align="left">
                        <td colspan="2" valign="middle">
                            <%if (doc != null) {%>
                            <script>
                                var bcode = "<%=doc.getDirCode()%>";
                            </script>
                            目录：
                            <%
                                if (leaf.getType() == leaf.TYPE_DOCUMENT) {
                                    out.print("<input name=dir_code type=hidden value='" + doc.getDirCode() + "'>" + leaf.getName());
                                } else {
                            %>
                            <!-- <select id="dir_code" name="dir_code" onChange="if(this.options[this.selectedIndex].value=='not'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示'); this.value=bcode; return false;}">
						  <option value="not" selected>请选择目录</option>
					<%
					//Leaf lf = dir.getLeaf("root");
					//DirectoryView dv = new DirectoryView(request, lf);
					//dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
					
					Leaf lf = dir.getLeaf(doc.getDirCode());
					%>
					</select> -->
                            <input value="<%=lf.getName() %>" id="directory" readOnly/>
                            &nbsp;<a href="javascript:;" onclick="selDept()" style="color:#666">选择</a>
                            <script>
                                //addform.dir_code.value = "<%=doc.getDirCode()%>";
                            </script>
                            &nbsp;( <span class="style3">蓝色</span>表示可选 )
                            <%}%>
                            <%} else {%>
                            <input type=hidden name="dir_code" value="<%=dir_code%>">
                            <%}%>
                            <input name="templateId" class="btn" value="<%=templateId%>" type=hidden>
                            排序号：
                            <input name="level" value="<%=doc!=null?doc.getLevel():"0"%>" size="2"/>
                            (<a href="javascript:;" onclick="o('level').value=100">置顶</a>)
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
                            <%if (doc != null) {%>
                            <script>
                                addform.color.value = "<%=StrUtil.getNullStr(doc.getColor())%>";
                                $('#color').css("color", addform.color.value);
                            </script>
                            <%}%>
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
                    <tr style="display:none">
                        <td style="width:5%" valign="middle">
                            <script>
                                var vp = "";
                            </script>
                            <%
                                String display = "none", ischecked = "false", isreadonly = "";
                                if (doc != null) {
                                    if (doc.getType() == 1) {
                                        display = "";
                                        ischecked = "checked disabled";
                                        isreadonly = "readonly";
                            %>
                            <script>
                                var voteoption = "<%=doc.getVoteOption()%>";
                                var votes = voteoption.split("|");
                                var len = votes.length;
                                for (var i = 0; i < len; i++) {
                                    if (vp == "")
                                        vp = votes[i];
                                    else
                                        vp += "\r\n" + votes[i];
                                }
                            </script>
                            <%
                                    }
                                }
                            %>
                            <input type="checkbox" name="isvote" value="1" onclick="showvote()" <%=ischecked%>/>
                            投票
                        </td>
                        <td width="95%" valign="middle">
                            <div id="divVote" style="display:<%=display%>"/>
                            截止日期
                            <input id="expire_date" name="expire_date"/>
                            最多可选
                            <input name="max_choice" size=1 value="1"/>
                            项<br>
                            <textarea <%=isreadonly%> cols="60" name="vote" rows="8" wrap="VIRTUAL" title="输入投票选项" type="_moz">
                            </textarea>
                            <script>
                                addform.vote.value = vp;
                            </script>
                            <br>
                            每行代表一个选项(编辑文档时选项不可更改)
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" valign="top">
                            <div style="clear:both">
<textarea id="htmlcode" name="htmlcode"><%
    if (template != null) {
        out.print(template.getContent(1));
    } else if (!op.equals("add")) {
        out.print(doc.getDocContent(1).getContent());
    }
%></textarea>
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
                    <%} %>
                    <tr>
                        <td style="width:5%" align="left" valign="top">附件：
                            <%
                                Calendar cal = Calendar.getInstance();
                                String year = "" + (cal.get(cal.YEAR));
                                String month = "" + (cal.get(cal.MONTH) + 1);
                                String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
                            %>
                            <input type="hidden" name="filepath" value="<%=filepath%>"/></td>
                        <td>
                            <input type="file" name="attachment0"/>
                            <input class="mybtn1" type=button onclick="AddAttach()" value="增加"/>
                            <div id="updiv"></div>
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
                                        <input id="attachName<%=am.getId()%>" name="attachName"
                                               value="<%=am.getName()%>" size="30">
                                        <a href="javascript:;"
                                           onclick="changeAttachName('<%=am.getId()%>', '<%=doc.getID()%>', '<%="attachName"+am.getId()%>')">重命名</a>&nbsp;&nbsp;
                                        <a href="javascript:;"
                                           onclick="delAttach(<%=am.getId()%>)">删除</a>&nbsp;&nbsp;
                                        <a target=_blank
                                           href="fileark/getfile.jsp?docId=<%=doc.getID()%>&attachId=<%=am.getId()%>">下载</a>&nbsp;&nbsp;
                                        <%if (StrUtil.getFileExt(am.getDiskName()).equals("doc") || StrUtil.getFileExt(am.getDiskName()).equals("docx") || StrUtil.getFileExt(am.getDiskName()).equals("xls") || StrUtil.getFileExt(am.getDiskName()).equals("xlsx")) {%>
                                        <a href="javascript:;" onClick="editdoc(<%=doc.getID()%>, <%=am.getId()%>)">编辑</a>&nbsp;&nbsp;
                                        <%}%>
                                        <a href="javascript:;" onclick="move('up', <%=am.getId()%>)"><img
                                                src="images/arrow_up.gif" alt="往上" width="16" height="20" border="0"
                                                align="absmiddle"></a>&nbsp;
                                        <a
                                            href="javascript:;" onclick="move('down', <%=am.getId()%>)"><img
                                            src="images/arrow_down.gif" alt="往下" width="16" height="20" border="0"
                                            align="absmiddle"></a>
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
                                if (op.equals("add"))
                                    action = "确 定";
                                else
                                    action = "保 存";
                            %>
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
                            %>
                            &nbsp;<input name="remsg" type="button" class="mybtn"
                                         onClick='location.href="fileark/document_list_m.jsp?dir_code=<%=dir_code %>&<%=prjUrl %>"'
                                         value=" 返 回 ">
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
                            &nbsp;<input name="remsg" type="button" class="mybtn"
                                         onClick='addTab("<%=doc.getTitle()%>", "<%=viewPage%>?id=<%=id%>")'
                                         value=" 预 览 ">
                            <%}%>
                        </td>
                    </tr>
                </table>
            </form>
        </TD>
    </TR>
</TABLE>
</body>
<script>
    $(function () {
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
    })

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

    function delAttach(attach_id) {
        jConfirm('您确定要删除么？', '提示', function(r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "fileark/delAttach.do",
                    contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        attachId: parseInt(attach_id)
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                        $('body').showLoading();
                    },
                    success: function(data, status){
                        data = $.parseJSON(data);
                        if (data.ret==1) {
                            $('#row' + attach_id).remove();
                        }
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
    }

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
            if (!LiveValidation.massValidate(title.formObj.fields)) {
                jAlert("请检查表单中的内容填写是否正常！","提示");
                return;
            }
            $('#btnOK').attr("disabled", true);
            $('#addform').submit();
        });
    })

    // ajaxForm序列化提交数据之前的回调函数
    function onBeforeSerialize() {
    }

    $(function() {
        var options = {
            beforeSerialize:  onBeforeSerialize,
            //target:        '#output2',   // target element(s) to be updated with server response
            beforeSubmit:  preSubmit,  // pre-submit callback
            success:       showResponse,  // post-submit callback
            dataType:  	'text'   // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析

            // other available options:
            //url:       url         // override for form's 'action' attribute
            //type:      type        // 'get' or 'post', override for form's 'method' attribute
            //clearForm: true        // clear all form fields after successful submit
            //resetForm: true        // reset the form after successful submit

            // $.ajax options can be used here too, for example:
            //timeout:   3000
        };

        var lastSubmitTime = new Date().getTime();
        $('#addform').submit(function() {
            // 通过判断时间，禁多次重复提交
            var curSubmitTime = new Date().getTime();
            // 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
            if (curSubmitTime - lastSubmitTime < 500) {
                lastSubmitTime = curSubmitTime;
                $('body').hideLoading();
                return false;
            }
            else {
                lastSubmitTime = curSubmitTime;
            }

            $(this).ajaxSubmit(options);
            return false;
        });
    });

    function preSubmit() {
        $('body').showLoading();
    }

    function showResponse(responseText, statusText, xhr, $form) {
        $('body').hideLoading();
        var data = $.parseJSON($.trim(responseText));
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
                    jAlert_Redirect(data.msg, "提示", "fileark/document_list_m.jsp?dir_code=<%=dir_code%>");
                }
            }
            else {
                jAlert(data.msg, "提示", function() {
                    // 可能会上传新的附件，所以需reload才能看到
                    window.location.reload();
                });
            }
        }
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
</script>
</html>