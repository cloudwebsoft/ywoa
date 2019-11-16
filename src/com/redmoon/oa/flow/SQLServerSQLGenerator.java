package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Vector;
import java.util.Iterator;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

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
public class SQLServerSQLGenerator implements ISQLGenerator {
    public static final String[] keywords;
    static {
        keywords = new String[] {
                   "ADD", "EXCEPT", "PERCENT",
                   "ALL", "EXEC", "PLAN",
                   "ALTER", "EXECUTE", "PRECISION",
                   "AND", "EXISTS", "PRIMARY",
                   "ANY", "EXIT", "PRINT",
                   "AS", "FETCH", "PROC",
                   "ASC", "FILE", "PROCEDURE",
                   "AUTHORIZATION", "FILLFACTOR", "PUBLIC",
                   "BACKUP", "FOR", "RAISERROR",
                   "BEGIN", "FOREIGN", "READ",
                   "BETWEEN", "FREETEXT", "READTEXT",
                   "BREAK", "FREETEXTTABLE", "RECONFIGURE",
                   "BROWSE", "FROM", "REFERENCES",
                   "BULK", "FULL", "REPLICATION",
                   "BY", "FUNCTION", "RESTORE",
                   "CASCADE", "GOTO", "RESTRICT",
                   "CASE", "GRANT", "RETURN",
                   "CHECK", "GROUP", "REVOKE",
                   "CHECKPOINT", "HAVING", "RIGHT",
                   "CLOSE", "HOLDLOCK", "ROLLBACK",
                   "CLUSTERED", "IDENTITY", "ROWCOUNT",
                   "COALESCE", "IDENTITY_INSERT", "ROWGUIDCOL",
                   "COLLATE", "IDENTITYCOL", "RULE",
                   "COLUMN", "IF", "SAVE",
                   "COMMIT", "IN", "SCHEMA",
                   "COMPUTE", "INDEX", "SELECT",
                   "CONSTRAINT", "INNER", "SESSION_USER",
                   "CONTAINS", "INSERT", "SET",
                   "CONTAINSTABLE", "INTERSECT", "SETUSER",
                   "CONTINUE", "INTO", "SHUTDOWN",
                   "CONVERT", "IS", "SOME",
                   "CREATE", "JOIN", "STATISTICS",
                   "CROSS", "KEY", "SYSTEM_USER",
                   "CURRENT", "KILL", "TABLE",
                   "CURRENT_DATE", "LEFT", "TEXTSIZE",
                   "CURRENT_TIME", "LIKE", "THEN",
                   "CURRENT_TIMESTAMP", "LINENO", "TO",
                   "CURRENT_USER", "LOAD", "TOP",
                   "CURSOR", "NATIONAL", "TRAN",
                   "DATABASE", "NOCHECK", "TRANSACTION",
                   "DBCC", "NONCLUSTERED", "TRIGGER",
                   "DEALLOCATE", "NOT", "TRUNCATE",
                   "DECLARE", "NULL", "TSEQUAL",
                   "DEFAULT", "NULLIF", "UNION",
                   "DELETE", "OF", "UNIQUE",
                   "DENY", "OFF", "UPDATE",
                   "DESC", "OFFSETS", "UPDATETEXT",
                   "DISK", "ON", "USE",
                   "DISTINCT", "OPEN", "USER",
                   "DISTRIBUTED", "OPENDATASOURCE", "VALUES",
                   "DOUBLE", "OPENQUERY", "VARYING",
                   "DROP", "OPENROWSET", "VIEW",
                   "DUMMY", "OPENXML", "WAITFOR",
                   "DUMP", "OPTION", "WHEN",
                   "ELSE", "OR", "WHERE",
                   "END", "ORDER", "WHILE",
                   "ERRLVL", "OUTER", "WITH",
                   "ESCAPE", "OVER", "WRITETEXT", "OTHER"

        };
    }

    public SQLServerSQLGenerator() {
        super();
    }
    
    public String getTableNameForLog(String tableName) {
    	return tableName + "_log";
    }    

