package com.redmoon.oa.netdisk;

import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.UserGroupDb;
import cn.js.fan.web.SkinUtil;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.*;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.person.UserMgr;


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
public class LeafPrivMgr {
    public LeafPrivMgr() {
    }

    public boolean add(HttpServletRequest request, String name, int type, String dirCode) throws
            ErrMsgException {
        LeafPriv lp = new LeafPriv(dirCode);
        boolean re = lp.add(name, type);
        if (re) {
            String msg = SkinUtil.LoadString(request, "res.module.netdisk",
                                             "msg_add");
            Privilege privilege = new Privilege();
            UserMgr um = new UserMgr();
            msg = msg.replaceAll("\\$user", um.getUserDb(privilege.getUser(request)).getRealName());
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(dirCode);
            msg = msg.replaceAll("\\$dir",leaf.getName());

            MessageDb md = new MessageDb();

            String c = msg;
            if (type == LeafPriv.TYPE_USER) {
                md.sendSysMsg(name, msg, c);
            } else {
                if (type == LeafPriv.TYPE_USERGROUP) {
                    UserGroupDb ugd = new UserGroupDb();
                    ugd = ugd.getUserGroupDb(name);

                    Vector vt = ugd.getAllUserOfGroup();
                    Iterator allUserOfGroup = vt.iterator();
                    while (allUserOfGroup.hasNext()) {
                        UserDb ud = (UserDb) allUserOfGroup.next();
                        md.sendSysMsg(ud.getName(), msg, c);
                    }
                }
            }
        }
        return re;
    }

    public boolean save(HttpServletRequest request,LeafPriv lp) throws
            ErrMsgException {
        String dirCode = lp.getDirCode();
        int type = lp.getType();
        String name = lp.getName();


        boolean re = lp.save();
        if (re) {
            String msg = SkinUtil.LoadString(request, "res.module.netdisk",
                                             "msg_modify");
            Privilege privilege = new Privilege();
            UserMgr um = new UserMgr();
            msg = msg.replaceAll("\\$user", um.getUserDb(privilege.getUser(request)).getRealName());
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(dirCode);
            msg = msg.replaceAll("\\$dir", leaf.getName());

            MessageDb md = new MessageDb();

            String c = msg;
            if (type == LeafPriv.TYPE_USER) {
                md.sendSysMsg(name, msg, c);
            } else {
                if (type == LeafPriv.TYPE_USERGROUP) {
                    UserGroupDb ugd = new UserGroupDb();
                    ugd = ugd.getUserGroupDb(name);

                    Vector vt = ugd.getAllUserOfGroup();
                    Iterator allUserOfGroup = vt.iterator();
                    while (allUserOfGroup.hasNext()) {
                        UserDb ud = (UserDb) allUserOfGroup.next();
                        md.sendSysMsg(ud.getName(), msg, c);
                    }
                }
            }
        }
        return re;
    }

    public boolean del(HttpServletRequest request, LeafPriv lp) throws
            ErrMsgException {
        String dirCode = lp.getDirCode();
        int type = lp.getType();
        String name = lp.getName();


        boolean re = lp.del();
        if (re) {
            String msg = SkinUtil.LoadString(request, "res.module.netdisk",
                                             "msg_del");
            Privilege privilege = new Privilege();
            UserMgr um = new UserMgr();
            msg = msg.replaceAll("\\$user", um.getUserDb(privilege.getUser(request)).getRealName());
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(dirCode);
            msg = msg.replaceAll("\\$dir", leaf.getName());

            MessageDb md = new MessageDb();

            String c = msg;
            if (type == LeafPriv.TYPE_USER) {
                md.sendSysMsg(name, msg, c);
            } else {
                if (type == LeafPriv.TYPE_USERGROUP) {
                    UserGroupDb ugd = new UserGroupDb();
                    ugd = ugd.getUserGroupDb(name);

                    Vector vt = ugd.getAllUserOfGroup();
                    Iterator allUserOfGroup = vt.iterator();
                    while (allUserOfGroup.hasNext()) {
                        UserDb ud = (UserDb) allUserOfGroup.next();
                        md.sendSysMsg(ud.getName(), msg, c);
                    }
                }
            }
        }
        return re;
    }


}

