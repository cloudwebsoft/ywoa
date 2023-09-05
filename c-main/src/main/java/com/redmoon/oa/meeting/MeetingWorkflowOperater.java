package com.redmoon.oa.meeting;

import com.redmoon.oa.fileark.plugin.base.*;
import cn.js.fan.util.*;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowActionDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MeetingWorkflowOperater implements IPluginWorkflowOperater {
    WorkflowDb wfd;

    public MeetingWorkflowOperater(int flowId) {
        wfd = new WorkflowDb();
        wfd = wfd.getWorkflowDb(flowId);
    }

    /**
     * OnStatusChange
     *
     * @param status int
     * @throws ErrMsgException
     * @todo Implement this
     *   com.redmoon.oa.fileark.plugin.base.IPluginWorkflowAction method
     */
    public void OnStatusChange(int status, WorkflowActionDb lastAction) {
        if (status==WorkflowDb.STATUS_FINISHED) {
            // 插入申请已被同意的记录，作为会议的使用记录
            if (lastAction.getResultValue()==lastAction.RESULT_VALUE_AGGREE) {
            }
        }
    }
}
