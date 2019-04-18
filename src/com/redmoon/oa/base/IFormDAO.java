package com.redmoon.oa.base;

import java.util.Vector;

import com.redmoon.oa.flow.FormDb;

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
         * @return int
         */
        public long getIdentifier();
        
        public long getId();

    public String getCwsId();

    public Vector getFields();

    public String getVisualPath();

    public String getFieldValue(String fieldName);

    public FormDb getFormDb();
    
    public int getFlowId();
    
    public int getCwsFlag();

    public int getCwsStatus();
}
