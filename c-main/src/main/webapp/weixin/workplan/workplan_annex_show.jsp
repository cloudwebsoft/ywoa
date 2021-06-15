<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanTaskDb" %>
<%@ page import="com.redmoon.oa.workplan.WorkPlanAnnexAttachment" %>
<%
    Privilege pvg = new Privilege();
    if (!pvg.auth(request)) {
        out.print(StrUtil.p_center("请登录"));
        return;
    }
    String skey = pvg.getSkey();

    long annexId = ParamUtil.getLong(request, "annexId", -1);
    WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
    wpad = (WorkPlanAnnexDb)wpad.getQObjectDb(annexId);
    if (wpad==null) {
        out.print(StrUtil.p_center("汇报不存在"));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no"/>
    <meta charset="utf-8">
    <meta name="format-detection" content="telephone=no,email=no,adress=no">
    <title>查看汇报</title>
    <link rel="stylesheet" href="../css/mui.css">
    <link rel="stylesheet" href="../css/iconfont.css"/>
    <link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css"/>
    <link href="../css/mui.indexedlist.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../css/my_dialog.css"/>
    <style>
        .mui-input-row .input-icon {
            width: 50%;
            float: left;
        }
        .mui-input-row a {
            margin-right: 10px;
            float: right;
            text-align: left;
            line-height: 1.5;
        }
        #captureFile {
            display: none;
        }
        .mui-input-row label {
            color: #000;
        }
    </style>
</head>
<body>
<%
    int workplanId = wpad.getInt("workplan_id");
    WorkPlanDb wpd = new WorkPlanDb();
    wpd = wpd.getWorkPlanDb(workplanId);
    int annexType = wpad.getInt("annex_type");
%>
<div class="mui-content">
    <ul class="mui-table-view">
        <li class="mui-table-view-cell" style="text-align:center"><b>
            <%=wpd.getTitle()%>&nbsp;
            <%
                if (annexType == WorkPlanAnnexDb.TYPE_WEEK) {
            %>
            周报
            <%
            } else {
            %>
            月报
            <%
                }

            %>
        </b>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">原进度</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpad.getInt("old_progress") %>%
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">现进度</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpad.getInt("progress") %>%
                </div>
            </div>
        </li>
        <li class="mui-table-view-cell">
            <div class="mui-row">
                <div class="mui-col-sm-2" style="width:30%; text-align:left;">内容</div>
                <div class="mui-col-sm-2" style="width:70%; text-align:left;"><%=wpad.getString("content") %>%
                </div>
            </div>
        </li>
    </ul>
    <ul class="mui-table-view mui-table-view-chevron att_ul">
        <li class="mui-table-view-cell mui-media ">附件：</li>
        <%
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String vpath = cfg.get("file_workplan");
            String attachmentBasePath = request.getContextPath() + "/" + vpath + "/";
            WorkPlanAnnexAttachment wpaa = new WorkPlanAnnexAttachment();
            Vector v = wpaa.getAttachments(wpad.getLong("id"));
            if (v.size() > 0) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    wpaa = (WorkPlanAnnexAttachment) ir.next();
        %>
        <li class="mui-table-view-cell mui-media" fId="<%=wpaa.getId() %>">
            <div class="mui-slider-handle">
                <a class="attFile" href="<%=attachmentBasePath + wpaa.getVisualPath() + "/" + wpaa.getDiskName() %>"
                   target="_blank">
                    <img class="mui-media-object mui-pull-left"
                         src="../images/file/<%=com.redmoon.oa.android.tools.Tools.getIcon(StrUtil.getFileExt(wpaa.getDiskName())) %>"/>
                    <div class="mui-media-body">
                        <%=wpaa.getName() %>
                    </div>
                </a>
            </div>
        </li>
        <%
                }
            }
        %>
    </ul>

    <input type="file" id="captureFile" name="upload" accept="image/*">
    <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jq_mydialog.js"></script>
    <script type="text/javascript" src="../js/newPopup.js"></script>
    <script src="../js/macro/macro.js"></script>
    <script src="../js/mui.min.js"></script>
    <script src="../js/mui.picker.min.js"></script>
    <script type="text/javascript" src="../js/config.js"></script>
    <script type="text/javascript" src="../js/base/mui.form.js"></script>
    <script type="text/javascript" src="../js/visual/module_list.js"></script>
    <link rel="stylesheet" href="../css/photoswipe.css">
    <link rel="stylesheet" href="../css/photoswipe-default-skin/default-skin.css">
    <script type="text/javascript" src="../js/photoswipe.js"></script>
    <script type="text/javascript" src="../js/photoswipe-ui-default.js"></script>
    <script type="text/javascript" src="../js/photoswipe-init.js"></script>
    <script type="text/javascript">
        $(".mui-content").on("tap", ".attFile", function () {
            var url = jQuery(this).attr("href");
            var p = url.lastIndexOf(".");
            var ext = url.substring(p+1);
            if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
                showImg(url);
            }
            else {
                mui.openWindow({
                    "url": "../../public/img_show.jsp?path=" + encodeURI(url)
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
            return {"btnAddShow": 0, "btnBackUrl": ""};
        }

        var iosCallJS = '{ "btnAddShow":0, "btnBackUrl":"" }';
    </script>
</div>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>" />
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>