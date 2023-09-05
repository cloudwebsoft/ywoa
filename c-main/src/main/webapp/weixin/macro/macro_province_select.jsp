<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    Privilege pvg = new Privilege();
    if (!pvg.isUserLogin(request)) {
        return;
    }

    String formCode = ParamUtil.get(request, "formCode");
    String fieldName = ParamUtil.get(request, "fieldName");
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    FormField ff = fd.getFormField(fieldName);

    String relateStr = ff.getDefaultValueRaw();
    if ("".equals(relateStr)) {
        relateStr = ff.getDescription();
    }
    String[] ary = StrUtil.split(relateStr, ",");
    boolean isCity = false, isCountry = false;

    String cityId = "", countryId = "";
    if (ary != null) {
        if (ary.length >= 1) {
            isCity = true;
            cityId = ary[0];
        }
        if (ary.length == 2) {
            isCountry = true;
            countryId = ary[1];
        }
    }

    String rid = cityId; // RandomSecquenceCreator.getId(6);
%>
    var cityId = '<%=cityId%>';
    var countryId = '<%=countryId%>';
    var isCity = <%=isCity%>;
    var isCountry = <%=isCountry%>;

    var errFuncCityCountry<%=rid%> = function (response) {
        mui.toast(response.responseText);
    }

    function doGetCityCountry<%=rid%>XXX(response) {
        if (response.responseText.trim() == '') return;
        var ary = response.responseText.trim().split('|');
        if (isCity) {
            if (o(cityId) == null) mui.toast('缺少城市输入框');
            else if (ary[0] != '') {
                var c = $('#' + cityId).val();
                $('#' + cityId).html(ary[0]);
                $('#' + cityId).val(c);
            }
        }
        if (isCountry) {
            if (o(countryId) == null) mui.toast('缺少区县输入框');
            else {
                var c = $('#' + countryId).val();
                console.log('doGetCityCountry country value', c);
                $('#' + countryId).html(ary[1].trim());
                $('#' + countryId).val(c);
            }
        }
    }

    function doGetCityCountry<%=rid%>(data, isForCountry) {
        if (data.trim() == '') return;
        var ary = data.trim().split('|');
        if (isCity) {
            if (o(cityId) == null) mui.toast('缺少城市输入框');
            else if (ary[0] != '') {
                var c = $('#' + cityId).val();
                $('#' + cityId).html(ary[0]);
                $('#' + cityId).val(c);
            }
        }
        if (isForCountry && isCountry) {
            if (o(countryId) == null) mui.toast('缺少区县输入框');
            else {
                var c = $('#' + countryId).val();
                console.log('doGetCityCountry country value', c);
                $('#' + countryId).html(ary[1].trim());
                $('#' + countryId).val(c);
            }
        }
    }

    function ajaxShowCityCountry(province, city) {
        <%--var str = "rid=<%=rid%>&cityId=" + cityId + "&countryId=" + countryId + "&isCity=" + isCity + "&isCountry=" + isCountry + "&province=" + province + "&city=" + city;
        var myAjax = new cwAjax.Request('<%=request.getContextPath()%>/visual/get_city_country.jsp',
            {
                method: 'post',
                parameters: str,
                onComplete: doGetCityCountry<%=rid%>,
                onError: errFuncCityCountry<%=rid%>
            }
        );--%>

        $.ajax({
            type: "post",
            url: '<%=request.getContextPath()%>/visual/get_city_country.jsp',
            data: {
                rid: "<%=rid%>",
                cityId: cityId,
                countryId: countryId,
                isCity: isCity,
                isCountry: isCountry,
                province: province,
                city: city
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
            },
            success: function(data, status){
                var isForCountry = province == '';
                doGetCityCountry<%=rid%>(data, isForCountry);
            },
            complete: function(XMLHttpRequest, status){
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    jQuery(function() {
        ajaxShowCityCountry(o('<%=fieldName%>').value, '');
        if (isCountry) {
            ajaxShowCityCountry('', o('<%=cityId%>').value);
        }
    });
