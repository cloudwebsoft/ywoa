package com.cloudwebsoft.framework.template;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 生成HTML文件</p>
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
public class HtmlCreator {
    public HtmlCreator() {
    }

    public void service(HttpServletRequest request,
                        HttpServletResponse response, String filePath) throws
            IOException, ServletException {
        /*
        java.util.Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String param = (String) e.nextElement();
            // 接下来对元素的操作
        }
        */
       try {
           TemplateLoader tl = new TemplateLoader(request, filePath);
           String pageContent = tl.toString();
           response.getWriter().write(pageContent);
       } catch (ErrMsgException e) {
           LogUtil.getLog(getClass()).error(e);
       }
    }
}
