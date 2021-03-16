package com.ibm.ro.api.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class ErrorFilterFactory extends AbstractGatewayFilterFactory<Object> {

	private final ErrorWebExceptionHandler errorWebExceptionHandler;

	public ErrorFilterFactory(ErrorWebExceptionHandler errorWebExceptionHandler) {
		super();
		this.errorWebExceptionHandler = errorWebExceptionHandler;
	}

	@Override
	public GatewayFilter apply(Object config) {
		return new ErrorPageForwardFilter(errorWebExceptionHandler);
	}

}
