package com.sme.erp.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final String WRAP_HEADER = "X-Wrap-Response";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (!shouldWrap(request, response) || body == null || body instanceof ApiResponse) {
            return body;
        }

        return ApiResponse.success(resolveMessage(request), body);
    }

    private boolean shouldWrap(ServerHttpRequest request, ServerHttpResponse response) {
        String wrapHeader = request.getHeaders().getFirst(WRAP_HEADER);
        if (!"true".equalsIgnoreCase(wrapHeader)) {
            return false;
        }

        if (!(response instanceof ServletServerHttpResponse servletResponse)) {
            return false;
        }

        int status = servletResponse.getServletResponse().getStatus();
        return status >= 200 && status < 300 && status != 204;
    }

    private String resolveMessage(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        if (path.contains("/products")) {
            return "Product request successful";
        }
        if (path.contains("/brands")) {
            return "Brand request successful";
        }
        if (path.contains("/categories")) {
            return "Category request successful";
        }
        if (path.contains("/uoms")) {
            return "UOM request successful";
        }
        if (path.contains("/warehouses")) {
            return "Warehouse request successful";
        }
        if (path.contains("/stocks")) {
            return "Stock request successful";
        }
        if (path.contains("/adjustments")) {
            return "Stock adjustment successful";
        }
        if (path.contains("/movements")) {
            return "Stock movement request successful";
        }
        return "Request successful";
    }
}
