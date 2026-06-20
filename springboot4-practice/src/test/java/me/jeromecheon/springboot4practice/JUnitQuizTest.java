package me.jeromecheon.springboot4practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JUnitQuizTest {
  @Test
  public void juitTest() {
    String name1 = "홍길동";
    String name2 = "홍길동";
    String name3 = "홍길은";

    assertThat(name1).isNotNull();
    assertThat(name2).isNotNull();
    assertThat(name3).isNotNull();

    assertThat(name1).isEqualTo(name2);
    assertThat(name1).isNotEqualTo(name3);
  }

  @Test
  public void juitTest2() {
    int number1 = 15;
    int number2 = 0;
    int number3 = -5;

    assertThat(number1).isPositive();
    assertThat(number2).isZero();
    assertThat(number3).isNegative();
    assertThat(number1).isGreaterThan(number2);
    assertThat(number3).isLessThan(number2);
  }

}
