<%@ page contentType="text/html;charset=utf-8"%>
<%
String exobudRootRath = request.getContextPath();
%>
<link rel="stylesheet" type="text/css" href="exobud.css">
<!--
============================================================【程式资讯及版权宣告】====
  ExoBUD MP(II) v4.1tc+ [Traditional Chinese Version]
  Copyright(Pe) 1999-2003 Jinwoong Yu[ExoBUD], Kendrick Wong[kiddiken.net].
======================================================================================
  程式原作者: 庾珍雄(Jinwoong Yu)         繁体中文化作者: 惊直(Kendrick Wong/kiddiken)
    个人网站: http://exobud.nayana.org          个人网站: http://kiddiken.net
    电子邮件: exobud@hanmail.net                电子邮件: webmaster@kiddiken.net
    ICQ 帐号: 96138429                          MSN 帐号: kiddiken@msn.com
    发表日期: 2003.01.10(此版本原韩文版)        发表日期: 2003.03.23(繁体中文首个版本)
======================================================================================

    版权所有。
    请尊重智慧财产权： 无论您对本程式 ExoBUD MP(II) 作任何修改、制作(或翻译)面板，请您
    *必须*保留此段版权宣告的内容，包括程式(及面板)原作者及中文化作者的名字和网站连结。

    如果您想要以这个繁体中文版的程式为基础，翻译成其他语言的版本，及／或在网际网路上，
    公开发表您所修改过的版本，请您首先以传送电子邮件的方式，征求我们的同意。

    请不要将程式(或面板)原作者或中文化作者的名字改成您自己的名字，
    然后以另一程式名称重新命名后在网路上公开发表及散播本程式，因为这是严重的侵权行为。

    这是公益免费程式，所以请不要使用在商业用途上。
    另外，您亦不可将本程式(全部或部份)复制到其他储存媒体(例如光碟片)上作贩卖获利用途。

    假如因为使用本程式而令您蒙受资料遗失或损毁，程式原作者及中文化作者均不用对其负责。

============================================================【面板(Skin)制作资讯】====
  ExoBUD MP(II) v4.1tc+ 麦金塔水族面板 (Mac-Aqua Skin, v1.2)
======================================================================================
  面板原作者: 庾珍雄(Jinwoong Yu)         繁体中文化作者: 惊直(Kendrick Wong/kiddiken)
    个人网站: http://exobud.nayana.org          个人网站: http://kiddiken.net
    电子邮件: exobud@hanmail.net                电子邮件: webmaster@kiddiken.net
    ICQ 帐号: 96138429                          MSN 帐号: kiddiken@msn.com
    发表日期: 2003.01.23                        发表日期: 2003.04.15
======================================================================================

    使用此面板播放器的相关注意事项：

        您可以在设定播放清单项目的媒体标题时，使用像 ? 这些"Unicode/万国码"
        来转换成您想要的字符，包括繁体中文以外的双字节字元 (例如韩文、日文) 和其他
        特殊字元；但您同时也需要将“在浏览器的状态列显示播放器文字讯息”的设定项目
        (blnStatusBar)设为 false，因为浏览器的状态列是无法转换像 ? 这些万国
        字符控制码的。

======================================================================================

-->

<!-- 载入 ExoBUD MP(II) 主程式 -->
<script language="JavaScript" src="<%=exobudRootRath%>/exobud/exobud_js.jsp"></script>

<!-- 载入 ExoBUD MP(II) 基本设定档 -->
<script language="JavaScript" src="<%=exobudRootRath%>/exobud/exobudset.js"></script>

<!-- 载入 ExoBUD MP(II) 播放清单设定档 -->
<script language="JavaScript" src="<%=exobudRootRath%>/exobud/exobudpl.js"></script>

<!-- 如果您不熟悉原始码编辑，请勿随便修改下面使用 JScript 的部份，否则可能会导致程式不能正常运作 -->
<script language="JScript" for="Exobud" event="openStateChange(sf)">evtOSChg(sf);</script>
<script language="JScript" for="Exobud" event="playStateChange(ns)">evtPSChg(ns);</script>
<script language="JScript" for="Exobud" event="error()">evtWmpError();</script>
<script language="JScript" for="Exobud" event="Buffering(bf)">evtWmpBuff(bf);</script>

