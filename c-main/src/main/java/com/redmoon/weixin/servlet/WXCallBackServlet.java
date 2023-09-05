package com.redmoon.weixin.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.aes.AesException;
import com.redmoon.weixin.aes.WXBizMsgCrypt;
import com.redmoon.weixin.mgr.WXAddressCallBack;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-21上午11:50:22
 */
public class WXCallBackServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public WXCallBackServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
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

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			response.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String _postData = readStream(request.getInputStream());
			String msg_signature = request.getParameter("msg_signature");
			String timestamp = request.getParameter("timestamp");
			String nonce = request.getParameter("nonce");
			Config config = Config.getInstance();
			String sToken = config.getProperty("token");
			String sCorpID = config.getProperty("corpId");
			String sEncodingAESKey = config.getProperty("encodingAESKey");
			WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken,sEncodingAESKey,sCorpID);
			String result = wxcpt.DecryptMsg(msg_signature,timestamp,nonce,_postData);
			//获取对象
            WXAddressCallBack wxAddressCallBack = new WXAddressCallBack();
            wxAddressCallBack.contentXml = result;
            //解析对象
            wxAddressCallBack.dispose();
		}  catch (AesException e) {
			LogUtil.getLog(getClass()).error(e);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

	/**
	 * 读取 InputStream 到 String字符串中
	 */
	public static String readStream(InputStream in) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024]; // 1KB
			int len = -1;
			while ((len = in.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
				baos.write(buffer, 0, len);   //把读取到的内容写到输出流中
			}
			String content = baos.toString();
			in.close();
			baos.close();
			return content;
		} catch (Exception e) {
			return  e.getMessage();
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