    /**
     * 创建增加表单的SQL语句
     * @param fields Vector
     * @return String
     */
    public Vector generateCreateStr(String tableName, Vector fields) {
        Vector v = new Vector();
        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";

        str += "CREATE TABLE dbo." + tableName + " (";
        str += "flowId int default -1 NOT NULL,";
        str += "flowTypeCode varchar(20) NOT NULL,";
        str += "id int NOT NULL IDENTITY (1, 1),";
        str += "cws_creator varchar(20),";
        str += "unit_code varchar(20),";
        str += "cws_parent_form varchar(20),";
        str += "cws_id varchar(20),";
        str += "cws_order int NULL,";

        // -1表示临时流程，0表示流程进行中，1表示流程结束或智能模块创建
        str += "cws_status int NOT NULL DEFAULT (-1),";   
        
        // 创建拉单时所关联的源表单的ID
        str += "cws_quote_id int NULL,";
        // 创建拉单后自动冲抵标志位
        str += "cws_flag int default 0 NOT NULL,"; 
        // 创建进度字段
        str += "cws_progress int DEFAULT 0 NOT NULL,";
        // 创建创建时间字段
        str += "cws_create_date DATETIME,";
        // 创建修改时间字段
        str += "cws_modify_date DATETIME,";
        // 创建流程结束时间字段
        str += "cws_finish_date DATETIME,";
        
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }
        str += ") ON [PRIMARY]";
        v.addElement(str);

