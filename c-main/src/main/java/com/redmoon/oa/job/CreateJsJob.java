package com.redmoon.oa.job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.kernel.License;
/**
 * 创建JS job 避免用户修改登录页面右上角的图标
 * @author Administrator
 *
 */
public class CreateJsJob implements Job {
	Logger logger = Logger.getLogger( CreateJsJob.class.getName() );
	/**
	 * 执行创建logo_show.js文件并写入内容，然后修改index.jsp文件，判断是否引用该js，若没有引用，则动态添加引用
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		//判断是否为商业用户，若是，允许修改
		// if (!License.getInstance().isBiz())
		// fgf 20161101，如果为平台版，则可以去掉
		if (!License.getInstance().isPlatformSrc())
		{
			createLogo_ShowJs();
			createFlow_logoJs();
			checkIndexJsp();
			checkFlow_designerJsp();
		}
	}
	
	/**
	 * 创建flow_logo.js
	 * @return
	 */
	private boolean createFlow_logoJs() {
		boolean flag = false;
		String path = this.getClass().getResource("/").getPath();
		String resourcePath = path.substring(1, path.lastIndexOf("WEB-INF"))+"js/flow_logo.js";
		try { 
			resourcePath = URLDecoder.decode(resourcePath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		File file = new File(resourcePath);
		if (!file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("create flow_logo.js error:" + e.getMessage());
			}
		}
		/*-----获取images/flow_logo.png图片的md5，来判读图片是否更改-----*/
    	String resourceImgPath = null;

		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"images/flow_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String flow_logo_MD5 = getImgMD5(resourceImgPath);
		//如果md5不一样要先下载flow_logo.png
		if(!"75c18ea426efc60a36f314eef1697d43".equals(flow_logo_MD5)){
			//下载新的图片，添加新图片
			downLoadImg(resourceImgPath);
		}
		
		//写入flow_logo.js
		FileWriter fw = null;
		Writer out = null;
		try {
	          FileOutputStream fos = new FileOutputStream(resourcePath);
	          out = new OutputStreamWriter(fos, "UTF-8");
	          out.write("var myDiv = document.getElementById(\"toolbar\");\r");
	          out.write("var newDiv = document.createElement(\"div\");\r");
	          out.write("newDiv.style.float = \"right\";\r");
	          out.write("newDiv.style.opacity = \"0.3\";\r");
	          out.write("newDiv.style.filter = \"alpha(opacity=30)\";\r");
	          out.write("newDiv.style.paddingTop = \"5px\";\r");
	          out.write("newDiv.style.paddingLeft = \"5px\";\r");
	          out.write("newDiv.style.paddingRight = \"15px\";\r");
	          out.write("newDiv.style.height = \"18px\";\r");
	          out.write("newDiv.style.width = \"64px\";\r");
	          out.write("newDiv.style.right = \"0px\";\r");
	          out.write("newDiv.style.position = \"absolute\";\r");
	          out.write("newDiv.innerHTML = \"<img src=\\\"../images/flow_logo.png\\\" width=50 height=14 />\";\r");
	          out.write("myDiv.appendChild(newDiv);");
	          out.flush();
	          
		 } catch (IOException e) {
			 logger.error("Error writing logo_show.js content:" + e.getMessage());
		 } finally {
			 if (fw!=null) {
				 try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
			 if (out!=null) {
		          try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		 }
		return flag;
	}
	
	private void downLoadImg(String resourceImgPath) {
		String imageUrl = "http://upgrade.yimihome.com/public/upgrade_archive/flow_logo.png";
		DataInputStream dis = null;
		FileOutputStream fos = null;
	    URL url;
		try {
			url = new URL(imageUrl);
			//打开网络输入流
		    dis = new DataInputStream(url.openStream());
		    //建立一个新的文件
		    fos = new FileOutputStream(new File(resourceImgPath));
		    byte[] buffer = new byte[1024];
		    int length;
		    //开始填充数据
		    while( (length = dis.read(buffer))>0){
		    	fos.write(buffer,0,length);
		    }
		    fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			 if (dis!=null) {
				 try {
					 dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
			 if (fos!=null) {
		          try {
		        	  fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		 }
	}

	/**
	 * 创建logo_show.js
	 * @return
	 */
	private boolean createLogo_ShowJs()
	{
		boolean flag = false;
		String path = this.getClass().getResource("/").getPath();
		String resourcePath = path.substring(1, path.lastIndexOf("WEB-INF"))+"js/logo_show.js";
		try { 
			resourcePath = URLDecoder.decode(resourcePath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		File file = new File(resourcePath);
		if (!file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.error("create logo_show.js error:" + e.getMessage());
			}
		}
		
		/*-----获取所有皮肤下面图片的md5，来判读图片是否更改-----*/
    	String resourceImgPath = null;

		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/bluethink/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String bluethink_MD5 = getImgMD5(resourceImgPath);
		
		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/gov/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String gov_MD5 = getImgMD5(resourceImgPath);
		
		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/greenery/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String greenery_MD5 = getImgMD5(resourceImgPath);

		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/happyWorld/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String happyWorld_MD5 = getImgMD5(resourceImgPath);
		
		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/nightSky/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String nightSky_MD5 = getImgMD5(resourceImgPath);
		
		resourceImgPath = path.substring(1, path.lastIndexOf("WEB-INF"))+"skin/placidValley/images/login/watermark_logo.png";
		try { 
			resourceImgPath = URLDecoder.decode(resourceImgPath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String placidValley_MD5 = getImgMD5(resourceImgPath);
			
		FileWriter fw = null;
		Writer out = null;
		try {
	          FileOutputStream fos = new FileOutputStream(resourcePath);
	          out = new OutputStreamWriter(fos, "UTF-8");
	          out.write("var mydiv = document.createElement(\"div\");\r");
	          out.write("mydiv.setAttribute(\"class\",\"login_topright\");\r");
	          out.write("mydiv.style.position = \"absolute\";\r");
	          out.write("mydiv.style.width = \"132px\";\r");
	          out.write("mydiv.style.height = \"44px\";\r");
	          out.write("mydiv.style.right = \"20px\";\r");
	          out.write("mydiv.style.top = \"30px\";\r");
	          out.write("mydiv.style.zIndex = \"1000\";\r");
	          out.write("if(imgPath == \"/oa/skin/bluethink\"){if(\""+bluethink_MD5+"\" == \"8fb06097631f44ba4952b5808a6571ca\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("if(imgPath == \"/oa/skin/gov\"){if(\""+gov_MD5+"\" == \"75c18ea426efc60a36f314eef1697d43\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("if(imgPath == \"/oa/skin/greenery\"){if(\""+greenery_MD5+"\" == \"55d7513847f79ee34cbc07c35acdd3c0\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("if(imgPath == \"/oa/skin/happyWorld\"){if(\""+happyWorld_MD5+"\" == \"8fb06097631f44ba4952b5808a6571ca\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("if(imgPath == \"/oa/skin/nightSky\"){if(\""+nightSky_MD5+"\" == \"8fb06097631f44ba4952b5808a6571ca\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("if(imgPath == \"/oa/skin/placidValley\"){if(\""+placidValley_MD5+"\" == \"75c18ea426efc60a36f314eef1697d43\"){mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\"><img src=\\\"\"+imgPath+\"/images/login/watermark_logo.png\\\" width=132 height=44 /></a>\";\r}else{mydiv.innerHTML = \"<a href=\\\"http://www.yimihome.com\\\" target=\\\"_blank\\\" style=\\\"color:red;\\\"><img src=\\\"_blank\\\" title=\\\"\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d\\\"/>\u66f4\u6362\u56fe\u6807\u8bf7\u8054\u7cfb\u4e00\u7c73\u5ba2\u670d</a>\";\r}}");
	          out.write("document.body.appendChild(mydiv);");
	          out.flush();
	          
		 } catch (IOException e) {
			 logger.error("Error writing logo_show.js content:" + e.getMessage());
		 } finally {
			 if (fw!=null) {
				 try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
			 if (out!=null) {
		          try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		 }
		return flag;
	}
	
	private String getImgMD5(String resourceImgPath) {
		String md5 = "";
		InputStream inputStream = null;
		File imgFile = new File(resourceImgPath);
		if(imgFile.exists()){
			try {
				inputStream = new FileInputStream(imgFile);
				md5 = DigestUtils.md5Hex(inputStream);
			}catch (FileNotFoundException e) {
			}catch (IOException e){
			}finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
			
		}
		return md5;
	}
	/**
	 * 校验index.jsp中是否引用logo_show.js文件，若引用，则不予修改，若无，则修改，把引用代码添加到</HTML>上面一行
	 * @return
	 */
	private boolean checkIndexJsp()
	{
		boolean flag = false;
		String path = this.getClass().getResource("/").getPath();
		String resourcePath = path.substring(1, path.lastIndexOf("WEB-INF"))+"index.jsp";
		try { 
			resourcePath = URLDecoder.decode(resourcePath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String js = "<script src=\"js/logo_show.js\" type=\"text/javascript\" ></script>";
		return check(resourcePath,js);
	}

	private boolean checkFlow_designerJsp(){
		boolean flag = false;
		String path = this.getClass().getResource("/").getPath();
		String resourcePath = path.substring(1, path.lastIndexOf("WEB-INF"))+"admin/flow_designer.jsp";
		try { 
			resourcePath = URLDecoder.decode(resourcePath, "utf-8"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        }
		String js = "<script src=\"../js/flow_logo.js\" type=\"text/javascript\" ></script><%}%>";
		return check(resourcePath,js);
	}
	
	private boolean check(String resourcePath,String js) {
		boolean flag = false;
		BufferedReader bufferedReader = null;
		FileInputStream input = null;
		InputStreamReader isr = null;

		FileOutputStream output = null;
		OutputStreamWriter osw = null;
		BufferedWriter bufferedWriter = null;
		
		try {
			input = new FileInputStream(resourcePath);
			isr = new InputStreamReader(input, "UTF-8");
			bufferedReader = new BufferedReader(isr);
			List<String> list = new ArrayList<String>();
			String lineTxt = null;
			int n = 0;
			int temp = -1;
			while((lineTxt = bufferedReader.readLine())!=null){ 
				if("</html>".equalsIgnoreCase(lineTxt.trim())){
					temp = n;
				}
				list.add(lineTxt);
				n++;
			}
			
			/*
			input.close();
			input = null;
			isr.close();
			isr = null;
			bufferedReader.close();
			bufferedReader = null;
			*/
			
			lineTxt =  (String)list.get(temp-1);

			if(!js.equalsIgnoreCase(lineTxt.trim()) && !"<script src=\"../js/flow_logo.js\" type=\"text/javascript\" ></script>".equalsIgnoreCase(lineTxt.trim())){
				output = new FileOutputStream(resourcePath);			
				osw = new OutputStreamWriter(output,"UTF-8");
				bufferedWriter = new BufferedWriter(osw);
				for(int i=0;i<n;i++){
					if(i == temp){
						// 20170120 fgf 发现这里存在无厘头错误，../js/flow_logo.js这个路径在根目录之上，根本无法访问，且flow_logo.js不是用于index.jsp的，应为logo_show.js
						// 暂时不予更正
						bufferedWriter.write("<script src=\"../js/flow_logo.js\" type=\"text/javascript\" ></script>");
						bufferedWriter.newLine();
					}
					bufferedWriter.write((String)list.get(i));
					bufferedWriter.newLine();
				}
				bufferedWriter.flush();
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("write index.jsp Encoding error:" + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("index.jsp can not find:" + e.getMessage());
		}catch(IOException ioe) {
			logger.error("write index.jsp content error:" + ioe.getMessage());
		}
		finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (isr!=null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bufferedReader!=null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (output!=null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
			if (osw!=null) {
				try {
					osw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bufferedWriter!=null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}
	
}
