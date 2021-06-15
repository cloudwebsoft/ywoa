/*-----------------------------------------------------------
 *version: mydesktop 3.0
 *author: muzilei
 *email: 530624206@qq.com
 *blog: http://www.muzilei.com/
 *date:2013-1-24
 *桌面组件脚本，包括窗口、状态栏、侧边栏、桌面、桌面导航切换、全局视图、登录框
 ----------------------------------------------------------*/
var desktopNames; 
//创建myWindow命名空间
myDesktop.myWindow={
	init:function(options){
 		 
		var wh={"w":$(window).width(),"h":$(window).height()},//浏览器窗口宽度、高度
 			curWinNum=$("div.myWindow").size(),//当前已打开窗口数量
 		    //默认参数配置
            defaults = {
                   windowTitle: null,                        /* true, false窗口标题*/
                   windowsId: null,                          /* true, false窗口id*/
				   iconSrc:null,
                   windowPositionTop: 'center',              /* Posible are pixels or 'center' 窗口初始位置top*/
                   windowPositionLeft: 'center',             /* Posible are pixels or 'center' 窗口初始位置left*/
                   windowWidth: Math.round(wh['w']*0.6),     /* Only pixels 窗口宽度*/
                   windowHeight: Math.round(wh['h']*0.8),    /* Only pixels 窗口高度*/
                   windowMinWidth: 250,                      /* Only pixels 窗口最小宽度*/
                   windowMinHeight: 250,                     /* Only pixels窗口最小高度 */
                   iframSrc: null,                           /* iframe的src路径*/
                   windowResizable: true,                    /* true, false是否可以resize窗口*/
                   windowMaximize: true,                     /* true, false是否可以最大化窗口*/
                   windowMinimize: true,                     /* true, false是否可以最小化窗口*/
                   windowClosable: true,                     /* true, false是否可以关闭窗口*/
                   windowDraggable: true,                    /* true, false是否拖曳窗口*/
                   windowStatus: 'regular',                  /* 'regular', 'maximized', 'minimized' 打开窗口时显示状态*/
                   windowAnimationSpeed: 500,                /* 动画执行时间*/
                   windowAnimation: false,                   /* true, false 是否启用动画*/ 
				   parentPanel:'body'                        /* 窗口被插入的容器元素*/
				   },
 		    options = $.extend(defaults, options),
		    $newWin=$("#win_"+options['windowsId']), //当前打开的窗口
			//窗口html结构
 		    winHtml=function(options){
				var winHtml="<div class='myWindow' id='win_"+options.windowsId+"' >";
				winHtml+="<div class='winTitle'>";
				winHtml+="<span class='winTitleName'>"+options.windowTitle+"</span>"; 
				winHtml+="<span class='winControlBtn'><a href='#' class='winMinBtn' title='最小化'></a><a href='#' class='winMaxBtn' title='最大化'></a><a href='#' class='winRestore' title='还原'></a><a href='#' class='winCloseBtn' title='关闭'></a></span></div>";
				winHtml+="<div class='winContent'>";
				winHtml+="<div class='loading'>正在加载中</div>";
				winHtml+="<iframe scrolling='auto' frameborder='no' class='iframeApp' name='iframeApp_"+options.windowsId+"' id='iframeApp_"+options.windowsId+"' src=''></iframe>";
				winHtml+="<div class='iframeFix' id='iframeFix_"+options.windowsId+"'></div>";
				winHtml+="</div>";
				winHtml+="</div>";
				return winHtml;
				},
				_self=this;
		 
		 
		//新建窗口,并判断此窗口是否已经存在
		if(!$newWin.size()){ 
		
			$(winHtml(options)).appendTo(options.parentPanel);
			
			var   $newWin=$("#win_"+options['windowsId']),
			      $allWins=$("div.myWindow"),
			      $iframe=$newWin.find("iframe"),
			      $loading=$newWin.find("div.loading"),
				  $wincontent=$newWin.find("div.winContent"),
				  $winTitle=$newWin.find("div.winTitle"),
				  winMaximize_btn=$newWin.find('a.winMaxBtn'),//最大化按钮
				  winMinimize_btn=$newWin.find('a.winMinBtn'),//最小化按钮
		          winClose_btn=$newWin.find('a.winCloseBtn'),//关闭按钮
		          winHyimize_btn=$newWin.find('a.winRestore');//还原按钮
 				  
			//设置窗口位置、大小
			var $topWin=$("div.topWin,div.topWin:hidden"),
			    dxy=Math.floor((Math.random()*200))+(wh['h']-options['windowHeight'])/2, //偏移量
			    zindex=curWinNum?parseInt($topWin.css("z-index"))+1:curWinNum+100,
 				deskWidth=$topWin.width(),
                wLeft=myDesktop.isTypeOf(options['windowPositionLeft'],"Number")?options['windowPositionLeft']+dxy:dxy,
                wTop=myDesktop.isTypeOf(options['windowPositionTop'],"Number")?options['windowPositionTop']+dxy/4:dxy/4,
				
				//初始化iframe函数
				iframe_init=function (){
					var frmSrc = options['iframSrc'];
					if (frmSrc==null || frmSrc=="") {
						frmSrc = "desktop_sub.jsp?iconId=" + options['windowsId'];
					}
					$iframe.attr("src",frmSrc)
					.load(function(){//当iframe加载完毕
						// ie11下，有时iframe中会存在input不能输入的问题，需作获得焦点后select处理才能正常
						var content = $(this).contents();
						content.find("input").each(function () {
						$(this).focus(function () {
						$(this).select();
						});
						});
						content.find("textarea").each(function () {
						$(this).focus(function () {
						$(this).select();
						});
						});
						
 						$loading.hide();
						$(this).css("left",0);
						});
				    };
			
				 
			//初始化窗口
			if(!$(".maxWin").size()){
			$allWins.removeClass("topWin").find("div.iframeFix").show();
			}else{
				$allWins.not(".maxWin").removeClass("topWin").find("div.iframeFix").show();
				}
			
			$newWin
			.addClass("topWin")
			.css({"width":options['windowWidth'],"height":options['windowHeight'],"left":wLeft,"top":wTop,"z-index":zindex})
			.find("div.winContent")
			.css({"width":options['windowWidth'],"height":options['windowHeight']-$winTitle.height()})
			.end()
			.find("div.iframeFix")
			.hide();
			
			//是否显示最大化按钮
			if(!options.windowMaximize){
				 winMaximize_btn.hide();
				}
				
			//是否显示最小化按钮	
			if(!options.windowMinimize){
				 winMinimize_btn.hide();
				}
				
			//是否显示关闭按钮	
			if(!options.windowClosable){
				 winClose_btn.hide();
				}
			
			//是否启用窗口动画
			if(!options.windowAnimation){ //默认无动画效果
				$newWin.show(options.windowAnimationSpeed,function(){iframe_init();});
 			}else{
				
			//启用动画效果
			var o=$("#"+options.windowsId),//获取图标相对文档的位置
			    offset=o.offset();
					
				$newWin
				.css({"left":offset.left,"top":offset.top})
				.animate({
						 top: wTop,
                         left: wLeft
						 },
						 options.windowAnimationSpeed,
						 "easeInOutQuad",
						 function(){
							 iframe_init();
						 });
				}
 			
 			//更新窗口当前位置大小信息
			$newWin.data('winLocation',{
			  'w':options['windowWidth'],
			  'h':options['windowHeight'],
			  'left':wLeft,
			  'top':wTop
			  });
  			
			//多个窗口上下切换
			$allWins.mousedown(function(event){
				                    event.stopPropagation();
									var $topWin=$("div.topWin,div.topWin:hidden"),id=this.id;	
									
								if(!$topWin.is($(this))){
								var maxZindx=$topWin.css("z-index");
								
								$topWin.removeClass("topWin").find("div.iframeFix").show();
								$(this).css("z-index",parseInt(maxZindx)+1).addClass("topWin").find("div.iframeFix").hide();
										
										//更新任务栏图标状态
										myDesktop.taskBar.upTaskTab(id);
									}
									});
			
			$newWin.find("div.iframeFix:hidden").on("click",function(event){
																 event.stopPropagation();	 
																$(this).hide();	 
																	 });
  			
			//启用窗口拖动
			if(options.windowDraggable){ _self.winDrag($newWin);}
				
			//启用拖曳改变窗口大小	
			if(options.windowResizable){ _self.winResize($newWin,[options.windowMinWidth,options.windowMinHeight,wh['w']-wLeft,wh['h']-wTop]);}
			
			//关闭窗口
			winClose_btn.click(function(event){ event.stopPropagation(); _self.winClose($newWin);	});
			
			//最大化窗口
			winMaximize_btn.click(function(event){ event.stopPropagation(); _self.winMaximize($newWin); });
			
			//最小化窗口
			winMinimize_btn.click(function(event){ event.stopPropagation(); _self.winMinize($newWin); });
			
			//还原窗口
			winHyimize_btn.click(function(event){ event.stopPropagation(); _self.winHyimize($newWin); });
			
			//双击标题栏最大化、还原窗口
		    $winTitle.dblclick(function(event){
				 event.stopPropagation();						
				var hasMaximizeBtn=$(this).find(winMaximize_btn);
				if(!hasMaximizeBtn.is(":hidden")){
					winMaximize_btn.trigger("click");
				}else{
					winHyimize_btn.trigger("click");
					}
			});
			
			//当改变浏览器窗口时且窗口处于最大化状态
			$(window).wresize(function(){
				if($newWin.data('windowStatus')=="maximized"){
					 _self.winMaximize($newWin);
				}
			 //更新窗口大小
			 _self.winResize($newWin,[options.windowMinWidth,options.windowMinHeight,$(window).width(),$(window).height()]);
 			  });
 	     
		return $newWin;
		 
		//已经存在窗口
		}else{
			 if($newWin.data('windowStatus')=="minsize"){
 				 $("#taskTab_"+options.windowsId).trigger("click");
				 }
			   }
		}, 
	//拖动窗口
	winDrag:function($newWin){
		var wh={'w':$(window).width(),'h':$(window).height()},
		   _self=this;
		
		$newWin
		.draggable({
				   handle:'div.winTitle',
				   scroll: false
				   })
		.bind("drag",function(event,ui){
					$(this).find("div.iframeFix").show();		  
						  })
		.bind("dragstop", function(event, ui) {
            $(this).find("div.iframeFix").hide();	
											
			//限制窗口拖曳范围
 			if(event.pageY>wh.h-80){
				$(this).css("top",wh.h-80);
 		    }else if(event.pageY<0){
				$(this).css("top",0);  
			}
								  
 			//更新窗口当前位置大小信息
			$newWin.data('winLocation',{
			  'w':$(this).width(),
			  'h':$(this).height(),
			  'left':$(this).css("left"),
			  'top':$(this).css("top")
			  });
 								  
		   });
		},
	//拖曳改变窗口大小
	winResize:function($newWin,arr){
		var _self=this,wintitHeight=$newWin.find(".winTitle").height();
		
		$newWin
		.resizable({
				   minWidth:arr[0],
				   minHeight:arr[1],
 				   containment:'document',
				   maxWidth:arr[2],
				   maxHeight:arr[3],
				   autoHide:true,
				   handles:"n, e, s, w, ne, se, sw, nw, all"
				   })
		.css("position","absolute")		   
		.bind("resize", function(event, ui) {
						var h=$(this).innerHeight(),w=$(this).innerWidth();							  
						$(this)
						.find("div.winContent")
						.css({"width":w,"height":h-wintitHeight})
						.end()
						.find("div.iframeFix")
						.show();	
						
						$("div.myWidget")
						.find("div.iframeFix")
						.show();
 		     })
		.bind("resizestop",function(event,ui){
 			$(this).find("div.iframeFix").hide();
			$("div.myWidget").find("div.iframeFix").hide();
			var wh=ui.size,
			    lt=ui.position;
			
			//更新窗口当前位置大小信息
			$newWin.data('winLocation',{
			  'w':wh.width,
			  'h':wh.height,
			  'left':lt.left,
			  'top':lt.top
			  });
			  
			  
 						  });
		},
	//获取当前最顶层窗口对象		
	findTopWin:function($win,maxZ){
		var topWin;
		$win.each(function(index){
 						   if($(this).css("z-index")==maxZ){
							   topWin=$(this);
							   return false;
							   } 
 						   });
		return topWin;  
		},
		
	/*
	 * 20171125 fgf 关闭当前窗口
	 * 窗口页面中的调用方法：
	 * 	if (window.top.myDesktop) {
	 *		window.top.myDesktop.myWindow.winCloseCurrent();	
	 *	}
	 */
	winCloseCurrent:function() {
		$curWin = $(".myWindow.topWin");
		this.winClose($curWin);	
		return;
	},
 	//关闭窗口
	winClose:function($newWin){
		var $topWin=$("div.myWindow,div.myWindow:hidden"),
			nextWin=this.findTopWin($topWin,parseInt($topWin.css("z-index"))-1);
											
			nextWin==undefined?"":nextWin.addClass("topWin");
			$newWin.remove();
			
			//删除对应任务栏图标
			myDesktop.taskBar.removeTaskTab($newWin.attr("id"));
			if(nextWin!==undefined){
			myDesktop.taskBar.upTaskTab(nextWin.attr("id"));
			}
			
		if(!$("div.maxWin").size()){	
		$("#desktopsContainer").css("z-index",50);	
		}else{
			$("div.maxWin").addClass("topWin");
			}
		},
	//最大化窗口
	winMaximize:function($newWin){
		var wh={'w':$(window).width(),'h':$(window).height()},
		    winHyimize_btn=$newWin.find("a.winRestore"),
		    winMaximize_btn=$newWin.find("a.winMaxBtn"),
			navBar=$("#navBar"),
			leftBar=$("#leftBar"),
			rightBar=$("#rightBar"),
			topBar=$("#topBar"),
			winTitHeight=$newWin.find("div.winTitle").height(),
			l=0,t=0;
		
		var slideWidth=topBar.is(":hidden")?leftBar.width():0,
		    topHeight=topBar.is(":hidden")?navBar.height():topBar.height()+navBar.height();	
			
		if(!leftBar.is(":hidden")){
			l=slideWidth*-1;
			t=topHeight*-1;
		}else{
			l=0;
			t=topHeight*-1;
			}
  		    
		$newWin
 		.data("windowStatus","maximized")
 		.draggable( "disable" )
		.resizable("disable")
		.removeClass("ui-state-disabled")
		.addClass("maxWin")
 		.css({"width":wh['w']-2,"height":wh['h'],"left":l,"top":t})
		.find("div.winContent")
		.css({"width":wh['w']-2,"height":wh['h']-winTitHeight})
		.find("div.iframeFix").hide();
  		
		winMaximize_btn.hide();
		winHyimize_btn.css("display","inline-block");
		$("#desktopsContainer").css("z-index",800);
		},
	//还原窗口
	winHyimize:function($newWin){
		var winInfo=$newWin.data("winLocation"),
		    winHyimize_btn=$newWin.find("a.winRestore"),
			winTitHeight=$newWin.find("div.winTitle").height(),
		    winMaximize_btn=$newWin.find("a.winMaxBtn");
			
			$newWin
			.data("windowStatus","regular")
			.draggable( "enable" )
			.resizable("enable")
			.removeClass("maxWin")
			.css({"width":winInfo.w,"height":winInfo.h,"left":winInfo.left,"top":winInfo.top})
			.find("div.winContent")
			.css({"width":winInfo.w,"height":winInfo.h-winTitHeight});
									
			winHyimize_btn.hide();
			winMaximize_btn.show();
			$("#desktopsContainer").css("z-index",50);
		},
	//最小化窗口	
	winMinize:function($newWin){
		var p=$("div.desktop").index($newWin.parent());
 		
		$newWin.data({"oldLeft":$newWin.css("left"),"index":p}).css("left",-99999).addClass("hideWin");
		//$newWin.data("windowStatus","minsize");
		if($("div.myWindow").size()>1){
			$newWin.removeClass("topWin");
			}
			
 		nextWin=this.findTopWin($("div.myWindow,div.myWindow:hidden"),parseInt($newWin.css("z-index"))-1);
		
		if(nextWin!==undefined){
			nextWin.addClass("topWin").css("z-index",parseInt($newWin.css("z-index"))+1);
			myDesktop.taskBar.upTaskTab(nextWin.attr("id"));
			}else{
 				//删除所有tab的taskCurrent样式
		        $("div.taskTab").removeClass("taskCurrent");
				}
		$("#desktopsContainer").css("z-index",50);		
		} 	
	};

   	
