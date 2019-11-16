package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Vector;
import java.util.Iterator;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class OracleSQLGenerator implements ISQLGenerator {

    public static final String[] keywords;

    static {
        keywords = new String[] {
                   "ACCESS", "DECIMAL", "INITIAL", "ON", "START",
                   "ADD", "NOT", "INSERT", "ONLINE", "SUCCESSFUL",
                   "ALL", "DEFAULT", "INTEGER", "OPTION", "SYNONYM",
                   "ALTER", "DELETE", "INTERSECT", "OR", "SYSDATE",
                   "AND", "DESC", "INTO", "ORDER", "TABLE",
                   "ANY", "DISTINCT", "IS", "PCTFREE", "THEN",
                   "AS", "DROP", "LEVEL", "PRIOR", "TO",
                   "ASC", "ELSE", "LIKE", "PRIVILEGES", "TRIGGER",
                   "AUDIT", "EXCLUSIVE", "LOCK", "PUBLIC", "UID",
                   "BETWEEN", "EXISTS", "LONG", "RAW", "UNION",
                   "BY", "FILE", "MAXEXTENTS", "RENAME", "UNIQUE",
                   "FROM", "FLOAT", "MINUS", "RESOURCE", "UPDATE",
                   "CHAR", "FOR", "MLSLABEL", "REVOKE", "USER",
                   "CHECK", "SHARE", "MODE", "ROW", "VALIDATE",
                   "CLUSTER", "GRANT", "MODIFY", "ROWID", "VALUES",
                   "COLUMN", "GROUP", "NOAUDIT", "ROWNUM", "VARCHAR",
                   "COMMENT", "HAVING", "NOCOMPRESS", "ROWS", "VARCHAR2",
                   "COMPRESS", "IDENTIFIED", "NOWAIT", "SELECT", "VIEW",
                   "CONNECT", "IMMEDIATE", "NULL", "SESSION", "WHENEVER",
                   "CREATE", "IN", "NUMBER", "SET", "WHERE",
                   "CURRENT", "INCREMENT", "OF", "SIZE", "WITH",
                   "DATE", "INDEX", "OFFLINE", "SMALLINT",
                   "CHAR", "VARHCAR", "VARCHAR2", "NUMBER", "DATE", "LONG",
                   "CLOB", "BLOB", "BFILE",
                   "INTEGER", "DECIMAL",
                   "SUM", "COUNT", "GROUPING", "AVERAGE",
                   "TYPE", "OTHER"
        };
    }

    public OracleSQLGenerator() {
        super();
    }

    public Vector generateCreateStr(String tableName, Vector fields) {
        Vector v = new Vector();

        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";
        str += "CREATE TABLE " + tableName + " (";
        str += "flowId int default -1 NOT NULL,";
        str += "flowTypeCode varchar2(20) NOT NULL,";
        str += "cws_creator varchar2(20),";
        str += "cws_id varchar2(20),";
        str += "unit_code varchar2(20),";
        str += "cws_parent_form varchar2(20),";        
        str += "ID NUMBER NOT NULL,";
        str += "cws_order int default 0 NOT NULL,";

        // -1表示临时流程，0表示流程进行中，1表示流程结束或智能模块创建
        str += "cws_status int default -1 NOT NULL";   
        
        // 创建拉单时所关联的源表单的ID
        str += "cws_quote_id int,";
        // 创建拉单后自动冲抵标志位
        str += "cws_flag int default 0 NOT NULL,";     
        // 创建进度字段
        str += "cws_progress int DEFAULT 0 NOT NULL,";
        // 创建创建时间字段
        str += "cws_create_date date DEFAULT sysdate,";
        // 创建修改时间字段
        str += "cws_modify_date date,";
        // 创建流程结束时间字段
        str += "cws_finish_date date,";
        
        String ffstr = "";
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ffstr.equals(""))
                ffstr += toStrForCreate(ff);
            else
                ffstr += "," + toStrForCreate(ff);
        }
        if (!ffstr.equals(""))
            str += "," + ffstr;
        // str += ",CONSTRANT PK_" + getTableNameByForm() + " PRIMARY KEY (flowId)";
        str += ",primary key (flowId)";
        str += ")";
        v.addElement(str);

        // @task:创建自动增长列
        str = "create sequence " + tableName + "_id minvalue 1 maxvalue 9999999999999999999 start with 1 increment by 1 cache 30 order";
        // str = "CREATE SEQUENCE " + tableName + "_ID INCREMENT BY 1 START WITH 1 MAXVALUE 1.0E28 MINVALUE 1 NOCYCLE CACHE 20 NOORDER";
        v.addElement(str);
        str = "CREATE OR REPLACE TRIGGER " + tableName + "_tg BEFORE INSERT ON " + tableName + " FOR EACH ROW BEGIN SELECT " + tableName +  "_id.NEXTVAL INTO :NEW.id FROM DUAL; END;";
        v.addElement(str);

        // 创建索引
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (flowTypeCode)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_creator)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (unit_code)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_id, cws_order)";
        v.addElement(str);

        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_quote_id)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_flag)";
        v.addElement(str);
        
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_parent_form)";
        v.addElement(str);        
        
        // 创建comment
        /*
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str = ff.toStrForCreateComment(tableName);
            v.addElement(str);
        }
        */
        // System.out.println("generateCreateStr:" + str);

        return v;
    }
    
    public String getTableNameForLog(String tableName) {
    	return tableName + "_log";
    }
    
    public Vector generateCreateStrForLog(String tableName, Vector fields) {
        Vector v = new Vector();

        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";
        str += "CREATE TABLE " + getTableNameForLog(tableName) + " (";
        str += "flowId int default 0 NOT NULL,";
        str += "flowTypeCode varchar2(20) NOT NULL,";
        str += "cws_creator varchar2(20),";
        str += "cws_id varchar2(20),";
        str += "unit_code varchar2(20),";
        str += "ID NUMBER NOT NULL,";
        
        str += "cws_log_user varchar(20),";
        str += "cws_log_type NUMBER DEFAULT '0',";
        str += "cws_log_date date DEFAULT sysdate,";
        str += "cws_log_id int default 0 NOT NULL,";

        str += "cws_order int default 0 NOT NULL";

        String ffstr = "";
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ffstr.equals(""))
                ffstr += toStrForCreate(ff);
            else
                ffstr += "," + toStrForCreate(ff);
        }
        if (!ffstr.equals(""))
            str += "," + ffstr;
        // str += ",CONSTRANT PK_" + getTableNameByForm() + " PRIMARY KEY (flowId)";
        str += ",primary key (flowId)";
        str += ")";
        v.addElement(str);

        // @task:创建自动增长列
        str = "create sequence " + tableName + "_id minvalue 1 maxvalue 9999999999999999999 start with 1 increment by 1 cache 30 order";
        // str = "CREATE SEQUENCE " + tableName + "_ID INCREMENT BY 1 START WITH 1 MAXVALUE 1.0E28 MINVALUE 1 NOCYCLE CACHE 20 NOORDER";
        v.addElement(str);
        str = "CREATE OR REPLACE TRIGGER " + tableName + "_tg BEFORE INSERT ON " + tableName + " FOR EACH ROW BEGIN SELECT " + tableName +  "_id.NEXTVAL INTO :NEW.id FROM DUAL; END;";
        v.addElement(str);

        // 创建索引
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (flowTypeCode)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_creator)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (unit_code)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_id, cws_order)";
        v.addElement(str);
        
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_log_type)";
        v.addElement(str);
        str = "create index idx_" + RandomSecquenceCreator.getId(5) + " on " + tableName + " (cws_log_id)";
        v.addElement(str);
        
        // 创建comment
        /*
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str = ff.toStrForCreateComment(tableName);
            v.addElement(str);
        }
        */
        // System.out.println("generateCreateStr:" + str);

        return v;
    }    

    public Vector generateModifyStr(String tableName, Vector[] vt) {
        Vector v = new Vector();
        String altstr = "ALTER TABLE " + tableName;
        Vector addv = vt[1];
        LogUtil.getLog(getClass()).info("FormDb generateModifyStr vt[1].size=" + vt[1].size());
        Iterator ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField)ir.next();
            LogUtil.getLog(getClass()).info("FormDb generateModifyStr ADD field name=" + addff.getName());
            addstr = altstr + " ADD " + toStrForCreate(addff);
            v.addElement(addstr);
        }

        // 把drop放到add的下面，以防止出现不能删除所有的列的错误！
        Vector delv = vt[0];
        ir = delv.iterator();
        while (ir.hasNext()) {
            FormField delff = (FormField)ir.next();
            // System.out.println("FormDb generateModifyStr: DROP field name=" + delff.getName());
            v.addElement(altstr + " DROP (" + delff.getName() + ")");
        }

        // mysql:ALTER TABLE `test`.`ff` DROP COLUMN `name`, ADD COLUMN `con` VARCHAR(45) NOT NULL AFTER `title`;
        return v;
    }

    public Vector generateModifyStrForLog(String tableName, Vector[] vt) {
        Vector v = new Vector();
        String altstr = "ALTER TABLE " + getTableNameForLog(tableName);
        Vector addv = vt[1];
        LogUtil.getLog(getClass()).info("FormDb generateModifyStr vt[1].size=" + vt[1].size());
        Iterator ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField)ir.next();
            LogUtil.getLog(getClass()).info("FormDb generateModifyStr ADD field name=" + addff.getName());
            addstr = altstr + " ADD " + toStrForCreate(addff);
            v.addElement(addstr);
        }

        // 把drop放到add的下面，以防止出现不能删除所有的列的错误！
        Vector delv = vt[0];
        ir = delv.iterator();
        while (ir.hasNext()) {
            FormField delff = (FormField)ir.next();
            // System.out.println("FormDb generateModifyStr: DROP field name=" + delff.getName());
            v.addElement(altstr + " DROP (" + delff.getName() + ")");
        }

        // mysql:ALTER TABLE `test`.`ff` DROP COLUMN `name`, ADD COLUMN `con` VARCHAR(45) NOT NULL AFTER `title`;
        return v;
    }
    
    public Vector generateDropTable(String tableName) {
        Vector v = new Vector();
        v.addElement("DROP SEQUENCE " + tableName + "_id");
        v.addElement("DROP TABLE " + tableName);
        return v;
    }
    
    public Vector generateDropTableForLog(String tableName) {
        Vector v = new Vector();
        v.addElement("DROP SEQUENCE " + getTableNameForLog(tableName) + "_id");
        v.addElement("DROP TABLE " + getTableNameForLog(tableName));
        return v;
    }    

    public boolean isFieldKeywords(String fieldName) {
        int len = keywords.length;
        for (int i=0; i<len; i++) {
            if (fieldName.equalsIgnoreCase(keywords[i]))
                return true;
        }
        return false;
    }

    public String toStrForCreate(FormField ff) {
        String typeStr = "";
         if (ff.getType().equals(FormField.TYPE_MACRO)) {
             MacroCtlMgr mm = new MacroCtlMgr();
             MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
             if (mu==null)
                 throw new IllegalArgumentException("Macro ctl type=" + ff.getMacroType() + " is not exist.");
             typeStr = mu.getFieldType();
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR) {
             typeStr = "varchar2(250)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_INT) {
             typeStr = "NUMBER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_LONG) {
             typeStr = "NUMBER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_FLOAT) {
             typeStr = "NUMBER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE) {
             typeStr = "NUMBER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
             typeStr = "NUMBER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_BOOLEAN) {
             typeStr = "char(1)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {
             typeStr = "varchar2(4000)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
             typeStr = "date";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
             typeStr = "date";
         }
         else
             typeStr = "varchar2(250)";

         String defaultStr = null;
         if (ff.getDefaultValue()!=null) {
             // System.out.println("FormField defaultValue=" + defaultValue + " type=" + type);
             if (ff.getType().equals(FormField.TYPE_DATE)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                     defaultStr = "sysdate"; // oracle 不需要 ' '
             } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                     defaultStr = "sysdate";
             }
             else
                 defaultStr = StrUtil.sqlstr(ff.getDefaultValue()); // oracle 不需要 ' '
         }
         else
            defaultStr = "''";

        String str = "";
        str = ff.getName() + " " + typeStr + " DEFAULT " + defaultStr;
         // System.out.println(getClass() + " toStrForCreate name=" + name + " fieldType=" + fieldType);
         // System.out.println(getClass() + " toStrForCreate:" + str);
        return str;
    }

    public String getTableColumnsFromDbSql(String tableName) {
        return "select * from " +
                    tableName + " where rownum<=1";
    }

    public boolean isTableForLogExist(String tableName) {
    	String sql = " SELECT COUNT(*) FROM User_Tables WHERE table_name = '" + getTableNameForLog(tableName) + "'";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		return rr.getInt(1)==1;
	    	}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return false;
    }
}
