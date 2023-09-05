package com.redmoon.oa.address;

/**
 * <p>Title: </p>
 * <p>
 * <p>Description: </p>
 * <p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.io.*;

import cn.js.fan.util.*;
import jxl.*;
import jxl.read.biff.*;
import jxl.write.Label;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.person.UserDb;

public class ExcelRead {
    Workbook book = null;

    public ExcelRead() {
    }

    public ExcelRead(String xlspath, String username, int type, String group) throws ErrMsgException,
            IndexOutOfBoundsException {
        boolean re = true;
        String person = "", job = "", tel = "", mobile = "", email = "",
                address = "", postalcode = "", introduction = "";
        String beepPager = "", city = "", company = "", companyCity = "",
                companyCountry = "", companyPostcode = "";
        String companyProvice = "", companyStreet = "", country = "",
                familyname = "", fax = "", firstname = "", middlename = "",
                nickname = "";
        String operationFax = "", operationPhone = "", operationweb = "",
                province = "", street = "", web = "", QQ = "", MSN = "";

        try {
            book = Workbook.getWorkbook(new java.io.File(xlspath));
            //获取sheet表的总行数、总列数
            jxl.Sheet rs = book.getSheet(0);
            int rsRows = rs.getRows();
            int rsColumns = rs.getColumns();
            Cell cc;
            String strc[] = new String[rsColumns];
            for (int i = 1; i < rsRows; i++) {
                for (int j = 0; j < rsColumns; j++) {
                    cc = rs.getCell(j, i);
                    strc[j] = cc.getContents();
                }
                person = strc[3];
                nickname = strc[4];
                email = strc[5];
                street = strc[6];
                tel = strc[11];
                fax = strc[12];
                mobile = strc[13];
                web = strc[14];
                company = strc[15];
                operationweb = strc[20];
                operationPhone = strc[21];
                operationFax = strc[22];
                job = strc[25];
                address = strc[27];
                AddressDb addr = new AddressDb();
                addr.setPerson(person);
                addr.setJob(job);
                addr.setTel(tel);
                addr.setMobile(mobile);
                addr.setEmail(email);
                addr.setAddress(address);
                addr.setPostalcode(postalcode);
                addr.setIntroduction(introduction);
                addr.setBeepPager(beepPager);
                addr.setCity(city);
                addr.setCompany(company);
                addr.setCompanyCity(companyCity);
                addr.setCompanyCountry(companyCountry);
                addr.setCompanyPostcode(companyPostcode);
                addr.setCompanyProvice(companyProvice);
                addr.setCompanyStreet(companyStreet);
                addr.setCountry(country);
                addr.setFamilyname(familyname);
                addr.setFax(fax);
                addr.setFirstname(firstname);
                addr.setMiddleName(middlename);
                addr.setNickname(nickname);
                addr.setOperationFax(operationFax);
                addr.setOperationPhone(operationPhone);
                addr.setOperationweb(operationweb);
                addr.setProvince(province);
                addr.setStreet(street);
                addr.setWeb(web);
                addr.setUserName(username);//添加userName.
                addr.setType(type);
                addr.setTypeId(group);
                addr.create();
            }
        } catch (BiffException ex) {
            LogUtil.getLog(getClass()).error(ex);
        } catch (IOException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
    }

    public void Excelhad(String xlspath, String username, int type, String group) throws ErrMsgException,
            IndexOutOfBoundsException {
        UserDb user = new UserDb();
        user = user.getUserDb(username);

        String person = "", job = "", tel = "", mobile = "", email = "",
                address = "", postalcode = "", introduction = "";
        String beepPager = "", city = "", company = "", companyCity = "",
                companyCountry = "", companyPostcode = "";
        String companyProvice = "", companyStreet = "", country = "",
                familyname = "", fax = "", firstname = "", middlename = "",
                nickname = "";
        String operationFax = "", operationPhone = "", operationweb = "",
                province = "", street = "", web = "", QQ = "", MSN = "";
        String errmsg = "", department = "", weixin = "";

        try {
            book = Workbook.getWorkbook(new java.io.File(xlspath));
            //获取sheet表的总行数、总列数
            jxl.Sheet rs = book.getSheet(0);
            int rsRows = rs.getRows();
            int rsColumns = rs.getColumns();
            Cell cc;
            int columns;
            String fieldsValue;
            int fields[] = new int[rsColumns];
            String strc[] = new String[rsColumns];
            int k = 0;
            for (int m = 0; m < rsColumns; m++) {
                cc = rs.getCell(m, 0);
                fieldsValue = cc.getContents();
                if (fieldsValue.equals("姓名")) {
                    fields[0] = m;
                    k = 3;
                }

                LogUtil.getLog(getClass()).info("fieldsValue=" + fieldsValue + ":" + m);

                if (fieldsValue.equals("单位")) fields[1] = m;
                if (fieldsValue.equals("职务")) fields[2] = m;
                if (fieldsValue.equals("手机")) fields[3] = m;
                if (fieldsValue.equals("电话")) fields[4] = m;
                if (fieldsValue.equals("短号")) fields[5] = m;
                if (fieldsValue.equals("微信")) fields[6] = m;
                if (fieldsValue.equals("Email")) fields[7] = m;
                if (fieldsValue.equals("传真")) fields[8] = m;
                if (fieldsValue.equals("QQ")) fields[9] = m;
                if (fieldsValue.equals("网页")) fields[10] = m;
                if (fieldsValue.equals("邮编")) fields[11] = m;
                if (fieldsValue.equals("地址")) fields[12] = m;
                if (fieldsValue.equals("附注")) fields[13] = m;
            }
            if (k < 2) throw new ErrMsgException("导入Excel格式不正确，请检查！");
            for (int i = 1; i < rsRows; i++) {
                for (int j = 0; j <= 13; j++) {
                    columns = fields[j];
                    cc = rs.getCell(columns, i);
                    strc[j] = cc.getContents();
                }
                // 写入数据库
                person = strc[0];
                company = strc[1];
                job = strc[2];
                mobile = strc[3];
                tel = strc[4];
                MSN = strc[5];
                weixin = strc[6];
                email = strc[7];
                fax = strc[8];
                QQ = strc[9];
                web = strc[10];
                postalcode = strc[11];
                address = strc[12];
                introduction = strc[13];

                AddressDb addr = new AddressDb();
                addr.setPerson(person);
                addr.setCompany(company);
                addr.setJob(job);
                addr.setMobile(mobile);
                addr.setEmail(email);
                addr.setTel(tel);
                addr.setFax(fax);
                addr.setQQ(QQ);
                addr.setMSN(MSN);
                addr.setWeb(web);
                addr.setPostalcode(postalcode);
                addr.setAddress(address);
                addr.setIntroduction(introduction);
                addr.setUserName(username);
                addr.setType(type);
                addr.setTypeId(group);
                addr.setUnitCode(user.getUnitCode());
                addr.setWeixin(weixin);
                addr.create();
            }
        } catch (BiffException ex) {
            throw new ErrMsgException("请上传.xls格式的文件！");
        } catch (IOException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

    }
}