//创建wallpaper命名空间
/*背景平铺三种类型,1背景拉伸,2背景居中,3背景自适应屏幕*/
myDesktop.wallpaper={
	init:function(imgUrl,type){
		var _self=this;
		$("body").data("wallpaperType",type);
		
		if(type!=3){
		 myDesktop.getImgWh(imgUrl,function(imgWidth,imgHeight){
			 $("#wallpaper").html("<img src='"+imgUrl+"' />");
			 _self.setWallpaper(imgWidth,imgHeight,type);
			 
			 $(window).wresize(function(){
				 _self.setWallpaper(imgWidth,imgHeight,type);
				 });	
					});
					
		 }else{ //背景平铺
 		   $("#wallpaper").css({"background":"url("+imgUrl+") repeat 0 0","height":$(window).height()});
		 }
    
   },
  setWallpaper:function(imgWidth,imgHeight,type){
	  var winW=$(window).width(),
		  winH=$(window).height();
		  
	  	if(type==1){//如果是拉伸
			$("#wallpaper").find("img").css({'width':winW,'height':winH});
			}
											
		if(type==2){//如果是居中
			if(imgWidth>winW){
			$("#wallpaper").find("img").css({'width':imgWidth,'height':imgHeight,'margin-left':(imgWidth-winW)/2+"px",'margin-top':(imgHeight-winH)/2+"px"});
			}else{
			$("#wallpaper").find("img").css({'width':imgWidth,'height':imgHeight,'margin-left':-(imgWidth-winW)/2+"px",'margin-top':-(imgHeight-winH)/2+"px"});
			}
 		}
	  },
  updateWallpaper:function(imgSrc){
	  //alert(imgSrc);
	  var type=$("body").data("wallpaperType");
	  this.init(imgSrc,type);
	  
	  //保存Wallpaper信息
	  $.ajax({
		  //这里添加自己的代码
		  });
	  }	  		
		};

 		
