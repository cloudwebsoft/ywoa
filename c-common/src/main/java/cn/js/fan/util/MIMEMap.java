package cn.js.fan.util;

import java.util.HashMap;

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
public class MIMEMap {
    static HashMap map = new HashMap();
    static {
        map = new HashMap();
         map.put("abs", "audio/x");
         map.put("ai", "pplication/postscript");
         map.put("aif", "audio/x-aiff");
         map.put("aifc", "audio/x-aiff");
         map.put("aiff", "audio/x-aiff");
         map.put("aim", "application/x-aim");
         map.put("art", "image/x-jg");
         map.put("asf", "video/x-ms-asf");
         map.put("asx", "video/x-ms-asf");
         map.put("au", "audio/basic");
         map.put("avi", "video/x-msvideo");
         map.put("avx", "video/x-rad-screenplay");
         map.put("bcpio", "application/x-bcpio");
         map.put("bin", "application/octet-stream");
         map.put("bmp", "image/bmp");
         map.put("body", "text/html");
         map.put("cdf", "application/x-cdf");
         map.put("cer", "application/x-x509-ca-cert");
         map.put("class", "application/java");
         map.put("cpio", "application/x-cpio");
         map.put("csh", "application/x-csh");
         map.put("css", "text/css");
         map.put("dib", "image/bmp");
         map.put("doc", "application/msword");
         map.put("xls", "application/vnd.ms-excel");
         map.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
         map.put("dtd", "text/plain");
         map.put("dv", "video/x-dv");
         map.put("dvi", "application/x-dvi");
         map.put("eps", "application/postscript");
         map.put("etx", "text/x-setext");
         map.put("exe", "application/octet-stream");
         map.put("gif", "image/gif");
         map.put("gtar", "application/x-gtar");
         map.put("gz", "application/x-gzip");
         map.put("hdf", "application/x-hdf");
         map.put("hqx", "application/mac-binhex40");
         map.put("htc", "text/x-component");
         map.put("htm", "text/html");
         map.put("html", "text/html");
         map.put("hqx", "application/mac-binhex40");
         map.put("ief", "image/ief");
         map.put("jad", "text/vnd.sun.j2me.app-descriptor");
         map.put("jar", "application/java-archive");
         map.put("java", "text/plain");
         map.put("jnlp", "application/x-java-jnlp-file");
         map.put("jpe", "image/jpeg");
         map.put("jpeg", "image/jpeg");
         map.put("jpg", "image/jpeg");
         map.put("js", "text/javascript");
         map.put("jsf", "text/plain");
         map.put("jspf", "text/plain");
         map.put("kar", "audio/x-midi");
         map.put("latex", "application/x-latex");
         map.put("m3u", "audio/x-mpegurl");
         map.put("mac", "image/x-macpaint");
         map.put("man", "application/x-troff-man");
         map.put("me", "application/x-troff-me");
         map.put("mid", "audio/x-midi");
         map.put("midi", "audio/x-midi");
         map.put("mif", "application/x-mif");
         map.put("mov", "video/quicktime");
         map.put("movie", "video/x-sgi-movie");
         map.put("mp1", "audio/x-mpeg");
         map.put("mp2", "audio/x-mpeg");
         map.put("mp3", "audio/x-mpeg");
         map.put("mpa", "audio/x-mpeg");
         map.put("mpe", "video/mpeg");
         map.put("mpeg", "video/mpeg");
         map.put("mpega", "audio/x-mpeg");
         map.put("mpg", "video/mpeg");
         map.put("mpv2", "video/mpeg2");
         map.put("ms", "application/x-wais-source");
         map.put("nc", "application/x-netcdf");
         map.put("oda", "application/oda");
         map.put("pbm", "image/x-portable-bitmap");
         map.put("pct", "image/pict");
         map.put("pdf", "application/pdf");
         map.put("pgm", "image/x-portable-graymap");
         map.put("pic", "image/pict");
         map.put("pict", "image/pict");
         map.put("pls", "audio/x-scpls");
         map.put("png", "image/png");
         map.put("pnm", "image/x-portable-anymap");
         map.put("pnt", "image/x-macpaint");
         map.put("ppm", "image/x-portable-pixmap");
         map.put("ps", "application/postscript");
         map.put("psd", "image/x-photoshop");
         map.put("qt", "video/quicktime");
         map.put("qti", "image/x-quicktime");
         map.put("qtif", "image/x-quicktime");
         map.put("ras", "image/x-cmu-raster");
         map.put("rgb", "image/x-rgb");
         map.put("rm", "application/vnd.rn-realmedia");
         map.put("roff", "application/x-troff");
         map.put("rtf", "application/rtf");
         map.put("rtx", "text/richtext");
         map.put("sh", "application/x-sh");
         map.put("shar", "application/x-shar");
         map.put("smf", "audio/x-midi");
         map.put("sit", "application/x-stuffit");
         map.put("snd", "audio/basic");
         map.put("src", "application/x-wais-source");
         map.put("sv4cpio", "application/x-sv4cpio");
         map.put("sv4crc", "application/x-sv4crc");
         map.put("swf", "application/x-shockwave-flash");
         map.put("t", "application/x-troff");
         map.put("tar", "application/x-tar");
         map.put("tcl", "application/x-tcl");
         map.put("tex", "application/x-tex");
         map.put("texi", "application/x-texinfo");
         map.put("texinfo", "application/x-texinfo");
         map.put("tif", "image/tiff");
         map.put("tiff", "image/tiff");
         map.put("tr", "application/x-troff");
         map.put("tsv", "text/tab-separated-values");
         map.put("txt", "text/plain");
         map.put("ulw", "audio/basic");
         map.put("ustar", "application/x-ustar");
         map.put("xbm", "image/x-xbitmap");
         map.put("xml", "text/xml");
         map.put("xpm", "image/x-xpixmap");
         map.put("xsl", "text/xml");
         map.put("xwd", "image/x-xwindowdump");
         map.put("wav", "audio/x-wav");
         map.put("svg", "image/svg+xml");
         map.put("svgz", "image/svg+xml");
         map.put("wbmp", "image/vnd.wap.wbmp");
         map.put("wml", "text/vnd.wap.wml");
         map.put("wmlc", "application/vnd.wap.wmlc");
         map.put("wmls", "text/vnd.wap.wmlscript");
         map.put("wmlscriptc", "application/vnd.wap.wmlscriptc");
         map.put("wrl", "x-world/x-vrml");
         map.put("Z", "application/x-compress");
         map.put("z", "application/x-compress");
         map.put("zip", "application/zip");
    }

    public MIMEMap() {
    }

    public static String get(String ext) {
        return (String)map.get(ext);
    }
}
