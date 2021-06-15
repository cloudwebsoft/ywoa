<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.module.cms.site.*" %>
<%@ page import="com.redmoon.oa.fileark.Attachment" %>
<%@ page import="com.redmoon.oa.fileark.Document" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id");
    UserDesktopSetupDb udsd = new UserDesktopSetupDb();
    udsd = udsd.getUserDesktopSetupDb(id);
    int count = udsd.getCount();
%>
<div id="drag_<%=id%>" class="portlet drag_div bor ibox" style="border:0px;padding:0px;">
    <div id="drag_<%=id%>_h" style="height:2px;padding:0px;margin:0px; font-size:1px"></div>
    <div id="cont_<%=id%>" class="portlet_content ibox-content" style="min-height:141px;_height:141px;padding:0px;margin:0px">
        <div>
            <div id="flexslider<%=id%>" class="flexslider">
                <ul class="slides">
                    <%
                        String strId = udsd.getModuleItem();
                        long flashImgId = StrUtil.toLong(strId, -1);
                        if (flashImgId != -1) {
                            SiteFlashImageDb sfid = new SiteFlashImageDb();
                            sfid = (SiteFlashImageDb) sfid.getQObjectDb(new Long(flashImgId));
                            if (sfid != null) {
                                if (sfid.getInt("is_auto") == 0) {
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
                            <a href="javascript:;" onclick="addTab('文档', '<%=imgLink%>')">
                                <img title="<%=imgText%>" src="<%=imgUrl%>" style="height:240px" align="absmiddle"/>
                            </a>
                        </div>
                    </li>
                    <%
                                    }
                                } else {
                                    Document doc = new Document();
                                    Attachment attachment = new Attachment();
                                    String dirCode = sfid.getString("dir_code");
                                    Vector<Attachment> v = attachment.getFlashImagesOfDirs(dirCode);
                                    if (v.size()==0) {
                                        out.print("图片轮播 目录中无图片");
                                    }
                                    else {
                                        Iterator<Attachment> ir = v.iterator();
                                        while (ir.hasNext()) {
                                            attachment = ir.next();
                                            doc = doc.getDocument(attachment.getDocId());
                                            if (doc == null) {
                                                continue;
                                            }
                    %>
                    <li>
                        <a href="javascript:;" onclick="addTab('<%=StrUtil.HtmlEncode(doc.getTitle())%>', '<%=request.getContextPath()%>/doc_show.jsp?id=<%=doc.getID()%>')">
                            <img title="<%=doc.getTitle()%>" src="<%=request.getContextPath()%>/<%=attachment.getVisualPath()%>/<%=attachment.getDiskName()%>" style="height:240px" align="absmiddle"/>
                            <div class="flex-caption"><%=doc.getTitle()%></div>
                        </a>
                    </li>
                    <%
                                        }
                                    }
                                }
                            } else {
                                out.print("图片轮播 " + flashImgId + " 不存在");
                            }
                        }
                    %>
                </ul>
            </div>
        </div>
    </div>
</div>
<script>
    // 绑定的事件不稳定，有时生效，有时不生效，因为在desktop.jsp对于load进来的元素重新作了删除操作，所以事件可能会丢失，故此处代码已移至desktop.jsp中
    /*jQuery(document).ready(function () {
        jQuery('#flexslider<%=id%>').flexslider({
            animation: "slide",
            controlNav: true,
            slideshow: true,
            controlNav: true,
            directionNav: true,
            pauseOnAction: false,
            // pauseOnHover: true,
            slideshowSpeed: 3000,
            start: function (slider) {
            }
        });
    });*/
</script>