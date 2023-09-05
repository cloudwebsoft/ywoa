package com.cloudweb.oa.filter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
//@WebFilter(urlPatterns="/*",filterName="fireWallFilter")
//@Order(2)
public class FirewallFilter implements Filter {
    FilterConfig config;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException, IOException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest req1 = (HttpServletRequest) req;

            // 预检请求直接放行，因为预检请求头部中不带有自定义的Authorization，否则预检如果被302重定向，则下一步的modular/list也会被报cors错误（仅管其带有Authorization）
            if ("OPTIONS".equals(req1.getMethod())) {
                chain.doFilter(req, res);
                return;
            }

            Privilege privilege = new Privilege();

            String requrl = req1.getRequestURL().toString();
            // DebugUtil.i(getClass(), "doFilter", "url=" + requrl);

            if (requrl.contains("bak") || requrl.contains("CacheTemp")) {
                // 如果不是管理员
                if (!privilege.isUserPrivValid(req1, "admin")) {
                    String url = "http://" + req1.getServerName() + ":" + req1.getServerPort() + req1.getContextPath() + "/images/err_pvg.gif";
                    ((HttpServletResponse) res).sendRedirect(url);
                    return;
                }
            } else {
                String requestUri = req1.getRequestURI();
                String ctxPath = req1.getContextPath();
                String path = requestUri.substring(ctxPath.length());
                // 如果未登录
                if (!privilege.isUserLogin(req1)) {
                    com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
                    boolean isAccessUpfileNeedLogin = scfg.getBooleanProperty("isAccessUpfileNeedLogin");
                    if ("/".equals(path) ||
                            path.contains(".css") ||
                            (!isAccessUpfileNeedLogin && path.contains("/upfile")) ||
                            path.contains("/lte/css/") ||  // 从前端iframe直接进入时无session
                            path.contains("/lte/fonts/") || // 从前端iframe直接进入时无session
                            path.contains("/actuator") ||
                            path.contains("/WXCallBack") ||
                            path.contains("/WXAddressCallBackServlet") ||
                            path.contains("/DdEventChangeReceiveServlet") ||
                            path.contains("/weixin") ||
                            path.contains("/dingding") ||
                            path.contains("common") ||
                            path.contains("module_field_ajax.jsp") ||
                            path.contains("/setup") ||
                            // 首页
                            path.contains("/index") ||
                            // swagger2
                            path.contains("/swagger-ui.html") ||
                            path.contains("/webjars") ||
                            path.contains("/v2") ||
                            path.contains("/swagger-resources") ||
                            path.contains("/configuration") ||

                            path.contains("/doLogin") ||
                            path.contains("/checkuser_ajax") || // checkuser_ajax.jsp
                            path.contains("/activex") ||
                            path.contains("/public") ||
                            path.contains("/js/") ||
                            path.contains("/inc/") ||
                            path.contains("/skin") ||
                            path.contains("/other") || // 预留目录,便于与其它系统对接，如MAS机
                            path.contains("chatservice") ||
                            path.contains("images/") ||
                            path.contains("nest_") || // 嵌套表   20140913 fgf
                            path.contains("/flow/") ||
                            path.contains("module_sel.jsp") || // 当表单设计时选择宏控件
                            path.contains("basic_select_sel.jsp") || // 当表单设计时选择宏控件
                            path.contains("flow_sequence_sel.jsp") || // 当表单设计时选择宏控件
                            path.contains("module_field_sel.jsp") || // 当表单设计时选择宏控件
                            // path.indexOf("netdisk_office_")!=-1 ||
                            path.contains("/wap") ||
                            path.contains("/test") ||
                            // path.indexOf("/m/")!=-1 ||
                            path.contains("desktop") ||
                            path.contains("admin/ide_left.jsp") ||
                            path.contains("reportServlet") ||
                            path.contains("/yimi_userconsole") ||//过滤精灵后台配置
                            path.contains(".txt") || // 企业微信域名归属验证，如：WW_verify_***.txt
                            path.contains("wiki_export_doc") ||
                            path.contains("exam") ||
                            path.contains("macro") ||
                            path.contains("document") ||
                            path.contains("module_check") || // 模块中字段的验证，如是否邮箱、手机号、身份证等
                            path.contains("/static") ||
                            path.contains("/showImg") || // 导出至word时，图片宏控件需用到
                            path.contains("/error") ||
                            path.contains(".html") ||
                            path.contains("/mobile")
                            // path.contains("/modular")   // modular/list 接口在options预检时需放行
                    ) {
                        ;
                    } else {
                        com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("Filter error: path=" + path);
                        String url = Global.getFullRootPath(req1) + "/error.jsp?type=login&url=" + requrl;
                        ((HttpServletResponse) res).sendRedirect(url);
                        return;
                    }
                } else {
                    // 已登录
                    // 如果是流程测试员
                    String tester = (String) Privilege.getAttribute(req1, Privilege.SESSION_OA_FLOW_TESTER);
                    if (tester != null) {
                        // 如果流程测试员不是当前的登录用户，则仅允许进入flowDispose.do及flow_list_debugger.jsp
                        if (!tester.equals(privilege.getUser(req1))) {
                            if (!(path.contains("/skin") ||
                                    path.contains("error.jsp") ||
                                    path.contains("/js") ||
                                    path.contains("/inc") ||
                                    path.contains(".css") ||
                                    path.contains("images") ||
                                    path.contains("flow_disp") || // flow_dispose_ajax_att.jsp
                                    path.contains("flowDispose") ||
                                    path.contains("getFuncVal.do") ||
                                    // path.indexOf("flow_dispose_ajax_att")!=-1 || // flow_dispose_ajax_att.jsp
                                    path.contains("debugger") ||  // flow_list_debugger.jsp
                                    path.contains("macro") ||  // flow/macro/macro_user_select_win_ctl_js.jsp
                                    path.contains("flow_getfile") ||
                                    path.contains("flow_ntko_") ||
                                    path.contains("user_multi_sel") ||
                                    path.contains("flow_do") ||
                                    path.contains("form_js") ||
                                    path.contains("flow_action_modify") || // 用于选择用户
                                    path.contains("module_show") || // 查看嵌套表内容
                                    path.contains("doFormula") || // 函数
                                    path.contains("nest_") ||

                                    /*path.contains("flow_") ||
                                    path.contains("oa.jsp") ||
                                    path.contains("document") ||
                                    path.contains("sale") ||
                                    path.contains("plan") ||
                                    path.contains("user") ||
                                    path.contains("netdisk") ||
                                    path.contains("project") ||
                                    path.contains("visual") ||*/

                                    path.contains("spwhitepad") ||
                                    path.contains("bottom") ||
                                    path.contains("activex") ||
                                    path.contains("user_sel") || // 人员选择宏控件
                                    path.contains("organize_dept_sel") ||    // 宏控件
                                    path.contains("location_list_mine") ||   // 宏控件
                                    path.contains("form_query_") ||  // 宏控件
                                    path.contains("nestsheetctl/") ||
                                    path.contains("public/getfile") || // WFDesigner需用到
                                    path.contains("favicon.ico") ||
									path.contains("flow/") ||
                                    path.contains("flowShowChartPage") ||
                                    path.contains("module_list_sel") ||
                                    path.contains("/dwr/") ||
                                    path.contains("/info")
                            )) {
                                try {
                                    DebugUtil.e(getClass(), "doFilter", "tester invalid path=" + path);
                                    privilege.logout(req1, (HttpServletResponse) res);
                                } catch (ErrMsgException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                                String url = Global.getFullRootPath(req1) + "/index";
                                ((HttpServletResponse) res).sendRedirect(url);
                                return;
                            }
                        }
                    }
                }
            }

        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
        this.config = config;
    }

    @Override
    public void destroy() {
        this.config = null;
    }
}
