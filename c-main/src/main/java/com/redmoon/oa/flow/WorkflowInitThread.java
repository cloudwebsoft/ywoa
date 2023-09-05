package com.redmoon.oa.flow;

import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class WorkflowInitThread extends Thread {
    Vector<String> vStart;
    WorkflowActionDb waStart;
    WorkflowDb wf;
    Map<String, WorkflowActionDb> actionMap;
    List<WorkflowLinkDb> linkList;

    WorkflowInitThread(WorkflowDb wf, Vector<String> vStart, WorkflowActionDb waStart, Map<String, WorkflowActionDb> actionMap, List<WorkflowLinkDb> linkList) {
        this.wf = wf;
        this.vStart = vStart;
        this.waStart = waStart;
        this.actionMap = actionMap;
        this.linkList = linkList;
    }

    @Override
    public void run() {
        Connection connection = new Connection(Global.getDefaultDB());
        Connection connectionLink = new Connection(Global.getDefaultDB());
        PreparedStatement pstmtAction = null;
        PreparedStatement pstmtLink = null;
        try {
            pstmtAction = connection.prepareStatement(WorkflowActionDb.getINSERT());
            pstmtLink = connectionLink.prepareStatement(WorkflowLinkDb.getINSERT());

            int actionCount = 0;
            int linkCount = 0;
            for (String internalName : actionMap.keySet()) {
                // 開始節點已創建
                if (!internalName.equals(waStart.getInternalName())) {
                    actionCount++;
                    WorkflowActionDb wa = actionMap.get(internalName);
                    wa.setFlowId(wf.getId());
                    wa.createAddBatch(pstmtAction);
                }
            }

            for (WorkflowLinkDb wl : linkList) {
                linkCount++;
                wl.setFlowId(wf.getId());
                wl.createAddBatch(pstmtLink);
            }

            if (actionCount > 0) {
                pstmtAction.executeBatch();
            }
            if (linkCount > 0) {
                pstmtLink.executeBatch();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            connection.close();
            connectionLink.close();
        }

        List<WorkflowActionDb> actionsToIgnore = new ArrayList<>();
        for (String internalName : vStart) {
            // 忽略无法被匹配的起点分支
            if (!internalName.equals(waStart.getInternalName())) {
                WorkflowMgr wm = new WorkflowMgr();
                wm.ignoreBranch(actionMap.get(internalName), null, actionsToIgnore);
            }
        }
        WorkflowActionDb wachk = new WorkflowActionDb();
        wachk.ignoreActions(wf, actionsToIgnore);
    }
}