<!-- 载入“动态按钮图档切换”的 JavaScript 程式档 -->
<script language="JavaScript" src="<%=exobudRootRath%>/exobud/imgchg.jsp"></script>
<!-- 当您将这个媒体播放器嵌入您的网站使用时，建议预留 640~760px(像素) 的宽度
     乘以 20~25px(像素) 的高度 (在不使用字幕功能的情况下) 来设计框架的内容。 -->
<object id="Exobud" classid="CLSID:6BF52A52-394A-11d3-B153-00C04F79FAA6"
  type="application/x-oleobject"
  style="position:relative;left:0px;top:0px;width:0px;height:0px;">
  <param name="autoStart" value="true">
  <param name="balance" value="0">
  <param name="currentPosition" value="0">
  <param name="currentMarker" value="0">
  <param name="enableContextMenu" value="false">
  <param name="enableErrorDialogs" value="false">
  <param name="enabled" value="true">
  <param name="fullScreen" value="false">
  <param name="invokeURLs" value="false">
  <param name="mute" value="false">
  <param name="playCount" value="1">
  <param name="rate" value="1">
  <param name="uiMode" value="none">
  <param name="volume" value="100">
</object>
<table width=100% height=25 align=center cellpadding=0 cellspacing=0 border=0>
  <tr>
    <td width=100% background="<%=exobudRootRath%>/exobud/img/bg.gif">

