package com.redmoon.oa.flow;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import javax.servlet.http.HttpServletRequest;

public class FormDb extends ObjectDb {
	
    String connname;
    
    private boolean log = false;

    private FormParser formParser;

    public FormDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("FormDb:默认数据库名为空！");
        isInitFromConfigDB = false;
        init();
    }

    public FormDb(String code) {
        this.code = code;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("FormDb:默认数据库名为空！");
        load();
        init();
    }

    public void initDB() {
        objectCache = new FormCache(this);
        tableName = "form";
        primaryKey = new PrimaryKey("code", PrimaryKey.TYPE_STRING);
        isInitFromConfigDB = false;
        QUERY_LOAD =
                "SELECT code, name, content, isSystem, flowTypeCode,isFlow,has_attachment,unit_code,ie_version,is_log,is_new_dtctl,is_progress,is_only_camera,view_setup,check_setup FROM " + tableName +
                " WHERE code=?";
        QUERY_SAVE =
                "update " + tableName + " set name=?,content=?,flowTypeCode=?,has_attachment=?,ie_version=?,is_log=?,unit_code=?,is_new_dtctl=?,is_progress=?,is_only_camera=?,view_setup=?,isFlow=?,check_setup=? where code=?";
        QUERY_DEL = "delete from " + tableName + " where code=?";
        QUERY_CREATE = "insert into " + tableName +
                       " (code,name,content,orders,flowTypeCode,isFlow,has_attachment,unit_code,ie_version,is_log,is_new_dtctl,is_progress,is_only_camera) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_LIST = "select code from " + tableName + " order by orders asc";
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        orders = getMaxOrders();
        boolean re = false;
        try {
            // 解析content，在表form_field中建立相应的域
            // FormParser fp = new FormParser(content);
            // Vector v = fp.getFields();

            Vector v = formParser.getFields();
            // 检查其中的fields是否合法
            formParser.validateFields();

            conn = new Conn(connname);
            conn.beginTrans();
            try {
                // 由表单的域来创建表格，删除表或者创建表的SQL语句会使得当表单中有重复的列名时，至使事务回滚失败！
                Vector vt = SQLGeneratorFactory.getSQLGenerator().
                            generateDropTable(getTableNameByForm());
                Iterator ir = vt.iterator();
                while (ir.hasNext()) {
                    String sql = (String) ir.next();
                    LogUtil.getLog(getClass()).debug("create1: sql=" + sql);
                    conn.executeUpdate(sql);
                }
                // conn.executeUpdate(SQLGeneratorFactory.getSQLGenerator().
                //                   generateDropTable(getTableNameByForm()));
            }
            catch (Exception e) {
                // 当用SQLServer数据库时，如果表不存在，会报异常
                LogUtil.getLog(getClass()).error("create:" + StrUtil.trace(e));
            }
            
            String sql = "";
            Vector vt = generateCreateStr(v);
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                sql = (String)ir.next();
                logger.info("create2: sql=" + sql);
                conn.executeUpdate(sql);
            }
            
            vt = SQLGeneratorFactory.getSQLGenerator().generateCreateStrForLog(getTableName(code), v);
            ir = vt.iterator();
            while (ir.hasNext()) {
                sql = (String)ir.next();
                conn.executeUpdate(sql);
            }            

            sql = "insert into form_field (formCode, name, title, type, macroType, defaultValue, fieldType, canNull, fieldRule, description, is_func, css_width, is_readonly,present) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // 增加新的表单域
            ir = v.iterator();
            PreparedStatement ps = conn.prepareStatement(sql);
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                ps.setString(1, code);
                ps.setString(2, ff.getName());
                ps.setString(3, ff.getTitle());
                ps.setString(4, ff.getType());
                ps.setString(5, ff.getMacroType());
                if (ff.isFunc() || FormField.TYPE_DATE.equals(ff.getType()) || FormField.TYPE_DATE_TIME.equals(ff.getType())){
                	ps.setString(6, ff.getDefaultValueRaw());
                }
                else{
                	ps.setString(6, ff.getDefaultValue());
                }
                
                ps.setInt(7, ff.getFieldType()); // 数据类型
                ps.setInt(8, ff.isCanNull()?1:0);
                ps.setString(9, ff.getRule());
                ps.setString(10, ff.getDescription());
                ps.setInt(11, ff.isFunc()?1:0);
                ps.setString(12, ff.getCssWidth());
                ps.setInt(13, ff.isReadonly()?1:0);
                ps.setString(14, ff.getPresent());
                LogUtil.getLog(getClass()).info("create:" + ff.getName() + "--" + ff.getDefaultValue() +
                                   "--" + ff.getDefaultValueRaw() + " canNull=" + ff.isCanNull() + " rule=" + ff.getRule());
                // System.out.println("FormDb save:" + ff.getName() + "=" + ff.getDefaultValue());
                conn.executePreUpdate();
            }

            if (ps != null) {
                ps.close();
                ps = null;
            }

            ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, code);
            ps.setString(2, name);

            // 新时间控件
            isNewDtctl = content.toLowerCase().indexOf("isnewdatetimectl=\"true\"") > -1;
            
            // 依旧有旧时间控件,新旧可以一起使用
