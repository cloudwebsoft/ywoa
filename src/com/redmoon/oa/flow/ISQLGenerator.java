package com.redmoon.oa.flow;

import java.util.Vector;

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
public interface ISQLGenerator {
    /**
     * 创建增加表单的SQL语句
     * @param tableName String
     * @param fields Vector
     * @return String
     */
    public Vector generateCreateStr(String tableName, Vector fields);

    /**
     * 创建修改表单中字段SQL语句，注意只能增加和删除字段，不能对字段的名称、类型、默认值等作修改
     * @param tableName String
     * @param vt Vector[]
     * @return String
     */
    public Vector generateModifyStr(String tableName, Vector[] vt);
    
    /**
     * 创建表单时，同步创建其历史记录表
     * @param tableName
     * @param vt
     * @return
     */
    public Vector generateCreateStrForLog(String tableName, Vector fields);

    public Vector generateModifyStrForLog(String tableName, Vector[] vt);
    
    public Vector generateDropTableForLog(String tableName);

    public Vector generateDropTable(String tableName);

    public boolean isFieldKeywords(String field);

    public String toStrForCreate(FormField ff);

    public String getTableColumnsFromDbSql(String tableName);
    
    public boolean isTableForLogExist(String tableName);


}
