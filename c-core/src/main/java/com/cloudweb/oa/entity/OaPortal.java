package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class OaPortal extends Model<OaPortal> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String userName;

    private String name;

    private Integer orders;

    private Long systemId;

    /**
     * 图标位置
     */
    private String icon;

    private Integer isFixed;

    private String depts;

    private String roles;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
