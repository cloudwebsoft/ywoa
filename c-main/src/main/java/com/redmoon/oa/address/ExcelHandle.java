package com.redmoon.oa.address;

import java.io.OutputStream;
import java.util.Iterator;

import com.cloudwebsoft.framework.util.LogUtil;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.redmoon.oa.basic.TreeSelectDb;

public class ExcelHandle {
    public ExcelHandle() {
    }

    /**
     * 输出Excel
     *
     * @param os
     */
    public static void writeExcel(OutputStream os, String sql) {
        try {
            //创建工作薄
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            //创建工作表
            WritableSheet ws = wwb.createSheet("通讯录", 0);
            AddressDb adb = new AddressDb();
            Iterator ir = adb.list(sql).iterator();
            Label labelA = new Label(0, 0, "姓名");
            Label labelB = new Label(1, 0, "单位");
            Label labelC = new Label(2, 0, "职务");
            Label labelD = new Label(3, 0, "手机");
            Label labelE = new Label(4, 0, "电话");
            Label labelF = new Label(5, 0, "短号");
            Label labelG = new Label(6, 0, "微信");
            Label labelH = new Label(7, 0, "Email");
            Label labelI = new Label(8, 0, "传真");
            Label labelJ = new Label(9, 0, "QQ");
            Label labelK = new Label(10, 0, "网页");
            Label labelL = new Label(11, 0, "邮编");
            Label labelM = new Label(12, 0, "地址");
            Label labelN = new Label(13, 0, "附注");

            ws.addCell(labelA);
            ws.addCell(labelB);
            ws.addCell(labelC);
            ws.addCell(labelD);
            ws.addCell(labelE);
            ws.addCell(labelF);
            ws.addCell(labelG);
            ws.addCell(labelH);
            ws.addCell(labelI);
            ws.addCell(labelJ);
            ws.addCell(labelK);
            ws.addCell(labelL);
            ws.addCell(labelM);
            ws.addCell(labelN);
            String person = "";
            int i = 1, j = 1;
            while (ir.hasNext()) {
                adb = (AddressDb) ir.next();
                person = adb.getPerson();
               
                Label labelA1 = new Label(0, j, person);
                Label labelB1 = new Label(1, j, adb.getCompany());
                Label labelC1 = new Label(2, j, adb.getJob());
                Label labelD1 = new Label(3, j, adb.getMobile());
                Label labelE1 = new Label(4, j, adb.getTel());
                Label labelF1 = new Label(5, j, adb.getShortMobile());
                Label labelG1 = new Label(6, j, adb.getWeixin());
                Label labelH1 = new Label(7, j, adb.getEmail());
                Label labelI1 = new Label(8, j, adb.getFax());
                Label labelJ1 = new Label(9, j, adb.getQQ());
                Label labelK1 = new Label(10, j, adb.getWeb());
                Label labelL1 = new Label(11, j, adb.getPostalcode());
                Label labelM1 = new Label(12, j, adb.getAddress());
                Label labelN1 = new Label(13, j, adb.getIntroduction());

                j++;
                ws.addCell(labelA1);
                ws.addCell(labelB1);
                ws.addCell(labelC1);
                ws.addCell(labelD1);
                ws.addCell(labelE1);
                ws.addCell(labelF1);
                ws.addCell(labelG1);
                ws.addCell(labelH1);
                ws.addCell(labelI1);
                ws.addCell(labelJ1);
                ws.addCell(labelK1);
                ws.addCell(labelL1);
                ws.addCell(labelM1);
                ws.addCell(labelN1);
            }
            wwb.write();
            wwb.close();
        } catch (Exception e) {
            LogUtil.getLog(ExcelHandle.class).error(e);
        }
    }
    /**
     * 输出Excel
     *
     * @param os
     */
    public static void writeOfficeEquipExcel(OutputStream os, String sql) {
        try { //创建工作薄
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            //创建工作表
            WritableSheet ws = wwb.createSheet("品名", 0);
            TreeSelectDb tsd = new TreeSelectDb();
            Iterator ir = tsd.list(sql).iterator();
            Label labelA = new Label(0, 0, "名称");
            
            ws.addCell(labelA);
            
            String name = "";
            int i = 1, j = 1;
            while (ir.hasNext()) {
            	tsd = (TreeSelectDb)ir.next();
                name = tsd.getName();
               
                Label labelA1 = new Label(0, j, name);

                j++;
                ws.addCell(labelA1);
            }
            wwb.write();
            wwb.close();
        } catch (Exception e) {
            LogUtil.getLog(ExcelHandle.class).error(e);
        }
    }
}
