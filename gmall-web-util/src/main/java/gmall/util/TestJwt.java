package gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nikename","zhangsan");
        String ip = "localhost";
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String encode = JwtUtil.encode("2020gmall", map, ip + format);
        System.err.println(encode);
        //eyJhbGciOiJIUzI1NiJ9.eyJuaWtlbmFtZSI6InpoYW5nc2FuIiwibWVtYmVySWQiOiIxIn0.Uhn3u8pW_1y96PEFCF7wHQTOqmK3BuI8QMVntiUrolI
        // 公有                   私有

        /*
            base64UrlCodec测试效果
            实际上只有盐值是真正的加密， 私有的数据只是base64编码的
         */
        String tokenUserInfo = StringUtils.substringBetween(encode, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map2 = JSON.parseObject(tokenJson, Map.class);
        System.out.println("64="+map2); //64={nikename=zhangsan, memberId=1}
    }
}
