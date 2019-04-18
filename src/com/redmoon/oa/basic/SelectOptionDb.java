package com.redmoon.oa.basic;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.StrUtil;
import cn.js.fan.db.ResultRecord;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;

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
public class SelectOptionDb {
    public SelectOptionDb() {
        super();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getOrders() {
        return orders;
    }

    public int getId() {
        return id;
    }

    public boolean isDefault() {
        return tolerant;
    }

    public void setDefault(boolean tolerant) {
        this.tolerant = tolerant;
    }

    /**
     * 根据值取得option的名称
     * @param code String
     * @param optionValue String
     * @return String
     */
    public String getOptionName(String code, String optionValue) {
        String sql = "select name from oa_select_option where code=? and value=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {code, optionValue});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return rr.getString("name");
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return "";
    }
    
    /**
     * 根据选项的名称取到值
     * @param code
     * @param optionName
     * @return
     */
    public String getOptionValue(String code, String optionName) {
        String sql = "select value from oa_select_option where code=? and name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {code, optionName});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return rr.getString(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return "";
    }
        

    private String code;
    private String name;
    private String value;
    private int orders = 0;
    private boolean tolerant = false;
    private int id;
    private String color;
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * @param open the open to set
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	/** 
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
	}


	private boolean open = true;
}
