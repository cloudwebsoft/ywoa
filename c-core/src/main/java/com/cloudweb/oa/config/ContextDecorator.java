package com.cloudweb.oa.config;

import org.springframework.core.task.TaskDecorator;

import org.springframework.web.context.request.RequestAttributes;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;


public class ContextDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // AsyncContext complete前request是不会被回收的
        AsyncContext asyncContext = null;
        boolean isStartAsync = false;
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 如果是在ModuleController中调用exportExcel，Calable已经使request是异步的了，此处就不能再调用startAsync
            if (!request.isAsyncStarted()) {
                asyncContext = request.startAsync();
                isStartAsync = true;
            }
            // 设置异步处理超时时间为1秒，默认为30秒
            // asyncContext.setTimeout(1000);
        }
        AsyncContext finalAsyncContext = asyncContext;
        boolean finalIsStartAsync = isStartAsync;
        return () -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes, true);
                runnable.run();
            } finally {
                RequestContextHolder.resetRequestAttributes();
                if (finalIsStartAsync) {
                    if (finalAsyncContext != null) {
                        finalAsyncContext.complete();
                    }
                }
            }
        };
    }

}