        str = "ALTER TABLE dbo." + tableName + " ADD CONSTRAINT PK_" +
              tableName + " PRIMARY KEY CLUSTERED (id) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName + "_flowId ON dbo." +
              tableName + " (flowId) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_flowTypeCode ON dbo." + tableName +
              " (flowTypeCode) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_cws_creator ON dbo." + tableName +
              " (cws_creator) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_unit_code ON dbo." + tableName +
              " (unit_code) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_cws_id_order ON dbo." + tableName +
              " (cws_id,cws_order) ON [PRIMARY]";
        v.addElement(str);
        
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
        	"_cws_quote_id ON dbo." + tableName +
        	" (cws_quote_id) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
        	"_cws_flag ON dbo." + tableName +
        	" (cws_flag) ON [PRIMARY]";
        v.addElement(str);  
        
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
        "_cws_parent_form ON dbo." + tableName +
        " (cws_parent_form) ON [PRIMARY]";
        v.addElement(str);        

        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            /*
                         // 字段说明，不能按如下方式调用，因为调用了存储过程
                         str = "DECLARE @v sql_variant";
                         v.addElement(str);
                         str = "SET @v = N" + StrUtil.sqlstr(ff.getTitle());
                         v.addElement(str);
                         str = "EXECUTE sp_addextendedproperty N'MS_Description', @v, N'user', N'dbo', N'table', N'" +
                  tableName + "', N'column', N'" + ff.getName() + "'";
                         v.addElement(str);
             */
            if (!ff.getDefaultValue().equals("")) {
                // 字段默认值
                if (ff.getFieldType() == FormField.FIELD_TYPE_TEXT ||
                    ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT N" +
                          StrUtil.sqlstr(ff.getDefaultValue()) + " FOR " +
                          ff.getName();
                } else if (ff.getType().equals(FormField.TYPE_DATE) ||
                           ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                    String defaultStr = ff.getDefaultValue();
                    if (ff.getType().equals(FormField.TYPE_DATE)) {
                        if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                            defaultStr = "getDate()";
                    } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                            defaultStr = "getDate()";
                    }
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT " +
                          defaultStr + " FOR " + ff.getName();
                } else {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT " +
                          ff.getDefaultValue() + " FOR " + ff.getName();
                }
                v.addElement(str);
            }
        }

        return v;
    }

    public Vector generateCreateStrForLog(String tableName, Vector fields) {    	
    	tableName = getTableNameForLog(tableName);
    	
        Vector v = new Vector();
        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";

        str += "CREATE TABLE dbo." + tableName + " (";
        str += "flowId int NOT NULL,";
        str += "flowTypeCode varchar(20) NOT NULL,";
        str += "id int NOT NULL IDENTITY (1, 1),";
        
        str += "cws_log_user varchar(20),";
        str += "cws_log_type int NOT NULL,";
        str += "cws_log_date DATETIME NOT NULL,";
        str += "cws_log_id int NOT NULL,";        
        
        str += "cws_creator varchar(20),";
        str += "unit_code varchar(20),";
        str += "cws_id varchar(20),";
        str += "cws_order int NULL,";

        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }
        str += ") ON [PRIMARY]";
        v.addElement(str);

        str = "ALTER TABLE dbo." + tableName + " ADD CONSTRAINT PK_" +
              tableName + " PRIMARY KEY CLUSTERED (id) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName + "_flowId ON dbo." +
              tableName + " (flowId) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_flowTypeCode ON dbo." + tableName +
              " (flowTypeCode) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_cws_creator ON dbo." + tableName +
              " (cws_creator) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_unit_code ON dbo." + tableName +
              " (unit_code) ON [PRIMARY]";
        v.addElement(str);
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
              "_cws_id_order ON dbo." + tableName +
              " (cws_id,cws_order) ON [PRIMARY]";
        v.addElement(str);
        
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
        "_cws_log_type ON dbo." + tableName +
        " (cws_log_type) ON [PRIMARY]";
        v.addElement(str);      
        
        str = "CREATE NONCLUSTERED INDEX IX_" + tableName +
        "_cws_log_id ON dbo." + tableName +
        " (cws_log_id) ON [PRIMARY]";
        v.addElement(str);             

        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            /*
                         // 字段说明，不能按如下方式调用，因为调用了存储过程
                         str = "DECLARE @v sql_variant";
                         v.addElement(str);
                         str = "SET @v = N" + StrUtil.sqlstr(ff.getTitle());
                         v.addElement(str);
                         str = "EXECUTE sp_addextendedproperty N'MS_Description', @v, N'user', N'dbo', N'table', N'" +
                  tableName + "', N'column', N'" + ff.getName() + "'";
                         v.addElement(str);
             */
            if (!ff.getDefaultValue().equals("")) {
                // 字段默认值
                if (ff.getFieldType() == FormField.FIELD_TYPE_TEXT ||
                    ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT N" +
                          StrUtil.sqlstr(ff.getDefaultValue()) + " FOR " +
                          ff.getName();
                } else if (ff.getType().equals(FormField.TYPE_DATE) ||
                           ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                    String defaultStr = ff.getDefaultValue();
                    if (ff.getType().equals(FormField.TYPE_DATE)) {
                        if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                            defaultStr = "getDate()";
                    } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                            defaultStr = "getDate()";
                    }
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT " +
                          defaultStr + " FOR " + ff.getName();
                } else
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + ff.getName() + " DEFAULT " +
                          ff.getDefaultValue() + " FOR " + ff.getName();
                v.addElement(str);
            }
        }
        
        str = "ALTER TABLE dbo." + tableName + " ADD CONSTRAINT DF_" +
        	tableName + "_cws_log_type DEFAULT 0 FOR cws_log_type";
        v.addElement(str);        

        str = "ALTER TABLE dbo." + tableName + " ADD CONSTRAINT DF_" +
    	tableName + "_cws_log_id DEFAULT 0 FOR cws_log_id";
        v.addElement(str);     
    
        return v;
    }

    
    /**
     * 创建修改表单中字段SQL语句，注意只能增加和删除字段，不能对字段的名称、类型、默认值等作修改
     * @param vt Vector[]
     * @return String
     */
    public Vector generateModifyStr(String tableName, Vector[] vt) {
        Vector v = new Vector();

        String str;
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            str = "ALTER TABLE dbo." + tableName + " DROP COLUMN " +
                  delff.getName();
            v.addElement(str);

            // 删除默认值
            if (delff.getDefaultValue()!=null && !delff.getDefaultValue().equals(""))
                str = "ALTER TABLE dbo." + tableName + " DROP CONSTRAINT DF_" +
                      tableName + "_" + delff.getName();
        }
        Vector addv = vt[1];
        // System.out.println("generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        while (ir.hasNext()) {
            FormField addff = (FormField) ir.next();
            // System.out.println("generateModifyStr field name=" + addff.getName());
            // 增加字段
            str = "ALTER TABLE dbo." + tableName + " ADD " +
                  toStrForCreate(addff);
            v.addElement(str);
            if (addff.getDefaultValue() != null &&
                !addff.getDefaultValue().equals("")) {
                // 处理字段默认值
                if (addff.getFieldType() == FormField.FIELD_TYPE_TEXT ||
                    addff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT N" +
                          StrUtil.sqlstr(addff.getDefaultValue()) + " FOR " +
                          addff.getName();
                } else if (addff.getType().equals(FormField.TYPE_DATE) ||
                           addff.getType().equals(FormField.TYPE_DATE_TIME)) {
                    String defaultStr = addff.getDefaultValue();
                    if (addff.getType().equals(FormField.TYPE_DATE)) {
                        if (addff.getDefaultValue().equals(FormField.
                                DATE_CURRENT))
                            defaultStr = "getDate()";
                    } else if (addff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        if (addff.getDefaultValue().equals(FormField.
                                DATE_CURRENT))
                            defaultStr = "getDate()";
                    }
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT " +
                          defaultStr + " FOR " + addff.getName();
                } else {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT " +
                          addff.getDefaultValue() + " FOR " + addff.getName();
                }
                v.addElement(str);

            }
        }
        return v;
    }
    
    public Vector generateModifyStrForLog(String tableName, Vector[] vt) {
    	tableName = getTableNameForLog(tableName);
    	
        Vector v = new Vector();

        String str;
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            str = "ALTER TABLE dbo." + tableName + " DROP COLUMN " +
                  delff.getName();
            v.addElement(str);

            // 删除默认值
            if (delff.getDefaultValue()!=null && !delff.getDefaultValue().equals(""))
                str = "ALTER TABLE dbo." + tableName + " DROP CONSTRAINT DF_" +
                      tableName + "_" + delff.getName();
        }
        Vector addv = vt[1];
        // System.out.println("generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        while (ir.hasNext()) {
            FormField addff = (FormField) ir.next();
            // System.out.println("generateModifyStr field name=" + addff.getName());
            // 增加字段
            str = "ALTER TABLE dbo." + tableName + " ADD " +
                  toStrForCreate(addff);
            v.addElement(str);
            if (addff.getDefaultValue() != null &&
                !addff.getDefaultValue().equals("")) {
                // 处理字段默认值
                if (addff.getFieldType() == FormField.FIELD_TYPE_TEXT ||
                    addff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT N" +
                          StrUtil.sqlstr(addff.getDefaultValue()) + " FOR " +
                          addff.getName();
                } else if (addff.getType().equals(FormField.TYPE_DATE) ||
                           addff.getType().equals(FormField.TYPE_DATE_TIME)) {
                    String defaultStr = addff.getDefaultValue();
                    if (addff.getType().equals(FormField.TYPE_DATE)) {
                        if (addff.getDefaultValue().equals(FormField.
                                DATE_CURRENT))
                            defaultStr = "getDate()";
                    } else if (addff.getType().equals(FormField.TYPE_DATE_TIME)) {
                        if (addff.getDefaultValue().equals(FormField.
                                DATE_CURRENT))
                            defaultStr = "getDate()";
                    }
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT " +
                          defaultStr + " FOR " + addff.getName();
                } else {
                    str = "ALTER TABLE dbo." + tableName +
                          " ADD CONSTRAINT DF_" +
                          tableName + "_" + addff.getName() + " DEFAULT " +
                          addff.getDefaultValue() + " FOR " + addff.getName();
                }
                v.addElement(str);

            }
        }
        return v;
    }
    

    public Vector generateDropTable(String tableName) {
        Vector v = new Vector();
        v.addElement("DROP TABLE " + tableName);
        return v;
    }
    
    public Vector generateDropTableForLog(String tableName) {
    	tableName = getTableNameForLog(tableName);
        Vector v = new Vector();
        v.addElement("DROP TABLE " + tableName);
        return v;
    }    

    public boolean isFieldKeywords(String fieldName) {
        int len = keywords.length;
        for (int i = 0; i < len; i++) {
            if (fieldName.equalsIgnoreCase(keywords[i]))
                return true;
        }
        return false;
    }

    /**
     * 得到创建字段的SQL语句
     * 20070425
     * MYSQL的极限长度 innodb表中的varchar()合计长度不能超过65535，因此将varchar(250)改为varchar(100)
     * ERROR http-80-Processor23 com.redmoon.oa.flow.FormDb - create:Row size too large. The maximum row size for the used table type, not counting BLOBs, is 65535. You have to change some columns to TEXT or BLOBs. Now transaction rollback
     * @return String
     */
    public String toStrForCreate(FormField ff) {
        String typeStr = "";
        if (ff.getType().equals(FormField.TYPE_MACRO)) {
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
            if (mu==null)
                 throw new IllegalArgumentException("Macro ctl type=" + ff.getMacroType() + " is not exist.");
            typeStr = mu.getFieldType();
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
            typeStr = "nvarchar(200)";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_INT) {
            typeStr = "int";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_LONG) {
            typeStr = "bigint";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
            typeStr = "real";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
            typeStr = "float";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
            typeStr = "float";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_BOOLEAN) {
            typeStr = "char(1)";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
            typeStr = "ntext";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
            typeStr = "smalldatetime";
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
            typeStr = "datetime";
        } else
            typeStr = "nvarchar(100)";

        String str = "";
        str = ff.getName() + " " + typeStr + " NULL";
        /*
                  if (!typeStr.equals("text"))
            str = ff.getName() + " " + typeStr + " default " + defaultStr +
                  " COMMENT " + StrUtil.sqlstr(ff.getTitle());
                  else
            str = ff.getName() + " " + typeStr + " COMMENT " +
                  StrUtil.sqlstr(ff.getTitle());
         */
        // System.out.println(getClass() + " toStrForCreate name=" + name + " fieldType=" + fieldType);
        // System.out.println(getClass() + " toStrForCreate:" + str);
        return str;
    }

    public String getTableColumnsFromDbSql(String tableName) {
        return "select top 1 * from " +
                tableName;
    }
    
    public boolean isTableForLogExist(String tableName) {
    	tableName = getTableNameForLog(tableName);
        String sql = "SELECT * FROM dbo.SysObjects WHERE ID = object_id(N'[" + tableName + "]') AND OBJECTPROPERTY(ID, 'IsTable')";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	if (ri.hasNext()) {
	    		return true;
	    	}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return false;
    }    
}
