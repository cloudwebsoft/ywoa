package com.cloudweb.oa.aop;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.ILogService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA
 *
 * @author weiwenjun
 * @date 2018/9/12
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    AuthUtil authUtil;

    /**
     * 此处的切点是注解的方式，也可以用包名的方式达到相同的效果
     * '@Pointcut("execution(* com.cloudweb.oa.service.impl.*.*(..))")'
     */
    @Pointcut("@annotation(com.cloudweb.oa.annotation.SysLog)")
    public void log(){}

    @Autowired
    ILogService logService;

    /**
     * 环绕增强，相当于MethodInterceptor
     */
    @Around("log()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object res = null;
        long time = System.currentTimeMillis();
        try {
            res =  joinPoint.proceed();
            time = System.currentTimeMillis() - time;
            return res;
        } finally {
            try {
                // 方法执行完成后增加日志
                addLog(joinPoint,res,time);
            }catch (Exception e){
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }

    private void addLog(JoinPoint joinPoint, Object res, long time){
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 从session里面获取对应的值
        // String str = (String) requestAttributes.getAttribute("name",RequestAttributes.SCOPE_SESSION);
        // HttpServletResponse response = ((ServletRequestAttributes)requestAttributes).getResponse();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        com.cloudweb.oa.entity.Log myLog = new com.cloudweb.oa.entity.Log();
        myLog.setDevice(ParamUtil.isMobile(request)? ConstUtil.DEVICE_MOBILE : ConstUtil.DEVICE_PC);
        myLog.setIp(StrUtil.getIp(request));
        myLog.setUserName(SpringUtil.getUserName());
        myLog.setUnitCode(authUtil.getUserUnitCode());
        myLog.setLogDate(new Date());

        SysLog annotationLog = signature.getMethod().getAnnotation(SysLog.class);
        if(annotationLog != null){
            myLog.setLevel(annotationLog.level().getLevel());
            myLog.setLogType(annotationLog.type().getType());
            myLog.setAction(replaceWithFunctionArgs(joinPoint, request, ((MethodSignature) joinPoint.getSignature()).getParameterNames(), joinPoint.getArgs(), annotationLog, annotationLog.action()));
            myLog.setRemark(replaceWithFunctionArgs(joinPoint, request, ((MethodSignature) joinPoint.getSignature()).getParameterNames(), joinPoint.getArgs(), annotationLog, annotationLog.remark()));
        }

        if (annotationLog.debug()) {
            log.info("记录日志: 等级 " + annotationLog.level() + "，" + myLog.getAction() + ", " + myLog.getRemark() + " 运行时间：" + (double)time/1000 + " 毫秒");
        }

        // 会报错：Mapped Statements collection does not contain value for insert
        // myLog.insert();
        logService.save(myLog);

    }

    /**
     * 以方法的参数值替换字符串中的${参数名}
     * @param request
     * @param argNames 方法参数名称数组
     * @param args 方法参数数组
     * @param annotation 注解信息
     * @return 返回处理后的描述
     */
    private String replaceWithFunctionArgs(JoinPoint joinPoint, HttpServletRequest request, String[] argNames, Object[] args, SysLog annotation, String str){
        Map<Object, Object> map = new HashMap<>(4);
        for(int i = 0;i < argNames.length;i++) {
            map.put(argNames[i], args[i]);
        }

        Pattern p = Pattern.compile(
                "\\$\\{([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String val = "";
            if (fieldName.startsWith("request.")) {
                String key = fieldName.substring("request.".length());
                val = ParamUtil.get(request, key);
            }
            else if (fieldName.equalsIgnoreCase("curUser")) {
                val = SpringUtil.getUserName();
            }
            else if (fieldName.equalsIgnoreCase("curDate")) {
                val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
            }
            else {
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    Object k = entry.getKey();
                    Object v = entry.getValue();
                    if (fieldName.equals(k)) {
                        if (v instanceof String[]) {
                            val = StringUtils.join((String[])v, ",");
                        }
                        else if (v instanceof JSONObject) {
                            val = JSON.toJSONString(v);
                        }
                        else {
                            if (v!=null) {
                                val = v.toString();
                            }
                            else {
                                MethodSignature signature = (MethodSignature)joinPoint.getSignature();
                                log.error(signature.getMethod().getName() + "中的参数 " + k + " is null");
                                val = "";
                            }
                        }
                    }
                }
            }
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);
        return sb.toString();
    }

//    @Before("log()")
//    public void doBeforeAdvice(JoinPoint joinPoint){
//        LogUtil.getLog(getClass()).info("进入方法前执行.....");
//    }
//
//    *
//     * 处理完请求，返回内容
//     * @param ret
//
//    @AfterReturning(returning = "ret", pointcut = "log()")
//    public void doAfterReturning(Object ret) {
//        LogUtil.getLog(getClass()).info("方法的返回值 : " + ret);
//    }
//
//    *
//     * 后置异常通知
//
//    @AfterThrowing("log()")
//    public void throwing(JoinPoint jp){
//        LogUtil.getLog(getClass()).info("方法异常时执行.....");
//    }
//
//
//    *
//     * 后置最终通知,final增强，不管是抛出异常或者正常退出都会执行
//
//    @After("log()")
//    public void after(JoinPoint jp){
//        LogUtil.getLog(getClass()).info("方法最后执行.....");
//    }

}