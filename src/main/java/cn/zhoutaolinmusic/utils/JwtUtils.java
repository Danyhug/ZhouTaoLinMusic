package cn.zhoutaolinmusic.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtUtils {
    // token 过期时间，设置为一年
    public static final long EXPIRE = 365L * 1000 * 60 * 60 * 24;
    // 秘钥
    public static final String SECRET = "ZhouTaoLinMusic";

    /**
     * 生成token的方法
     * @param id
     * @param nickname
     * @return
     */
    public static String getJwtToken(Long id, String nickname) {
        String jwtToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")

                .setSubject("guli-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))

                // 设置 token 主体部分，存储用户信息
                .claim("id", id)
                .claim("nickname", nickname)

                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, SECRET)
                .compact();
        return jwtToken;
    }

    /**
     * 判断 token 是否有效
     * @param request
     * @return
     */
    public static boolean checkToken(HttpServletRequest request) {
        try {
            String jwtToken = request.getHeader("token");
            if (ObjectUtils.isEmpty(jwtToken)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    public static Long getUserId(HttpServletRequest request) {
        String jwtToken = request.getHeader("token");
        if (ObjectUtils.isEmpty(jwtToken)) {
            return null;
        }

        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(jwtToken);
        Claims claims = claimsJws.getBody();
        return Long.valueOf(claims.get("id").toString());
    }
}
