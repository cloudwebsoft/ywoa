<%@ page contentType="text/html;charset=utf-8"%><%
String exobudjsRootRath = request.getContextPath();
%><!--
//
// ===========================================================【程式资讯及版权宣告】====
//  ExoBUD MP(II) v4.1tc+ [Traditional Chinese Version]
//  Copyright(Pe) 1999-2003 Jinwoong Yu[ExoBUD], Kendrick Wong[kiddiken.net].
// =====================================================================================
//  程式原作者: 庾珍雄(Jinwoong Yu)         繁体中文化作者: 惊直(Kendrick Wong/kiddiken)
//    个人网站: http://exobud.nayana.org          个人网站: http://kiddiken.net
//    电子邮件: exobud@hanmail.net                电子邮件: webmaster@kiddiken.net
//    ICQ 帐号: 96138429                          MSN 帐号: kiddiken@msn.com
//    发表日期: 2003.01.10(此版本原韩文版)        发表日期: 2003.03.23(繁体中文首个版本)
// =====================================================================================
//
//    版权所有。
//    请尊重智慧财产权： 无论您对本程式 ExoBUD MP(II) 作任何修改、制作(或翻译)面板，请您
//    *必须*保留此段版权宣告的内容，包括程式(及面板)原作者及中文化作者的名字和网站连结。
//
//    如果您想要以这个繁体中文版的程式为基础，翻译成其他语言的版本，及／或在网际网路上，
//    公开发表您所修改过的版本，请您首先以传送电子邮件的方式，征求我们的同意。
//
//    请不要将程式(或面板)原作者或中文化作者的名字改成您自己的名字，
//    然后以另一程式名称重新命名后在网路上公开发表及散播本程式，因为这是严重的侵权行为。
//
//    这是公益免费程式，所以请不要使用在商业用途上。
//    另外，您亦不可将本程式(全部或部份)复制到其他储存媒体(例如光碟片)上作贩卖获利用途。
//
//    假如因为使用本程式而令您蒙受资料遗失或损毁，程式原作者及中文化作者均不用对其负责。
//
// =====================================================================================

// 当您修改本程式的原始码时，请注意执行修改后的程式，可能会导致一些正在执行中的应用程式
// 无法正常运作；另外亦要留意在JavaScript上所使用的变数名称和设定值，大小写是有分别的。

var objMmInfo = null;
var intMmCnt = 0;
var intSelMmCnt = 0;
var intActMmCnt = 0;
var cActIdx = 0;
var cActTit = "nAnT";
var strMmInfo = "ExoBUD 媒体档案资讯";

var blnfpl = false;
var blnEnabled = false;
var blnEOT = false;
var arrSelMm = null;
var arrActMm = null;
var intExobudStat = 0;
var tidTLab = null;
var tidErr = null;
var tidMsg = null;
var intErrCnt = 0;
var blnRept = false;

// 这是“自动连续播放”的设定。一般来说，播放一首音乐完毕后就会自动跳到下一首。
// 但是如果您要播放的媒体是视讯档案(例如:MV)的话，最好将这个设定值改为 false 。
//   true = 自动连续播放
//   false = 不要自动连续播放，让使用者自行挑选下一首曲目
var blnAutoProc = true;

// 设定播放面板上所显示的时间长度，预设是以正常方式(Elapse)抑或倒数方式(Lapse)显示：
//   true = 以正常方式显示时间长度，即动态地显示曲目已播放的时间
//   false = 以倒数方式显示时间长度，即动态地显示曲目剩余的时间
var blnElaps = true;

// 设定播放每首曲目之间的延迟时间(Delay Time)，单位是毫秒(msec)。
// 每100毫秒代表0.1秒，预设值是500毫秒(即0.5秒)，最少也要设为100毫秒。
var intDelay = 500;

