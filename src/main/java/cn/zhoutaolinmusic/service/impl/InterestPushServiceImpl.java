package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.entity.video.Video;
import cn.zhoutaolinmusic.entity.vo.Model;
import cn.zhoutaolinmusic.entity.vo.UserModel;
import cn.zhoutaolinmusic.service.InterestPushService;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisCacheUtil redisCacheUtil;

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
            if (modelMap.containsKey(model.getLabel())) {
                // 获取本来的分数
                String score = modelMap.get(model.getLabel()).toString();
                // 增加得分
                modelMap.put(
                        model.getLabel(), Double.parseDouble(score) + model.getScore()
                );
                // 获取更新后的得分
                Double newScore = (Double) modelMap.get(model.getLabel());
                if (newScore < 0) {
                    // 说明不感兴趣
                    modelMap.remove(model.getLabel());
                }

            } else {
                // 用户首次关心这个标签
                modelMap.put(model.getLabel(), model.getScore());
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
        return null;
    }

    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        return null;
    }

    @Override
    public void deleteSystemTypeStockIn(Video video) {

    }
}
