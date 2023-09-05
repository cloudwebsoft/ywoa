package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.visual.Formula;
import com.redmoon.oa.visual.FormulaResult;

public interface IFormulaUtil {

    FormulaResult render(String formula) throws ErrMsgException;

    Formula getFormula(JdbcTemplate jt, String formulaCode) throws ErrMsgException;

    Formula getFormulaById(long id) throws ErrMsgException;
}
