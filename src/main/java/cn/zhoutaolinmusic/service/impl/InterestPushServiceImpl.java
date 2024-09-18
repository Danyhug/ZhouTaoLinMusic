package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.vo.Model;
import cn.zhoutaolinmusic.entity.vo.UserModel;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.service.video.VideoService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private TypeService typeService;

    @Override
    @Async
    public void pushSystemStockIn(Video video) {
        // 获取标签列表
        List<String> labels = video.buildLabel();
        Long videoId = video.getId();

        // 将多个命令一起发送给redis作为缓存
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label: labels) {
                byte[] key = (RedisConstant.SYSTEM_STOCK + label).getBytes();
                // 将标签保存到系统中
                connection.sAdd(key, videoId.toString().getBytes());
            }
            return null;
        });
    }

    @Override
    public void pushSystemTypeStockIn(Video video) {
        Long typeId = video.getTypeId();
        redisCacheUtil.sSet(RedisConstant.SYSTEM_TYPE_STOCK + typeId, video.getId());
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        // 获取标签列表随机获取 10 个
        List<Object> list = redisTemplate.opsForSet()
                .randomMembers(RedisConstant.SYSTEM_TYPE_STOCK + typeId, 12);

        HashSet<Long> result = new HashSet<>();
        if (list != null) {
            for (Object obj: list) {
                if (obj != null) {
                    result.add(Long.parseLong(obj.toString()));
                }
            }
            return result;
        }

        return Collections.emptyList();
    }

    @Override
    public void deleteSystemStockIn(Video video) {
        Long videoId = video.getId();

        // 因为一个视频可能存在多个标签，所以使用 批量命令
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label: video.buildLabel()) {
                byte[] key = (RedisConstant.SYSTEM_STOCK + label).getBytes();
                // 将标签从系统中删除
                connection.sRem(key, videoId.toString().getBytes());
            }
            return null;
        });
    }

    @Override
    public void initUserModel(Long userId, List<String> labels) {

    }

    @Override
    public void updateUserModel(UserModel userModel) {
        Long userId = userModel.getUserId();

        // 不管游客的情况
        if (userId == null) return;

        List<Model> models = userModel.getModels();
        // 获取用户模型
        String key = RedisConstant.USER_MODEL + userId;
        // 获取用户模型
        Map<Object, Object> modelMap = redisCacheUtil.getHashMap(key);

        for (Model model: models) {
            // 更新用户模型
            if (modelMap.containsKey(model.getLabels())) {
                // 获取本来的分数
                String score = modelMap.get(model.getLabels()).toString();
                // 增加得分
                modelMap.put(
                        model.getLabels(), Double.parseDouble(score) + model.getScore()
                );
                // 获取更新后的得分
                Double newScore = (Double) modelMap.get(model.getLabels());
                if (newScore < 0) {
                    // 说明不感兴趣
                    modelMap.remove(model.getLabels());
                }

            } else {
                // 用户首次关心这个标签
                modelMap.put(model.getLabels(), model.getScore());
            }
        }

        // 获取所有标签总数
        int labelSize = modelMap.keySet().size();
        for (Object o: modelMap.keySet()) {
            modelMap.put(o,(Double.parseDouble(modelMap.get(o).toString()) + labelSize )/ labelSize);
        }

        // 更新用户模型
        redisCacheUtil.setHashMap(key, modelMap);
    }


    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        // 创建结果集
        Set<Long> videoIds = new HashSet<>(10);

        if (user != null) {
            final Long userId = user.getId();
            // 从模型中拿概率
            final Map<Object, Object> modelMap = redisCacheUtil.getHashMap(RedisConstant.USER_MODEL + userId);
            if (!ObjectUtils.isEmpty(modelMap)) {
                // 组成数组
                final String[] probabilityArray = initProbabilityArray(modelMap);
                final Boolean sex = user.getSex();
                // 获取视频
                final Random randomObject = new Random();
                final ArrayList<String> labelNames = new ArrayList<>();
                // 随机获取X个视频
                for (int i = 0; i < 8; i++) {
                    String labelName = probabilityArray[randomObject.nextInt(probabilityArray.length)];
                    labelNames.add(labelName);
                }
                // 提升性能
                String t = RedisConstant.SYSTEM_STOCK;
                // 随机获取
                List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (String labelName : labelNames) {
                        String key = t + labelName;
                        connection.sRandMember(key.getBytes());
                    }
                    return null;
                });
                // 获取到的videoIds
                Set<Long> ids = list.stream().filter(id->id!=null).map(id->Long.parseLong(id.toString())).collect(Collectors.toSet());
                String key2 = RedisConstant.HISTORY_VIDEO;

                // 去重
                List simpIds = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (Long id : ids) {
                        connection.get((key2 + id + ":" + userId).getBytes());
                    }
                    return null;
                });
                simpIds = (List) simpIds.stream().filter(o->!ObjectUtils.isEmpty(o)).collect(Collectors.toList());;
                if (!ObjectUtils.isEmpty(simpIds)){
                    for (Object simpId : simpIds) {
                        final Long l = Long.valueOf(simpId.toString());
                        if (ids.contains(l)){
                            ids.remove(l);
                        }
                    }
                }

                videoIds.addAll(ids);

                return videoIds;
            }
        }
        // 游客
        // 随机获取10个标签
        final List<String> labels = typeService.random10Labels();
        final ArrayList<String> labelNames = new ArrayList<>();
        int size = labels.size();
        final Random random = new Random();
        // 获取随机的标签
        for (int i = 0; i < 10; i++) {
            final int randomIndex = random.nextInt(size);
            labelNames.add(RedisConstant.SYSTEM_STOCK + labels.get(randomIndex));
        }
        // 获取videoId
        final List<Object> list = redisCacheUtil.sRandom(labelNames);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }

        return videoIds;
    }
    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        final ArrayList<String> labelKeys = new ArrayList<>();
        for (String labelName : labelNames) {
            labelKeys.add(RedisConstant.SYSTEM_STOCK + labelName);
        }
        Set<Long> videoIds = new HashSet<>();
        final List<Object> list = redisCacheUtil.sRandom(labelKeys);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }
        return videoIds;
    }

    @Override
    public void deleteSystemTypeStockIn(Video video) {

    }


    // 初始化概率数组 -> 保存的元素是标签
    public String[] initProbabilityArray(Map<Object, Object> modelMap) {
        // key: 标签  value：概率
        Map<String, Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        final AtomicInteger n = new AtomicInteger(0);
        modelMap.forEach((k, v) -> {
            // 防止结果为0,每个同等加上标签数
            int probability = (((Double) v).intValue() + size) / size;
            n.getAndAdd(probability);
            probabilityMap.put(k.toString(), probability);
        });
        final String[] probabilityArray = new String[n.get()];

        final AtomicInteger index = new AtomicInteger(0);
        // 初始化数组
        probabilityMap.forEach((labelsId, p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit) {
                probabilityArray[i++] = labelsId;
            }
            index.set(limit);
        });
        return probabilityArray;
    }
}
