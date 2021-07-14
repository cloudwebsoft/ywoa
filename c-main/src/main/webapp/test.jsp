<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.mq.MsgProducer" %>
<%@ page import="com.cloudweb.oa.mq.MsgInfo" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb" %>
<%@ page import="com.redmoon.oa.flow.WorkflowUtil" %>
<%
    Date d = new Date();
    d = DateUtil.addDate(d, -90);

    // out.print(d.getTime());
    // TwoDimensionCode twcode = new TwoDimensionCode();
    // twcode.encoderQRCode("http://www.yimihome.com/download_app2.1.html", "d:/2.1.png");

    /*ModuleLogPreducer moduleLogPreducer = SpringUtil.getBean(ModuleLogPreducer.class);
    moduleLogPreducer.sendMessage1("呵呵123");
    moduleLogPreducer.sendMessage2("好的");*/

    MsgProducer msgProducer = SpringUtil.getBean(MsgProducer.class);
    // messageProducer.sendMessage("yes，我来了");

    MsgInfo msgInfo = new MsgInfo();
    msgInfo.setType(ConstUtil.MQ_MSG_TYPE_MSG);
    msgInfo.setTitle("我是谁");
    msgInfo.setContent("fgf");
    msgProducer.sendMsgInfo(msgInfo);

    /*WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow("15957648502438126504");
    String flowString = wpd.getFlowString();
        out.print(new MyflowUtil().toMyflow(flowString));*/

    /*if (true) {
        return;
    }*/

    String url = "http://115.153.102.133:8088/jzzs/lte/index.jsp?mainTitle=" + StrUtil.UrlEncode("项目") + "&mainPage=" + StrUtil.UrlEncode("visual/module_list.jsp?sxqej=袁州区&sxqej_cond=1&gshxcy_cond=1&ysfxcy_cond=1&sfwzdxm_cond=1&op=search&code=jx_xmgl");
    out.print(url);
%>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="js/crypto-js.min.js"></script>
<script>
    // window.open("<%=url%>");
</script>
<div id="drag_612" class="portlet drag_div bor" style="border:0px;padding:0px;">
    <div id="drag_612_h" style="height:3px;padding:0px;margin:0px; font-size:1px"></div>
    <div id="cont_612" class="portlet_content" style="min-height:141px;_height:141px;padding:0px;margin:0px">
        <div>
            <section class="slider">
                <div id="flexslider612" class="flexslider">
                    <ul class="slides">
                        <li>
                            <div class="">
                                <img src="upfile/file_folder/2017/4/14918292637391084758088.png"/>
                            </div>
                        </li>
                        <li>
                            <div class="">
                                <img src="upfile/file_folder/2017/4/14918290560012112413376.jpg"/>
                            </div>
                        </li>
                    </ul>
                </div>
            </section>

            <input id="btnOk" type="button" value="确定"/>
        </div>
    </div>
</div>
<script>
    function show(msg) {
        console.log(msg);
    }

    var str = "my\\commatest";
    console.log(str);
    str = str.replaceAll("\\\\comma", ",");
    console.log(str);

    $(function () {
        $('#btnOk').click(function () {

        })
    });

    $(function () {
        $.ajax({
            type: "post",
            url: "activex/ac.dat?time=" + new Date().getTime(),
            data: {
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            complete: function (XMLHttpRequest, status) {
            },
            success: function (data, status) {
                console.log("data=" + data);
                var d1 = decryptByDES(data, "88896956");
                console.log("d1=" + d1);
                var ary = d1.split("|");
                if (ary.length == 2) {
                    console.log("ary[1]=" + ary[1]);
                    // console.log("56163BC25E2CC2B1".substr(0, 8));
                    var d2 = decryptByDES(ary[1], "56163BC25E2CC2B1".substr(0, 8));
                    console.log("d2=" + d2);
                }
                else {
                    console.log("激活码非法");
                }
            },
            error: function (XMLHttpRequest, textStatus) {
                console.log(XMLHttpRequest.responseText);
            }
        });
    });

    //DES加密
    function encryptByDES(message, key){
        var keyHex = CryptoJS.enc.Utf8.parse(key);
        var encrypted = CryptoJS.DES.encrypt(message, keyHex, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        return encrypted.ciphertext.toString();
    }

    //DES解密
    function decryptByDES(ciphertext, key){
        var keyHex = CryptoJS.enc.Utf8.parse(key);
        var decrypted = CryptoJS.DES.decrypt({
            ciphertext: CryptoJS.enc.Hex.parse(ciphertext)
        }, keyHex, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        var result_value = decrypted.toString(CryptoJS.enc.Utf8);
        return result_value;
    }

    /*
    function encryptBy3DES(message, key) {
        var keyHex = CryptoJS.enc.Utf8.parse(key);
        var encrypted = CryptoJS.TripleDES.encrypt(message, keyHex, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        return encrypted.toString();
    }

    function decryptBy3DES(ciphertext, key) {
        var keyHex = CryptoJS.enc.Utf8.parse(key);
        var decrypted = CryptoJS.TripleDES.decrypt(ciphertext, keyHex, {
            mode: CryptoJS.mode.ECB,
            padding: CryptoJS.pad.Pkcs7
        });
        return decrypted.toString(CryptoJS.enc.Utf8);
    }*/

    var key = "82986728";
    var plainText = "测试一下中文及englisth text";
    var cipherText = encryptByDES(plainText, key);
    var decryptedText = decryptByDES(cipherText, key);
    console.log("加密：" + cipherText);
    console.log("解密：" + decryptedText);

</script>