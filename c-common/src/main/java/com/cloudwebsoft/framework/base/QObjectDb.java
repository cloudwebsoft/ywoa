package com.cloudwebsoft.framework.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import cn.js.fan.web.Global;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * Title: QObject (Quick Object)
 * </p>
 * 
 * <p>
 * Description:
 * 
 * 注意： 当创建新对象时，例如： QObjectDb qdb = new QObjectDb(); qdb = qdb.getQObjectDb(...);
 * 
 * qdb.create(...);
 * 按以上方法，qdb中将存储新对象的相关值，会变成create的记录的相关值，原qdb的resultRecord被重写，qdb在内存中会被冲掉
 * 被重写符合面向对象的思路，相当于ObjectDb的先set，然后create
 * 
 * 正确的方法应该是： QObjectDb newQdb = new QObjectDb(); newQdb.create(...);
 * 以上情况详见：TMsgMgr中的quote方法
 * 
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class QObjectDb implements java.io.Serializable {
	/**
	 * config_db.xml中的信息
	 */
	protected QDBTable table;

	/**
	 * 缓存组名称
	 */
	protected String cacheGroup;

	/**
	 * 计数缓存组名称
	 */
	protected String cacheGroupCount;

	/**
	 * 是否通过config文件初始化
	 */
	protected boolean isInitFromConfig = true;

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * 主键
	 */
	protected PrimaryKey primaryKey;

	/**
	 * 是否从数据库中load成功所有SQL语句中指定的字段
	 */
	protected boolean loaded = false;
	protected ResultRecord resultRecord;

	public QObjectDb() {
		// 从配置中读取queryCreate,queryLoad, querySave, cacheGroup
		init();
	}

	public void init() {
		// connName = Global.defaultDB;
		initDB();
		if (isInitFromConfig) {
			initFromConfig();
		}
		cacheGroup = getClass().getName();
		cacheGroupCount = cacheGroup + "_count";
	}

	public void initDB() {
		// isInitFromConfig = false;
	}

	public void initFromConfig() {
		QDBConfig dc = new QDBConfig();
		// LogUtil.getInstance(getClass()).info("initFromConfig:" +
		// getClass().getName());
		table = dc.getQDBTable(getClass().getName());
		if (table == null) {
			// LogUtil.getLog(this.getClass()).error("initFromConfig2: cann't find table '"
			// + getClass().getName() + "' defination in config file.");
			throw new IllegalArgumentException(
					"initFromConfig2: cann't find table '"
							+ getClass().getName()
							+ "' defination in config file.");
		}
		// 注意需通过clone产生一个新的实例，否则因为如果是自缓存中获得的，则dt.getPrimaryKey()引用的是同一个实例
		this.primaryKey = (PrimaryKey) table.getPrimaryKey().clone();
		this.objCachable = table.isObjCachable();
		this.listCachable = table.isListCachable();
	}

	public QDBTable getTable() {
		return table;
	}

	public String getCacheKey(PrimaryKey pk) {
		return table.getName() + "_" + pk.toString();
	}

	public QObjectDb getQObjectDb(Object primaryKeyValue) {
		PrimaryKey pk = new PrimaryKey();
		pk.setValue(primaryKeyValue);
		QObjectDb obj = null;
		if (objCachable) {
			try {
				obj = (QObjectDb) QCache.getInstance().getFromGroup(
						getCacheKey(pk), cacheGroup);
			} catch (Exception e) {
				LogUtil.getLog(this.getClass()).error(
						"getQObjectDb:" + StrUtil.trace(e));
			}

			if (obj == null) {
				// obj = new QObjectDb();
				obj = getQObjectDbRaw(primaryKeyValue);
				if (obj != null && obj.isLoaded()) {
					try {
						QCache.getInstance().putInGroup(getCacheKey(pk),
								cacheGroup, obj);
					} catch (Exception e) {
						LogUtil.getLog(this.getClass()).error(
								"getQObject2: key=" + getCacheKey(primaryKey)
										+ " group=" + cacheGroup + " "
										+ e.getMessage());
						LogUtil.getLog(this.getClass()).error(StrUtil.trace(e));
					}
				}
			}
		} else {
			obj = getQObjectDbRaw(primaryKeyValue);
		}
		return obj;
	}

	/**
	 * 从数据库中取出对象，不经过缓存
	 * 
	 * @param primaryKeyValue
	 *            Object
	 * @return QObjectDb
	 */
	public QObjectDb getQObjectDbRaw(Object primaryKeyValue) {
		QObjectDb obj = null;
		try {
			obj = getClass().newInstance();
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("getQObjectDbRaw:" + e.getMessage());
		}
		obj.primaryKey.setValue(primaryKeyValue);

		try {
			obj.load();
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			LogUtil.getLog(getClass()).error("getQObjectDbRaw2:primaryKey=" + primaryKey);
		}

		// LogUtil.getLog(getClass()).info("obj=" + obj + " primaryKeyValue=" +
		// primaryKeyValue + " " + primaryKeyValue.getClass() + " isLoaded=" +
		// obj.isLoaded());

		if (!obj.isLoaded()) {
			obj = null;
		}
		return obj;
	}

	public boolean initQObject(Object[] loadParams) throws SQLException {
		return initQObject(new JdbcTemplate(), table.getQueryLoad(), loadParams);
	}

	/**
	 * 初始化对象，生成一个空对象，从数据库中初始化对象中的mapIndex，置字段值为object，用于ArchiveUserInfoDb
	 * getArchiveUserInfo(String userName, java.util.Date timePoint)
	 * 
	 * @param sql
	 *            String
	 * @return Vector
	 */
	public boolean initQObject(JdbcTemplate jt, String sql,
			Object[] objectParams) throws SQLException {
		int colCount = 0;
		HashMap mapIndex = new HashMap();

		ResultSet rs = null;
		Vector result = null;
		Connection connection = jt.getConnection();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			JdbcTemplate.fillPreparedStatement(ps, objectParams);
			rs = connection.executePreQuery();
			if (rs == null) {
				return false;
			} else {
				// 取得列名信息
				ResultSetMetaData rm = rs.getMetaData();
				colCount = rm.getColumnCount();
				result = new Vector();
				for (int i = 1; i <= colCount; i++) {
					mapIndex.put(rm.getColumnName(i).toUpperCase(), new Integer(i));
					result.addElement(new Object());
				}

				resultRecord = new ResultRecord(result, mapIndex);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (connection != null) {
				connection.close();
			}
			jt.close();
		}
		return true;
	}

	public boolean load() throws SQLException {
		JdbcTemplate jt = new JdbcTemplate(new Connection(table.getConnName()));
		Object[] params = primaryKey.toObjectArray();
		ResultIterator ri = jt.executeQueryTFO(table.getQueryLoad(), params);

		if (ri.hasNext()) {
			resultRecord = ri.next();
			loaded = true;
			return true;
		}

		return false;
	}

	/**
	 * 用于直接创建对象 2007.3.13
	 * 
	 * @return boolean
	 */
	public boolean create() throws ResKeyException, ErrMsgException {
		// 解析queryCreate语句
		String[] fields = getFieldsFromQueryCreate();
		int len = fields.length;

		Object[] params = new Object[len];
		for (int i = 0; i < len; i++) {
			params[i] = resultRecord.get(fields[i]);
		}

		return create(new JdbcTemplate(new Connection(table.getConnName())), params);
	}

	/**
	 * 用于从表单创建对象
	 * 
	 * @param paramChecker
	 *            ParamChecker
	 * @return boolean
	 * @throws ResKeyException
	 * @throws ErrMsgException
	 */
	public boolean create(ParamChecker paramChecker) throws ResKeyException,
			ErrMsgException {
		return create(new JdbcTemplate(new Connection(table.getConnName())), paramChecker);
	}

	public boolean create(JdbcTemplate jt, ParamChecker paramChecker)
			throws ResKeyException, ErrMsgException {
		// 解析queryCreate语句
		String[] fields = getFieldsFromQueryCreate();
		int len = fields.length;

		Object[] params = new Object[len];
		for (int i = 0; i < len; i++) {
			params[i] = paramChecker.getValue(fields[i]);
		}

		return create(jt, params);
	}

	public boolean create(JdbcTemplate jt, Object[] params)
			throws ResKeyException {
		boolean re = false;
		try {
			// 解析queryCreate语句
			String[] fields = getFieldsFromQueryCreate();
			int len = fields.length;
			if (params.length != len) {
				throw new IllegalArgumentException("Params array's lenth is "
						+ params.length
						+ ", but create sql's fields number is " + len);
			}

			// 填充ResultRecord，这样填充的ResultRecord，row中字段的排列顺序与load()出来的并不一致，而与queryCreate中的排列顺序一致
			// 只是为了便于在创建后，得到创建的记录中含有哪些值
			HashMap mapIndex = new HashMap();
			Vector row = new Vector();

			for (int i = 0; i < len; i++) {
				mapIndex.put(fields[i].toUpperCase(), new Integer(i + 1));
				if (params[i] instanceof String[]) {
					params[i] = StringUtils.join((String[])params[i], ",");
				}
				row.addElement(params[i]);
			}

			resultRecord = new ResultRecord(row, mapIndex);

			re = jt.executeUpdate(table.getQueryCreate(), params) == 1;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			throw new ResKeyException(SkinUtil.ERR_DB, e);
		}finally{
			jt.close();
		}
		if (re) {
			// 刷新缓存
			refreshCreate();
		}
		return re;
	}

	public long getQObjectCount(String sql) {
		return getQObjectCount(sql, "");
	}

	/**
	 * 
	 * @param sql
	 *            String
	 * @return int -1 表示sql语句不合法
	 */
	public long getQObjectCount(String sql, String groupName) {
		// 根据sql语句得出计算总数的sql查询语句
		String query = cn.js.fan.db.SQLFilter.getCountSql(sql);
		Long count = null;
		if (listCachable) {
			try {
				count = (Long) QCache.getInstance().getFromGroup(query, cacheGroupCount + groupName);
			} catch (Exception e) {
				LogUtil.getLog(this.getClass()).error(e.getMessage());
			}
			// If already in cache, return the count.
			if (count != null) {
				return count.longValue();
			}
		}
		// Otherwise, we have to load the count from the db.
		long docCount = 0;
		Connection conn = new Connection(table.getConnName());
		ResultSet rs = null;
		try {
			rs = conn.executeQuery(query);
			if (rs.next()) {
				docCount = rs.getLong(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		// Add the count to cache
		if (listCachable) {
			try {
				QCache.getInstance().putInGroup(query, cacheGroupCount,
						new Long(docCount));
			} catch (Exception e) {
				LogUtil.getLog(this.getClass()).error(e.getMessage());
			}
		}
		return docCount;
	}

	public void refresh(PrimaryKey pk) {
		if (!objCachable) {
			return;
		}
		try {
			if (!cacheGroup.equals("")) {
				QCache.getInstance().remove(getCacheKey(pk), cacheGroup);
			} else {
				QCache.getInstance().remove(getCacheKey(pk));
			}
		} catch (Exception e) {
			LogUtil.getLog(this.getClass()).error("refresh:" + e.getMessage());
		}
	}

	public void refreshList() {
		refreshList("");
	}

	/**
	 * 刷新列表
	 * 
	 * @param groupName
	 *            String 通常以对象的分类，如圈子的类别作为groupName
	 */
	public void refreshList(String groupName) {
		if (!listCachable) {
			return;
		}
		try {
			// 当在修改OA中档案模块时，发现如果置用户为invalid，则在置用户职位时，因为user_sel.jsp中使用了getObjects，而当save时，并未刷新列表，就是因为未刷新列表
			QCache.getInstance().invalidateGroup(cacheGroupCount + groupName);
			QCache.getInstance().invalidateGroup(cacheGroup + groupName);
		} catch (Exception e) {
			LogUtil.getLog(this.getClass()).error(StrUtil.trace(e));
		}
	}

	public void refreshCreate() {
		refreshCreate("");
	}

	public void refreshCreate(String groupName) {
		if (!listCachable) {
			return;
		}
		refreshList(groupName);
	}

	public void refreshSave(PrimaryKey pk) {
		if (!objCachable) {
			return;
		}
		refresh(pk);
	}

	public void refreshDel(PrimaryKey pk) {
		refreshDel(pk, "");
	}

	public void refreshDel(PrimaryKey pk, String groupName) {
		try {
			if (objCachable) {
				refresh(pk);
			}
			if (listCachable) {
				refreshList(groupName);
			}
		} catch (Exception e) {
			LogUtil.getLog(this.getClass()).error("refreshDel:" + e.getMessage());
		}
	}

	public boolean del() throws ResKeyException {
		return del(new JdbcTemplate(new Connection(table.getConnName())));
	}

	public boolean del(JdbcTemplate jt) throws ResKeyException {
		boolean re = false;

		Object[] params = primaryKey.toObjectArray();
		try {
			re = jt.executeUpdate(table.getQueryDel(), params) == 1;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("del:" + e.getMessage());
			throw new ResKeyException(SkinUtil.ERR_DB);
		}finally{
			if (jt != null){
				jt.close();
			}
		}
		// 刷新缓存
		refreshDel(primaryKey);

		return re;
	}

	public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
		boolean re = false;
		try{
			re = jt.executeUpdate(table.getQuerySave(), params) == 1;
		}catch(Exception ex){
			LogUtil.getLog(getClass()).error("save:" + ex.getMessage());
			LogUtil.getLog(getClass()).error(ex);
		}finally{
			if (jt != null){
				jt.close();
			}
		}
		// 刷新缓存
		refreshSave(primaryKey);

		return re;
	}

	public boolean save() throws ResKeyException {
		return save(new JdbcTemplate(new Connection(table.getConnName())));
	}

	public synchronized boolean save(ParamChecker paramChecker)
			throws ResKeyException, ErrMsgException {
		return save(new JdbcTemplate(new Connection(table.getConnName())), paramChecker);
	}

	public synchronized boolean save(JdbcTemplate jt, ParamChecker paramChecker)
			throws ResKeyException, ErrMsgException {
		String[] fieldsForSave = getFieldsFromQuerySave();
		if (fieldsForSave == null) {
			throw new IllegalArgumentException("fieldsForSave is null.");
		}
		int len = fieldsForSave.length;
		ResultRecord resultRecord = getResultRecord();
		if (resultRecord == null) {
			throw new IllegalArgumentException("resultRecord is null, may because of object is not loaded.");
		}
		for (int i = 0; i < len; i++) {
			try {
				Object objVal = paramChecker.getValue(fieldsForSave[i]);
				// 如果是数据组则转换为逗号分隔的字符串
				if (objVal instanceof String[]) {
					objVal = StringUtils.join((String[])objVal, ",");
				}
				resultRecord.set(fieldsForSave[i], objVal);
			} catch (ParamCheckerException e) {
				// 跳过在form表单中未获
				if (e.getType() == ParamCheckerException.TYPE_PARAM_NOT_SET_IN_FORM_RULE) {
					LogUtil.getLog(getClass()).info(e.getMessage());
				} else {
					throw e;
				}
			}
		}
		return save(jt);
	}

	/**
	 * 根据resultRecord及primaryKey自动更新
	 * 
	 * @param jt
	 *            JdbcTemplate
	 * @return boolean
	 */
	public boolean save(JdbcTemplate jt) throws ResKeyException {
		String[] fieldsForSave = getFieldsFromQuerySave();
		if (fieldsForSave == null) {
			return false;
		}
		int len = fieldsForSave.length;
		Object[] paramsPk = primaryKey.toObjectArray();

		Object[] params = new Object[fieldsForSave.length + paramsPk.length];
		for (int i = 0; i < len; i++) {
			// LogUtil.getLog(getClass()).info("save:" + fieldsForSave[i]);
			params[i] = resultRecord.get(fieldsForSave[i]);
		}
		len = params.length;
		int k = 0;
		for (int i = fieldsForSave.length; i < len; i++) {
			params[i] = paramsPk[k];
			k++;
		}
		boolean re = false;
		try {
			re = save(jt, params);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("save:" + e.getMessage());
			throw new ResKeyException(SkinUtil.ERR_DB, e);
		}
		return re;
	}

	/**
	 * 解析queryCreate，取得其中的字段
	 * 
	 * @return String[]
	 */
	public String[] getFieldsFromQueryCreate() {
		String queryCreate = table.getQueryCreate();
		int p = queryCreate.indexOf("(");
		int q = queryCreate.indexOf(")");
		if (p == -1) {
			return null;
		}
		if (q == -1) {
			return null;
		}

		String str = queryCreate.substring(p + 1, q);
		// 替换掉空格
		str = str.replaceAll(" ", "");
		String[] ary = StrUtil.split(str, ",");
		return ary;
	}

	/**
	 * 解析querySave，获取其中更新的字段名称及其顺序
	 * 
	 * @return String[]
	 */
	public String[] getFieldsFromQuerySave() {
		String querySave = table.getQuerySave();
		
		int p = querySave.indexOf(" set ");
		if (p == -1) {
			LogUtil.getLog(getClass()).error("getFieldsFromQuerySave: querySave may error sql=" + querySave);
			return null;
		}
		p += 5;

		int q = querySave.indexOf(" where ");

		// update *** set a=?,b=?,... where f=? and g=?"
		// ^ ^
		// 取得这一段的字符串
		String str = querySave.substring(p, q);
		// 替换掉空格
		str = str.replaceAll(" ", "");
		// 替换掉=?
		str = str.replaceAll("=\\?", "");
		String[] ary = StrUtil.split(str, ",");
		return ary;
	}

	public ListResult listResult(String sql, Object[] params, int curPage,
			int pageSize) throws ResKeyException {
		return listResult(Global.getDefaultDB(), sql, params, curPage, pageSize);
	}

	/**
	 * 2007.1.7 添加，用于OA中sms_receive_record的处理(改为使用list(JdbcTemplate jt, String
	 * sql, Object[] params))
	 * 
	 * @param sql
	 *            String
	 * @param params
	 *            Object[]
	 * @param curPage
	 *            int
	 * @param pageSize
	 *            int
	 * @return ListResult
	 * @throws ResKeyException
	 */
	public ListResult listResult(String connName, String sql, Object[] params,
			int curPage, int pageSize) throws ResKeyException {
		int total = 0;
		Vector result = new Vector();

		ListResult lr = new ListResult();
		lr.setTotal(total);
		lr.setResult(result);

		JdbcTemplate jt = new JdbcTemplate(new Connection(connName));
		try {
			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(sql);
			ResultIterator ri = jt.executeQuery(countsql, params);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				total = rr.getInt(1);
			}

			// 防止受到攻击时，curPage被置为很大，或者很小
			int totalpages = (int) Math.ceil((double) total / pageSize);
			if (curPage > totalpages) {
				curPage = totalpages;
			}
			if (curPage <= 0) {
				curPage = 1;
			}

			// jt = new JdbcTemplate(new Connection(connName));
/*
			if (total != 0) {
				jt.getConnection().setMaxRows(curPage * pageSize); // 尽量减少内存的使用
			}*/
			ri = jt.executeQuery(sql, params, curPage, pageSize);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
					result.addElement(getQObjectDb(new Integer(rr.getInt(1))));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
					result.addElement(getQObjectDb(rr.getString(1)));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
					result.addElement(getQObjectDb(new Long(rr.getLong(1))));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
					result.addElement(getQObjectDb(rr.getDate(1)));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
					HashMap keys = ((PrimaryKey) primaryKey.clone()).getKeys();
					Iterator ir = keys.keySet().iterator();
					while (ir.hasNext()) {
						String keyName = (String) ir.next();
						KeyUnit ku = (KeyUnit) keys.get(keyName);
						if (ku.getType() == PrimaryKey.TYPE_INT) {
							ku.setValue(new Integer(rr.getInt(ku.getOrders() + 1)));
						} else if (ku.getType() == PrimaryKey.TYPE_LONG) {
							ku.setValue(new Long(rr.getLong(ku.getOrders() + 1)));
						} else if (ku.getType() == PrimaryKey.TYPE_DATE) {
							ku.setValue(rr.getDate(ku.getOrders() + 1));
						} else {
							ku.setValue(rr.getString(ku.getOrders() + 1));
						}
					}
					result.addElement(getQObjectDb(keys));
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
			throw new ResKeyException(SkinUtil.ERR_DB);
		}finally{
			jt.close();
		}

		lr.setResult(result);
		lr.setTotal(total);
		return lr;
	}

	public ListResult listResult(String sql, int curPage, int pageSize)
			throws ResKeyException {
		return listResult(
				new JdbcTemplate(new Connection(table.getConnName())), sql,
				curPage, pageSize);
	}
	
	/**
	 * 
	 * @param sql
	 * @param idIndex  主键ID在sql语句中字段的索引，从1开始
	 * @param curPage
	 * @param pageSize
	 * @return
	 * @throws ResKeyException
	 */
	public ListResult listResult(String sql, int idIndex, int curPage, int pageSize)
		throws ResKeyException {
		return listResult(
			new JdbcTemplate(new Connection(table.getConnName())), sql, idIndex, 
			curPage, pageSize);
	}	

	public ListResult listResult(JdbcTemplate jt, String sql, int curPage,
			int pageSize) throws ResKeyException {
		return listResult(jt, sql, 1, curPage, pageSize);
	}

	/**
	 * 
	 * @param jt
	 * @param sql
	 * @param idIndex  主键ID在sql语句中字段的索引，从1开始
	 * @param curPage
	 * @param pageSize
	 * @return
	 * @throws ResKeyException
	 */
	public ListResult listResult(JdbcTemplate jt, String sql, int idIndex,
			int curPage, int pageSize) throws ResKeyException {
		int total = 0;
		ResultSet rs = null;
		Vector result = new Vector();

		ListResult lr = new ListResult();
		lr.setTotal(total);
		lr.setResult(result);

		Connection conn = jt.getConnection(); // new
												// Connection(Global.defaultDB);
		try {
			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(sql);
			rs = conn.executeQuery(countsql);
			if (rs != null && rs.next()) {
				total = rs.getInt(1);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}

			// 防止受到攻击时，curPage被置为很大，或者很小
			int totalpages = (int) Math.ceil((double) total / pageSize);
			if (curPage > totalpages) {
				curPage = totalpages;
			}
			if (curPage <= 0) {
				curPage = 1;
			}

			conn.prepareStatement(sql);

			if (total != 0) {
				conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
			}

			// rs = conn.executeQuery(sql);
			rs = conn.executePreQuery();
			if (rs == null) {
				return lr;
			} else {
				rs.setFetchSize(pageSize);
				int absoluteLocation = pageSize * (curPage - 1) + 1;
				if (rs.absolute(absoluteLocation) == false) {
					return lr;
				}
				do {
					if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
						result.addElement(getQObjectDb(new Integer(rs.getInt(idIndex))));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_STRING)
						result.addElement(getQObjectDb(rs.getString(idIndex)));
					else if (primaryKey.getType() == PrimaryKey.TYPE_LONG)
						result.addElement(getQObjectDb(new Long(rs.getLong(idIndex))));
					else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
						result.addElement(getQObjectDb(new Date(rs.getTimestamp(idIndex).getTime())));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
						HashMap keys = ((PrimaryKey) primaryKey.clone())
								.getKeys();
						Iterator ir = keys.keySet().iterator();
						while (ir.hasNext()) {
							String keyName = (String) ir.next();
							KeyUnit ku = (KeyUnit) keys.get(keyName);
							if (ku.getType() == PrimaryKey.TYPE_INT) {
								ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_LONG) {
								ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_DATE) {
								ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
							} else {
								ku.setValue(rs.getString(ku.getOrders() + 1));
							}
						}
						result.addElement(getQObjectDb(keys));
					}
				} while (rs.next());
			}
		} catch (SQLException e) {
			LogUtil.getLog(this.getClass()).error(
					"listResult:" + StrUtil.trace(e));
			throw new ResKeyException(SkinUtil.ERR_DB, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
			jt.close();
		}

		lr.setResult(result);
		lr.setTotal(total);
		return lr;
	}

	public Vector list() {
		
		return list(new JdbcTemplate(new Connection(table.getConnName())),
				table.getQueryList());
	}

	public Vector list(String sql) {
		return list(new JdbcTemplate(new Connection(table.getConnName())), sql);
	}

	public Vector list(String sql, Object[] params) {
		return list(new JdbcTemplate(new Connection(table.getConnName())), sql,
				params);
	}

	/**
	 * 全部的记录列表,当记录不多时，可以使用本方法，如列出友情链接，而当记录较多时，不宜使用
	 * 
	 * @return Vector
	 */
	public Vector list(JdbcTemplate jt, String sql) {
		int total = 0;
		ResultSet rs = null;
		Vector result = new Vector();
		Connection conn = jt.getConnection(); // Connection(Global.defaultDB);
		try {
			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(sql);
			rs = conn.executeQuery(countsql);
			if (rs != null && rs.next()) {
				total = rs.getInt(1);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}

			conn.prepareStatement(sql);
			if (total != 0) {
				// sets the limit of the maximum nuber of rows in a ResultSet
				// object
				conn.setMaxRows(total); // 尽量减少内存的使用
			}
			rs = conn.executePreQuery();
			if (rs == null) {
				return result;
			} else {
				// defines the number of rows that will be read from the
				// database when the ResultSet needs more rows
				rs.setFetchSize(total); // rs一次从POOL中所获取的记录数
				if (rs.absolute(1) == false) {
					return result;
				}
				do {
					if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
						result.addElement(getQObjectDb(new Integer(rs
										.getInt(1))));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
						result.addElement(getQObjectDb(rs.getString(1)));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
						result.addElement(getQObjectDb(new Long(rs.getLong(1))));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
						result.addElement(getQObjectDb(new Date(rs.getTimestamp(1).getTime())));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
						HashMap keys = ((PrimaryKey) primaryKey.clone()).getKeys();
						Iterator ir = keys.keySet().iterator();
						while (ir.hasNext()) {
							String keyName = (String) ir.next();
							KeyUnit ku = (KeyUnit) keys.get(keyName);
							if (ku.getType() == PrimaryKey.TYPE_INT) {
								ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_LONG) {
								ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_DATE) {
								ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
							} else {
								ku.setValue(rs.getString(ku.getOrders() + 1));
							}
						}
						result.addElement(getQObjectDb(keys));
					}
				} while (rs.next());
			}
		} catch (SQLException e) {
			LogUtil.getLog(this.getClass()).error("list: " + StrUtil.trace(e));
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
			jt.close();
		}
		return result;
	}

	/**
	 * 2007.1.7
	 * 添加，用于OA中sms_receive_record的处理,当记录不多时，可以使用本方法，如列出友情链接，而当记录较多时，不宜使用
	 * 
	 * @param jt
	 *            JdbcTemplate
	 * @param sql
	 *            String
	 * @param params
	 *            Object[]
	 * @return Vector
	 */
	public Vector list(JdbcTemplate jt, String sql, Object[] params) {
		int total = 0;
		Vector result = new Vector();
		// String connName = jt.getConnection().connName; // Connection(Global.defaultDB);

		try {
/*			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(sql);
			ResultIterator ri = jt.executeQuery(countsql, params);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				total = rr.getInt(1);
			}

			// jt = new JdbcTemplate(new Connection(connName));
			if (total != 0) {
				jt.getConnection().setMaxRows(total); // 尽量减少内存的使用
			}*/
			ResultIterator ri = jt.executeQuery(sql, params);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				if (primaryKey.getType() == PrimaryKey.TYPE_INT) {
					result.addElement(getQObjectDb(new Integer(rr.getInt(1))));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_STRING) {
					result.addElement(getQObjectDb(rr.getString(1)));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_LONG) {
					result.addElement(getQObjectDb(new Long(rr.getLong(1))));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_DATE) {
					result.addElement(getQObjectDb(rr.getDate(1)));
				} else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) {
					HashMap keys = ((PrimaryKey) primaryKey.clone()).getKeys();
					Iterator ir = keys.keySet().iterator();
					while (ir.hasNext()) {
						String keyName = (String) ir.next();
						KeyUnit ku = (KeyUnit) keys.get(keyName);
						if (ku.getType() == PrimaryKey.TYPE_INT) {
							ku.setValue(new Integer(rr.getInt(ku.getOrders() + 1)));
						} else if (ku.getType() == PrimaryKey.TYPE_LONG) {
							ku.setValue(new Long(rr.getLong(ku.getOrders() + 1)));
						} else if (ku.getType() == PrimaryKey.TYPE_DATE) {
							ku.setValue(rr.getDate(ku.getOrders() + 1));
						} else {
							ku.setValue(rr.getString(ku.getOrders() + 1));
						}
					}
					result.addElement(getQObjectDb(keys));
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(this.getClass()).error("list: " + e.getMessage());
		}finally{
			jt.close();
		}
		return result;
	}

	public QObjectBlockIterator getQObjects(String query, int startIndex,
			int endIndex) {
		return getQObjects(query, "", startIndex, endIndex);
	}

	/**
	 * 取得startIndex至endIndex-1个对象列表
	 * 
	 * @param query
	 *            String
	 * @param startIndex
	 *            int
	 * @param endIndex
	 *            int
	 * @return QObjectBlockIterator
	 */
	public QObjectBlockIterator getQObjects(String query, String groupName,
			int startIndex, int endIndex) {
		// 可能取得的infoBlock中的元素的顺序号小于endIndex
		Object[] blockValues = getQObjectBlock(query, groupName, startIndex);
		// LogUtil.getLog(getClass()).info("getObjects blockValues.lendth=" +
		// blockValues.length);

		// for (int i=0; i<blockValues.length; i++)
		// LogUtil.getLog(getClass()).info("getObjects i=" + i + " " +
		// blockValues[i]);
		return new QObjectBlockIterator(this, blockValues, query, groupName,
				startIndex, endIndex);
	}

	public Object[] getQObjectBlock(String sql, String groupName, int startIndex) {
		// First, discover what block number the results will be in.
		int blockSize = table.getBlockSize();
		int blockID = startIndex / blockSize;
		int blockStart = blockID * blockSize;

		// 取得根据主键的查询语句
		// String pk = primaryKey.getName();
		// String query = "select " + pk + " " + SQLFilter.getFromSql(sql); //
		// 当为联合查询时，此句中的pk会带来问题，因为缺少表的别名作为前缀 2006.6.9
		// String query = "select " + objectDb.getTableName() + "." + pk + " " +
		// SQLFilter.getFromSql(sql); // 加表名作为前缀在oracle中也不行
		String query = sql;

		// 缓存所用的key
		String key = query + blockID;

		Object[] objArray = null;
		if (listCachable) {
			try {
				objArray = (Object[]) QCache.getInstance().getFromGroup(key,
						cacheGroup + groupName);
			} catch (Exception e) {
				LogUtil.getLog(this.getClass()).error(
						"getQObjectBlock:" + e.getMessage());
			}
		}
		// If already in cache, return the block.
		if (objArray != null) {
			/**
			 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
			 * the case, it means two things: 1) We're at the end boundary of
			 * all the results. 2) If the start index is greater than the length
			 * of the current block, than there aren't really any results to
			 * return.
			 */
			Object[] objkeys = objArray;
			// 当startIndex过大时
			if (startIndex >= blockStart + objkeys.length) {
				// Return an empty array
				return ObjectDb.EMPTY_BLOCK;
			} else {
				return objkeys;
			}
		}
		// Otherwise, we have to load up the block from the database.
		else {
			Vector block = new Vector();
			ResultSet rs = null;
			Connection conn = new Connection(table.getConnName());
			try {
				// Set the maxium number of rows to end at the end of this
				// block.
				conn.setMaxRows(blockSize * (blockID + 1));
				rs = conn.executeQuery(query);
				// LogUtil.getLog(getClass()).info("getQObjectBlock2 query=" +
				// query);
				// Grab THREAD_BLOCK_ROWS rows at a time.
				conn.setFetchSize(blockSize);
				// Many JDBC drivers don't implement scrollable cursors the real
				// way, but instead load all results into memory. Looping
				// through
				// the results ourselves is more efficient.
				for (int i = 0; i < blockStart; i++) {
					rs.next();
				}
				// LogUtil.getLog(getClass()).info("getQObjectBlock2 blockStart="
				// + blockStart);

				// Keep reading results until the result set is exaughsted or
				// we come to the end of the block.
				int count = 0;
				while (rs.next() && count < blockSize) {
					// LogUtil.getLog(getClass()).info("getQObjectBlock2 blockStart="
					// + blockStart);
					// 如果不是复合主键
					if (primaryKey.getKeyCount() == 1) {
						if (primaryKey.getType() == PrimaryKey.TYPE_INT)
							block.addElement(new Integer(rs.getInt(1)));
						else if (primaryKey.getType() == PrimaryKey.TYPE_STRING)
							block.addElement(rs.getString(1));
						else if (primaryKey.getType() == PrimaryKey.TYPE_LONG)
							block.addElement(new Long(rs.getLong(1)));
						else if (primaryKey.getType() == PrimaryKey.TYPE_DATE)
							block.addElement(new java.util.Date(rs
									.getTimestamp(1).getTime()));
					} else if (primaryKey.getType() == PrimaryKey.TYPE_COMPOUND) { // 如果是复合主键
						HashMap keys = ((PrimaryKey) primaryKey.clone())
								.getKeys();
						Iterator ir = keys.keySet().iterator();
						while (ir.hasNext()) {
							String keyName = (String) ir.next();
							KeyUnit ku = (KeyUnit) keys.get(keyName);
							if (ku.getType() == PrimaryKey.TYPE_INT) {
								ku.setValue(new Integer(rs.getInt(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_LONG) {
								ku.setValue(new Long(rs.getLong(ku.getOrders() + 1)));
							} else if (ku.getType() == PrimaryKey.TYPE_DATE) {
								ku.setValue(new java.util.Date(rs.getTimestamp(ku.getOrders() + 1).getTime()));
							} else {
								ku.setValue(rs.getString(ku.getOrders() + 1));
							}
						}

						block.addElement(keys);
					}
					count++;
				}
			} catch (SQLException sqle) {
				LogUtil.getLog(this.getClass()).error(
						"getQObjectBlock2:" + sqle.getMessage());
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
					}
					rs = null;
				}
				if (conn != null) {
					conn.close();
					conn = null;
				}
			}
			int len = block.size();

			// LogUtil.getLog(getClass()).info("getQObjectBlock block.size=" +
			// len);

			Object[] objkeys = new Object[len];
			for (int i = 0; i < len; i++) {
				objkeys[i] = block.elementAt(i);
			}

			if (listCachable) {
				try {
					QCache.getInstance().putInGroup(key,
							cacheGroup + groupName, objkeys);
				} catch (Exception e) {
					LogUtil.getLog(this.getClass()).error(
							"getQObjectBlock3:" + e.getMessage());
				}
			}

			/**
			 * The actual block may be smaller than THREAD_BLOCK_SIZE. If that's
			 * the case, it means two things: 1) We're at the end boundary of
			 * all the results. 2) If the start index is greater than the length
			 * of the current block, than there aren't really any results to
			 * return.
			 */
			if (startIndex >= blockStart + objkeys.length) {
				// Return an empty array
				return ObjectDb.EMPTY_BLOCK;
			} else {
				return objkeys;
			}
		}
	}

	public String getString(String field) {
		return resultRecord.getString(field);
	}

	public boolean getBoolean(String field) {
		return resultRecord.getBoolean(field);
	}

	public int getInt(String field) {
		return resultRecord.getInt(field);
	}

	public java.util.Date getDate(String field) {
		return resultRecord.getDate(field);
	}

	public long getLong(String field) {
		return resultRecord.getLong(field);
	}

	public double getDouble(String field) {
		return resultRecord.getDouble(field);
	}

	public float getFloat(String field) {
		return resultRecord.getFloat(field);
	}

	public Object get(String field) {
		return resultRecord.get(field);
	}

	public void set(String field, Object value) {
		resultRecord.set(field, value);
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public void setObjCachable(boolean objCachable) {
		this.objCachable = objCachable;
	}

	public void setListCachable(boolean listCachable) {
		this.listCachable = listCachable;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public ResultRecord getResultRecord() {
		return resultRecord;
	}

	public boolean isObjCachable() {
		return objCachable;
	}

	public boolean isListCachable() {
		return listCachable;
	}

	protected boolean objCachable = true;
	protected boolean listCachable = true;

}
