package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public interface IModuleUtil {

    List<String> parseFieldNameInFilter(HttpServletRequest request, String formCode, String filter);

    String[] parseFilter(HttpServletRequest request, String formCode, String filter);

    String parseConds(HttpServletRequest request, IFormDAO ifdao, String conds);

    String doGetViewJS(HttpServletRequest request, FormDb fd, IFormDAO fdao, String userName, boolean isForReport);

    String doGetViewJSMobile(HttpServletRequest request, FormDb fd,  com.redmoon.oa.visual.FormDAO fdao, String userName, boolean isForReport);

    boolean evalCheckSetupRule(HttpServletRequest request, String userName, JSONArray ary, IFormDAO fdao, List filedList, FileUpload fu) throws JSONException, ErrMsgException;

    com.alibaba.fastjson.JSONArray getConditions(HttpServletRequest request, ModuleSetupDb msd, ArrayList<String> dateFieldNamelist);

}
