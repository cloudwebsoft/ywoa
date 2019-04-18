package com.redmoon.forum.plugin.witkey;

import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;

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
public class WitkeyUserMgr {
    public WitkeyUserMgr() {
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        String realName = ParamUtil.get(request, "realName");
        if (realName.equals("") || realName.length() > 20) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addRealNameError"));
        }

        String msgRootId = ParamUtil.get(request, "msgId");
        if (msgRootId.equals("") || !StrUtil.isNumeric(msgRootId)) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addMsgRootIdError"));
        }

        String city = ParamUtil.get(request, "city");
        if (city.equals("") || city.length() > 50) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addCityError"));
        }

        String tel = ParamUtil.get(request, "tel");
        if (tel.length() > 30) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addTelError"));
        }

        String oicq = ParamUtil.get(request, "oicq");
        if (oicq.length() > 15) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addOicqError"));
        }


        String otherContact = ParamUtil.get(request, "otherContact");
        if (otherContact.length() > 200) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addOtherContactError"));
        }


        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        WitkeyUserDb wud = new WitkeyUserDb();

        wud.setMsgRootId(Long.parseLong(msgRootId));
        wud.setUserName(userName);
        wud.setRealName(realName);
        wud.setCity(city);
        wud.setTel(tel);
        wud.setOicq(oicq);
        wud.setOtherContact(otherContact);

        return wud.create();
    }

    public boolean edit(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
        String realName = ParamUtil.get(request, "realName");
        if (realName.equals("") || realName.length() > 20) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addRealNameError"));
        }

        String city = ParamUtil.get(request, "city");
        if (city.equals("") || city.length() > 50) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addCityError"));
        }

        String contact = ParamUtil.get(request, "contact");
        if (contact.equals("") || contact.length() > 255) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addContactError"));
        }

        String msgRootId = ParamUtil.get(request, "msgId");
        if (msgRootId.equals("") || !StrUtil.isNumeric(msgRootId)) {
            throw new ErrMsgException(WitkeySkin.LoadString(request, "addMsgRootIdError"));
        }

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        WitkeyUserDb wud = new WitkeyUserDb();
        wud = wud.getWitkeyUserDb(Long.parseLong(msgRootId), userName);

        wud.setRealName(realName);
        wud.setCity(city);
        wud.setContact(contact);

        return wud.save();
    }

}
