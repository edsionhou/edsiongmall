package gmall.util.interceptor;

import com.alibaba.fastjson.JSON;
import gmall.annotation.LoginRequired;
import gmall.util.CookieUtil;
import gmall.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

/**
 *  拦截器  继承  HandlerInterceptorAdapter
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("拦截器开始检查工作"+request.getRequestURI());
        //判断被拦截的请求 的访问方法 的注解（是否需要拦截）   handler 就是请求的方法！！！
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        if(methodAnnotation==null){
            //1.不需要被拦截
            return true;
        }
        System.out.println("拦截器开始拦截工作--> "+request.getRequestURI());
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if(StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if(StringUtils.isNotBlank(newToken)){
            token = newToken;
        }


        boolean b = methodAnnotation.LoginNecessary();  //获取 注解的 LoginNecessary 状态  是否必须登录成功
        //6.使用token去verify验证中心 验证， 既然有两处验证，不如先验证
        String success = "failed";
        Map<String,String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)){
            String remoteAddr = request.getHeader("x-forwarded-for");//可以从nginx代理中获取真正的 请求地址
            if(StringUtils.isBlank(remoteAddr)){
                remoteAddr = request.getRemoteAddr(); // 从request请求中获取远端请求IP ，如果是nginx负载均衡 那其实不是真正的IP
            }
            String successJson = HttpClientUtil.doGet("http://passport.gmall.com:8020/verify?token=" + token+"&currentIP="+remoteAddr); //成功返回success
             successMap = JSON.parseObject(successJson, Map.class);
             success = successMap.get("status");
        }

        if (b) { //2.系统要求 此用户必须登录成功才能使用
            if(!success.equals("success")){
                //4.验证失败，重定向到passport登录
                StringBuffer requestURL = request.getRequestURL();
                System.out.println("requestURL--> "+requestURL);
                response.sendRedirect("http://passport.gmall.com:8020/index?ReturnUrl="+requestURL);
                return false;
            }else{
                //5.验证通过，覆盖cookie中的token
                //需要将token携带的用户信息写入 request
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("username",successMap.get("username"));

                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request, response, "oldToken", token, 60*60 * 2, true);
                }
            }
        }else{   //3.系统 不要求必须登录   但要验证 ? 为什么
            //验证
            if(success.equals("success")){
                //6.需要将token携带的用户信息写入 request
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("username",successMap.get("username"));

                /*
                 拦截器是属于每个不同应用的，所以每次cookie的写入， 浏览器拿到的都是其 所要访问的项目返回的 cookie，不存在顶级域名的限制。
                 */
                //验证通过，覆盖cookie中的token
                if(StringUtils.isNotBlank(token)){
                    CookieUtil.setCookie(request, response, "oldToken", token, 60*60 * 2, true);
                }
            }

        }
//        request.getRequestDispatcher("/cartList").forward(request,response);


        return true;
    }

}
