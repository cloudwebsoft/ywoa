package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.cache.FormArchiveCache;
import com.cloudweb.oa.service.FormArchiveService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author fgf
 */
public class FormMgr {
    public FormMgr() {
    }

    public FormDb getFormDb(String code) {
        FormDb ftd = new FormDb();
        return ftd.getFormDb(code);
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        FormForm fc = new FormForm();
        fc.checkMove(request);

        FormDb ft = getFormDb(fc.getFormDb().getCode());
        return ft.move(fc.getDirection());
    }

    public synchronized boolean create(HttpServletRequest request) throws
            ErrMsgException {
        // 许可证验证
        License.getInstance().validate(request);

        FormForm fc = new FormForm();
        fc.checkCreate(request);

        String ieVersion = ParamUtil.get(request, "ieVersion");

        FormDb ft = fc.getFormDb();
        ft.setIeVersion(ieVersion);

        String fieldsAry = ParamUtil.get(request, "fieldsAry");
        FormParser fp = new FormParser();
        try {
            JSONArray ary = new JSONArray(fieldsAry);
            fp.getFields(ary);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }
        ft.setFormParser(fp);

        boolean re = ft.create();
        if (re) {
            FormArchiveService formArchiveService = SpringUtil.getBean(FormArchiveService.class);
            formArchiveService.create(SpringUtil.getUserName(), ft.getCode(), ft.getContent());

            // 生成模块
            ModuleSetupDb msd = new ModuleSetupDb();
            msd.getModuleSetupDbOrInit(ft.getCode());
        }
        return re;
    }

    public synchronized boolean del(String code) throws ErrMsgException {
        Leaf lf = new Leaf();
        for (Object o : lf.getLeavesUseForm(code)) {
            lf = (Leaf) o;
            WorkflowDb wfd = new WorkflowDb();
            int count = wfd.getWorkflowCountOfType(lf.getCode());
            if (count > 0) {
                throw new ErrMsgException("流程 " + lf.getName() + " 中已有流程 " + count + " 个，表单不能被删除！");
            }
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(code);
        boolean re = fd.del();
        if (re) {
            // 记录日志
            HttpServletRequest request = SpringUtil.getRequest();
            com.redmoon.oa.LogUtil.log(SpringUtil.getUserName(), StrUtil.getIp(request), LogDb.TYPE_ACTION, "FORM_DEL " + fd.getName() + ":" + fd.getCode());

            FormArchiveCache formArchiveCache = SpringUtil.getBean(FormArchiveCache.class);
            formArchiveCache.refreshAll(code);
        }
        return re;
    }

    public synchronized boolean modify(HttpServletRequest request) throws ErrMsgException {
        // 许可性验证
        License.getInstance().validate(request);

        FormForm fc = new FormForm();
        fc.checkModify(request);

        FormDb ft = fc.getFormDb();

        FormDb ftd = getFormDb(ft.getCode());
        ftd.setName(ft.getName());
        ftd.setOldContent(ftd.getContent());
        ftd.setContent(ft.getContent());
        ftd.setFlowTypeCode(ft.getFlowTypeCode());
        ftd.setHasAttachment(ft.isHasAttachment());

        String ieVersion = ParamUtil.get(request, "ieVersion");
        ftd.setIeVersion(ieVersion);
        ftd.setLog(ft.isLog());
        ftd.setProgress(ft.isProgress());

        ftd.setUnitCode(ft.getUnitCode());
        ftd.setOnlyCamera(ft.isOnlyCamera());
        ftd.setFlow(ft.isFlow());

        String fieldsAry = ParamUtil.get(request, "fieldsAry");
        FormParser fp = new FormParser();
        try {
            JSONArray ary = new JSONArray(fieldsAry);
            fp.getFields(ary);
        } catch (JSONException e) {
            DebugUtil.i(getClass(), "modify", fieldsAry);
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }
        ftd.setFormParser(fp);

        boolean re = ftd.save();
        if (re) {
            // 记录日志
            com.redmoon.oa.LogUtil.log(SpringUtil.getUserName(), StrUtil.getIp(request), LogDb.TYPE_ACTION, "FORM_EDIT " + ftd.getName() + ":" + ftd.getCode());

            // 当表单更新时，处理表单归档记录
            FormArchiveService formArchiveService = SpringUtil.getBean(FormArchiveService.class);
            formArchiveService.onFormUpdate(ftd);

            // 重新生成视图
            re = regenerateFormView(ft.getCode());
        }
        return re;
    }

    public boolean regenerateFormView(String formCode) {
        // 重新生成视图
        boolean re = true;
        FormParser fp = new FormParser();
        FormViewDb fvd = new FormViewDb();
        for (Object o : fvd.getViews(formCode)) {
            fvd = (FormViewDb) o;
            String form = fp.generateView(fvd.getString("content"), fvd.getString("ie_version"), formCode);
            fvd.set("form", form);
            try {
                re = fvd.save();
            } catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return re;
    }
}