//创建desktop命名空间
myDesktop.desktop={
	init:function(iconData,options){
		//默认配置
		var defaults={
				arrangeType:1,       //图标排列类型,1竖排,2横排
				iconMarginLeft:30,   //图标左边距
				iconMarginTop:20,    //图标上边距
				dragStop:function(ret){}
 				};
				
		var options = $.extend(defaults, options);
		
		desktopNames = options.desktopNames;
 		
		//存储desktop配置
		$("body").data("desktopCofig",options);
 		
		var _self=this;
 			
		//创建初始化桌面图标
 		_self.desktopIconInit(iconData);
			
		var desktops=$("div.desktop"), 
   			desktopNum=desktops.size(),
			innerDesktop=$("div.innerDesktop");
				
		//默认显示第一个桌面 
		desktops.eq(0).addClass("currDesktop")
		.css("left",0)
		.find("div.innerDesktop").fadeIn(3000);
  			
 		if(desktopNum>1){ //是否显示桌面控制栏
			myDesktop.desktopBar.init(desktopNum);
 				
			//拖动桌面滑动切换桌面
 			var dxStart,dxEnd,tabs=$("#navBar").find("span > a");
				
			desktops
			.draggable({
					axis:'x',
					scroll: false,
					start:function(event,ui){
						$(this).css("cursor","move");
						dxStart=event.pageX;
						},
					stop:function(event,ui){
						$(this).css("cursor","inherit");
						dxEnd=event.pageX;
						
						var dxCha=dxEnd-dxStart
						    ,deskIndex=desktops.index($(this));
						 
						//左移
						if(dxCha < -150 && deskIndex<desktopNum-1){
  							tabs.eq(deskIndex+1).trigger('click');
						//右移
						}else if(dxCha > 150 && deskIndex>0){
							tabs.eq(deskIndex-1).trigger('click');
 						}else{
							 $(this).animate({'left':0},500,"easeInOutQuint");
							} 
						    }
								}); 
 				}
			
 			//设置桌面区域大小和排列桌面图标
			_self.arrangeIcons(desktops,options);
			 
 			//如果窗口大小改变，则重新排列图标
		    $(window).wresize(function(){
							 _self.arrangeIcons(desktops,options);
    								   });
			
   			//拖曳图标，在桌面空白处释放，插入最后
			/*innerDesktop.droppable({
				scope:'a',
                drop: function(event,ui){
				 	
 				ui.draggable
 				.addClass("desktopIcon")
				.insertBefore($(this).find(".addIcon")); 
                
				_self.arrangeIcons(desktops,options);
 					}
                  });*/		
			
	     //桌面图标效果初始化
		 _self.clickInit();		
			
		//单击添加应用按钮
		/*desktops
		.find("div.addIcon")
		.click(function(){
		$("#win_appShop").remove();
		$("#taskTab_appShop").parent().remove();
		
		var	p=$(this).parents("div.desktop");
 			myDesktop.myWindow.init({
						'iconSrc':'icon/icon11.png', 	
					   'windowsId':'appShop', 	
					   'windowTitle':'添加应用',
					   'iframSrc':'appShop.html',
					   'windowWidth':600,
					   'windowHeight':600,
					   'parentPanel':p
			       });
			
		//添加到状态栏
		if(!$("#taskTab_appShop").size()){
 		myDesktop.taskBar.addTask("appShop","添加应用","icon/icon11.png");
		} 
		});
		*/
 		},
	//桌面图标效果初始化	
	clickInit:function(){
		 var desktops=$("div.desktop"),
		     icons=desktops.find("div.desktopIcon"),
			 innerDesktop=$(".innerDesktop"),
			 o=$("body").data("desktopCofig"),
			 _self=this;
		   
		   //鼠标经过图标
			icons
			.on({
			mouseenter: function(event){
				event.stopPropagation();
				$(this).addClass("desktopIconOver");
				},
			mouseleave: function(event){
				event.stopPropagation();
				$(this).removeClass("desktopIconOver");
				}
			})
 			 
			//单击图标打开窗口
			.not(".addIcon")
			.on("click",function(event){
							event.stopPropagation();
							var data=$(this).data("winAttrData");
 							
							//打开的是窗口
							if(!data.isWidget){
							
							//如果窗口未打开	
							if(!$("#taskTab_"+data.windowsId).size()){
									
							var	p=$(this).parents("div.desktop");
							data.parentPanel=p;	
							myDesktop.myWindow.init(data);
							
							//添加到状态栏
							myDesktop.taskBar.addTask(data.windowsId,data.windowTitle,data.iconSrc);
							
							}else{
								//如果窗口已打开切最小化
								if($("#win_"+data.windowsId).has("hideWin")){
									$("#taskTab_"+data.windowsId).trigger("click");
									}
 								}
							
							}
							//小工具
							else{
								myDesktop.widget.init({
													  id:data.windowsId,
													  width:data.windowWidth,
													  height:data.windowHeight,
													  title:data.windowTitle,
													  isDrag:true,
													  iframeSrc:data.iframSrc,
													  top:data.top,
													  left:data.left,
													  right:data.right,
													  parentTo:".desktop:first"
													  });
								}
							
							})
			/*.draggable({
					helper: "clone",
					scroll:false,
 					scope:'a',
					appendTo: 'body' ,
					zIndex:91,
					start: function(event, ui) {
 						ui.helper.removeClass("desktopIconOver");
						} 
					})
					
		    .droppable({
				scope:'a',
                drop: function(event,ui) {
 				ui.draggable
 				.addClass("desktopIcon")
				.insertBefore($(this)); 
                
				_self.arrangeIcons(desktops,$("body").data("desktopCofig"));
				
				}
           })*/;	
			
		//桌面图标拖曳排序	
		innerDesktop.sortable({helper:'clone',
		          items:'div.desktopIcon:not(.addIcon)',
			  scroll:false,
			  'appendTo':innerDesktop.parents("div.desktop"),
			  stop: function() {
					var ids = "";
					$(this).children("div").each(function () {
						if (ids=="")
							ids = $(this).attr("id");
						else
							ids += "," + $(this).attr("id");
					});
					
					// alert(ids);
					
					$.ajax({
						type: "post",
						url: "admin/slide_menu_main.jsp",
						data: {
							op: "sort",
							// groupIndex暂无用
							groupIndex: $("div.desktop").index($("div.currDesktop")),
							ids: ids
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							// $('#sortable').showLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="0") {
								// jAlert(data.msg, "提示");
							}
							else {
								// jAlert_Redirect(data.msg, "提示", "slide_menu_main.jsp");
							}
						},
						complete: function(XMLHttpRequest, status){
							// $('#sortable').hideLoading();
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});

				}			
			});
        innerDesktop.disableSelection();	
		
		},	    	
 	//桌面图标初始化
	arrangeIcons:function(desktops,options){
		var desktopsContainer=$("#desktopsContainer"),
		    desktopContainer=$("#desktopContainer"),
		    bottomBarBgTask=$("#bottomBarBgTask"),
			navBar=$("#navBar"),
			topBar=$("#topBar"),
			leftBar=$("#leftBar"),
			rightBar=$("#rightBar"),
			desktops=$("div.desktop"),
			innerDesktop=$("div.innerDesktop"),
			outerDesktop=$("div.outerDesktop"),
 			desktopNum=desktops.size(),
			winW=$(window).width(),
			winH=$(window).height(),
 			topBarH=topBar.is(":hidden")?0:topBar.height();
			
		//设置桌面外围区域大小
		var slideWidth=leftBar.is(":hidden") && rightBar.is(":hidden")?0:leftBar.width(),
		    topHeight=topBar.is(":hidden")?navBar.height():topBar.height()+navBar.height();
			
		var sw=winW-slideWidth,
		    sh=winH-topBarH-bottomBarBgTask.height()-10;
 		 
		desktopsContainer.css({'width':sw,'height':0,'left':slideWidth,'top':topHeight});
 		
		if(!rightBar.is(":hidden")){
		desktopsContainer.css({'width':sw,'height':0,'left':0,'top':topHeight});
		}
		
		desktopContainer.css({'width':sw,'height':sh});
		desktops.css({'width':sw,'height':sh});
		//innerDesktop.css({'width':sw,'height':sh});
  		outerDesktop.css({'width':sw,'height':sh}).eq(0).width(sw-390);
   		
		//排列图标 
		desktops.each(function(index){
			var did="#desktop"+(index+1);
			
			$(did).find(".outerDesktop").niceScroll(did+" .innerDesktop",{touchbehavior:false,cursorcolor:"#666",horizrailenabled:true,cursoropacitymax:0.8,cursorborder:"1px solid #ccc",horizrailenabled:false,zindex:0});
  			$(window).wresize(function(){$(did).find(".outerDesktop").getNiceScroll().resize();});
			
		    var desktop=$(this),
			desktopIcon=desktop.find("div.desktopIcon"),
			iconW=desktopIcon.width(),
			iconH=desktopIcon.height(),
			iconNum=desktopIcon.size();
			gH=iconH+options.iconMarginTop,//一个图标总高度，包括上下margin
			gW=iconW+options.iconMarginLeft,//图标总宽度,包括左右margin
			maxCols=Math.floor(outerDesktop.width()/gW),
			maxRows=Math.floor(outerDesktop.height()/gH),
			rows=Math.floor(outerDesktop.height()/gH),
			cols=Math.ceil(iconNum/rows),
			curcol=0,currow=0;
  		 
		 if(cols>maxCols){
			 rows=Math.ceil(iconNum/maxCols);
			 desktop.find(".innerDesktop").css({'height':rows*gH});
			 }
		 	
		 //存储当前总共有多少桌面图标
		 desktop.data('deskIconNum',iconNum);
         
		 //如果是竖排
		 if(options.arrangeType==1){
		 desktopIcon
		 .css({
				   "position":"absolute",
				   "margin":0,
				   "left":function(index,value){
					       var v=curcol*gW+30;
					           if((index+1)%rows==0){
							       curcol=curcol+1;
					              }
						   return v;	 
 						},
					"top":function(index,value){
 							var v=(index-rows*currow)*gH+20;
								if((index+1)%rows==0){
									 currow=currow+1;
									}
						    return v;
							}});
			}
			
		//如果是横排	
		if(options.arrangeType==2){
			//desktopIcon.css({"float":"left","margin-left":options.iconMarginLeft,"margin-top":options.iconMarginTop});
			}	
			
		 });
 		
		},
	creatIcon:function(o){
			var str="";	
			str+="<div class='desktopIcon' id='"+o.windowsId+"'><span class='icon'>";
			if(o.txNum){
				str+="<div class='txInfo'>"+o.txNum+"</div>";
				}
			str+="<img src='"+o.iconSrc+"' title='"+o.windowTitle+"'/></span><div class='text'><span>"+o.windowTitle+"</span><s></s></div></div>";
			return str;
			},	
	//初始化创建桌面图标	
	desktopIconInit:function(data){
		var html="",_self=this;
			
		 for(var a in data){
			 html+="<div class='desktop' id='"+a+"'><div class='outerDesktop'><div class='innerDesktop'>";
			 var arr=data[a];
			 for(var i=0;i<arr.length;i++){
				 html+=_self.creatIcon(arr[i]);
				 }
 			 //html+="<div class='desktopIcon addIcon'><span class='icon'><img src='theme/default/images/add_icon.png'/></span><div class='text'><span>添加</span><s></s></div></div></div></div></div>";
			 html+="</div></div></div>";
			 }
			 
		$("#desktopContainer").html(html);
		
		//给每个图标附加窗口属性数据
		for(var a in data){
			var arr=data[a];
			for(var i=0;i<arr.length;i++){
				$("#"+arr[i].windowsId).data("winAttrData",arr[i]);
				}
			}	 
		},
	//这个是给桌面添加图标用的，调用方法在appShop.html里面有	
	addApp:function(appData){
		if(!$("#"+appData.windowsId).size()){
			
		var p=$("div.currDesktop").find("div.addIcon"),_self=this;
 		var html=_self.creatIcon(appData);
				 
		//插入图标
		p.before(html);
				 
		//附加数据给应用图标
		var thisApp=$("#"+appData.windowsId),config=$("body").data("desktopCofig");
		    thisApp.data("winAttrData",appData); 
		
		//更新桌面布局	
		_self.arrangeIcons(p,config);
		_self.clickInit();
		
		//图标添加到的桌面id并保存,使用ajax方法
		//$.ajax() 
		   }
		}	
	};
	

