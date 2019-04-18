<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanDb" %>
<%@ page import="com.redmoon.oa.workplan.Attachment" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTypeDb" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTaskDb" %>
<%
    int id = ParamUtil.getInt(request, "id", -1);
    if (id == -1) {
        out.print(SkinUtil.LoadString(request, "err_id"));
        return;
    }

    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();
    String userName = pvg.getUserName();

    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(id);
    if (!wpd.isLoaded()) {
        out.print(StrUtil.p_center("计划不存在"));
        return;
    }
    UserMgr um = new UserMgr();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title><%=wpd.getTitle()%></title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta content="telephone=no" name="format-detection"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <link href="../../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet"/>
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
    <style type="text/css">
        h5 {
            font-weight: bold;
            text-align: center;
            font-size: 16px;
            color: #000;
        }

        .createdate {
            font-size: 12px;
            color: #8f8f94;
        }
    </style>
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="../css/mui.css"></script>
    <script type="text/javascript" src="../js/mui.min.js"></script>
    <script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
    <script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
    <script src="../js/jq_mydialog.js"></script>
</head>
<body>
<div class="mui-content">
    <div style="padding: 10px 10px;">
        <div id="segmentedControl" class="mui-segmented-control mui-segmented-control-inverted">
            <a class="mui-control-item mui-active" href="#item1">
                任务
            </a>
            <a id="cardDetail" class="mui-control-item" href="#item2">
                详情
            </a>
            <a class="mui-control-item annex-day">
                日报
            </a>
            <a class="mui-control-item annex-week">
                周报
            </a>
            <a class="mui-control-item annex-month">
                月报
            </a>
            <script>
                $(function() {
                    <%
                    String action = ParamUtil.get(request, "action");
                    if ("detail".equals(action)) {
                        %>
                        mui.trigger($('.mui-control-item').eq(1)[0],'touchstart');
                        mui.trigger($('.mui-control-item').eq(1)[0],'tap');
                        <%
                    }
                    %>
                    mui('#segmentedControl').on('tap', '.annex-day', function () {
                        mui.openWindow({
                            "url": "workplan_annex_day.jsp?id=<%=id%>"
                        })
                    });
                    mui('#segmentedControl').on('tap', '.annex-week', function () {
                        mui.openWindow({
                            "url": "workplan_annex_list_week.jsp?id=<%=id%>"
                        })
                    });
                    mui('#segmentedControl').on('tap', '.annex-month', function () {
                        mui.openWindow({
                            "url": "workplan_annex_list_month.jsp?id=<%=id%>"
                        })
                    });
                })
            </script>
        </div>
    </div>
    <div>
        <div id="item1" class="mui-control-content mui-active">
            <ul class="mui-table-view">
                <%
                    String sql = "select id from work_plan_task where work_plan_id=" + id;
                    WorkPlanTaskDb wpt = new WorkPlanTaskDb();
                    Iterator ir = wpt.list(sql).iterator();
                    while (ir.hasNext()) {
                        wpt = (WorkPlanTaskDb)ir.next();
                        String strStartDate = DateUtil.format(wpt.getDate("start_date"), "yyyy-MM-dd");
                        String faIcon = wpt.getInt("progress")==100?"fa-star":"fa-star-half-o";
                %>
                <li class="mui-table-view-cell mui-table-view-chevron li-task">
                    <a id="<%=wpt.getLong("id")%>"><i class="fa <%=faIcon%> mui-pull-left"
                                                     style="margin-right: 10px" aria-hidden="true"></i>
                        <div class="mui-media-body">
                            <span><%=wpt.getString("name")%></span><span class="mui-pull-right createdate"><%=strStartDate%></span>
                            <p class="mui-ellipsis">
                                <%
                                if (!StrUtil.getNullStr(wpt.getString("task_resource")).equals("")) {
                                    UserDb user = um.getUserDb(wpt.getString("task_resource"));
                                %>
                                    <%=user.getRealName()%>
                                <%}%>
                                &nbsp;&nbsp;<%=wpt.getInt("duration")%>天&nbsp;&nbsp;<%=wpt.getInt("progress")%>%</p>
                        </div>
                    </a>
                </li>
                <%}%>
            </ul>
        </div>
        <script>
            $(function() {
                mui('.mui-table-view').on('tap','a',function() {
                    mui.openWindow({
                        "url":"workplan_task_show.jsp?id=" + $(this).attr("id")
                    });
                })
            })
        </script>
        <div id="item2" class="mui-control-content">
            <ul class="mui-table-view">
                <li class="mui-table-view-cell" style="text-align:center"><b><%=wpd.getTitle() %>
                </b><br></li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">进度</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpd.getProgress() %>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">剩余天数</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;">
                            <%
                                int nowDays = DateUtil.datediff(wpd.getEndDate(), new Date());
                                if (nowDays < 0) {
                                    // nowDays = 0;
                                }
                                int sumDays = DateUtil.datediff(wpd.getEndDate(), wpd.getBeginDate());
                                float progress = (float) nowDays / sumDays;

                                float r23 = (float) 2 / 3;
                                if (progress > r23) {
                            %>
                            <img src="../../images/green.jpg" width="16" height="18" border="0"/>
                            <%} else if (progress < r23 && progress > ((float) 1 / 3)) {%>
                            <img src="../../images/yel.jpg" width="16" height="18" border="0"/>
                            <%} else if (progress < ((float) 1 / 3) && progress >= 0) {%>
                            <img src="../../images/red.jpg" width="16" height="18" border="0"/>
                            <%} else {%>
                            <img src="../../images/red_hot.jpg" width="16" height="18" border="0"/>
                            <%}%>
                            <%if (progress < 1 && nowDays < 0) {%>
                            <font color="red">过期<%=-nowDays%>天</font>
                            <%} else {%>
                            剩余<%=nowDays%>天
                            <%}%>
                            &nbsp;(总天数：<%=sumDays%>天)
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">内容</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpd.getContent() %>
                        </div>
                    </div>
                </li>
                <%
                    String beginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
                    String endDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
                    WorkPlanTypeDb wptd = new WorkPlanTypeDb();
                    wptd = wptd.getWorkPlanTypeDb(wpd.getTypeId());

                    String[] arydepts = wpd.getDepts();
                    String[] aryusers = wpd.getUsers();
                    String depts = "";
                    String deptNames = "";
                    String users = "";

                    int len = 0;
                    if (arydepts != null) {
                        len = arydepts.length;
                        DeptDb dd = new DeptDb();
                        for (int i = 0; i < len; i++) {
                            if (depts.equals("")) {
                                depts = arydepts[i];
                                dd = dd.getDeptDb(arydepts[i]);
                                deptNames = dd.getName();
                            } else {
                                depts += "," + arydepts[i];
                                dd = dd.getDeptDb(arydepts[i]);
                                deptNames += "，" + dd.getName();
                            }
                        }
                    }

                    if (aryusers != null) {
                        len = aryusers.length;
                        for (int i = 0; i < len; i++) {
                            UserDb user = um.getUserDb(aryusers[i]);
                            if (users.equals("")) {
                                users = user.getRealName();
                            } else {
                                users += "，" + user.getRealName();
                            }
                        }
                    }

                    String[] principalAry = wpd.getPrincipals();
                    len = principalAry.length;
                    String principals = "";
                    for (int i = 0; i < len; i++) {
                        if (principalAry[i].equals(""))
                            continue;
                        UserDb user = um.getUserDb(principalAry[i]);
                        if (principals.equals(""))
                            principals = user.getRealName();
                        else
                            principals += "，" + user.getRealName();
                    }
                %>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">开始日期</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=beginDate %>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">结束日期</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=endDate%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">类型</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wptd.getName()%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">发布范围（部门）</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=deptNames%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">参与人</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=users%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">负责人</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=principals%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">创建者</div>
                        <div class="mui-col-sm-2"
                             style="width:70%; text-align:left;"><%=um.getUserDb(wpd.getAuthor()).getRealName()%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">审核状态</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;">
                            <%if (wpd.getCheckStatus() == WorkPlanDb.CHECK_STATUS_NOT) {%>
                            未审
                            <%} else {%>
                            已审
                            <%}%>
                        </div>
                    </div>
                </li>
                <li class="mui-table-view-cell">
                    <div class="mui-row">
                        <div class="mui-col-sm-2" style="width:30%; text-align:left;">备注</div>
                        <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpd.getRemark()%>
                        </div>
                    </div>
                </li>
            </ul>
            <%
                Vector v = wpd.getAttachments();
                if (v.size() > 0) {
            %>
            <ul class="mui-table-view mui-table-view-chevron att_ul">
                <li class="mui-table-view-cell mui-media ">附件列表：</li>
                <%
                    ir = v.iterator();
                    while (ir.hasNext()) {
                        Attachment att = (Attachment) ir.next();
                %>
                <li class="mui-table-view-cell mui-media att_li" fId="<%=att.getId() %>">
                    <div class="mui-slider-handle">
                        <a class="attFile" href="javascript:;" ext="<%=StrUtil.getFileExt(att.getDiskName())%>"
                           link="<%=att.getVisualPath() + att.getDiskName() %>">
                            <img class="mui-media-object mui-pull-left"
                                 src="../images/file/<%=com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(att.getDiskName())) %>"/>
                            <div class="mui-media-body">
                                <%=att.getName() %>
                            </div>
                        </a>
                    </div>
                </li>
                <%} %>
            </ul>
            <%} %>
        </div>
</div>
<script>
    $(".mui-table-view").on("tap", ".attFile", function () {
        var url = jQuery(this).attr("link");
        var ext = jQuery(this).attr("ext");
        if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
            showImg(url);
        } else {
            mui.openWindow({
                "url": "<%=request.getContextPath()%>/" + url
            })
        }
    })

    function showImg(path) {
        var openPhotoSwipe = function () {
            var pswpElement = document.querySelectorAll('.pswp')[0];
            var items = [{
                src: "../../public/img_show.jsp?path=" + encodeURI(path),
                w: 964,
                h: 1024
            }
            ];
            // define options (if needed)
            var options = {
                // history & focus options are disabled on CodePen
                history: false,
                focus: false,
                showAnimationDuration: 0,
                hideAnimationDuration: 0
            };
            var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
            gallery.init();
        };
        openPhotoSwipe();
    }

    function callJS() {
        return {"btnAddShow": 0, "btnAddUrl": ""};
    }

    var iosCallJS = '{ "btnAddShow":0, "btnAddUrl":"" }';
</script>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
