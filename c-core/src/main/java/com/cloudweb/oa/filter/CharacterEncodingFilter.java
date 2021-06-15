package com.cloudweb.oa.filter;

import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@WebFilter(urlPatterns="/*",filterName="CharacterEncodingFilter")
@Order(1)
public class CharacterEncodingFilter implements Filter {

    private int mainVersion;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 获取服务器信息，判断是不是Tomcat8以上的版本
        String serverInfo = filterConfig.getServletContext().getServerInfo();
        // 两种版本的serverInfo示例：Apache Tomcat/7.0.69 、Apache Tomcat/8.0.36
        if (serverInfo.startsWith("Apache Tomcat")) {
            // 获取Tomcat主版本
            mainVersion = Integer.parseInt(serverInfo.substring(14, 15));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (mainVersion >= 8) {
            // 注意过滤仅对post数据生效
            // 处理请求体中数据的编码（查询字符串已经）
            request.setCharacterEncoding("UTF-8");
            // 也顺便对响应编码进行处理
            // response.setCharacterEncoding("UTF-8");
            chain.doFilter(request, response);
        } else {
/*            // 低于8的版本不作处理，以免post时是UTF-8，而get时却是ISO-8859-1
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            // 使用自己的request对象包装原request对象，实现对请求参数编码处理的效果
            MyHttpServletRequest myRequest = new MyHttpServletRequest(httpRequest);
            // 也顺便对响应编码进行处理
            // response.setCharacterEncoding("UTF-8");
            chain.doFilter(myRequest, response);*/
        }
    }

    @Override
    public void destroy() {

    }
}

// 自己包装的request对象，保证所有的获取参数操作都会进行编码处理
class MyHttpServletRequest extends HttpServletRequestWrapper {

    public MyHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        value = convertToUtf_8(value);

        return value;
    }

    private String convertToUtf_8(String oldValue) {
        if (oldValue != null && oldValue.length() > 0) {
            try {
                oldValue = new String(oldValue.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return oldValue;
    }

    private String[] convertToUtf_8(String[] values) {

        if (values == null || values.length == 0) {
            return values;
        }
        String[] newValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            newValues[i] = convertToUtf_8(values[i]);
        }

        return newValues;
    }

    // Map<String,String[]>
    @Override
    public Map getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        Map<String, String[]> newMap = new HashMap<String, String[]>();
        for (Entry<String, String[]> entry : map.entrySet()) {
            newMap.put(convertToUtf_8(entry.getKey()), convertToUtf_8(entry.getValue()));
        }
        return newMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        return convertToUtf_8(super.getParameterValues(name));
    }

    @Override
    public Enumeration getParameterNames() {
        final Enumeration oldEnum = super.getParameterNames();
        Enumeration<String> newEnum = new Enumeration<String>() {

            @Override
            public String nextElement() {
                return convertToUtf_8((String) oldEnum.nextElement());
            }

            @Override
            public boolean hasMoreElements() {
                return oldEnum.hasMoreElements();
            }
        };

        return newEnum;
    }
}