package com.condingfly.sql;

import java.util.List;

/**
 * @author hgp
 * @date 20-8-6
 */
public class SqlContent {
    private String sql; // sql
    private List<Object> placeholders;// 占位符, 按照问号顺序排列好的占位符
    public SqlContent() { }

    public SqlContent(String sql, List<Object> placeholders) {
        this.sql = sql;
        this.placeholders = placeholders;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(List<Object> placeholders) {
        this.placeholders = placeholders;
    }
}
