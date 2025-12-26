//UnauthorizedException.java
package com.chatbot.modules.tenant.billing.exception;



public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {

        super(message);

    }

}