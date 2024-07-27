package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.entity.user.Favorites;
import cn.zhoutaolinmusic.mapper.user.FavoritesMapper;
import cn.zhoutaolinmusic.service.user.FavoritesService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {
    @Override
    public void remove(Long id, Long userId) {

    }

    @Override
    public List<Favorites> listByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();

        LambdaQueryWrapper<Favorites> wrapper = new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    public List<Long> listVideoIds(Long favoritesId, Long userId) {
        return null;
    }

    @Override
    public boolean favorites(Long fId, Long vId) {
        return false;
    }

    @Override
    public Boolean favoritesState(Long videoId, Long userId) {
        return null;
    }

    @Override
    public void exist(Long userId, Long defaultFavoritesId) {

    }
}
