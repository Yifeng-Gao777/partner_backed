package com.gyf.partner_backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页删除请求
 *
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -5860707094194210842L;

    private long id;
}
