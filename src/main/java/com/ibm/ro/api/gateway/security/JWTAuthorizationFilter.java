package com.ibm.ro.api.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.HttpHeaders;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JWTAuthorizationFilter extends AbstractGatewayFilterFactory<JWTAuthorizationFilter.Config> {

	@Autowired
	Environment environment;

	String tokenSecret = null;
	String tokenExpirationTime = null;

	public JWTAuthorizationFilter(Environment environment) {
		super(Config.class);
		this.environment = environment;
		tokenSecret = environment.getProperty("token.secret");
		tokenExpirationTime = environment.getProperty("token.expiration_time");
	}

	public static class Config {
		// Put config props
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest serverHttpRequest = exchange.getRequest();
			if (!serverHttpRequest.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
			}

			String authorization = serverHttpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			String jwt = authorization.replaceAll("Bearer", "");
			boolean isJwtValid = false;
			try {
				isJwtValid = isJWTValid(jwt);
			} catch (ExpiredJwtException e) {
				e.printStackTrace();
				String requestURL = serverHttpRequest.getURI().toString();
				// allow for Refresh Token creation if following conditions are true.
				if (requestURL.contains("refreshtoken")) {
					// allowForRefreshToken(e, serverHttpRequest);
					isJwtValid = true;
				}
			}
			if (!isJwtValid) {
				return onError(exchange, "API-Gateway:: Invalid JWT Token", HttpStatus.UNAUTHORIZED);
			}
			return chain.filter(exchange);
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String string, HttpStatus httpStatus) {
		ServerHttpResponse res = exchange.getResponse();
		res.setStatusCode(httpStatus);
		return res.setComplete();
	}

	private boolean isJWTValid(String jwtToken) {
		boolean retVal = false;

		String userId = null;

		try {
			userId = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(jwtToken).getBody().getSubject();
		} catch (ExpiredJwtException e) {
			e.printStackTrace();
			throw e;
		}

		if (userId != null)
			retVal = true;
		return retVal;
	}

}
