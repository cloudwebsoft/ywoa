package com.redmoon.oa.base;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import java.util.Vector;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.kit.util.FileUpload;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface IFormValidator {

        /**
         * 取得是否启用validator
         * @return boolean
         */
        public boolean isUsed();

    /**
     * 置是否启用validator
     * @param isUsed boolean
     */
    public void setIsUsed(boolean isUsed);

    /**
     * 用于获取配置文件XML中的数据
     * @return String
     */
    public String getExtraData();

    public void setExtraData(String extraData);

    /**
     * 流程提交后表单保存前进行验证，保存草稿的时候不验证
     * @param request HttpServletRequest
     * @param fileUpload FileUpload
     * @param flowId int
     * @param fields Vector 来自于数据库
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean validate(HttpServletRequest request, FileUpload fileUpload,
                            int flowId, Vector fields) throws ErrMsgException;

    /**
     * 当流程完成时执行
     * @param request HttpServletRequest
     * @param flowId int
     * @throws ErrMsgException
     */
    public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction) throws
            ErrMsgException;

    /**
     * 当流程转交下一步时事件处理，在此之前流程节点已处理完毕，状态已改变
     * @param request
     * @param flowId
     * @param fu
     */
    public void onActionFinished(HttpServletRequest request, int flowId, FileUpload fu);
}
