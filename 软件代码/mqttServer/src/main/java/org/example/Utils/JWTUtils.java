package org.example.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.HashMap;

public class JWTUtils {

    private static String SIGNATURE = "ts1331_c64c";

    /**
     * 生成token
     * @param userID    传入userID
     * @param username  传入username
     * @return token
     */
    public static String createToken(String userID, String username) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);

        return JWT.create()
                .withHeader(new HashMap<>())
                .withClaim("userID", userID)
                .withClaim("username", username)
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.HMAC256(SIGNATURE));
    }

    public static DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SIGNATURE)).build().verify(token);
    }
}
