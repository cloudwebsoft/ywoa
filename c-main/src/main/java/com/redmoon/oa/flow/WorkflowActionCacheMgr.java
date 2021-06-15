package com.redmoon.oa.flow;

import cn.js.fan.cache.jcs.*;
import org.apache.log4j.Logger;

public class WorkflowActionCacheMgr extends AbstractRMCacheMgr {
    public WorkflowActionCacheMgr() {
    }

    @Override
    public void initLogger() {
        logger = Logger.getLogger(WorkflowActionCacheMgr.class.getName());
    }

    @Override
    public void initCachePrix() {
        cachePrix = "WorkflowAction_";
    }

    public WorkflowActionDb getWorkflowActionDb(int id) {
        WorkflowActionDb wfa = (WorkflowActionDb) rmCache.get(cachePrix + id);
        if (wfa == null) {
            wfa = new WorkflowActionDb(id);
            try {
                rmCache.put(cachePrix + id, wfa);
            } catch (Exception e) {
                logger.error("getWorkflowActionDb:" + e.getMessage());
            }
            return wfa;
        } else {
            return wfa;
        }
    }

    public WorkflowActionDb getWorkflowActionDb2(int id) {
        WorkflowActionDb wfa = (WorkflowActionDb) rmCache.get(cachePrix + id);
        if (wfa == null) {
            wfa = new WorkflowActionDb(id);
            try {
                rmCache.put(cachePrix + id, wfa);
            } catch (Exception e) {
                logger.error("getWorkflowActionDb:" + e.getMessage());
            }
            return wfa;
        } else {
            return wfa;
        }
    }

    public void removeFromCache(int id) {
        try {
            rmCache.remove(cachePrix + id);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void refreshSave(int id) {
        removeFromCache(id);
    }

    public void refreshDel(int id) {
        removeFromCache(id);
    }

}
