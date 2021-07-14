package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
@TableName(value="oa_menu")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Menu extends Model<Menu> {

    private static final long serialVersionUID = 1L;

    private String code;

    private String name;

    @TableField("isHome")
    private Boolean isHome;

    private String link;

    private String parentCode;

    private String rootCode;

    private Integer orders;

    private Integer childCount;

    private String addDate;

    private Boolean islocked;

    private Integer type;

    private Integer layer;

    private String preCode;

    private Integer width;

    private Integer isHasPath;

    private Integer isResource;

    private String target;

    private String pvg;

    private String icon;

    private Integer isUse;

    private Integer isNav;

    private String formCode;

    private Integer canRepeat;

    private String bigIcon;

    private Integer isWidget;

    private Integer widgetWidth;

    private Integer widgetHeight;

    private Integer kind;

    private Boolean isSystem;

    private String fontIcon;

    private String description;

    /**
     * 实际生成的链接
     */
    @TableField(exist=false)
    private String realLink;

    /**
     * 含有父节点的名称
     */
    @TableField(exist = false)
    private String fullName;

    @Override
    protected Serializable pkVal() {
        return this.code;
    }

}
