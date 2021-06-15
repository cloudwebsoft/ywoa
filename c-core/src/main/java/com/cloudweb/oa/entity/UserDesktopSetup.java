package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
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
 * @since 2020-02-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserDesktopSetup extends Model<UserDesktopSetup> {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String userName;

    private String title;

    private String moreUrl;

    private Integer moduleRows;

    private String moduleCode;

    private String moduleItem;

    private Boolean isSystem;

    private Integer td;

    private Integer orderInTd;

    private Integer wordCount;

    private Long portalId;

    private Integer systemId;

    private Integer canDelete;

    private String metaData;

    private String icon;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
