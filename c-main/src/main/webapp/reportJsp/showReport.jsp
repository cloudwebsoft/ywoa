<%@ page contentType="text/html;charset=GBK" %>
<%@page import="com.redmoon.oa.report.ReportManageDb" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="java.net.URLDecoder" %>
<%@ taglib uri="/WEB-INF/tlds/runqianReport4.tld" prefix="report" %>
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
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<link type="text/css" href="css/style.css" rel="stylesheet"/>
<script src="../inc/common.js" type="text/javascript"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
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

     .loading{
         display: none;
         position: fixed;
         z-index:1801;
         top: 45%;
         left: 45%;
         width: 100%;
         margin: auto;
         height: 100%;
     }
    .SD_overlayBG2 {
        background: #FFFFFF;
        filter: alpha(opacity = 20);
        -moz-opacity: 0.20;
        opacity: 0.20;
        z-index: 1500;
    }
    .treeBackground {
        display: none;
        position: absolute;
        top: -2%;
        left: 0%;
        width: 100%;
        margin: auto;
        height: 200%;
        background-color: #EEEEEE;
        z-index: 1800;
        -moz-opacity: 0.8;
        opacity: .80;
        filter: alpha(opacity = 80);
    }
</style>
<body topmargin=0 leftmargin=0 rightmargin=0 bottomMargin=0>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
    // request.setCharacterEncoding( "utf-8" );
    //String report = request.getParameter("raq");
    //report = new String (report.getBytes("iso8859-1"),"utf-8");
    //report = URLDecoder.decode(report,"utf-8");
    int id = ParamUtil.getInt(request, "id", -1);        //得到报表的主键

    if (id == -1) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " id=" + id));
        return;
    }

    String Authorization = ParamUtil.get(request, "Authorization");

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
    reportFileHome = "reportFiles";
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
            DebugUtil.i(getClass(), paramName, paramValue);
            DebugUtil.i(getClass(), paramName, ParamUtil.get(request, paramName));

            // out.print(paramName + "=" + paramValue + "<br/>");

            // 前后端分离后，不需要再转GB2312
            // paramValue = new String(paramValue.getBytes("iso-8859-1"), "GB2312");
            // DebugUtil.i(getClass(), paramName + "2", paramValue);
            // out.print(paramName + "2=" + paramValue + "<br/>");

            String paramValue2 = "";
            // if ("userName1".equals(paramName)) {
            // paramValue2 = new String(request.getParameter(paramName).getBytes("iso-8859-1"), "GB2312");
            // paramValue2 = new String(request.getParameter(paramName).getBytes("GBK"), "GBK");
            // paramValue2 = new String(request.getParameter(paramName).getBytes("iso-8859-1"), "GB2312");
            // paramValue2 = new String(request.getParameter(paramName).getBytes("iso-8859-1"), "utf8");

            paramValue2 = new String(request.getParameter(paramName).getBytes(), "GB2312");
            // }
            // out.print(paramName + "2===" + paramValue2 + "<br/>");
            if ("userName1".equals(paramName)) {
                paramValue2 = request.getParameter("userName");
            }

            if (paramValue != null) {
                /*try {
                    com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, paramName, paramValue, getClass().getName());
                } catch (ErrMsgException e) {
                    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
                    return;
                }
                */
                //把参数拼成name=value;name2=value2;.....的形式
                // param.append(paramName).append("=").append(paramValue).append(";");
                // param.append(paramName).append("=").append("鼓楼商务局1").append(";");
                // param.append(paramName).append("=").append(StrUtil.UrlEncode(paramValue)).append(";");
                param.append(paramName).append("=").append(paramValue2).append(";");
            }
        }
    }
    // out.print(param.toString());

    //以下代码是检测这个报表是否有相应的参数模板
    String paramFile = report.substring(0, iTmp) + "_arg.raq";
    // File f = new File(application.getRealPath(reportFileHome + File.separator + paramFile));
    File f = new File(Global.getAppPath() + reportFileHome + File.separator + paramFile);

    // out.print("report=" + report + "<BR>");
    // out.print("paramFile=" + paramFile + "<BR>");
    // out.print("param=" + param.toString());
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
                    <td style="background-color: #fff" <%--align="center" 如果置为居中，则某些超宽的报表因标题未居中，而查询条件居中则使得反而看起来不美观--%>>
                        <report:param name="form1" paramFileName="<%=paramFile%>"
                                      needSubmit="no"
                                      params="<%=param.toString()%>"/>
                    </td>
                    <td id="btnTd">
                        <a id="btnSearch" style="display: none; margin-left: 10px" href="javascript:showLoading(); _submit( form1 )">
                            <img src="../images/search.png" border=no style="vertical-align:middle">
                        </a>
                        <%
                            String val = StrUtil.getNullStr(request.getParameter("userName"));
                            String valGb2312 = new String(val.getBytes("iso-8859-1"), "GB2312");
                            // DebugUtil.i(getClass(), "valGb2312", valGb2312);
                        %>
                        <script>
                            function showLoading() {
                                $(".treeBackground").addClass("SD_overlayBG2");
                                $(".treeBackground").css({"display":"block"});
                                $(".loading").css({"display":"block"});
                            }
                            $(function() {
                                // o("form1").action = "showReport.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(valGb2312, "GB2312")%>";
                                // o("form1").action = "showReport.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(val, "GB2312")%>";
                                o("form1").action = "showReport.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(val)%>&Authorization=<%=Authorization%>";

                                // o("resultPage").value = "/reportJsp/showReport.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(valGb2312, "GB2312")%>";
                                // console.log("action=2" + o("form1").action);

                                // o('userName1').value = "%E9%BC%93%E6%A5%BC%E5%95%86%E5%8A%A1%E5%B1%801";
                                // o('userName1').value = "<%=val%>";
                                // o('form1').method = "GET";
                            });
                        </script>
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

        // 隐藏pdf按钮
        $('.pdf').parent().parent().hide();

        // 隐藏导出Excel对话框中的 请选择导出格式：2003、2007、OpenXML
        var sint = setInterval(function(){
            var frame = document.getElementById("popupFrame");
            if (frame) {
                var doc = frame.contentWindow.document;
                var row1 = doc.getElementById('formatRow1');
                if (row1) {
                    $(row1).hide();
                    $(doc.getElementById('formatRow2')).hide();
                    frame.contentWindow.excelFormat='OpenXML';
                }
            }
        },500);
    })
</script>
</html>
