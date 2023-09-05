package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.cond.CondUnit;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public interface ICondUtil {

    CondUnit getConditonUnit(HttpServletRequest request, ModuleSetupDb msd, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list) throws ErrMsgException;

    Object[] getFieldTitle(FormDb fd, String fieldName, String fieldTitle);
}
