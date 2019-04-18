package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
/**
*
* <p>Title: 适用与postgresql数据库</p>
*
* <p>Description: </p>
*
* <p>2015-01-15</p>
*
* <p>Company: yimihome</p>
*
* @author jfy
* @version 1.0
*/
public class PostGreSQLGenerator implements ISQLGenerator {
	public static final String[] keywords;
    static {
        keywords = new String[] {
                   "ALL",  "ANALYSE",  "ANALYZE" ,"AND",
                  "ANY", "ARRAY", "AS" ,"ASC","ASYMMETRIC","AUTHORIZATION",
                  "BINARY",   "BOTH", "CASE",  "CAST", "CHECK" ,"COLLATE","COLLATION","COLUMN",
                  "CONCURRENTLY", "CONSTRAINT" ,  "CREATE",  "CROSS",  "CURRENT_CATALOG",
                  "CURRENT_DATE",  "CURRENT_ROLE", "CURRENT_SCHEMA",
                  "CURRENT_TIME", "CURRENT_TIMESTAMP",  "CURRENT_USER",
                  "DEFAULT",  "DEFERRABLE",  "DESC",
                  "DISTINCT",  "DO",  "ELSE",
                  "END",  "EXCEPT",  "FALSE",
                  "FETCH",  "FOR",  "FOREIGN",
                  "FREEZE",  "FROM",  "FULL",
                  "GRANT",  "GROUP",  "HAVING",
                  "ILIKE",  "IN",  "INITIALLY",
                  "INNER",  "INTERSECT",  "INTO",
                  "IS",  "ISNULL",  "JOIN",
                  "LEADING",  "LEFT",  "LIKE",
                  "LIMIT",  "LOCALTIME",  "LOCALTIMESTAMP",
                  "NATURAL",  "NOT",  "NOTNULL",
                  "NULL",  "OFFSET",  "ON",
                  "ONLY",  "OR",  "ORDER",
                  "OUTER",  "OVER",  "OVERLAPS",
                  "PLACING",  "PRIMARY",  "REFERENCES",
                  "RETURNING",  "RIGHT",  "SELECT",
                  "SESSION_USER",  "SIMILAR",  "SOME",
                  "SYMMETRIC",  "TABLE",  "THEN",
                  "TO",  "TRAILING",  "TRUE",
                  "UNION",  "UNIQUE",  "USER",
                  "USING",  "VARIADIC",  "VERBOSE",
                  "WHEN",  "WHERE",  "WINDOW",
                  "WITH", "OTHER"
        };
    }
    
    public PostGreSQLGenerator() {
        super();
    }
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector generateCreateStr(String tableName, Vector fields) {
		Vector v = new Vector();
        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";      
        
        str = "CREATE TABLE " + tableName + " (";
        str += "flowId integer  NOT NULL default -1,";
        str += "flowTypeCode character varying(20) NOT NULL,";
        str += "id bigserial NOT NULL ,";
        str += "cws_creator character varying(20),";
        str += "cws_id character varying(20),";
        str += "cws_order integer NOT NULL DEFAULT 0,";
        str += "unit_code character varying(20),";
        str += "cws_parent_form varying(20),";                
        // -1表示临时流程，0表示流程进行中，1表示流程结束或智能模块创建
        str += "cws_status smallint NOT NULL DEFAULT (-1),";        
        
        // 创建拉单时所关联的源表单的ID
        str += "cws_quote_id integer,";
        // 创建拉单后自动冲抵标志位
        str += "cws_flag smallint NOT NULL DEFAULT (0),"; 
        // 创建进度字段
        str += "cws_progress INTEGER NOT NULL DEFAULT (0),";
        
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }      
        str += " CONSTRAINT "+ tableName +"_id PRIMARY KEY (id)";
        str += " ) WITH (  OIDS=FALSE);";
        v.addElement(str);
        
        // 创建索引
        str = "CREATE INDEX " + tableName + "_cws_creator";
        str += " ON " + tableName;
        str += " USING btree  (cws_creator);";
        v.addElement(str);
        
        str = "CREATE INDEX " + tableName + "_cws_idcws_order";
        str += " ON " + tableName;
        str += " USING btree  (cws_id, cws_order);";
        v.addElement(str);
        
        str = "CREATE INDEX " + tableName + "_flowid";
        str += " ON " + tableName;
        str += " USING btree  (flowid);";
        v.addElement(str);
        
