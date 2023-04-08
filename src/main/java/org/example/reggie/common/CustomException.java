package org.example.reggie.common;

import org.apache.logging.log4j.message.Message;

public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
