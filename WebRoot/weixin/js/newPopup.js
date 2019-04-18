if(!Array.prototype.push){
Array.prototype.push=function(){
   var startLength=this.length;
   for(var i=0;i<arguments.length;i++)
    this[startLength+i]=arguments[i];
   return this.length;
}
};
function G(){
  //alert("aa");
   var elements=new Array();
   for(var i=0;i<arguments.length;i++){
    var element=arguments[i];
    if(typeof element=='string')
     element=document.getElementById(element);
    if(arguments.length==1)
     return element;elements.push(element)
   };
   return elements
};
Function.prototype.bind=function(object){
var __method=this;
return function(){__method.apply(object,arguments)}
};
Function.prototype.bindAsEventListener=function(object){
var __method=this;
return function(event){__method.call(object,event||window.event)}
};
Object.extend=function(destination,source){
for(property in source){destination[property]=source[property]};return destination
};
if(!window.Event){var Event=new Object()};
Object.extend(Event,{observers:false,element:function(event){return event.target||event.srcElement},isLeftClick:function(event){return(((event.which)&&(event.which==1))||((event.button)&&(event.button==1)))},pointerX:function(event){return event.pageX||(event.clientX+(document.documentElement.scrollLeft||document.body.scrollLeft))},pointerY:function(event){return event.pageY||(event.clientY+(document.documentElement.scrollTop||document.body.scrollTop))},stop:function(event){if(event.preventDefault){event.preventDefault();event.stopPropagation()}else{event.returnValue=false;event.cancelBubble=true}},findElement:function(event,tagName){var element=Event.element(event);while(element.parentNode&&(!element.tagName||(element.tagName.toUpperCase()!=tagName.toUpperCase())))element=element.parentNode;return element},_observeAndCache:function(element,name,observer,useCapture){if(!this.observers)this.observers=[];if(element.addEventListener){this.observers.push([element,name,observer,useCapture]);element.addEventListener(name,observer,useCapture)}else if(element.attachEvent){this.observers.push([element,name,observer,useCapture]);element.attachEvent('on'+name,observer)}},unloadCache:function(){if(!Event.observers)return;for(var i=0;i<Event.observers.length;i++){Event.stopObserving.apply(this,Event.observers[i]);Event.observers[i][0]=null};Event.observers=false},observe:function(element,name,observer,useCapture){var element=G(element);useCapture=useCapture||false;if(name=='keypress'&&(navigator.appVersion.match(/Konqueror|Safari|KHTML/)||element.attachEvent))name='keydown';this._observeAndCache(element,name,observer,useCapture)},stopObserving:function(element,name,observer,useCapture){var element=G(element);useCapture=useCapture||false;if(name=='keypress'&&(navigator.appVersion.match(/Konqueror|Safari|KHTML/)||element.detachEvent))name='keydown';if(element.removeEventListener){element.removeEventListener(name,observer,useCapture)}else if(element.detachEvent){element.detachEvent('on'+name,observer)}}});
Event.observe(window,'unload',Event.unloadCache,false);
var Class=function(){
var _class=function(){
   this.initialize.apply(this,arguments)
};
for(i=0;i<arguments.length;i++){
   superClass=arguments[i];for(member in superClass.prototype){_class.prototype[member]=superClass.prototype[member]}
};
_class.child=function(){return new Class(this)};
_class.extend=function(f){for(property in f){_class.prototype[property]=f[property]}};
return _class
};
function space(flag){
if(flag=="begin"){
   var ele=document.getElementById("ft");
   if(typeof(ele)!="undefined"&&ele!=null)
    ele.id="ft_popup";
   ele=document.getElementById("usrbar");
   if(typeof(ele)!="undefined"&&ele!=null)
    ele.id="usrbar_popup"
}
else if(flag=="end"){
   var ele=document.getElementById("ft_popup");
   if(typeof(ele)!="undefined"&&ele!=null)ele.id="ft";
   ele=document.getElementById("usrbar_popup");
   if(typeof(ele)!="undefined"&&ele!=null)ele.id="usrbar"}
};
// Popup Class begin....
var Popup=new Class();
Popup.prototype={
iframeIdName:'ifr_popup', //iframe的名字
initialize:function(config){
   this.config=Object.extend({ //属性设置
    contentType:1, //pop类型 1. 内嵌iframe，2. 显示给定的html 3. confirm框 4. alert框
    isHaveTitle:true, //是否有标题栏
    scrollType:'yes', //内嵌iframe是否可以滚动
    isBackgroundCanClick:false, //背景是否可以点击
    isSupportDraging:true, //是否支持拖动
    isShowShadow:true, //是否显示阴影
    isReloadOnClose:true, //关闭后是否重新加载页面
    width:400, //宽度
    height:300 //高度
   },config||{});
   this.info={ //参数
    shadowWidth:4, //阴影宽度
    title:"", //标题
    contentUrl:"", //iframe的url
    contentHtml:"", //内容的html
    callBack:null, //回调函数
    parameter:null, //调用的参数 如 {id:"divCall",str:"点击确定后显示的字符串",obj:pop}
    confirmCon:"", //confirm的内容
    alertCon:"", //alert的内容
    someHiddenTag:"select,object,embed", //自动隐藏的页面元素
    someDisabledBtn:"", //禁用的btn
    someHiddenEle:"", //隐藏的匀速
    overlay:0, //覆盖
    coverOpacity:40 //蒙皮不透明度
   };
    this.color={
    cColor:"#EEEEEE", //蒙皮颜色
    bColor:"#FFFFFF", //背景颜色
    tColor:"#007aff", //标题背景颜色，边框颜色
    wColor:"#FFFFFF" //标题文字颜色
   };
    this.dropClass=null;
    this.someToHidden=[];
    this.someToDisabled=[];
    if(!this.config.isHaveTitle)
     this.config.isSupportDraging=false;
    this.iniBuild()
},
setContent:function(arrt,val){ //设置内容，即 this.info 的参数内容
   if(val!=''){
    switch(arrt){
     case 'width':this.config.width=val;break;
     case 'height':this.config.height=val;break;
     case 'title':this.info.title=val;break;
     case 'contentUrl':this.info.contentUrl=val;break;
     case 'contentHtml':this.info.contentHtml=val;break;
     case 'callBack':this.info.callBack=val;break;
     case 'parameter':this.info.parameter=val;break;
     case 'confirmCon':this.info.confirmCon=val;break;
     case 'alertCon':this.info.alertCon=val;break;
     case 'someHiddenTag':this.info.someHiddenTag=val;break;
     case 'someHiddenEle':this.info.someHiddenEle=val;break;
     case 'someDisabledBtn':this.info.someDisabledBtn=val;break;
     case 'overlay':this.info.overlay=val}
   }
},
iniBuild:function(){
   G('dialogCase')?G('dialogCase').parentNode.removeChild(G('dialogCase')):function(){};
   var oDiv=document.createElement('span');oDiv.id='dialogCase';document.body.appendChild(oDiv)
},
build:function(){
   var baseZIndex=10001+this.info.overlay*10;
   var showZIndex=baseZIndex+2;this.iframeIdName='ifr_popup'+this.info.overlay;
   //关闭按钮
   //var path="http://img.baidu.com/hi/img/";
   //var close='<input type="image" id="dialogBoxClose" src="'+path+'dialogclose.gif" border="0" width="16" height="16" align="absmiddle" title="关闭"/>';
   var close='<a id="dialogBoxClose" title="关闭" style="padding:0;color:#fff;cursor:pointer;font-size:26px;font-family:Airal;">×</a>';
   var cB='filter: alpha(opacity='+this.info.coverOpacity+');opacity:'+this.info.coverOpacity/100+';';
   var cover='<div id="dialogBoxBG" style="position:absolute;top:0px;left:0px;width:100%;height:100%;z-index:'+baseZIndex+';'+cB+'background-color:'+this.color.cColor+';display:none;"></div>';
   var mainBox='<div id="dialogBox" style="border:1px solid '+this.color.tColor+';display:none;z-index:'+showZIndex+';position:relative;width:'+this.config.width+'px;"><table width="100%" border="0" cellpadding="0" cellspacing="0" bgcolor="'+this.color.bColor+'">';if(this.config.isHaveTitle){mainBox+='<tr height="24" bgcolor="'+this.color.tColor+'"><td><table style="-moz-user-select:none;height:24px;" width="100%" border="0" cellpadding="0" cellspacing="0" ><tr>'+'<td width="6" height="24"></td><td id="dialogBoxTitle" style="color:'+this.color.wColor+';font-size:14px;font-weight:bold;">'+this.info.title+'&nbsp;</td>'+'<td id="dialogClose" width="20" align="right" valign="middle">'+close+'</td><td width="6"></td></tr></table></td></tr>'}
   else{mainBox+='<tr height="10"><td align="right">'+close+'</td></tr>'};
   mainBox+='<tr style="height:'+this.config.height+'px" valign="top"><td id="dialogBody" style="position:relative;"></td></tr></table></div>'+'<div id="dialogBoxShadow" style="display:none;z-index:'+baseZIndex+';"></div>';
   if(!this.config.isBackgroundCanClick){G('dialogCase').innerHTML=cover+mainBox;G('dialogBoxBG').style.height=document.body.scrollHeight}
   else G('dialogCase').innerHTML=mainBox;Event.observe(G('dialogBoxClose'),"click",this.reset.bindAsEventListener(this),false);
   if(this.config.isSupportDraging){dropClass=new Dragdrop(this.config.width,this.config.height,this.info.shadowWidth,this.config.isSupportDraging,this.config.contentType);G("dialogBoxTitle").style.cursor="move"};this.lastBuild()},lastBuild:function(){var confirm='<div style="width:100%;height:100%;text-align:center;"><div style="margin:20px 20px 0 20px;font-size:14px;line-height:16px;color:#000000;">'+this.info.confirmCon+'</div><div style="margin:20px;"><input id="dialogOk" type="button" value=" 确定 "/>&nbsp;<input id="dialogCancel" type="button" value=" 取消 "/></div></div>';
   var alert='<div style="width:100%;height:100%;text-align:center;"><div style="margin:20px 20px 0 20px;font-size:14px;line-height:16px;color:#000000;">'+this.info.alertCon+'</div><div style="margin:20px;"><input id="dialogYES" type="button" value=" 确定 "/></div></div>';
   var baseZIndex=10001+this.info.overlay*10;var coverIfZIndex=baseZIndex+4;
   if(this.config.contentType==1){var openIframe="<iframe width='100%' style='height:"+this.config.height+"px' name='"+this.iframeIdName+"' id='"+this.iframeIdName+"' src='"+this.info.contentUrl+"' frameborder='0' scrolling='"+this.config.scrollType+"'></iframe>";
   var coverIframe="<div id='iframeBG' style='position:absolute;top:0px;left:0px;width:1px;height:1px;z-index:"+coverIfZIndex+";filter: alpha(opacity=00);opacity:0.00;background-color:#ffffff;'><div>";G("dialogBody").innerHTML=openIframe+coverIframe}
   else if(this.config.contentType==2){G("dialogBody").innerHTML=this.info.contentHtml}else if(this.config.contentType==3){G("dialogBody").innerHTML=confirm;Event.observe(G('dialogOk'),"click",this.forCallback.bindAsEventListener(this),false);
   Event.observe(G('dialogCancel'),"click",this.close.bindAsEventListener(this),false)}else if(this.config.contentType==4){G("dialogBody").innerHTML=alert;Event.observe(G('dialogYES'),"click",this.close.bindAsEventListener(this),false)}},reBuild:function(){G('dialogBody').height=G('dialogBody').clientHeight;this.lastBuild()},show:function(){this.hiddenSome();this.middle();if(this.config.isShowShadow)this.shadow()},forCallback:function(){return this.info.callBack(this.info.parameter)},shadow:function(){var oShadow=G('dialogBoxShadow');var oDialog=G('dialogBox');oShadow['style']['position']="absolute";oShadow['style']['background']="#000";oShadow['style']['display']="";oShadow['style']['opacity']="0.2";oShadow['style']['filter']="alpha(opacity=20)";oShadow['style']['top']=oDialog.offsetTop+this.info.shadowWidth;oShadow['style']['left']=oDialog.offsetLeft+this.info.shadowWidth;oShadow['style']['width']=oDialog.offsetWidth;oShadow['style']['height']=oDialog.offsetHeight},middle:function(){if(!this.config.isBackgroundCanClick)G('dialogBoxBG').style.display='';
   var oDialog=G('dialogBox');
   oDialog['style']['position']="absolute";
   oDialog['style']['display']='';
   //取得页面的宽度和高度
   var sClientWidth=document.body.clientWidth;
   var sClientHeight=document.body.clientHeight;
   var sScrollTop=document.body.scrollTop;
   //
   var sleft=(sClientWidth/2)-(oDialog.offsetWidth/2);
   var iTop=-80+(sClientHeight/2+sScrollTop)-(oDialog.offsetHeight/2);
   var sTop=iTop>0?iTop:(sClientHeight/2+sScrollTop)-(oDialog.offsetHeight/2);
   if(sTop<1)sTop="20";if(sleft<1)sleft="20";
   oDialog['style']['left']=sleft+"px"; //左侧位置
   oDialog['style']['top']=sTop+"px"     //顶部位置
   },
   reset:function(){if(this.config.isReloadOnClose){top.location.reload()};this.close()},close:function(){G('dialogBox').style.display='none';
   if(!this.config.isBackgroundCanClick)G('dialogBoxBG').style.display='none';
   if(this.config.isShowShadow)G('dialogBoxShadow').style.display='none';G('dialogBody').innerHTML='';
   this.showSome()},hiddenSome:function(){var tag=this.info.someHiddenTag.split(",");
   if(tag.length==1&&tag[0]=="")tag.length=0;
   for(var i=0;i<tag.length;i++){this.hiddenTag(tag[i])};
   var ids=this.info.someHiddenEle.split(",");
   if(ids.length==1&&ids[0]=="")ids.length=0;
   for(var i=0;i<ids.length;i++){this.hiddenEle(ids[i])};
   var ids=this.info.someDisabledBtn.split(",");
   if(ids.length==1&&ids[0]=="")ids.length=0;
   for(var i=0;i<ids.length;i++){this.disabledBtn(ids[i])};
   space("begin")},disabledBtn:function(id){var ele=document.getElementById(id);
   if(typeof(ele)!="undefined"&&ele!=null&&ele.disabled==false){ele.disabled=true;this.someToDisabled.push(ele)}},hiddenTag:function(tagName){var ele=document.getElementsByTagName(tagName);
   if(ele!=null){for(var i=0;i<ele.length;i++){if(ele[i].style.display!="none"&&ele[i].style.visibility!='hidden'){ele[i].style.visibility='hidden';this.someToHidden.push(ele[i])}}}},hiddenEle:function(id){var ele=document.getElementById(id);if(typeof(ele)!="undefined"&&ele!=null){ele.style.visibility='hidden';this.someToHidden.push(ele)}},showSome:function(){for(var i=0;i<this.someToHidden.length;i++){this.someToHidden[i].style.visibility='visible'};for(var i=0;i<this.someToDisabled.length;i++){this.someToDisabled[i].disabled=false};space("end")}};var Dragdrop=new Class();Dragdrop.prototype={initialize:function(width,height,shadowWidth,showShadow,contentType){this.dragData=null;this.dragDataIn=null;this.backData=null;this.width=width;this.height=height;this.shadowWidth=shadowWidth;this.showShadow=showShadow;this.contentType=contentType;this.IsDraging=false;this.oObj=G('dialogBox');Event.observe(G('dialogBoxTitle'),"mousedown",this.moveStart.bindAsEventListener(this),false)},moveStart:function(event){this.IsDraging=true;if(this.contentType==1){G("iframeBG").style.display="";G("iframeBG").style.width=this.width;G("iframeBG").style.height=this.height};Event.observe(document,"mousemove",this.mousemove.bindAsEventListener(this),false);Event.observe(document,"mouseup",this.mouseup.bindAsEventListener(this),false);Event.observe(document,"selectstart",this.returnFalse,false);this.dragData={x:Event.pointerX(event),y:Event.pointerY(event)};this.backData={x:parseInt(this.oObj.style.left),y:parseInt(this.oObj.style.top)}},mousemove:function(event){if(!this.IsDraging)return;var iLeft=Event.pointerX(event)-this.dragData["x"]+parseInt(this.oObj.style.left);var iTop=Event.pointerY(event)-this.dragData["y"]+parseInt(this.oObj.style.top);if(this.dragData["y"]<parseInt(this.oObj.style.top))iTop=iTop-12;else if(this.dragData["y"]>parseInt(this.oObj.style.top)+25)iTop=iTop+12;this.oObj.style.left=iLeft;this.oObj.style.top=iTop;if(this.showShadow){G('dialogBoxShadow').style.left=iLeft+this.shadowWidth;G('dialogBoxShadow').style.top=iTop+this.shadowWidth};this.dragData={x:Event.pointerX(event),y:Event.pointerY(event)};document.body.style.cursor="move"},mouseup:function(event){if(!this.IsDraging)return;if(this.contentType==1)G("iframeBG").style.display="none";document.onmousemove=null;document.onmouseup=null;var mousX=Event.pointerX(event)-(document.documentElement.scrollLeft||document.body.scrollLeft);var mousY=Event.pointerY(event)-(document.documentElement.scrollTop||document.body.scrollTop);if(mousX<1||mousY<1||mousX>document.body.clientWidth||mousY>document.body.clientHeight){this.oObj.style.left=this.backData["x"];this.oObj.style.top=this.backData["y"];if(this.showShadow){G('dialogBoxShadow').style.left=this.backData.x+this.shadowWidth;G('dialogBoxShadow').style.top=this.backData.y+this.shadowWidth}};this.IsDraging=false;document.body.style.cursor="";Event.stopObserving(document,"selectstart",this.returnFalse,false)},returnFalse:function(){return false}};