//创建状态栏命名空间
myDesktop.taskBar={
	init:function(){
		
		//存储任务栏jq元素对象
        this.taskData();
		    
 		var taskBarData=$("body").data("taskBar"),
		    taskNextBox=taskBarData.taskNextBox,
			taskPreBox=taskBarData.taskPreBox,
			ww=taskBarData.ww,
			taskInnnerBlock=taskBarData.taskInnnerBlock,
			taskOuterBlock=taskBarData.taskOuterBlock,
			ow=ww-taskNextBox.outerWidth(true)*2,
		    _self=this;
				   
		//taskOuterBlock.width(ow);
		
		$(window).wresize(function(){
								  var mw=$(window).width()-taskNextBox.outerWidth(true)*2,tw=0;
								  tw=taskOuterBlock.width()<=mw?taskOuterBlock.width():mw;
 								  taskOuterBlock.width(tw);
								   });
		
		function taskMove(a){
			taskInnnerBlock.animate({"margin-right":'+='+a},1000);
			}
		
		taskNextBox.on("click",function(){
			var mr=taskInnnerBlock.css("margin-right"),
			    mr=parseInt(mr),
				taskTabWidth=$("body").data("tabWidth");
				
			if(Math.abs(mr)>taskTabWidth){	
			taskMove(taskTabWidth);
			}else{
				taskMove(Math.abs(mr));
				}
			
			});
			
		taskPreBox.on("click",function(){
			var ml=taskInnnerBlock.position(),
			    ml=Math.abs(ml.left),
				taskTabWidth=$("body").data("tabWidth");
				
			if(ml>taskTabWidth){
			taskMove(taskTabWidth*-1);
			}else{
				taskMove(ml*-1);
				}
				
			});	
				   
		},
	taskData:function(){
				$("body").data("taskBar",{
					   taskBlock:$("#taskBlock"),
		               taskInnnerBlock:$("#taskInnnerBlock"),
		               taskOuterBlock:$("#taskOuterBlock"),
			           taskNextBox:$("#taskNextBox"),
			           taskPreBox:$("#taskPreBox"),
			           ww:$(window).width(),
			           wh:$(window).height()
					   });
		},
	upTaskTab:function(id){
		id="" + id; // 如果ID是数字，需转换为字符，否则在split时会出错
		var str=id.split("_").slice(1);
		//删除所有tab的taskCurrent样式
		$("div.taskTab").removeClass("taskCurrent");
		$("#taskTab_"+str).parent().addClass("taskCurrent"); 
		    
 		},
	removeTaskTab:function(id){
		var str=id.split("_").slice(1),
		    taskBarData=$("body").data("taskBar"),
 			taskTabWidth=$("body").data("tabWidth");
			 
		    $("#taskTab_"+str).parent().remove();
			var taskTabNum=$("div.taskTab").size(),
			    maxTabNum=$("body").data("maxTabNum");
			
			taskBarData.taskInnnerBlock.width(taskTabNum*taskTabWidth);
			if(taskTabNum<=maxTabNum){
				taskBarData.taskNextBox.hide();
			    taskBarData.taskPreBox.hide();
				}
		},			
	addTask:function(id,text,icon){
		var taskBarData=$("body").data("taskBar"),
		    taskNextBox=taskBarData.taskNextBox,
			taskPreBox=taskBarData.taskPreBox,
			ww=taskBarData.ww,
			taskInnnerBlock=taskBarData.taskInnnerBlock,
			taskOuterBlock=taskBarData.taskOuterBlock,
			ow=ww-taskNextBox.outerWidth(true)*2,
		    _self=this;
				
		//删除所有tab的taskCurrent样式
		$("div.taskTab").removeClass("taskCurrent");
		
		var taskTabHtml="<div class='taskTab taskCurrent'><a href='#'  title='"+text+"' class='taskItem' id='taskTab_"+id+"'><div class='tabIcon'><img src='"+icon+"'/></div><div class='tabTxt'><span>"+text+"</span></div></a></div>";
		
		$(taskTabHtml).prependTo(taskInnnerBlock);
		
		var taskTab=$("div.taskTab"),
		    tabNum=taskTab.size(),
 			tabWidth=taskTab.width();
			maxTabNum=Math.floor((ww-taskNextBox.outerWidth()*2)/tabWidth);
 			
			$("body").data({"tabWidth":tabWidth,"maxTabNum":maxTabNum});
			if(tabNum*tabWidth<=ow){
			taskOuterBlock.width(tabNum*tabWidth);
			}else{
				taskOuterBlock.width(ow);
				}
		    taskInnnerBlock.width(tabNum*tabWidth);
		   
		   //单击tab
		   $("#taskTab_"+id).on("click",function(){
			var win=$("#win_"+id),
		    left=win.data("oldLeft"),
			i=$("div.desktop").index(win.parent()),
			j=$("div.desktop").index($("div.currDesktop"));

		    if(win.hasClass("hideWin")){
		     win.css("left",left).removeClass("hideWin");
 		    }
							
			_self.upTaskTab(id);

			win.trigger("mousedown");
			//alert(win.data("windowStatus"));
			if(win.data("windowStatus")=="maximized"){
				$("#desktopsContainer").css("z-index",800);
				}
			
			if(i!=j){
		      myDesktop.desktopBar.moveDesktop(i);
		      }
			    
			   });
		   
 		   //如果tab超过最大显示数目，则显示左右移动按钮		
		   if(tabNum>maxTabNum){
			  taskNextBox.show();
			  taskPreBox.show();
  		    }
		} 
	}

 

