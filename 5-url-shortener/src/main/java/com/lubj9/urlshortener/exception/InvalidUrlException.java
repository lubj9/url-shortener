package com.lubj9.urlshortener.exception;

public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String mensagem) { super(mensagem); }
}
