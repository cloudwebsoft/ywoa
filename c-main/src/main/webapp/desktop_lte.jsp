<%@ page contentType="text/html; charset=utf-8" language="java" import="cn.js.fan.util.ParamUtil" errorPage="" %>
<%@ page import = "cn.js.fan.util.StrUtil" %>
<%@ page import = "com.cloudweb.oa.api.IDesktopCard"%>
<%@ page import = "com.cloudweb.oa.entity.OaNotice"%>
<%@ page import = "com.cloudweb.oa.module.desktop.DesktopCard"%>
<%@ page import = "com.cloudweb.oa.module.desktop.DesktopCardFactory"%>
<%@ page import = "com.cloudweb.oa.module.desktop.DesktopCardUtil"%>
<%@ page import = "com.cloudweb.oa.service.IOaNoticeService"%>
<%@ page import = "com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserDesktopSetupDb" %>
<%@ page import="com.redmoon.oa.person.UserSet" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Vector" %>
<%
    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
    String skincode = UserSet.getSkin(request);
    if (skincode==null || "".equals(skincode)) {
        skincode = UserSet.defaultSkin;
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
    String userName = privilege.getUser(request);
    
    PortalDb pd = new PortalDb();
    long portalId = ParamUtil.getLong(request, "portalId", -1);
    if (portalId==-1) {
        pd = pd.getDefaultPortalOfUser(userName);
    } else {
        pd = (PortalDb)pd.getQObjectDb(portalId);
    }
    if (pd==null) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
        response.setContentType("text/html;charset=utf-8");
%>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<%
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查看失败，请检查门户的权限设置，也可能是门户尚未初始化或加载失败！"));
        return;
    }
    
    UserDesktopSetupDb udid = new UserDesktopSetupDb();
    String sql = udid.getSqlByPortalId(pd.getLong("id"));
    Vector items = udid.list(sql);
    Iterator iItems = items.iterator();
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    Long time = Long.parseLong(cfg.get("autoRefresh"));

    UserDb user = new UserDb();
    user = user.getUserDb(userName);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>桌面</title>
    <link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
    <link href="lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="lte/css/animate.css" rel="stylesheet">
    <link href="lte/css/style.css?v=4.1.0" rel="stylesheet">
    <link href="lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <style>
        .col-sm-4 {
            padding-left: 10px;
            padding-right: 5px;
        }

        h5 i{
            margin-right: 10px;
        }

        .gray-bg {
            background-color: #f3f3f4;
        }

        #sortable {
            list-style-type: none;
            padding: 0;
            margin: 0 auto;
            height: 140px;
        }

        #sortable li {
            margin: 0 3px 3px 0;
            padding: 1px;
            float: left;
            width: 120px;
            height: 120px;
            font-size: 10pt;
            text-align: center;
            color: black;
            font-weight: normal;
        }
        
        .ibox-content {
            line-height: 2.5;
            min-height: 245px;
        }

        .article_table {
            width: 100%;
            table-layout:fixed;
        }
        .article_content {
            width: 70%;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
        }
        .article_time {
            width: 30%;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
            text-align: right;
        }
        .portlet {
            width: 100%;
            border: 0;
        }

        .t-left {
            float: left;
            width: 69%;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis; /*Not working in FF*/
        }

        .t-right {
            float: right;
        }

        .titleimg {
            float: left;
        }

        .titletxt {
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
        ul {
            list-style: none;
        }
        .unreaded {
            font-weight: bold;
        }
    </style>
    <script type="text/javascript" src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src="lte/js/bootstrap.min.js?v=3.3.6"></script>
    <script src="lte/js/plugins/layer/layer.min.js"></script>
    <script src="js/echarts/echarts.js"></script>
    <link type="text/css" rel="stylesheet" href="js/flexslider/flexslider.css" />
    <script type="text/javascript" src="js/flexslider/jquery.flexslider.js"></script>
    <script src="inc/ajax_getpage.jsp"></script>
    <script>
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
                addTab($li.text(), '<%=request.getContextPath()%>/fileark/docListPage.do?dirCode=' + $li.attr('code'));
            });
        });

        function loadDesktopUnit(url, divId, containDivId) {
            // 创建临时div，保存传入页面
            var tempDivId = "tmp_" + containDivId;
            jQuery("#" + divId).append("<div id='" + tempDivId + "' style='display:none'></div>");
            ajaxpage(url, tempDivId);
            // 定时获取临时div内容，若是临时div已经把对应页面内容加载进来，则把临时div内容after到临时div后，然后删除临时div
            var sint = window.setInterval(function () {
                // 判断临时DIV是否存在
                if (jQuery("#" + tempDivId).length > 0) {
                    var content = jQuery("#" + tempDivId).html();
                    if (content != null && content.indexOf(containDivId) != -1) {
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
    </script>
    <link rel="stylesheet" href="js/layui/css/layui.css" media="all">
    <script src="js/layui/layui.js" charset="utf-8"></script>
    <script src="js/numscroll/numscroll.js" charset="utf-8"></script>
    <style>
        .layui-panel {
            border-radius: 6px;
            /*box-shadow: 0 1px 2px 0 rgb(0 0 0 / 20%);*/
            transition: all 0.2s ease-in-out;
        }
        .panel-1 {
            background-color: #578ebe;
            border-radius: 6px;
        }
        .panel-2 {
            background-color: #e35b5a;
            border-radius: 6px;
        }
        .panel-3 {
            background-color: #44b6ae;
            border-radius: 6px;
        }
        .panel-4 {
            background-color: #8775a7;
            border-radius: 6px;
        }
        .panel-5 {
            background-color: #4f5c65;
            border-radius: 6px;
        }
        .panel-6 {
            background-color: #14aae4;
            border-radius: 6px;
        }
        .panel-7 {
            background-color: #949FB1;
            border-radius: 6px;
        }
        .panel-8 {
            background-color: #f29503;
        }

        .panel-9 {
            background-color: #ff934b;
        }
        .panel-10 {
            background-color: #28c0c6;
        }
        .panel-11 {
            background-color: #5bb705;
        }
        .panel-12 {
            background-color: #ac6ae4;
        }

        .panel-item {
            position: relative;
            overflow: hidden;
            color: #fff;
            cursor: pointer;
            height: 85px;
            margin-right: 10px;
            margin-bottom: 10px;
            padding-left: 15px;
            padding-top: 20px;
        }

        .panel-glow {
            -moz-box-shadow: 0 0 5px #007ca6;
            -webkit-box-shadow: 0 0 5px #007ca6;
            box-shadow: 0px 0px 5px #007ca6;
        }

        .panel-item .m-top-none {
            margin-top: 5px;
        }

        .panel-item h2 {
            font-size: 28px;
            font-family: inherit;
            line-height: 1.1;
            font-weight: 500;
            padding-left: 70px;
        }

        .panel-item h2 .num-scroll {
            font-size: 28px;
            padding-left: 5px;
        }

        .panel-item h2 span {
            font-size: 12px;
            padding-left: 5px;
        }

        .panel-item h5 {
            font-size: 14px;
            font-family: inherit;
            margin-top: 1px;
            line-height: 1.1;
            padding-left: 70px;
        }
        
        .panel-item .stat-icon {
            position: absolute;
            top: 18px;
            font-size: 50px;
            opacity: .3;
        }

        .panel i.fa.stats-icon {
            width: 50px;
            padding: 20px;
            font-size: 50px;
            text-align: center;
            color: #fff;
            height: 50px;
            border-radius: 10px;
        }
        .chart-title {
/*            height: 32px;
            text-align: left;
            padding: 10px 0 0 20px;*/
        }
        .chart-title h5 {
            font-weight: bold;
            color: #606060;
        }
        .drag-h {
            display: none;
        }
    </style>
</head>
<body class="gray-bg">
<%
    DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
    List<DesktopCard> cardList = desktopCardUtil.listByPortal(pd.getLong("id"));
    if (cardList.size() > 0) {
        String colCls;
        if (cardList.size() % 6 == 0) {
            colCls = "layui-col-md2";
        }
        else {
            colCls = "layui-col-md3";
        }
%>
<div style="padding: 20px 10px 0 15px;">
    <div class="layui-row layui-col-space15">
        <%
            UserDb userDb = new UserDb();
            userDb = userDb.getUserDb(userName);
            boolean isAdmin = privilege.isUserPrivValid(request, Privilege.ADMIN);
            int cardNo = 0;
            for (DesktopCard desktopCard : cardList) {
                IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
                if (iDesktopCard == null) {
                    out.print(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
                    continue;
                }
                boolean canSeeCard = false;
                String roles = desktopCard.getRoles();
                if (isAdmin) {
                    canSeeCard = true;
                }
                else {
                    if (!StringUtils.isEmpty(roles)) {
                        String[] arr = StrUtil.split(roles, ",");
                        for (String roleCode : arr) {
                            if (userDb.isUserOfRole(roleCode)) {
                                canSeeCard = true;
                                break;
                            }
                        }
                    }
                    else {
                        canSeeCard = true;
                    }
                }
                if (!canSeeCard) {
                    continue;
                }
                String onclick = "";
                String cardStyle = "";
                if (iDesktopCard.isLink()) {
                    onclick = " onclick=\"addTab('" + iDesktopCard.getTitle() + "', '" + request.getContextPath() + "/" + iDesktopCard.getUrl() + "')\" ";
                    cardStyle = "style='cursor:auto'";
                }
        %>
            <div class="<%=colCls%>" <%=onclick%> <%=cardStyle%>>
                <div class="layui-panel" style="background-color: <%=iDesktopCard.getBgColor()%>">
                    <div class="panel-item">
                        <div class="stat-icon">
                            <i class="fa <%=iDesktopCard.getIcon()%>"></i>
                        </div>
                        <h2 class="m-top-none">
                            <%
                                int endVal = iDesktopCard.getEndVal(request);
                                if (endVal >= 0) {
                            %>
                            <span id="panelNum<%=cardNo%>" class="num-scroll" data-startVal="<%=iDesktopCard.getStartVal()%>" data-endVal="<%=endVal%>" data-speed="4" data-decimals="0">0</span>
                            <span><%=iDesktopCard.getUnit()%></span>
                            <%
                                }
                            %>
                        </h2>
                        <h5><%=iDesktopCard.getTitle()%></h5>
                    </div>
                </div>
            </div>
        <%
                cardNo++;
            }
        %>
    </div>
</div>
<script>
    $(function() {
        $('.num-scroll').each(function(){
            var id = $(this).attr('id');
            var decimals = $(this).attr('data-decimals'),
                startVal = $(this).attr('data-startVal'),
                endVal = $(this).attr('data-endVal'),
                duration = $(this).attr('data-speed');
            new CountUp(id, startVal, endVal, decimals, duration, {
                useEasing: true,//效果
                separator: ''//数字分隔符
            }).start();// target：目标元素id, startVal：你想要开始的值, endVal：你想要到达的值, decimals：小数位数，默认值为0, duration：动画持续时间为秒，默认值为2, options：选项的可选对象
            isplay = false;
        });

        $('.layui-row').on('mouseover', '.layui-panel', function(e) {
            $(this).addClass("panel-glow");
        });
        $('.layui-row').on('mouseout', '.layui-panel', function(e) {
            $(this).removeClass("panel-glow");
        });
    });
</script>
<%
    }
%>
    <div class="wrapper wrapper-content" id="columns">
        <div class="row">
            <div class="col-sm-4" id="col_1">
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
                <div id="drag_<%=udid.getId()%>" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>"
                     count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>"
                     class="portlet drag_div ibox float-e-margins" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="ibox-title">
                      <h5>
                                <!--<img src="images/desktop/<%=udid.getModuleCode().trim()%>.png" width="40" height="40" />-->
                                <i class="fa <%=udid.getIcon()%>"></i>
                                <a
                                    href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"
                                    class=""><%=udid.getTitle()%>
                            </a>
                        </h5>
                    </div>
                    <div class="ibox-content" id="drag_<%=udid.getId()%>_c">
                        <%
                            out.print(moduleContent);
                        %>
                    </div>
                </div>
                <%
                            }
                    }
                }
                %>
            </div>
            <div class="col-sm-4" id="col_2">
                <%
                    iItems = items.iterator();
                    while(iItems.hasNext()) {
                        udid = (UserDesktopSetupDb)iItems.next();
                        if(udid.getTd() == UserDesktopSetupDb.TD_RIGHT) {
                            DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
                            if (du==null) {
                                out.print("模块3:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            IDesktopUnit idu = du.getIDesktopUnit();
                            if (idu==null) {
                                out.print("模块4:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit)
                            {
                                request.setAttribute("parentId","col_2");
                                out.print(idu.display(request, udid));
                            }else {
                                moduleContent = du.getIDesktopUnit().display(request, udid);
                %>
                <div id="drag_<%=udid.getId()%>" class="portlet drag_div ibox float-e-margins" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>" count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="ibox-title">
                        <h5>
                        <i class="fa <%=udid.getIcon()%>"></i>
                        <a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%></a>
                        </h5>
                    </div>
                    <div class="ibox-content" id="drag_<%=udid.getId()%>_c">
                        <%=moduleContent%>
                    </div>
                </div>
                <%
                            }
                    }
                }
                %>
            </div>
            <div class="col-sm-4" id="col_0">
                <%
                    iItems = items.iterator();
                    while(iItems.hasNext()) {
                        udid = (UserDesktopSetupDb)iItems.next();
                        if(udid.getTd() == UserDesktopSetupDb.TD_SIDEBAR) {
                            DesktopUnit du = dm.getDesktopUnit(udid.getModuleCode());
                            if (du==null) {
                                out.print("模块5:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            IDesktopUnit idu = du.getIDesktopUnit();
                            if (idu==null) {
                                out.print("模块6:" + udid.getModuleCode() + " " + udid.getTitle() + "不存在！");
                                continue;
                            }
                            if (idu instanceof com.redmoon.oa.ui.desktop.IncludeDesktopUnit) {
                                request.setAttribute("parentId", "col_0");
                                out.print(idu.display(request, udid));
                            } else {
                                moduleContent = du.getIDesktopUnit().display(request, udid);
                %>
                <div id="drag_<%=udid.getId()%>" class="portlet drag_div ibox float-e-margins" type="<%=du.getType()%>" dragTitle="<%=udid.getTitle()%>" count="<%=udid.getCount()%>" wordCount="<%=udid.getWordCount()%>" <%=moduleContent.startsWith("<table") ? "style='overflow-y:auto'" : "" %>>
                    <div id="drag_<%=udid.getId()%>_h" class="ibox-title">
                        <h5>
                        <i class="fa <%=udid.getIcon()%>"></i>
                        <a href="<%=du.getIDesktopUnit().getPageList(request, udid)%>"><%=udid.getTitle()%></a>
                        </h5>
                    </div>
                    <div class="ibox-content" id="drag_<%=udid.getId()%>_c">
                        <%=moduleContent%>
                    </div>
                </div>
                <%
                            }
                    }
                }
                %>
            </div>
        </div>
    </div>
    <div class="bg"></div>
    <%
        IOaNoticeService oaNoticeService = SpringUtil.getBean(IOaNoticeService.class);
        List<OaNotice> list = oaNoticeService.listImportant(userName);
        int totalUnknown = 0;
        if (list.size() > 0) {
    %>
    <!--滑动的重要通知-->
    <div class="advbox" style="z-index:3">
        <div class="advpic">
            <a href="javascript:void(0);" class="closebtn" title="关闭"><img src="images/close.png" style="border:0"/></a>
            <div>
                <section class="slider">
                    <div class="flexslider">
                        <ul class="slides">
                            <%
                                //String token = "," + privilege.getUser(request);
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
</body>
<script>
    window.onload = function(){
        jQuery(document).click(function(e) {
            onClickDoc(e);
            return false;
        })

        //获取光标
        //document.body.focus();
        //禁止退格键 作用于Firefox、Opera
        document.onkeypress = banBackSpace;
        //禁止退格键 作用于IE、Chrome
        document.onkeydown = banBackSpace;
    }

    function onClickDoc(e) {
        var obj=isIE()? event.srcElement : e.target;
        if (isIE() && event.shiftKey) {
            if (obj.tagName=="A") {
                window.open(obj.getAttribute("href"));
                return false;
            }
        }

        if (obj.parentNode) {
            // alert(obj.parentNode.getAttribute("href"));
            if (obj.parentNode.tagName=="A") {
                var href = obj.parentNode.getAttribute("href");
                if (href!=null && href.indexOf("javascript")!=0) {
                    var inText = isIE()? obj.parentNode.innerText : obj.parentNode.textContent;
                    if (href.indexOf('download') != -1) {
                        window.open(href);
                    }
                    else {
                        addTab(inText, href);
                    }
                    return false;
                }
            }
            if (obj.parentNode.parentNode) {
                if (obj.parentNode.parentNode.tagName=="A") {
                    var inText = isIE()? obj.parentNode.parentNode.innerText : obj.parentNode.parentNode.textContent;
                    if (href.indexOf('download') != -1) {
                        window.open(href);
                    }
                    else {
                        addTab(inText, obj.parentNode.parentNode.getAttribute("href"));
                    }
                    return false;
                }
            }

        }
        if (obj.tagName=="A") {
            var href = obj.getAttribute("href");

            var cls = "";
            if(isIE7){
                cls = obj.getAttribute("className");
            }else{
                cls = obj.getAttribute("class");
            }
            // var cls = obj.getAttribute("className");
            // 跳过flexslider的左右滑动按钮
            if (cls!=null && cls.indexOf("flex")==0) {
                return false;
            }
            if (href!=null && href.indexOf("javascript")!=0) {
                var inText = isIE()? obj.innerText : obj.textContent;
                if (href.indexOf('download') != -1) {
                    window.open(href);
                }
                else {
                    addTab(inText, href);
                }
                return false;
            }
        }
    }

    //解决页面按了退格键，回到登陆页bug
    function banBackSpace(e){
        var ev = e || window.event;//获取event对象
        var obj = ev.target || ev.srcElement;//获取事件源
        var t = obj.type || obj.getAttribute('type');//获取事件源类型
        //获取作为判断条件的事件类型
        var vReadOnly = obj.readOnly;
        var vDisabled = obj.disabled;
        //处理undefined值情况
        vReadOnly = (vReadOnly == undefined) ? false: vReadOnly;
        vDisabled = (vDisabled == undefined) ? true: vDisabled;
        //当敲Backspace键时，事件源类型为密码或单行、多行文本的，
        //并且readOnly属性为true或disabled属性为true的，则退格键失效
        var flag1= ev.keyCode == 8 && (t=="password"|| t=="text"|| t=="textarea")&& (vReadOnly==true|| vDisabled==true);
        //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
        var flag2= ev.keyCode == 8 && t != "password"&& t != "text"&& t != "textarea";
        //判断
        if(flag2 || flag1) return false;
    }
</script>
</html>
