package com.redmoon.blog;

import java.util.Iterator;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.PrimaryKey;

/**
 * <p>Title:脚印 </p>
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
public class FootprintDb extends QObjectDb {
    public FootprintDb() {
    }
    
    public void delOfBlog(long blogId) {
    	String sql = "select msg_id,user_name from "+ getTable().getName() + " where blog_id=?";
    	Iterator ir = list(sql, new Object[]{new Long(blogId)}).iterator();
    	while (ir.hasNext()) {
    		FootprintDb fd = (FootprintDb)ir.next();
    		try {
				fd.del();
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }    

    public boolean create(JdbcTemplate jt, Object[] params) throws
            ResKeyException {
        FootprintDb fpd = getFootprintDb(((Long)params[0]).longValue(), (String)params[2]);
        if (fpd!=null) {
            throw new ResKeyException("res.blog.UserConfigDb", "footprint_repeat");
        }
        boolean re = super.create(jt, params);
        return re;
    }

    public FootprintDb getFootprintDb(long msgId, String userName) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("msg_id", new Long(msgId));
        pk.setKeyValue("user_name", userName);

        return (FootprintDb)getQObjectDb(pk.getKeys());
    }

}