        str = "CREATE INDEX " + tableName + "_flowtypecode";
        str += " ON " + tableName;
        str += " USING btree  (flowtypecode);";
        v.addElement(str);
        
        str = "CREATE INDEX " + tableName + "_unit_code";
        str += " ON " + tableName;
        str += " USING btree  (unit_code);";
        v.addElement(str);
        
        str = "CREATE INDEX " + tableName + "_cws_parent_form";
        str += " ON " + tableName;
        str += " USING btree  (cws_parent_form);";
        v.addElement(str);        
        
        str = "CREATE INDEX " + tableName + "_cwsquoteid";
        str += " ON " + tableName;
        str += " USING btree  (cws_quote_id);";
        v.addElement(str);
        str = "CREATE INDEX " + tableName + "_cwsflag";
        str += " ON " + tableName;
        str += " USING btree  (cws_flag);";
        v.addElement(str);
        
        return v;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector generateModifyStr(String tableName, Vector[] vt) {
		Vector v = new Vector();
        String altstr = "ALTER TABLE " + tableName;
        //删除字段
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        String delstr = "";
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            delstr = altstr + " DROP COLUMN " + delff.getName();
            v.addElement(delstr );
        }        
        //新增字段
        Vector addv = vt[1];
        LogUtil.getLog(getClass()).info("FormDb generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField)ir.next();
            LogUtil.getLog(getClass()).info("FormDb generateModifyStr ADD field name=" + addff.getName());
            addstr = altstr + " ADD COLUMN " + toStrForCreate(addff);
            v.addElement(addstr);
        }
        return v;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Vector generateCreateStrForLog(String tableName, Vector fields) {
		Vector v = new Vector();
        if (fields == null)
            return v;
        Iterator ir = fields.iterator();
        String str = "";
        if (isTableForLogExist(tableName)){
        	generateDropTableForLog(tableName);
        }
        str += "CREATE TABLE " +getTableNameForLog(tableName) + " (";
        str += "flowId integer  NOT NULL default 0,";
        str += "flowTypeCode character varying(20) NOT NULL,"; 
        str += "id bigserial NOT NULL ,";
        str += "cws_creator character varying(20),";
        str += "cws_id character varying(20),";
        str += "cws_order integer NOT NULL DEFAULT 0,";
        str += "unit_code character varying(20),";
        // -1表示临时流程，0表示流程进行中，1表示流程结束或智能模块创建
        str += "cws_status smallint  NOT NULL DEFAULT (-1),";        
        
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            str += toStrForCreate(ff) + ",";
        }      
        str += " CONSTRAINT "+ getTableNameForLog(tableName) +"_id PRIMARY KEY (id)";
        str += " ) WITH (  OIDS=FALSE);";
        // System.out.println("generateCreateStr:" + str);
        v.addElement(str);
        // 创建索引
        str = "CREATE INDEX " + getTableNameForLog(tableName) + "_cws_creator";
        str += " ON " + getTableNameForLog(tableName);
        str += " USING btree  (cws_creator);";
        v.addElement(str);
        
        str = "CREATE INDEX " + getTableNameForLog(tableName) + "_cws_idcws_order";
        str += " ON " + getTableNameForLog(tableName);
        str += " USING btree  (cws_id, cws_order);";
        v.addElement(str);
        
        str = "CREATE INDEX " + getTableNameForLog(tableName) + "_flowid";
        str += " ON " + getTableNameForLog(tableName);
        str += " USING btree  (flowid);";
        v.addElement(str);
        
        str = "CREATE INDEX " + getTableNameForLog(tableName) + "_flowtypecode";
        str += " ON " + getTableNameForLog(tableName);
        str += " USING btree  (flowtypecode);";
        v.addElement(str);
        
        str = "CREATE INDEX " + getTableNameForLog(tableName) + "_unit_code";
        str += " ON " + getTableNameForLog(tableName);
        str += " USING btree  (unit_code);";
        v.addElement(str);
        
