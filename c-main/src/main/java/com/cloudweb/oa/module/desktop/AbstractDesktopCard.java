package com.cloudweb.oa.module.desktop;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IDesktopCard;
import com.cloudweb.oa.api.IFormulaUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.Formula;
import com.redmoon.oa.visual.FormulaResult;

import javax.servlet.http.HttpServletRequest;

public class AbstractDesktopCard implements IDesktopCard {
    DesktopCard desktopCard;

    @Override
    public long getId() {
        return desktopCard.getId();
    }

    @Override
    public String getName() {
        return desktopCard.getName();
    }

    @Override
    public String getTitle() {
        return desktopCard.getTitle();
    }

    @Override
    public int getStartVal() {
        return desktopCard.getStartVal();
    }

    @Override
    public int getEndVal(HttpServletRequest request) {
        return 0;
    }

    @Override
    public boolean isLink() {
        return desktopCard.isLink();
    }

    @Override
    public String getUrl() {
        return desktopCard.getUrl();
    }

    @Override
    public String getUnit() {
        return desktopCard.getUnit();
    }

    @Override
    public String getBgColor() {
        return desktopCard.getBgColor();
    }

    @Override
    public String getIcon() {
        return desktopCard.getIcon();
    }

    @Override
    public JSONObject getQuery() {
        return new JSONObject();
    }

    @Override
    public int getEndValByFunc(DesktopCard desktopCard) {
        long formulaId = StrUtil.toLong(desktopCard.getEndValfunc(), -1);
        IFormulaUtil formulaUtil = SpringUtil.getBean(IFormulaUtil.class);
        Formula formula = formulaUtil.getFormulaById(formulaId);
        String formulaStr = "#" + formula.getCode() + "()";
        int val = 0;
        try {
            FormulaResult fr = formulaUtil.render(formulaStr);
            val = fr.getIntVal();
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return val;
    }
}
