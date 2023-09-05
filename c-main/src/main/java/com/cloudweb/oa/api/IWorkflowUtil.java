package com.cloudweb.oa.api;

import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowDb;
import org.jdom.Element;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IWorkflowUtil {

    String doGetViewJSMobile(HttpServletRequest request, FormDb fd, FormDAO fdao, String userName, boolean isForReport);

    void writeBack(WorkflowDb wf, Leaf lf, Element nodeFinish) throws SQLException, JSONException;

    void writeBackDb(WorkflowDb wf, Leaf lf, Element nodeFinish) throws SQLException;

    String makeViewJSMobile(FormDb fd, com.alibaba.fastjson.JSONArray ifArr, com.alibaba.fastjson.JSONObject json, Map<String, List<String>> fieldChangeFuncMap, int k) throws JSONException;

    String makeViewBindChangeEvent(FormDb fd, Map<String, List<String>> fieldChangeFuncMap);

    String makeViewBindChangeEventMobile(FormDb fd, Map<String, List<String>> fieldChangeFuncMap);

}
