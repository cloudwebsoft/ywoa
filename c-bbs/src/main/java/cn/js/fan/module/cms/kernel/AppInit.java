package cn.js.fan.module.cms.kernel;

import java.io.*;
import javax.servlet.*;
import cn.js.fan.web.Global;
import cn.js.fan.kernel.Scheduler;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AppInit implements Servlet {
    public AppInit() {
    }

    /**
     * destroy
     *
     * @todo Implement this javax.servlet.Servlet method
     */
    public void destroy() {
        System.out.println(Global.AppName + " has been stopped.");
        SchedulerManager sm = SchedulerManager.getInstance();
        sm.shutdown(); // 结束调度
        Scheduler.getInstance().doExit();
    }

    /**
     * getServletConfig
     *
     * @return ServletConfig
     * @todo Implement this javax.servlet.Servlet method
     */
    public ServletConfig getServletConfig() {
        return null;
    }

    /**
     * getServletInfo
     *
     * @return String
     * @todo Implement this javax.servlet.Servlet method
     */
    public String getServletInfo() {
        return "";
    }

    /**
     * init
     *
     * @param servletConfig ServletConfig
     * @throws ServletException
     * @todo Implement this javax.servlet.Servlet method
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        System.out.println(Global.AppName + " has been started.");

        SchedulerManager sm = SchedulerManager.getInstance();
    }

    /**
     * service
     *
     * @param servletRequest ServletRequest
     * @param servletResponse ServletResponse
     * @throws ServletException
     * @throws IOException
     * @todo Implement this javax.servlet.Servlet method
     */
    public void service(ServletRequest servletRequest,
                        ServletResponse servletResponse) throws
            ServletException, IOException {
    }
}
