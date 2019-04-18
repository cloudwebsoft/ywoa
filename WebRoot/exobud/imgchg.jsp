<%@ page contentType="text/html;charset=utf-8"%>
<%
String imgchgRootRath = request.getContextPath();
%>
<!--
//
// ■ 进行动态按钮图档的切换动作
//

toggleKey = new Object();
toggleKey[0] = "_off";
toggleKey[1] = "_on";
toggleKey[2] = "_ovr";
toggleKey[3] = "_out";
toggleKey[4] = "_mdn";
toggleKey[5] = "_mup";

function imgChange(id,act){
 if(document.images){ document.images[id].src = eval("img." + id + toggleKey[act] + ".src");}
}

// 当这段程式码应用到播放器使用时：
// 以函式 imgChange('按钮识别名称',0) 进行的动作即使用 "off" 的图档；
// 以函式 imgChange('按钮识别名称',1) 进行的动作即使用 "on"  的图档。

// 下面的部份就是设定 "off" 与 "on" 的动态按钮图档。
// vmute, pmode, rept, playt, pauzt, stopt 这些都是“按钮识别名称”。

if(document.images){
 img = new Object();

 // “静音模式”按钮的图档 (已关闭／已开启)
 img.vmute_off = new Image();
 img.vmute_off.src = "<%=imgchgRootRath%>/exobud/img/btn_mute_off.gif";
 img.vmute_on = new Image();
 img.vmute_on.src = "<%=imgchgRootRath%>/exobud/img/btn_mute_on.gif";
 
 // “播放顺序模式”按钮的图档 (循序／随机)
 img.pmode_off = new Image();
 img.pmode_off.src = "<%=imgchgRootRath%>/exobud/img/btn_rndmode_off.gif";
 img.pmode_on = new Image();
 img.pmode_on.src = "<%=imgchgRootRath%>/exobud/img/btn_rndmode_on.gif";

 // “是否重复播放”按钮的图档 (不重复／重复)
 img.rept_off = new Image();
 img.rept_off.src = "<%=imgchgRootRath%>/exobud/img/btn_rept_off.gif";
 img.rept_on = new Image();
 img.rept_on.src = "<%=imgchgRootRath%>/exobud/img/btn_rept_on.gif";

 // “播放”按钮的图档 (非播放中／播放中／滑鼠移至时)
 img.playt_off = new Image();
 img.playt_off.src = "<%=imgchgRootRath%>/exobud/img/btn_play.gif";
 img.playt_on = new Image();
 img.playt_on.src = "<%=imgchgRootRath%>/exobud/img/btn_play_on.gif";
 img.playt_ovr = new Image();
 img.playt_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_play_ovr.gif";

 // “暂停”按钮的图档 (非暂停中／暂停中／滑鼠移至时)
 img.pauzt_off = new Image();
 img.pauzt_off.src = "<%=imgchgRootRath%>/exobud/img/btn_pauz_off.gif";
 img.pauzt_on = new Image();
 img.pauzt_on.src = "<%=imgchgRootRath%>/exobud/img/btn_pauz_on.gif";
 img.pauzt_ovr = new Image();
 img.pauzt_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_pauz_ovr.gif";

 // “停止”按钮的图档 (未被停止／已停止／滑鼠移至时)
 img.stopt_off = new Image();
 img.stopt_off.src = "<%=imgchgRootRath%>/exobud/img/btn_stop.gif";
 img.stopt_on = new Image();
 img.stopt_on.src = "<%=imgchgRootRath%>/exobud/img/btn_stop_on.gif";
 img.stopt_ovr = new Image();
 img.stopt_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_stop_ovr.gif";

 // “上一首曲目”按钮的图档 (一般显示／滑鼠移至时)
 img.prevt_out = new Image();
 img.prevt_out.src = "<%=imgchgRootRath%>/exobud/img/btn_prev.gif";
 img.prevt_ovr = new Image();
 img.prevt_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_prev_ovr.gif";

 // “下一首曲目”按钮的图档 (一般显示／滑鼠移至时)
 img.nextt_out = new Image();
 img.nextt_out.src = "<%=imgchgRootRath%>/exobud/img/btn_next.gif";
 img.nextt_ovr = new Image();
 img.nextt_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_next_ovr.gif";

 // “增加音量”按钮的图档 (一般显示／滑鼠移至时)
 img.vup_out = new Image();
 img.vup_out.src = "<%=imgchgRootRath%>/exobud/img/btn_vup.gif";
 img.vup_ovr = new Image();
 img.vup_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_vup_ovr.gif";

 // “减少音量”按钮的图档 (一般显示／滑鼠移至时)
 img.vdn_out = new Image();
 img.vdn_out.src = "<%=imgchgRootRath%>/exobud/img/btn_vdn.gif";
 img.vdn_ovr = new Image();
 img.vdn_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_vdn_ovr.gif";

 // “显示播放清单内容”按钮的图档 (一般显示／滑鼠移至时)
 img.plist_out = new Image();
 img.plist_out.src = "<%=imgchgRootRath%>/exobud/img/btn_plist.gif";
 img.plist_ovr = new Image();
 img.plist_ovr.src = "<%=imgchgRootRath%>/exobud/img/btn_plist_ovr.gif";

 // 显示播放状态的 Scope 动态图档 (静止／转动)
 img.scope_off = new Image();
 img.scope_off.src = "<%=imgchgRootRath%>/exobud/img/scope_off.gif";
 img.scope_on = new Image();
 img.scope_on.src = "<%=imgchgRootRath%>/exobud/img/scope_on.gif";

}

