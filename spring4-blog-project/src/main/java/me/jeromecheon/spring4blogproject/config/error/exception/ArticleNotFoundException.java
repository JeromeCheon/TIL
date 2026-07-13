package me.jeromecheon.spring4blogproject.config.error.exception;

import me.jeromecheon.spring4blogproject.config.error.ErrorCode;

public class ArticleNotFoundException extends NotFoundException {
  public ArticleNotFoundException() {
    super(ErrorCode.ARTICLE_NOT_FOUND);
  }
}
