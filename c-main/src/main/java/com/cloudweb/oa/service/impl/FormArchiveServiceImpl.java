package com.cloudweb.oa.service.impl;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.cache.FormArchiveCache;
import com.cloudweb.oa.service.FormArchiveService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

@Service
public class FormArchiveServiceImpl implements FormArchiveService {

    @Autowired
    private FormArchiveCache formArchiveCache;

    @Override
    public IFormDAO create(String userName, String formCode, String content) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(ConstUtil.FORM_ARCHIVE);
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("form_code", formCode);
        fdao.setFieldValue("content", content);
        fdao.setFieldValue("create_time", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        fdao.setFieldValue("user_name", userName);
        fdao.setCreator(userName);
        fdao.setUnitCode(ConstUtil.DEPT_ROOT);
        boolean re = false;
        try {
            re = fdao.create();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (re) {
            formArchiveCache.refreshCreate(formCode);
            return fdao;
        }
        else {
            return null;
        }
    }

    /**
     * 取得当前form_archive记录，排在第一位的
     * @param formCode
     * @return
     */
    @Override
    public IFormDAO getCurFormArchiveRaw(String formCode) {
        String sql = "select id from form_table_" + ConstUtil.FORM_ARCHIVE + " where form_code=" + StrUtil.sqlstr(formCode) + " order by id desc";
        FormDAO fdao = new FormDAO();
        try {
            ListResult listResult = fdao.listResult(ConstUtil.FORM_ARCHIVE, sql, 1, 1);
            Iterator ir = listResult.getResult().iterator();
            if (ir.hasNext()) {
                return (FormDAO)ir.next();
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IFormDAO getCurFormArchive(String formCode) {
        return formArchiveCache.getCurFormArchive(formCode);
    }

    @Override
    public IFormDAO getCurFormArchiveOrInit(String formCode) {
        IFormDAO fdao = formArchiveCache.getCurFormArchive(formCode);
        if (fdao == null) {
            FormDb formDb = new FormDb();
            formDb = formDb.getFormDb(formCode);
            return create(ConstUtil.USER_SYSTEM, formCode, formDb.getContent());
        }
        else {
            return fdao;
        }
    }

    /**
     * 判断是否在流程中已用过此表单归档记录
     * @param formArchiveId
     * @return
     */
    @Override
    public boolean isUsedByFlow(long formArchiveId) {
        WorkflowDb wf = new WorkflowDb();
        return wf.isFormArchiveUsed(formArchiveId);
    }

    /**
     * 当表单更新时，处理表单归档记录
     * @param formDb
     * @throws ErrMsgException
     */
    @Override
    public void onFormUpdate(FormDb formDb) throws ErrMsgException {
        // 如果最近一条form_archive记录尚未被使用，则置其内容等于此次编辑的表单内容，如果已被使用，且内容不一样，则在form_archive中新增记录
        IFormDAO fdao = getCurFormArchive(formDb.getCode());
        if (fdao == null) {
            create(SpringUtil.getUserName(), formDb.getCode(), formDb.getContent());
        }
        else if (!isUsedByFlow(fdao.getId())) {
            fdao.setFieldValue("content", formDb.getContent());
            fdao.save();
            formArchiveCache.refreshAll(formDb.getCode());
        }
        else {
            if (!fdao.getFieldValue("content").equals(formDb.getContent())) {
                create(SpringUtil.getUserName(), formDb.getCode(), formDb.getContent());
            }
        }
    }
}
