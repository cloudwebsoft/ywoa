package com.cloudweb.oa.cache;

import com.cloudweb.oa.base.AbstractVisualFormDaoCache;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component("BaseFormDaoCache")
public class BaseVisualFormDaoCache extends AbstractVisualFormDaoCache {

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
}