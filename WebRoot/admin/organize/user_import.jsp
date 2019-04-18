<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.io.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@page import="org.apache.poi.hssf.usermodel.*" %>
<%@page import="org.apache.poi.ss.usermodel.*" %>
<%@page import="org.apache.poi.xssf.usermodel.*" %>
<%@page import="org.apache.poi.openxml4j.exceptions.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script type="text/javascript" src="../../js/jquery.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
<%
    String priv = "admin.user";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
    String op = ParamUtil.get(request, "op");
    String[][] info = null;
    request.getSession().removeAttribute("info");
    if (op.equals("import")) {
        FileUpMgr fum = new FileUpMgr();
        String excelFile = "";
        try {
            excelFile = fum.uploadExcel(application, request);
            if (excelFile.equals("")) {
                //out.print("<script type='text/javascript'>parent.hiddenLoading();</script>");
                out.print(StrUtil.jAlert_Back("请上传excel文件", "提示"));
                out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");

                return;
            }
        } catch (ErrMsgException e) {
            //out.print("<script type='text/javascript'>parent.hiddenLoading();</script>");
            out.print(StrUtil.jAlert_Back("请上传excel文件", "提示"));
            out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
            return;
        }
        FileInputStream in = new FileInputStream(excelFile);
        int cols = 19;
        try {
            if (excelFile.endsWith("xls")) {
                HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
                HSSFSheet sheet = w.getSheetAt(0);
                if (sheet != null) {
                    // 获取行数
                    int rowcount = sheet.getLastRowNum();
                    info = new String[rowcount][cols];
                    // 获取每一行
                    for (int k = 1; k <= rowcount; k++) {
                        HSSFRow row = sheet.getRow(k);
                        if (row != null) {
                            for (int i = 0; i < cols; i++) {
                                HSSFCell cell = row.getCell(i);
                                if (cell == null) {
                                    info[k - 1][i] = "";
                                } else {
                                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                    info[k - 1][i] = StrUtil.getNullStr(cell.getStringCellValue()).trim();
                                }
                            }
                        }
                    }
                }
            } else if (excelFile.endsWith("xlsx")) {
                XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
                XSSFSheet sheet = w.getSheetAt(0);
                if (sheet != null) {
                    int rowcount = sheet.getLastRowNum();
                    info = new String[rowcount][cols];
                    for (int k = 1; k <= rowcount; k++) {
                        XSSFRow row = sheet.getRow(k);
                        if (row != null) {
                            for (int i = 0; i < cols; i++) {
                                XSSFCell cell = row.getCell(i);
                                if (cell == null) {
                                    info[k - 1][i] = "";
                                } else {
                                    cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                    info[k - 1][i] = StrUtil.getNullStr(cell.getStringCellValue()).trim();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.getLog(page.getClass()).equals("用户导入出现异常");
        } catch (InvalidFormatException e) {
            LogUtil.getLog(page.getClass()).equals("用户导入出现异常");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LogUtil.getLog(page.getClass()).equals("用户导入出现异常");
                }
            }
        }
        java.io.File file = new java.io.File(excelFile);
        file.delete();
        request.getSession().setAttribute("info", info);
        request.getRequestDispatcher("/admin/organize/user_import_confirm.jsp").forward(request, response);
    } else if (op.equals("sync")) {
        DeptUserMgr dum = new DeptUserMgr();
        dum.syncUnit();
        out.print(StrUtil.jAlert_Redirect("同步成功！", "提示", "user_import.jsp"));
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>用户导入</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css"/>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
    <script src="<%=request.getContextPath() %>/inc/upload.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css"/>
</head>
<style>
    /*兼容IE8下上传文件样式问题IE8*/
    .upload {
        width: 88px;
    }

    .uploadFile {
        margin-top: -30px;
    }
</style>
<body>
<div class="oranize-number">
    <!--最底层灰色条-->
    <div class="oranize-number-linegray"></div>
    <!--蓝色条1-->
    <div class="oranize-number-lineblue1"></div>
    <!--灰色条2-->
    <div style="display:none;" class="oranize-number-lineblue3"></div>


    <!--1步-->
    <div class="oranize-blue1">1</div>
    <!--2步-->
    <div class="oranize-gray2">2</div>
    <!--3步-->
    <div class="oranize-gray3">3</div>
    <!--1步文字-->
    <div class="oranize-txt1 oranize-txt-sel">导入Excel</div>
    <!--2步文字-->
    <div class="oranize-txt2">确认信息</div>
    <!--3步文字-->
    <div class="oranize-txt3">完成</div>
</div>
<form action="?op=import" method="post" enctype="multipart/form-data" name="form1" id="form1"
      onSubmit="return submitCheck()">
    <table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
        <thead>
        <tr>
            <td class="tabStyle_1_title">导入excel</td>
        </tr>
        </thead>
        <tr>
            <td align="left">1.编辑Excel电子表格信息，将员工信息按照模板（<a href="javascript:;" style="color:blue;"
                                                          onclick="downloadTemplate() ">下载模板</a>）进行整理
            </td>
        </tr>
        <tr>
            <td align="left">2.选择整理完成的Excel文件进行上传</td>
        </tr>
        <tr>
            <td align="left">3.如果导入已完成，点击此处<a href="javascript:;" title="在导入用户后同步用户所在单位" style="color:blue;"
                                              onclick="syncUnitUser()">同步</a>用户所在单位
            </td>
        </tr>
        <tr>
            <td align="left">4.导入功能不支持兼职，原兼职部门将被清除</td>
        </tr>
        <tr>
            <td align="left">
                <script>initUpload()</script>
            </td>
        </tr>
        <tr>
            <td align="left"></td>
        </tr>
        <tr>
            <td align="left"></td>
        </tr>

        <tr>
            <td align="center" style="height:50px"><input type="submit" value="下一步" class="org-btn"/>
            </td>
            <%
                if ("introduction".equals(flag)) {
            %>
            <script>
                jQuery(document).ready(function () {
                    var tour = {
                        id: "hopscotch",
                        steps: [{
                            title: "提示",
                            content: "此处可以下载模版，请根据模版编写Excel(红色*号为必填项)",
                            target: "btn",
                            placement: "bottom",
                            showNextButton: false
                        }]
                    };
                    hopscotch.startTour(tour);
                });
            </script>
            <%
                }
            %>
        </tr>
    </table>
</form>
</body>
<script>
    function downloadTemplate() {
        //window.location.href="user_import_template.jsp";
        window.location.href = "<%=request.getContextPath() %>/admin/organize/user_template.xls";
    }
    ;
    // 表单提交校验
    function submitCheck() {
        //parent.showLoading();
    }

    function syncUnitUser() {
        jConfirm('您确定要同步么？', '提示', function (r) {
            if (r) {
                window.location.href = "user_import.jsp?op=sync";
            }
        });
    }
</script>
</html>