<table height=25 align=center cellpadding=0 cellspacing=0 border=0>
  <tr>

      <!-- 显示播放状态的 Scope 动态图档：
           如果您想要变更这个图档，请同时参考及修改 imgchg.js 接近档末的部份。 -->
    <td width=25 align=center
     ><img name="scope" src="<%=exobudRootRath%>/exobud/img/scope_off.gif" width=21 height=20
      onClick="vizExobud()" style="cursor:help" title="到访 ExoBUD MP 原作者 Jinwoong Yu 的网站 [韩文]"
    ></td>

    <td width=11 background="<%=exobudRootRath%>/exobud/img/bg1l.gif"></td>
    <td width=245 background="<%=exobudRootRath%>/exobud/img/bg1c.gif">&nbsp;

      <!-- 显示媒体标题的方块：
           本面板播放器使用跑马灯(marquee)的方式显示媒体标题，所以您不用担心因为标题过长而不能完整显示。
           您可以根据个人喜好来设定跑马灯的属性，包括跑动方式(behavior)及跑动速度(scrollamount,scrolldelay)。 -->
      <marquee behavior="scroll" width=230 height=12 scrollamount=2 scrolldelay=70 style="padding-bottom:2px">
        <span id="disp1" class="title">ExoBUD MP(II) v4.1tc+ 网站媒体播放程式</span>
      </marquee>

    </td>
    <td width=11 background="<%=exobudRootRath%>/exobud/img/bg1r.gif"></td>
    <td width=11 background="<%=exobudRootRath%>/exobud/img/bg2l.gif"></td>
    <td width=105 align=center background="<%=exobudRootRath%>/exobud/img/bg2c.gif" nowrap onClick="chgTimeFmt()">

      <!-- 显示时间长度的方块：
           假如媒体的时间长度超过一小时，请将 width 的数值适当地增加，令资讯可以显示得完整。 -->
      <span id="disp2" class="time" title="时间长度显示方式 (正常/倒数)"
        style="width:105;cursor:hand;padding-bottom:2px">00:00 | 00:00</span>

    </td>
    <td width=11 background="<%=exobudRootRath%>/exobud/img/bg2r.gif"></td>
    <td width=5></td>

    <td nowrap>

      <!-- 播放器控制面板上的所有按钮：
           如果您想要变更这些图档，请同时参考及修改 imgchg.js 这个档案的内容。 -->
      <img name="vmute" src="<%=exobudRootRath%>/exobud/img/btn_mute_off.gif"    width=15 height=25 border=0 onClick="wmpMute()"
        onMouseOver="imgtog('vmute',2)" onMouseOut="imgtog('vmute',3)" style="cursor:hand" title="静音模式"
     ><img name="vdn"   src="<%=exobudRootRath%>/exobud/img/btn_vdn.gif"         width=15 height=25 border=0 onClick="wmpVolDn()"
        onMouseOver="imgtog('vdn',2)"   onMouseOut="imgtog('vdn',3)"   style="cursor:hand" title="减少音量"
     ><img name="vup"   src="<%=exobudRootRath%>/exobud/img/btn_vup.gif"         width=15 height=25 border=0 onClick="wmpVolUp()"
        onMouseOver="imgtog('vup',2)"   onMouseOut="imgtog('vup',3)"   style="cursor:hand" title="增加音量">

      <img name="pmode" src="<%=exobudRootRath%>/exobud/img/btn_rndmode_off.gif" width=15 height=25 border=0 onClick="chgPMode()"
        onMouseOver="imgtog('pmode',2)" onMouseOut="imgtog('pmode',3)" style="cursor:hand" title="播放顺序 (S=循序 R=随机)"
     ><img name="rept"  src="<%=exobudRootRath%>/exobud/img/btn_rept_off.gif"    width=15 height=25 border=0 onClick="chkRept()"
        onMouseOver="imgtog('rept',2)"  onMouseOut="imgtog('rept',3)"  style="cursor:hand" title="切换是否重复播放目前的曲目">

      <img name="prevt" src="<%=exobudRootRath%>/exobud/img/btn_prev.gif"        width=24 height=25 border=0 onClick="playPrev()"
        onMouseOver="imgtog('prevt',2)" onMouseOut="imgtog('prevt',3)" style="cursor:hand" title="上一段"
     ><img name="pauzt" src="<%=exobudRootRath%>/exobud/img/btn_pauz_off.gif"    width=24 height=25 border=0 onClick="wmpPP()"
        onMouseOver="imgtog('pauzt',2)" onMouseOut="imgtog('pauzt',3)" style="cursor:hand" title="暂停．继续"
     ><img name="stopt" src="<%=exobudRootRath%>/exobud/img/btn_stop.gif"        width=24 height=25 border=0 onClick="wmpStop()"
        onMouseOver="imgtog('stopt',2)" onMouseOut="imgtog('stopt',3)" style="cursor:hand" title="停止"
     ><img name="playt" src="<%=exobudRootRath%>/exobud/img/btn_play.gif"        width=24 height=25 border=0 onClick="startExobud()"
        onMouseOver="imgtog('playt',2)" onMouseOut="imgtog('playt',3)" style="cursor:hand" title="播放"
     ><img name="nextt" src="<%=exobudRootRath%>/exobud/img/btn_next.gif"        width=24 height=25 border=0 onClick="playNext()"
        onMouseOver="imgtog('nextt',2)" onMouseOut="imgtog('nextt',3)" style="cursor:hand" title="下一段">

      <img name="plist" src="<%=exobudRootRath%>/exobud/img/btn_plist.gif"       width=24 height=25 border=0 onClick="openPlist()"
        onMouseOver="imgtog('plist',2)" onMouseOut="imgtog('plist',3)" style="cursor:hand" title="显示播放清单内容">

    </td>
  </tr>
</table>
    </td>
  </tr>
  <tr>
    <td height=0>
      <!-- 显示字幕框的部份：
           如果您想要使用字幕功能，您便需要预留页面一些空间用来显示这个字幕框。
           无论您是否使用字幕功能，请勿随便修改或删除下面这段  DIV 区块的语法。 -->
      <div id="capText" style="width:100%;height:60;color:white;background-color:#555555;padding-top:3px;padding-left:5px;display:none"
        >ExoBUD MP(II) 字幕显示系统(SMI)</div>
    </td>
  </tr>
</table>
