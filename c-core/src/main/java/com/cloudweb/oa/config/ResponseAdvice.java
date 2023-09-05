package com.cloudweb.oa.config;

import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.annotation.IgnoreResponseAnnotation;
import com.cloudweb.oa.pojo.ErrorResponseEntity;
import com.cloudweb.oa.utils.JsonUtil;
import com.cloudweb.oa.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;

/**
 * 必须要加basePackages，否则可能会影响swagger
 */
@RestControllerAdvice(basePackages = "com.cloudweb.oa")
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        /*
        // 通过判断返回参数类型为String，已改为通过判断o instanceof String
        if (methodParameter.getParameterType().equals(String.class)) {
            Result<Object> result = new Result<>(true);
            result.setData(o);
            return JsonUtil.beanToStr(result);
        }*/

        // 如果是void类型的返回值
        if (StringUtils.equals(methodParameter.getParameterType().getName(), "void") && mediaType.equals(MediaType.APPLICATION_JSON)) {
            return new Result<>(true);
        }

        if (o instanceof Result) {
            return o;
        } else if (o instanceof ErrorResponseEntity) {
            return o;
        } else if (o instanceof String) {
            /*Result<Object> result = new Result<>(true);
            result.setData(o);
            return JsonUtil.beanToStr(result);*/
            // 对String型不作处理，因为原有很多写法会返回String
            // 对String类型的返回值, spring会默认加载StringHttpMessageConverter解析器, 并使用text/html媒体格式
            return o;
        } else {
            if (o instanceof JSONObject) {
                // 是通过ResponseUtil.getResultJson返回的
                return o;
            }
            return new Result<>(o);
        }
    }

    @Override
    public boolean supports(MethodParameter paramMethodParameter, Class<? extends HttpMessageConverter<?>> paramClass) {
        boolean isIntercept = true;

        // 示例：不拦截test方法
        /*Method method = paramMethodParameter.getMethod();
        if (method.getReturnType().isAssignableFrom(String.class)
                && method.getName().startsWith("test")) {
            isIntercept = false;
        }*/

        // 示例：不拦截 @IgnoreResponseAnnotation 注解的接口
        AnnotatedElement annotatedElement = paramMethodParameter.getAnnotatedElement();
        IgnoreResponseAnnotation responseAnnotation = AnnotationUtils.findAnnotation(annotatedElement, IgnoreResponseAnnotation.class);
        if (responseAnnotation != null) {
            isIntercept = false;
        }

        // 示例：不拦截指定方法
        /*if ("queryTest".equals(method.getName())) {
            isIntercept = false;
        }*/
        return isIntercept;
    }

}
