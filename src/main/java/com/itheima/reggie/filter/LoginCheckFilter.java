package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查是否登陆的过滤器
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    BaseContext baseContext;

    @Autowired
    public LoginCheckFilter(BaseContext baseContext) {
        this.baseContext = baseContext;
    }

    public static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)  servletResponse;
        log.info("拦截到请求：{}, method = {}", request.getRequestURI(), request.getMethod());

        String requestURI = request.getRequestURI();
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/loginout"
        };

        if(check(urls, requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果用户已经登录
        Long empId = (Long) request.getSession().getAttribute("employee");
        if(empId != null) {
            baseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }

        response.sendRedirect("/backend/page/login/login.html");
//        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登录");
    }

    public boolean check(String[] urls, String requestURI) {
        for(String url : urls) {
            if(ANT_PATH_MATCHER.match(url, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
