package cn.js.fan.module.cms.ad;

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.Iterator;
import cn.js.fan.web.Global;

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
public class AdRender {
    String dirCode;
    int type;

    public AdRender(String dirCode, int type) {
        this.dirCode = dirCode;
        this.type = type;
    }

    public AdRender(String dirCode, String adType) {
        this.dirCode = dirCode;
        if (adType.equalsIgnoreCase("doc")) {
            type = AdDb.TYPE_DOC;
        } else if (adType.equalsIgnoreCase("docBottom")) {
            type = AdDb.TYPE_DOC_BOTTOM;
        } else if (adType.equalsIgnoreCase("float")) {
            type = AdDb.TYPE_FLOAT;
        } else if (adType.equalsIgnoreCase("couple")) {
            type = AdDb.TYPE_COUPLE_LEFT;
        } else if (adType.equalsIgnoreCase("header")) {
            type = AdDb.TYPE_HEADER;
        } else if (adType.equalsIgnoreCase("footer")) {
            type = AdDb.TYPE_FOOTER;
        }
        else if (adType.equals("couple_r")) {
            type = AdDb.TYPE_COUPLE_RIGHT;
        }
    }

    public String render(HttpServletRequest request) {
        String str = "";
        if (type == AdDb.TYPE_DOC) {
            str = renderDoc(request);
        } else if (type == AdDb.TYPE_DOC_BOTTOM) {
            str = renderDocBottom(request);
        } else if (type == AdDb.TYPE_FLOAT) {
            str = renderFloat(request);
        } else if (type == AdDb.TYPE_COUPLE_LEFT) {
            str = renderCouple(request);
        } else if (type == AdDb.TYPE_HEADER) {
            str = renderHeader(request);
        } else if (type == AdDb.TYPE_FOOTER) {
            str = renderFooter(request);
        }
        return str;
    }

    public String renderDocBottom(HttpServletRequest request) {
        String str = "";
        java.util.Vector ads = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_DOC_BOTTOM);
        // System.out.println(getClass() + " ads.size=" + ads.size());
        Iterator ir = ads.iterator();
        while (ir.hasNext()) {
            AdDb ad = (AdDb) ir.next();
            str +=
                    "<table width=100% align=center cellpadding=0 cellspacing=0>";
            str += "<tr><td align='center' valign='top'>";
            str += ad.render(request);
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderDoc(HttpServletRequest request) {
        String str = "";
        java.util.Vector ads = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_DOC);

        if (ads.size()>0) {
            AdDb ad = (AdDb)ads.elementAt(0);
            str += "<table width=180 height=200 border=0 align=right cellpadding=3 cellspacing=0>";
            str += "<tr><td align='right' valign='top'>";
            str += ad.render(request);
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderHeader(HttpServletRequest request) {
        Vector headerAds = AdDb.getADOnDir(dirCode, AdDb.TYPE_HEADER);
        // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " headerAds.size=" + headerAds.size());
        int n = headerAds.size();
        if (n == 1) {
            AdDb ad = (AdDb) headerAds.elementAt(0);
            return ad.render(request);
        } else if (n > 1) {
            Random random = new Random();
            int k = random.nextInt(n);
            // LogUtil.getLog(getClass()).info("n=" + n + " dirCode=" + dirCode + " k=" + k);

            AdDb ad = (AdDb) headerAds.elementAt(k);
            return ad.render(request);
        } else
            return "";
    }

    public String renderFooter(HttpServletRequest request) {
        String str = "";
        java.util.Vector footerAds = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_FOOTER);

        if (footerAds.size() == 1) {
            str +=
                    "<table width='98%' align='center'><tr><td align='center'>";
            AdDb ad = (AdDb) footerAds.elementAt(0);
            str += ad.render(request);
            str += "</td></tr></table>";
        } else if (footerAds.size() > 1) {
            str +=
                    "<table width='98%' align='center'><tr><td align='center'>";
            Random random = new Random();
            int k = random.nextInt(footerAds.size());
            AdDb ad = (AdDb) footerAds.elementAt(k);
            str += ad.render(request);
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderCouple(HttpServletRequest request) {
        String str = "";
        java.util.Vector ads = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_COUPLE_LEFT);

        // System.out.println(getClass() + " size=" + ads.size() + " dirCode=" + dirCode);

        java.util.Vector ads2 = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_COUPLE_RIGHT);
        if (ads.size() > 0) {
            AdDb ad = (AdDb) ads.elementAt(0);
            str +=
                    "<div id=AdLayer1 style='position: absolute;visibility:hidden;z-index:1' WIDTH=80 HEIGHT=260><div align=left style='cursor:hand;border-width:0px;' onClick=DobAdv_Show('AdLayer1',false)>×</div>" +
                    ad.render(request) + "</div>";
        }
        if (ads2.size() > 0) {
            AdDb ad = (AdDb) ads2.elementAt(0);
            str +=
                    "<div id=AdLayer2 style='position: absolute;visibility:hidden;z-index:1' WIDTH=80 HEIGHT=260><div align=right style='cursor:hand;border-width:0px;' onClick=DobAdv_Show('AdLayer2',false)>×</div>" +
                    ad.render(request) + "</div>";
        }
        if (ads.size()>0 || ads2.size()>0)
            str += "<script src='" + Global.getRootPath() + "/inc/ad_couple.js'></script>";

        return str;
    }

    public String renderFloat(HttpServletRequest request) {
        String str = "";
        java.util.Vector ads = AdDb.getADOnDir(
                dirCode, AdDb.TYPE_FLOAT);
        if (ads.size()>0) {
            AdDb ad = (AdDb)ads.elementAt(0);
            str += "<div id='ad_float' style='position:absolute'>";
            str += ad.render(request);
            str += "</div>";
            str += "<script src='" + Global.getRootPath() + "/inc/ad_float.js'></script>";
        }
        return str;
    }

}
