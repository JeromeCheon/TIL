package me.jeromecheon.spring4blogproject.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ExampleController {
  @GetMapping("/thymeleaf/example")
  public String thymeleafExample(Model model) {
    Person examplePerson = new Person(
            1L,
            "홍길동",
            11,
            List.of("운동", "독서")
    );
    model.addAttribute("person", examplePerson);
    model.addAttribute("today", LocalDate.now());
    return "example";
  }

  @AllArgsConstructor
  @Getter
  @Setter
  class Person {
    private Long id;
    private String name;
    private int age;
    private List<String> hobbies;
  }
}
