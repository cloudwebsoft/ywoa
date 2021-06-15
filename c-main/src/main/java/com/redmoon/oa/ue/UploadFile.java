package com.redmoon.oa.ue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redmoon.oa.flow.DocumentMgr;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.fileark.Attachment;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

/**
 * @Description: 
 * @author: 
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
		out
				.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
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

		String op = ParamUtil.get(request, "op");
		JSONObject json = new JSONObject();
		response.setContentType("text/html");		
		PrintWriter out = response.getWriter();

		// 用于富文本编辑器宏控件
		if ("UEditorCtl".equals(op)) {
			DocumentMgr dm = new DocumentMgr();
			ServletContext application=this.getServletContext();
			String [] r = null;
			try {
				r = dm.uploadMedia(application, request);

				try {
					json.put("state", "SUCCESS"); // UEDITOR的规则:不为SUCCESS则显示state的内容
					json.put("url", r[1]); // "http://localhost:8080/oa/images/man.png");         //能访问到你现在图片的路径
					json.put("title", "");
					json.put("original", r[3]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					try {
						json.put("state", "上传失败");
						json.put("url", "");         //能访问到你现在图片的路径
						json.put("title", "");
						json.put("original", "");
					} catch (JSONException e1) {
					}
				}
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}

			out.print(json);
			return;
		}
		
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
                if (v.size() > 0) {
					fi = (FileInfo) v.get(0);
				}
                String vpath = "";
                if (fi != null) {
                    // 置保存路径
                    Calendar cal = Calendar.getInstance();
                    String year = "" + (cal.get(Calendar.YEAR));
                    String month = "" + (cal.get(Calendar.MONTH) + 1);
                    // vpath = "upfile/" +
                    //        fi.getExt() + "/" + year + "/" + month + "/";
                    vpath = "upfile/form/"; // 用于表单上传
                    if ("".equals(op) || "formDesigner".equals(op)) {
                        String filepath = Global.getRealPath() + vpath;
                        fileUpload.setSavePath(filepath);
                        // 使用随机名称写入磁盘
                        fileUpload.writeFile(true);
                    }
                    else if ("robot".equals(op)) {
                    	vpath = "upfile/robot/"; // 社群配置
                        String filepath = Global.getRealPath() + vpath;
                        fileUpload.setSavePath(filepath);
                        // 使用随机名称写入磁盘
                        fileUpload.writeFile(true);
                    }                    
                    else if ("notice".equals(op)) {
                    	vpath = "upfile/notice/" + year + "/" + month + "/";
                        String filepath = Global.getRealPath() + vpath;
                        fileUpload.setSavePath(filepath);
                        // 使用随机名称写入磁盘
                        fileUpload.writeFile(true);
                    }
                    else if ("fileark".equals(op)) {
                		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                		String attPath = cfg.get("file_folder");
                		
                		String virtualpath = year + "/" + month;
                        String filepath = Global.getRealPath() + attPath + "/" + virtualpath + "/";
                        
                        vpath = attPath + "/" + virtualpath + "/";
                        
                        fileUpload.setSavePath(filepath);
                        // 使用随机名称写入磁盘
                        fileUpload.writeFile(true);
                        
                        int orders = 0;

                    	// 记录于数据库
                        com.redmoon.oa.fileark.Attachment att = new Attachment();
                        att.setDiskName(fi.getDiskName());
                        // logger.info(fpath);
						// 在Document.java中的create、update方法中，解析内容中的图片，根据文件名找到attachment，将其docId置为文章的id
                        att.setDocId(Attachment.TEMP_DOC_ID);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setOrders(orders);
                        att.setVisualPath(attPath + "/" + virtualpath);
                        att.setUploadDate(new java.util.Date());
                        att.setSize(fi.getSize());
                        att.setExt(StrUtil.getFileExt(fi.getName()));
                        att.setEmbedded(true);
                        att.create();

/*                      String module = ParamUtil.get(request, "module");
                        if (module.equals("notice")) {
                        	att.setPageNum(1);
                        }   */                	
                    }
                    
                    //File f = new File(vpath + fi.getDiskName());
                    //f.delete();
                    //System.out.println("FleUpMgr " + fi.getName() + " " + fi.getFieldName() + " " + fi.getDiskName());
                }            	
            	
	    	    /*你的处理图片的代码*/
	    	    try {
	    			json.put("state", "SUCCESS"); // UEDITOR的规则:不为SUCCESS则显示state的内容
	    		    if ("formDesigner".equals(op) || "robot".equals(op) || "notice".equals(op)) {
		    		    json.put("url", request.getContextPath() + "/" + vpath + fi.getDiskName()); // "http://localhost:8080/oa/images/man.png");         //能访问到你现在图片的路径	    		    	
	    		    }
	    		    else {
	    		    	json.put("url", vpath + fi.getDiskName()); // "http://localhost:8080/oa/images/man.png");         //能访问到你现在图片的路径
	    		    }
	    			json.put("title", "");
	    		    json.put("original", fi.getDiskName()); 
	    		} catch (JSONException e) {
	    			// TODO Auto-generated catch block
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
