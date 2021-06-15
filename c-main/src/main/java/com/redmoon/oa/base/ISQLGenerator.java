package com.redmoon.oa.base;

import com.redmoon.oa.flow.FormField;

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
    Vector<String> generateCreateStr(String tableName, Vector<FormField> fields);
    
    Vector<String> generateModifyStr(String tableName, Vector[] vt);
    
    Vector<String> generateCreateStrForLog(String tableName, Vector<FormField> fields);

    Vector<String> generateModifyStrForLog(String tableName, Vector[] vt);
    
    Vector<String> generateDropTableForLog(String tableName);

    Vector<String> generateDropTable(String tableName);

    boolean isFieldKeywords(String field);

    String toStrForCreate(FormField ff);

    String getTableColumnsFromDbSql(String tableName);
    
    boolean isTableForLogExist(String tableName);
}
