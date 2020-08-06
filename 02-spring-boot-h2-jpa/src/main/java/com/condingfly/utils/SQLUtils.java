package com.condingfly.utils;

import com.condingfly.sql.Condition;
import com.condingfly.sql.Predicate;
import com.condingfly.sql.SqlContent;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hgp
 * @date 20-8-5
 */
public class SQLUtils {
    public static String insertSql(String tableName, List<String> fields) {
        Assert.isTrue(StringUtils.hasText(tableName), "表名不允许为空");
        Assert.isTrue(ObjectUtils.isEmpty(fields)==false, "字段不允许为空");
        StringBuffer sb = new StringBuffer("INSERT INTO "+tableName+"(");
        StringBuffer value = new StringBuffer("VALUES(");
        int i = 0;
        for (i = 0; i < fields.size()-1; i++) {
            sb.append(fields.get(i)+",");
            value.append("?,");
        }
        sb.append(fields.get(i)+") ");
        value.append("?);");
        return sb.toString() + value.toString();
    }

    public static String updateSql(String tableName, List<String> fields) {
        StringBuffer sb = new StringBuffer("UPDATE "+tableName+" SET ");
        for (int i = 0; i < fields.size()-1; i++) {
            sb.append(fields.get(i)+"=?,");
        }
        sb.append(fields.get(fields.size()-1)+"=? ");
        return sb.toString();
    }

    public static SqlContent updateSql(String tableName, List<String> fields, List<Object> values) {
        return updateSql(tableName, fields, values, null, null);
    }

    // 这个只能拼接简单的SQL语句
    // 如果查null或者notNull数据, paramMap必须把该字段传进来, value就是Null或者notNull
    public static SqlContent updateSql(String tableName, List<String> fields, List<Object> values, List<Predicate> predicates, Map<String,Object> paramMap) {
        Assert.isTrue(StringUtils.hasText(tableName), "表名不允许为空");
        Assert.isTrue(!ObjectUtils.isEmpty(fields), "字段不允许为空");
        Assert.isTrue(!ObjectUtils.isEmpty(values), "values不允许为空");
        StringBuffer sb = new StringBuffer(updateSql(tableName, fields));
        List<Object> params = new ArrayList(values);
        if (!ObjectUtils.isEmpty(predicates) && !ObjectUtils.isEmpty(paramMap)) {
            predicates = predicates.stream().filter(o->!ObjectUtils.isEmpty(paramMap.get(o.getField()))).collect(Collectors.toList());
            if (!ObjectUtils.isEmpty(predicates)) {
                sb.append("WHERE ");
                for (int i = 0; i < predicates.size()-1; i++) {
                    assemblyCondition(sb, predicates.get(i), paramMap, params);
                    sb.append(" AND ");
                }
                assemblyCondition(sb, predicates.get(predicates.size()-1), paramMap, params);
            }
        }
        return new SqlContent(sb.toString(), params);
    }

    private static void assemblyCondition(StringBuffer sb, Predicate predicate, Map<String,Object> paramMap, List<Object> params) {
        Condition condition = predicate.getCondition();
        String field = predicate.getField();
        Object value = paramMap.get(field);
        switch (condition) {
            case eq:
                sb.append(field+"=?");
                params.add(value);
                break;
            case ne:
                sb.append(field+"!=?");
                params.add(value);
                break;
            case gt:
                sb.append(field+">?");
                params.add(value);
                break;
            case ge:
                sb.append(field+">=?");
                params.add(value);
                break;
            case lt:
                sb.append(field+"<?");
                params.add(value);
                break;
            case le:
                sb.append(field+"<=?");
                params.add(value);
                break;
            case between:
                if (value instanceof Map) {
                    Map<String, Object> map = (Map)value;
                    Object beginValue = map.get("beginValue");
                    if (!ObjectUtils.isEmpty(beginValue)) {
                        sb.append(field+">=?");
                        params.add(beginValue);
                    }
                    Object endValue = map.get("endValue");
                    if (!ObjectUtils.isEmpty(endValue)) {
                        sb.append(field+"<=?");
                        params.add(endValue);
                    }
                }
                break;
            case in:
                // jdbcTemplate 不能识别 IN 后面的数组问号,数组参数必须一个个用问号占位符
                if (value instanceof List) {
                    List<Object> list = (List)value;
                    sb.append(field+" IN "+assemblyListParams(list));
                    params.addAll(list);
                }
                break;
            case notIn:
                if (value instanceof List) {
                    List<Object> list = (List)value;
                    sb.append(field+" NOT IN "+assemblyListParams(list));
                    params.addAll(list);
                }
                break;
            case Null:
                sb.append(field+" IS NULL");
                break;
            case notNull:
                sb.append(field+" IS NOT NULL");
                break;
            case like:
                sb.append(field+" LIKE ?");
                params.add("%"+value+"%");
                break;
            case notLike:
                sb.append(field+" NOT LIKE ?");
                params.add("%"+value+"%");
                break;
            case prefixLike:
                sb.append(field+" LIKE ?");
                params.add(value+"%");
                break;
            case notPrefixLike:
                sb.append(field+" NOT LIKE ?");
                params.add(value+"%");
                break;
            case suffixLike:
                sb.append(field+" LIKE ?");
                params.add("%"+value);
                break;
            case notSuffixLike:
                sb.append(field+" NOT LIKE ?");
                params.add(value+"%");
                break;
            case regex:
                break;
            case notRegex:
                break;
        }
    }

    public static String assemblyListParams(List<Object> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for (int i = 0; i < params.size()-1; i++) {
            sb.append("?,");
        }
        sb.append("?)");
        return sb.toString();
    }

    public static void main(String[] args) {
        String tableName = "sys_user";
        List<String> fields = Arrays.asList("id","username","nickname");
        List<Object> values = Arrays.asList("id","username","nickname");
        System.out.println(updateSql(tableName, fields, values).getSql());

        // updateSql(String tableName, List<String> fields, List<Predicate> predicates, Map<String,Object> paramMap)
        List<Predicate> predicates = new ArrayList();
        predicates.add(new Predicate("username", Condition.in));
        predicates.add(new Predicate("nickname", Condition.ne));
        Map<String,Object> paramMap = new HashMap() {{
            put("username", Arrays.asList("关键字1", "关键字2", "关键字3", "关键字4"));
            put("nickname", "昵称");
        }};
        SqlContent sqlContent = updateSql(tableName, fields, values, predicates, paramMap);
        System.out.println(sqlContent.getSql());
    }
}
