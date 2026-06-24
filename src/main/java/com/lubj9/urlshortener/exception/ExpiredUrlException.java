package com.lubj9.urlshortener.exception;

public class ExpiredUrlException extends RuntimeException {
    public ExpiredUrlException(String mensagem) { super(mensagem); }
}
