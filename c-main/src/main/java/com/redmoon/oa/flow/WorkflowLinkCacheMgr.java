package com.redmoon.oa.flow;

import cn.js.fan.cache.jcs.*;
import org.apache.log4j.Logger;

public class WorkflowLinkCacheMgr extends AbstractRMCacheMgr {
    public WorkflowLinkCacheMgr() {
    }

    public void initLogger() {
        logger = Logger.getLogger(WorkflowLinkCacheMgr.class.getName());
    }

    public void initCachePrix() {
        cachePrix = "WorkflowLink_";
    }

    public WorkflowLinkDb getWorkflowLinkDb(int id) {
        WorkflowLinkDb wfa = (WorkflowLinkDb) rmCache.get(cachePrix + id);
        if (wfa == null) {
            //logger.info( "getWorkflow: id=" + id);
            wfa = new WorkflowLinkDb(id);
            try {
                rmCache.put(cachePrix + id, wfa);
            } catch (Exception e) {
                logger.error("getWorkflowLinkDb:" + e.getMessage());
            }
            return wfa;
        } else
            return wfa;
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
