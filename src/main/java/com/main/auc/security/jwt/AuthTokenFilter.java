package com.main.auc.security.jwt;

import com.main.auc.config.MyHttpServletRequestWrapper;
import com.main.auc.security.services.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            MDC.put("requestId", RandomString.make(15));

            log.info("contentType: " + request.getContentType());
            if(!Strings.isEmpty(request.getContentType()) && request.getContentType().contains("multipart/form-data")){

                filterChain.doFilter(request, response);
                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
                String resStr = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

                log.info("http code: " + responseWrapper.getStatusCode());
                log.info("rs: " + resStr);
            }else{
                MyHttpServletRequestWrapper requestWrapper = new MyHttpServletRequestWrapper(request);
                ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
                log.info("HTTP method: {}", requestWrapper.getMethod());

                log.info("endpoint: " + requestWrapper.getRequestURI());
                String jsonData = IOUtils.toString(requestWrapper.getReader());
                log.info("Json Request: {}", jsonData);
//            requestWrapper.setAttribute("JSON_REQ", jsonData);
                requestWrapper.resetInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
                filterChain.doFilter(requestWrapper, responseWrapper);
                String resStr = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                log.info("http code: " + responseWrapper.getStatusCode());
                log.info("rs: " + resStr);
                responseWrapper.copyBodyToResponse();
            }

//            MyHttpServletRequestWrapper requestWrapper = new MyHttpServletRequestWrapper(request);
//            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
//            log.info("HTTP method: {}", requestWrapper.getMethod());
//
//            log.info("endpoint: " + requestWrapper.getRequestURI());
//            String jsonData = IOUtils.toString(requestWrapper.getReader());
//            log.info("Json Request: {}", jsonData);
////            requestWrapper.setAttribute("JSON_REQ", jsonData);
//            requestWrapper.resetInputStream(jsonData.getBytes(StandardCharsets.UTF_8));
//            filterChain.doFilter(requestWrapper, responseWrapper);
//            String resStr = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
//            log.info("http code: " + responseWrapper.getStatusCode());
//            log.info("rs: " + resStr);
//            responseWrapper.copyBodyToResponse();

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e);
        }

//        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }

        return null;
    }

}
