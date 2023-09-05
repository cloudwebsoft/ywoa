package com.redmoon.oa.xmpp;

import java.io.StringReader;

import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MessageUtil {
    public static final String MSG = "MSG";
    public static final String OP_TYPE_SEND = "01";

    /**
     * 用于发送系统消息
     */
    public static final String USER_SYSTEM = "system";
    /**
     * 用于发送回复短信
     */
    public static final String USER_SMS = "sms";
    
    public static final String MSG_TYPE_GROUPCHAT = "groupchat";

    public MessageUtil() {
    }

    public String getXML(String from, String to, String msg, String type) {
        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
        String key = config.get("key");
        java.util.Date d = new java.util.Date();
        String timeStamp = DateUtil.format(d, "yyyyMMddHHmmss");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("<TXL>");
        sb.append("<header>");
        sb.append("<oper_code>" + MSG + "</oper_code>");
        sb.append("<version>1.0.0</version>");
        sb.append("<key>" + key + "</key>");
        sb.append("<TimeStamp>" + timeStamp + "</TimeStamp>");
        sb.append("</header>");
        sb.append("<body>");
        sb.append("<oper_type>" + OP_TYPE_SEND + "</oper_type>");
        sb.append("<fromUser>" + from + "</fromUser>");
        sb.append("<toUser>" + to + "</toUser>");
        sb.append("<msg>" + msg + "</msg>");
        sb.append("<msg_type>" + type + "</msg_type>");
        sb.append("</body>");
        sb.append("</TXL>");
        return sb.toString();
    }
    
    public boolean send(String fromUser, String toUser, String msg) {
    	return send(fromUser, toUser, msg, "");
    }
    
    /**
     * 群聊
     * @param fromUser
     * @param toUser
     * @param msg
     * @return
     */
    public boolean sendGroupChat(String fromUser, String toUser, String msg) {
    	msg += "\\par";
    	return send(fromUser, toUser, msg, MSG_TYPE_GROUPCHAT);
    }    

    public boolean send(String fromUser, String toUser, String msg, String type) {
        boolean re = false;
        try {
        	msg += "\\par";
            String xmlStr = getXML(fromUser, toUser, msg, type);
            re = executeHttpRequest(xmlStr);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return re;
        }
        return re;
    }

    public boolean executeHttpRequest(String xml){
        Config config = new Config();
        String descUrl = config.get("msgTargetUrl");
        HttpClient httpclient = new HttpClient();
        PostMethod post = new PostMethod(descUrl);
        try {
            post.setRequestHeader("Content-type", "text/xml; charset=utf-8");
            RequestEntity entity = new StringRequestEntity(xml, "text/xml", "utf-8");
            post.setRequestEntity(entity);
            httpclient.executeMethod(post);
            // 打印服务器返回的状态
            String status = "";
            String result = post.getResponseBodyAsString();
            if (post.getStatusCode() == HttpStatus.SC_OK) {
                result = new String(post.getResponseBody());
                StringReader read = new StringReader(result);
                // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
                InputSource source = new InputSource(read);
                // 创建一个新的SAXBuilder
                SAXBuilder sBuilder = new SAXBuilder();
                // 通过输入源构造一个Document
                Document doc = sBuilder.build(source);
                // 取的根元素
                Element root = doc.getRootElement();
                // out.print(root.getName());
                // Element header = root.getChild("Head");
                Element body = root.getChild("Body");
                if (body != null) {
                    status = body.getChildText("Status");
                    String OperType = body.getChildText("OperType");
                    String OperCode = root.getChildText("OperCode");
                    if (status.equals("0")) {
                        return true;
                    }
                    else {
                        LogUtil.getLog(getClass()).warn("SyncUtil executeHttpRequest: OperCode=" + OperCode + " OperType=" + OperType + " status=" + status );
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            post.releaseConnection();
        }

        return false;
    }
}
