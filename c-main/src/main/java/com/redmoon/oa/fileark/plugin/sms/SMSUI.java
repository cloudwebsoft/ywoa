package com.redmoon.oa.fileark.plugin.sms;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.base.ISkin;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.fileark.plugin.base.IPluginViewAddDocument;
import com.redmoon.oa.fileark.plugin.base.IPluginRender;
import com.redmoon.oa.fileark.plugin.base.IPluginViewEditDocument;
import com.redmoon.oa.fileark.Document;

public class SMSUI implements IPluginUI {
    HttpServletRequest request;

    public SMSUI(HttpServletRequest request) {
        this.request = request;
    }

    public IPluginViewAddDocument getViewAddDocument(String dirCode) {
        return new SMSViewAddDocument(request, dirCode);
    }

    public IPluginViewEditDocument getViewEditDocument(Document doc) {
        return new SMSViewEditDocument(request, doc);
    }

    public ISkin getSkin() {
        return new SMSSkin();
    }

    public String getViewPage() {
        return "";
    }

    public IPluginRender getRender() {
        return new SMSRender();
    }
    
    public String getListPage() {
    	return "";
    }

}