function imgtog(tg,act){
 if(tg=="vmute")    { if(act=="2"){imgChange("vmute",1);} else {imgmute("vmute",0);} }
 if(tg=="vdn")      { if(act=="2"){imgChange("vdn",2);} else {imgChange("vdn",3);} }
 if(tg=="vup")      { if(act=="2"){imgChange("vup",2);} else {imgChange("vup",3);} }
 if(tg=="pmode")    { if(act=="2"){imgChange("pmode",1);} else {imgrnd();} }
 if(tg=="rept")     { if(act=="2"){imgChange("rept",1);} else {imgrept();} }
 if(tg=="nextt")    { if(act=="2"){imgChange("nextt",2);} else {imgChange("nextt",3);} }
 if(tg=="prevt")    { if(act=="2"){imgChange("prevt",2);} else {imgChange("prevt",3);} }
 if(tg=="pauzt")    { if(act=="2"){imgpauz(2);} else {imgpauz();} }
 if(tg=="playt")    { if(act=="2"){imgplay(2);} else {imgplay();} }
 if(tg=="stopt")    { if(act=="2"){imgstop(2);} else {imgstop();} }
 if(tg=="plist")    { if(act=="2"){imgChange("plist",2);} else {imgChange("plist",3);} }
}

function imgmute(){
 var ps=Exobud.settings;
 if(ps.mute){imgChange("vmute",1);}
 else {imgChange("vmute",0);}
}

function imgrnd(){
 if(blnRndPlay){imgChange("pmode",1);}
 else {imgChange("pmode",0);}
}

function imgrept(){
 if(blnRept){imgChange("rept",1);}
 else {imgChange("rept",0);}
}

function imgpauz(f){
 var wmps=Exobud.playState;
 if(f==2){imgChange("pauzt",2);}
 else {
   if(wmps==2){imgChange("pauzt",1);}
   else {imgChange("pauzt",0);}
 }
}

function imgplay(f){
 var wmps=Exobud.playState;
 if(f==2){imgChange("playt",2);}
 else {
   if(wmps==3){imgChange("playt",1);}
   else {imgChange("playt",0);}
 }
}

function imgstop(f){
 var wmps=Exobud.playState;
 if(f==2){imgChange("stopt",2);}
 else {
   if(wmps==2 || wmps==3){imgChange("stopt",0);}
   else {imgChange("stopt",1);}
 }
}

//-->