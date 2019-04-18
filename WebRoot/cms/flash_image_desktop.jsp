<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.module.cms.site.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
/*
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);
int count = udsd.getCount();
%>
<div id="drag_<%=id%>" class="portlet drag_div bor" style="border:0px;padding:0px;" >
    <div id="drag_<%=id%>_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
    <div id="cont_<%=id%>" class="portlet_content" style="min-height:141px;_height:141px;padding:0px;margin:0px">
    	<div>
        <div id="flexslider<%=id%>" class="flexslider">
        <ul class="slides">
        <%
        String siteCode = com.redmoon.oa.fileark.Leaf.ROOTCODE;
        String strId = udsd.getModuleItem();
        long flashImgId = StrUtil.toLong(strId, -1);
        if (flashImgId!=-1) {
            SiteFlashImageDb sfid = new SiteFlashImageDb();
            sfid = (SiteFlashImageDb)sfid.getQObjectDb(new Long(flashImgId));
            for (int i = 1; i <= 5; i++) {
                String imgUrl = StrUtil.getNullStr(sfid.getString("url" + i));
                String imgText = StrUtil.getNullStr(sfid.getString("title" + i));
                String imgLink = StrUtil.getNullStr(sfid.getString("link" + i));
                if ("".equals(imgUrl)) {
                	continue;
                }
                %>
                  <li>
                  <div>
                  <a href="javascript:;" onclick="addTab('文档', '<%=imgLink%>')"><img title="<%=imgText%>" src="<%=imgUrl%>" style="height:240px" align="absmiddle" /></a>
                  </div>
                  </li>
            <%}
        }%>    
      	</ul>
        </div>

    	</div>
  </div>
</div>

<script>
jQuery(document).ready(function(){
  jQuery('#flexslider<%=id%>').flexslider({
	animation: "slide",
	controlNav: false,
	start: function(slider){
	}
  });
});	
</script>