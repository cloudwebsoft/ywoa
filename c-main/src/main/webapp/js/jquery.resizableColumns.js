$.fn.ReSizeTablecolumn = function(options) {
    var defaultOptions = {
        SplitBarColor: '#000000',
        SplitBarWidth: '4px',
        SplitBarCursor: 'col-resize',
        FixedFirstColumn: false,
        AfterChange: function(e) { },
        BeginChange: function(e) { }
    };
    
    currTh = null;
    var tableWidth = parseInt($(this).css("width").replace("px", ""));
    var tableLeft = $(this).offset().left;
    var opts = $.extend(defaultOptions, options);
    if ($("#split_line").length == 0) {
        var splitBar = "<div id='split_line' style='' mce_style='width:" + opts.SplitBarWidth + ";height:200px;border-left:1px solid #00000000;background-color:" + opts.SplitBarColor +
                    ";cursor:" + opts.SplitBarCursor + "; position:absolute;display:none'></div><input id='__tableMoveing' type='hidden' value='false' /> ";
        $("body").append(splitBar);
        $(document).bind("mousemove", function(event) {
            if ($("#__tableMoveing").val() == "true") {
                if (event.clientX < tableLeft) {
                    return false;
                }
                if (event.clientX > tableLeft + tableWidth) {
                    return false;
                }
                $("#split_line").css({ "left": event.clientX }).show();
                return false;
            }
        });
        $(document).bind("mouseup", function(event) {
            if ($("#__tableMoveing").val() == "true") {
                $("#split_line").hide();
                $("#__tableMoveing").val("false");
                var e = { sender: currTh, cancel: false };
                opts.AfterChange(e);
                if (e.cancel == true) {
                    return;
                }
                var pos = currTh.offset();
                var index = currTh.prevAll().length;
                currTh.width(event.clientX - pos.left);
                currTh.parent().parent().find("tr").each(function() {
                    $(this).children().eq(index).width(event.clientX - pos.left);
                });
            }
        });
    }

    $(this).find("th").bind("mousemove", function(event) {
        var th = $(this);
        if (opts.FixedFirstColumn && th.prevAll().length == 1) {
            return;
        }
        if (th.prevAll().length < 1 || th.nextAll().length < 1) {
            return;
        }
        var left = th.offset().left;
        if (event.clientX - left < 4 || (th.width() - (event.clientX - left)) < 4) {
            th.css({ 'cursor': 'col-resize' });
        }
        else {
            th.css({ 'cursor': 'default' });
        }
    });
    $(this).find("th").bind("mousedown", function(event) {
        var th = $(this);
        if (opts.FixedFirstColumn && th.prevAll().length == 1) {
            return;
        }
        if (th.prevAll().length < 1 | th.nextAll().length < 1) {
            return;
        }
        var pos = th.offset();
        if (event.clientX - pos.left < 4 || (th.width() - (event.clientX - pos.left)) < 4) {
            var e = { sender: th, cancel: false };
            opts.BeginChange(e);
            if (e.cancel == true) {
                return;
            }
            $("#__tableMoveing").val("true");
            var height = th.parent().parent().height();
            var top = pos.top;
            $("#split_line").css({ "height": height, "top": top, "left": event.clientX, "display": "" });
            if (event.clientX - pos.left < th.width() / 2) {
                currTh = th.prev();
            }
            else {
                currTh = th;
            }
        }
        event.stopPropagation();
    });
};