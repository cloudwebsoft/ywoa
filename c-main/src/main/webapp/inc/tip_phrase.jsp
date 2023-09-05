<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="org.json.*" %>
<%@page import="com.redmoon.oa.ui.LocalUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%
    String opTip = ParamUtil.get(request, "op");
    if (opTip.equals("get")) {
        UserPhraseDb updTip = new UserPhraseDb();
        Iterator irTip = updTip.list(updTip.getTable().getSql("listMine"), new Object[]{new Privilege().getUser(request)}).iterator();
        while (irTip.hasNext()) {
            updTip = (UserPhraseDb) irTip.next();
%>
<span style="cursor:pointer" onclick="insertText(this.innerHTML); addFrequency('<%=updTip.getLong("id")%>')"><%=updTip.getString("phrase")%></span>
<span onclick="removePhrase('<%=updTip.getLong("id")%>')">×</span>&nbsp;&nbsp;&nbsp;
<%
        }
        return;
    } else if (opTip.equals("add")) {
        JSONObject json = new JSONObject();
        // request.setCharacterEncoding("UTF-8");
        String phrase = ParamUtil.get(request, "phrase");
        phrase = java.net.URLDecoder.decode(phrase, "UTF-8");
        UserPhraseDb upd = new UserPhraseDb();
        boolean re = false;
        re = upd.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{new Privilege().getUser(request), phrase, new Integer(1), new java.util.Date()});
        if (re) {
            json.put("ret", "1");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str1);
        } else {
            json.put("ret", "0");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str1);
        }
        out.print(json);
        return;
    } else if (opTip.equals("del")) {
        JSONObject json = new JSONObject();
        long idPhrase = ParamUtil.getLong(request, "id", -1);
        UserPhraseDb upd = new UserPhraseDb();
        upd = (UserPhraseDb) upd.getQObjectDb(new Long(idPhrase));
        boolean re = false;
        re = upd.del();
        if (re) {
            json.put("ret", "1");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str1);
        } else {
            json.put("ret", "0");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str1);
        }
        out.print(json);
        return;
    } else if (opTip.equals("addFrequency")) {
        JSONObject json = new JSONObject();
        long idPhrase = ParamUtil.getLong(request, "id", -1);
        UserPhraseDb upd = new UserPhraseDb();
        upd = (UserPhraseDb) upd.getQObjectDb(new Long(idPhrase));
        boolean re = false;
        upd.set("frequency", new Integer(upd.getInt("frequency") + 1));
        re = upd.save();
        if (re) {
            json.put("ret", "1");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_success");
            json.put("msg", str1);
        } else {
            json.put("ret", "0");
            String str1 = LocalUtil.LoadString(request, "res.common", "info_op_fail");
            json.put("msg", str1);
        }
        out.print(json);
        return;
    }
%>
<style>
    #phraseBox {
        margin: 0 auto 0;
        position: absolute;
        display: none
    }

    #phraseBox cite {
        position: relative;
        margin: 0;
        padding: 0 30px 1px;
        background: transparent url(images/tip.gif) no-repeat 50px 10px;
        font-style: normal;
    }

    #phraseBox .phraseCnt div {
        float: left;
    }

    #phraseBox .phraseCnt .phraseIcon img {
        margin: 2px 3px 2px 3px;
    }

    #phraseBox .phraseCnt {
        width: 300px;
        height: 150px;
        clear: both;
        overflow-y: auto;
        overflow-x: hidden;
        -moz-border-radius: 5px;
        -webkit-border-radius: 5px;
        border-radius: 5px;
        position: relative;
        padding: 8px;
        behavior: url(css/ie-css3.htc);
        background-color: #fff9ec;
        border: 1px solid #e5dcc6;
        word-wrap: break-word;
        line-height: 25px;
        word-break: break-all;
    }

    #phraseBox .phraseCnt .phraseAdd {
        cursor: pointer;
        float: right;
        margin-right: 10px;
        border: 1px solid #c3c3c3;
        width: 16px;
        line-height: 16px;
        text-align: center;
        background-color: #FFF;
        font-size: 16px;
    }

    #phraseBox .phraseCnt .phraseClose {
        cursor: pointer;
        float: right;
        border: 1px solid #c3c3c3;
        width: 16px;
        line-height: 16px;
        text-align: center;
        background-color: #FFF;
        font-size: 16px;
    }