//创建widget窗口
myDesktop.widget={
	init:function(options){
		
		var defaults={
			id:"",
			width:210,
			height:210,
			title:"小工具",
			isDrag:true,
			iframeSrc:"",
			top:0,
			left:0,
			right:'auto',
			bottom:'auto',
			parentTo:"body"
			},
			_self=this;
			
		var o = $.extend(defaults, options);
		
		// 如果存在，则删除并重新显示
		if ($("#myWidget_"+o.id).size()) {
			$("#myWidget_"+o.id).remove();
		}
		
		if(!$("#myWidget_"+o.id).size()){
			  
			$(o.parentTo).append(_self.widgetHtml(o));
 			
			var newWidget=$("#myWidget_"+o.id)
			    widgetTitle=newWidget.find("div.widgetTitle"),
				widgetClose=newWidget.find("a.widgetClose"),
				widgetCon=newWidget.find("div.widgetCon"),
				zindex=$("div.myWidget").size()+5;
				
				newWidget
				.css({"width":o.width,"height":o.height,"left":o.left,"right":o.right,"top":o.top,"bottom":o.bottom,"z-index":zindex})
 				.hover(function(){
 					$(this).find(".innerWidgetTitle").show();
					},function(){
						$(this).find(".innerWidgetTitle").hide();
						})
				.find("iframe")
				.attr("src",o.iframeSrc)
				.load(function(){
							 newWidget.find("div.loading").hide();
 							   });
				
				widgetCon.height(o.height-widgetTitle.height());
				
				_self.postWidgetStatus("openWidget", o.id, options.left, options.top);
						
			    if(o.isDrag){			
				newWidget.draggable({
 					scroll:false,
					stop:function(){
						var l=parseInt($(this).css("left")),tw=$(this).width();
						 
						$(this).css({"left":"auto","right":$(window).width()-l-tw});
						
						var widgetId = newWidget.attr('id').substring('myWidget_'.length);
						_self.postWidgetStatus("moveWidget", widgetId, l, parseInt($(this).css("top")));
						
						
						}
					});
					}
			
			widgetClose.click(function(){
									   newWidget.remove();
									   
									   var widgetId = newWidget.attr('id').substring('myWidget_'.length);
									   
									   _self.postWidgetStatus("closeWidget", widgetId, 0, 0);
								});
					
			}
  	  		
 		},
		
	// 保存widget状态
	postWidgetStatus:function(op, widgetId, left, top) {
		$.ajax({
			type: "post",
			url: "mydesktop_do.jsp",
			data: {
				op: op,
				left: left,
				top: top,
				id: widgetId
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				// $('#sortable').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="0") {
					// jAlert(data.msg, "提示");
				}
				else {
					// jAlert(data.msg, "提示");
				}
			},
			complete: function(XMLHttpRequest, status){
				// $('#sortable').hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});	
	},
		
	widgetHtml:function(o){
		return "<div class='myWidget' id='myWidget_"+o.id+"'><div class='widgetTitle'><div class='innerWidgetTitle'><b>"+o.title+"</b><span class='widgetBtn'><a href='#' class='widgetClose'></a></span></div></div><div class='widgetCon'><iframe src='#' allowtransparency='true' frameborder='0' scrolling='no' width='100%' height='100%'></iframe><div class='loading'>正在加载中...</div><div class='iframeFix' id='iframeFix_"+o.id+"'></div></div></div>";
		 
		}	
	};
 

//登陆窗口
myDesktop.login={
	init:function(src){
		var loginHtml="<div class='login_box'><div id='ui_boxyClose' class='ui_boxyClose'></div><div class='login_logo'></div><iframe src='#' frameborder='0' width='380' height='258' scrolling='no'></iframe></div>",
		    loginMark="<div class='ui_maskLayer'></div>";
		
		$("body").append($(loginMark));
		$("body").append($(loginHtml));
		
		var login_box=$("div.login_box"),ui_boxyClose=$("#ui_boxyClose");
		
		login_box
		.draggable({
					scroll:false,
					containment:'parent',
					handle:".login_logo"
					})
		.find("iframe")
		.attr("src",src);
		
		ui_boxyClose.click(function(){
			$("div.login_box,div.ui_maskLayer").remove();  
										  });
	},
	close:function() {
		$("div.login_box,div.ui_maskLayer").remove();		
	}
		
};

//顶部栏
myDesktop.topBar={
	init:function(){
		var topBar=$("#topBar"),
		     _self=this;
			 
			 topBar.find("img")
			 .hover(function(){
							 var src=this.src;
							 this.src=src.replace(".png","Over.png");
							 },function(){
								 var src=this.src;
							     this.src=src.replace("Over.png",".png");
								 });
		
		//顶部图标点击打开对应窗口
		$("#home").click(function(){
								  //在这里写你的代码
								  });
		//其它图标如上写在这里就行
		}
	};

//消息框
myDesktop.infoBar={
	init:function(infoData){
		var infoBlock=$("#infoBlock"),
		    newinfo=$("div.infoList"),
			zcount=$("#zcount"),
			messageBubble=$("#messageBubble"),
			weiduInfo=$("#messageBubble_bubbleMsgList_userCount"),
			bubbleMsgList=$("#messageBubble_bubbleMsgList"),
			infoList=$("#messageBubble_bubbleMsgList_ul"),
			_self=this;
		    
			//如果有最新消息
			if(infoData.length){
				infoBlock.show();
				_self.updateInfo(infoData);
				}
			
			infoBlock
			.on("mouseover",function(event){
				event.stopPropagation();
 				bubbleMsgList.show();
				})	
 			.draggable({
 				scroll:false,
				containment:"body",
				handle:$("div.infoC")
			 });
			
 			 myDesktop.contextMenu(infoBlock,[],"infoBlock",10);
			
			bubbleMsgList
			.on("mouseover",function(event){
				event.stopPropagation();
				$(this).show();
				});
 			
			$("body").on("mouseover",function(){
				bubbleMsgList.hide();
				});
				
			infoList.find("a")
			.on("click",function(){
				var id=this.id;
				if (id=="iKnow") {
					var ids = "";
					for (i=0; i<infoData.length; i++) {
						if (ids=="")
							ids = infoData[i].id;
						else
							ids += "," + infoData[i].id;
					}
					$.ajax({
						type: "post",
						url: "message_oa/iknow.jsp",
						data: {
							msgIds: ids
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							;
						},
						success: function(data, status){
							
						},
						complete: function(XMLHttpRequest, status){
							$('#infoBlock').hide();		
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});					
					
					
					return;
				}
				else if (id=="iClose") {
					$('#infoBlock').hide();
					return;
				}
						
				if(!$("#win_newInfo").size()){
				myDesktop.myWindow.init({
													  'windowsId':"newInfo",
													  'windowWidth':800,
													  'windowHeight':500,
													  'windowTitle':'消息查看',
													  'iconSrc':'icon/icon0.png', 
													  'iframSrc':'message_oa/message_ext/showmsg.jsp?id='+id,
 													  'parentPanel':".currDesktop"
													  });
                //添加到状态栏
				if(!$("#taskTab_newInfo").size()){
							myDesktop.taskBar.addTask("newInfo",'消息查看','desktop/icon/default.png');
							}
				}else{
					$("#win_newInfo").trigger("mousedown").find("iframe").attr("src","message_oa/message_ext/showmsg.jsp?id="+id);
					}
				});	
				
			//打开消息管理器	
			messageBubble.on("click",function(){
 				
				myDesktop.myWindow.init({
													  'windowsId':"infoControl",
													  'windowWidth':800,
													  'windowHeight':500,
													  'windowTitle':'消息管理器',
													  'iconSrc':'icon/icon0.png', 
													  'iframSrc':'message_oa/message_ext/message.jsp',
 													  'parentPanel':".currDesktop"
													  });
                //添加到状态栏
				if(!$("#taskTab_infoControl").size()){
							myDesktop.taskBar.addTask("infoControl",'消息管理器','desktop/icon/default.png');
							}
				});		
		},
	updateInfo:function(infoData){
		var infoList=$("#messageBubble_bubbleMsgList_ul"),
		    newinfo=$("div.infoList"),
			zcount=$("#zcount"),
		    weiduInfo=$("#messageBubble_bubbleMsgList_userCount"),
			infoHtml="";
		
		newinfo.html(infoData[infoData.length-1].info);
		zcount.text("("+infoData.length+")");
		weiduInfo.text(infoData.length);
		
		for(var i=0;i<infoData.length;i++){
			infoHtml+="<li><a href='javascript:void(0)' id='"+infoData[i].id+"'>"+infoData[i].info+"</a></li>";
			}
			
		infoHtml += "<li style='text-align:center'><a id='iKnow' href='#' style='display:inline'>我知道了</a>&nbsp;&nbsp;<a id='iClose' href='javascript:;' onclick='' style='display:inline'>关闭窗口</a></li>";
			
		infoList.html(infoHtml);	
		}
	};

	
//创建桌面控制栏desktopBar
myDesktop.desktopBar={
	init:function(desktopNum){
 		
		var navBar=$("#navBar"),
 		    nav="",
			desktops=$("div.desktop"),
		    bottomBarBgTask=$("#bottomBarBgTask"),
			_self=this;
		
		for(var i=0;i<desktopNum;i++){
			var desktopName = eval("desktopNames.desktop" + (i+1) + "_name");
			// i==0?nav+="<a href='#' class='currTab' title='桌面"+(i+1)+"'>"+(i+1)+"</a>":nav+="<a href='#' title='桌面"+(i+1)+"'>"+(i+1)+"</a>";
			i==0?nav+="<a href='#' class='currTab' title='"+ desktopName +"'>"+(i+1)+"</a>":nav+="<a href='#' title='"+desktopName+"'>"+(i+1)+"</a>";
			}
 		
 		navBar
		.find("span")
		.html(nav) 
		.end()
 		.css("margin-left",(navBar.width()+20)*-1/2)
		.draggable({
 					scroll:false,
					containment:"body"
						});
  		
		//单击tab切换桌面
 		var tabs=navBar.find("span > a");
		    
		tabs
		.on("click",function(){
			_self.moveDesktop(tabs.index($(this)));
 			})
		.droppable({
			scope:'a',
            over:function(event,ui){
  					_self.moveDesktop(tabs.index($(this)));
 					}
			});	
		
		//单击头像，弹出登陆框
		$("#navbarHeaderImg").click(function(){
						// myDesktop.login.init("desktop/login.jsp");			 
						addTab('控制面板', 'user/control_panel.jsp');
							  });
		//单击全局视图
		$("a.indicator_manage").click(function(){
						$("#appManagerPanel").css("top",0);
 						$("#desktopWrapper").hide();
 								 });
		
 		},
	moveDesktop:function(i){
		var navBar=$("#navBar"),
		    tabs=navBar.find("span > a"),
		    desktops=$("div.desktop"),
			innerDesktop=$("div.innerDesktop");
		
		innerDesktop.hide();
		desktops.find(".myWindow").hide();
 		
		desktops.eq(i).animate({left:0}, 500,"easeInOutQuint").find(".myWindow").show();
	    innerDesktop.eq(i).show();
 		
		tabs.removeClass("currTab").eq(i).addClass("currTab");
		desktops.removeClass("currDesktop").eq(i).addClass("currDesktop");	
		
		for(var j=0;j<desktops.size();j++){
			desktops.eq(j).css('left',j>i?'2000px':'-2000px'); 
			 }
  		}	
	};

//全局视图
myDesktop.appManagerPanel={
	init:function(){
		var appManagerPanel=$("#appManagerPanel"),
		    aMg_close=$(".aMg_close"),
			aMg_dock_container=$(".aMg_dock_container"),
			aMg_folder_container=$(".aMg_folder_container"),
			aMg_folder_innercontainer=$(".aMg_folder_innercontainer"),
 			aMg_prev=$("#aMg_prev"),
			aMg_next=$("#aMg_next"),
			wh=$(window).height(),
			ww=$(window).width(),
			deskTopNum=$("div.desktop").size(),
			dhtml="",
			_self=this;
			
			aMg_folder_container.height(wh-aMg_dock_container.height());
  			
			//复制侧边栏到全局视图
			//aMg_dock_container.append($("#default_app").clone(true));
			
			function amgClose(){
				appManagerPanel.css("top","-9999px");
 				$("#desktopWrapper").show();
				aMg_folder_innercontainer.css("margin-left",0);
				myDesktop.desktop.arrangeIcons($("div.desktop"),$("body").data("desktopCofig"));
				}
				
			aMg_close.click(function(){
									 amgClose();
									 });
			
			for(var i=0;i<deskTopNum;i++){
				dhtml+="<div class='folderItem folderItem_turn' id='folder_"+i+"'><div class='folder_bg'>"+(i+1)+"</div><div class='folderOuter'><div class='folderInner' style='overflow: hidden;'></div></div><div class='aMg_line_y'></div></div>";
 				}
				
		   	aMg_folder_innercontainer.html(dhtml);
			
			var folderItem=$("div.folderItem"),fitemWidth=parseInt(ww/5),folderOuter=$(".folderOuter");
			folderItem.css("width",fitemWidth);
  			
			for(var i=0;i<deskTopNum;i++){
				$("#folder_"+i).find(".folderInner").append($("div.innerDesktop").eq(i).find(".desktopIcon:not(.addIcon)").clone());
				$("#folder_"+i).find(".folderOuter").niceScroll("#folder_"+i+" .folderInner",{touchbehavior:false,cursorcolor:"#666",horizrailenabled:true,cursoropacitymax:0.8,cursorborder:"1px solid #ccc"});
  				$(window).wresize(function(){$("#folder_"+i).find(".folderOuter").getNiceScroll().resize();});
 				}
  			
			var folderIcon=folderItem.find(".desktopIcon");
			
 			folderIcon
 			.on("mouseover",function(){
							$(this).addClass("hover");
							})
			.on("mouseout",function(){
									$(this).removeClass("hover");	 
										 })
			.attr("style","");
			
			$(".aMg_dock_container,.folderItem")
			.find(".desktopIcon")
			.on("click",function(e){
								 var index=$(this).parent().parent().parent().attr("id").split("_")[1],navBar=$("#navBar");
								 $("#"+this.id).trigger('click');
								 amgClose();
 								 navBar.find("span > a").eq(parseInt(index)).trigger('click');
								 
								 var ev=e||event;
								 ev.stopPropagation();
								 return false;
 								 });
			
			aMg_folder_innercontainer.width(deskTopNum*(fitemWidth)).height(wh-aMg_dock_container.height());
			
			$(window).wresize(function(){
									var h=$(window).height()-aMg_dock_container.height(),fw=$(window).width()/5;   
									aMg_folder_container.height(h);    
									aMg_folder_innercontainer.height(h).width(deskTopNum*fw);
									folderItem.css("width",fw);
									fitemWidth=parseInt(fw);
									  });
									  
		   if(deskTopNum>5){
			   aMg_folder_container.mousemove(function(event){
				   if(event.pageX<50){
					   aMg_prev.show();
					   }else{
						   aMg_prev.hide();
						   }
				   		   
				   if(event.pageX>$(window).width()-50){
					   aMg_next.show();
					   }else{
						   aMg_next.hide();
						   }	   
				   });
			   }
		 
		 var moveIndex=0,maxMoveNum=deskTopNum-5;
		 
		 function move_amg(a){
 			 aMg_folder_innercontainer.animate({
				 "margin-left": '+='+a
				 },100,"easeInOutCirc");
			 }
			 	   
		 //单击向上翻页	   
		 aMg_prev.click(function(){
			 moveIndex=parseInt(aMg_folder_innercontainer.css("margin-left"));
   			 if(moveIndex<0){
				 move_amg(fitemWidth);
				 }
			 });	   		  
		
		 //下一页
		 aMg_next.click(function(){
			 moveIndex=parseInt(aMg_folder_innercontainer.css("margin-left"));
 
			 if(moveIndex>maxMoveNum*-1*fitemWidth){
				  move_amg(-1*fitemWidth);
				 }
			 });
		
 		} 	
	};	