package com.cloudweb.oa.utils;

import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.service.IUserService;
import com.redmoon.oa.sys.DebugUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author :
 * @date :
 * description :
 */
@Component("jwtUtil")
@Slf4j
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserCache userCache;

    public static final String USER_ID = "userId";

    /**
     * 从request中获取token
     *
     * @param request
     * @return {@link String}
     * @author
     * @date 2020/11/26 17:15
     */
    public String getToken(HttpServletRequest request) {
        String token = "";
        String bearerToken = request.getHeader(jwtProperties.getHeader());
        if (!StringUtils.hasText(bearerToken)) {
            bearerToken = request.getParameter(jwtProperties.getHeader());
        }

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getStartWith())) {
            token = bearerToken.substring(jwtProperties.getStartWith().length());
        }
        return token;
    }

    /**
     * 根据token获取AuthenticationToken
     *
     * @param token
     * @return {@link UsernamePasswordAuthenticationToken}
     * @author
     * @date 2020/11/26 18:16
     */
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = validate(token);
        if (claims == null) {
            return null;
        }

        /*HashMap map = (HashMap) claims.get(USER_ID);
        Collection<? extends GrantedAuthority> authorities =
                ((List<Map<String, String>>) map.get("authorities")).stream()
                        .map(a -> new SimpleGrantedAuthority(a.get("authority")))
                        .collect(Collectors.toList());
        User principal = new User((String) map.get("username"), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);*/

        // String encryUserId = claims.get(USER_ID).toString();
        // DebugUtil.i(getClass(), "getAuthentication",  Base64Util.decode(encryUserId));

        // spring security 手工认证
        UserDetailsService userDetailsService = SpringUtil.getBean(UserDetailsService.class);
        //根据用户名username加载userDetails
        UserDetails userDetails = null;
        try {
            userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        }
        catch (UsernameNotFoundException e) {
            DebugUtil.e(getClass(), "getAuthentication", claims.getSubject() + ":" + e.getMessage());
            return null;
        }
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), userDetails.getPassword(), userDetails.getAuthorities());
    }

    /**
     * 生成jwt
     *
     * @param userName
     * @author
     * @date 2020/11/26 17:15
     */
    public String generate(String userName) {
        com.cloudweb.oa.entity.User myUser = userCache.getUser(userName);
        // userId是重要信息，进行加密
        String encryUserId = Base64Util.encode(String.valueOf(myUser.getId()));

        String token = Jwts.builder()
                // 可以将不重要的对象信息放到claims, 要先设置私有的声明，写在标准的声明赋值之后会覆盖标准声明
                .claim(USER_ID, encryUserId)
                // 设置jti(JWT ID)：是JWT的唯一标识，可设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                // 可以在未登陆前作为身份标识使用
                .setId(UUID.randomUUID().toString().replace("-", ""))
                .setIssuedAt(new Date())
                .setSubject(userName)           // 代表这个JWT的主体，即它的所有人
                // .setIssuer(JwtConst.clientId)              // 代表这个JWT的签发主体
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getValidateSecond() * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey()).compact();
        return jwtProperties.getStartWith() + token;
    }


    /**
     * 复制jwt，并重新设置签发时间(为当前时间)和失效时间
     *
     * @param jwt
     *            被复制的jwt令牌
     * @return
     */
    public String copyJwt(String jwt) {
        Claims claims = validate(jwt);

        // 生成JWT的时间，即签发时间
        long nowMillis = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                .setId(UUID.randomUUID().toString().replace("-", ""))
                // .setIssuer("zking")
                // iat: jwt的签发时间
                .setIssuedAt(new Date(nowMillis))
                // 代表这个JWT的主体，即它的所有人
                .setSubject((String)claims.getSubject())           // 代表这个JWT的主体，即它的所有人
                // 设置签名使用的签名算法和签名使用的秘钥
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                // 设置JWT的过期时间
                .setExpiration(new Date(nowMillis + jwtProperties.getValidateSecond() * 1000));
        return jwtProperties.getStartWith() + builder.compact();
    }

    /**
     * 校验token
     *
     * @param token
     * @return {@link java.lang.Boolean}
     * @author
     * @date 2020/11/26 17:16
     */
    public Claims validate(String token) {
        try {
            return Jwts.parser().setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException eje) {
            log.error("===== Token过期 =====", eje);
            return null;
        } catch (Exception e){
            log.error("===== token解析异常 =====", e);
            return null;
        }
    }

}