//            if (isNewDtctl && content.indexOf("SelectDate(") > -1) {
//            	throw new ErrMsgException("请统一使用新版的时间控件！");
//            }

            content = content.trim();
            if (content.equals("")) {
            	throw new ErrMsgException("表单不能为空！");
            }
            
            ps.setString(3, content);
            ps.setInt(4, orders + 1);
            ps.setString(5, flowTypeCode);
            ps.setInt(6, flow?1:0);
            ps.setInt(7, hasAttachment?1:0);
            ps.setString(8, unitCode);
            ps.setString(9, ieVersion);
			ps.setInt(10, log ? 1 : 0);
			ps.setInt(11, isNewDtctl ? 1 : 0);
			ps.setInt(12, progress?1:0);
			ps.setInt(13, onlyCamera?1:0);
            re = conn.executePreUpdate() == 1 ? true : false;

            conn.commit();

            if (re) {
                FormCache mc = new FormCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + StrUtil.trace(e) +
                         ". Now transaction rollback");
            conn.rollback();
            throw new ErrMsgException("插入时出错！");
        } catch (Exception e) {
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FormDb(pk.getStrValue());
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setString(1, code);
            conn.beginTrans();
            re = conn.executePreUpdate() > 0 ? true : false;

            if (re) {
                FormCache mc = new FormCache(this);
                primaryKey.setValue(code);
                mc.refreshDel(primaryKey);

                // 删除数据form_field表中的域
                String sql =
                        "delete from form_field where formCode=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, code);
                conn.executePreUpdate();
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }

                // 删除主副模块列表设置及权限设置
                ModulePrivDb mpd = new ModulePrivDb();
                ModuleSetupDb msd = new ModuleSetupDb();
                sql = "select code from visual_module_setup where form_code=?";
                Iterator ir = msd.list(sql, new Object[]{code}).iterator();
                while (ir.hasNext()) {
                	msd = (ModuleSetupDb)ir.next();
                    try {
                        msd.del();
                    }
                    catch(ResKeyException e) {
                        LogUtil.getLog(getClass()).error("del2:" + StrUtil.trace(e));
                    }

                    // 删除模块权限
                    mpd.delPrivsOfModule(code);
                }

                // 删除关联记录
                ModuleRelateDb mrd = new ModuleRelateDb();
                ir = mrd.getModulesRelated(code).iterator();
                while (ir.hasNext()) {
                    mrd = (ModuleRelateDb)ir.next();
                    try {
                        mrd.del();
                    } catch (ResKeyException ex) {
                        ex.printStackTrace();
                    }
                }

                // 删除数据库中对应的表
                try {
                    Vector vt = SQLGeneratorFactory.getSQLGenerator().
                                generateDropTable(getTableNameByForm());
                    ir = vt.iterator();
                    while (ir.hasNext()) {
                        sql = (String) ir.next();
                        conn.executeUpdate(sql);
                    }
                }
                catch (SQLException e) {
                    // 当用SQLServer数据库时，如果表不存在，会报异常
                    LogUtil.getLog(getClass()).error("del:" + StrUtil.trace(e));
                }
                
                try {
                    Vector vt = SQLGeneratorFactory.getSQLGenerator().
                                generateDropTableForLog(getTableNameByForm());
                    ir = vt.iterator();
                    while (ir.hasNext()) {
                        sql = (String) ir.next();
                        conn.executeUpdate(sql);
                    }
                }
                catch (SQLException e) {
                    // 当用SQLServer数据库时，如果表不存在，会报异常
                    LogUtil.getLog(getClass()).error("delOfLog:" + StrUtil.trace(e));
                }                
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /*
        public boolean save() {
            Conn conn = new Conn(connname);
            PreparedStatement pstmt = null;
            boolean re = false;
            try {
                conn.beginTrans();
                pstmt = conn.prepareStatement(QUERY_SAVE);
                pstmt.setString(1, name);
                pstmt.setString(2, content);
                pstmt.setString(3, code);
                re = conn.executePreUpdate()>0?true:false;
                if (re) {
                    // 解析content，在表form_field中建立相应的域
                    FormParser fp = new FormParser(content);
                    Vector v = fp.getFields();
                    // 删除原来的表单域
                    String sql = "delete from form_field where formCode=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, code);
                    conn.executePreUpdate();
                    if (ps!=null) {
                        ps.close();
                        ps = null;
                    }
                    if (ps!=null) {
                        ps.close();
                        ps = null;
                    }

                    sql = "insert into form_field (formCode, name, title, type, isMacro) values (?,?,?,?,?)";
                    // 增加新的表单域
                    Iterator ir = v.iterator();
                    ps = conn.prepareStatement(sql);
                    while (ir.hasNext()) {
                        FormField ff = (FormField)ir.next();
                        ps.setString(1, code);
                        ps.setString(2, ff.getName());
                        ps.setString(3, ff.getTitle());
                        ps.setString(4, ff.getType());
                        ps.setInt(5, ff.isMacro()?1:0);
                        conn.executePreUpdate();
                    }
                    conn.commit();
                    FormCache mc = new FormCache(this);
                    primaryKey.setValue(code);
                    mc.refreshSave(primaryKey);
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.error("save:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            return re;
        }
     */

    /**
     * 取得表单对应的表名
     * @param code String
     * @return String
     */
    public static String getTableName(String code) {
        return "form_table_" + code;
    }
    
    public static String getTableNameForLog(String code) {
    	return getTableName(code) + "_log";
    }    
    
    /**
     * 取得表格名称对应的表单编码
     * @param tableName
     * @return 如果表格名称不是以form_table_开头，则返回null
     */
    public static String getCodeByTableName(String tableName) {
    	tableName = tableName.toUpperCase();
    	if (tableName.startsWith("FORM_TABLE_"))
    		return tableName.substring(11);
    	else
    		return null;
    }

    /**
     * 取得表单对应的表名，已被getTableName取代
     * @deprecated
     * @return String
     */
    public String getTableNameByForm() {
        return getTableName(code);
    }

    /**
     * 对比新域和原来的域，找出将来添加的新域和将要删除的旧的域
     * @param oldFields Vector 原来的表单域（来自于数据库中的字段）
     * @param newFields Vector
     * @param fields Vector 原来的表单域
     * @return Vector[]
     */
    public static Vector[] checkFieldChange(Vector oldFields, Vector newFields, Vector fields) {
    	// oldFields是从数据库中取出的，只含有字段，无其它信息
        Vector fieldsForDel = new Vector();
        Vector fieldsForAdd = new Vector();
        int oldlen = oldFields.size();
        int newlen = newFields.size();
        // 找出新域中新增的字段
        for (int i = 0; i < newlen; i++) {
            boolean isFinded = false;
            FormField nff = (FormField) newFields.get(i);
            for (int j = 0; j < oldlen; j++) {
                FormField off = (FormField) oldFields.get(j);
                if (off.getName().equalsIgnoreCase(nff.getName())) {
                    isFinded = true;
                    break;
                }
            }
            if (!isFinded) {
                fieldsForAdd.addElement(nff);
            }
        }
        // 修改后的表单中保留下来的字段，用于alter更新字段
        Vector fieldsForRemain = new Vector();
        // 找出旧域中被删的字段
        for (int i = 0; i < oldlen; i++) {
            boolean isFinded = false;
            FormField off = (FormField) oldFields.get(i);
            for (int j = 0; j < newlen; j++) {
                FormField nff = (FormField) newFields.get(j);
                if (off.getName().equalsIgnoreCase(nff.getName())) {
                	fieldsForRemain.addElement(nff);
                    isFinded = true;
                    break;
                }
            }
            if (!isFinded) {
                fieldsForDel.addElement(off);
            }
        }
        
        // 将保留下来的字段与原字段对比，如果默认值或者注释有变化，则alter
        Iterator ir = fieldsForRemain.iterator();
        while (ir.hasNext()) {
        	FormField ffNew = (FormField)ir.next();
        	Iterator irOld = fields.iterator();
        	while (irOld.hasNext()) {
        		FormField ffOld = (FormField)irOld.next();
        		if (ffNew.getName().equals(ffOld.getName())) {
        			if (ffNew.getDefaultValueRaw().equals(ffOld.getDefaultValueRaw())) {
        				if (ffNew.getTitle().equals(ffOld.getTitle())) {
        					// 如果字段无变化，则从fieldsForRemain中去除
        					ir.remove();
        				}
        			}
        			break;
        		}
        	}
        }
        
        Vector[] v = new Vector[3];
        v[0] = fieldsForDel;
        v[1] = fieldsForAdd;
        v[2] = fieldsForRemain;
        return v;
    }

    /**
     * 创建修改表单中字段SQL语句，注意只能增加和删除字段，不能对字段的名称、类型、默认值等作修改
     * @param vt Vector[]
     * @return String
     */
    public Vector generateModifyStr(Vector[] vt) {
        return SQLGeneratorFactory.getSQLGenerator().generateModifyStr(getTableNameByForm(), vt);
    }

    /**
     * 创建增加表单的SQL语句
     * @param fields Vector
     * @return String
     */
    public Vector generateCreateStr(Vector fields) {
        return SQLGeneratorFactory.getSQLGenerator().generateCreateStr(getTableNameByForm(), fields);
    }

    public Vector getTableColumnsFromDb() {
        Conn conn = new Conn(connname);
        ResultSet rs;
        Vector v = new Vector();
        try {
            PreparedStatement ps = conn.prepareStatement(SQLGeneratorFactory.getSQLGenerator().getTableColumnsFromDbSql(getTableNameByForm()));
            rs = conn.executePreQuery();
            ResultSetMetaData rm = rs.getMetaData();
            int colCount = rm.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                // 注意：如果字段的field为flowId或者flowTypeCode，则跳过
                String colName = rm.getColumnName(i);
                if (colName.equalsIgnoreCase("flowId") ||
                    colName.equalsIgnoreCase("flowTypeCode") ||
                    colName.equalsIgnoreCase("id") ||
                    colName.equalsIgnoreCase("cws_creator") ||
                    colName.equalsIgnoreCase("cws_id") ||
                    colName.equalsIgnoreCase("unit_code") ||
                    colName.equalsIgnoreCase("cws_status") ||
                    colName.equalsIgnoreCase("cws_quote_id") ||
                    colName.equalsIgnoreCase("cws_flag") ||
                    colName.equalsIgnoreCase("cws_progress") ||
                    colName.equalsIgnoreCase("cws_parent_form") ||
                    colName.equalsIgnoreCase("cws_order") ||
                    colName.equalsIgnoreCase("cws_create_date") ||
                    colName.equalsIgnoreCase("cws_modify_date") ||
                    colName.equalsIgnoreCase("cws_finish_date"))

                    continue;
                FormField ff = new FormField();
                ff.setName(colName);
                v.addElement(ff);
            }
        } catch (SQLException e) {
            logger.error("getTableColumnsFromDb:" + StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /*
    * 仅保存content、viewSetup、checkSetup，用于setup3.jsp中对content中图片路径处理后的保存
    */
    public boolean saveContent() {
        String sql = "update form set content=?,view_setup=?,check_setup=?,flowTypeCode=?,orders=? where code=?";
        boolean re = false;
        JdbcTemplate jt = new JdbcTemplate();
        try {
            re = jt.executeUpdate(sql, new Object[] {content, viewSetup, checkSetup, flowTypeCode, orders, code})==1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return re;
    }

    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        boolean re = false;
        try {
            // 解析content，在表form_field中建立相应的域
            // FormParser fp = new FormParser(content);
            formParser.validateFields();
            Vector newv = formParser.getFields();
            // 检查其中的fields是否合法

            Vector oldv = getTableColumnsFromDb();
            Vector[] vt = checkFieldChange(oldv, newv, fields);
            LogUtil.getLog(getClass()).info("save: oldv.size=" + oldv.size() + " newv.size=" + newv.size());

            conn.beginTrans();

            // 更新修改过的字段
            Vector v = generateModifyStr(vt);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                String sql = (String) ir.next();
                LogUtil.getLog(getClass()).info("save: v.size()=" + v.size() + " sql=" + sql);
                ps = conn.prepareStatement(sql);
                conn.executePreUpdate();
                ps.close();
                ps = null;
            }
            
            // 20170830 fgf 只能同步修改，否则如果中间设为不保留历史记录，则不能同步字段，会导致混乱
            if (true || log) {
	            // 检查原来是否已存在对应的log表，如果不存在则创建
            	ISQLGenerator is = SQLGeneratorFactory.getSQLGenerator();
	            boolean isLogExist = is.isTableForLogExist(getTableName(code));
	            if (!isLogExist) {
	            	Vector vtCreate = SQLGeneratorFactory.getSQLGenerator().generateCreateStrForLog(getTableName(code), newv);
	                ir = vtCreate.iterator();
	                while (ir.hasNext()) {
	                    String sql = (String)ir.next();
	                    logger.info("save2: sql=" + sql);
	                    conn.executeUpdate(sql);
	                }	            	
	            }
	            else {
	                v = SQLGeneratorFactory.getSQLGenerator().generateModifyStrForLog(getTableName(code), vt);
	                ir = v.iterator();
	                while (ir.hasNext()) {
	                    String sql = (String) ir.next();
	                    // LogUtil.getLog(getClass()).info("save: v.size()=" + v.size() + " sql=" + sql);
	                    ps = conn.prepareStatement(sql);
	                    conn.executePreUpdate();
	                    ps.close();
	                    ps = null;
	                }	            	
	            }
            }
            
            // 新时间控件
            isNewDtctl = content.toLowerCase().indexOf("isnewdatetimectl=\"true\"") > -1;

            ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, name);
            ps.setString(2, content.trim());
            ps.setString(3, flowTypeCode);
            ps.setInt(4, hasAttachment?1:0);
            ps.setString(5, ieVersion);
            ps.setInt(6, log?1:0);
            ps.setString(7, unitCode);
            ps.setInt(8, isNewDtctl ? 1 : 0);
            ps.setInt(9, progress?1:0);
            ps.setInt(10, onlyCamera?1:0);
            ps.setString(11, viewSetup);
            ps.setInt(12, flow?1:0);
            ps.setString(13, checkSetup);
            ps.setString(14, code);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 取得原来表单域的序号置于map中
            HashMap<String, FormField> map = new HashMap<String, FormField>();
            String sql = "select name,orders,canNull,canQuery,canList,width,is_mobile_display,is_hide,more_than,more_than_mode,present from form_field where formCode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = conn.executePreQuery();
            while (rs.next()) {
            	FormField ff = new FormField();
            	ff.setName(rs.getString(1));
            	ff.setOrders(rs.getInt(2));
            	ff.setCanNull(rs.getInt(3) == 1);
            	ff.setCanQuery(rs.getInt(4) == 1);
            	ff.setCanList(rs.getInt(5) == 1);
            	ff.setWidth(rs.getInt(6));
            	ff.setMobileDisplay(rs.getString(7).equals("1"));
            	ff.setHide(rs.getInt(8));
            	ff.setMoreThan(rs.getString(9));
            	ff.setMorethanMode(rs.getString(10));
            	ff.setPresent(StrUtil.getNullStr(rs.getString(11)));
                map.put(rs.getString(1), ff);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 删除原来的表单域
            sql = "delete from form_field where formCode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            sql = "insert into form_field (formCode, name, title, type, macroType, defaultValue, fieldType, canNull, fieldRule, orders, canQuery, canList, is_mobile_display, width, description,is_hide,more_than,more_than_mode,is_func,css_width,is_readonly,present) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // 增加新的表单域
            ir = newv.iterator();
            ps = conn.prepareStatement(sql);
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                ps.setString(1, code);
                ps.setString(2, ff.getName());
                ps.setString(3, ff.getTitle());
                ps.setString(4, ff.getType());
                ps.setString(5, ff.getMacroType());
                ps.setString(6, ff.getDefaultValueRaw());
                ps.setInt(7, ff.getFieldType());
                ps.setInt(8, ff.isCanNull()?1:0);
                ps.setString(9, ff.getRule());
                LogUtil.getLog(getClass()).info("save:" + ff.getName() + "--" +
                                                ff.getDefaultValue() +
                                                "--" + ff.getDefaultValueRaw());
                FormField o = map.get(ff.getName());
                // 如果字段原先有序号等，则赋予原来的序号等
                if (o != null) {
                    ps.setInt(10, o.getOrders());
                    ps.setInt(11, o.isCanQuery() ? 1 : 0);
                    ps.setInt(12, o.isCanList() ? 1 : 0);
                    ps.setString(13, o.isMobileDisplay() ? "1" : "0");
                    ps.setInt(14, o.getWidth());
                } else {
                    ps.setInt(10, 0);
                    ps.setInt(11, 1);
                    ps.setInt(12, 1);
                    ps.setString(13, "1");
                    ps.setInt(14, 100);
                }
                ps.setString(15, ff.getDescription());
                // System.out.println(getClass() + " " + ff.getTitle() + " " + " ff.getDescription()=" + ff.getDescription());
                if (o != null) {
                	// System.out.println(getClass() + " " + o.getName() + " " + o.getTitle() + " " + o.isHidden());
                	ps.setInt(16, o.getHide());
                }
                else {
                	ps.setInt(16, ff.getHide());                	
                }

                ps.setString(17, ff.getMoreThan());
                ps.setString(18, ff.getMorethanMode());
                ps.setInt(19, ff.isFunc()?1:0);
                ps.setString(20, ff.getCssWidth());
                ps.setInt(21, ff.isReadonly()?1:0);
                ps.setString(22, ff.getPresent());
                
                conn.executePreUpdate();
            }

            // 由表单的域来修改原来创建的表格，这样做不合理
            // conn.executeUpdate("DROP TABLE IF EXISTS " + "`" + getTableNameByForm() + "`;");
            // sql = generateCreateStr(v);

            // 不能用原来的内容来解析，因为这样会使得手工删除form_field表中的记录时，因为oldContent未变化，使得不到正确的结果
            // fp = new FormParser(oldContent);
            // Vector oldv = fp.getFields();
            // 也不能用下句的方法来比对域，因为有可能当修改表出错时，比如原来的域已记录，而因为出错，导致表中的域未更新，下次再修改时，就比对不到正确的结果
            // Vector oldv = fields;

            conn.commit();

            FormCache mc = new FormCache(this);
            primaryKey.setValue(code);
            mc.refreshSave(primaryKey);
            
        } catch (SQLException e) {
            conn.rollback();
            logger.error("save:" + StrUtil.trace(e));

            throw new ErrMsgException(e.getMessage());
        } catch (Exception e) {
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public String getFieldTitle(String fieldName) {
        if (fields == null)
            return "";
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField fd = (FormField) ir.next();
            if (fd.getName().equals(fieldName))
                return fd.getTitle();
        }
        return "";
    }

    /**
     * 根据表单域名称获取域
     * @param fieldTitle String
     * @return FormField
     */
    public FormField getFormFieldByTitle(String fieldTitle) {
        if (fields == null)
            return null;
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getTitle().equals(fieldTitle))
                return ff;
        }
        return null;
    }

    /**
     * 根据表单域编码获取域
     * @param fieldName String
     * @return FormField
     */
    public FormField getFormField(String fieldName) {
        if (fields == null)
            return null;
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getName().equalsIgnoreCase(fieldName))
                return ff;
        }
        return null;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            if (!rs.next()) {
                logger.error("load:表单 " + code +
                             " 在数据库中未找到." + StrUtil.trace(new Exception()));
            } else {
                this.code = rs.getString(1);
                this.name = rs.getString(2);
                this.content = rs.getString(3);
                this.system = rs.getInt(4) == 1 ? true : false;
                this.flowTypeCode = StrUtil.getNullStr(rs.getString(5));
                flow = rs.getInt(6)==1;
                hasAttachment = rs.getInt(7)==1;
                unitCode = rs.getString(8);
                ieVersion = StrUtil.getNullStr(rs.getString(9));
                log = rs.getInt(10)==1;
                isNewDtctl = rs.getInt(11) == 1;
                progress = rs.getInt(12)==1;
                onlyCamera = rs.getInt(13)==1;
                viewSetup = StrUtil.getNullStr(rs.getString(14));
                checkSetup = StrUtil.getNullStr(rs.getString(15));
                loaded = true;

                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }

                fields = new Vector();
                String sql =
                        "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList,orders,width,description,is_mobile_display,is_hide,more_than,more_than_mode,is_func,css_width,is_readonly,present,is_unique from form_field where formCode=? order by orders desc";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, code);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        FormField ff = new FormField();
                        ff.setFormCode(code);
                        ff.setName(rs.getString(1));
                        ff.setTitle(rs.getString(2));
                        ff.setType(rs.getString(3));
                        ff.setMacroType(rs.getString(4));
                        ff.setDefaultValue(StrUtil.getNullStr(rs.getString(5)));
                        ff.setFieldType(rs.getInt(6));
                        ff.setCanNull(rs.getInt(7)==1);
                        ff.setRule(StrUtil.getNullStr(rs.getString(8)));
                        ff.setCanQuery(rs.getInt(9)==1);
                        ff.setCanList(rs.getInt(10)==1);
                        ff.setOrders(rs.getInt(11));
                        ff.setWidth(rs.getInt(12));
                        ff.setDescription(rs.getString(13));
                        ff.setMobileDisplay(rs.getInt(14)==1);
                        ff.setHide(rs.getInt(15));
                        ff.setMoreThan(StrUtil.getNullStr(rs.getString(16)));
                        ff.setMorethanMode(StrUtil.getNullStr(rs.getString(17)));
                        ff.setFunc(rs.getInt(18)==1);
                        ff.setCssWidth(StrUtil.getNullStr(rs.getString(19)));
                        ff.setReadonly(rs.getInt(20)==1);
                        ff.setPresent(StrUtil.getNullStr(rs.getString(21)));
                        ff.setUnique(rs.getInt(22)==1);
                        fields.addElement(ff);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setFlowTypeCode(String flowTypeCode) {
        this.flowTypeCode = flowTypeCode;
    }

    public void setFlow(boolean flow) {
        this.flow = flow;
    }

    public void setHasAttachment(boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public int getOrders() {
        return orders;
    }

    public Vector getFields() {
        // 因为FormDb取自缓存时，FormDAO中使用的FormDb当同为一个formCode时，其fields为同一个，于是当重复利用FormDb时，在FormDAO的load中取值就会存入同一个Vector中，这样就会带来问题
        // return fields;
        Vector v = new Vector();
        // 2012-06-17 fgf 防止在module_field_list.jsp中ModuleRelateDb.getFormsRelatedWith获取已删除的关联表单的fields时，fields为null情况
        if (fields==null)
            return v;
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            try {
                v.addElement(ff.clone());
            } catch (Exception e) {
                logger.error("getFields:" + e.getMessage());
            }
        }
        return v;
    }

    public boolean isSystem() {
        return system;
    }

    public String getFlowTypeCode() {
        return flowTypeCode;
    }

    public boolean isFlow() {
        return flow;
    }

    public boolean isHasAttachment() {
        return hasAttachment;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public FormDb getFormDb(String typeCode) {
        return (FormDb) getObjectDb(typeCode);
    }

    public FormDb getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        FormDb bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select code from " + tableName + " where code=" +
                      StrUtil.sqlstr(code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from " + tableName + " where code=" +
                      StrUtil.sqlstr(code) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getFormDb(rr.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return bleaf;
    }

    public int getMaxOrders() {
        String GETMAXORDERS =
                "select max(orders) from " + tableName;
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("getMaxOrder:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    public boolean move(String direction) throws ErrMsgException {
        // 根据direction检查是否可以移动
        if (direction.equals("up")) {
            if (orders == 1)
                throw new ErrMsgException("该项已处在首位！");
        }
        if (direction.equals("down")) {
            if (orders == getMaxOrders())
                throw new ErrMsgException("该项已处于最后一位！");
        }

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if (direction.equals("up")) {
                String sql = "select code from " + tableName +
                             " where orders=?"; ;
                ResultSet rs = null;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, orders - 1);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    String code = rs.getString(1);
                    FormDb ldb = getFormDb(code);
                    ldb.setOrders(ldb.getOrders() + 1);
                    ldb.saveContent();
                }
                orders = orders - 1;
                re = saveContent();
            } else {

                String sql = "select code from " + tableName +
                             " where roders=?";
                ResultSet rs = null;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, orders + 1);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    String code = rs.getString(1);
                    FormDb ldb = getFormDb(code);
                    ldb.setOrders(ldb.getOrders() - 1);
                    ldb.saveContent();
                }
                orders += 1;
                re = saveContent();
            }
            re = true;

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 列出属于流程大类的表单
     * @param flowTypeCode
     * @return
     */
    public Vector listOfFlow(String flowTypeCode) {
        String sql = "select code from form where flowTypeCode=" + StrUtil.sqlstr(flowTypeCode) + " and isFlow=1 order by orders asc";
        return list(sql);
    }

    public String getSqlList(HttpServletRequest request) {
        Privilege privilege = new Privilege();
        String name = ParamUtil.get(request, "name");
        String isFlow = ParamUtil.get(request, "isFlow");
        String op = ParamUtil.get(request, "op");
        String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
        String curUnitCode = ParamUtil.get(request, "unitCode");
        if (curUnitCode.equals("")) {
            curUnitCode = privilege.getUserUnitCode(request);
        }
        String searchUnitCode = ParamUtil.get(request, "searchUnitCode");
        if (!searchUnitCode.equals("")) {
            curUnitCode = searchUnitCode;
        }

        String sql = "";
        if (isFlow.equals("0")) {
            sql = "select code from form where isFlow=0";
        } else {
            if (!flowTypeCode.equals(""))
                sql = "select code from form where flowTypeCode=" + StrUtil.sqlstr(flowTypeCode) + " and isFlow=1";
            else {
                sql = "select code from form where isFlow=1";
            }
        }

        if (op.equals("search")) {
            if (!"".equals(searchUnitCode)) {
                sql += " and unit_code=" + StrUtil.sqlstr(searchUnitCode);
            }
            if (!"".equals(name)) {
                sql += " and (name like " + StrUtil.sqlstr("%" + name + "%")
                        + "or code like " + StrUtil.sqlstr("%" + name + "%") + ")";
            }
        } else {
            sql += " and unit_code=" + StrUtil.sqlstr(curUnitCode);
        }

        sql += " order by code asc";
        return sql;
    }

    public boolean updateFieldProps(String title, String canNull, String canQuery, String canList, String orders, String width, String isMobileDisplay, String isHide, String moreThan, String morethanMode, int isUnique, String formCode, String name) {
        String sql = "update form_field set title=?, canNull=?, canQuery=?, canList=?, orders=?, width=?, is_mobile_display=?, is_hide=?,more_than=?,more_than_mode=?,is_unique=? where formCode=? and name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            return jt.executeUpdate(sql, new Object[]{title, canNull, canQuery, canList, orders, width, isMobileDisplay, isHide, moreThan, morethanMode, isUnique, formCode, name})==1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setIeVersion(String ieVersion) {
        this.ieVersion = ieVersion;
    }

    public String getIeVersion() {
        return ieVersion;
    }

    public void setLog(boolean log) {
		this.log = log;
	}

	public boolean isLog() {
		return log;
	}
	
	public boolean isNewDtctl() {
		return isNewDtctl;
	}

	public void setNewDtctl(boolean isNewDtctl) {
		this.isNewDtctl = isNewDtctl;
	}

	public void setProgress(boolean progress) {
		this.progress = progress;
	}

	public boolean isProgress() {
		return progress;
	}

	public void setOnlyCamera(boolean onlyCamera) {
		this.onlyCamera = onlyCamera;
	}

	public boolean isOnlyCamera() {
		return onlyCamera;
	}

	private String code;
    private String name;
    private String content;
    private int orders = 1;
    private Vector fields;
    private String oldContent;
    private boolean system;
    private String flowTypeCode;
    private boolean flow = true;
    private boolean hasAttachment = true;
    private String unitCode;

    private String ieVersion;
    private boolean isNewDtctl;
    private boolean progress = false;
    private boolean onlyCamera = false;
    
    private String viewSetup;
    private String checkSetup;

	public String getViewSetup() {
		return viewSetup;
	}

	public void setViewSetup(String viewSetup) {
		this.viewSetup = viewSetup;
	}

    public String getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(String checkSetup) {
        this.checkSetup = checkSetup;
    }

    public FormParser getFormParser() {
        return formParser;
    }

    public void setFormParser(FormParser formParser) {
        this.formParser = formParser;
    }
}
