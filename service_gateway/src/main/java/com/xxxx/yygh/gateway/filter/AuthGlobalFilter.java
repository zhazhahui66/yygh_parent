package com.xxxx.yygh.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.common.helper.JwtHelper;
import com.xxxx.common.result.Result;
import com.xxxx.common.result.ResultCodeEnum;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //获取请求路径
        String path = request.getURI().getPath();
        //内部接口不允许访问
        if(antPathMatcher.match("/**/inner/**",path)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //api接口，异步请求校验用户必须登录
        if(antPathMatcher.match("/api/**/auth/**",path)){
            Long userId = this.getUserId(request);
            if(StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
                return out(response,ResultCodeEnum.LOGIN_AURH);
            }
        }


        return null;
    }

    /**
     * api 接口鉴权失败返回数据
     * @param response
     * @param loginAuth
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum loginAuth) {
        Result result = Result.build(null, loginAuth);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    private Long getUserId(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(null!=tokenList){
            token = tokenList.get(0);
        }
        if(!StringUtils.isEmpty(token)){
            return JwtHelper.getUserId(token);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
