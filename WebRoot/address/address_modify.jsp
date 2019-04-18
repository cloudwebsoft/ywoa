<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudweb.oa.service.AddressService" %>
<%@ page import="com.cloudweb.oa.bean.Address" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="../inc/inc.jsp" %>
<%
    String strtype = ParamUtil.get(request, "type");
    int type = AddressService.TYPE_USER;
    if (!strtype.equals(""))
        type = Integer.parseInt(strtype);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>编辑通讯录</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script language=JavaScript src='../js/jquery1.7.2.min.js'></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td class="tdStyle_1">通讯录</td>
    </tr>
</table>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    int id = ParamUtil.getInt(request, "id");

    // AddressDb addr = new AddressDb();
    AddressService addressService = new AddressService();
    Address addr = addressService.getAddress(id);
    // addr = addr.getAddressDb(id);

    String person = "", job = "", tel = "", mobile = "", email = "", address = "", postalcode = "", introduction = "", business = "";
    person = addr.getPerson();
    job = addr.getJob();
    tel = addr.getTel();
    mobile = addr.getMobile();
    email = addr.getEmail();
    address = addr.getAddress();
    postalcode = StrUtil.getNullString(addr.getPostalcode());
    if (postalcode.equals(""))
        postalcode = "&nbsp;";
    introduction = addr.getIntroduction();
    if (business.equals(""))
        business = "&nbsp;";
%>
<form name="form1" id="form1" action="edit.do?id=<%=id%>" method="post">
    <table width="98%" border="0" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
        <tr>
            <td class="tabStyle_1_title" colspan="4">分组</td>
        </tr>
        <tr>
            <td align="center">类别</td>
            <td colspan="3">
                <%
                    String who = privilege.getUser(request);
                    if (type == AddressService.TYPE_PUBLIC)
                        who = Leaf.USER_NAME_PUBLIC;
                %>
                <select name="typeId" id="typeId">
                    <option value="">-----请选择-----</option>
                    <%
                        Leaf lf = new Leaf();
                        lf = lf.getLeaf(who);
                        DirectoryView dv = new DirectoryView(lf);
                        int rootlayer = 1;
                        dv.ShowDirectoryAsOptionsWithCode(out, lf, rootlayer);
                    %>
                </select>
                <script>
                    o("typeId").value = "<%=addr.getTypeId()%>";
                </script>
            </td>
        </tr>
        <tr>
            <td class="tabStyle_1_title" colspan="4">个人信息</td>
        </tr>
        <tr>
            <td width="8%" align="center">姓名</td>
            <td colspan="3" width="46%"><input name="person" size=25 value="<%=person%>">
                <input name="type" type="hidden" value="<%=addr.getType()%>">
                <input name="id" type="hidden" value="<%=addr.getId()%>">
                <script>
                    var person = new LiveValidation('person');
                    person.add(Validate.Presence);
                    person.add(Validate.Length, {minimum: 1, maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">单位</td>
            <td colspan="3"><input name="company" size=35 value="<%=addr.getCompany()%>">
                <script>
                    var company = new LiveValidation('company');
                    company.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">职务</td>
            <td colspan="3"><input name="job" size=35 value="<%=addr.getJob()%>">
                <script>
                    var job = new LiveValidation('job');
                    job.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">手机</td>
            <td colspan="3"><input name="mobile" size="35" value="<%=mobile%>"/>
                <script>
                    var mobile = new LiveValidation('mobile');
                    mobile.add(Validate.Mobile);
                    //mobile.add( Validate.Length, { is: 11 } );
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">短号</td>
            <td colspan="3"><input name="MSN" size="35" value="<%=addr.getMSN()%>"/>
                <script>
                    var MSN = new LiveValidation('MSN');
                    MSN.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">Email</td>
            <td colspan="3"><input name="email" size=35 value="<%=addr.getEmail()%>">
                <script>
                    var email = new LiveValidation('email');
                    email.add(Validate.Email);
                    email.add(Validate.Length, {maximum: 40});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">微信</td>
            <td colspan="3"><input name="weixin" size=35 value="<%=addr.getWeixin()%>">
                <script>
                    var weixin = new LiveValidation('weixin');
                    weixin.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">电话</td>
            <td colspan="3"><input name="tel" size=25 value="<%=addr.getTel()%>">
                <script>
                    var tel = new LiveValidation('tel');
                    tel.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">传真</td>
            <td colspan="3"><input name="fax" size=35 value="<%=addr.getFax()%>">
                <script>
                    var fax = new LiveValidation('fax');
                    fax.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">QQ</td>
            <td colspan="3"><input name="QQ" size=35 value="<%=addr.getQQ()%>">
                <script>
                    var QQ = new LiveValidation('QQ');
                    QQ.add(Validate.Numericality);
                    QQ.add(Validate.Length, {maximum: 20});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">网页</td>
            <td colspan="3"><input name="web" size=35 value="<%=addr.getWeb()%>">
                <script>
                    var web = new LiveValidation('web');
                    web.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">邮编</td>
            <td colspan="3"><input name="postalcode" size=35 value="<%=addr.getPostalcode()%>">
                <script>
                    var companyPostcode = new LiveValidation('companyPostcode');
                    companyPostcode.add(Validate.Numericality);
                    companyPostcode.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td align="center">地址</td>
            <td colspan="3"><input name="address" size=45 value="<%=addr.getAddress()%>">
                <script>
                    var address = new LiveValidation('address');
                    address.add(Validate.Length, {maximum: 50});
                </script>
            </td>
        </tr>
        <tr>
            <td height="17" align="center">附<span class="stable">&nbsp;&nbsp;</span>注</td>
            <td height="17" colspan="3"><textarea name="introduction" cols="50"
                                                  rows="8"><%=addr.getIntroduction()%> </textarea>
                <script>
                    var introduction = new LiveValidation('introduction');
                    introduction.add(Validate.Length, {maximum: 500});
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="4" align="center">
                <input type="text" id="unitCode" name="unitCode" hidden value="<%=addr.getUnitCode()%>"/>
                <input type="text" id="userName" name="userName" hidden value="<%=addr.getUserName()%>"/>
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
                url: "edit.do?id=<%=id%>",
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
