package com.redmoon.forum.plugin.render;

import com.redmoon.forum.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.DefaultRender;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.web.Global;
import cn.js.fan.util.DateUtil;

public class RenderMM extends DefaultRender {
    static Logger logger = Logger.getLogger(RenderMM.class.getName());

    public RenderMM() {
    }

    public String RenderAttachment(HttpServletRequest request, MsgDb md) {
        if (!isShowAttachment())
            return "";
        // if (md.getIsWebedit() == md.WEBEDIT_REDMOON) {
            if (md != null) {
                java.util.Vector attachments = md.
                                               getAttachments();
                java.util.Iterator ir = attachments.
                                        iterator();
                String str = "";
                Config cfg = Config.getInstance();
                while (ir.hasNext()) {
                    Attachment am = (Attachment)
                                    ir.next();
                    // 根据其diskName取出ext
                    String ext = FileUtil.getFileExt(am.getDiskName());
                    String link = Global.getRootPath() + "/" + cfg.getAttachmentPath() + "/" +
                                  am.getVisualPath() +
                                  "/" + am.getDiskName();
                    if (ext.equals("mp3") || ext.equals("wma") || ext.equals("mid")) {
                        // 使用realplay会导致IE崩溃
                        // str += "<div><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=80><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" + link + "'><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></div>";
                        if (md.getRootid() == md.getId()) {
                            str += "<div><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=70><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></div>";
                            str += "<BR>";
                        } else {
                            str += "<div><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=70><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><param name='AutoStart' value=0><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></div>";
                            str += "<BR>";
                        }
                    } else if (ext.equals("wmv") || ext.equals("mpg") || ext.equals("avi")) {
                        if (md.getRootid() == md.getId()) {
                            str += "<div><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=375><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></div>";
                            str += "<BR>";
                        } else {
                            str += "<div><object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=500 height=375><param name=ShowStatusBar value=-1><param name=Filename value='" +
                                    link + "'><param name='AutoStart' value=0><embed type=application/x-oleobject codebase=http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src='" +
                                    link +
                                    "'  width=500 height=70></embed></object></div>";
                            str += "<BR>";
                        }
                    } else if (ext.equals("rm") || ext.equals("rmvb")) {
                        if (md.getRootid() == md.getId())
                            str += "<div><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=380><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></div>";
                        else
                            str += "<div><OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=500 height=380><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=false></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=500><PARAM NAME=SRC VALUE='" +
                                    link + "'><PARAM NAME=AUTOSTART VALUE=0><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT></div>";
                        str += "<BR>";
                    }
                    else if (ext.equals("swf")) {
                        // 高级方式才显示附件为flash，其它方式已经在页面中嵌入了flash
                        if (md.getIsWebedit()==md.WEBEDIT_REDMOON) {
                            String linkFlash = request.getContextPath() +
                                               am.getVisualPath() + "/" +
                                               am.getDiskName();
                            str += "<div><a href=\"" + linkFlash +
                                    "\" TARGET=_blank><IMG SRC=\"" +
                                    request.getContextPath() + "images/pic/swf.gif\" border=0 alt=" + SkinUtil.LoadString(request,"res.forum.plugin.rendermm","msg") + " height=16 width=16>[" + SkinUtil.LoadString(request,"res.forum.plugin.rendermm","display_all_screen") + "]</a><br><OBJECT codeBase=http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=4,0,2,0 classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000 width=480 height=320><PARAM NAME=movie VALUE=\"" +
                                    linkFlash +
                                    "\"><PARAM NAME=quality VALUE=high><embed src=\"" +
                                    linkFlash + "\" quality=high pluginspage='http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' type='application/x-shockwave-flash'></embed></OBJECT></div>";
                            str += "<BR>";
                        }
                    }
                    else if (ext.equals("jpg") || ext.equals("gif") || ext.equals("bmp") || ext.equals("png")) {
                    	str += "<div class='attachImg'><img src='" + link + "'>";
                    	str += "<div>" + am.getDesc() + "</div>";
                    	str += "</div>";
                    }
                    else {
                        str +=
                                "<div><img src='" + Global.getRootPath() + "/netdisk/images/" + am.getIcon() + "'>";
                        str +=
                                "    &nbsp; <a target=_blank href='" + request.getContextPath() + "/forum/getfile.jsp?msgId=" + am.getMsgId() + "&attachId=" + am.getId() +
                                "'>" + am.getName() +
                              "</a>";
                        str += "&nbsp;" + am.getDesc();
                        str += "&nbsp;(" + DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm") + ", &nbsp;" + NumberUtil.round((double)am.getSize()/1024000, 3) + "&nbsp;M)";
                        String str1 = SkinUtil.LoadString(request, "info_attach_download_count").replaceFirst("\\$count", ""+am.getDownloadCount());
                        str += str1;
                        str += "</div>";
                    }
                }
                return str;
            }
        // }

        return "";
    }

}
