package cn.js.fan.util;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author bluewind
 * @version 1.0
 */
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;
import javax.servlet.UnavailableException;

public class SetCharacterEncodingFilter
    implements javax.servlet.Filter {

  protected String encoding = "GBK";

  protected FilterConfig filterConfig = null;

  public void setFilterConfig(FilterConfig config) {
    this.filterConfig = config;
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  public void destroy() {

    this.encoding = null;
    this.filterConfig = null;

  }

  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain) throws IOException, ServletException {

// Select and set (if needed) the character encoding to be used
    String encoding = selectEncoding(request);
    if (encoding != null) {
      request.setCharacterEncoding(encoding);
    }

// Pass control on to the next filter
    chain.doFilter(request, response);

  }

  public void init(FilterConfig filterConfig) throws ServletException {

    this.filterConfig = filterConfig;
    this.encoding = filterConfig.getInitParameter("encoding");
  }

  protected String selectEncoding(ServletRequest request) {

    return (this.encoding);
  }

}