// wmpInit() 函式: 使用 wmp-obj v7.x 程式库建立环境设定
function wmpInit(){
 var wmps = Exobud.settings;
 var wmpc = Exobud.ClosedCaption;

 wmps.autoStart = true;
 wmps.balance = 0;
 wmps.enableErrorDialogs = false;
 wmps.invokeURLs = false;
 wmps.mute = false;
 wmps.playCount = 1;
 wmps.rate = 1;
 wmps.volume = 100;
 if(blnUseSmi){wmpc.captioningID="capText"; capText.style.display="";}
 Exobud.enabled = true;
}

// mkMmPath() 函式: 准备建立 Multi-object 的阵列
function mkMmPath(u,t,f,s){
 this.mmUrl = u;
 this.mmTit = t;
 this.mmDur = 0;
 this.selMm = f;
 this.actMm = f;
 if(blnUseSmi){this.mmSmi=s;}
}

// mkList() 函式: 建立 Multi-object 的阵列
function mkList(u,t,s,f){
 var cu = u;
 var ct = t;
 var cs = s;
 var cf = f;
 var idx = 0;

 if(objMmInfo == null){objMmInfo=new Array(); idx=0;}
 else {idx=objMmInfo.length;}
 if(u=="" || u==null){cu="mms://";}
 if(t=="" || t==null){ct="nAnT";}
 if(f=="f" || f=="F"){cf="f";}
 else {cf="t"; intSelMmCnt++;}

 if(blnUseSmi){
   objMmInfo[idx]=new mkMmPath(cu,ct,cf,cs);
 } else {
   objMmInfo[idx]=new mkMmPath(cu,ct,cf);
 }

 intActMmCnt = intSelMmCnt;
 intMmCnt = objMmInfo.length;
}

// mkSel() 函式: 建立已选取播放项目(Selected Media)的阵列
function mkSel(){
 arrSelMm = null;
 intSelMmCnt = 0;
 var selidx = 0;

 if(intMmCnt<=0){intExobudStat=1; blnEnabled=false; return;} // 没有任何播放清单项目

 arrSelMm = new Array();
 for(var i=0; i<intMmCnt; i++){
   if(objMmInfo[i].selMm =="t"){arrSelMm[selidx]=i;selidx++;}
 }
 intSelMmCnt=arrSelMm.length;

 if(intSelMmCnt<=0){blnEnabled=false; intExobudStat=2; arrSelMm=null; return;}
 else {blnEnabled=true; mkAct();}
}

// mkAct() 函式: 建立已启用播放项目(Activated Media)的阵列
function mkAct(){
 arrActMm = null;
 intActMmCnt = 0;
 var selidx = 0;
 var actidx = 0;

 if(blnEnabled){
   arrActMm=new Array();
   for(var i=0; i<intSelMmCnt; i++){
     selidx=arrSelMm[i];
     if(objMmInfo[selidx].actMm=="t"){arrActMm[actidx]=selidx; actidx++;}
   }
   intActMmCnt=arrActMm.length;
 }
 else { return;}
 if(intActMmCnt<=0){blnEOT=true;arrActMm=null;}
 else {blnEOT=false;}
}

// chkAllSel() 函式: 全部选取所有的播放清单项目
function chkAllSel(){
 for(var i=0; i<intMmCnt; i++){
   objMmInfo[i].selMm="t";
   objMmInfo[i].actMm="t";
 }
 mkSel();
}

// chkAllDesel() 函式: 不选取所有的播放清单项目
function chkAllDesel(){
 for(var i=0; i<intMmCnt; i++){
   objMmInfo[i].selMm="f";
   objMmInfo[i].actMm="f";
 }
 mkSel();
}

// chkItemSel() 函式: 选取或不选取播放清单项目
function chkItemSel(idx){
 if(objMmInfo[idx].selMm =="t"){
   objMmInfo[idx].selMm="f";objMmInfo[idx].actMm="f";
 } else {
   objMmInfo[idx].selMm="t";objMmInfo[idx].actMm="t";
 }
 mkSel();
}

// chkItemAct() 函式: 将某个已启用播放项目(Activated Media)冻结
function chkItemAct(idx){
 objMmInfo[idx].actMm="f";
 mkAct();
}

