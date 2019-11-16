<%@ page contentType="text/html;charset=GBK" %>
<%@page import="com.redmoon.oa.report.ReportManageDb" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="java.net.URLDecoder" %>
<%@ taglib uri="/WEB-INF/runqianReport4.tld" prefix="report" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.runqian.report4.usermodel.Context" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.redmoon.oa.visual.FormDAOLog" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<!-- 
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
-->
<link type="text/css" href="css/style.css" rel="stylesheet"/>
<script src="../js/jquery.js" type="text/javascript"></script>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<style type="text/css">
    #watermark {
        width: 100%;
        height: 100%;
        background: red;
        background-color: #ffffff;
        position: absolute;
        z-index: 10;
        opacity: 1;
        filter: alpha(opacity=100);
    }
</style>
<body topmargin=0 leftmargin=0 rightmargin=0 bottomMargin=0>
<%
    
    // request.setCharacterEncoding( "utf-8" );
    //String report = request.getParameter("raq");
    //report = new String (report.getBytes("iso8859-1"),"utf-8");
    //report = URLDecoder.decode(report,"utf-8");
    String id = request.getParameter("id");        //得到报表的主键
    
    if (id == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " id=" + id));
        return;
    }
    
    //根据传入文件的名称获取访问权限集合
    ReportManageDb rmdb = new ReportManageDb();
    rmdb = (ReportManageDb) rmdb.getQObjectDb(id);
    String priv_code = "";
    String report = "";
    if (rmdb != null) {
        priv_code = rmdb.getString("priv_code");
        report = rmdb.getString("name");
    }
    String[] privArr = priv_code.split(",");
    boolean isValid = false;
    if (privArr != null && privArr.length > 0) {
        for (int i = 0; i < privArr.length; i++) {
            if (privilege.isUserPrivValid(request, privArr[i]) || privArr[i].equals("")) {
                isValid = true;
                break;
            }
        }
    }
    if (!isValid) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    Config cfg = new Config();
    // 创建浏览日志
    if (cfg.getBooleanProperty("isModuleLogRead")) {
        FormDb fd = new FormDb("module_log_read");
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("read_type", String.valueOf(FormDAOLog.READ_TYPE_REPORT));
        fdao.setFieldValue("log_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        fdao.setFieldValue("module_code", "");
        fdao.setFieldValue("form_code", "");
        fdao.setFieldValue("module_id", String.valueOf(id));
        fdao.setFieldValue("form_name", "");
        fdao.setFieldValue("user_name", privilege.getUser(request));
        fdao.setCreator(privilege.getUser(request)); // 参数为用户名（创建记录者）
        fdao.setUnitCode(privilege.getUserUnitCode(request)); // 置单位编码
        fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis())); // 置冗余字段“流程编码”，可用于取出刚插入的记录，也可以为空
        boolean re = fdao.create();
    }
    
    String reportFileHome = Context.getInitCtx().getMainDir();
    StringBuffer param = new StringBuffer();
    
    //保证报表名称的完整性
    int iTmp = 0;
    if ((iTmp = report.lastIndexOf(".raq")) <= 0) {
        report = report + ".raq";
        iTmp = 0;
    }
    
    Enumeration paramNames = request.getParameterNames();
    if (paramNames != null) {
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            paramValue = new String(paramValue.getBytes("iso-8859-1"), "GB2312");
            if (paramValue != null) {
                try {
                    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, paramName, paramValue, getClass().getName());
                } catch (ErrMsgException e) {
                    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
                    return;
                }
                
                //把参数拼成name=value;name2=value2;.....的形式
                param.append(paramName).append("=").append(paramValue).append(";");
            }
        }
    }
    
    //以下代码是检测这个报表是否有相应的参数模板
    String paramFile = report.substring(0, iTmp) + "_arg.raq";
    File f = new File(application.getRealPath(reportFileHome + File.separator + paramFile));

%>
<jsp:include page="toolbar.jsp" flush="false"/>
<table id="rpt" align="center">
    <tr>
        <td>
            <% //如果参数模板存在，则显示参数模板
                if (f.exists()) {
            %>
            <table id="param_tbl" width="100%" height="100%">
                <tr>
                    <td <%--align="center" 如果置为居中，则某些超宽的报表因标题未居中，而查询条件居中则使得反而看起来不美观--%>>
                        <report:param name="form1" paramFileName="<%=paramFile%>"
                                      needSubmit="no"
                                      params="<%=param.toString()%>"
                        
                        />
                    </td>
                    <td id="btnTd">
                        <a id="btnSearch" style="display: none; margin-left: 10px" href="javascript:_submit( form1 )">
                            <img src="../images/search.png" border=no style="vertical-align:middle">
                        </a>
                    </td>
                </tr>
            </table>
            <%
                }
            %>
            <report:html name="report1" reportFileName="<%=report%>"
                         funcBarLocation="top"
                         needPageMark="no"
                         generateParamForm="no"
                         needPrintPrompt="no"
                         params="<%=param.toString()%>"
                         exceptionPage="/reportJsp/myError2.jsp"
                         appletJarName="runqianReport4Applet.jar,dmGraphApplet.jar"
                         width="-1"
                         height="-1"
            />
        </td>
    </tr>
</table>
<div id="watermark" style="width: 2000px;height: 50px; margin-top: -30px;"></div>
<script language="javascript">
    //设置分页显示值

    document.getElementById("t_page_span").innerHTML = report1_getTotalPage();
    document.getElementById("c_page_span").innerHTML = report1_getCurrPage();

    // 财务报表查询
    function query() {
        var start_date = document.getElementById("begin_date").value;
        var end_date = document.getElementById("end_date").value;
        var start_month = document.getElementById("month_1").value;
        var end_month = document.getElementById("month_2").value;
        window.location.href = "<%=request.getContextPath()%>/reportJsp/showReport.jsp?id=<%=id%>&start_date=" + start_date + "&end_date=" +
            end_date + "&start_month=" + start_month + "&end_month=" + end_month;
    }

    function jb_query() {
        var year = document.getElementById("year").value;
        window.location.href = "<%=request.getContextPath()%>/reportJsp/showReport.jsp?id=<%=id%>&year=" + year;

    }

    // 将页面生成的一个提交隐藏
    window.onload = function () {
        $('#report1_prompt').hide();
        $('#watermark').hide();
        if (document.getElementById("runqian_submit"))
            document.getElementById("runqian_submit").style.display = "none";
    }
</script>
</body>
<script>
    // 使查询按钮紧靠在参数控件右侧
    $(function () {
        // 找到含有参数控件的那一行
        var $trs = $('#form1_tbl tr');
        $trs.each(function(k) {
            var $tr = $(this).children('td');
            // 判断tr是否显示
            if ($(this).css("display") != "none") {
                var lastParamTdIndex = -1;
                $tr.each(function (i) {
                    if ($(this).attr('paramname')) {
                        lastParamTdIndex = i;
                    }
                });
                if (lastParamTdIndex != -1) {
                    $tr.eq(lastParamTdIndex + 1).html($('#btnTd').html());
                    return false;
                }
            }
        })

        $('#btnSearch').show();
    })
</script>
</html>
