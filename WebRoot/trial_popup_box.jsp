<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/trial_popup.css" />
<div id="pop" class="popup_box" style="display:none">
  <div class="popup_fram">
    <div class="popup_title">提示
       <div class="popup_close"><a id="popCloseBtn" href="javascript:"><img src="<%=SkinMgr.getSkinPath(request)%>/images/popup_close-1.png" width="30" height="30" /></a></div>
    </div>
     <div class="popup_kh"><a href="javascript:void(0);" id="openYimiHome">http://www.yimihome.com(一米之家)</a></div>
    <div class="popup_txt">
    <p>您的系统还没有注册，现在注册试用智能平台，体验高端报表、手机客户端！</p>
    </div>
    <div class="popup_button"><a href="javascript:void(0);" id="popRegistBtn">注册永久许可证</a></div>
    <div class="popup_button_cancel"><a href="javascript:void(0)" id="loginInSys">进入系统</a></div>
    </div>
</div>