// mkSelAct() 函式: 将已选取播放项目(Selected Media)加入到已启用播放项目(Activated Media)
function mkSelAct(){
 var idx=0;
 for(var i=0; i<intSelMmCnt; i++){
   idx=arrSelMm[i];
   objMmInfo[idx].actMm="t";
 }
 mkAct();
}

// initExobud() 函式: 初始化 ExoBUD MP(II) 媒体播放程式
function initExobud(){
 wmpInit();
 mkSel();
 blnfpl = false;

 if(!blnShowVolCtrl) {
   document.images['vmute'].style.display = "none";
   document.images['vdn'].style.display = "none";
   document.images['vup'].style.display = "none";
 }
 if(!blnShowPlist){ document.images['plist'].style.display = "none";}

 if(blnRept){imgChange('rept',1);}
 else {imgChange('rept',0);}

 if(blnRndPlay){imgChange('pmode',1);}
 else {imgChange('pmode',0);}
 showTLab();
 disp1.innerHTML = "ExoBUD MP(II) v4.1tc+ 网站媒体播放程式";
 if(blnStatusBar){ window.status=('ExoBUD MP(II) v4.1tc+ 网站媒体播放程式');}
 if(blnAutoStart){startExobud();}
}

// startExobud() 函式: 开始播放曲目
function startExobud(){
 var wmps = Exobud.playState;
 if(wmps==2){Exobud.controls.play(); return;}
 if(wmps==3){ return;}

 blnfpl=false;
 if(!blnEnabled){waitMsg();return;}
 if(blnEOT){mkSelAct();}
 if(intErrCnt>0){intErrCnt=0;tidErr=setTimeout('retryPlay(),1000');return;}
 if(blnRndPlay){rndPlay();}
 else {cActIdx=arrActMm[0]; selMmPlay(cActIdx);}
}

// selMmPlay() 函式: 处理媒体标题
function selMmPlay(idx){
 clearTimeout(tidErr);
 cActIdx=idx;
 var trknum=idx+1;
 var ctit;
 try {
	 ctit = objMmInfo[idx].mmTit;
 }
 catch (e) {
 	alert("该曲目不存在！");
	return;
 }
 if(ctit=="nAnT"){ctit="(没有媒体标题)"}
 if(blnUseSmi){Exobud.ClosedCaption.SAMIFileName = objMmInfo[idx].mmSmi;}
 Exobud.URL = objMmInfo[idx].mmUrl;
 cActTit = "T" + trknum + ". " + ctit;
 disp1.innerHTML = cActTit;
 if(blnStatusBar){ window.status=(cActTit);}
 chkItemAct(cActIdx);
}

// wmpPlay() 函式: 使用 wmp-obj v7.x 程式库播放曲目
function wmpPlay(){Exobud.controls.play();}

// wmpStop() 函式: 停止播放曲目及显示“就绪”状态讯息
function wmpStop(){
 intErrCnt=0;
 clearTimeout(tidErr);
 clearInterval(tidTLab);
 imgChange("stopt",1);
 imgChange("pauzt",0);
 imgChange("scope",0);
 showTLab();
 mkSelAct();
 Exobud.controls.stop();
 Exobud.close();
 disp1.innerHTML = "ExoBUD MP(II) v4.1tc+ 网站媒体播放程式 [就绪]";
 if(blnStatusBar){ window.status=('ExoBUD MP(II) v4.1tc+ 网站媒体播放程式 [就绪]');return true;}
}

// wmpPause() 函式: 使用 wmp-obj v7.x 程式库暂停播放曲目
function wmpPause(){Exobud.controls.pause();}

// wmpPP() 函式: 在暂停播放和继续播放之间进行切换
function wmpPP(){
 var wmps = Exobud.playState;
 var wmpc = Exobud.controls;
 clearInterval(tidTLab);
 clearTimeout(tidMsg);
 if(wmps==2){wmpc.play();}
 if(wmps==3){wmpc.pause(); disp2.innerHTML="暂停"; tidMsg=setTimeout('rtnTLab()',1500);}
 return;
}

