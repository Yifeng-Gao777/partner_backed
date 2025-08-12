package com.gyf.partner_backend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author lihui
 */
@Data
public class UserRegistRequest implements Serializable {
    //序列化id
    public static final long seriaVersionUID = 3191241716373120793L;
    private String userAccount;
    private String userPassword;
    public String checkPassword;
//    public String planetCode;


}
