<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudweb.oa.service.IOaNoticeService" %>
<%@ page import="com.cloudweb.oa.entity.OaNotice" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%
    /**
     * 注意因为引入了prototype.js，所以$不能用了，得用jQuery，所有include进来的内容也需要把$改为jQuery，如desktop_clock.jsp
     */
    String mode = ParamUtil.get(request, "mode");
    Config cfg = Config.getInstance();
    if (!"force".equals(mode)) {
        if (2 == cfg.getInt("styleDesktop")) {
            request.getRequestDispatcher("desktop_lte.jsp").forward(request, response);
            return;
        }
    }
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
    String op = ParamUtil.get(request, "op");
    if ("close".equals(op)) {
        response.setContentType("text/html;charset=utf-8");
        int id = ParamUtil.getInt(request, "id");
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);

        JSONObject json = new JSONObject();
        UserDesktopSetupDb udsdSys = new UserDesktopSetupDb();
        udsdSys = udsdSys.getUserDesktopSetupDb(udid.getSystemId());
        if (!udsdSys.isCanDelete()) {
            json.put("ret", "0");
            json.put("msg", "该项不允许被删除！");
            out.print(json);
            return;
        }

        boolean re = udid.del();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    } else if ("mod".equals(op)) {
        response.setContentType("text/html;charset=utf-8");
        int id = ParamUtil.getInt(request, "id");
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        int count = ParamUtil.getInt(request, "count", -1);
        int wordCount = ParamUtil.getInt(request, "wordCount", -1);
        JSONObject json = new JSONObject();
        if (count == -1 || wordCount == -1) {
            json.put("ret", "0");
            json.put("msg", "请输入数字！");
            out.print(json);
            return;
        }
        udid.setCount(count);
        udid.setWordCount(wordCount);
        boolean re = udid.save();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    }

    String skincode = UserSet.getSkin(request);
    if (skincode == null || "".equals(skincode)) {
        skincode = UserSet.defaultSkin;
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
    String userName = privilege.getUser(request);

    PortalDb pd = new PortalDb();
    long portalId = ParamUtil.getLong(request, "portalId", -1);
    if (portalId == -1) {
        pd = pd.getDefaultPortalOfUser(userName);
    } else {
        pd = (PortalDb) pd.getQObjectDb(portalId);
    }
    if (pd == null) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
        response.setContentType("text/html;charset=utf-8");
%>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查看失败，请检查门户的权限设置，也可能是门户尚未初始化或加载失败！"));
        return;
    }

    UserDesktopSetupDb udid = new UserDesktopSetupDb();
    String sql = udid.getSqlByPortalId(pd.getLong("id"));
    Vector items = udid.list(sql);
    Iterator iItems = items.iterator();
    Long time = Long.parseLong(cfg.get("autoRefresh"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>桌面</title>
    <link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
    <link href="lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <link href="lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="<%=skinPath%>/main.css" rel="stylesheet" type="text/css"/>
    <style>
        .btnIcon {
            cursor: pointer;
            margin: 3px -3px 0 0;
        }

        .btnSpan {
            float: right;
        }

        .unreaded {
            font-weight: bold;
        }

        .advbox {
            position: absolute;
            top: expression(eval(document.documentElement.scrollTop));
        }
        .advbox {
            width: 650px;
            position: fixed;
            display: none;
            left: 50%;
            top: 0;
            margin: -160px 0 0 -325px;
        }
        .advbox .advpic {
            position: relative;
            height: 360px;
            overflow: hidden;
            font-size: 14px;
        }
        .advbox .advpic .closebtn {
            display: block;
            width: 33px;
            height: 22px;
            line-height: 22px;
            font-size: 12px;
            color: #fff;
            text-indent: 12px;
            overflow: hidden;
            position: absolute;
            right: 2px;
            top: 5px;
            z-index: 99;
        }
        .bg {
            display: none;
            position: fixed;
            width: 100%;
            height: 100%;
            background: #333;
            z-index: 2;
            top: 0;
            left: 0;
            opacity: 0.2;
            -moz-opacity: 0.2;
            filter: alpha(opacity=20);
            -ms-filter: "progid:DXImageTransform.Microsoft.Alpha(Opacity=20)";
            filter: progid:DXImageTransform.Microsoft.Alpha(Opacity=20);
        }
        .flexslider {
            width: 640px;
            height: 280px;
            margin-top: 30px;
        }
        h5 {
            font-size: 22px;
            font-family: Microsoft YaHei;
            margin-top: 20px;
            margin-bottom: 0px
        }
        .notice-title-box {
            text-align: center;
            font-size: 22px;
            margin-top: 20px;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #ccc;
        }
        .notice-content {
            font-size: 14px;
            height: 210px;
            margin: 10px;
            font-family: Microsoft YaHei;
            line-height: 1.5;
            OVERFLOW-Y: auto;
            OVERFLOW-X: hidden;
        }

        .no_content {
            display: none;
        }
        .t-left {
            float: left;
            width: 68%;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis; /*Not working in FF*/
        }

        .t-right {
            float: right;
        }
    </style>

    <script type="text/javascript" src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src="lte/js/bootstrap.min.js?v=3.3.6"></script>
    <script type="text/javascript" src="js/prototype.js"></script>
    <script type="text/javascript" src="js/drag.js"></script>
    <script type="text/javascript" src="js/google_drag_2.js"></script>
    <script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <link type="text/css" rel="stylesheet" href="js/flexslider/flexslider.css"/>
    <script type="text/javascript" src="js/flexslider/jquery.flexslider.js"></script>
    <script type="text/javascript" src="inc/ajax_getpage.jsp"></script>
    <script>
        function getPosition(id) {
            var obj = o(id);
            var ids = "";
            for (var i = 0; i < obj.childNodes.length; i++) {
                if (obj.childNodes[i].className == "portlet") {
                    if (ids == "")
                        ids = obj.childNodes[i].id;
                    else
                        ids += "," + obj.childNodes[i].id;
                }
            }
            return ids;
        }

        function doOnSaveDesktop(response) {
        }

        function errFunc() {
        }

        function saveDesktop() {
            // 如果是chrome，则退出，以免弹出"The page at ... says:"
            /*if (getOS()==3)
                return;*/

            var col;
            var ids;

            col = DragUtil.getSortIndexOfCol('col_1').replace(/drag_/g, "");
            ids = col;
            ids += "|";
            col = DragUtil.getSortIndexOfCol('col_2').replace(/drag_/g, "");
            ids += col;
            ids += "|";
            col = DragUtil.getSortIndexOfCol('col_0').replace(/drag_/g, "");
            ids += col;

            var str = "op=save&ids=" + ids;
            var myAjax = new cwAjax.Request(
                "bottom.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doOnSaveDesktop,
                    onError: errFunc
                }
            );
        }

        function removeScript(html) {
            // 清除html中的script
            var SCRIPT_REGEX = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
            while (SCRIPT_REGEX.test(html)) {
                html = html.replace(SCRIPT_REGEX, "");
            }
            return html;
        }

        jQuery(function() {
            // 绑定桌面文件柜组件的选项卡事件
            // on需要绑定父级元素（此元素必须为静态元素，不是后来动态生成的），然后设定on()方法的selector参数才行
            jQuery("#columns").on("hover", ".drag_div .nav-tab li", function(e) {
                jQuery(this).addClass("act").siblings().removeClass('act');
                var $dragDiv = jQuery(this).parent().parent();
                $dragDiv.find(".con").hide().eq(jQuery(this).index()).show();
            });

            jQuery("#columns").on("click", ".drag_div .nav-tab li", function(e) {
                var $li = jQuery(this);
                addTab($li.text(), '<%=request.getContextPath()%>/fileark/document_list_m.jsp?dir_code=' + $li.attr('code'));
            });

	    /*
            // 轮播图片初始化
            jQuery("div[id^='flexslider']").flexslider({
                animation: "slide",
                controlNav: true,
                slideshow: true,
                directionNav: true,
                pauseOnAction: false,
                // pauseOnHover: true,
                slideshowSpeed: 5000,
                start: function (slider) {
                }
            });
	    */
        });

        function loadDesktopUnit(url, divId, containDivId) {
            // 创建临时div，保存传入页面
            var tempDivId = "tmp_" + containDivId;
            jQuery("#" + divId).append("<div id='" + tempDivId + "' style='display:none'></div>");
            // ajaxpage(url, tempDivId);
            // console.log(url);
            jQuery.ajax({
                async: false,
                type: "get",
                url: url,
                contentType : "application/x-www-form-urlencoded; charset=iso8859-1",
                data: {
                },
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                },
                success: function(data, status) {
                    jQuery("#" + tempDivId).html(data);
                },
                complete: function(XMLHttpRequest, status){
                },
                error: function(XMLHttpRequest, textStatus){
                    // 请求出错处理
                    console.log(XMLHttpRequest.responseText);
                }
            });
            // 定时获取临时div内容，若是临时div已经把对应页面内容加载进来，则把临时div内容after到临时div后，然后删除临时div
            var sint = window.setInterval(function () {
                // 判断临时DIV是否存在
                if (jQuery("#" + tempDivId).length > 0) {
                    var content = jQuery("#" + tempDivId).html();
                    if (content != null && content.indexOf(containDivId) != -1) {
                        /*
                        // 去除content中的script，否则会使其中的script在页面上载入两次，导致事件会出现两次，无效，事件将无响应，可能因为dom关系不对，因为事件对应的临时div已被删除
                        content = removeScript(content);
                        try {
                            // 当载入本年度业绩的时候会出现异常，所以加入了try catch
                            jQuery("#" + tempDivId).after(content);
                        } catch (e) {
                        }
                        jQuery("#" + tempDivId).remove();
                        */

                        jQuery("#" + tempDivId).children().unwrap();

                        window.clearInterval(sint);

                        // 轮播图片初始化
                        if (url.indexOf("flash_image_desktop")!=-1) {
                            // containDivId的格式为drag_id
                            var divId = containDivId.substring("drag_".length);

                            jQuery("#flexslider" + divId).flexslider({
                                animation: "slide",
                                controlNav: true,
                                slideshow: true,
                                directionNav: true,
                                pauseOnAction: false,
                                // pauseOnHover: true,
                                slideshowSpeed: 5000,
                                start: function (slider) {
                                }
                            });
                        }
                    }
                }
            }, 20);
        }

        function initColsTable() {
            setInterval(function () {
                window.location.reload();
            }, <%=time%>);
            // 15为两个侧边的margin的总和
            columns.style.width = (document.documentElement.clientWidth - 15) + "px";
            if (isIE6) {
                // columns.style.width = (document.documentElement.clientWidth-16) + "px";

                // only apply to IE
                if (!/*@cc_on!@*/0) return;
                var all = document.getElementsByTagName('*'), i = all.length;
                while (i--) {
                    // if the scrollWidth (the real width) is greater than
                    // the visible width, then apply style changes
                    if (all[i].scrollWidth > all[i].offsetWidth) {
                        all[i].style['paddingBottom'] = '20px';
                        all[i].style['overflowY'] = 'auto';
                    }
                    if (all[i].scrollHeight > all[i].offsetHeight) {
                        all[i].style['paddingBottom'] = '0px';
                        all[i].style['overflowX'] = 'hidden';
                        all[i].style['overflowY'] = 'auto';
                    }
                }
            }

            /*
            var w = 400;
            tdSideBar.style.width = w + "px";
            tdLeft.style.width = Math.floor((columns.clientWidth - w)/2) + "px";
            tdRight.style.width = tdLeft.style.width;
            */
            var w = Math.floor(columns.clientWidth / 3);
            tdSideBar.style.width = w + "px";
            tdLeft.style.width = w + "px";
            tdRight.style.width = tdLeft.style.width;
        }
    </script>
