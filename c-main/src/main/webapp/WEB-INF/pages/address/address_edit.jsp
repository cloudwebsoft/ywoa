<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>通讯录-修改</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery.toaster.js"></script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tdStyle_1">通讯录</td>
    </tr>
</table>
<form name="form1" id="form1" action="edit.do?id=${addr.id}" method="post" style="margin-top: 10px">
    <table width="98%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
        <tr>
            <td class="tabStyle_1_title" colspan="4">分组</td>
        </tr>
        <tr>
            <td align="center">类别</td>
            <td colspan="3">
                <select name="typeId" id="typeId">
                    <option value="">-请选择-</option>
                    ${dirOpts}
                </select>
                <script>
                    o("typeId").value = "${addr.typeId}";
                </script>
            </td>
        </tr>
        <tr>
            <td class="tabStyle_1_title" colspan="4">个人信息</td>
        </tr>
        <tr>
            <td width="8%" align="center">姓名</td>
            <td colspan="3" width="46%"><input name="person" size=25 value="${addr.person}">
                <input name="type" type="hidden" value="${addr.type}"/>
                <input name="id" type="hidden" value="${addr.id}">
                <script>
                    var person = new LiveValidation('person');
                    person.add(Validate.Presence);
                    person.add(Validate.Length, {minimum: 1, maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">单位</td>
            <td colspan="3"><input name="company" size=35 value="${addr.company}">
                <script>
                    var company = new LiveValidation('company');
                    company.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">职务</td>
            <td colspan="3"><input name="job" size=35 value="${addr.job}">
                <script>
                    var job = new LiveValidation('job');
                    job.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">手机</td>
            <td colspan="3"><input name="mobile" size="35" value="${addr.mobile}"/>
                <script>
                    var mobile = new LiveValidation('mobile');
                    mobile.add(Validate.Mobile);
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">短号</td>
            <td colspan="3"><input name="MSN" size="35" value="${addr.MSN}"/>
                <script>
                    var MSN = new LiveValidation('MSN');
                    MSN.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">Email</td>
            <td colspan="3"><input name="email" size=35 value="${addr.email}">
                <script>
                    var email = new LiveValidation('email');
                    email.add(Validate.Email);
                    email.add(Validate.Length, {maximum: 40});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">微信</td>
            <td colspan="3"><input name="weixin" size=35 value="${addr.weixin}">
                <script>
                    var weixin = new LiveValidation('weixin');
                    weixin.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">电话</td>
            <td colspan="3"><input name="tel" size=25 value="${addr.tel}">
                <script>
                    var tel = new LiveValidation('tel');
                    tel.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">传真</td>
            <td colspan="3"><input name="fax" size=35 value="${addr.fax}">
                <script>
                    var fax = new LiveValidation('fax');
                    fax.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">QQ</td>
            <td colspan="3"><input name="QQ" size=35 value="${addr.QQ}">
                <script>
                    var QQ = new LiveValidation('QQ');
                    QQ.add(Validate.Numericality);
                    QQ.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">网页</td>
            <td colspan="3"><input name="web" size=35 value="${addr.web}">
                <script>
                    var web = new LiveValidation('web');
                    web.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">邮编</td>
            <td colspan="3"><input name="postalcode" size=35 value="${addr.postalcode}">
                <script>
                    var companyPostcode = new LiveValidation('companyPostcode');
                    companyPostcode.add(Validate.Numericality);
                    companyPostcode.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">地址</td>
            <td colspan="3"><input name="address" size=45 value="${addr.address}">
                <script>
                    var address = new LiveValidation('address');
                    address.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td height="17" align="center">附<span class="stable">&nbsp;&nbsp;</span>注</td>
            <td height="17" colspan="3"><textarea name="introduction" cols="50"
                                                  rows="8">${addr.introduction}</textarea>
                <script>
                    var introduction = new LiveValidation('introduction');
                    introduction.add(Validate.Length, {maximum: 500});
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="4" align="center">
                <input type="text" id="unitCode" name="unitCode" hidden value="${addr.unitCode}"/>
                <input type="text" id="userName" name="userName" hidden value="${addr.userName}"/>
                <input id="btnSubmit" class="btn" type="button" value="确 定">
            </td>
        </tr>
    </table>
    <script>
        $('#btnSubmit').click(function() {
            var params = $("#form1").serialize();
            console.log(params);
            $.ajax({
                type: "post",
                url: "save.do?id=${addr.id}",
                contentType:"application/x-www-form-urlencoded; charset=UTF-8",
                data: params,
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                    $('body').showLoading();
                },
                success: function(data, status){
                    data = $.parseJSON(data);
                    if (data.ret == "1") {
                        $.toaster({priority: 'info', message: data.msg});
                        <%
                            String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
                        %>
                        reloadTab("<%=tabIdOpener%>");
                    } else {
                        $.toaster({priority: 'info', message: data.msg});
                    }
                },
                complete: function(XMLHttpRequest, status){
                    $('body').hideLoading();
                },
                error: function(XMLHttpRequest, textStatus){
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        })
    </script>
</form>
</body>
</html>