<%@ page language="java" import="java.util.*" pageEncoding="utf-8" %>
<%@ page import="com.redmoon.oa.android.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.Document" %>
<%@ page import="com.redmoon.oa.fileark.Attachment" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="org.htmlparser.Parser" %>
<%@ page import="org.htmlparser.filters.TagNameFilter" %>
<%@ page import="org.htmlparser.util.NodeList" %>
<%@ page import="org.htmlparser.tags.ImageTag" %>
<%@ page import="org.htmlparser.util.ParserException" %>
<%!
    public String repairImgUrl(HttpServletRequest request, String content) {
        Parser parser;
        try {
            parser = new Parser(content);
            parser.setEncoding("utf-8");//
            TagNameFilter filter = new TagNameFilter("img");
            NodeList nodes = parser.parse(filter);
            if (nodes == null || nodes.size() == 0) {
                ;
            } else {
                for (int k = 0; k < nodes.size(); k++) {
                    ImageTag node = (ImageTag) nodes.elementAt(k);
                    String imgUrl = node.getImageURL();
                    int p = imgUrl.indexOf("upfile");
                    if (p == 0) {
                        imgUrl = request.getContextPath() + "/" + imgUrl;
                        node.setImageURL(imgUrl);
                        int s = node.getStartPosition();
                        int e = node.getEndPosition();
                        String c = content.substring(0, s);
                        c += node.toHtml();
                        c += content.substring(e);
                        content = c;
                    }
                }
            }
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }
%>
<%
    int id = ParamUtil.getInt(request, "id", -1);
    if (id == -1) {
        out.print(SkinUtil.LoadString(request, "err_id"));
        return;
    }

    com.redmoon.oa.robot.Config robotCfg = com.redmoon.oa.robot.Config.getInstance();
    boolean isRobotOpen = robotCfg.getBooleanProperty("isRobotOpen");

    Privilege pvg = new Privilege();
    boolean isLogin = pvg.auth(request);
    if (!isLogin) {
        if (isRobotOpen) {
            // 来自于分享
            String visitKey = ParamUtil.get(request, "visitKey");
            com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
            String desKey = ssoconfig.get("key");
            visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
            String[] ary = StrUtil.split(visitKey, "\\|"); // 格式为：id|timestamp
            if (ary != null && ary.length == 2) {
                if (!String.valueOf(id).equals(ary[0])) {
                    out.print(StrUtil.p_center("请登录"));
                    return;
                }
            } else {
                out.print(StrUtil.p_center("非法访问"));
                return;
            }
        }
    }
    String skey = pvg.getSkey();
    String groupId = ParamUtil.get(request, "groupId");

    Document doc = new Document();
    doc = doc.getDocument(id);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>文件柜</title>
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

        .author_p {
            text-align: center;
            font-size: 14px;
        }

        .content {
            font-size: 16px;
            color: #666;
        }
    </style>
</head>
<body>
<%
    String author = StrUtil.getNullStr(doc.getAuthor());
    UserDb user = new UserDb();
    user = user.getUserDb(author);
    if (user.isLoaded()) {
        author = user.getRealName();
    }
    String createDate = DateUtil.format(doc.getCreateDate(), "yyyy-MM-dd");
%>
<div class="mui-content">
    <div class="mui-table-view">
        <div class="mui-table-view-cell">
            <h5 class="title"><%=doc.getTitle()%>
            </h5>
            <p class="mui-pull-right"><%=author%>&nbsp;&nbsp;<%=createDate%>
            </p>
        </div>
        <div class="mui-table-view-cell">
            <p class="p-content"><%=repairImgUrl(request, doc.getContent(1))%>
            </p>
        </div>
    </div>
    <%
        Vector v = doc.getAttachments(1);
        if (v.size() > 0) {
    %>
    <ul class="mui-table-view mui-table-view-chevron att_ul">
        <li class="mui-table-view-cell mui-media ">附件列表：</li>
        <%
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
        %>
        <li class="mui-table-view-cell mui-media att_li" fId="<%=att.getId() %>">
            <div class="mui-slider-handle">
                <a class="attFile" href="javascript:;" ext="<%=att.getExt()%>"
                   link="public/doc_getfile.jsp?skey=<%=skey%>&id=<%=id%>&attachId=<%=att.getId()%>">
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
<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="../css/mui.css"></script>
<script type="text/javascript" src="../js/mui.min.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
<script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
<script src="../js/jq_mydialog.js"></script>

<script>
    $(".mui-table-view").on("tap", ".attFile", function () {
        /*
         mui.openWindow({
         "url":url
         });
         */
        // showImg(url);

        var url = jQuery(this).attr("link");
        var ext = jQuery(this).attr("ext");
        if (ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "gif" || ext == "bmp") {
            showImg(url);
        }
        else {
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
<%
    if (isRobotOpen) {
        String imgUrl = doc.getFirstImagePathOfDoc();
        if ("".equals(imgUrl)) {
            imgUrl = "http://qqsales.yimihome.com/images/fileark_share.png";
        } else {
            if (!imgUrl.startsWith("http:")) {
                imgUrl = Global.getFullRootPath(request) + "/" + imgUrl;
            }
        }

        String summary = StrUtil.getNullStr(doc.getSummary());
        if ("".equals(summary)) {
            summary = StrUtil.getAbstract(request, doc.getContent(1), 100, "");
        }

        summary = summary.replace("&nbsp;", " ");
        summary = summary.replace("\n", " ");

        com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
        String desKey = ssoconfig.get("key");
        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(id) + "|" + new Date().getTime());

%>
<script typet="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script type="text/javascript" src="http://qzonestyle.gtimg.cn/qzone/qzact/common/share/share.js"></script>
<script type="text/javascript">
    function isWeiXin() {
        var ua = window.navigator.userAgent.toLowerCase();
        if (ua.match(/MicroMessenger/i) == 'micromessenger') {
            return true;
        } else {
            return false;
        }
    }

    // 分享至朋友圈
    function onShareTimeline() {
        $.ajax({
            type: "post",
            url: "shareTimeline.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            dataType: "json",
            async: false,
            data: {
                docId: <%=id%>
            },
            success: function (data) {
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            error: function (xhr, status, error) {
                //alert(status);
                alert(xhr.responseText);
            }
        })
    }

    // 分享给朋友
    function onShareApp() {
        $.ajax({
            type: "post",
            url: "shareApp.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            dataType: "json",
            async: false,
            data: {
                docId: <%=id%>
            },
            success: function (data) {
                $.toaster({
                    "priority": "info",
                    "message": data.msg
                });
            },
            error: function (xhr, status, error) {
                //alert(status);
                alert(xhr.responseText);
            }
        })
    }

    var appId, timestamp, nonceStr, signature;
    $(function () {
        if (!isWeiXin()) {
            // return;
        }

        var url = location.href.split('#').toString();//url必须与本页面地址一致，前不含#以后的内容
        // 取微信设置的相关参数
        $.ajax({
            type: "get",
            url: "../../public/wechat/wechatParam.do",
            dataType: "json",
            async: false,
            data: {url: url},
            success: function (data) {
                // console.log(data);
                var errMsg = data.errMsg;
                if (errMsg) {
                    mui.toast(errMsg);
                    return;
                }
                appId = data.appid;
                timestamp = data.timestamp;
                nonceStr = data.nonceStr;
                signature = data.signature;

                // 权限注入
                wx.config({
                    debug: false,	//生产环境需要关闭debug模式
                    appId: data.appid,	//appId通过微信服务号后台查看
                    timestamp: data.timestamp,//生成签名的时间戳
                    nonceStr: data.nonceStr,//生成签名的随机字符串
                    signature: data.signature,//签名
                    jsApiList: [//需要调用的JS接口列表
                        // 'checkJsApi',//判断当前客户端版本是否支持指定JS接口
                        'onMenuShareTimeline',//分享给好友
                        'onMenuShareAppMessage'//分享到朋友圈
                    ]
                });
            },
            error: function (xhr, status, error) {
                //alert(status);
                alert(xhr.responseText);
            }
        })

        wx.ready(function () {
            var link = window.location.href + "&visitKey=<%=visitKey%>";
            // 分享朋友圈
            wx.onMenuShareTimeline({
                title: '<%=doc.getTitle()%>',
                link: link,
                imgUrl: "<%=imgUrl%>",// 自定义图标
                trigger: function (res) {
                    // 不要尝试在trigger中使用ajax异步请求修改本次分享的内容，因为客户端分享操作是一个同步操作，这时候使用ajax的回包会还没有返回.
                },
                success: function (res) {
                    <%
                        if (!"".equals(groupId)) {
                            com.redmoon.oa.robot.Config rcfg = com.redmoon.oa.robot.Config.getInstance();
                            com.redmoon.oa.robot.Group group = rcfg.getGroup(groupId);
                            if (group.isDocShare()) {
                    %>
                    onShareTimeline();
                    <%
                            }
                    }%>
                },
                cancel: function (res) {
                },
                fail: function (res) {
                    alert(JSON.stringify(res));
                }
            });
            // 分享给好友
            wx.onMenuShareAppMessage({
                title: '<%=doc.getTitle()%>', // 分享标题
                desc: '<%=summary%>', // 分享描述
                link: link, // 分享链接，该链接域名或路径必须与当前页面对应的公众号JS安全域名一致
                imgUrl: "<%=imgUrl%>", // 自定义图标
                type: 'link', // 分享类型,music、video或link，不填默认为link
                dataUrl: '', // 如果type是music或video，则要提供数据链接，默认为空
                success: function () {
                    // 用户确认分享后执行的回调函数
                    <%
                        if (!"".equals(groupId)) {
                            com.redmoon.oa.robot.Config rcfg = com.redmoon.oa.robot.Config.getInstance();
                            com.redmoon.oa.robot.Group group = rcfg.getGroup(groupId);
                            if (group.isDocShare()) {
                    %>
                    onShareApp();
                    <%
                            }
                    }%>
                },
                cancel: function () {
                    // 用户取消分享后执行的回调函数
                }
            });
        });
        wx.error(function (res) {
            // console.log(res);
            // alert(res.errMsg);
        });

        // 置QQ中分享设置
        var pageUrl = window.location.href + "&visitKey=<%=visitKey%>";
        setShareInfo({
            title: '<%=doc.getTitle()%>',
            summary: '<%=summary%>',
            pic: '<%=imgUrl%>',
            url: pageUrl,
            // 微信权限验证配置信息，若不在微信传播，可忽略
            WXconfig: {
                swapTitleInWX: true, // 是否标题内容互换（仅朋友圈，因朋友圈内只显示标题）
                appId: appId, // 公众号的唯一标识
                timestamp: timestamp, // 生成签名的时间戳
                nonceStr: nonceStr, // 生成签名的随机串
                signature: signature // 签名
            }
        });
    });
</script>
<%
    }
%>
<jsp:include page="../inc/navbar.jsp">
    <jsp:param name="skey" value="<%=pvg.getSkey()%>"/>
    <jsp:param name="isBarBottomShow" value="false"/>
</jsp:include>
</body>
</html>
