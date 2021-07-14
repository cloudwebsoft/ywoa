package com.cloudweb.oa.api;

import com.redmoon.oa.base.IFormDAO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IModuleUtil {

    List<String> parseFieldNameInFilter(HttpServletRequest request, String formCode, String filter);

    String[] parseFilter(HttpServletRequest request, String formCode, String filter);

    String parseConds(HttpServletRequest request, IFormDAO ifdao, String conds);
}
