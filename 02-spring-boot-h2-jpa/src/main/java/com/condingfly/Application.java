package com.condingfly;

import com.condingfly.entity.UserEntity;
import com.condingfly.repository.UserRepository;
import com.condingfly.utils.SQLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.*;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
//        UserEntity user = new UserEntity();
//        user.setUsername("username");
//        user.setNickname("nickname");
//        user.setPhone("phone");
//        user.setDel(false);
//        Timestamp now = new Timestamp(System.currentTimeMillis());
//        user.setCreateAt(now);
//        user.setUpdateAt(now);
//        userRepository.save(user);
//        String sql = SQLUtils.insertSql("sys_user", Arrays.asList("id"));
////        String sql = "INSERT INTO sys_user (id, username, nickname, phone, del, create_at, update_at) VALUES (?,?,?,?,?,?,?);";
//        jdbcTemplate.update(sql, "i");
//        batchInsertByParams();
//        insertByOne();
        batchInsertByParams();
        String sql = "SELECT * FROM sys_user WHERE id IN (?) ";
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, "1");
        System.out.println("000000000000000");
    }

    private void batchInsertByParams() {
        String sql = SQLUtils.insertSql("sys_user", Arrays.asList("id", "username", "nickname", "phone", "del", "create_at", "update_at"));
//        String sql = "INSERT INTO sys_user (id, username, nickname, phone, del, create_at, update_at) VALUES (?,?,?,?,?,?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int size = 0;
        List<Object[]> params = new ArrayList();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            params.add(new Object[]{i + "", "username" + i, null, null, false, now, now});
            if ((++size)>=200) {
                size = 0;
                jdbcTemplate.batchUpdate(sql, params);
                params = new ArrayList();
            }
        }
        if (ObjectUtils.isEmpty(params)==false) {
            jdbcTemplate.batchUpdate(sql, params);
        }
        long end = System.currentTimeMillis();
        System.out.println("===>>>>  "+(end-start));
    }

    private void insertByOne() {
        String sql = SQLUtils.insertSql("sys_user", Arrays.asList("id", "username", "nickname", "phone", "del", "create_at", "update_at"));
//        String sql = "INSERT INTO sys_user (id, username, nickname, phone, del, create_at, update_at) VALUES (?,?,?,?,?,?,?);";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long start = System.currentTimeMillis();
        for (int i = 1000; i < 2000; i++) {
            jdbcTemplate.update(sql, i + "", "username" + i, null, null, false, now, now);
        }
        long end = System.currentTimeMillis();
        System.out.println("===>>>>  "+(end-start));
    }

    /**
     * private boolean insertByStatement() {
     *     String sql = "INSERT INTO `money` (`name`, `money`, `is_deleted`) VALUES (?, ?, ?);";
     *     return jdbcTemplate.update(sql, new PreparedStatementSetter() {
     *         @Override
     *         public void setValues(PreparedStatement preparedStatement) throws SQLException {
     *             preparedStatement.setString(1, "一灰灰3");
     *             preparedStatement.setInt(2, 300);
     *             byte b = 0;
     *             preparedStatement.setByte(3, b);
     *         }
     *     }) > 0;
     * }
     *
     * private boolean insertByStatement2() {
     *     String sql = "INSERT INTO `money` (`name`, `money`, `is_deleted`) VALUES (?, ?, ?);";
     *     return jdbcTemplate.update(new PreparedStatementCreator() {
     *         @Override
     *         public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
     *             PreparedStatement preparedStatement = connection.prepareStatement(sql);
     *             preparedStatement.setString(1, "一灰灰4");
     *             preparedStatement.setInt(2, 400);
     *             byte b = 0;
     *             preparedStatement.setByte(3, b);
     *             return preparedStatement;
     *         }
     *     }) > 0;
     * }
     */
    /**
     * 动态组装 简单sql语法
     *
     * @param tableName 表名
     * @param operation 操作标识符 select|delete|update ,默认为 select
     * @param mapData   数据的map集合
     * @param useMySQL  true|false , false 为使用动态组装SQL，true为使用自已的sql
     * @param mySql     自已的sql
     *                  注意：update 这里，where xxx = xxx ,的时候，mapData 里的键必须要有 Key_ 前缀（其他的 并不影响到）
     * @return
     * @throws Exception
     */
    public static String getSql(String tableName, String operation, Map<?, ?> mapData, boolean useMySQL, String mySql) throws Exception {
        String sql = null;
        // 使用组装sql的功能
        if (!useMySQL) {
            if (!(tableName != null && !tableName.equals("") && tableName.length() > 0)) {
                throw new Exception(" 参数 tableName 的值为空！");
            } else if (!(mapData != null && !mapData.equals("") && mapData.size() > 0)) {
                throw new Exception(" 参数 mapData 的值为空！");
            }
            // 操作标识 默认为 select
            String operations = "select";
            String condition = " a.* from " + tableName + " a where ";
            if (operation != null && !operation.equals("")) {
                if (operation.equals("update") || operation.equals("UPDATE")) {
                    operations = "update";
                    condition = " " + tableName + " a set ";
                } else if (operation.equals("delete") || operation.equals("DELETE")) {
                    operations = "delete";
                    condition = " from " + tableName + " a where ";
                } else if (operation.equals("insert") || operation.equals("INSERT")) {
                    operations = "insert";
                    condition = " into " + tableName + " (";
                    String link = "";
                    Iterator<?> iterator = mapData.keySet().iterator();
                    while (iterator.hasNext()) {
                        String next = (String) iterator.next();
                        condition += link + next;
                        link = ",";
                    }
                    condition += ") values( ";
                }
            }
            String value = "";
            String link = "";
            String keyValueOperations = " where ";
            Iterator<? extends Map.Entry<?, ?>> iterator = mapData.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> next = iterator.next();
                if (next.getValue() instanceof String) {
                    value = "'" + next.getValue() + "'";
                } else {
                    value = "" + next.getValue() + "";
                }
                if (next.getKey().toString().lastIndexOf("Key_") == -1) {
                    if (!operations.equals("insert")) {
                        if (operations.equals("select") || operations.equals("delete")) {
                            condition += link + "a." + next.getKey();
                            condition += "=" + value;
                            link = " and ";
                        } else {
                            condition += link + "a." + next.getKey();
                            condition += "=" + value;
                            link = ",";
                        }
                    } else {
                        condition += link + value;
                        link = ",";
                    }
                } else {
                    continue;
                }
            }

            // 组装 insert sql 的结尾
            if (operations.equals("insert")) {
                condition += ")";
            } else if (operations.equals("update")) { // 组装 update sql 的结尾
                condition += " where ";
                String and = "";
                Iterator<? extends Map.Entry<?, ?>> iterator1 = mapData.entrySet().iterator();
                while (iterator1.hasNext()) {
                    Map.Entry<?, ?> next = iterator1.next();
                    if (next.getValue() instanceof String) {
                        value = "'" + next.getValue() + "'";
                    } else {
                        value = "" + next.getValue() + "";
                    }
                    String key = next.getKey().toString();
                    if (key.lastIndexOf("Key_") != -1) {
                        key = key.substring(key.indexOf("Key_") + 4, key.length());
                        condition += and + "a." + key + "=" + value;
                        and = " and ";
                    }
                }
            }

            sql = operations + condition;
        } else { // 不使用组装sql的功能
            sql = mySql;
        }
        return sql;
    }

}