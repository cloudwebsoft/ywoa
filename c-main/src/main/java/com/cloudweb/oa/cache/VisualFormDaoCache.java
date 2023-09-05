package com.cloudweb.oa.cache;

import com.redmoon.oa.base.IFormDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VisualFormDaoCache extends BaseVisualFormDaoCache {

    @Autowired
    FlowFormDaoCache flowFormDaoCache;

    @Autowired
    @Qualifier(value = "BaseFormDaoCache")
    BaseVisualFormDaoCache formDaoCache;

    public IFormDAO getFormDao(String formCode, long id) {
        return super.getFormDao(formDaoCache, formCode, id);
    }

    @Override
    public void removeFromCache(IFormDAO iFormDAO) {
        super.removeFromCache(iFormDAO.getFormCode(), iFormDAO.getId());
        if (iFormDAO.getFlowId() != -1) {
            // 清空关联的流程记录
            flowFormDaoCache.removeFromCache(iFormDAO.getFlowId(), iFormDAO.getFormCode());
        }
    }
}