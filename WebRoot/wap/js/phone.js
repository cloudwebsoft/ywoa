var view_timer = null;
function viewPort(userAgent, pageWidth) {
    var oView = document.getElementById('viewport');
    if (oView) {
        document.head.removeChild(oView);
    }
    if (!pageWidth) {
        pageWidth = 640;
    }
    var screen_w = parseInt(window.screen.width),
        scale = screen_w / pageWidth;
    if (/Android (\d+\.\d+)/.test(userAgent)) {
        var creat_meta = document.createElement('meta');
        creat_meta.name = 'viewport';
        creat_meta.id = 'viewport';
        var version = parseFloat(RegExp.$1);
        if (version > 2.3) {
            creat_meta.content = 'width=' + pageWidth + ', initial-scale = ' + scale + ',user-scalable=1, minimum-scale = ' + scale + ', maximum-scale = ' + scale + ', target-densitydpi=device-dpi';
        } else {
            creat_meta.content = '"width=' + pageWidth + ', target-densitydpi=device-dpi';
        }
        document.head.appendChild(creat_meta);
    } else {
        var creat_meta = document.createElement('meta');
        creat_meta.name = 'viewport';
        creat_meta.id = 'viewport';
        if(window.orientation=='-90' || window.orientation == '90'){
            scale = window.screen.height / pageWidth;
            creat_meta.content = 'width=' + pageWidth + ', initial-scale = ' + scale + ' ,minimum-scale = ' + scale + ', maximum-scale = ' + scale + ', user-scalable=no, target-densitydpi=device-dpi';
        }
        else{
            creat_meta.content = 'width=' + pageWidth + ', initial-scale = ' + scale + ' ,minimum-scale = ' + scale + ', maximum-scale = ' + scale + ', user-scalable=no, target-densitydpi=device-dpi';
        }
        document.head.appendChild(creat_meta);
    }
}
viewPort(navigator.userAgent);

window.onresize = function() {
    clearTimeout(view_timer);
    view_timer = setTimeout(function(){
        viewPort(navigator.userAgent);
    }, 500);
}
