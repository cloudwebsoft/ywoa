package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import com.cloudweb.oa.base.AbstractVisualFormDaoCache;
import com.cloudweb.oa.service.FormArchiveService;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class FormArchiveCache extends AbstractVisualFormDaoCache {

    @Autowired
    FormArchiveService formArchiveService;

    private static final Lock lock = new ReentrantLock();

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getEmptyFlag(IFormDAO obj) {
        return obj.getFlowTypeCode();
    }

    @Override
    public IFormDAO getEmptyFormDao(String formCode, long id, String emptyFlag) {
        FormDAO formDAO = new FormDAO();
        formDAO.setFlowTypeCode(emptyFlag);
        return formDAO;
    }

    @Override
    public IFormDAO getFormDaoRaw(String formCode, long id) {
        FormDb formDb = new FormDb();
        formDb = formDb.getFormDb(formCode);
        FormDAO fdao = new FormDAO();
        return fdao.getFormDAO(id, formDb);
    }

    public IFormDAO getCurFormArchive(String formCode) {
        try {
            IFormDAO formDAO = (IFormDAO) RMCache.getInstance().getFromGroup(formCode, getGroup(formCode));
            if (formDAO == null) {
                // 当formCode非法时，可能会被穿透，但考虑到本类仅用于FormDb更新时，故可能性不大
                formDAO = formArchiveService.getCurFormArchiveRaw(formCode);
                if (formDAO == null) {
                    return null;
                }
                else {
                    RMCache.getInstance().putInGroup(formCode, getGroup(formCode), formDAO);
                    return formDAO;
                }
            }
            else {
                return formDAO;
            }
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
            return null;
        }
    }
}
