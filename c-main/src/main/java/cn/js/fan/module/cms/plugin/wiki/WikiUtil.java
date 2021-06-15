package cn.js.fan.module.cms.plugin.wiki;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

public class WikiUtil {
	
	public static String parseH3(String cont, WikiDoc wd, String num) {
	       	String patternStr = "<H3>(.*?)<\\/H3>";
	        Pattern pattern = Pattern.compile(patternStr,
	                                          Pattern.DOTALL |
	                                          Pattern.CASE_INSENSITIVE);

//	    var reg = new RegExp("<H2>(.*?)<\/H2>", "ig");
//	    var reg3 = new RegExp("<H3>(.*?)<\/H3>", "ig");

	        Matcher matcher = pattern.matcher(cont);
	        StringBuffer sb = new StringBuffer();
	        boolean result = matcher.find();
	        
	        int k = 1;
	        if (result) {
		        while (result) {
		            String str =
		                    "<h3 name='sec_" + num + "_" + k + "'>$1</h3>";
		            		            
		            WikiDocLeaf lf = new WikiDocLeaf(num + "_" + k);
		            lf.setTitle(matcher.group(1));
		            lf.setNo(k);
		            
		            wd.getRoot().getChild(num).addChild(lf);
		            
		            // System.out.println("t1_1=" + t1_1);
		            
		            matcher.appendReplacement(sb, str);
		            result = matcher.find();
		            
		            k++;
		        }
	        }
	        else
	        	k++;
	        
	        matcher.appendTail(sb);
	        cont = sb.toString();	
	        
	        // System.out.println("cont=" + cont);
	        return cont;
	}
	
