package com.redmoon.oa.pvg;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.base.AbstractCheck;

public class PrivCheck  extends AbstractCheck {
    String priv = "", desc = "";
    int layer;

    public PrivCheck() {
    }

    public String getPriv() {
        return priv;
    }

    public String getDesc() {
        return desc;
    }
    
    public int getLayer() {
    	return layer;
    }

    public String chkPriv(HttpServletRequest request) {
        priv = ParamUtil.get(request, "priv");
        if (priv.equals("")) {
            log("编码必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(priv))
            log("请勿使用' ; 等字符！");
        if (!priv.equals("")) {
            PrivDb pd = new PrivDb();
            pd = pd.getPrivDb(priv);
            if (pd.isLoaded()) {
                log("编码已存在！");
            }
        }
        return priv;
    }

    public String chkDesc(HttpServletRequest request) {
        desc = ParamUtil.get(request, "desc");
        if (desc.equals("")) {
            log("描述必须填写！");
        }

        return desc;
    }
    
    public int chkLayer(HttpServletRequest request) {
        layer = ParamUtil.getInt(request, "layer", 1);

        return layer;
    }    

    public boolean checkAdd(HttpServletRequest request) throws ErrMsgException {
        init();
        chkPriv(request);
        chkDesc(request);
        chkLayer(request);
        report();
        return true;
    }

    public boolean checkUpdate(HttpServletRequest request) throws ErrMsgException {
        init();

        priv = ParamUtil.get(request, "priv");
        if (priv.equals("")) {
            log("编码必须填写！");
        }

        chkDesc(request);
        chkLayer(request);
        
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        priv = ParamUtil.get(request, "priv");
        report();
        return true;
    }
}
