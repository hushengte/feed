package com.disciples.feed;

public class Response<T> {
    
    private static final int CODE_SUCCESS = 0;
    private static final int CODE_ERROR = 1;
    
    private final int code;
    private final T result;
    private final int total;
    
    private Response(int code, T result, int total) {
        this.code = code;
        this.result = result;
        this.total = total;
    }
    
    public static <T> Response<T> success(T result, long total) {
        return new Response<T>(CODE_SUCCESS, result, (int)total);
    }
    
    public static <T> Response<T> success(T result, int total) {
        return new Response<T>(CODE_SUCCESS, result, total);
    }
    
    public static <T> Response<T> success(T result) {
        return new Response<T>(CODE_SUCCESS, result, 1);
    }
    
    public static Response<String> error(String message) {
        return new Response<String>(CODE_ERROR, message, 0);
    }
    
    public boolean isError() {
        return code == CODE_ERROR;
    }

    public int getCode() {
        return code;
    }

    public T getResult() {
        return result;
    }

    public int getTotal() {
        return total;
    }
    
}
