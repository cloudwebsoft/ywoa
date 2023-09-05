package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import com.cloudweb.oa.base.AbstractFlowFormDaoCache;
import com.cloudweb.oa.base.AbstractVisualFormDaoCache;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlowFormDaoCache extends BaseFlowFormDaoCache {

    @Autowired
    @Qualifier(value = "BaseFlowFormDaoCache")
    BaseFlowFormDaoCache baseFlowFormDaoCache;

    @Autowired
    VisualFormDaoCache visualFormDaoCache;

    public IFormDAO getFormDao(int flowId, String formCode) {
        return super.getFormDao(baseFlowFormDaoCache, flowId, formCode);
    }

    @Override
    public void removeFromCache(IFormDAO iFormDAO) {
        super.removeFromCache(iFormDAO.getFlowId(), iFormDAO.getFormCode());
        // 清空关联的模块记录
        visualFormDaoCache.removeFromCache(iFormDAO.getFormCode(), iFormDAO.getId());
    }
}