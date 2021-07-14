package com.redmoon.oa.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redmoon.oa.robot.RobotUtil;

import cn.js.fan.util.StrUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-11-3下午06:49:12
 */
public class Test {
	//字节数组转换为十六进制字符串
	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash)
		{
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

    public static void main(String[] args) {
/*
		String nonceStr = createNonceStr();
		String timestamp = createTimestamp();
		String string1;
		String signature = "";
*/

		//注意这里参数名必须全部小写，且必须有序
		String string1 = "jsapi_ticket=sM4AOVdWfPE4DxkXGEs8VBkF9kCiCNC8hncQLQVEzbvkguocLzARSPnxriKumNeXSFCENhboMaCSWTJS--X06g&noncestr=a6f52fbc-4831-4b63-bd91-21e8f41f06be&timestamp=1543755385&url=http://partner.yimihome.com/public/robot/redbag_share.jsp?id=1265";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(string1.getBytes("UTF-8"));
			String signature = byteToHex(crypt.digest());
			System.out.println(signature);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (true) return;

    	Pattern p = Pattern.compile(
                "@([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.\\(\\)]+) ([^\\s]+)", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("@老范-OA-办公(2530476560) 123");
        while (m.find()) {
            String realName = m.group(1);	
            String cmdMsg = StrUtil.getNullStr(m.group(2));
            cmdMsg = cmdMsg.trim(); // @用户 后面的消息
            
			System.out.println(RobotUtil.class.getName() + " @user=" + realName + " cmdMsg=" + cmdMsg);		
        }
    	
        if (true)
        	return;
        
    	String strSessionKey = "";
       	
       	String strUser = "admin";
       	String strURL = "http://141.60.16.101:8012/GetSession.cgi?receiver=" + strUser;
       	// String strURL = "http://" + serverAddr + ":8012/GetSession.cgi?receiver=" + strUser;
       	System.out.println(strURL);
        try {
        	java.net.URL url = new URL(strURL);
           	System.out.println(" strURL=" + strURL);

        	HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
           	System.out.println(" strURL2=" + strURL);

        	BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
           	System.out.println(" strURL3=" + strURL);
        	
        	strSessionKey=reader.readLine();  
           	System.out.println(" strSessionKey=" + strSessionKey);
        	
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	System.out.println("error"+e);
        }
    }

    public void test() {
	}
}
