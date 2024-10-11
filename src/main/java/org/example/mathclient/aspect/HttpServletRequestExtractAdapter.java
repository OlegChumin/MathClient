package org.example.mathclient.aspect;

import io.opentracing.propagation.TextMap;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

public class HttpServletRequestExtractAdapter implements TextMap {

    private final HttpServletRequest request;

    public HttpServletRequestExtractAdapter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("iterator should not be used with TextMapExtractAdapter");
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("This is a read-only adapter");
    }

    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    public String getHeader(String name) {
        return request.getHeader(name);
    }
}