</head>
<body onunload="saveDesktop()" onresize="initColsTable()" style="background-color: #e0e5e7;">
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="columns" style="width: 100%; table-layout: fixed; margin-left:10px; margin-right:5px; margin-top:5px;">
    <tr>
        <td valign="top" id="tdLeft" style="width:33%;height:300px;">
            <div id="col_1" class="col_div">
                <%
                    DesktopMgr dm = new DesktopMgr();
                    String moduleContent = "";
                    while (iItems.hasNext()) {
                        udid = (UserDesktopSetupDb) iItems.next();
                        if (udid.getTd() == UserDesktopSetupDb.TD_LEFT) {
                            DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
                            if (du == null) {
                                out.print("模块1:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            IDesktopUnit idu = du.getIDesktopUnit();
                            if (idu == null) {
                                out.print("模块2:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit) {
                                request.setAttribute("parentId", "col_1");
                                out.print(idu.display(request, udid));
                            } else {
                                moduleContent = idu.display(request, udid);
                %>
                <div id="drag_<%=udid.getId()%>" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>" count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>" class="portlet drag_div bor" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="box">
                        <!-- <span class="titletxt"><img src="<%=skinPath%>/images/titletype.png" width="8" height="12" /><a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>" class=""><%=udid.getTitle()%></a></span>  -->
                        <!--	<div class="opbut-1"><img onclick="mini('<%=udid.getId()%>')" title="最小化" class="btnIcon" src="<%=skinPath%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>  -->
                        <!-- <div class="opbut-2"><img onclick="mod('<%=udid.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=skinPath%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>  -->
                        <!-- <div class="opbut-3"><img onclick="clo('<%=udid.getId()%>')" title="关闭" class="btnIcon" src="<%=skinPath%>/images/close.png" align="absmiddle" width="19" height="19"/></div>  -->
                        <div class="titleimg">
                            <!--<img src="images/desktop/<%=udid.getModuleCode().trim()%>.png" width="40" height="40" />-->
                            <i class="fa <%=udid.getIcon()%>"></i>
                            &nbsp;&nbsp;
                        </div>
                        <div class="titletxt">&nbsp;&nbsp;<a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>" class=""><%=udid.getTitle()%>
                        </a></div>
                    </div>
                    <div class="article" id="drag_<%=udid.getId()%>_c">
                        <%
                            out.print(moduleContent);
                        %>
                    </div>
                </div>
                <% }
                }
                }
                %>
            </div>

        </td>
        <td valign="top" id="tdRight" style="width:33%">
            <div id="col_2" class="col_div">
                <%
                    iItems = items.iterator();
                    while (iItems.hasNext()) {
                        udid = (UserDesktopSetupDb) iItems.next();
                        if (udid.getTd() == UserDesktopSetupDb.TD_RIGHT) {
                            DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
                            if (du == null) {
                                out.print("模块3:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            IDesktopUnit idu = du.getIDesktopUnit();
                            if (idu == null) {
                                out.print("模块4:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit) {
                                request.setAttribute("parentId", "col_2");
                                out.print(idu.display(request, udid));
                            } else {
                                moduleContent = du.getIDesktopUnit().display(request, udid);
                %>
                <div id="drag_<%=udid.getId()%>" class="portlet drag_div bor" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>" count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="box">
                        <!-- <span class="titletxt"><img src="<%=skinPath%>/images/titletype.png" width="8" height="12" /><a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%></a></span>-->
                        <!-- <div class="opbut-1"><img onclick="mini('<%=udid.getId()%>')" title="最小化" class="btnIcon" src="<%=skinPath%>/images/minimization.png" align="absmiddle" /></div>-->
                        <!-- <div class="opbut-2"><img onclick="mod('<%=udid.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=skinPath%>/images/configure.png" align="absmiddle" /></div>-->
                        <!-- <div class="opbut-3"><img onclick="clo('<%=udid.getId()%>')" title="关闭" class="btnIcon" src="<%=skinPath%>/images/close.png" align="absmiddle" /></div>-->
                        <div class="titleimg">
                            <!--<img src="images/desktop/<%=udid.getModuleCode().trim()%>.png" width="40" height="40" />-->
                            <i class="fa <%=udid.getIcon()%>"></i>
                            &nbsp;&nbsp;
                        </div>
                        <div class="titletxt">&nbsp;&nbsp;<a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%>
                        </a></div>
                    </div>
                    <div class="article" id="drag_<%=udid.getId()%>_c">
                        <%=moduleContent%>
                    </div>
                </div>
                <% }
                }
                }
                %>
            </div>
        </td>
        <td valign="top" id="tdSideBar" class="right" width="120px">
            <div id="col_0" class="col_div">
                <%
                    iItems = items.iterator();
                    while (iItems.hasNext()) {
                        udid = (UserDesktopSetupDb) iItems.next();
                        if (udid.getTd() == UserDesktopSetupDb.TD_SIDEBAR) {
                            DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
                            if (du == null) {
                                out.print("模块5:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            IDesktopUnit idu = du.getIDesktopUnit();
                            if (idu == null) {
                                out.print("模块6:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit) {
                                request.setAttribute("parentId", "col_0");
                                out.print(idu.display(request, udid));
                            } else {
                                moduleContent = du.getIDesktopUnit().display(request, udid);
                %>
                <div id="drag_<%=udid.getId()%>" class="portlet drag_div bor" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>" count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="box">
                        <!-- <span class="titletxt"><img src="<%=skinPath%>/images/titletype.png" width="8" height="12" /><a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%></a></span>-->
                        <!-- <div class="opbut-1"><img onclick="mini('<%=udid.getId()%>')" title="最小化" class="btnIcon" src="<%=skinPath%>/images/minimization.png" align="absmiddle" /></div>-->
                        <!-- <div class="opbut-2"><img onclick="mod('<%=udid.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=skinPath%>/images/configure.png" align="absmiddle" /></div>-->
                        <!-- <div class="opbut-3"><img onclick="clo('<%=udid.getId()%>')" title="关闭" class="btnIcon" src="<%=skinPath%>/images/close.png" align="absmiddle" /></div>-->
                        <div class="titleimg">
                            <!--<img src="images/desktop/<%=udid.getModuleCode().trim()%>.png" width="40" height="40" />-->
                            <i class="fa <%=udid.getIcon()%>"></i>
                            &nbsp;&nbsp;
                        </div>
                        <div class="titletxt">&nbsp;&nbsp;<a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%>
                        </a></div>
                    </div>
                    <div class="article" id="drag_<%=udid.getId()%>_c">
                        <%=moduleContent%>
                    </div>
                </div>
                <% }
                }
                }
                %>
            </div>
        </td>
    </tr>
</table>

<div class="bg"></div>

<div id="dlg" style="display:none">
    <span id="spanRowCount">行数</span><span id="spanVerticalCount">字数或高度</span>&nbsp;&nbsp;<input id="count" name="count" size="10"/><br/>
    每行字符数&nbsp;&nbsp;<input id="wordCount" name="wordCount" size="10"/>
</div>
<%
    IOaNoticeService oaNoticeService = SpringUtil.getBean(IOaNoticeService.class);
    List<OaNotice> list = oaNoticeService.listImportant(userName);
    int totalUnknown = 0;
    if (list.size() > 0) {
%>
<!--滑动通知-->
<div class="advbox" style="z-index:3">
    <div class="advpic">
        <a href="javascript:void(0);" class="closebtn" title="关闭"><img src="images/close.png" style="border:0"/></a>
        <div>
            <section class="slider">
                <div class="flexslider">
                    <ul class="slides">
                        <%
                            for (OaNotice oaNotice : list) {
                                if (oaNoticeService.isUserReaded(oaNotice.getId(), userName)) {
                                    continue;
                                }
                                if (totalUnknown++ >= 5) {
                                    break;
                                }
                        %>
                        <li id='notice_<%=oaNotice.getId() %>'>
                            <div class="notice-title-box">
                                <a id="notice_<%=oaNotice.getId() %>" class="notice-title" href="notice/show.do?id=<%=oaNotice.getId()%>&isShow=<%=oaNotice.getIsShow()%>">
                                    <h5><%=oaNotice.getTitle()%></h5>
                                </a>
                            </div>
                            <div class="notice-content">
                                <%=oaNotice.getContent()%>
                            </div>
                        </li>
                        <%
                            }
                        %>
                    </ul>
                </div>
            </section>
        </div>
        <%if (totalUnknown > 0) {%>
        <script>
            jQuery(window).load(function () {
                jQuery('.flexslider').flexslider({
                    animation: "slide",
                    start: function (slider) {
                    }
                });
            });

            jQuery(document).ready(function () {
                jQuery('.bg').fadeIn(200);

                jQuery(".advbox").show();
                jQuery(".advbox").animate({top: "50%"}, 1000);
                jQuery(".closebtn").click(function () {
                    jQuery(".advbox").fadeOut(500);
                    jQuery('.bg').fadeOut(800);
                })

                jQuery(".notice-title").click(function () {
                    var slider = jQuery('.flexslider').data('flexslider');
                    var index = slider.currentSlide;
                    slider.removeSlide(index);
                    if (index == slider.pagingCount) {
                        jQuery(".advbox").fadeOut(500);
                        jQuery('.bg').fadeOut(800);
                    }
                })
            })
        </script>
        <%}%>
    </div>
</div>
<%}%>

<div style="text-align:center;clear:both">
</div>

</body>
<script>
    function mini(id) {
        var objId = "#drag_" + id + "_c";
        if (jQuery(objId).css("display") == "none")
            jQuery(objId).show();
        else
            jQuery(objId).hide();
        return false;
    }

    function mod(id) {
        jQuery("#count").val(jQuery("#drag_" + id).attr("count"));
        jQuery("#wordCount").val(jQuery("#drag_" + id).attr("wordCount"));

        if (jQuery("#drag_" + id).attr("type") == "<%=DesktopUnit.TYPE_LIST%>") {
            jQuery("#spanRowCount").show();
            jQuery("#spanVerticalCount").hide();
        } else {
            jQuery("#spanRowCount").hide();
            jQuery("#spanVerticalCount").show();
        }

        var dragTitle = jQuery("#drag_" + id).attr("dragTitle") + "显示方式";

        jQuery("#dlg").dialog({
            title: dragTitle,
            modal: true,
            // bgiframe:true,
            buttons: {
                "取消": function () {
                    jQuery(this).dialog("close");
                },
                "确定": function () {
                    jQuery.ajax({
                        type: "post",
                        url: "desktop.jsp",
                        data: {
                            op: "mod",
                            id: id,
                            count: jQuery("#count").val(),
                            wordCount: jQuery("#wordCount").val()
                        },
                        dataType: "json",
                        beforeSend: function (XMLHttpRequest) {
                            //ShowLoading();
                        },
                        success: function (data, status) {
                            window.location.href = "desktop.jsp?portalId=<%=portalId%>";
                        },
                        complete: function (XMLHttpRequest, status) {
                            // HideLoading();
                        },
                        error: function (XMLHttpRequest, textStatus) {
                            // 请求出错处理
                            //alert(XMLHttpRequest.responseText);
                        }
                    });

                    jQuery(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });

    }

    function clo(id) {
        jConfirm('您确定要关闭么？', '提示', function (r) {
            if (r) {
                jQuery.ajax({
                    type: "post",
                    url: "desktop.jsp",
                    data: {
                        op: "close",
                        id: id
                    },
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        if (data.ret == "0") {
                            jAlert(data.msg, "提示");
                        } else {
                            jAlert_Redirect(data.msg, "提示", "desktop.jsp");
                        }
                    },
                    complete: function (XMLHttpRequest, status) {
                        // HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    DragUtil.onAfterDrag = function () {
        saveDesktop();
    };

    initColsTable();

    window.onload = function () {
        setTimeout(function () {
            initDrag();
        }, 2000);

        jQuery(document).click(function (e) {
            onClickDoc(e);
            return false;
        });

        //获取光标
        //document.body.focus();
        //禁止退格键 作用于Firefox、Opera
        document.onkeypress = banBackSpace;
        //禁止退格键 作用于IE、Chrome
        document.onkeydown = banBackSpace;
    }

    function onClickDoc(e) {
        var obj = isIE() ? event.srcElement : e.target;
        if (isIE() && event.shiftKey) {
            if (obj.tagName == "A") {
                window.open(obj.getAttribute("href"));
                return false;
            }
        }

        if (obj.parentNode) {
            // alert(obj.parentNode.getAttribute("href"));
            if (obj.parentNode.tagName == "A") {
                var href = obj.parentNode.getAttribute("href");
                if (href != null && href.indexOf("javascript") != 0) {
                    var inText = isIE() ? obj.parentNode.innerText : obj.parentNode.textContent;
                    addTab(inText, href);
                    return false;
                }
            }
            if (obj.parentNode.parentNode) {
                if (obj.parentNode.parentNode.tagName == "A") {
                    var inText = isIE() ? obj.parentNode.parentNode.innerText : obj.parentNode.parentNode.textContent;
                    addTab(inText, obj.parentNode.parentNode.getAttribute("href"));
                    return false;
                }
            }

        }
        if (obj.tagName == "A") {
            var href = obj.getAttribute("href");

            var cls = "";
            if (isIE7) {
                cls = obj.getAttribute("className");
            } else {
                cls = obj.getAttribute("class");
            }
            //var cls = obj.getAttribute("className");
            // 跳过flexslider的左右滑动按钮
            if (cls != null && cls.indexOf("flex") == 0) {
                return false;
            }
            if (href != null && href.indexOf("javascript") != 0) {
                var inText = isIE() ? obj.innerText : obj.textContent;
                addTab(inText, href);
                return false;
            }
        }
    }

    //解决页面按了退格键，回到登陆页bug
    function banBackSpace(e) {
        var ev = e || window.event;//获取event对象
        var obj = ev.target || ev.srcElement;//获取事件源
        var t = obj.type || obj.getAttribute('type');//获取事件源类型
        //获取作为判断条件的事件类型
        var vReadOnly = obj.readOnly;
        var vDisabled = obj.disabled;
        //处理undefined值情况
        vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;
        vDisabled = (vDisabled == undefined) ? true : vDisabled;
        //当敲Backspace键时，事件源类型为密码或单行、多行文本的，
        //并且readOnly属性为true或disabled属性为true的，则退格键失效
        var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea") && (vReadOnly == true || vDisabled == true);
        //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
        var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea";
        //判断
        if (flag2 || flag1) return false;
    }
</script>
</html>