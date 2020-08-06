package com.condingfly.sql;

/**
 * @author hgp
 * @date 20-8-5
 */
public class Predicate {
    private String field;
    private Condition condition;
    public Predicate() { }

    public Predicate(String field, Condition condition) {
        this.field = field;
        this.condition = condition;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
