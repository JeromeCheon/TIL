package me.jeromecheon.spring4blogproject.config.error.exception;

import lombok.Getter;
import me.jeromecheon.spring4blogproject.config.error.ErrorCode;

@Getter
public class BusinessBaseException extends RuntimeException {
  private final ErrorCode errorCode;

  public BusinessBaseException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public BusinessBaseException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
