package com.redmoon.oa.licenceValidate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import com.cloudwebsoft.framework.util.LogUtil;


/**
 * @Description: 重启本地tomcat服务
 * @author: lichao	
 * @Date: 2015-8-28下午02:26:34
 */
public class RestartTomcat {

	public static void restartTomcatServer() {
		Socket socket = null;
		OutputStream os = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		
		try {
			socket = new Socket("127.0.0.1", 36667);
			
			//向服务器端发送数据  
			os = socket.getOutputStream();
			out = new DataOutputStream(os);
			
			byte send = 0x01;
			out.write(send);
			out.flush();
			out.close();
			
			//接收服务器端数据  
			//in = new DataInputStream(socket.getInputStream());
			//System.out.println(in.readUTF());  
			//in.close();
		} catch (UnknownHostException e) {
			LogUtil.getLog("restartTomcatServer").error(e);
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("restartTomcatServer").error(e);
			e.printStackTrace();
		} finally {
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	 public static void main(String[] args) {
		 restartTomcatServer();
	 }
	
}
