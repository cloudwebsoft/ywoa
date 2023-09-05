package com.cloudweb.oa.cache;

import com.cloudweb.oa.base.AbstractFlowFormDaoCache;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component("BaseFlowFormDaoCache")
public class BaseFlowFormDaoCache extends AbstractFlowFormDaoCache {

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
    public IFormDAO getEmptyFormDao(int flowId, String formCode, String emptyFlag) {
        FormDAO formDAO = new FormDAO();
        formDAO.setFlowTypeCode(emptyFlag);
        return formDAO;
    }

    @Override
    public IFormDAO getFormDaoRaw(int flowId, String formCode) {
        FormDb formDb = new FormDb();
        formDb = formDb.getFormDb(formCode);
        FormDAO fdao = new FormDAO();
        return fdao.getFormDAO(flowId, formDb);
    }
}