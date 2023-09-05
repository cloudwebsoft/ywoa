package com.cloudweb.oa.utils.file.check.enums;

import lombok.Getter;
import org.springframework.lang.NonNull;

/**
 * @author cube.li
 * @date 2021/6/25 20:29
 * @description 文件类型
 * https://www.jianshu.com/p/be3f4c26c39a
 */
@Getter
public enum FileType {

    /**
     * JPEG  (jpg)
     */
    JPEG("JPEG", "FFD8FF"),

    JPG("JPG", "FFD8FF"),

    /**
     * PNG
     */
    PNG("PNG", "89504E47"),

    /**
     * GIF
     */
    GIF("GIF", "47494638"),

    /**
     * TIFF (tif)
     */
    TIFF("TIF", "49492A00"),

    /**
     * Windows bitmap (bmp)
     */
    BMP("BMP", "424D"),

    /**
     * 16色位图(bmp)
     */
    BMP_16("BMP", "424D228C010000000000"),

    /**
     * 24位位图(bmp)
     */
    BMP_24("BMP", "424D8240090000000000"),

    /**
     * 256色位图(bmp)
     */
    BMP_256("BMP", "424D8E1B030000000000"),

    /**
     * CAD  (dwg)
     */
    DWG("DWG", "41433130"),

    /**
     * Adobe photoshop  (psd)
     */
    PSD("PSD", "38425053"),

    /**
     * Rich Text Format  (rtf)
     */
    RTF("RTF", "7B5C727466"),

    /**
     * XML
     */
    XML("XML", "3C3F786D6C"),

    /**
     * HTML (html)
     */
    HTML("HTML", "68746D6C3E"),

    /**
     * Email [thorough only] (eml)
     */
    EML("EML", "44656C69766572792D646174653A"),

    /**
     * Outlook Express (dbx)
     */
    DBX("DBX", "CFAD12FEC5FD746F "),

    /**
     * Outlook (pst)
     */
    PST("", "2142444E"),

    /**
     * doc;xls;dot;ppt;xla;ppa;pps;pot;msi;sdw;db
     */
    OLE2("OLE2", "0xD0CF11E0A1B11AE1"),

    /**
     * Microsoft Word/Excel 注意：word 和 excel的文件头一样
     */
    XLS("XLS", "D0CF11E0"),

    /**
     * Microsoft Word/Excel 注意：word 和 excel的文件头一样
     */
    DOC("DOC", "D0CF11E0"),

    /**
     * Microsoft Word/Excel 2007以上版本文件 注意：word 和 excel的文件头一样
     */
    DOCX("DOCX", "504B0304"),

    /**
     * Microsoft Word/Excel 2007以上版本文件 注意：word 和 excel的文件头一样 504B030414000600080000002100
     */
    XLSX("XLSX", "504B0304"),

    /**
     * Microsoft Access (mdb)
     */
    MDB("MDB", "5374616E64617264204A"),

    /**
     * Adobe Acrobat (pdf) 255044462D312E
     */
    PDF("PDF", "25504446"),

    /**
     * Windows Password  (pwl)
     */
    PWL("PWL", "E3828596"),

    /**
     * WAVE (wav)
     */
    WAV("WAV", "57415645"),

    /**
     * AVI
     */
    AVI("AVI", "41564920"),

    /**
     * Real Audio (ram)
     */
    RAM("RAM", "2E7261FD"),

    /**
     * Real Media (rm) rmvb/rm相同
     */
    RM("RM", "2E524D46"),

    /**
     * Real Media (rm) rmvb/rm相同
     */
    RMVB("RMVB", "2E524D46000000120001"),

    /**
     * MPEG (mpg)
     */
    MPG("MPG", "000001BA"),

    /**
     * Quicktime  (mov)
     */
    MOV("MOV", "6D6F6F76"),

    /**
     * MIDI (mid)
     */
    MID("MID", "4D546864"),

    /**
     * MP4
     */
    MP4("MP4", "00000020667479706D70"),

    /**
     * MP3
     */
    MP3("MP3", "49443303000000002176"),

    /**
     * FLV
     */
    FLV("FLV", "464C5601050000000900"),

    /**
     * torrent
     */
    TORRENT("TORRENT", "6431303A637265617465"),

    /**
     * JSP Archive
     */
    JSP("JSP", "3C2540207061676520"),

    /**
     * JAVA Archive
     */
    JAVA("JAVA", "7061636B61676520"),

    /**
     * CLASS Archive
     */
    CLASS("CLASS", "CAFEBABE0000002E00"),

    /**
     * JAR Archive
     */
    JAR("JAR", "504B03040A000000"),

    /**
     * MF Archive
     */
    MF("MF", "4D616E69666573742D56"),

    /**
     * EXE Archive
     */
    EXE("EXE", "4D5A9000030000000400"),

    /**
     * ELF Executable
     */
    ELF("ELF", "7F454C4601010100"),

    /**
     * Lotus 123 v1
     */
    WK1("WK1", "2000604060"),

    /**
     * Lotus 123 v3
     */
    WK3("WK3", "00001A0000100400"),

    /**
     * Lotus 123 v5
     */
    WK4("WK4", "00001A0002100400"),

    /**
     * Lotus WordPro v9
     */
    LWP("LWP", "576F726450726F"),

    /**
     * Sage(sly.or.srt.or.slt;sly;srt;slt)
     */
    SLY("SLY", "53520100");


    /**
     * 后缀 大写字母
     */
    private final String suffix;

    /**
     * 魔数
     */
    private final String magicNumber;

    FileType(String suffix, String magicNumber) {
        this.suffix = suffix;
        this.magicNumber = magicNumber;
    }

    @NonNull
    public static FileType getBySuffix(String suffix) {
        for (FileType fileType : values()) {
            if (fileType.getSuffix().equals(suffix.toUpperCase())) {
                return fileType;
            }
        }
        throw new IllegalArgumentException("unsupported file suffix : " + suffix);
    }
}