package com.cloudweb.oa.filter;

import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.sys.DebugUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨域过滤器，在FilterConfig中配置允许的url路径
 * @author
 * @since
 */
@Slf4j
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 不使用*的话，可以自动适配跨域域名，避免携带Cookie时失效
        String origin = request.getHeader("Origin");
        // DebugUtil.i(getClass(), "doFilter", ((HttpServletRequest) req).getRequestURI() + " origin=" + origin);
        /*if(StringUtils.isNotBlank(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        else {*/
            // 允许全部
            response.setHeader("Access-Control-Allow-Origin", "*");
        // }

        // 自适应所有自定义头
        String headers = request.getHeader("Access-Control-Request-Headers");
        // getAppIcon时，取得的headers为null，因为前端没有给header，而list接口取得的则是Authorization，因为后端给了该header属性
        // 故注释掉，统一走else中的逻辑
        /*if(StringUtils.isNotBlank(headers)) {
            response.setHeader("Access-Control-Allow-Headers", headers);
            response.setHeader("Access-Control-Expose-Headers", headers);
        }
        else {*/
            JwtProperties jwtProperties = SpringUtil.getBean(JwtProperties.class);
            // 响应头设置，允许客户端发送请求头jwt
            response.setHeader("Access-Control-Allow-Headers", "Origin,X-Requested-With,X_Requested_With,Content-Type,Accept," + ConstUtil.SKEY + "," + ConstUtil.CUR_ROLE_CODE + "," + ConstUtil.CUR_DEPT_CODE + "," + jwtProperties.getHeader());
            // 允许客户端跨域允许访问的响应头的内容，因为跨域访问的时候，不能随意获取服务器响应头
            response.setHeader("Access-Control-Expose-Headers", ConstUtil.SKEY + "," + ConstUtil.CUR_ROLE_CODE + "," + ConstUtil.CUR_DEPT_CODE + "," + jwtProperties.getHeader());
        // }

        // 允许跨域的请求方法类型
        response.setHeader("Access-Control-Allow-Methods", "*");
        // 响应类型
        // response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, DELETE");

        // 浏览检测到请求跨域时，会自动发起预检请求，以检测实际请求是否可以被服务器所接受
        // 预检有效保持时间，单位：秒，指定本次预检请求的有效期，在此期间不用发出另一条预检请求
        response.setHeader("Access-Control-Max-Age", "3600");

        // 允许跨域带上cookies
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 判断此次是否是预检请求，如果是，立即返回一个204状态吗，标示允许跨域
        if ("OPTIONS".equals(request.getMethod())) {
            // SC_NO_CONTENT 204 表示无内容。服务器成功处理，但未返回内容。在未更新网页的情况下，可确保浏览器继续显示当前文档
            response.setStatus(HttpStatus.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}