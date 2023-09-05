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

import com.cloudweb.oa.cache.FlowFormDaoCache;
import com.cloudweb.oa.cache.FlowShowRuleCache;
import com.cloudweb.oa.cache.FormShowRuleCache;
import com.cloudweb.oa.cache.VisualFormDaoCache;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.ISQLGenerator;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
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
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("FormDb:默认数据库名为空！");
        }
        isInitFromConfigDB = false;
        init();
    }

    public FormDb(String code) {
        this.code = code;
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("FormDb:默认数据库名为空！");
        }
        load();
        init();
    }

    @Override
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
    }

    @Override
    public boolean create() throws ErrMsgException {
        Conn conn = null;
        orders = getMaxOrders();
        boolean re = false;
        try {
            Vector<FormField> v = formParser.getFields();
            // 检查其中的fields是否合法
            formParser.validateFields();

            conn = new Conn(connname);
            conn.beginTrans();
            try {
                // 由表单的域来创建表格，删除表或者创建表的SQL语句会使得当表单中有重复的列名时，至使事务回滚失败！
                Vector<String> vt = SQLGeneratorFactory.getSQLGenerator().generateDropTable(getTableNameByForm());
                for (Object o : vt) {
                    String sql = (String) o;
                    // LogUtil.getLog(getClass()).debug("create1: sql=" + sql);
                    conn.executeUpdate(sql);
                }
            }
            catch (SQLException e) {
                // 当用SQLServer数据库时，如果表不存在，会报异常
                LogUtil.getLog(getClass()).error("create:" + StrUtil.trace(e));
            }
            
            String sql = "";
            Vector<String> vt = generateCreateStr(v);
            Iterator<String> ir = vt.iterator();
            while (ir.hasNext()) {
                sql = ir.next();
                // LogUtil.getLog(getClass()).info("create2: sql=" + sql);
                conn.executeUpdate(sql);
            }
            
            vt = SQLGeneratorFactory.getSQLGenerator().generateCreateStrForLog(getTableName(code), v);
            ir = vt.iterator();
            while (ir.hasNext()) {
                sql = ir.next();
                conn.executeUpdate(sql);
            }            

            sql = "insert into form_field (formCode, name, title, type, macroType, defaultValue, fieldType, canNull, fieldRule, description, is_func, css_width, is_readonly,present, is_helper,read_only_type,format) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // 增加新的表单域
            Iterator<FormField> irField = v.iterator();
            PreparedStatement ps = conn.prepareStatement(sql);
            while (irField.hasNext()) {
                FormField ff = irField.next();
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
                ps.setInt(15, ff.isHelper()?1:0);
                ps.setString(16, ff.getReadOnlyType());
                ps.setString(17, ff.getFormat());
                LogUtil.getLog(getClass()).info("create:" + ff.getName() + "--" + ff.getDefaultValue() +
                                   "--" + ff.getDefaultValueRaw() + " canNull=" + ff.isCanNull() + " rule=" + ff.getRule());
                // LogUtil.getLog(getClass()).info("FormDb save:" + ff.getName() + "=" + ff.getDefaultValue());
                conn.executePreUpdate();
            }

            ps.close();

            ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, code);
            ps.setString(2, name);

            // 新时间控件
            isNewDtctl = content.toLowerCase().contains("isnewdatetimectl=\"true\"");

            content = content.trim();
            if ("".equals(content)) {
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
            re = conn.executePreUpdate() == 1;

            conn.commit();

            if (re) {
                FormCache mc = new FormCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("插入时出错！");
        } catch (Exception e) {
            throw new ErrMsgException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return re;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new FormDb(pk.getStrValue());
    }

    @Override
    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setString(1, code);
            conn.beginTrans();
            re = conn.executePreUpdate() > 0;

            if (re) {
                FormCache mc = new FormCache(this);
                primaryKey.setValue(code);
                mc.refreshDel(primaryKey);

                // 删除数据form_field表中的域
                String sql = "delete from form_field where formCode=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, code);
                conn.executePreUpdate();
                pstmt.close();

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
                        LogUtil.getLog(getClass()).error(ex);
                    }
                }

                // 删除数据库中对应的表
                try {
                    Vector<String> vt = SQLGeneratorFactory.getSQLGenerator().
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
                    Vector<String> vt = SQLGeneratorFactory.getSQLGenerator().
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
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    /**
     * 取得表单对应的表名
     * @param code String
     * @return String
     */
    public static String getTableName(String code) {
        return "ft_" + code;
    }
    
    public static String getTableNameForLog(String code) {
    	return getTableName(code) + "_log";
    }    
    
    /**
     * 取得表格名称对应的表单编码
     * @param tableName 表名
     * @return 如果表格名称不是以ft_开头，则返回null
     */
    public static String getCodeByTableName(String tableName) {
    	tableName = tableName.toLowerCase();
    	if (tableName.startsWith("ft_")) {
            return tableName.substring(3);
        } else {
            return null;
        }
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
    public static Vector<FormField>[] checkFieldChange(Vector<FormField> oldFields, Vector<FormField> newFields, Vector<FormField> fields) {
    	// oldFields是从数据库中取出的，只含有字段，无其它信息
        Vector<FormField> fieldsForDel = new Vector<>();
        Vector<FormField> fieldsForAdd = new Vector<>();
        int oldlen = oldFields.size();
        int newlen = newFields.size();
        // 找出新域中新增的字段
        for (Object newField : newFields) {
            boolean isFinded = false;
            FormField nff = (FormField) newField;
            for (Object oldField : oldFields) {
                FormField off = (FormField) oldField;
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
        Vector<FormField> fieldsForRemain = new Vector<>();
        // 找出旧域中被删的字段
        for (Object oldField : oldFields) {
            boolean isFinded = false;
            FormField off = (FormField) oldField;
            for (Object newField : newFields) {
                FormField nff = (FormField) newField;
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
        Iterator<FormField> ir = fieldsForRemain.iterator();
        while (ir.hasNext()) {
        	FormField ffNew = (FormField)ir.next();
            for (Object field : fields) {
                FormField ffOld = (FormField) field;
                if (ffNew.getName().equals(ffOld.getName())) {
                    if (ffNew.getDefaultValueRaw().equals(ffOld.getDefaultValueRaw())) {
                        if (ffNew.getTitle().equals(ffOld.getTitle())) {
                            // 20210320 加上规则的判断，以改变数据库中字段的长度
                            if (ffNew.getRule().equals(ffOld.getRule())) {
                                // 如果字段无变化，则从fieldsForRemain中去除
                                ir.remove();
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        Vector<FormField>[] v = new Vector[3];
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
    public Vector<String> generateModifyStr(Vector<FormField>[] vt) {
        return SQLGeneratorFactory.getSQLGenerator().generateModifyStr(getTableNameByForm(), vt);
    }

    /**
     * 创建增加表单的SQL语句
     * @param fields Vector
     * @return String
     */
    public Vector<String> generateCreateStr(Vector<FormField> fields) {
        return SQLGeneratorFactory.getSQLGenerator().generateCreateStr(getTableNameByForm(), fields);
    }

    public Vector<FormField> getTableColumnsFromDb() {
        Conn conn = new Conn(connname);
        ResultSet rs;
        Vector<FormField> v = new Vector<>();
        try {
            conn.prepareStatement(SQLGeneratorFactory.getSQLGenerator().getTableColumnsFromDbSql(getTableNameByForm()));
            rs = conn.executePreQuery();
            ResultSetMetaData rm = rs.getMetaData();
            int colCount = rm.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                // 注意：如果字段的field为flowId或者flowTypeCode，则跳过
                String colName = rm.getColumnName(i);
                if ("flowId".equalsIgnoreCase(colName) ||
                    "flowTypeCode".equalsIgnoreCase(colName) ||
                    "id".equalsIgnoreCase(colName) ||
                    "cws_creator".equalsIgnoreCase(colName) ||
                    "cws_id".equalsIgnoreCase(colName) ||
                    "unit_code".equalsIgnoreCase(colName) ||
                    "cws_status".equalsIgnoreCase(colName) ||
                    "cws_quote_id".equalsIgnoreCase(colName) ||
                    "cws_flag".equalsIgnoreCase(colName) ||
                    "cws_progress".equalsIgnoreCase(colName) ||
                    "cws_parent_form".equalsIgnoreCase(colName) ||
                    "cws_order".equalsIgnoreCase(colName) ||
                    "cws_create_date".equalsIgnoreCase(colName) ||
                    "cws_modify_date".equalsIgnoreCase(colName) ||
                    "cws_quote_form".equalsIgnoreCase(colName) ||
                    "cws_visited".equalsIgnoreCase(colName) ||
                    "cws_finish_date".equalsIgnoreCase(colName)) {
                    continue;
                }
                FormField ff = new FormField();
                ff.setName(colName);
                v.addElement(ff);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
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

            FormCache mc = new FormCache(this);
            primaryKey.setValue(code);
            mc.refreshSave(primaryKey);

            // 因为FormDAO中会缓存FormDb，故在修改显示规则后应清空FormDAO所有的缓存记录
            VisualFormDaoCache visualFormDaoCache = SpringUtil.getBean(VisualFormDaoCache.class);
            visualFormDaoCache.refreshAll(code);

            FormShowRuleCache formShowRuleCache = SpringUtil.getBean(FormShowRuleCache.class);
            formShowRuleCache.refresh(code);

            // 取出跟该表单相关的流程类型，如果采用的是表单验证规则，则刷新流程中的显示规则
            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            Leaf lf = new Leaf();
            for (Object o : lf.getLeavesUseForm(code)) {
                lf = (Leaf) o;
                wfd = wfd.getDefaultPredefineFlow(lf.getCode());
                if (wfd == null) {
                    wfd = new WorkflowPredefineDb();
                } else {
                    if (wfd.isUseFormViewRule()) {
                        FlowShowRuleCache flowShowRuleCache = SpringUtil.getBean(FlowShowRuleCache.class);
                        flowShowRuleCache.refresh(wfd.getTypeCode());
                    }
                }
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return re;
    }

    @Override
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        boolean re = false;
        try {
            // 解析content，在表form_field中建立相应的域
            // FormParser fp = new FormParser(content);
            if (formParser == null) {
                formParser = new FormParser(content);
            }
            formParser.validateFields();
            Vector<FormField> newv = formParser.getFields();
            // 检查其中的fields是否合法

            Vector<FormField> oldv = getTableColumnsFromDb();
            Vector<FormField>[] vt = checkFieldChange(oldv, newv, fields);
            LogUtil.getLog(getClass()).info("save: oldv.size=" + oldv.size() + " newv.size=" + newv.size());

            conn.beginTrans();

            // 更新修改过的字段
            Vector<String> v = generateModifyStr(vt);
            Iterator<String> ir = v.iterator();
            while (ir.hasNext()) {
                String sql = ir.next();
                // LogUtil.getLog(getClass()).info("save: v.size()=" + v.size() + " sql=" + sql);
                DebugUtil.i(getClass(), "save", "generateModifyStr sql=" + sql);
                ps = conn.prepareStatement(sql);
                conn.executePreUpdate();
                ps.close();
            }
            
            // 20170830 fgf 只能与log表同步修改，否则如果中间设为不保留历史记录，则不能同步字段，会导致混乱
            // 检查原来是否已存在对应的log表，如果不存在则创建
            ISQLGenerator is = SQLGeneratorFactory.getSQLGenerator();
            boolean isLogExist = is.isTableForLogExist(getTableName(code));
            if (!isLogExist) {
                Vector<String> vtCreate = SQLGeneratorFactory.getSQLGenerator().generateCreateStrForLog(getTableName(code), newv);
                ir = vtCreate.iterator();
                while (ir.hasNext()) {
                    String sql = ir.next();
                    LogUtil.getLog(getClass()).info("save2: sql=" + sql);
                    conn.executeUpdate(sql);
                }
            }
            else {
                v = SQLGeneratorFactory.getSQLGenerator().generateModifyStrForLog(getTableName(code), vt);
                ir = v.iterator();
                while (ir.hasNext()) {
                    String sql = ir.next();
                    ps = conn.prepareStatement(sql);
                    conn.executePreUpdate();
                    ps.close();
                }
            }
            
            // 新时间控件
            isNewDtctl = content.toLowerCase().contains("isnewdatetimectl=\"true\"");

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
            re = conn.executePreUpdate() > 0;
            ps.close();

            // 取得原来表单域的序号置于map中
            HashMap<String, FormField> map = new HashMap<String, FormField>();
            String sql = "select name,orders,canNull,canQuery,canList,width,is_mobile_display,is_hide,more_than,more_than_mode,present,is_unique,is_helper,read_only_type,format from form_field where formCode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = conn.executePreQuery();
            while (rs.next()) {
            	FormField ff = new FormField();
            	ff.setName(rs.getString(1));
            	ff.setOrders(rs.getInt(2));
            	ff.setCanNull(rs.getInt(3) == 1);
            	ff.setCanQuery(rs.getInt(4) >= 1);
            	ff.setQueryMode(rs.getInt(4));
            	ff.setCanList(rs.getInt(5) == 1);
            	ff.setWidth(rs.getInt(6));
            	ff.setMobileDisplay("1".equals(rs.getString(7)));
            	ff.setHide(rs.getInt(8));
            	ff.setMoreThan(rs.getString(9));
            	ff.setMorethanMode(rs.getString(10));
            	ff.setPresent(StrUtil.getNullStr(rs.getString(11)));
                int isUnique = rs.getInt(12);
                if (isUnique == FormField.UNIQUE_GLOBAL) {
                    ff.setUnique(true);
                }
                else if (isUnique == FormField.UNIQUE_NEST) {
                    ff.setUniqueNest(true);
                    ff.setUnique(false);
                }
                else {
                    ff.setUnique(false);
                }
            	ff.setHelper(rs.getInt(13)==1);
				ff.setReadOnlyType(StrUtil.getNullStr(rs.getString(14)));
                ff.setFormat(StrUtil.getNullStr(rs.getString(15)));
                map.put(rs.getString(1), ff);
            }
            rs.close();
            ps.close();

            // 删除原来的表单域
            sql = "delete from form_field where formCode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            conn.executePreUpdate();
            ps.close();

            sql = "insert into form_field (formCode, name, title, type, macroType, defaultValue, fieldType, canNull, fieldRule, orders, canQuery, canList, is_mobile_display, width, description,is_hide,more_than,more_than_mode,is_func,css_width,is_readonly,present,is_unique,is_helper,read_only_type,format) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // 增加新的表单域
            Iterator<FormField> irField = newv.iterator();
            ps = conn.prepareStatement(sql);
            while (irField.hasNext()) {
                FormField ff = irField.next();
                ps.setString(1, code);
                ps.setString(2, ff.getName());
                ps.setString(3, ff.getTitle());
                ps.setString(4, ff.getType());
                ps.setString(5, ff.getMacroType());
                ps.setString(6, ff.getDefaultValueRaw());
                ps.setInt(7, ff.getFieldType());
                ps.setInt(8, ff.isCanNull() ? 1 : 0);
                ps.setString(9, ff.getRule());
                /*DebugUtil.i(getClass(), "save", ff.getName() + "--" +
                                                ff.getDefaultValue() +
                                                "--" + ff.getDefaultValueRaw() + "--" + ff.getReadOnlyType());*/
                FormField o = map.get(ff.getName());
                // 如果字段原先有序号等，则赋予原来的序号等
                if (o != null) {
                    ps.setInt(10, o.getOrders());
                    ps.setInt(11, o.getQueryMode());
                    ps.setInt(12, o.isCanList() ? 1 : 0);
                    ps.setString(13, o.isMobileDisplay() ? "1" : "0");
                    ps.setInt(14, o.getWidth());
                    int unique;
                    if (o.isUnique()) {
                        unique = FormField.UNIQUE_GLOBAL;
                    }
                    else if (o.isUniqueNest()) {
                        unique = FormField.UNIQUE_NEST;
                    }
                    else {
                        unique = FormField.UNIQUE_NONE;
                    }
                    ps.setInt(23, unique);
                } else {
                    ps.setInt(10, 0);
                    ps.setInt(11, 1);
                    ps.setInt(12, 1);
                    ps.setString(13, "1");
                    ps.setInt(14, 100);
                    ps.setInt(23, FormField.UNIQUE_NONE); // 是否唯一
                }
                ps.setString(15, ff.getDescription());
                if (o != null) {
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
                if (o != null) {
                    ps.setInt(24, o.isHelper() ? 1 : 0);
                }
                else {
                    ps.setInt(24, ff.isHelper() ? 1 : 0);
                }
                ps.setString(25, ff.getReadOnlyType());
                ps.setString(26, ff.getFormat());
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

            FlowFormDaoCache flowFormDaoCache = SpringUtil.getBean(FlowFormDaoCache.class);
            flowFormDaoCache.refreshAll(code);
            VisualFormDaoCache visualFormDaoCache = SpringUtil.getBean(VisualFormDaoCache.class);
            visualFormDaoCache.refreshAll(code);

            FormCache mc = new FormCache(this);
            primaryKey.setValue(code);
            mc.refreshSave(primaryKey);

            FormShowRuleCache formShowRuleCache = SpringUtil.getBean(FormShowRuleCache.class);
            formShowRuleCache.refresh(code);

            // 取出跟该表单相关的流程类型，如果采用的是表单验证规则，则刷新流程中的显示规则
            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            Leaf lf = new Leaf();
            for (Object o : lf.getLeavesUseForm(code)) {
                lf = (Leaf) o;
                wfd = wfd.getDefaultPredefineFlow(lf.getCode());
                if (wfd == null) {
                    wfd = new WorkflowPredefineDb();
                } else {
                    if (wfd.isUseFormViewRule()) {
                        FlowShowRuleCache flowShowRuleCache = SpringUtil.getBean(FlowShowRuleCache.class);
                        flowShowRuleCache.refresh(wfd.getTypeCode());
                    }
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        } finally {
            conn.close();
        }
        return re;
    }

    public String getFieldTitle(String fieldName) {
        FormField ff = fieldsMap.get(fieldName);
        if (ff == null) {
            return null;
        }
        else {
            return ff.getTitle();
        }
        /*
        if (fields == null) {
            return "";
        }
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField fd = (FormField) ir.next();
            if (fd.getName().equals(fieldName)) {
                return fd.getTitle();
            }
        }
        return "";*/
    }

    /**
     * 根据表单域名称获取域
     * @param fieldTitle String
     * @return FormField
     */
    public FormField getFormFieldByTitle(String fieldTitle) {
        if (fields == null) {
            return null;
        }
        for (FormField ff : fields) {
            if (ff.getTitle().equals(fieldTitle)) {
                return ff;
            }
        }
        return null;
    }

    /**
     * 根据表单域编码获取域
     * @param fieldName String
     * @return FormField
     */
    public FormField getFormField(String fieldName) {
/*        if (fields == null) {
            return null;
        }
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff.getName().equalsIgnoreCase(fieldName)) {
                return ff;
            }
        }
        return null;
        */

        // 20200512 优化为使用fieldsMap
        return fieldsMap.get(fieldName);
    }

    @Override
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
                DebugUtil.e(getClass(), "load", "load:表单 " + code + " 在数据库中未找到."); // + StrUtil.trace(new Exception()));
            } else {
                this.code = rs.getString(1);
                this.name = rs.getString(2);
                this.content = rs.getString(3);
                this.system = rs.getInt(4) == 1;
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

                rs.close();
                pstmt.close();

                fields = new Vector<>();
                fieldsMap = new LinkedHashMap<>();

                String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList,orders,width,description,is_mobile_display,is_hide,more_than,more_than_mode,is_func,css_width,is_readonly,present,is_unique,is_helper,read_only_type,format from form_field where formCode=? order by orders desc";
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
                        ff.setCanQuery(rs.getInt(9)>=1);
                        ff.setQueryMode(rs.getInt(9));
                        ff.setCanList(rs.getInt(10)==1);
                        ff.setOrders(rs.getInt(11));
                        ff.setWidth(rs.getInt(12));
                        ff.setDescription(StrUtil.getNullStr(rs.getString(13)));
                        ff.setMobileDisplay(rs.getInt(14)==1);
                        ff.setHide(rs.getInt(15));
                        ff.setMoreThan(StrUtil.getNullStr(rs.getString(16)));
                        ff.setMorethanMode(StrUtil.getNullStr(rs.getString(17)));
                        ff.setFunc(rs.getInt(18)==1);
                        ff.setCssWidth(StrUtil.getNullStr(rs.getString(19)));
                        ff.setReadonly(rs.getInt(20)==1);
                        ff.setPresent(StrUtil.getNullStr(rs.getString(21)));

                        int isUnique = rs.getInt(22);
                        if (isUnique == FormField.UNIQUE_GLOBAL) {
                            ff.setUnique(true);
                        }
                        else if (isUnique == FormField.UNIQUE_NEST) {
                            ff.setUniqueNest(true);
                            ff.setUnique(false);
                        }
                        else {
                            ff.setUnique(false);
                        }

                        ff.setHelper(rs.getInt(23)==1);
                        ff.setReadOnlyType(StrUtil.getNullStr(rs.getString(24)));
                        ff.setFormat(StrUtil.getNullStr(rs.getString(25)));
                        fields.addElement(ff);
                        fieldsMap.put(ff.getName(), ff);

                        if (ff.getType().equals(FormField.TYPE_SELECT)) {
                            String[][] ary = FormParser.getOptionsArrayOfSelect(this, ff);
                            for (String[] opt : ary) {
                                ff.getOptions().put(opt[1], opt[0]);
                            }
                        }
                        else if (ff.getType().equals(FormField.TYPE_RADIO)) {
                            String[][] ary = FormParser.getOptionsArrayOfRadio(this, ff);
                            for (String[] opt : ary) {
                                ff.getOptions().put(opt[0], opt[1]);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {}
            }
            conn.close();
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

    public Vector<FormField> getFields() {
        // 因为FormDb取自缓存时，FormDAO中使用的FormDb当同为一个formCode时，其fields为同一个，于是当重复利用FormDb时，在FormDAO的load中取值就会存入同一个Vector中，这样就会带来问题
        // return fields;
        Vector<FormField> v = new Vector<>();
        // 2012-06-17 fgf 防止在module_field_list.jsp中ModuleRelateDb.getFormsRelatedWith获取已删除的关联表单的fields时，fields为null情况
        if (fields==null) {
            return v;
        }
        for (FormField ff : fields) {
            try {
                v.addElement((FormField) ff.clone());
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getFields:" + e.getMessage());
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
            if ("down".equals(direction)) {
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
            LogUtil.getLog(getClass()).error(e);
        }
        return bleaf;
    }

    public int getMaxOrders() {
        String GETMAXORDERS =
                "select max(orders) from " + tableName;
        Conn conn = new Conn(connname);
        ResultSet rs;
        int maxorders = -1;
        try {
            //更新文件内容
            conn.prepareStatement(GETMAXORDERS);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return maxorders;
    }

    public boolean move(String direction) throws ErrMsgException {
        // 根据direction检查是否可以移动
        if ("up".equals(direction)) {
            if (orders == 1) {
                throw new ErrMsgException("该项已处在首位！");
            }
        }
        if ("down".equals(direction)) {
            if (orders == getMaxOrders()) {
                throw new ErrMsgException("该项已处于最后一位！");
            }
        }

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if ("up".equals(direction)) {
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

        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    /**
     * 列出属于流程大类的表单
     * @param flowTypeCode 流程类型
     * @return
     */
    public Vector<FormDb> listOfFlow(String flowTypeCode) {
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
        if ("".equals(curUnitCode)) {
            curUnitCode = privilege.getUserUnitCode(request);
        }
        String searchUnitCode = ParamUtil.get(request, "searchUnitCode");
        if (!"".equals(searchUnitCode)) {
            curUnitCode = searchUnitCode;
        }

        String sql = "";
        if ("0".equals(isFlow)) {
            sql = "select code from form where isFlow=0";
        } else {
            if (!"".equals(flowTypeCode)) {
                sql = "select code from form where flowTypeCode=" + StrUtil.sqlstr(flowTypeCode) + " and isFlow=1";
            } else {
                sql = "select code from form where isFlow=1";
            }
        }

        if ("search".equals(op)) {
            if (!"".equals(searchUnitCode)) {
                sql += " and unit_code=" + StrUtil.sqlstr(searchUnitCode);
            }
            if (!"".equals(name)) {
                sql += " and (name like " + StrUtil.sqlstr("%" + name + "%")
                        + "or code like " + StrUtil.sqlstr("%" + name + "%") + ")";
            }
        } else {
            if (!privilege.isUserPrivValid(request, Privilege.ADMIN)) {
                sql += " and unit_code=" + StrUtil.sqlstr(curUnitCode);
            }
        }

        sql += " order by code asc";
        return sql;
    }

    /**
     * 保存表单域的属性
     * @param title
     * @param canNull
     * @param canQuery
     * @param canList
     * @param orders
     * @param width
     * @param isMobileDisplay
     * @param isHide
     * @param moreThan
     * @param morethanMode
     * @param isUnique
     * @param formCode
     * @param name
     * @return
     */
    public boolean updateFieldProps(String title, String canNull, String canQuery, String canList, String orders, String width, String isMobileDisplay, String isHide, String moreThan, String morethanMode, int isUnique, int isHelper, String formCode, String name) {
        boolean re = false;
        FormField ffOld = getFormField(name);
        String sql = "update form_field set title=?, canNull=?, canQuery=?, canList=?, orders=?, width=?, is_mobile_display=?, is_hide=?,more_than=?,more_than_mode=?,is_unique=?,is_helper=? where formCode=? and name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            re = jt.executeUpdate(sql, new Object[]{title, canNull, canQuery, canList, orders, width, isMobileDisplay, isHide, moreThan, morethanMode, isUnique, isHelper, formCode, name})==1;
            if (re) {
                boolean isCanNull = "1".equals(canNull);
                if (ffOld.isCanNull() != isCanNull) {
                    FormParser formParser = new FormParser();
                    formParser.setFieldAttribute(this, name, "cannull", isCanNull ? "1" : "0");
                }
                if (isHelper == 1) {
                    // 如果为辅助字段，则置表单中的字段为readonly
                    sql = "update form_field set is_readonly=1 where formCode=? and name=?";
                    re = jt.executeUpdate(sql, new Object[]{formCode, name}) == 1;
                    if (re) {
                        FormParser formParser = new FormParser();
                        formParser.setFieldAttribute(this, name, "readonly", "readonly");
                    }
                }
                else {
                    // 原来是
                    if (ffOld.isHelper()) {
                        FormParser formParser = new FormParser();
                        formParser.removeFieldAttribute(this, name, "readonly");
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
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

	public Map<String, FormField> getFieldsMap() {
        return fieldsMap;
    }

    /**
     * 列出所有某一类宏控件字段，用于查询出二维码宏控件
     * @param macroType
     * @return
     */
    public List<FormField> listMacorFields(String macroType) {
        List<FormField> list = new ArrayList<>();
        String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList,orders,width,description,is_mobile_display,is_hide,more_than,more_than_mode,is_func,css_width,is_readonly,present,is_unique,is_helper,read_only_type,format,formCode from form_field where macroType=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[]{macroType});
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                FormField ff = new FormField();
                ff.setFormCode(code);
                ff.setName(rr.getString(1));
                ff.setTitle(rr.getString(2));
                ff.setType(rr.getString(3));
                ff.setMacroType(rr.getString(4));
                ff.setDefaultValue(StrUtil.getNullStr(rr.getString(5)));
                ff.setFieldType(rr.getInt(6));
                ff.setCanNull(rr.getInt(7)==1);
                ff.setRule(StrUtil.getNullStr(rr.getString(8)));
                ff.setCanQuery(rr.getInt(9)>=1);
                ff.setQueryMode(rr.getInt(9));
                ff.setCanList(rr.getInt(10)==1);
                ff.setOrders(rr.getInt(11));
                ff.setWidth(rr.getInt(12));
                ff.setDescription(StrUtil.getNullStr(rr.getString(13)));
                ff.setMobileDisplay(rr.getInt(14)==1);
                ff.setHide(rr.getInt(15));
                ff.setMoreThan(StrUtil.getNullStr(rr.getString(16)));
                ff.setMorethanMode(StrUtil.getNullStr(rr.getString(17)));
                ff.setFunc(rr.getInt(18)==1);
                ff.setCssWidth(StrUtil.getNullStr(rr.getString(19)));
                ff.setReadonly(rr.getInt(20)==1);
                ff.setPresent(StrUtil.getNullStr(rr.getString(21)));

                int isUnique = rr.getInt(22);
                if (isUnique == FormField.UNIQUE_GLOBAL) {
                    ff.setUnique(true);
                }
                else if (isUnique == FormField.UNIQUE_NEST) {
                    ff.setUniqueNest(true);
                    ff.setUnique(false);
                }
                else {
                    ff.setUnique(false);
                }

                ff.setHelper(rr.getInt(23)==1);
                ff.setReadOnlyType(StrUtil.getNullStr(rr.getString(24)));
                ff.setFormat(StrUtil.getNullStr(rr.getString(25)));
                ff.setFormCode(rr.getString(26));
                list.add(ff);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return list;
    }

	private String code;
    private String name;
    private String content;
    private int orders = 1;
    private Vector<FormField> fields;
    /**
     * 20200512添加，为了提高效率
     */
    private Map<String, FormField> fieldsMap;
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
