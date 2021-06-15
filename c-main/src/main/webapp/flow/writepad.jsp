<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%
    int width = ParamUtil.getInt(request, "width", 600);
    int height = ParamUtil.getInt(request, "height", 200);
    int w = ParamUtil.getInt(request, "w", -1);
    int h = ParamUtil.getInt(request, "h", -1);
    String fieldName = ParamUtil.get(request, "fieldName");
%>
<!doctype html>
<html lang="zh">
<head>
    <meta http-equiv="content-type"  content="text/html; charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,minimum-scale=1.0,user-scalable=no" />
    <meta name="apple-mobile-web-app-capable" content="yes">
</head>
<body>
<div class="htmleaf-container">
    <div class="container">
        <div class="row">
            <div class="col-xs-12">
                <div class="js-signature" data-line-color="#01018b" data-background="transparent url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAlgAAADICAYAAAA0n5+2AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTM4IDc5LjE1OTgyNCwgMjAxNi8wOS8xNC0wMTowOTowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTcgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjNGQjExNjZFNUMzRDExRThBMEVBODlEOEI3QUZEQTA3IiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjNGQjExNjZGNUMzRDExRThBMEVBODlEOEI3QUZEQTA3Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6M0ZCMTE2NkM1QzNEMTFFOEEwRUE4OUQ4QjdBRkRBMDciIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6M0ZCMTE2NkQ1QzNEMTFFOEEwRUE4OUQ4QjdBRkRBMDciLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4uosxwAAAJuUlEQVR42uza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)" style="background-size:cover;"></div>
                <p style="text-align: center; width: <%=width%>px">
                    <button id="clearBtn" class="btn btn-default" >清空</button>
                    &nbsp;&nbsp;
                    <button id="saveBtn" class="btn btn-default" disabled>保存</button>
                </p>
            </div>
        </div>
    </div>
</div>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jq-signature.min.js"></script>
<script type="text/javascript">
    $(document).on('ready', function() {
        if ($('.js-signature').length) {
            var _width = <%=width%>;
            var _height = <%=height%>;
            var rate = 0;
            if(screen.width<_width){
                rate = _width/screen.width;
                _width = _width/rate - 15;
                _height = _height/rate ;
            }
            $('.js-signature').jqSignature({
                autoFit: false,
                color : '#01018b',
                // background-color: '#fff',
                lineWidth: 3,
                width: _width,
                height: _height
            });
        }

        $('.js-signature').on('jq.signature.changed', function() {
            $('#saveBtn').attr('disabled', false);
        });

        $('#clearBtn').on('click',function(){
            $('#signature').html('<p>写上签名并点击预览</p><div></div>');
            $('.js-signature').jqSignature('clearCanvas');
            $('#saveBtn').attr('disabled', true);
        })

        $('#saveBtn').on('click',function(){
            var dataUrl = $('.js-signature').jqSignature('getDataURL');
            var $img = $('<img>').attr('src', dataUrl);
            var w = <%=w%>, h = <%=h%>;
            if (w!=-1 && h!=-1) {
                $img.css({"width":w + "px", "height":h + "px"});
            }
            var $pad = $('#pad_<%=fieldName%>', window.opener.document);
            $pad.html($img.prop('outerHTML'));

            var $input = $('#<%=fieldName%>', window.opener.document);
            $input.val(dataUrl);
            window.close();
        })
    });
</script>
</body>
</html>