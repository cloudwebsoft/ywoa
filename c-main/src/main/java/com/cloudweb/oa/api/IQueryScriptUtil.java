package com.cloudweb.oa.api;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormQueryDb;
import com.redmoon.oa.visual.FormDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public interface IQueryScriptUtil {
    String getSqlExpressionReplacedWithFieldValue(String sql, Map mapCondValue, Map mapCondType);

    String getSqlExpressionReplacedWithFieldValue(String sql, FormDAO moduleFdao, JSONObject jsonTabSetup);

    String getSqlExpressionReplacedWithFieldValue(String sql, FormDb parentFormDb, JSONArray mapsCond, HashMap mapCondValue);

    String getSqlOrderByReplaced(String sql, String orderBy, String sort);

    String getSqlSearchInResult(HttpServletRequest request, FormQueryDb fqd, String sql);
}
