package cn.js.fan.security;

public enum PwdLevel {
    /**
     * EASY
     */
    EASY(0, "弱"),
    /**
     * MIDIUM
     */
    MIDIUM(1, "中"),
    /**
     * STRONG
     */
    STRONG(2, "强"),
    /**
     * VERY_STRONG
     */
    VERY_STRONG(3, "很强"),
    /**
     * EXTREMELY_STRONG
     */
    EXTREMELY_STRONG(4, "非常强");

    private int type;
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    PwdLevel(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