        return v;
	}
	public String getTableNameForLog(String tableName) {
    	return tableName + "_log";
    }
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector generateModifyStrForLog(String tableName, Vector[] vt) {
		Vector v = new Vector();
        String altstr = "ALTER TABLE " +getTableNameForLog( tableName);
        //删除字段
        Vector delv = vt[0];
        Iterator ir = delv.iterator();
        String delstr = "";
        while (ir.hasNext()) {
            FormField delff = (FormField) ir.next();
            delstr = altstr + " DROP COLUMN " + delff.getName();
            v.addElement(delstr );
        }        
        //新增字段
        Vector addv = vt[1];
        LogUtil.getLog(getClass()).info("FormDb generateModifyStr vt[1].size=" + vt[1].size());
        ir = addv.iterator();
        String addstr = "";
        while (ir.hasNext()) {
            FormField addff = (FormField)ir.next();
            LogUtil.getLog(getClass()).info("FormDb generateModifyStr ADD field name=" + addff.getName());
            addstr = altstr + " ADD COLUMN " + toStrForCreate(addff);
            v.addElement(addstr);
        }
        return v;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Vector generateDropTableForLog(String tableName) {
		  Vector v = new Vector();
		  if (isTableForLogExist(tableName)){
			  v.addElement("DROP TABLE " + getTableNameForLog( tableName));
		  }
       	  return v;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Vector generateDropTable(String tableName) {
		  Vector v = new Vector();
		  if (isTableExist(tableName)){
			  v.addElement("DROP TABLE " +  tableName);
		  }
     	  return v;
	}

	@Override
	public boolean isFieldKeywords(String field) {
		 int len = keywords.length;
	        for (int i=0; i<len; i++) {
	            if (field.equalsIgnoreCase(keywords[i]))
	                return true;
	        }
	        return false;
	}

	@Override
	public String toStrForCreate(FormField ff) {
		 String typeStr = "";
         if (ff.getType().equals(FormField.TYPE_MACRO)) {
             MacroCtlMgr mm = new MacroCtlMgr();
             MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
             if (mu==null)
                 throw new IllegalArgumentException("Macro ctl type=" + ff.getMacroType() + " is not exist.");
             typeStr = mu.getFieldType();
             if (typeStr.startsWith("varchar"))
            	 typeStr = "CHARACTER VARYING(250)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR) {
             typeStr = "CHARACTER VARYING(250)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_INT) {
             typeStr = "INTEGER";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_LONG) {
             typeStr = "BIGINT";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_FLOAT) {
             typeStr = "NUMERIC";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE) {
             typeStr = "DOUBLE PRECISION";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
             typeStr = "DOUBLE PRECISION";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_BOOLEAN) {
             typeStr = "CHAR(1)";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {
             typeStr = "TEXT";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
             typeStr = "DATE";
         }
         else if (ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
             typeStr = "TIMESTAMP";
         }
         else
             typeStr = "CHARACTER VARYING(250)";

         String defaultStr = null;
         if (ff.getDefaultValue()!=null) {
             // System.out.println("FormField defaultValue=" + defaultValue + " type=" + type);
        	 //System.out.println("type : " + ff.getType());
             if (ff.getType().equalsIgnoreCase(FormField.TYPE_DATE)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT)){
                     defaultStr = "current_date"; // oracle 不需要 ' '
                     //System.out.println("current_date : " + ff.getDefaultValue());
                 }
             } else if (ff.getType().equalsIgnoreCase(FormField.TYPE_DATE_TIME)) {
                 if (ff.getDefaultValue().equals(FormField.DATE_CURRENT))
                     defaultStr = "CURRENT_TIMESTAMP(0) :: TIMESTAMP WITHOUT TIME ZONE";
             }
             else
                 defaultStr = StrUtil.sqlstr(ff.getDefaultValue()); // oracle 不需要 ' '
         }
        

        String str = "";
       // System.out.println("defaultStr : " + defaultStr);
        if (defaultStr != null && !defaultStr.equals("''")){
        	
        	str = ff.getName() + " " + typeStr + " DEFAULT " + defaultStr;
        } else{
        	//System.out.println("defaultStr : null" );
        	str = ff.getName() + " " + typeStr ;
        }
        return str;
	}

	@Override
	public String getTableColumnsFromDbSql(String tableName) {
		return "select * from " +
                tableName + " limit 1";
	}

	@Override
	public boolean isTableForLogExist(String tableName) {
		String sql = "select count(*) from pg_class where relname = '" + getTableNameForLog(tableName) + "'";
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
		}finally{
			jt.close();
		}
    	
    	return false;
	}
	/**
	 * 查看表是否存在
	 * @param tableName
	 * @return
	 */
	public boolean isTableExist(String tableName) {
		String sql = "select count(*) from pg_class where relname = '" + tableName + "'";
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
		}finally{
			jt.close();
		}
    	
    	return false;
	}
}
