package com.redmoon.forum.security.flood;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.cloudwebsoft.framework.util.IPUtil;
import com.redmoon.forum.security.flood.FloodMonitor;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

public class FloodFilter implements Filter {
	FilterConfig config;

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws ServletException, IOException {
		if (req instanceof HttpServletRequest) {
			if (FloodConfig.getInstance().getBooleanProperty("flood.isUsed")) {
				HttpServletRequest request = (HttpServletRequest) req;
				String uri = request.getRequestURI();

				// 经测试，伪静态页面Filter过滤不到，并且服务器端forward的页面也过滤不到
				// 但Servlet2.4以上可以通过在filter-mapping中设置<dispatcher>forward</dispatcher>来过滤
				// uri.endsWith("/")是为了捕获http://***/forum/即论坛首页
				if (uri.endsWith(".jsp") || uri.endsWith("/")) {
					if (uri.indexOf("admin") == -1) {
						// System.out.println(getClass() + " uri2=" + uri);
						String clientIP = IPUtil.getRemoteAddr(request);
						FloodMonitor.increaseCount(
								new Integer(FloodMonitor.FLOOD_HTTP_REQUEST), clientIP, uri);
						if (FloodMonitor.isReachMax(
								new Integer(FloodMonitor.FLOOD_HTTP_REQUEST), IPUtil
										.getRemoteAddr(request))) {
							String url = Global.getRootPath()
									+ "/info.jsp?info=Too many actions of ip("
									+ clientIP + ") per hour";
							/*
							 * // 以下方式,会使得重定向后,如果链接的相对路径不在同一级,将会致图片或CSS文件找不到
							 * RequestDispatcher rd =
							 * req.getRequestDispatcher(url); rd.forward(req,
							 * res);
							 */
							// 记录IP及其访问地址至数据库，并通过短消息或Email报警
							
							((HttpServletResponse) res).sendRedirect(url);
							return;
						}
					}
				}
			}

			chain.doFilter(req, res);
		}

	}

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	public void destroy() {
		this.config = null;
	}
}
