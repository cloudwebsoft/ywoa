package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2020-01-23
 */
@Data
@TableName(value="user_role_priv")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RolePriv extends Model<RolePriv> {

    private static final long serialVersionUID = 1L;

    @TableId("roleCode")
    private String roleCode;

    private String priv;

    @Override
    protected Serializable pkVal() {
        return this.roleCode;
    }

}