// rndPlay() 函式: 随机播放(Random Play)的运算方式
function rndPlay(){
 if(!blnEnabled){waitMsg();return;}
 intErrCnt=0;
 var idx=Math.floor(Math.random() * intActMmCnt);
 cActIdx=arrActMm[idx];
 selMmPlay(cActIdx);
}

// playAuto() 函式: 对已启用播放项目进行“自动连续播放”的处理
// 这是根据上面 blnAutoProc 的设定值而决定的动作。
function playAuto(){
 if(blnRept){selMmPlay(cActIdx);return;}
 if(!blnAutoProc){wmpStop();return;}
 if(blnfpl){wmpStop();return;}
 if(!blnEnabled){wmpStop();return;}
 if(blnEOT){
   if(blnLoopTrk){startExobud();}
   else {wmpStop();}
 } else {
   if(blnRndPlay){rndPlay();}
   else {cActIdx=arrActMm[0]; selMmPlay(cActIdx);}
 }
}

// 播放使用者在播放清单上所点选的单一曲目
function selPlPlay(idx){
 blnfpl=true;
 selMmPlay(idx);
}

// playPrev() 函式: 播放上一首已启用播放项目
function playPrev(){
 var wmps = Exobud.playState;
 if(wmps==2 || wmps==3){Exobud.controls.stop();}
 blnfpl=false;
 if(!blnEnabled){waitMsg();return;}
 if(blnEOT){mkSelAct();}

 intErrCnt=0;
 if(blnRndPlay){rndPlay();}
 else {
   var idx=cActIdx;
   var blnFind=false;
   for(var i=0;i<intSelMmCnt;i++){ if(cActIdx==arrSelMm[i]){idx=i-1; blnFind=true;}}
   if(!blnFind){startExobud();return;}
   if(idx<0){idx=intSelMmCnt-1;cActIdx=arrSelMm[idx];}
   else {cActIdx=arrSelMm[idx];}
   selMmPlay(cActIdx);
 }
}

// playNext() 函式: 播放下一首已启用播放项目
function playNext(){
 var wmps = Exobud.playState;
 if(wmps==2 || wmps==3){Exobud.controls.stop();}
 blnfpl=false;
 if(!blnEnabled){waitMsg();return;}
 if(blnEOT){mkSelAct();}

 intErrCnt=0;
 if(blnRndPlay){rndPlay();}
 else {
   var idx=cActIdx;
   var blnFind=false;
   for(var i=0;i<intSelMmCnt;i++){ if(cActIdx==arrSelMm[i]){idx=i+1; blnFind=true;}}
   if(!blnFind){startExobud();return;}
   if(idx>=intSelMmCnt){idx=0;cActIdx=arrSelMm[idx];}
   else {cActIdx=arrSelMm[idx];}
   selMmPlay(cActIdx);
 }
}

// retryPlay() 函式: 再次尝试连线到媒体档案
function retryPlay(){
 selMmPlay(cActIdx);
}

