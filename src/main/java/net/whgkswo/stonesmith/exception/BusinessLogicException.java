package net.whgkswo.stonesmith.exception;

import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException {
    private int status;

    public BusinessLogicException(int status, String message) {
        super(message);
    }
}
