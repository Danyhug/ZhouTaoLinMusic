package cn.zhoutaolinmusic.authority;

import lombok.Getter;
import org.springframework.util.ObjectUtils;

import java.util.*;

public class AuthorityUtils {
    /**权限集合*/
    private static final Map<Long, Collection<String>> map = new HashMap<>();

    /**过滤权限集合*/
    private static final Set<String> filterPermission = new HashSet<>();

    /**全局权限校验类*/
    private static Class c;

    /**
     * 是否开启 @PostMapping 权限校验，默认不开启
     */
    @Getter
    private static Boolean postAuthority = false;

    /**
     * 获取全局校验类
     */
    public static Class getGlobalVerify() {
        return c;
    }

    /**
     * 开启全局校验
     */
    public static void setGlobalVerify(Boolean state, Object o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (!(o instanceof AuthorityVerify)) {
            throw new ClassCastException(o.getClass() + "类型不是 Authority 实现类");
        }
        c = o.getClass();
    }

    /**
     * 设置权限
     * @param uid
     * @param authority
     */
    public static void setAuthority(Long uid, Collection<String> authority) {
        map.put(uid, authority);
    }

    /**
     * 校验权限
     * @param uid
     * @param authority
     * @return
     */
    public static Boolean verify(Long uid, String authority) {
        if (uid == null) return false;
        System.out.println("用户权限map的值" + map);
        return map.get(uid).contains(authority);
    }

    /**
     * 排除权限
     * @param permissions
     */
    public static void exclude(String... permissions) {
        filterPermission.addAll(Arrays.asList(permissions));
    }

    /**
     * 判断权限是否为空
     * @param uid
     * @return
     */
    public static Boolean isEmpty(Long uid) {
        return ObjectUtils.isEmpty(map.get(uid));
    }
}
