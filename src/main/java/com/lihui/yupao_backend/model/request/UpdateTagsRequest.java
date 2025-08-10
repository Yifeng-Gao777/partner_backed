package com.lihui.yupao_backend.model.request;

import java.util.List;

public class UpdateTagsRequest {

    // 旧标签（即要替换的标签）
    private String oldTag;

    // 新标签（用于替换的标签）
    private String newTag;

    // 操作类型（用于指定操作，比如 "add", "remove", "update"）
    private String operation;

    // getter 和 setter 方法
    public String getOldTag() {
        return oldTag;
    }

    public void setOldTag(String oldTag) {
        this.oldTag = oldTag;
    }

    public String getNewTag() {
        return newTag;
    }

    public void setNewTag(String newTag) {
        this.newTag = newTag;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "UpdateTagsRequest{" +
                "oldTag='" + oldTag + '\'' +
                ", newTag='" + newTag + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}


