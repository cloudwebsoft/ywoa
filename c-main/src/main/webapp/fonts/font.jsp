<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%
    String kind = ParamUtil.get(request, "kind");
    if ("".equals(kind)) {
        out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>iconfont</title>
    <link rel="stylesheet" href="font.css">
    <link rel="stylesheet" href="<%=kind%>/iconfont.css">
    <style>
        .dib {
            vertical-align: top;
            font-size: 12px;
            letter-spacing: normal;
            word-spacing: normal;
            line-height: inherit;
            display: inline-block;
            *display: inline;
            *zoom: 1;
        }
        .icon {
            width: 1em;
            height: 1em;
            vertical-align: -0.15em;
            fill: currentColor;
            overflow: hidden;
            cursor: pointer;
        }
    </style>
</head>
<body>
<div class="main">
    <div class="tab-container">
        <div class="content symbol">
            <ul class="icon_lists dib-box">
            </ul>
        </div>
    </div>
</div>
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=kind%>/iconfont.js"></script>
<script>
    $(function () {
        $.ajax({
            url: "<%=kind%>/iconfont.json",
            type: "post",
            data: {
            },
            dataType: "json",
            success: function (data, status) {
                var ary = data.glyphs;
                for (i in ary) {
                    var li = '<li class="dib"><svg class="icon svg-icon" aria-hidden="true">';
                    li += '    <use xlink:href="#icon-' + ary[i].font_class + '"></use>';
                    li += '</svg></li>';
                    $('.icon_lists').append(li);
                }

                $('.icon_lists li').click(function (e) {
                    var fontIcon = $(this).find('use').attr('xlink:href');
                    window.opener.setFontIcon(fontIcon);
                    window.close();
                });
            },
            error: function (XMLHttpRequest, textStatus) {
                alert(XMLHttpRequest.responseText);
            }
        });
    });
</script>
</body>
</html>
