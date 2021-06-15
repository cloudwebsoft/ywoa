package com.redmoon.forum.ad;

import javax.servlet.jsp.tagext.*;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import java.util.Vector;
import java.util.Random;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;

public class AdTag extends BodyTagSupport {
    int type = AdDb.TYPE_TEXT;
    String boardCode;

    Logger logger = Logger.getLogger(AdTag.class.getName());

    public AdTag() {
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        try {
            // BodyContent bc = getBodyContent();
            // String body = bc.getString();
            String str = "";
            if (type == AdDb.TYPE_TEXT) {
                str = renderText();
            }
            else if (type==AdDb.TYPE_TOPIC_RIGHT) {
                str = renderTopicRight();
            }
            else if (type==AdDb.TYPE_FLOAT) {
                str = renderFloat();
            }
            else if (type==AdDb.TYPE_COUPLE) {
                str = renderCouple();
            }
            else if (type==AdDb.TYPE_HEADER) {
                str = renderHeader();
            }
            else if (type==AdDb.TYPE_FOOTER) {
                str = renderFooter();
            }
            else if (type==AdDb.TYPE_TOPIC_AFTER) {
                str = renderTopicAfter();
            }
            pageContext.getOut().print(str);
        } catch (Exception e) {
            logger.error("doEndTag:" + e.getMessage() + " " + StrUtil.trace(e));
        }
        return EVAL_PAGE;
    }

    public String renderHeader() {
        if (boardCode.equals(""))
            boardCode = com.redmoon.forum.Leaf.CODE_ROOT;
        Vector headerAds = AdDb.getADOnBoard(boardCode, AdDb.TYPE_HEADER);
        // LogUtil.getLog(getClass()).info("boardCode=" + boardCode + " headerAds.size=" + headerAds.size());
        int n = headerAds.size();
        if (n == 1) {
            AdDb ad = (AdDb) headerAds.elementAt(0);
            return ad.render((HttpServletRequest) pageContext.
                             getRequest());
        } else if (n > 1) {
            Random random = new Random();
            int k = random.nextInt(n);
            // LogUtil.getLog(getClass()).info("n=" + n + " boardCode=" + boardCode + " k=" + k);

            AdDb ad = (AdDb) headerAds.elementAt(k);
            return ad.render((HttpServletRequest) pageContext.
                             getRequest());
        } else
            return "";
    }

