package com.redmoon.forum.util;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import java.util.Map;
import java.util.regex.Pattern;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import com.redmoon.forum.ForumDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ForumFilter {

    public ForumFilter() {
    }

    public static boolean filterUserName(HttpServletRequest request, String name) throws ErrMsgException {
        ForumDb fd = ForumDb.getInstance();
        int len = fd.filterUserNameAry.length;
        for (int i=0; i<len; i++) {
            Pattern pat = Pattern.compile(
                    fd.filterUserNameAry[i],
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            // System.out.println(ForumFilter.class.getName() + " " + fd.filterUserNameAry[i]);
            Matcher m = pat.matcher(name);
            if (m.find()) {
                String str = SkinUtil.LoadString(request, "res.forum.Forum",
                                                 "err_user_name");
                String s = fd.filterUserNameAry[i].replaceAll("\\.\\*\\?", "");
                str = str.replaceFirst("\\$s", s);
                throw new ErrMsgException(str); // "对不起，名称中含有非法关键字：" + filterUserNameAry[i]);
            }
        }
        return true;
    }

    public static String filterMsg(HttpServletRequest request, String msg) throws ErrMsgException {
        ForumDb fd = ForumDb.getInstance();
        Map map = fd.getFilterMsgMap();
        int len = fd.filterMsgAry.length;
        for (int i=0; i<len; i++) {
            Pattern pat = Pattern.compile(
                    fd.filterMsgAry[i],
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            // System.out.println(ForumFilter.class.getName() +" fd.filterMsgAry[" + i + "]=" +  fd.filterMsgAry[i]);
            // System.out.println(ForumFilter.class.getName() + " " + fd.filterUserNameAry[i]);
            Matcher m = pat.matcher(msg);
            if (m.find()) {
            	String replaceStr = (String)map.get(fd.filterMsgAry[i]);
            	// System.out.println(ForumFilter.class.getName() +" replaceStr=" +  replaceStr);
            	if (replaceStr==null) {
	                String str = SkinUtil.LoadString(request, "res.forum.Forum", "err_msg");
	                String s = fd.filterMsgAry[i].replaceAll("\\.\\*\\?", "");
	                str = str.replaceFirst("\\$s", s);
	                throw new ErrMsgException(str); // "对不起，语句中含有非法关键字：" + filterMsgAry[i]);
            	}
            	else {
            		msg = m.replaceAll(replaceStr);
            	}
            }
        }
        return msg;
    }
}
