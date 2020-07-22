package com.condingfly.utils;

import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个是工具类的
 *
 * @author hgp
 * @date 20-7-16
 */
public class RedisGeoUtils {
    /**
     * 添加地理坐标
     *
     * @param key
     * @param point
     * @param member
     */
    public static void addGeo(RedisTemplate redisTemplate, String key, Point point, Object member) {
        redisTemplate.opsForGeo().add(key, point, member);
    }

    /**
     * latitude
     *
     * @param key
     * @param longitude
     * @param latitude
     * @param member
     */
    public static void addGeo(RedisTemplate redisTemplate, String key, double longitude, double latitude, Object member) {
        redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), member);
    }

    /**
     * 删除地理坐标
     *
     * @param key
     * @param member
     */
    public static void delGeo(RedisTemplate redisTemplate, String key, Object member) {
        redisTemplate.opsForGeo().remove(key, member);
    }

    /**
     * 获取多个member的坐标
     *
     * @param key
     * @param members
     * @return
     */
    public static List<Point> queryPointsOfMembers(RedisTemplate redisTemplate, String key, Object... members) {
        return redisTemplate.opsForGeo().position(key, members);
    }

    /**
     * 查看两个member的距离, 返回的距离单位是米
     *
     * @param key
     * @param member1
     * @param member2
     * @return
     */
    public static Double distance(RedisTemplate redisTemplate, String key, Object member1, Object member2) {
        return distance(redisTemplate, key, member1, member2, RedisGeoCommands.DistanceUnit.METERS);
    }

    /**
     * 查看两个member的距离
     *
     * @param key
     * @param member1
     * @param member2
     * @param metric
     * @return
     */
    public static Double distance(RedisTemplate redisTemplate, String key, Object member1, Object member2, Metric metric) {
        Distance distance = redisTemplate.opsForGeo().distance(key, member1, member2, metric);
        if (distance != null) {
            return distance.getValue();
        } else {
            return null;
        }
    }

    /**
     * 通过指定的经度, 纬度, 距离(以米为单位), 返回的数量来查询坐标, 返回类型是 GeoResults<RedisGeoCommands.GeoLocation>
     *
     * @param key       redis保存的key
     * @param longitude 经度 x轴
     * @param latitude  纬度 y轴
     * @param distance  距离(以米为单位)
     * @param limit     返回数据量
     */
    public static GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByXY(RedisTemplate redisTemplate,
                                                                             String key,
                                                                             double longitude,
                                                                             double latitude,
                                                                             double distance,
                                                                             int limit) {
        return nearPointsGeoByXY(redisTemplate, key, longitude, latitude, distance, RedisGeoCommands.DistanceUnit.METERS, limit);
    }

    /**
     * 通过指定的经度, 纬度, 距离, 距离的单位(米,千米,公里,英里), 返回的数量来查询坐标, 返回类型是 GeoResults<RedisGeoCommands.GeoLocation>
     *
     * @param key       redis保存的key
     * @param longitude 经度 x轴
     * @param latitude  纬度 y轴
     * @param distance  距离
     * @param metric    距离的单位(米,千米,公里,英里)
     * @param limit     返回数据量
     */
    public static GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByXY(RedisTemplate redisTemplate,
                                                                             String key,
                                                                             double longitude,
                                                                             double latitude,
                                                                             double distance,
                                                                             Metric metric,
                                                                             int limit) {
        Circle circle = new Circle(new Point(longitude, latitude), new Distance(distance, metric));

        //设置geo查询参数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates().includeDistance() //查询返回结果包括距离和坐标
                .sortAscending()   // args.sortDescending();
                .limit(limit);     //限制查询数量
        GeoResults<RedisGeoCommands.GeoLocation> results = redisTemplate.opsForGeo().radius(key, circle, args);
        return results;
    }

    /**
     * 通过指定的经度, 纬度, 距离(以米为单位), 返回的数量来查询坐标, 返回类型是 List<RedisGeoCommands.GeoLocation>
     *
     * @param key       redis保存的key
     * @param longitude 经度 x轴
     * @param latitude  纬度 y轴
     * @param distance  距离(以米为单位)
     * @param limit     返回数据量
     */
    public static List<RedisGeoCommands.GeoLocation> nearPointsByXY(RedisTemplate redisTemplate,
                                                                    String key,
                                                                    double longitude,
                                                                    double latitude,
                                                                    double distance,
                                                                    int limit) {
        return nearPointsByXY(redisTemplate, key, longitude, latitude, distance, RedisGeoCommands.DistanceUnit.METERS, limit);
    }

    /**
     * 通过指定的经度, 纬度, 距离, 距离的单位(米,千米,公里,英里), 返回的数量来查询坐标, 返回类型是 List<RedisGeoCommands.GeoLocation>
     *
     * @param key       redis保存的key
     * @param longitude 经度 x轴
     * @param latitude  纬度 y轴
     * @param distance  距离
     * @param metric    距离的单位(米,千米,公里,英里)
     * @param limit     返回数据量
     */
    public static List<RedisGeoCommands.GeoLocation> nearPointsByXY(RedisTemplate redisTemplate,
                                                                    String key,
                                                                    double longitude,
                                                                    double latitude,
                                                                    double distance,
                                                                    Metric metric,
                                                                    int limit) {
        List<GeoResult<RedisGeoCommands.GeoLocation>> results = nearPointsGeoByXY(redisTemplate, key, longitude, latitude, distance, metric, limit).getContent();
        if (ObjectUtils.isEmpty(results)) return new ArrayList<>();
        return results.stream().map(item -> item.getContent()).collect(Collectors.toList());
    }

    /**
     * 通过指定的member, 距离(以米为单位), 返回的数量来查询坐标, 返回类型是 List<RedisGeoCommands.GeoLocation>
     *
     * @param key      redis保存的key
     * @param member   member
     * @param distance 距离(以米为单位)
     * @param limit    返回数据量
     * @return
     */
    public static GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByMember(RedisTemplate redisTemplate,
                                                                                 String key,
                                                                                 Object member,
                                                                                 double distance,
                                                                                 int limit) {
        return nearPointsGeoByMember(redisTemplate, key, member, distance, RedisGeoCommands.DistanceUnit.METERS, limit);
    }

    /**
     * 通过指定的member, 距离, 距离的单位(米,千米,公里,英里), 返回的数量来查询坐标, 返回类型是 GeoResults<RedisGeoCommands.GeoLocation>
     *
     * @param key      redis保存的key
     * @param member   member
     * @param distance 距离
     * @param metric   距离的单位(米,千米,公里,英里)
     * @param limit    返回数据量
     * @return
     */
    public static GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByMember(RedisTemplate redisTemplate,
                                                                                 String key,
                                                                                 Object member,
                                                                                 double distance,
                                                                                 Metric metric,
                                                                                 int limit) {
        Distance radius = new Distance(distance, metric);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates().includeDistance() //查询返回结果包括距离和坐标
                .sortAscending()   // args.sortDescending();
                .limit(limit);     //限制查询数量
        GeoResults<RedisGeoCommands.GeoLocation> results = redisTemplate.opsForGeo()
                .radius(key, member, radius, args);
        return results;
    }

    /**
     * 通过指定的member, 距离(以米为单位), 返回的数量来查询坐标, 返回类型是 List<RedisGeoCommands.GeoLocation>
     *
     * @param key      redis保存的key
     * @param member   member
     * @param distance 距离(以米为单位)
     * @param limit    返回数据量
     * @return
     */
    public static List<RedisGeoCommands.GeoLocation> nearPointsByMember(RedisTemplate redisTemplate,
                                                                        String key,
                                                                        Object member,
                                                                        double distance,
                                                                        int limit) {
        return nearPointsByMember(redisTemplate, key, member, distance, RedisGeoCommands.DistanceUnit.METERS, limit);
    }

    /**
     * 通过指定的member, 距离, 距离的单位(米,千米,公里,英里), 返回的数量来查询坐标, 返回类型是 List<RedisGeoCommands.GeoLocation>
     *
     * @param key      redis保存的key
     * @param member   member
     * @param distance 距离
     * @param metric   距离的单位(米,千米,公里,英里)
     * @param limit    返回数据量
     * @return
     */
    public static List<RedisGeoCommands.GeoLocation> nearPointsByMember(RedisTemplate redisTemplate,
                                                                        String key,
                                                                        Object member,
                                                                        double distance,
                                                                        Metric metric,
                                                                        int limit) {
        List<GeoResult<RedisGeoCommands.GeoLocation>> results = nearPointsGeoByMember(redisTemplate, key, member, distance, metric, limit).getContent();
        if (ObjectUtils.isEmpty(results)) return new ArrayList<>();
        return results.stream().map(item -> item.getContent()).collect(Collectors.toList());
    }
}