    public static WikiDoc parseDocument(String content) {
    	WikiDoc wd = new WikiDoc();
    	
    	// 一级目录
        // String patternStr = "\\[whitepad\\](.[^\\[]*)\\[\\/whitepad\\]";
        String patternStr = "<H2>(.*?)<\\/H2>";
        Pattern pattern = Pattern.compile(patternStr,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        int k = 1;
        
        // 第一次遍历，只是修改h2
        while (result) {
            String str =
                    "<h2 name='sec_" + k + "'>$1</h2>";
            
            WikiDocLeaf leaf = new WikiDocLeaf("" + k);
            leaf.setTitle(matcher.group(1));
            leaf.setNo(k);
            
            wd.getRoot().addChild(leaf);
            
            matcher.appendReplacement(sb, str);
            result = matcher.find();
            k++;
        }
        
        matcher.appendTail(sb);
                
        content = sb.toString();
        
        // System.out.println("content=" + content);

        // 第二次遍历，修改h2
        patternStr = "<H2 name='sec_([0-9]+?)'>(.*?)<\\/H2>";
        pattern = Pattern.compile(patternStr,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);


        matcher = pattern.matcher(content);
        
        result = matcher.find();
        
        sb = new StringBuffer();        
        
        int a = 0;
        int b = 0;
        if (result)
        	a = matcher.end();
        String strNum = "";
        String newContent = "";
        
        // System.out.println("content=" + content);
        // System.out.println("patternStr=" + patternStr + " result=" + result);
        
        while (result) {
            strNum = matcher.group(1);
            
            // System.out.println("strNum0=" + strNum);
            
            
            result = matcher.find();
            
            if (result) {
	            b = matcher.start();
	            
	            // 解析二级目录
	            
	            // System.out.println("a=" + a + " b=" + b + " strNum=" + strNum);
	            
	            String cont = parseH3(content.substring(a, b), wd, strNum);
	            
	            newContent = content.substring(0, a) + cont + content.substring(b);

	            // 获得a与b之间的t1_1
	            a = matcher.end() + 1;
            }
        }
        
        // 最后一个t1_1
        if (a>0 && a<content.length()-1) {
            // 解析剩余部分的二级目录
        	// System.out.println("strNum=" + strNum);
            String cont = parseH3(content.substring(a), wd, strNum);
	        newContent = content.substring(0, a) + cont;
        }
        
        if (newContent.equals("")) {
        	newContent = content;
        }

        // System.out.println(WikiUtil.class.getName() + " newContent=" + newContent);
        
        wd.setContent(newContent);
        
        // System.out.println("content=" + newContent);
        return wd;
    }
    
    /**
     * 将目录解析为word格式
     * @param chapterNo
     * @param wd
     * @return
     */
    public static String renderDocDir(int chapterNo, WikiDoc wd) {
        String realPath = Global.getRealPath(); // "d:/cwbbs/WebRoot/";

    	// String path = Global.getRealPath() + "cms/plugin/wiki/admin/template/";
    	String path = realPath + "cms/plugin/wiki/admin/template/";
    	String t_dir_t1_1 = "", t_dir_t1_1_1 = "";
    	
    	try {
	    	t_dir_t1_1 = FileUtil.ReadFile(path + "t-dir-t1-1.htm");
	    	t_dir_t1_1_1 = FileUtil.ReadFile(path + "t-dir-t1-1-1.htm");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	// String path = Global.getRealPath() + "cms/plugin/wiki/admin/template/";
		
    	Iterator ir = wd.getRoot().getChildren().iterator();
    	
    	String docDir = "";
    	while (ir.hasNext()) {
    		WikiDocLeaf lf = (WikiDocLeaf)ir.next();
    		
    		String t = t_dir_t1_1;
    		t = t.replace("[title]", lf.getTitle());
    		t = t.replace("[no]", chapterNo + "." + lf.getNo());
    		t = t.replaceAll("\\[anchor\\]", chapterNo + "_" + lf.getNo());
    		
    		docDir += t;    		
    		
    		Iterator ir2 = lf.getChildren().iterator();
    		while (ir2.hasNext()) {
    			WikiDocLeaf lf2 = (WikiDocLeaf)ir2.next();
    			
    			t = t_dir_t1_1_1;
    	   		t = t.replace("[title]", lf2.getTitle());
        		t = t.replace("[no]", chapterNo + "." + lf.getNo() + "." + lf2.getNo());
        		t = t.replaceAll("\\[anchor\\]", chapterNo + "_" + lf.getNo() + "_" + lf2.getNo());
        		
        		docDir += t;
    		}
    	}
    	
    	return docDir;
    }
    
    /**
     * 将h2解析为word中的格式
     * @param chapter
     * @param content
     * @return
     */
	public static String renderDocH2(int chapter, String content) {
			// System.out.println("doch2=" + content);
		
			String realPath = Global.getRealPath(); // "d:/cwbbs/WebRoot/";

	    	String path = realPath + "cms/plugin/wiki/admin/template/";
	    	
	    	String t_content_t1_1 = "";
	    	
	    	try {
		    	t_content_t1_1 = FileUtil.ReadFile(path + "t-content-t1-1.htm");
		    	
		    	t_content_t1_1 = t_content_t1_1.replaceAll("\\[chapter\\]", ""+chapter);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	       	String patternStr = "<H2 name='sec_([0-9]+?)'>(.*?)<\\/H2>";
			Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL
					| Pattern.CASE_INSENSITIVE);
	
			Matcher matcher = pattern.matcher(content);
			boolean result = matcher.find();
	
			int a = 0;
			if (result)
				a = matcher.start();
			
			String newContent = "";
			StringBuffer sb = new StringBuffer();
			
			while (result) {
				// System.out.println("t1_1=" + t1_1);
				int b = matcher.end();
				
				String t = t_content_t1_1.replaceAll("\\[no\\]", matcher.group(1));
				t = t.replace("[title]", matcher.group(2));
				
				
	            matcher.appendReplacement(sb, t);

				/*
				System.out.println("no=" + matcher.group(1));
				System.out.println("title=" + matcher.group(2));
				System.out.println("t=" + t);
				*/
				
				newContent = content.substring(0, a) + t + content.substring(b + 1);

				result = matcher.find();
				if (result) {
					a = matcher.start();
				}
			}
			
	        matcher.appendTail(sb);

			if (newContent.equals(""))
				newContent = content;

			// System.out.println("newContent123=" + newContent);
			return sb.toString();
	}    
    
	/**
	 * 将h3解析为word中的格式
	 * @param chapter
	 * @param content
	 * @return
	 */
	public static String renderDocH3(int chapter, String content) {
		String realPath = Global.getRealPath(); // "d:/cwbbs/WebRoot/";

    	String path = realPath + "cms/plugin/wiki/admin/template/";
    	
    	String t_content_t1_1_1 = "";
    	
    	try {
    		t_content_t1_1_1 = FileUtil.ReadFile(path + "t-content-t1-1-1.htm");
	    	
    		t_content_t1_1_1 = t_content_t1_1_1.replaceAll("\\[chapter\\]", ""+chapter);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
       	String patternStr = "<H3 name='sec_([0-9]+?)_([0-9]+?)'>(.*?)<\\/H3>";
		Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL
				| Pattern.CASE_INSENSITIVE);


		Matcher matcher = pattern.matcher(content);
		boolean result = matcher.find();
		// System.out.println(WikiUtil.class.getName() + " result=" + result);

		int a = 0;
		if (result)
			a = matcher.start();
		
		String newContent = "";
		StringBuffer sb = new StringBuffer();

		int p = 0;
		
		while (result) {
			// System.out.println("t1_1=" + t1_1);
			int b = matcher.end();

			String t = t_content_t1_1_1.replaceAll("\\[no1\\]", matcher.group(1));
			t = t.replaceAll("\\[no2\\]", matcher.group(2));
			t = t.replace("[title]", matcher.group(2));
			if (content.length()-1 >b)
				newContent = content.substring(0, a) + t + content.substring(b + 1);
			else
				newContent = content.substring(0, a) + t;

            matcher.appendReplacement(sb, t);

			result = matcher.find();
			
			if (result) {
				a = matcher.start();
			}
		}
        matcher.appendTail(sb);

		if (newContent.equals(""))
			newContent = content;		

		// System.out.println("cont=" + cont);
		return sb.toString();
	}    	
	
	   public static String renderDocImage(String content) {	    		        
	        String patternStr = "<img (.*?)src=\"/(.*?)\"([^>]*?)>";	        
	        
	        Pattern pattern = Pattern.compile(patternStr,
	                                          Pattern.DOTALL |
	                                          Pattern.CASE_INSENSITIVE);

	        Matcher matcher = pattern.matcher(content);
	        StringBuffer sb = new StringBuffer();
	        boolean result = matcher.find();
	        int k = 1;
	        
	        // 第一次遍历，只是修改h2
	        while (result) {
	            String str = "<img$1src=\"" + Global.getFullRootPath() + "\\/$2\"$3>";
	            // String str = "<img$1src=\"cws\\/$2\"$3>";
	            
	            matcher.appendReplacement(sb, str);
	            result = matcher.find();
	            k++;
	        }
	        
	        matcher.appendTail(sb);
	                
	        content = sb.toString();
	        
	        return content;
	   }

	
	/**
	 * 将内容解析为word中的格式
	 * @param chapterNo
	 * @param wd
	 * @return
	 */
    public static String renderDocContent(int chapterNo, WikiDoc wd) {
    	String cont = renderDocH2(chapterNo, wd.getContent());
    	cont = renderDocH3(chapterNo, cont);
    	cont = renderDocImage(cont);
    	return cont;
    }    
	
	public static void main(String[] args) throws Exception {
		
		WikiUtil wu = new WikiUtil();
		
		
		String str = "<img alt=\"点击在新窗口中打开\" onclick=\"window.open('http://localhost:8080/cwbbs/upfile/webeditimg/2011/2/bc886851e1b34163b88781d9120b9706.jpg')\" onload=\"if(this.width&gt;screen.width-333)this.width=screen.width-333\" src=\"/cwbbs/upfile/webeditimg/2011/2/bc886851e1b34163b88781d9120b9706.jpg\" style=\"cursor: hand\" /><br />";
		
		System.out.println(renderDocImage(str));
		
		/*
		String content = FileUtil.ReadFile("d:/text.txt");
		WikiDoc wd = wu.parseDocument(content);
		
		System.out.println(renderDocDir(1, wd));
		
		System.out.println(wd.getContent());
		*/
		
		// System.out.println(wd.getContent());
		
	}
}
