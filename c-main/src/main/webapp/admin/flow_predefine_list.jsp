<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudweb.oa.config.JwtProperties" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%
    Privilege privilege = new Privilege();
    String priv = "admin.flow";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String dirCode = ParamUtil.get(request, "dirCode");

    if (dirCode.equals("")) {
        out.print(SkinUtil.makeInfo(request, "请选择流程类型！"));
        return;
    }
    Leaf lf = new Leaf();
    lf = lf.getLeaf(dirCode);
    if (lf == null || !lf.isLoaded()) {
        out.print(SkinUtil.makeErrMsg(request, "节点不存在！"));
        return;
    }

	// 用于前端集成
    JwtProperties jwtProperties = SpringUtil.getBean(JwtProperties.class);
    String header = jwtProperties.getHeader();
    String headerVal = ParamUtil.get(request, header);

	// 如果是分类节点，则重定向至表单处理页面
    if (lf.getType() == Leaf.TYPE_NONE) {
        // response.sendRedirect("form_m.jsp?flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
        response.sendRedirect("flow_predefine_dir.jsp?op=modify&" + header + "=" + headerVal + "&code=" + StrUtil.UrlEncode(lf.getCode()));
        return;
    } else if (lf.getType() == Leaf.TYPE_FREE) {
        response.sendRedirect("flow_predefine_free.jsp?" + header + "=" + headerVal + "&flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
        return;
    } else {
        response.sendRedirect("flow_predefine_init_myflow.jsp?" + header + "=" + headerVal + "&flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
    }
%>
