package com.redmoon.oa.util;

import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.OSUtil;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;

import java.io.File;
import java.io.IOException;

public class AsianFontProvider extends XMLWorkerFontProvider {

    @Override
    //上下的方法都是可以的
    /* public Font getFont(final String fontname, String encoding, float size, final int style) {
         BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);

         String fntname = fontname;
         if (fntname == null) {
             使用的windows里的宋体，可将其文件放资源文件中引入
              *请确保simsun.ttc字体在windows下支持
              *我是将simsun.ttc字体打进maven的jar包中使用

             fntname = "fonts/simsun.ttc";
         }
         if (size == 0) {
             size = 4;
         }
         return super.getFont(fntname, encoding, size, style);
     }*/


    public Font getFont(final String fontname, final String encoding, final boolean embedded, final float size, final int style, final BaseColor color) {
        /*BaseFont bf = null;
        try {
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
        }
        Font font = new Font(bf, size, style, color);
        font.setColor(color);*/

        String prefixFont = "";
        if (OSUtil.isWindows()) {
            prefixFont = "C:\\Windows\\Fonts" + File.separator;
        } else {
            prefixFont = "/usr/share/fonts/chinese" + File.separator;
        }

        BaseFont bf = null;
        try {
            // 服务器上有simsun.ttc字体（本机也有），但是下行会报：com.itextpdf.text.DocumentException: Font 'C:\Windows\Fonts\simsun.ttc' with 'Identity-H' is not recognized.
            // bf = BaseFont.createFont(prefixFont + "simsun.ttc", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            // 在字体路径的后面加了一个“,0”解决了，猜测可能跟字体组有关，因为雅黑有三种，常规、加粗、极细
            bf = BaseFont.createFont(prefixFont + "simsun.ttc,0", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // 本机与服务器都未成功
            // bf = BaseFont.createFont(prefixFont + "simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            // 华文细黑，服务器上没有该字体，需安装，否则会报：java.io.IOException: C:\Windows\Fonts\STXIHEI.TTF not found as file or resource.
            // 安装以后，还是会报此错，是否需重启Windows ？
            // bf = BaseFont.createFont(prefixFont + "STXIHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        Font font = new Font(bf, size, style, color);

        return font;
    }
}