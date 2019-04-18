package com.redmoon.oa.basic;

import java.util.Iterator;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SelectDb {
	public static int TYPE_LIST = 0;
	public static int TYPE_TREE = 1;
	
    public SelectDb() {
        super();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getOrders() {
        return orders;
    }
    
    public Vector getOptions() {
    	return getOptions(new JdbcTemplate());
    }

    public Vector getOptions(JdbcTemplate jt) {
    	return getOptions(jt, "");
    }
    
    public Vector getOptions(JdbcTemplate jt, String key) {
        options = new Vector();
        String sql = "select * from oa_select_option where code=? order by orders";
        if (!"".equals(key)) {
        	sql = "select * from oa_select_option where code=? and name like " + StrUtil.sqlstr("%" + key + "%") + " order by orders";
        }
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {code});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                SelectOptionDb sod = new SelectOptionDb();
                sod.setId(rr.getInt("id"));
                sod.setCode(rr.getString("code"));
                sod.setName(rr.getString("name"));
                sod.setValue(rr.getString("value"));
                sod.setOrders(rr.getInt("orders"));
                sod.setDefault(rr.getBoolean("is_default"));
                sod.setColor(StrUtil.getNullStr(rr.getString("color")));
                sod.setOpen(rr.getInt("is_open")==1);
                options.add(sod);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return options;    	
    }    

    public void setOptions(Vector options) {
        this.options = options;
    }

    private String code;
    private String name;
    private int orders = 0;

    public Vector options;
    
    /**
     * 类别，列表型或树型
     */
    private int type = TYPE_LIST;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	private int kind;

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}
	

	   /**
	    * 只有列表状的才有默认值
	    * @param code String
	    * @return String
	    */
	   public String getDefaultValue() {
	       if (getType() == SelectDb.TYPE_LIST) {
	           Vector v = getOptions(new JdbcTemplate());
	           Iterator ir = v.iterator();
	           while (ir.hasNext()) {
	               SelectOptionDb sod = (SelectOptionDb) ir.next();
	               if (sod.isDefault())
	                   return sod.getValue();
	           }
	       } 
	       /*
	       else {
	           TreeSelectDb tsd = new TreeSelectDb();
	           tsd = tsd.getTreeSelectDb(sd.getCode());
	           Vector vt = new Vector();
	           try {
	               tsd.getAllChild(vt, tsd);
	           } catch (ErrMsgException ex) {
	               ex.printStackTrace();
	               LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
	           }
	           
	           Iterator ir = vt.iterator();
	           while (ir.hasNext()) {
	               tsd = (TreeSelectDb)ir.next();
	               if (tsd.getis
	           }
	       }
	       */
	       return "";
	    }	
}
