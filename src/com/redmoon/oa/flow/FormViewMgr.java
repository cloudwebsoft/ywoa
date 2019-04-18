package com.redmoon.oa.flow;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;

public class FormViewMgr {

    public synchronized boolean create(HttpServletRequest request) throws
    		ErrMsgException, ResKeyException {
		// 许可性验证
		License.getInstance().validate(request);
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		
		String name = ParamUtil.get(request, "name");
		String formCode = ParamUtil.get(request, "formCode");
		String content = ParamUtil.get(request, "content");
		boolean hasAttachment = ParamUtil.getInt(request, "hasAttachment")==1;
		
		String ieVersion = ParamUtil.get(request, "ieVersion");
		int kind = ParamUtil.getInt(request, "kind");
		
		FormParser fp = new FormParser();
		String form = fp.generateView(content, ieVersion, formCode);
		
		FormViewDb fvd = new FormViewDb();
		return fvd.create(new JdbcTemplate(), new Object[]{formCode,name,content,new Integer(kind), new Integer(hasAttachment?1:0),userName,new java.util.Date(),ieVersion, form});
	}
    
    public synchronized boolean modify(HttpServletRequest request) throws
		ErrMsgException, ResKeyException {
		// 许可性验证
		License.getInstance().validate(request);
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		
		String name = ParamUtil.get(request, "name");
		String content = ParamUtil.get(request, "content");
		boolean hasAttachment = ParamUtil.getInt(request, "hasAttachment")==1;
		
		String ieVersion = ParamUtil.get(request, "ieVersion");
		int kind = ParamUtil.getInt(request, "kind");
		
		int id = ParamUtil.getInt(request, "id");
		FormViewDb fvd = new FormViewDb();
		fvd = fvd.getFormViewDb(id);
		
		FormParser fp = new FormParser();
		String form = fp.generateView(content, ieVersion, fvd.getString("form_code"));
		
		fvd.set("name", name);
		fvd.set("content", content);
		fvd.set("kind", new Integer(kind));
		fvd.set("has_attachment", new Integer(hasAttachment?1:0));
		fvd.set("user_name", userName);
		fvd.set("modify_date", new java.util.Date());
		fvd.set("ie_version", ieVersion);
		fvd.set("form", form);
		
		return fvd.save();
	}    
    
    public synchronized boolean del(HttpServletRequest request) throws
            ErrMsgException, ResKeyException {
    	int id = ParamUtil.getInt(request, "id");

        FormViewDb fvd = new FormViewDb();
        fvd = fvd.getFormViewDb(id);

        return fvd.del();
    }    
}
