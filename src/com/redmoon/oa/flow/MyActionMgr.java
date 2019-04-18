package com.redmoon.oa.flow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class MyActionMgr {

    public static String renderResultForMobile(MyActionDb mad) {
        String content = mad.getResult();
        
		boolean isImg = false;
		content = StrUtil.getAbstract(null, content, 10000, " ", isImg);
                
        String patternStr = "#(.*?)#";
        Pattern pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
		boolean re = matcher.find();
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        FormDb prjFd = new FormDb();
        prjFd = prjFd.getFormDb("project");
        while (re) {
        	String prjId = matcher.group(1);

        	int projectId = StrUtil.toInt(prjId, -1);
        	if (projectId==-1) {
    			re = matcher.find();        		
        		continue;
        	}
            fdao = fdao.getFormDAO(projectId, prjFd);
            
        	String str = " " + fdao.getFieldValue("name") + " ";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);        
        content = sb.toString();
        
        /*
        patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
		while (re) {
        	String tagName = matcher.group(1);
        	String str = "<a href='" + request.getContextPath() + "/user/t_tag_msg.jsp?tid=" + msg.getLong("t_id") + "&tagName=" + StrUtil.UrlEncode(tagName) + "'>" + tagName + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();         
        */
        
        // content = content.replaceAll("\\n", "<BR />");
        
        content = content.replaceAll("&nbsp;", " ");

        return content;
    }    
    
    public static String renderResult(HttpServletRequest request, MyActionDb mad) {
        String content = mad.getResult();
                
    	content = StrUtil.ubb(request, content, true, true);
    	
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        
        patternStr = "@(.*?)([&|<| |　])+";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
		boolean re = matcher.find();
		UserMgr um = new UserMgr();
		UserDb user;
		while (re) {
        	String userName = matcher.group(1);
        	
        	// 是否以尖括号结束，如 @test</span> 在ckeditor中
        	boolean isEndWithLeftJkh = false;
        	boolean isEndWidthAnd = false;
        	String endStr = matcher.group(2);
        	if ("<".equals(endStr)) {
        		isEndWithLeftJkh = true;
        	}
        	else if ("&".equals(endStr)) {
        		isEndWidthAnd = true;
        	}
        	
        	user = um.getUserDb(userName);
        	// 防止邮箱地址被误处理
        	if (!user.isLoaded()) {
    			re = matcher.find();
    			continue;
        	}
        	
        	String str = "<a class='at-user' href='javascript:;' onclick=\"atUser('" + user.getName() + "', '" + user.getRealName() + "')\">@" + user.getRealName() + "</a>&nbsp;&nbsp;";
			if (isEndWithLeftJkh) {
				str += "<";
			}
			else if (isEndWidthAnd) {
				str += "&";
			}
        	matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();

        // patternStr = "#(.*?)#";
        patternStr = "#([0-9]+)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        FormDb prjFd = new FormDb();
        prjFd = prjFd.getFormDb("project");
        while (re) {
        	String prjId = matcher.group(1);

        	int projectId = StrUtil.toInt(prjId, -1);
        	if (projectId==-1) {
    			re = matcher.find();        		
        		continue;
        	}
            fdao = fdao.getFormDAO(projectId, prjFd);
            
        	String str = "&nbsp;<a href='javascript:;' onclick=\"addTab('" + fdao.getFieldValue("name") + "', '" + request.getContextPath() + "/project/project_show.jsp?projectId=" + projectId + "&formCode=project" + "')\" title=\"" + fdao.getFieldValue("name") + "\">" + fdao.getFieldValue("name") + "</a>&nbsp;";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);        
        content = sb.toString();
        
        /*
        patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
		while (re) {
        	String tagName = matcher.group(1);
        	String str = "<a href='" + request.getContextPath() + "/user/t_tag_msg.jsp?tid=" + msg.getLong("t_id") + "&tagName=" + StrUtil.UrlEncode(tagName) + "'>" + tagName + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();         
        */
        
        content = content.replaceAll("\\n", "<BR />");
        
        return content;
    }    

    public static String renderTitle(HttpServletRequest request, WorkflowDb wf) {
    	String content = StrUtil.ubb(request, wf.getTitle(), true, false);
    	
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        
        /*
        StringBuffer sb = new StringBuffer();
        patternStr = "@(.*?)([ |　])+";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
		boolean re = matcher.find();
		UserMgr um = new UserMgr();
		UserDb user;
		while (re) {
        	String userName = matcher.group(1);
        	user = um.getUserDb(userName);
        	String str = "<a href='javascript:;' onclick=\"addTab('" + user.getRealName() + "', '" + request.getContextPath() + "/user_info.jsp?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getRealName() + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();
		*/
        
        patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
		boolean re = matcher.find();
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        FormDb prjFd = new FormDb();
        prjFd = prjFd.getFormDb("project");
        while (re) {
        	String prjId = matcher.group(1);

        	int projectId = StrUtil.toInt(prjId, -1);
        	if (projectId==-1) {
        		re = matcher.find();
        		continue;
        	}
            fdao = fdao.getFormDAO(projectId, prjFd);
            
        	String str = fdao.getFieldValue("name");
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);        
        content = sb.toString();
        
        /*
        patternStr = "#(.*?)#";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
        matcher = pattern.matcher(content);
        sb = new StringBuffer();
		re = matcher.find();
		while (re) {
        	String tagName = matcher.group(1);
        	String str = "<a href='" + request.getContextPath() + "/user/t_tag_msg.jsp?tid=" + msg.getLong("t_id") + "&tagName=" + StrUtil.UrlEncode(tagName) + "'>" + tagName + "</a>";
			matcher.appendReplacement(sb, str);
			re = matcher.find();
		}
        matcher.appendTail(sb);
        content = sb.toString();         
        */
        
        return content;
    }       
}
