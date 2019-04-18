package com.redmoon.oa.exam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import bsh.This;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.fileark.Attachment;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

/**
 * @Description: 考试题目中的文件上传
 * @author: sht
 * @Date: 2017-4-8下午05:43:35
 */
public class UploadFile extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UploadFile() {
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

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject json = new JSONObject();
		response.setContentType("text/html");		
		PrintWriter out = response.getWriter();
		FileUpload fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"jpg","jpeg","png","bmp","gif","mp4"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        int ret = 0;
        try {
        	ServletContext application=this.getServletContext(); 	        	
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                // throw new ErrMsgException(fileUpload.getErrMessage());
    			try {
    				json.put("state", "上传失败");
    			    json.put("url", "");         //能访问到你现在图片的路径
    			    json.put("title", "");
    			    json.put("original", ""); 					
    			} catch (JSONException e1) {
    				e1.printStackTrace();
    			}  			
            }
            else {
            	Vector v = fileUpload.getFiles();
                FileInfo fi = null;
                if (v.size() > 0)
                    fi = (FileInfo) v.get(0);
                String vpath = "";
                if (fi != null) {
                    // 置保存路径
                    Calendar cal = Calendar.getInstance();
                    String year = "" + (cal.get(Calendar.YEAR));
                    String month = "" + (cal.get(Calendar.MONTH) + 1);
                    vpath = "upfile/form/"; // 用于表单上传
            		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            		String attPath = cfg.get("exam_file_Path");
            		String virtualpath = year + "/" + month;
                    String filepath = Global.getRealPath() + attPath + "/" + virtualpath + "/";
                    vpath = attPath + "/" + virtualpath + "/";
                    fileUpload.setSavePath(filepath);
                    // 使用随机名称写入磁盘
                    fileUpload.writeFile(true);
                    int orders = 0;
                	// 记录于数据库
                    com.redmoon.oa.exam.Attachment att1 = new com.redmoon.oa.exam.Attachment();
                    att1.setDiskName(fi.getDiskName());
                    att1.setFullPath(filepath + fi.getDiskName());
                    att1.setDocId(-1);
                    att1.setName(fi.getName());
                    att1.setDiskName(fi.getDiskName());
                    att1.setOrders(orders);
                    att1.setVisualPath(attPath + "/" + virtualpath);
                    att1.setUploadDate(new java.util.Date());
                    att1.setSize(fi.getSize());
                    att1.setExt(StrUtil.getFileExt(fi.getName()));
                    att1.setEmbedded(true);
                    att1.create();
                }            	
            	
	    	    /*你的处理图片的代码*/
	    	    try {
	    			json.put("state", "SUCCESS"); // UEDITOR的规则:不为SUCCESS则显示state的内容
	    		    json.put("url", request.getContextPath() + "/" + vpath + fi.getDiskName()); // "http://localhost:8080/oa/images/man.png");         //能访问到你现在图片的路径
	    		    json.put("title", "");
	    		    json.put("original", fi.getDiskName()); 
	    		} catch (JSONException e) {
	    			e.printStackTrace();
	    			try {
	    				json.put("state", "上传失败");
	    			    json.put("url", "");         //能访问到你现在图片的路径
	    			    json.put("title", "");
	    			    json.put("original", ""); 					
	    			} catch (JSONException e1) {
	    			}
	    		
	    		} 
            }
            
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
			try {
				json.put("state", "上传失败");
			    json.put("url", "");         //能访问到你现在图片的路径
			    json.put("title", "");
			    json.put("original", ""); 					
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
        }		

		out.print(json);
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
