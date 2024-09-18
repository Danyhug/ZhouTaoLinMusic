package cn.zhoutaolinmusic.constant;

public interface RedisConstant {

    String USER_MODEL = "jjj:user:model:";

    // 用于兴趣推送时去重   和下面的浏览记录存储数据结构不同  这里只需要存id
    String HISTORY_VIDEO = "jjj:history:video:";

    // 用于用户浏览记录  这里需要存video
    String USER_HISTORY_VIDEO = "jjj:user:history:video:";

    // 系统视频库,每个公开的都会存在这
    String SYSTEM_STOCK = "jjj:system:stock:";

    // 注册逻辑
    String EMAIL_CODE = "jjj:email:code:";

    // 热门排行榜
    String HOT_RANK = "jjj:hot:rank";

    // 热门视频
    String HOT_VIDEO = "jjj:hot:video:";

    Long HISTORY_TIME = 432000L;

    Long EMAIL_CODE_TIME = 300L;

    Long USER_SEARCH_HISTORY_TIME = 432000L;

    // 系统分类库，用于查询分类下的视频随机获取
    String SYSTEM_TYPE_STOCK = "jjj:system:type:stock:";

    // 发件箱
    String OUT_FOLLOW = "jjj:out:follow:feed:";

    // 收件箱
    String IN_FOLLOW = "jjj:in:follow:feed:";

    // 用户搜索记录
    String USER_SEARCH_HISTORY = "jjj:user:search:history:";

    // 用户关注人
    String USER_FOLLOW = "jjj:user:follow:";

    // 用户粉丝
    String USER_FANS = "jjj:user:fans:";

    // 发布视频限流
    String VIDEO_LIMIT = "jjj:video:limit";
}
