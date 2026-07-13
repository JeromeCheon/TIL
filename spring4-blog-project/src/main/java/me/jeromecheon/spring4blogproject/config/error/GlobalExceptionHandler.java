package me.jeromecheon.spring4blogproject.config.error;

import lombok.extern.slf4j.Slf4j;
import me.jeromecheon.spring4blogproject.config.error.exception.BusinessBaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e) {
    log.error("HttpRequestMethodNotSupportedException", e);
    return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException", e);
    return createErrorResponseEntity(ErrorCode.INVALID_INPUT_VALUE);
  }


  @ExceptionHandler(BusinessBaseException.class)
  protected ResponseEntity<ErrorResponse> handle(BusinessBaseException e) {
    log.error("BusinessBaseException", e);
    return createErrorResponseEntity(e.getErrorCode());
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handle(Exception e) {
    e.printStackTrace();
    log.error("Exception", e);
    return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {
    return new ResponseEntity<>(
            ErrorResponse.of(errorCode),
            errorCode.getStatus()
    );
  }
}
