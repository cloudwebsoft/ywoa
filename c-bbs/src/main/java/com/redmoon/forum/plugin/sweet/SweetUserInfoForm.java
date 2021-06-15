package com.redmoon.forum.plugin.sweet;

import cn.js.fan.base.AbstractForm;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.Privilege;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

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
public class SweetUserInfoForm extends AbstractForm {
    SweetUserInfoDb sweetUserInfoDb = new SweetUserInfoDb();

    public SweetUserInfoForm() {
    }

    public void checkAdd(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String name = ParamUtil.get(request, "userName");
        if (name.equals("")) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.sweet", "ERR_NICK")); //呢称不能为空
        }
        String strage = ParamUtil.get(request, "age").trim();
        if (!StrUtil.isNumeric(strage)) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.sweet", "ERR_AGE")); //请填写年龄
        }
        int age = Integer.parseInt(strage);

        sweetUserInfoDb.setName(name);
        sweetUserInfoDb.setGender(ParamUtil.get(request, "gender"));
        sweetUserInfoDb.setAge(age);
        String manager = privilege.getUser(request);
        sweetUserInfoDb.setManager(manager);
        try {
            String y = ParamUtil.get(request, "year");
            String m = ParamUtil.get(request, "month");
            String d = ParamUtil.get(request, "day");
            sweetUserInfoDb.setBirthday(DateUtil.parse(y + "-" + m + "-" + d,
                    "yyyy-MM-dd"));
            sweetUserInfoDb.setMarriage(ParamUtil.get(request, "marriage"));
            sweetUserInfoDb.setProvince(ParamUtil.get(request, "province"));
            sweetUserInfoDb.setWorkAddress(ParamUtil.get(request, "workAddress"));
            sweetUserInfoDb.setTall(ParamUtil.getInt(request, "tall"));
            sweetUserInfoDb.setXueli(ParamUtil.get(request, "xueli"));
            sweetUserInfoDb.setJob(ParamUtil.get(request, "job"));
            sweetUserInfoDb.setSalary(ParamUtil.get(request, "salary"));
            sweetUserInfoDb.setAddress(ParamUtil.get(request, "address"));
            sweetUserInfoDb.setPostCode(ParamUtil.getInt(request, "postCode"));
            sweetUserInfoDb.setTel(ParamUtil.get(request, "tel"));
            sweetUserInfoDb.setEmail(ParamUtil.get(request, "email"));
            sweetUserInfoDb.setOICQ(ParamUtil.getInt(request, "OICQ"));
            sweetUserInfoDb.setICQ(ParamUtil.get(request, "ICQ"));
            sweetUserInfoDb.setMSN(ParamUtil.get(request, "MSN"));
            sweetUserInfoDb.setDescription(ParamUtil.get(request, "desc"));
            sweetUserInfoDb.setSport(ParamUtil.get(request, "sport"));
            sweetUserInfoDb.setBook(ParamUtil.get(request, "book"));
            sweetUserInfoDb.setMusic(ParamUtil.get(request, "music"));
            sweetUserInfoDb.setCelebrity(ParamUtil.get(request, "celebrity"));
            sweetUserInfoDb.setPhoto(ParamUtil.get(request, "photo"));
            sweetUserInfoDb.setHobby(ParamUtil.get(request, "hobby"));
            sweetUserInfoDb.setFrendType(ParamUtil.get(request, "frendType"));
            sweetUserInfoDb.setFrendAge(ParamUtil.get(request, "frendAge"));
            sweetUserInfoDb.setFrendTall(ParamUtil.get(request, "frendTall"));
            sweetUserInfoDb.setFrendMarriage(ParamUtil.get(request,
                    "frendMarriage"));
            sweetUserInfoDb.setFrendProvince(ParamUtil.get(request,
                    "frendProvince"));
            sweetUserInfoDb.setFrendXueli(ParamUtil.get(request, "frendXueli"));
            sweetUserInfoDb.setFrendSalary(ParamUtil.get(request, "frendSalary"));
            sweetUserInfoDb.setFrendRequire(ParamUtil.get(request,
                    "frendRequire"));
            sweetUserInfoDb.setChecked(ParamUtil.getBoolean(request,
                    "isChecked", false));
        } catch (Exception e) {
            logger.error("checkAdd:" + e.getMessage());
        }
    }

    public void checkEdit(HttpServletRequest request) throws ErrMsgException {
        String userName = ParamUtil.get(request, "userName");
        if (userName.equals("")) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.sweet", "ERR_NICK")); //呢称不能为空
        }
        sweetUserInfoDb = sweetUserInfoDb.getSweetUserInfoDb(userName);
        if (!sweetUserInfoDb.isLoaded()) {
            String str = SkinUtil.LoadString(request, "res.forum.plugin.sweet",
                                             "ERR_USER");
            str = StrUtil.format(str, new Object[] {"" + userName});
            throw new ErrMsgException(str);

        }

        String strage = ParamUtil.get(request, "age").trim();
        if (!StrUtil.isNumeric(strage)) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.sweet", "ERR_AGE")); //请填写年龄
        }
        int age = Integer.parseInt(strage);

        sweetUserInfoDb.setGender(ParamUtil.get(request, "gender"));
        sweetUserInfoDb.setAge(age);
        Privilege privilege = new Privilege();
        sweetUserInfoDb.setManager(privilege.getUser(request));

        try {
            String y = ParamUtil.get(request, "year");
            String m = ParamUtil.get(request, "month");
            String d = ParamUtil.get(request, "day");
            sweetUserInfoDb.setBirthday(DateUtil.parse(y + "-" + m + "-" +
                    d, "yyyy-MM-dd"));
            sweetUserInfoDb.setMarriage(ParamUtil.get(request, "marriage"));
            sweetUserInfoDb.setProvince(ParamUtil.get(request, "province"));
            sweetUserInfoDb.setWorkAddress(ParamUtil.get(request,
                    "workAddress"));
            sweetUserInfoDb.setTall(ParamUtil.getInt(request, "tall"));
            sweetUserInfoDb.setXueli(ParamUtil.get(request, "xueli"));
            sweetUserInfoDb.setJob(ParamUtil.get(request, "job"));
            sweetUserInfoDb.setSalary(ParamUtil.get(request, "salary"));
            sweetUserInfoDb.setAddress(ParamUtil.get(request, "address"));
            sweetUserInfoDb.setPostCode(ParamUtil.getInt(request,
                    "postCode"));
            sweetUserInfoDb.setTel(ParamUtil.get(request, "tel"));
            sweetUserInfoDb.setEmail(ParamUtil.get(request, "email"));
            sweetUserInfoDb.setOICQ(ParamUtil.getInt(request, "OICQ"));
            sweetUserInfoDb.setICQ(ParamUtil.get(request, "ICQ"));
            sweetUserInfoDb.setMSN(ParamUtil.get(request, "MSN"));
            sweetUserInfoDb.setDescription(ParamUtil.get(request, "desc"));
            sweetUserInfoDb.setSport(ParamUtil.get(request, "sport"));
            sweetUserInfoDb.setBook(ParamUtil.get(request, "book"));
            sweetUserInfoDb.setMusic(ParamUtil.get(request, "music"));
            sweetUserInfoDb.setCelebrity(ParamUtil.get(request, "celebrity"));
            // sweetUserInfoDb.setPhoto(ParamUtil.get(request, "photo"));
            sweetUserInfoDb.setHobby(ParamUtil.get(request, "hobby"));
            sweetUserInfoDb.setFrendType(ParamUtil.get(request, "frendType"));
            sweetUserInfoDb.setFrendAge(ParamUtil.get(request, "frendAge"));
            sweetUserInfoDb.setFrendTall(ParamUtil.get(request, "frendTall"));
            sweetUserInfoDb.setFrendMarriage(ParamUtil.get(request,
                    "frendMarriage"));
            sweetUserInfoDb.setFrendProvince(ParamUtil.get(request,
                    "frendProvince"));
            sweetUserInfoDb.setFrendXueli(ParamUtil.get(request,
                    "frendXueli"));
            sweetUserInfoDb.setFrendSalary(ParamUtil.get(request,
                    "frendSalary"));
            sweetUserInfoDb.setFrendRequire(ParamUtil.get(request,
                    "frendRequire"));
            sweetUserInfoDb.setChecked(ParamUtil.getBoolean(request,
                    "isChecked", false));
            sweetUserInfoDb.setMember(ParamUtil.getInt(request, "member"));
        } catch (Exception e) {
            logger.error("checkEdit:" + e.getMessage());
        }
    }

    public String checkDel(HttpServletRequest request) throws
            ErrMsgException {
        init();
        String name = ParamUtil.get(request, "name");
        if (name.equals("")) {
            log(SkinUtil.LoadString(request,"res.forum.plugin.sweet","ERR_USER_NAME"));//用户名不能为空!
        }
        report();
        this.sweetUserInfoDb.setName(name);
        return name;
    }

}