</style>
<script>
    function getPhrases() {
        $.ajax({
            type: "post",
            url: "<%=request.getContextPath()%>/inc/tip_phrase.jsp",
            data: {
                op: "get"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // ShowLoading();
            },
            success: function (data, status) {
                // $.toaster({priority : 'info', message : '操作成功！' });
                $('.phraseIcon').html(data);
            },
            complete: function (XMLHttpRequest, status) {
                // HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function removePhrase(id) {
        jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "<%=request.getContextPath()%>/inc/tip_phrase.jsp",
                    data: {
                        op: "del",
                        id: id
                    },
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        //ShowLoading();
                    },
                    success: function (data, status) {
                        getPhrases();
                        $.toaster({priority: 'info', message: '<lt:Label res="res.common" key="info_op_success"/>'});
                    },
                    complete: function (XMLHttpRequest, status) {
                        // HideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    function submitPhrase() {
        if ($("#phrase").val().trim() == "") {
            jAlert('<lt:Label res="res.flow.Flow" key="fillStatement"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
            return;
        }
        $.ajax({
            type: "post",
            url: "<%=request.getContextPath()%>/inc/tip_phrase.jsp",
            data: {
                op: "add",
                // contentType: 'application/json;charset=UTF-8',
                phrase: encodeURI($("#phrase").val(), "UTF-8")
            },
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                getPhrases();
                $.toaster({priority: 'info', message: '<lt:Label res="res.common" key="info_op_success"/>'});
            },
            complete: function (XMLHttpRequest, status) {
                // HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });

        $("#dlgAddPhrase").dialog("close");
    }

    function showAddPhraseDlg() {
        $("#dlgAddPhrase").dialog({
            title: '<lt:Label res="res.flow.Flow" key="add"/>',
            modal: true,
            // bgiframe:true,
            buttons: {
                '<lt:Label res="res.flow.Flow" key="cancel"/>': function () {
                    $(this).dialog("close");
                },
                '<lt:Label res="res.flow.Flow" key="sure"/>': function () {
                    submitPhrase();
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable: true,
            width: 300
        });
    }

    function phrase_presskey() {
        if (window.event.keyCode == 13) {
            submitPhrase();
        }
    }

    $(function () {
        $('.phraseAdd').click(function () {
            showAddPhraseDlg();
        });
    });

    function addFrequency(id) {
        $.ajax({
            type: "post",
            url: "<%=request.getContextPath()%>/inc/tip_phrase.jsp",
            data: {
                op: "addFrequency",
                id: id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
            },
            complete: function (XMLHttpRequest, status) {
                // HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function insertText(txt) {
        if (typeof (insertCkeditorText) == "function") {
            insertCkeditorText(txt);
        } else {
            insertPhrase(txt);
        }
    }
</script>
<div id="dlgAddPhrase" style="display:none">
    <lt:Label res="res.flow.Flow" key="commonStatement"/>
    <input id="phrase" name="phrase" onKeyPress="return phrase_presskey()" maxlength="45"/>
</div>
<div id="phraseBox">
    <div class="phraseCnt">
        <div class="phraseClose" onclick="closeTipPhrase()">×</div>
        <div class="phraseAdd">+</div>
        <div class="phraseIcon" style="clear:both">
            <%
                FlowConfig flowConfig = new FlowConfig();
                List<String> listPhrase = flowConfig.getPhrases();
                for (String phrase : listPhrase) {
            %>
            <span style="cursor:pointer" onclick="insertText(this.innerHTML);"><%=phrase%></span>&nbsp;&nbsp;&nbsp;
            <%
                }

                UserPhraseDb updTip = new UserPhraseDb();
                Iterator irTip = updTip.list(updTip.getTable().getSql("listMine"), new Object[]{new Privilege().getUser(request)}).iterator();
                while (irTip.hasNext()) {
                    updTip = (UserPhraseDb) irTip.next();
            %>
            <span style="cursor:pointer" onclick="insertText(this.innerHTML);addFrequency('<%=updTip.getLong("id")%>')"><%=updTip.getString("phrase")%></span>
            <span onclick="removePhrase('<%=updTip.getLong("id")%>');">×</span>&nbsp;&nbsp;&nbsp;
            <%
                }
            %>
        </div>
    </div>
</div>
<script src="<%=request.getContextPath()%>/inc/tip_phrase.js"></script>