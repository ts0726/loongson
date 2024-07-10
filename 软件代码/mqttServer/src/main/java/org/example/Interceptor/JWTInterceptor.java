package org.example.Interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Utils.JWTUtils;
import org.example.Utils.ResultData;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JWTInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws IOException {

        String JWT = httpServletRequest.getHeader("Authorization");
        System.out.println(JWT);
        ResultData<String> resultData = new ResultData<>();

        try {
            JWTUtils.verifyToken(JWT);
            System.out.println("token验证成功");
            return true;

        }catch (SignatureVerificationException e){
            System.out.println("无效签名");
            resultData.setCode(401);
            resultData.setMsg("无效签名");
            writer(resultData, httpServletResponse);
        }catch (TokenExpiredException e){
            System.out.println("token已经过期");
            resultData.setCode(401);
            resultData.setMsg("Token已过期");
            writer(resultData, httpServletResponse);
        }catch (AlgorithmMismatchException e){
            System.out.println("算法不一致");
            resultData.setCode(401);
            resultData.setMsg("Token算法不一致");
            writer(resultData, httpServletResponse);
        }catch (Exception e){
            System.out.println("token无效");
            resultData.setCode(401);
            resultData.setMsg("Token无效");
            writer(resultData, httpServletResponse);
        }

        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

    }

    private void writer(ResultData<String> resultData, HttpServletResponse httpServletResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String resultDataJson = objectMapper.writeValueAsString(resultData);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setStatus(401);
        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(resultDataJson);
        writer.flush();
        writer.close();
    }
}
