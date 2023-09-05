package com.cloudweb.oa.utils;

import com.cloudwebsoft.framework.util.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author
 * @create 2019/1/13 14:37
 * @desc
 **/
public class JsonUtil {

    public static String beanToStr(Object bean){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            LogUtil.getLog(JsonUtil.class).error(e);
        }
        return "";
    }
}
