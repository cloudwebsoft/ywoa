<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id");
    UserDesktopSetupDb udsd = new UserDesktopSetupDb();
    udsd = udsd.getUserDesktopSetupDb(id);
    String kind = ParamUtil.get(request, "kind"); // 是否来自于lte界面

    String dirCode = udsd.getModuleItem();
    String[] ary = StrUtil.split(dirCode, ",");
    Document doc = new Document();
%>
<style>
    .file-icon {
        width: 20px;
        height: 20px;
    }
    .con {
        display:none;
        /*padding: 15px 10px;*/
    }
    .nav-tab {
        list-style:none;
        height:47px;
        overflow:hidden;
        margin-bottom: 0px;
    }
    .nav-tab li {
        float:left;
        width:100px;
        height:100px;
        background:#eee;
        color:#000;
        text-align:center;
        line-height:45px;
        cursor: pointer;
    }
    .nav-con-on {
        display:block;
    }
    .nav-tab li.act {
        background:#fff;
        color:#000;
    }
</style>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor ibox">
    <%if (ary!=null && ary.length==1) {%>
    <div id="drag_<%=id%>_h" class="box ibox-title">
        <!-- <div class="opbut-1"> <img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" width="19" height="19"/></div>  -->
        <!-- <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" width="19" height="19"/></div>  -->
        <!-- <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" width="19" height="19"/></div>  -->
        <%
            if ("lte".equals(kind)) {
        %>
        <h5>
            <i class="fa <%=udsd.getIcon()%>"></i>&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/fileark/document_list_m.jsp?dir_code=<%=dirCode%>"><%=udsd.getTitle()%></a>
        </h5>
        <%
        }
        else {
        %>
        <div class="titleimg">
            <!--<img src="images/desktop/notepaper.png" width="40" height="40" />-->
            <i class="fa <%=udsd.getIcon()%>"></i>
            &nbsp;&nbsp;</div>
        <div class="titletxt">&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/fileark/document_list_m.jsp?dir_code=<%=dirCode%>"><%=udsd.getTitle()%></a></div>
        <%
            }
        %>
    </div>
    <div id="drag_<%=udsd.getId()%>_c" class="portlet_content ibox-content article">
        <%
            com.redmoon.oa.fileark.Leaf lf = new com.redmoon.oa.fileark.Leaf();
            lf = lf.getLeaf(dirCode);
            boolean canSee = true;
            if (lf == null) {
                out.print("<div class='no_content'><img title='文件柜无内容' src='images/desktop/no_content.jpg'></div>");
                canSee = false;
            }
            if (!"".equals(dirCode)) {
                com.redmoon.oa.fileark.LeafPriv lp = new com.redmoon.oa.fileark.LeafPriv(dirCode);
                if (!lp.canUserSee(request)) {
                    out.print(SkinUtil.LoadString(request, "pvg_invalid"));
                    canSee = false;
                }
            }
            if (canSee) {
                String str = doc.getDesktopList(request, udsd, dirCode);
                out.print(str);
            }
        %>
    </div>
    <%
        }
        else {
    %>
        <div id="drag_<%=id%>_h" class="drag-h" style="height:1px;padding:0px;margin:0px; font-size:1px"></div>
        <ul class="nav-tab" style="border-color: #e7eaec; border-style:solid solid none; border-width: 4px 0 0;">
        <%
            Leaf lf = new Leaf();
            for (int i = 0; i < ary.length; i++) {
                String cls = i == 0 ? "act" : "";
                lf = lf.getLeaf(ary[i]);
                if (lf==null) {
                    lf = new Leaf();
                    continue;
                }
        %>
        <li class="<%=cls%>" code="<%=lf.getCode()%>"><%=lf.getName()%></li>
        <%
            }
        %>
    </ul>
    <%
        for (int i=0; i<ary.length; i++) {
            String cls = i==0?"nav-con-on":"";
            lf = lf.getLeaf(ary[i]);
            if (lf==null) {
                lf = new Leaf();
                continue;
            }
            com.redmoon.oa.fileark.LeafPriv lp = new com.redmoon.oa.fileark.LeafPriv(ary[i]);
            if (!lp.canUserSee(request)) {
                out.print(SkinUtil.LoadString(request, "pvg_invalid"));
                continue;
            }
    %>
            <div id="drag_<%=udsd.getId()%>_c" class="con <%=cls%> portlet_content ibox-content article">
            <%
                String str = doc.getDesktopList(request, udsd, ary[i]);
                out.print(str);
            %>
            </div>
    <%
        }
    %>
    <script>
        // 绑定的事件不稳定，有时生效，有时不生效，因为在desktop.jsp对于load进来的元素重新作了删除操作，所以事件可能会丢失，故此处代码已移至desktop.jsp中
        /*// 防止门户上有多块文件柜版块时，导致事件加载多次，致选项卡点击一次打开多个页签
        if (typeof hoverEventInit<%=id%> == "undefined" || hoverEventInit<%=id%> == null) {
            // consoleLog("hoverEventInit<%=id%> is not defined");
            hoverEventInit<%=id%> = 1;
            jQuery(function() {
                jQuery("#drag_<%=id%> .nav-tab li").hover(function(e) {
                    jQuery("#drag_<%=id%> .nav-tab li").eq(jQuery(this).index()).addClass("act").siblings().removeClass('act');
                    jQuery("#drag_<%=id%> .con").hide().eq(jQuery(this).index()).show();
                });

                jQuery("#drag_<%=id%> .nav-tab li").click(function(e) {
                    var $li = jQuery(this);
                    addTab($li.text(), '<%=request.getContextPath()%>/fileark/document_list_m.jsp?dir_code=' + $li.attr('code'));
                });
            });
        }*/
    </script>
    <%
        }
    %>
</div>