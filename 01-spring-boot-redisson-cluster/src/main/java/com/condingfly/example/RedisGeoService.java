package com.condingfly.example;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个是redis通用的查找附近的人的功能
 * 实现附近人的功能
 *
 * @author hgp
 * @date 20-7-16
 */
@Service
public class RedisGeoService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @PostConstruct
    public void test() {
        String key = "test-location100";
        List<Point> points = Arrays.asList(new Point(116.9411419137, 39.2211812254), new Point(116.9421419137, 39.2221812254), new Point(116.9431419137, 39.2231812254),
                new Point(116.9441419137, 39.2241812254), new Point(116.9451419137, 39.2251812254), new Point(116.9461419137, 39.2261812254));
        // 新增
        for (int i = 0; i < points.size(); i++) {
            addGeo(key, points.get(i), "userId-" + i);
        }

        // 查询多个member的坐标
        points = queryPointsOfMembers(key, "userId-0", "userId-1", "userId-2");
        System.out.println("查询几个member userId-0 userId-1 userId-2 的坐标 ===>>> " + points);

        // 查询多个member的距离
        Double distance = distance(key, "userId-0", "userId-1");
        System.out.println("userId-0 和 userId-1 以米为单位的距离 ===>>> " + distance);

        distance = distance(key, "userId-0", "userId-1", RedisGeoCommands.DistanceUnit.KILOMETERS);
        System.out.println("userId-0 和 userId-1 以米为单位的距离 ===>>> " + distance);

        // 查询某个经纬度坐标 10000 米以内的3个member
        List<RedisGeoCommands.GeoLocation> locations = nearPointsByXY(key, 116.9412419137, 39.2212812254, 10000, 3);
        System.out.println(locations);

        // 查询某个经纬度坐标 10000 米以内的3个points的Geo信息
        GeoResults<RedisGeoCommands.GeoLocation> geoResults = nearPointsGeoByXY(key, 116.9412419137, 39.2212812254, 10000, 3);
        System.out.println(geoResults);


        // 查询某个Member 10000 米以内的3个member
        String member1 = "userId-2";
        locations = nearPointsByMember(key, member1, 10000, 3);
        System.out.println(locations);

        // 查询某个Member 10000 米以内的3个points的Geo信息
        geoResults = nearPointsGeoByMember(key, member1, 10, 3);
        System.out.println(geoResults);

        // 删除一个或者多个member
        delGeo(key, "userId-0");
        System.out.println("删除 userId-0 的数据");
    }


    /**
     * 添加地理坐标
     *
     * @param key
     * @param point
     * @param member
     */
    public void addGeo(String key, Point point, Object member) {
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
    public void addGeo(String key, double longitude, double latitude, Object member) {
        redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), member);
    }

    /**
     * 删除地理坐标
     *
     * @param key
     * @param member
     */
    public void delGeo(String key, Object member) {
        redisTemplate.opsForGeo().remove(key, member);
    }

    /**
     * 获取多个member的坐标
     *
     * @param key
     * @param members
     * @return
     */
    public List<Point> queryPointsOfMembers(String key, Object... members) {
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
    public Double distance(String key, Object member1, Object member2) {
        return distance(key, member1, member2, RedisGeoCommands.DistanceUnit.METERS);
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
    public Double distance(String key, Object member1, Object member2, Metric metric) {
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
    public GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByXY(String key,
                                                                      double longitude,
                                                                      double latitude,
                                                                      double distance,
                                                                      int limit) {
        Circle circle = new Circle(new Point(longitude, latitude), new Distance(distance, RedisGeoCommands.DistanceUnit.METERS));

        //设置geo查询参数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates().includeDistance() //查询返回结果包括距离和坐标
                .sortAscending()   // args.sortDescending();距离远近排序
                .limit(limit);     // 限制查询数量
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
    public List<RedisGeoCommands.GeoLocation> nearPointsByXY(String key,
                                                             double longitude,
                                                             double latitude,
                                                             double distance,
                                                             int limit) {
        List<GeoResult<RedisGeoCommands.GeoLocation>> results = nearPointsGeoByXY(key, longitude, latitude, distance, limit).getContent();
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
    public GeoResults<RedisGeoCommands.GeoLocation> nearPointsGeoByMember(String key,
                                                                          Object member,
                                                                          double distance,
                                                                          int limit) {
        Distance radius = new Distance(distance, RedisGeoCommands.DistanceUnit.METERS);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates().includeDistance() //查询返回结果包括距离和坐标
                .sortAscending()   // args.sortDescending();距离远近排序
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
    public List<RedisGeoCommands.GeoLocation> nearPointsByMember(String key,
                                                                 Object member,
                                                                 double distance,
                                                                 int limit) {
        List<GeoResult<RedisGeoCommands.GeoLocation>> results = nearPointsGeoByMember(key, member, distance, limit).getContent();
        if (ObjectUtils.isEmpty(results)) return new ArrayList<>();
        return results.stream().map(item -> item.getContent()).collect(Collectors.toList());
    }
}
