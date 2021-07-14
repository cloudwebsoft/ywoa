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
 * @since 2020-02-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserOfGroup extends Model<UserOfGroup> {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String groupCode;


    @Override
    protected Serializable pkVal() {
        return this.userName;
    }

}
