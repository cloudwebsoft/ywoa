package com.cloudwebsoft.framework.security;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

public class ProtectFilter implements Filter {
	  FilterConfig config;

	  public void doFilter(ServletRequest req, ServletResponse res,
	                       FilterChain chain) throws ServletException, IOException {
	    // ServletContext context = config.getServletContext();

	    if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) req;

			String url = request.getRequestURL().toString();
			if (url.indexOf("error.jsp")==-1) {
				boolean isValid = true;
				String kind = "";
				String param = "";
				String value = "";
				try {
					SecurityUtil.filter(request, url);
				} catch (ProtectXSSException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					isValid = false;
					param = e.getParam();
					value = e.getValue();
					kind = "XSS";
				} catch (ProtectSQLInjectException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					isValid = false;
					param = e.getParam();
					value = e.getValue();
					kind = "SQLInject";
				}
				
				if (!isValid) {
			    	com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("Filter error: url=" + url + " type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value));
			        String toUrl = Global.getFullRootPath(request) + "/error.jsp?type=protect&kind=" + kind + "&param=" + param + "&value=" + StrUtil.UrlEncode(value) + "&sourceUrl=" + StrUtil.UrlEncode(url);
				    ((HttpServletResponse)res).sendRedirect(toUrl);
				    return;
				}
			}
	    }

	    chain.doFilter(req, res);

	  }

	  public void init(FilterConfig config) throws ServletException {
	    this.config = config;
	  }

	  public void destroy() {
	    this.config = null;
	  }
	}
