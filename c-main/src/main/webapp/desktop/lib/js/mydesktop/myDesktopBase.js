/*-----------------------------------------------------------
 *version: mydesktop 3.0
 *author: muzilei
 *email: 530624206@qq.com
 *blog: http://www.muzilei.com/
 *date:2013-1-24
 *桌面基础库和资源加载脚本
 ----------------------------------------------------------*/
 
//声明mydesktop命名空间 
var myDesktop={
      //获取对象类型名,["Array", "Boolean", "Date", "Number", "Object", "RegExp", "String", "Window", "HTMLDocument"]
    getType:function(object){
        return Object.prototype.toString.call(object).match(/^\[object\s(.*)\]$/)[1];
        },
    //用来判断对象类型
    isTypeOf:function(object,typeStr){
        return this.getType(object)==typeStr;
        },
    textLength:function(text){
        var intLength=0;
        for (var i=0;i<text.length;i++){
            if ((text.charCodeAt(i) < 0) || (text.charCodeAt(i) > 255)){
                intLength=intLength+2;
            }else{
                intLength=intLength+1;
                }
         }
         return intLength;
         },    
    getImgWh:function(url, callback) {
        var width, height, intervalId, check, div, img = new Image(),
            body = document.body;
            
			img.src = url;

        //从缓存中读取
        if (img.complete) {
          return callback(img.width, img.height);
        };

        //通过占位提前获取图片头部数据
        if (body) {
          div = document.createElement('div');
          div.style.cssText = 'visibility:hidden;position:absolute;left:0;top:0;width:1px;height:1px;overflow:hidden';
          div.appendChild(img);
          body.appendChild(div);
          width = img.offsetWidth;
          height = img.offsetHeight;
          check = function() {
            if (img.offsetWidth !== width || img.offsetHeight !== height) {
              clearInterval(intervalId);
              callback(img.offsetWidth, img.clientHeight);
              img.onload = null;
              div.innerHTML = '';
              div.parentNode.removeChild(div);
            };
          };
          intervalId = setInterval(check, 150);
        };
        // 加载完毕后方式获取
        img.onload = function() {
          callback(img.width, img.height);
          img.onload = img.onerror = null;
          clearInterval(intervalId);
          body && img.parentNode.removeChild(img);
        };
      },
	disableSelect:function(){
		function disableselect(e){return false;} 
		function reEnable(){return true;} 
		
		//if IE4+ 
		document.onselectstart=new Function ("return false");
		
		//if NS6 
		if (window.sidebar){
			document.onmousedown=disableselect;
			document.onclick=reEnable;
			} 
		},  
    //全屏
    fullscreen:function(){
         var docElm = document.documentElement;
             if (docElm.requestFullscreen) {
                 docElm.requestFullscreen();
               }
             else if (docElm.mozRequestFullScreen) {
             docElm.mozRequestFullScreen();
               }
             else if (docElm.webkitRequestFullScreen) {
             docElm.webkitRequestFullScreen();
                    }
        },
    //退出全屏
    exitFullscreen:function(){
        if (document.exitFullscreen) {
                document.exitFullscreen();
                }
                else if (document.mozCancelFullScreen) {
                    document.mozCancelFullScreen();
                    }
                    else if (document.webkitCancelFullScreen) {
                        document.webkitCancelFullScreen();
                        }
        },
    //IE全屏
    fullscreenIE:function(){
        if($.browser.msie){
                        var  wsh =  new  ActiveXObject("WScript.Shell");  
                        wsh.sendKeys("{F11}");
                    }
        },
	mouseXY:function(){
		var mouseXY=[];
		$(document).bind('mousemove',function(e){ 
			mouseXY[0]=e.pageX;
			mouseXY[1]=e.pageY;
           });
		return mouseXY;
		},	
	//右键菜单	
	contextMenu:function(jqElem,data,menuName,textLimit){
		  var _this=this
		      ,mXY=_this.mouseXY();
		  
          jqElem
		  .smartMenu(data,{
            name: menuName,
			textLimit:textLimit,
			beforeShow:function(event){
				 
				},
			afterShow:function(){
				var menu=$("#smartMenu_"+menuName),
				    wh={'w':$(window).width(),'h':$(window).height};
				
 				var menuXY=menu.offset(),menuH=menu.height(),menuW=menu.width();
				if(menuXY.top>wh['h']-menuH){
					menu.css('top',mXY[1]-menuH-2);
					}
				if(menuXY.left>wh['w']-menuW){
					menu.css('left',mXY[0]-menuW-2);
					}	
				}
           });
		   
		  $(document.body).click(function(event){
			           event.preventDefault(); 			  
			           $.smartMenu.hide();
						  });
		},
	//加载进度条
	progressBar:function(){
		$("<div id='loadingCover'></div><div id='loadimg'><span>正在加载，请稍等...</span></div>").appendTo('body');
		var w=$(window).width(),h=$(window).height();
		$('#loadingCover').css({'width':'100%','height':h,'position':'absolute','background':'#fff url(desktop/theme/default/images/wallpaper.jpg) no-repeat center center','z-index':999999,'left':0,'top':0}).fadeTo('slow',1);
		$('#loadimg').css({'position':'absolute','background':'url(desktop/theme/default/images/loading.gif) no-repeat center center','z-index':1000000,'width':'110px','height':'64px','left':(w-110)/2,'top':((h-64)/2)-50}).find('span').css({'position':'absolute','left':0,'bottom':'-40px','width':110,'display':'block','height':40,'text-align':'center'});
		},
	//停止进度条
	stopProgress:function(){
			$('#loadingCover').remove();
			$('#loadimg').remove();
	  }, 
	//动态加载文件,includePath 要加载文件所在根路径,files 要加载文件的数组
	includFile:function(includePath,file){
		var files = typeof file == "string" ? [file] : file;
        for (var i = 0; i < files.length; i++)
        {
            var name = files[i].replace(/^\s|\s$/g, "");
            var att = name.split('.');
            var ext = att[att.length - 1].toLowerCase();
            var isCSS = ext == "css";
            var tag = isCSS ? "link" : "script";
            var attr = isCSS ? " type='text/css' rel='stylesheet' " : " language='javascript' type='text/javascript' ";
            var link = (isCSS ? "href" : "src") + "='" + includePath + name + "'";
            if ($(tag + "[" + link + "]").length == 0) document.write("<" + tag + attr + link + "></" + tag + ">");
        }
		},
	trim:function(str){
				return str.replace(/^\s+|\s+$/g,"");
	}
};

$(function(){
	//加载进度条
    myDesktop.progressBar();
	});


//加载所需的样式文件和jquery插件
myDesktop.includFile("desktop/lib/css/",['reset-min.css']);
// myDesktop.includFile("desktop/theme/default/css/",['myWindow.css','desktop.css','desktopBar.css','topBar.css','widget.css','loginBox.css']);
myDesktop.includFile("desktop/theme/default/css/",['myWindow.css','desktop.css','topBar.css','widget.css','loginBox.css','infoBar.css','desktopBar.css','appManager.css']);

myDesktop.includFile("desktop/lib/js/jquery/",['themes/jquery-ui.min.css','jquery-ui-1.9.0.custom.min.js','jquery.winResize.js','jquery.easing.1.3.min.js','jquery.mousewheel.min.js','jquery.nicescroll.min.js','jquery.ui.touch-punch.min.js']);

myDesktop.includFile("desktop/lib/js/jquery/jquery-smartMenu/",['jquery-smartMenu-min.js','smartMenu.css']);

myDesktop.includFile("desktop/lib/js/mydesktop/",['myDesktop.3.0.js']); 