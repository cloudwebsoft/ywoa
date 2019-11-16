package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Vector;
import java.util.Iterator;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;

import cn.js.fan.db.ResultIterator;
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
public class MySQLSQLGenerator implements ISQLGenerator {
    public static final String[] keywords;
    static {
        keywords = new String[] {
                   "ADD",  "ALL",  "ALTER" ,
                  "ANALYZE", "AND", "AS" ,
                  "ASC",  "AUTO_INCREMENT", "BDB" ,
                  "BEFORE",  "BERKELEYDB",  "BETWEEN",
                  "BIGINT",  "BINARY",  "BLOB",
                  "BOTH",  "BTREE",  "BY",
                  "CASCADE",  "CASE",  "CHANGE",
                  "CHAR",  "CHARACTER",  "CHECK",
                  "COLLATE",  "COLUMN",  "COLUMNS",
                  "CONSTRAINT", "CREATE",  "CROSS",
                  "CURRENT_DATE",  "CURRENT_TIME",  "CURRENT_TIMESTAMP",
                  "DATABASE",  "DATABASES",  "DAY_HOUR",
                  "DAY_MINUTE",  "DAY_SECOND",  "DEC",
                  "DECIMAL",  "DEFAULT",  "DELAYED",
                  "DELETE",  "DESC",  "DESCRIBE",
                  "DISTINCT",  "DISTINCTROW",  "DIV",
                  "DOUBLE",  "DROP",  "ELSE",
                  "ENCLOSED",  "ERRORS",  "ESCAPED",
                  "EXISTS",  "EXPLAIN",  "FALSE",
                  "FIELDS",  "FLOAT",  "FOR",
                  "FORCE",  "FOREIGN",  "FROM",
                  "FULLTEXT",  "FUNCTION",  "GRANT",
                  "GROUP",  "HASH",  "HAVING",
                  "HIGH_PRIORITY",  "HOUR_MINUTE",  "HOUR_SECOND",
                  "IF",  "IGNORE",  "IN",
                  "INDEX",  "INFILE",  "INNER",
                  "INNODB",  "INSERT",  "INT",
                  "INTEGER",  "INTERVAL",  "INTO",
                  "IS",  "JOIN",  "KEY",
                  "KEYS",  "KILL",  "LEADING",
                  "LEFT",  "LIKE",  "LIMIT",
                  "LINES",  "LOAD",  "LOCALTIME",
                  "LOCALTIMESTAMP",  "LOCK",  "LONG",
                  "LONGBLOB",  "LONGTEXT",  "LOW_PRIORITY",
                  "MASTER_SERVER_ID",  "MATCH",  "MEDIUMBLOB",
                  "MEDIUMINT",  "MEDIUMTEXT",  "MIDDLEINT ",
                  "MINUTE_SECOND",  "MOD",  "MRG_MYISAM",
                  "NATURAL",  "NOT",  "NULL",
                  "NUMERIC",  "ON",  "OPTIMIZE",
                  "OPTION",  "OPTIONALLY",  "OR",
                  "ORDER",  "OUTER",  "OUTFILE",
                  "PRECISION",  "PRIMARY",  "PRIVILEGES",
                  "PROCEDURE",  "PURGE",  "READ",
                  "REAL",  "REFERENCES",  "REGEXP",
                  "RENAME",  "REPLACE",  "REQUIRE",
                  "RESTRICT",  "RETURNS",  "REVOKE",
                  "RIGHT",  "RLIKE",  "RTREE",
                  "SELECT",  "SET",  "SHOW",
                  "SMALLINT",  "SOME",  "SONAME",
                  "SPATIAL",  "SQL_BIG_RESULT",  "SQL_CALC_FOUND_ROWS ",
                  "SQL_SMALL_RESULT",  "SSL",  "STARTING ",
                  "STRAIGHT_JOIN",  "STRIPED",  "TABLE ",
                  "TABLES",  "TERMINATED",  "THEN",
                  "TINYBLOB",  "TINYINT",  "TINYTEXT ",
                  "TO",  "TRAILING",  "TRUE",
                  "TYPES",  "UNION",  "UNIQUE",
                  "UNLOCK",  "UNSIGNED",  "UPDATE",
                  "USAGE",  "USE",  "USER_RESOURCES ",
                  "USING",  "VALUES",  "VARBINARY",
                  "VARCHAR",  "VARCHARACTER",  "VARYING ",
                  "WARNINGS",  "WHEN",  "WHERE",
                  "WITH",  "WRITE",  "XOR",
                  "YEAR_MONTH",  "ZEROFILL", "OTHER"
        };
    }

