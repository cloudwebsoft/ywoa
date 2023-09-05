package com.redmoon.weixin.servlet;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.aes.AesException;
import com.redmoon.weixin.aes.WXBizMsgCrypt;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WXAddressCallBackServlet extends HttpServlet {

    public WXAddressCallBackServlet() {
    }
    public void destroy() {
        super.destroy(); // Just puts "destroy" string in log
        // Put your code here
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Config config = Config.getInstance();
        String sToken = config.getProperty("token");
        String sCorpID = config.getProperty("corpId");
        String sEncodingAESKey = config.getProperty("encodingAESKey");
        String msg_signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        PrintWriter out = response.getWriter();
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        String result = null;
        try {
            WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken,sEncodingAESKey,sCorpID);
            result = wxcpt.VerifyURL(msg_signature, timestamp, nonce, echostr);
        } catch (AesException e) {
            LogUtil.getLog(getClass()).error(e);
        }finally{
            if (result == null) {
                result = sToken;
            }
            out.print(result);
            out.close();
            out = null;
        }
    }


}

