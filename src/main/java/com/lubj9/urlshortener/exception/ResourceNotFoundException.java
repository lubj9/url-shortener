package com.lubj9.urlshortener.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mensagem) { super(mensagem); }
}
