package me.jeromecheon.springboot4practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class MemberRepositoryTest {
  @Autowired
  MemberRepository repository;


  @AfterEach
  public void cleanUp() {
    this.repository.deleteAll();
  }

  @Sql("/insert-members.sql")
  @Test
  void getAllMembers() {
    List<Member> members = this.repository.findAll();

    assertThat(members.size()).isEqualTo(3);
  }

  @Sql("/insert-members.sql")
  @Test
  void getMemberById() {
    Member member = this.repository.findById(2L).get();
    assertThat(member.getName()).isEqualTo("B");
  }

  @Sql("/insert-members.sql")
  @Test
  void getMemberByName() {
    Member member = this.repository.findByName("C").get();
    assertThat(member.getId()).isEqualTo(3);
  }

  @Test
  void saveMember() {
    Member member = new Member("A");
    this.repository.save(member);
    assertThat(this.repository.findById(1L).get().getName()).isEqualTo("A");
  }

  @Test
  void saveMembers() {
    List<Member> members = List.of(new Member("B"), new Member("C"));
    this.repository.saveAll(members);
    assertThat(this.repository.findAll().size()).isEqualTo(2);
  }

  @Sql("/insert-members.sql")
  @Test
  void deleteMemberById() {
    this.repository.deleteById(2L);
    assertThat(this.repository.findAll().size()).isEqualTo(2);
    assertThat(this.repository.findById(2L).isEmpty()).isTrue();
  }

  @Sql("/insert-members.sql")
  @Test
  void update() {
    Member member = this.repository.findById(2L).get();
    member.changeName("BC");
    assertThat(this.repository.findById(2L).get().getName()).isEqualTo("BC");
  }
}