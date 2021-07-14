package cn.js.fan.module.pvg;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import java.sql.SQLException;

public class PrivMgr {
    String connname;
    Logger logger = Logger.getLogger(PrivMgr.class.getName());

    public PrivMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Priv:connname is empty.");
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        PrivCheck pc = new PrivCheck();
        pc.checkAdd(request);

        Priv pv = new Priv();
        return pv.insert(pc.getPriv(), pc.getDesc());
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        PrivCheck pc = new PrivCheck();
        pc.checkUpdate(request);

        Priv pv = new Priv();
        pv.setPriv(pc.getPriv());
        pv.setDesc(pc.getDesc());
        return pv.store();
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        PrivCheck pc = new PrivCheck();
        pc.checkDel(request);

        Priv pv = new Priv();
        return pv.del(pc.getPriv());
    }

    public Priv getPriv(String priv) {
        return new Priv(priv);
    }

    public Priv[] getAllPriv() {
        String sql = "select priv from privilege";
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        Priv[] pv = null;
        try {
            ri = rmconn.executeQuery(sql);
            int count = ri.size();
            if (count>0)
                pv = new Priv[count];
            if (ri != null) {
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    String priv = rr.getString(1);
                    pv[i] = getPriv(priv);
                    i++;
                }
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return pv;
    }
}
