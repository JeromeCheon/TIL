package me.jeromecheon.springboot4practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JUnitCycleQuiz {

    @BeforeEach
    public void beforeEach() {
        System.out.println("Hello!");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("Bye!");
    }

    @Test
    public void juintQuiz3() {
        System.out.println("This is first test");
    }

    @Test
    public void juintQuiz4() {
        System.out.println("This is second test");
    }
}
