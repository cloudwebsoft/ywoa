package com.redmoon.oa;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.pvg.Privilege;

public class DownloadFilter
        implements Filter {
    FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException, IOException {
        // ServletContext context = config.getServletContext();
        if (req instanceof HttpServletRequest) {
            HttpServletRequest req1 = (HttpServletRequest) req;
            Privilege privilege = new Privilege();

            String requrl = req1.getRequestURL().toString();
            // System.out.println(getClass() + " requrl="+requrl);
            if (requrl.indexOf("bak") != -1 || requrl.indexOf("CacheTemp") != -1) {
                // 如果不是管理员
                if (!privilege.isUserPrivValid(req1, "admin")) {
                    String url = "http://" + req1.getServerName() + ":" + req1.getServerPort() + req1.getContextPath() + "/images/err_pvg.gif"; // onerror.htm";//  + req1.getRequestURI();
                    //rd = req.getRequestDispatcher(url);
                    //rd.forward(req, res); //有时这行会不灵，所以用下行
                    ((HttpServletResponse) res).sendRedirect(url);
                    return;
            /*
            res.setContentType("text/html;charset=gb2312");
            PrintWriter out = res.getWriter();
            out.print("<p align=center>对不起，您未获得授权！</p>");
            return;
            */
                }
            } else {
                String requestUri = req1.getRequestURI();
                String ctxPath = req1.getContextPath();
                String path = requestUri.substring(ctxPath.length());
                // 如果未登录
                if (!privilege.isUserLogin(req1)) {
                    com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
                    boolean isAccessUpfileNeedLogin = scfg.getBooleanProperty("isAccessUpfileNeedLogin");

                    if (path.equals("/") ||
                            path.indexOf("/WXCallBack") != -1 ||
                            path.indexOf("/WXAddressCallBackServlet") != -1 ||
                            path.indexOf("/DdEventChangeReceiveServlet") != -1 ||
                            path.indexOf("/weixin") != -1 ||
                            path.indexOf("/dingding") != -1 ||
                            path.indexOf("common.js") != -1 ||
                            path.indexOf("module_field_ajax.jsp") != -1 ||
                            path.indexOf("/setup") != -1 ||
                            path.equals("/index.jsp") ||
                            path.indexOf("/login_oa") != -1 || // login_oa_ajax.jsp
                            path.indexOf("/checkuser_ajax") != -1 || // checkuser_ajax.jsp
                            path.indexOf("/activex") != -1 ||
                            path.indexOf("/public") != -1 ||
                            path.indexOf("/js/") != -1 ||
                            path.indexOf("/inc/") != -1 ||
                            path.indexOf("/skin") != -1 ||
                            path.indexOf("/other") != -1 || // 预留目录,便于与其它系统对接，如MAS机
                            path.indexOf("chatservice") != -1 ||
                            path.indexOf("images/") != -1 ||
                            path.indexOf("nest_") != -1 || // 嵌套表   20140913 fgf
                            path.indexOf("/flow/") != -1 ||
                            path.indexOf("module_sel.jsp") != -1 || // 当表单设计时选择宏控件
                            path.indexOf("basic_select_sel.jsp") != -1 || // 当表单设计时选择宏控件
                            path.indexOf("flow_sequence_sel.jsp") != -1 || // 当表单设计时选择宏控件
                            path.indexOf("module_field_sel.jsp") != -1 || // 当表单设计时选择宏控件

                            (!isAccessUpfileNeedLogin && path.indexOf("/upfile") != -1) ||

                            // path.indexOf("netdisk_office_")!=-1 ||
                            path.indexOf("/wap") != -1 ||
                            path.indexOf("/test") != -1 ||
                            // path.indexOf("/m/")!=-1 ||
                            path.indexOf("desktop") != -1 ||
                            path.indexOf("admin/ide_left.jsp") != -1 ||
                            path.indexOf("reportServlet") != -1 ||
                            path.indexOf("/yimi_userconsole") != -1 ||//过滤一米精灵后台配置
                            path.indexOf(".txt") != -1 || // 企业微信域名归属验证，如：WW_verify_***.txt
                            path.indexOf("wiki_export_doc") != -1 ||
                            path.indexOf("exam") != -1 ||
                            path.indexOf("macro") != -1 ||
                            path.indexOf("module_check") !=-1 || // 模块中字段的验证，如是否邮箱、手机号、身份证等
                            requrl.indexOf("/error.jsp") != -1) {
                        ;
                    } else {
                        // System.out.println(getClass() + " path=" + path);
                        com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("Filter error: path=" + path);
                        String url = Global.getFullRootPath(req1) + "/error.jsp?type=login";
                        ((HttpServletResponse) res).sendRedirect(url);
                        return;
                    }
                } else {
                    String tester = (String) Privilege.getAttribute(req1, Privilege.SESSION_OA_FLOW_TESTER); // 流程测试员
                    if (tester != null) {
                        // 如果流程测试员不是当前的登录用户，则仅允许进入flow_dispose.jsp及flow_list_debugger.jsp
                        if (!tester.equals(privilege.getUser(req1))) {
                            if (path.indexOf("/skin") != -1 ||
                                    path.indexOf("error.jsp") != -1 ||
                                    path.indexOf("/js") != -1 ||
                                    // path.indexOf(".css")!=-1 ||
                                    path.indexOf("/inc") != -1 ||
                                    path.indexOf("images") != -1 ||
                                    path.indexOf("flow_dis") != -1 || // flow_dispose.jsp
                                    path.indexOf("getFuncVal.do") != -1 ||
                                    // path.indexOf("flow_dispose_ajax_att")!=-1 || // flow_dispose_ajax_att.jsp
                                    path.indexOf("debugger") != -1 ||  // flow_list_debugger.jsp
                                    path.indexOf("macro") != -1 ||  // flow/macro/macro_user_select_win_ctl_js.jsp
                                    path.indexOf("flow_getfile") != -1 ||
                                    path.indexOf("flow_ntko_") != -1 ||
                                    path.indexOf("user_multi_sel") != -1 ||
                                    path.indexOf("flow_do") != -1 ||
                                    path.indexOf("flow_action_modify") != -1 || // 用于选择用户
                                    path.indexOf("module_show") != -1 || // 查看嵌套表内容
                                    path.indexOf("nest_") != -1) { // 嵌套表
                            } else if (path.indexOf("flow_") != -1 ||
                                    path.indexOf("oa.jsp") != -1 ||
                                    path.indexOf("document") != -1 ||
                                    path.indexOf("sale") != -1 ||
                                    path.indexOf("plan") != -1 ||
                                    path.indexOf("user") != -1 ||
                                    path.indexOf("netdisk") != -1 ||
                                    path.indexOf("project") != -1 ||
                                    path.indexOf("visual") != -1
                            ) {
                                try {
                                    com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("tester invalid path=" + path);
                                    privilege.logout(req1, (HttpServletResponse) res);
                                } catch (ErrMsgException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                String url = Global.getFullRootPath(req1) + "/index.jsp";
                                ((HttpServletResponse) res).sendRedirect(url);
                                return;
                            }
                        }
                    }
                }
            }

        }

        chain.doFilter(req, res);
        if (true)
            return;
        ByteArrayResponseWrapper wrapper = new
                ByteArrayResponseWrapper((HttpServletResponse) res);
        chain.doFilter(req, wrapper);

        // System.out.println("length3="+wrapper.getData().length);

        // 如果没有下面这几行，则length3的值为0，说明未真正操作取得数据
        OutputStream out = res.getOutputStream();//这儿注意不能用printwriter
        out.write(wrapper.getData());
        // System.out.println("length4="+wrapper.getData().length);

    }

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void destroy() {
        this.config = null;
    }
}
