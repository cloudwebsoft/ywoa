package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.PortalService;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.stereotype.Service;

import java.util.Vector;

@Service
public class PortalServiceImpl implements PortalService {
    @Override
    public JSONArray getChartTypes(String chartType) {
        JSONArray arr = new JSONArray();
        String sql = "select id,title from ft_config_chart where chart_type=" + StrUtil.sqlstr(chartType);
        FormDAO fdao = new FormDAO();
        Vector<FormDAO> vtChart;
        try {
            vtChart = fdao.list("config_chart", sql);
            for (FormDAO fdaoChart : vtChart) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", fdaoChart.getId());
                jsonObject.put("title", fdaoChart.getFieldValue("title"));
                arr.add(jsonObject);
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return arr;
    }
}
