package me.jeromecheon.spring4blogproject.config.error.exception;

import me.jeromecheon.spring4blogproject.config.error.ErrorCode;

public class NotFoundException extends BusinessBaseException {
  public NotFoundException(ErrorCode errorCode) {
    super(errorCode.getMessage(), errorCode);
  }

  public NotFoundException() {
    super(ErrorCode.NOT_FOUND);
  }
}
