package com.redmoon.oa.base;

import java.util.List;
import java.util.Vector;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

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
public interface IFormDAO {
    /**
     * 获得标识列，在流程表单中为flowId，在模块表单中为id
     *
     * @return int
     */
    long getIdentifier();

    long getId();

    String getCwsId();

    Vector<FormField> getFields();

    String getVisualPath();

    String getFieldValue(String fieldName);

    FormDb getFormDb();

    int getFlowId();

    int getCwsFlag();

    int getCwsStatus();

    FormField getFormField(String fieldName);

    void setFieldValue(String fieldName, String value);

    boolean save() throws ErrMsgException;

    boolean isLoaded();

    String getFlowTypeCode();

    void setFlowTypeCode(String flowTypeCode);

    long getCwsQuoteId();

    String getCwsQuoteForm();

    String getFormCode();

    Vector<IAttachment> getAttachments();
}
