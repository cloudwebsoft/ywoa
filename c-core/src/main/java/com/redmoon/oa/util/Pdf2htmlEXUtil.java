package com.redmoon.oa.util;

import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.File;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-2-18上午08:07:01
 */
public class Pdf2htmlEXUtil {
	/**
     * 调用pdf2htmlEX将pdf文件转换为html文件
     * 
     * @param exeFilePath
     *            pdf2htmlEX.exe文件路径
     * @param pdfFile
     *            pdf文件绝对路径
     *            生成的html文件名称
     * @return
     */
    public static boolean pdf2html(String exeFilePath, String pdfFile,
            String destDir, String htmlFileName) {
        if (!(exeFilePath != null && !"".equals(exeFilePath) && pdfFile != null
                && !"".equals(pdfFile) && htmlFileName != null && !""
                    .equals(htmlFileName))) {
            LogUtil.getLog(Pdf2htmlEXUtil.class).error("传递的参数有误！");
            return false;
        }
        
        destDir = destDir.replaceAll("/", "\\\\");
        
        Runtime rt = Runtime.getRuntime();
        StringBuilder command = new StringBuilder();
        command.append(exeFilePath).append(" ");
        if (destDir != null && !"".equals(destDir.trim()))// 生成文件存放位置,需要替换文件路径中的空格
            command.append("--dest-dir ").append(destDir.replace(" ", "\" \""))
                    .append(" ");
        command.append("--optimize-text 1 ");// 尽量减少用于文本的HTML元素的数目 (default: 0)
        command.append("--zoom 1.4 ");
        command.append("--process-outline 0 ");// html中显示链接：0——false，1——true
        command.append("--font-format woff ");// 嵌入html中的字体后缀(default ttf)
                                                // ttf,otf,woff,svg
        command.append(pdfFile.replace(" ", "\" \"")).append(" ");// 需要替换文件路径中的空格
        // --embed-image <int>           将图片文件嵌入到输出中 (default: 1)  
        // command.append("--embed-image 0 ");
        
        if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
            command.append(htmlFileName);
            if (!htmlFileName.contains(".html")) {
                command.append(".html");
            }
        }
        try {
            LogUtil.getLog(Pdf2htmlEXUtil.class).info("Command：" + command.toString());
            Process p = rt.exec(command.toString());
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            // 开启屏幕标准错误流
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            // 开启屏幕标准输出流
            outGobbler.start();
            int w = p.waitFor();
            int v = p.exitValue();
            if (w == 0 && v == 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.getLog(Pdf2htmlEXUtil.class).error(e);
        }
        return false;
    }


    public static boolean pdf2html_linux(String pdfFile, String destDir,
            String htmlFileName) {
        if (!(pdfFile != null && !"".equals(pdfFile) && htmlFileName != null && !""
                .equals(htmlFileName))) {
            LogUtil.getLog(Pdf2htmlEXUtil.class).error("传递的参数有误！");
            return false;
        }
        Runtime rt = Runtime.getRuntime();
        StringBuilder command = new StringBuilder();
        command.append("pdf2htmlEX").append(" ");
        if (destDir != null && !"".equals(destDir.trim()))// 生成文件存放位置,需要替换文件路径中的空格
            command.append("--dest-dir ").append(destDir.replace(" ", "\" \""))
                    .append(" ");
        command.append("--optimize-text 1 ");// 尽量减少用于文本的HTML元素的数目 (default: 0)
        command.append("--process-outline 0 ");// html中显示链接：0——false，1——true
        command.append("--font-format woff ");// 嵌入html中的字体后缀(default ttf)
                                                // ttf,otf,woff,svg
        command.append(pdfFile.replace(" ", "\" \"")).append(" ");// 需要替换文件路径中的空格
        if (htmlFileName != null && !"".equals(htmlFileName.trim())) {
            command.append(htmlFileName);
            if (htmlFileName.indexOf(".html") == -1)
                command.append(".html");
        }
        try {
            LogUtil.getLog(Pdf2htmlEXUtil.class).info("Command：" + command.toString());
            Process p = rt.exec(command.toString());
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
                    "ERROR");
            // 开启屏幕标准错误流
            errorGobbler.start();
            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
                    "STDOUT");
            // 开启屏幕标准输出流
            outGobbler.start();
            int w = p.waitFor();
            int v = p.exitValue();
            if (w == 0 && v == 0) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.getLog(Pdf2htmlEXUtil.class).error(e);
        }
        return false;
    }
    
    public synchronized static boolean createPreviewHTML(String previewfile) {
    	// 专业版不提供生成预览功能
    	// if (License.getInstance().getVersionType().equals(License.VERSION_PROFESSIONAL))
    	// if (true)
    	// 	return false;
    	
    	boolean returnValue = false;
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
    	if (canPdfFilePreview) {
    		//判断操作系统，windows可以生成预览文件
			if ("\\".equals(File.separator)) {
				//根据上传文件生成预览文件
				pdfFilePreview(previewfile);
				//判断是否成功生成html预览文件
				String htmlfile=previewfile.substring(0, previewfile.lastIndexOf("."))+".html";
				File fileExist=new File(htmlfile);
				if(fileExist.exists()){
					returnValue=true;
				}else{
					returnValue=false;
				}
			} else if ("/".equals(File.separator)) {
				returnValue=false;
			} 
    	}
    	return returnValue;
    }    
    
    public static void pdfFilePreview(String filepath){
    	//生成的html文件
    	String htmlfile=filepath.substring(0,filepath.lastIndexOf("."))+".html";
    	//获取上传文件后缀	
    	String fileType=filepath.substring(filepath.lastIndexOf(".")+1);
    	
    	//先删除原有html文件
    	File htmlFile=new File(htmlfile);
    	if(htmlFile.exists()){
    		if(htmlFile.isFile()){
    			htmlFile.delete();
    		}
    	}
    	
    	String exePath = Global.getRealPath() + "tools/pdf2htmlEX/pdf2htmlEX.exe";
		int p = filepath.lastIndexOf("/");
    	if (p==-1) {
    		p = filepath.lastIndexOf("\\");
    	}
    	
    	String destDir = filepath.substring(0, p);
    	String destFileName = filepath.substring(p + 1, filepath.lastIndexOf("."));
    	pdf2html(exePath, filepath, destDir, destFileName);
    }    
    
    /**
     * 删除预览文件
     * @Description: 
     * @param filepath 原始文件的完整路径
     */
    public static void del(String filepath){
    	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    	boolean canPdfFilePreview = cfg.getBooleanProperty("canPdfFilePreview");
    	if (canPdfFilePreview) {    	
	    	String htmlfile=filepath.substring(0,filepath.lastIndexOf("."))+".html";
	    	File htmlFile=new File(htmlfile);
	    	if(htmlFile.exists()){
	    		if(htmlFile.isFile()){
	    			htmlFile.delete();
	    		}
	    	}
    	}
    }
    
    // (注意pdf2htmlEX.exe文件不要单独copy出来用,
    // 需要和pdf2htmlEX-v1.0文件夹里面的东西放在一起使用,
    // 不然会报错:Error: Cannot open the manifest file)
      
    public static void main(String[] args) {          
        pdf2html("D:\\oa\\WebRoot\\tools\\pdf2htmlEX\\pdf2htmlEX.exe","D:\\v.pdf","D:\\test","v.html");        
    }  
}  