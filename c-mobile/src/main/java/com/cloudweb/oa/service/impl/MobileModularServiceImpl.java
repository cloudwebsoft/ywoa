package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.cloudweb.oa.service.MobileModularService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
public class MobileModularServiceImpl implements MobileModularService {

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public String create(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String skey = ParamUtil.get(request,"skey");
        com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
        String userName = pri.getUserName(skey);
        if("".equals(userName)){
            json.put("res", "-1");
            json.put("msg", "skey不存在！");
            return json.toString();
        }
        pri.doLogin(request,skey);

        String moduleCode = ParamUtil.get(request, "moduleCode");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);
        String formCode = msd.getString("form_code");
        try {
            FormMgr fm = new FormMgr();
            FormDb fdAdd = fm.getFormDb(formCode);
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdAdd);
            boolean re = fdm.create(SpringUtil.getServletContext(), request);
            if(re){
                json.put("res","0");
                json.put("msg","操作成功");
            }else{
                json.put("res","-1");
                json.put("msg","操作失败");
            }
        }catch (ErrMsgException e) {
            json.put("res","-1");
            json.put("msg",e.getMessage());
        }
        return json.toString();
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public String update(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String skey = ParamUtil.get(request, "skey");
        com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
        String userName = pri.getUserName(skey);
        if("".equals(userName)){
            json.put("res", "-1");
            json.put("msg", "skey不存在！");
            return json.toString();
        }
        pri.doLogin(request,skey);
        String moduleCode = ParamUtil.get(request, "moduleCode");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(moduleCode);
        String formCode = msd.getString("form_code");
        long id = ParamUtil.getLong(request, "id", -1);
        try {
            FormMgr fm = new FormMgr();
            FormDb fdEdit = fm.getFormDb(formCode);
            // 置嵌套表需要用到的cwsId
            request.setAttribute("cwsId", "" + id);
            // 置嵌套表需要用到的pageType
            request.setAttribute("pageType", "edit");
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdEdit);
            boolean re = fdm.update(SpringUtil.getServletContext(), request);
            if(re){
                json.put("res","0");
                json.put("msg","操作成功！");
            }else{
                json.put("res","-1");
                json.put("msg","操作失败！");
            }
        }catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            json.put("res","-1");
            json.put("msg",e.getMessage());
        }
        return json.toString();
    }
}