// chkRept() 函式: 切换是否重复播放目前的曲目(已启用播放项目)
function chkRept(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 if(blnRept){
   blnRept=false; imgChange('rept',0); disp2.innerHTML="不重复播放";
 } else {
   blnRept=true; imgChange('rept',1); disp2.innerHTML="重复播放";
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// chgPMode() 函式: 切换以循序(Sequential)抑或随机(Random)的方式来播放媒体项目
function chgPMode(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 if(blnRndPlay){
   blnRndPlay=false; imgChange('pmode',0); disp2.innerHTML="循序播放";
 } else {
   blnRndPlay=true; imgChange('pmode',1); disp2.innerHTML="随机播放";
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// evtOSChg() 函式: 以弹出视窗方式显示媒体档案资讯
function evtOSChg(f){
//   以下是状态值 (f) 的说明:
//    0(未定义)       8(转换媒体中)   9(寻找媒体中)  10(连线媒体中)  11(载入媒体中)
//   12(开启媒体中)  13(媒体已开启)  20(等待播放中)  21(正在开启不明的连结)

 if(f==8){capText.innerHTML="ExoBUD MP(II) 字幕显示系统(SMI)";}
 if(f==13){
   var strTitle = Exobud.currentMedia.getItemInfo("Title");
   if(strTitle.length <= 0){strTitle = "(未命名的标题)"}
   var strAuthor = Exobud.currentMedia.getItemInfo("Author");
   if(strAuthor.length <= 0){strAuthor = "(未命名的演出者)"}
   var strCopy = Exobud.currentMedia.getItemInfo("Copyright");
   if(strCopy.length <= 0){strCopy = "(没有著作权资讯)"}
   var strType = Exobud.currentMedia.getItemInfo("MediaType");
   var strDur = Exobud.currentMedia.durationString;
   var strUrl = Exobud.URL;
   var trknum = cActIdx+1;
   var ctit = objMmInfo[cActIdx].mmTit;
   if(ctit=="nAnT"){
     objMmInfo[cActIdx].mmTit = strAuthor + " - " + strTitle;
     ctit = strAuthor + " - " + strTitle;
     cActTit = "T" + trknum + ". " + ctit;
     disp1.innerHTML = cActTit;
   }

   strMmInfo  = "　　标题： " + strTitle + " (形式: " + strType +")" + "\n\n";
   strMmInfo += "　演出者： " + strAuthor + "\n\n";
   strMmInfo += "档案位置： " + strUrl + "\n\n";
   strMmInfo += "　著作权： " + strCopy + "\n\n";
   strMmInfo += "时间长度： " + strDur + "\n\n\n";
   strMmInfo += "　　 Brought to you by ExoBUD MP(II).\n";
   strMmInfo += "　　 Copyright(C) 1999-2003 Jinwoong Yu.\n";
   strMmInfo += "　　 ALL RIGHTS RESERVED.\n";
   if(blnShowMmInfo){alert(strMmInfo);}
 }
}

// evtPSChg() 函式: 切换播放程式的动作
function evtPSChg(f){
//   以下是状态值 (f) 的说明:
//    0(未定义)       1(已停止播放)   2(已暂停播放)   3(正在播放中)   4(向前搜索)     5(向后搜索)
//    6(缓冲处理中)   7(等待中)       8(已播放完毕)   9(转换曲目中)  10(就绪状态)

 switch(f){
   case 1:
     evtStop();
     break;
   case 2:
     evtPause();
     break;
   case 3:
     evtPlay();
     break;
   case 8:
     setTimeout('playAuto()', intDelay);
     break;
 }
}

// evtWmpBuff() 函式: 对媒体档案进行缓冲处理(Buffering)的动作
function evtWmpBuff(f){
 if(f){
   disp2.innerHTML = "缓冲处理中";
   var msg = "(缓冲处理中) " + cActTit;
   disp1.innerHTML = msg;
   if(blnStatusBar){ window.status=(msg);}
 } else {
   disp1.innerHTML = cActTit; showTLab();
 }
}

// evtWmpError() 函式: 当无法连线到媒体档案时，显示错误讯息
function evtWmpError(){
 intErrCnt++;
 Exobud.Error.clearErrorQueue();
 if(intErrCnt<=3){
   disp2.innerHTML = "尝试连线 (" + intErrCnt + ")";
   var msg = "(尝试第 " + intErrCnt + " 次连线) " + cActTit;
   disp1.innerHTML = "<无法播放> " + cActTit;
   if(blnStatusBar){ window.status=(msg);}
   tidErr=setTimeout('retryPlay()',1000);
 } else {
   clearTimeout(tidErr);
   intErrCnt=0;showTLab();
   var msg = "已放弃尝试再连线。现在将会播放下一首曲目。";
   if(blnStatusBar){ window.status=(msg);}
   setTimeout('playAuto()',1000);}
}

// evtStop() 函式: 停止播放
function evtStop(){
 clearTimeout(tidErr);
 clearInterval(tidTLab);
 showTLab();
 intErrCnt=0;
 imgChange("pauzt",0);
 imgChange("playt",0);
 imgChange("scope",0);
 disp1.innerHTML = "ExoBUD MP(II) v4.1tc+ [等待播放下一首曲目]";
 if(blnStatusBar){ window.status=('ExoBUD MP(II) v4.1tc+ [等待播放下一首曲目]');return true;}
}

// evtPause() 函式: 暂停播放
function evtPause(){
 imgChange("pauzt",1)
 imgChange("playt",0);
 imgChange("stopt",0);
 imgChange("scope",0);
 clearInterval(tidTLab);
 showTLab();
}

// evtPlay() 函式: 开始播放
function evtPlay(){
 imgChange("pauzt",0)
 imgChange("playt",1);
 imgChange("stopt",0);
 imgChange("scope",1);
 tidTLab=setInterval('showTLab()',1000);
}

// showTLab() 函式: 显示时间长度
function showTLab(){
 var ps = Exobud.playState;
 if(ps==2 || ps==3){
   var cp=Exobud.controls.currentPosition;
   var cps=Exobud.controls.currentPositionString;
   var dur=Exobud.currentMedia.duration;
   var durs=Exobud.currentMedia.durationString;
   if(blnElaps){
     disp2.innerHTML = cps + " | " + durs;
     var msg = cActTit + " (" + cps + " | " + durs + ")";
     if(ps==2){msg = "(暂停) " + msg;}
     if(blnStatusBar){ window.status=(msg);return true;}
   } else {
     var laps = dur-cp;
     var strLaps = wmpTime(laps);
     disp2.innerHTML = strLaps + " | " + durs;
     var msg = cActTit + " (" + strLaps + " | " + durs + ")";
     if(ps==2){msg = "(暂停) " + msg;}
     if(blnStatusBar){ window.status=(msg);return true;}
   }

 } else {
   disp2.innerHTML = "00:00 | 00:00";
 }
}

// chgTimeFmt() 函式: 变更时间长度的显示方式
function chgTimeFmt(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 if(blnElaps){
   blnElaps=false; disp2.innerHTML="倒数方式";
 } else {
   blnElaps=true; disp2.innerHTML="正常方式";
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// rtnTLab() 函式: 传回时间长度
function rtnTLab(){
 clearTimeout(tidMsg);
 var wmps = Exobud.playState;
 if(wmps==3){tidTLab=setInterval('showTLab()',1000);}
 else {showTLab();}
}

// wmpTime() 函式: 计算时间长度
function wmpTime(dur){
 var hh, min, sec, timeLabel;
 hh=Math.floor(dur/3600);
 min=Math.floor(dur/60)%60;
 sec=Math.floor(dur%60);
 if(isNaN(min)){ return "00:00";}
 if(isNaN(hh) || hh==0){timeLabel="";}
 else {
   if(hh>9){timeLabel = hh.toString() + ":";}
   else {timeLabel = "0" + hh.toString() + ":";}
 }
 if(min>9){timeLabel = timeLabel + min.toString() + ":";}
 else {timeLabel = timeLabel + "0" + min.toString() + ":";}
 if(sec>9){timeLabel = timeLabel + sec.toString();}
 else {timeLabel = timeLabel + "0" + sec.toString();}
 return timeLabel;
}

// wmpVolUp(), wmpVolDn(), wmpMute() 这几个都是用来调校音量的函式。(单位：％)
// vmax 代表最大音量(100), vmin 代表最小音量(0), vdep 代表调校音量的间隔(建议设为5至20之间)
// 您只可以在 vmin, vmax, vdep 设为0至100之间的整数数值，vmin 和 vdep 数值不可以大过 vmax。

var vmax = 100;
var vmin = 0;
var vdep = 10;

// wmpVolUp() 函式: 增加音量(Volume Up)
function wmpVolUp(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 var ps = Exobud.settings;
 if(ps.mute){ps.mute=false; disp2.innerHTML="音量恢复"; imgChange('vmute',0);}
 else {
   if(ps.volume >= (vmax-vdep)){ps.volume = vmax;}
   else {ps.volume = ps.volume + vdep;}
   disp2.innerHTML = "音量: " + ps.volume + "%";
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// wmpVolDn() 函式: 减少音量(Volume Down)
function wmpVolDn(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 var ps = Exobud.settings;
 if(ps.mute){ps.mute=false; disp2.innerHTML="音量恢复"; imgChange('vmute',0);}
 else {
   if(ps.volume <= vdep){ps.volume = vmin;}
   else {ps.volume = ps.volume - vdep;}
   disp2.innerHTML = "音量: " + ps.volume + "%";
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// wmpMute() 函式: 静音模式(Mute)
function wmpMute(){
 var wmps = Exobud.playState;
 if(wmps==3){clearInterval(tidTLab);}
 var ps = Exobud.settings;
 if(!ps.mute){
   ps.mute=true; disp2.innerHTML="开启静音模式"; imgChange("vmute",1);
 } else {
   ps.mute=false; disp2.innerHTML="关闭静音模式"; imgChange("vmute",0);
 }
 tidMsg=setTimeout('rtnTLab()',1000);
}

// waitMsg() 函式: 显示因播放清单空白而无法播放的讯息
function waitMsg(){
 capText.innerHTML="ExoBUD MP(II) 字幕显示系统(SMI)";
 if(intExobudStat==1){disp1.innerHTML = "无法播放 － 播放清单上没有设定任何曲目。";}
 if(intExobudStat==2){disp1.innerHTML = "无法播放 － 您没有选取播放清单上任何一首曲目。";}
 if(blnStatusBar){
   if(intExobudStat==1){ window.status=('无法播放 － 播放清单上没有设定任何曲目。'); return true;}
   if(intExobudStat==2){ window.status=('无法播放 － 您没有选取播放清单上任何一首曲目。'); return true;}
 }
}

// openPlist() 函式: 以弹出视窗显示播放清单内容
function openPlist(){
 window.open("<%=exobudjsRootRath%>/exobud/exobudpl.htm","mplist","top=120,left=320,width=280,height=490,scrollbars=no,resizable=yes,copyhistory=no");
}

// chkWmpState() 函式: 当播放程式动作变更时，传回 playState 的状态值
function chkWmpState(){
//   以下是状态值的说明:
//    0(未定义)       1(已停止播放)   2(已暂停播放)   3(正在播放中)   4(向前搜索)     5(向后搜索)
//    6(缓冲处理中)   7(等待中)       8(已播放完毕)   9(转换曲目中)  10(就绪状态)
 return Exobud.playState;
}

// chkWmpOState() 函式: 当播放程式开启媒体档案准备播放时，传回 openState 的状态值
function chkWmpOState(){
//   以下是状态值的说明:
//    0(未定义)       8(转换媒体中)   9(寻找媒体中)  10(连线媒体中)  11(载入媒体中)
//   12(开启媒体中)  13(媒体已开启)  20(等待播放中)  21(正在开启不明的连结)
 return Exobud.openState;
}

// chkOnline() 函式: 检查使用者的连线状态 (不一定每款面板都会使用)
function chkOnline(){
// 传回值: true(已连线到网际网路) false(没有连线到网际网路)
 return Exobud.isOnline;
}

// vizExobud() 函式: 点选连到 ExoBUD MP 播放器原作者的官方网站[韩文] (不一定每款面板都会使用)
function vizExobud(){
// 使用范例: <span onClick="vizExobud()" style="cursor:hand" title="到访 ExoBUD MP 原作者 Jinwoong Yu 的网站 [韩文]">
 window.open("http://exobud.nayana.org","vizExobud");
}

//-->