    public MySQLSQLGenerator() {
        super();
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
        str += "CREATE TABLE `" + tableName + "` (";
        str += "`flowId` int(11) NOT NULL default '-1',";
        str += "`flowTypeCode` varchar(20) NOT NULL,";
        str += "`id` int(10) unsigned NOT NULL auto_increment,";
        str += "`cws_creator` varchar(20),";
        str += "`cws_id` varchar(20),";
        str += "`cws_order` int(11) NOT NULL default '0',";
        str += "`cws_parent_form` varchar(20),";
        str += "`unit_code` varchar(20),";
        // -1表示临时流程，0表示流程进行中，1表示流程结束或智能模块创建
        str += "`cws_status` TINYINT(1) NOT NULL default '-1',";    
        
        // 20161030 fgf 添加
        // 创建拉单时所关联的源表单的ID
        str += "`cws_quote_id` int(10) unsigned,";
        // 创建拉单后自动冲抵标志位
        str += "`cws_flag` TINYINT(1) NOT NULL default '0',";    
        // 创建进度字段
        str += "`cws_progress` INTEGER UNSIGNED NOT NULL DEFAULT 0,";
        // 创建创建时间字段
        str += "`cws_create_date` datetime,";
        // 创建修改时间字段
        str += "`cws_modify_date` datetime,";
        // 创建流程结束时间字段
        str += "`cws_finish_date` datetime,";

        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }
        str += "KEY (`flowId`),"; // 2006.6.25 考虑到visual智能设计的时候flowId为-1可以重复的问题，flowId因此不能作为主键
        str += "KEY `flowTypeCode` (`flowTypeCode`),";
        str += "KEY `cwsCreator` (`cws_creator`),";
        str += "KEY `cws_parent_form` (`cws_parent_form`),";
        str += "KEY `unit_code` (`unit_code`),";
        str += "KEY `cwsIdOrder` (`cws_id`, `cws_order`),";
        str += "KEY `cwsQuoteId` (`cws_quote_id`),";
        str += "KEY `cwsFlag` (`cws_flag`),";
        str += "KEY `cwsStatus` (`cws_status`),";
        str += "KEY `cws_id` (`cws_id`),";
        str += "PRIMARY KEY `id` (`id`)";
        str += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        // System.out.println("generateCreateStr:" + str);
        v.addElement(str);
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
        str += "CREATE TABLE `" + getTableNameForLog(tableName) + "` (";
        str += "`flowId` int(11) NOT NULL default '0',";
        str += "`flowTypeCode` varchar(20) NOT NULL,";
        str += "`id` int(10) unsigned NOT NULL auto_increment,";
        
        str += "`cws_log_user` varchar(20),";
        str += "`cws_log_type` TINYINT(1) unsigned NOT NULL default '0',";
        str += "`cws_log_date` DATETIME NOT NULL,";
        str += "`cws_log_id` int(10) unsigned NOT NULL default '0',";
        
