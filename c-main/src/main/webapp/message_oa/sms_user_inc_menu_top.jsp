<%@ page contentType="text/html;charset=utf-8" %>
<div id="tabs1">
    <ul>
        <li id="menu1"><a href="sms_send_message.jsp"><span>发送短信</span></a></li>
        <%
            com.redmoon.oa.pvg.Privilege pvg3 = new com.redmoon.oa.pvg.Privilege();
            if (pvg3.isUserPrivValid(request, "message.group")) {
        %>
        <li id="menu2"><a href="sms_send_message_to_group.jsp"><span>短信群发</span></a></li>
        <%
            }
            if (pvg3.isUserPrivValid(request, "sms")) {
        %>
        <li id="menu3"><a href="sms_send.jsp"><span>按号码发送</span></a></li>
        <%}%>
        <li id="menu4"><a href="sms_user_send_list.jsp"><span>发送记录</span></a></li>
        <%
            if (pvg3.isUserPrivValid(request, "sms")) {
                com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
                org.jdom.Element root = cfg.getRootElement();
                java.util.Iterator ir = root.getChildren("sms").iterator();
                String code = "";
                while (ir.hasNext()) {
                    org.jdom.Element e = (org.jdom.Element) ir.next();
                    String isUsed = e.getAttributeValue("isUsed");
                    if (isUsed.equals("true")) {
                        code = e.getAttributeValue("code");
                    }
                }
                boolean isShowRemain = true;
                if (!code.equals("qxtmobile") && !code.equals("mmmobile")) {
                    isShowRemain = false;
                }
                if (isShowRemain) {
        %>
        <li id="menu5"><a href="sms_count_search.jsp"><span>剩余条数</span></a></li>
        <%
                }
            }
        %>
        <!--
        <li id="menu5"><a href="sms_user_receive_list.jsp"><span>短信收取列表</span></a></li>
        -->
    </ul>
</div>