    public String renderFooter() {
        if (boardCode.equals(""))
            boardCode = com.redmoon.forum.Leaf.CODE_ROOT;
        String str = "";
        java.util.Vector footerAds = com.redmoon.forum.ad.AdDb.getADOnBoard(
                boardCode, com.redmoon.forum.ad.AdDb.TYPE_FOOTER);
        // LogUtil.getLog(getClass()).info("boardCode=" + boardCode + " footerAds.size=" + footerAds.size());

        if (footerAds.size() == 1) {
            str +=
                    "<table width=\"98%\" align=\"center\"><tr><td align=\"center\">";
            AdDb ad = (AdDb) footerAds.elementAt(0);
            str += ad.render((HttpServletRequest) pageContext.
                             getRequest());
            str += "</td></tr></table>";
        } else if (footerAds.size() > 1) {
            str +=
                    "<table width=\"98%\" align=\"center\"><tr><td align=\"center\">";
            Random random = new Random();
            int k = random.nextInt(footerAds.size());
            AdDb ad = (AdDb) footerAds.elementAt(k);
            str += ad.render((HttpServletRequest) pageContext.
                             getRequest());
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderCouple() {
        String str = "";
        java.util.Vector ads = AdDb.getADOnBoard(
                boardCode, AdDb.TYPE_COUPLE);
        java.util.Vector ads2 = AdDb.getADOnBoard(
                boardCode, AdDb.TYPE_COUPLE_RIGHT);
        if (ads.size() > 0) {
            AdDb ad = (AdDb) ads.elementAt(0);
            str +=
                    "<div id=AdLayer1 style='position: absolute;visibility:hidden;z-index:1' WIDTH=80 HEIGHT=260>" +
                    ad.render((HttpServletRequest) pageContext.
                              getRequest()) + "<div align=left style='cursor:hand;border-width:0px;' onClick='DobAdv_Show(\"AdLayer1\", false)'>×</div></div>";
        }
        if (ads2.size() > 0) {
            AdDb ad = (AdDb) ads2.elementAt(0);
            str +=
                    "<div id=AdLayer2 style='position: absolute;visibility:hidden;z-index:1' WIDTH=80 HEIGHT=260>" +
                    ad.render((HttpServletRequest) pageContext.
                              getRequest()) + "<div align=right style='cursor:hand;border-width:0px;' onClick='DobAdv_Show(\"AdLayer2\", false)'>×</div></div>";
        }
        // System.out.println(getClass() + " " + ads.size() + "--" + ads2.size());
        if (ads.size()>0 || ads2.size()>0) {
            str += "<script src=\"" + Global.getRootPath() + "/inc/ad_couple.js\"></script>";
        }
        return str;
    }

    public String renderFloat() {
        String str = "";
        java.util.Vector ads = com.redmoon.forum.ad.AdDb.getADOnBoard(
                boardCode, com.redmoon.forum.ad.AdDb.TYPE_FLOAT);
        if (ads.size()>0) {
            AdDb ad = (AdDb)ads.elementAt(0);
            str += "<div id=\"ad_float\" style=\"position:absolute\">";
            str += ad.render((HttpServletRequest) pageContext.
                                  getRequest());
            str += "</div>";
            str += "<script src=\"../inc/ad_float.js\"></script>";
        }
        return str;
    }

    public String renderTopicRight() {
        String str = "";
        java.util.Vector ads = com.redmoon.forum.ad.AdDb.getADOnBoard(
                boardCode, com.redmoon.forum.ad.AdDb.TYPE_TOPIC_RIGHT);
        if (ads.size()>0) {
            AdDb ad = (AdDb)ads.elementAt(0);
            str += "<table width=180 height=200 border=0 align=right cellpadding=3 cellspacing=0>";
            str += "<tr><td align='right' valign='top'>";
            str += ad.render((HttpServletRequest) pageContext.
                                  getRequest());
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderTopicAfter() {
        String str = "";
        java.util.Vector afterAds = com.redmoon.forum.ad.AdDb.getADOnBoard(
                boardCode, com.redmoon.forum.ad.AdDb.TYPE_TOPIC_AFTER);
        // LogUtil.getLog(getClass()).info("boardCode=" + boardCode + " afterAds.size=" + afterAds.size());

        if (afterAds.size() == 1) {
            str +=
                    "<table width=\"98%\" align=\"center\"><tr><td align=\"center\">";
            AdDb ad = (AdDb) afterAds.elementAt(0);
            str += ad.render((HttpServletRequest) pageContext.
                             getRequest());
            str += "</td></tr></table>";
        } else if (afterAds.size() > 1) {
            str +=
                    "<table width=\"98%\" align=\"center\"><tr><td align=\"center\">";
            Random random = new Random();
            int k = random.nextInt(afterAds.size());
            AdDb ad = (AdDb) afterAds.elementAt(k);
            str += ad.render((HttpServletRequest) pageContext.
                             getRequest());
            str += "</td></tr></table>";
        }
        return str;
    }

    public String renderText() {
        String str = "";
        java.util.Vector ads = com.redmoon.forum.ad.AdDb.getADOnBoard(
                boardCode, com.redmoon.forum.ad.AdDb.TYPE_TEXT);
        int adsLen = ads.size();

        if (adsLen > 0) {
            int num = 5;
            int cols = 0;
            str += "<div id=\"adTextBox\">";
            str += "<table width=\"100%\" align=\"center\" cellSpacing=\"1\" cellpadding=\"3\">";
            int a = 0;
            int row = 0; // 记录行数
            for (a = 0; a < adsLen; a++) {
                // 每行放五个
                if (cols == 0)
                    str += "<tr>";
                str +=
                        "<td align=\"center\" width=\"19%\" height=\"30px\">";
                com.redmoon.forum.ad.AdDb ad = (com.redmoon.forum.ad.
                                                AdDb)
                                               ads.elementAt(a);
                str +=
                        ad.render((HttpServletRequest) pageContext.
                                  getRequest());
                str += "</td>";
                cols++;
                if (cols == num) {
                    str += "</tr>";
                    cols = 0;
                    row ++;
                }
            }
            if (cols != 0) {
                if (row>0) {
                    int n = num - cols;
                    for (int k = 0; k < n; k++) {
                        str += "<td>&nbsp;</td>";
                    }
                }
                str += "</tr>";
            }
            str += "</table>";
            str += "</div>";
        }
        return str;
    }

}