        str += "`cws_creator` varchar(20),";
        str += "`cws_id` varchar(20),";
        str += "`cws_order` int(11) NOT NULL default '0',";
        str += "`unit_code` varchar(20),";
        
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }
        str += "KEY (`flowId`),"; // 2006.6.25 考虑到visual智能设计的时候flowId为-1可以重复的问题，flowId因此不能作为主键
        str += "KEY `flowTypeCode` (`flowTypeCode`),";
        str += "KEY `cwsCreator` (`cws_creator`),";
        str += "KEY `unit_code` (`unit_code`),";
        str += "KEY `cwsIdOrder` (`cws_id`, `cws_order`),";
        str += "KEY `cws_log_type` (`cws_log_type`),";
        str += "KEY `cws_log_id` (`cws_log_id`),";
        str += "PRIMARY KEY `id` (`id`)";
        str += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        // System.out.println("generateCreateStr:" + str);
        v.addElement(str);
        return v;
    }    
    
    /**
     * 判断log表格是否存在
     * @param tableName
     * @return
     */
    public boolean isTableForLogExist(String tableName) {
    	String sql = "show tables like '" + getTableNameForLog(tableName) + "'";
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
    

    /**
     * 创建修改表单中字段SQL语句，注意只能增加和删除字段，不能对字段的名称、类型、默认值等作修改
     * 20171209 fgf 改为支持对名称及默认值的修改，数据类型在JSP中已经限制了不可修改，所以不用考虑
     * @param vt Vector[]
     * @return String
     */
    public Vector generateModifyStr(String tableName, Vector[] vt) {
        Vector v = new Vector();

        String str = "ALTER TABLE `" + tableName + "`";
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        String delstr = "";
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            if (delstr.equals(""))
                delstr += " DROP COLUMN `" + delff.getName() + "`";
            else
                delstr += ",DROP COLUMN `" + delff.getName() + "`";
        }
        Vector addv = vt[1];
        // System.out.println("generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField) ir.next();
            // System.out.println("generateModifyStr field name=" + addff.getName());
            if (addstr.equals(""))
                addstr += " ADD COLUMN " + toStrForCreate(addff);
            else
                addstr += ",ADD COLUMN " + toStrForCreate(addff);
        }
        if (!delstr.equals("")) {
            if (!addstr.equals(""))
                addstr = "," + addstr;
        }

        // ALTER TABLE `test`.`ff` DROP COLUMN `name`, ADD COLUMN `con` VARCHAR(45) NOT NULL AFTER `title`;
        // if (addstr.equals("") && delstr.equals(""))
        //    return v;
        
        Vector remainV = vt[2];
        ir = remainV.iterator();
        String remainStr = "";
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if ("".equals(remainStr)) {
            	remainStr = " MODIFY COLUMN " + toStrForCreate(ff);
            }
            else {
            	remainStr += ", MODIFY COLUMN " + toStrForCreate(ff);
            }
        	// ALTER TABLE `sip`.`form_table_bmyssp` MODIFY COLUMN `status2` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 123445 COMMENT '状态2567';
        }
        if (!delstr.equals("") || !addstr.equals("")) {
        	if (!"".equals(remainStr)) {
        		remainStr = "," + remainStr;
        	}
        }
        
        str = str + delstr + addstr + remainStr;
        
        LogUtil.getLog(getClass()).info("generateModifyStr:" + str);
        v.addElement(str);
        return v;
    }
    

    /**
     * 创建修改表单中字段SQL语句，注意只能增加和删除字段，不能对字段的名称、类型、默认值等作修改
     * @param vt Vector[]
     * @return String
     */
    public Vector generateModifyStrForLog(String tableName, Vector[] vt) {
        Vector v = new Vector();

        String str = "ALTER TABLE `" + getTableNameForLog(tableName) + "`";
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        String delstr = "";
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            if (delstr.equals(""))
                delstr += " DROP COLUMN `" + delff.getName() + "`";
            else
                delstr += ",DROP COLUMN `" + delff.getName() + "`";
        }
        Vector addv = vt[1];
        // System.out.println("generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField) ir.next();
            // System.out.println("generateModifyStr field name=" + addff.getName());
            if (addstr.equals(""))
                addstr += " ADD COLUMN " + toStrForCreate(addff);
            else
                addstr += ",ADD COLUMN " + toStrForCreate(addff);
        }
        if (!delstr.equals("")) {
            if (!addstr.equals(""))
                addstr = "," + addstr;
        }

        // ALTER TABLE `test`.`ff` DROP COLUMN `name`, ADD COLUMN `con` VARCHAR(45) NOT NULL AFTER `title`;
        if (addstr.equals("") && delstr.equals(""))
            return v;
        str = str + delstr + addstr;
        LogUtil.getLog(getClass()).info("generateModifyStr:" + str);
        v.addElement(str);
        return v;
    }    

    public Vector generateDropTable(String tableName) {
        Vector v = new Vector();
        v.addElement("DROP TABLE IF EXISTS " + "`" +
                     tableName + "`");
        return v;
    }
    
    public Vector generateDropTableForLog(String tableName) {
        Vector v = new Vector();
        v.addElement("DROP TABLE IF EXISTS " + "`" +
                     getTableNameForLog(tableName) + "`");
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

    /**
     * 得到创建字段的SQL语句
     * 20070425
     * MYSQL的极限长度 innodb表中的varchar()合计长度不能超过65535，因此将varchar(250)改为varchar(100)
     * ERROR http-80-Processor23 com.redmoon.oa.flow.FormDb - create:Row size too large. The maximum row size for the used table type, not counting BLOBs, is 65535. You have to change some columns to TEXT or BLOBs. Now transaction rollback
     * @return String
     */
    public String toStrForCreate(FormField ff) {
        String typeStr = "";
        MacroCtlUnit mu = null;
         if (ff.getType().equals(FormField.TYPE_MACRO)) {
             MacroCtlMgr mm = new MacroCtlMgr();
             mu = mm.getMacroCtlUnit(ff.getMacroType());
             if (mu==null)
                 throw new IllegalArgumentException("Macro ctl type=" + ff.getMacroType() + " is not exist.");
             typeStr = mu.getFieldType();
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR) {
             typeStr = "varchar(100)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_INT) {
             typeStr = "INTEGER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_LONG) {
             typeStr = "BIGINT";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_FLOAT) {
             typeStr = "FLOAT";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE) {
             typeStr = "DOUBLE";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
             typeStr = "DOUBLE";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_BOOLEAN) {
             typeStr = "char(1)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {
             typeStr = "text";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
             typeStr = "date";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
             typeStr = "datetime";
         }
         else
             typeStr = "varchar(100)";
         
         if (ff.getType().equals(FormField.TYPE_BUTTON)) {
             typeStr = "varchar(300)";
         }

         String defaultStr = StrUtil.sqlstr("");
         if (ff.getDefaultValue() != null) {
             // System.out.println("FormField.java defaultValue=" + defaultValue +
             //                   " type=" + type);
             if (ff.getType().equals(FormField.TYPE_DATE)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT) || ff.getDefaultValue().equals("")) {
                     defaultStr = StrUtil.sqlstr("0000-00-00");
                 }
                 else {
                     defaultStr = StrUtil.sqlstr(ff.getDefaultValue());
                 }
             } else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT) || ff.getDefaultValue().equals(""))
                     defaultStr = StrUtil.sqlstr("0000-00-00 00:00:00");
                 else
                     defaultStr = StrUtil.sqlstr(ff.getDefaultValue());
             } else
                 defaultStr = StrUtil.sqlstr(ff.getDefaultValue());
         }
         String str = "";
         if (!typeStr.equals("text")) {
             if (defaultStr.equals("''")) {
                 // 在MySQL5.1中，不能用 ... default '' ...的方式，而MySQL4.1可以这样用
                 str = "`" + ff.getName() + "` " + typeStr +
                       " COMMENT " + StrUtil.sqlstr(ff.getTitle());
             }
             else {
                 if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                     str = "`" + ff.getName() + "` " + typeStr +
                       " COMMENT " + StrUtil.sqlstr(ff.getTitle());
                 }
                 else {
                     str = "`" + ff.getName() + "` " + typeStr + " default " +
                           defaultStr +
                           " COMMENT " + StrUtil.sqlstr(ff.getTitle());
                 }
             }
         }
         else
             str = "`" + ff.getName() + "` " + typeStr + " COMMENT " +
                   StrUtil.sqlstr(ff.getTitle());

         LogUtil.getLog(getClass()).info("toStrForCreate str=" + str);
         // System.out.println(getClass() + " toStrForCreate name=" + name + " fieldType=" + fieldType);
         // System.out.println(getClass() + " toStrForCreate:" + str);
        return str;
    }

    public String getTableColumnsFromDbSql(String tableName) {
        return "select * from " +
                    tableName + " limit 1";
    }
}
