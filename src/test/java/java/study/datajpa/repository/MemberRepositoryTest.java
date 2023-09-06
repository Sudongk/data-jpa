package java.study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.study.datajpa.dto.MemberDto;
import java.study.datajpa.entity.Member;
import java.study.datajpa.entity.Team;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void testMember() {
        // given
        Member member = new Member("memberA");

        // when
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId())
                .orElseThrow(RuntimeException::new);

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    @DisplayName("기본적인 CRUD를 테스트한다.")
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> members = memberRepository.findAll();
        assertThat(members).hasSize(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 이름과 나이를 조건으로 검색한다.")
    void findByUsernameAndAgeGreaterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        Member actual = result.get(0);

        assertThat(actual.getUsername()).isEqualTo("AAA");
        assertThat(actual.getAge()).isEqualTo(20);
        assertThat(result).hasSize(1);
    }

    @Test
    void helloBy() {
        List<Member> helloBy = memberRepository.findHelloBy();
    }

    @Test
    @DisplayName("@Query 어노테이션을 이용한 메소드를 테스팅해본다.")
    void testQueryAnnotation() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    @DisplayName("@Query 어노테이션을 이용한 메소드로 특정 칼럼을 가져온다.")
    void testFindUsernames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernames = memberRepository.findUsernameList();
        for (String username : usernames) {
            System.out.println("username = " + username);
        }
        assertThat(usernames).hasSize(2);
    }

    @Test
    @DisplayName("@Query 어노테이션을 이용한 메소드로 DTO 객체를 생성한다.")
    void testMemberDTO() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        m1.setTeam(teamA);
        m2.setTeam(teamA);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    @DisplayName("@Param 어노테이션을 이용해 컬렉션을 파라미터로 바인딩한다.")
    void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(List.of("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
        assertThat(result).hasSize(2);
    }

    @Test
    void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        Member findOne = memberRepository.findOneByUsername("AAA");
        Optional<Member> findOptional = memberRepository.findOptionalByUsername("AAA");
        List<Member> listByUsername = memberRepository.findListByUsername("AAA");

        assertThat(findOne).isEqualTo(m1);
        assertThat(findOptional.orElseThrow()).isEqualTo(m1);
        assertThat(listByUsername.get(0)).isEqualTo(m1);

    }

    @Test
    void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
//        카운트 쿼리가 같이 나간다.
//        long totalCount = memberRepository.totalCount(age);

        Page<MemberDto> memberDtoPage = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then
        List < Member > contents = page.getContent();
        for (Member member : contents) {
            System.out.println("member = " + member);
        }

        long totalElements = page.getTotalElements();
        System.out.println("totalElements = " + totalElements);

        assertThat(contents).hasSize(3);    // 현재 가져온 로우의 개수
        assertThat(page.getTotalElements()).isEqualTo(5);   // 총 로우의 개수
        assertThat(page.getNumber()).isEqualTo(0);  // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);  // 총 페이지 개수
        assertThat(page.isFirst()).isTrue();    // 첫 페이지인지
        assertThat(page.hasNext()).isTrue();    // 다음 페이지가 있는지
    }

//    @Test
//    void slicing() {
//        // given
//        memberRepository.save(new Member("member1", 10));
//        memberRepository.save(new Member("member2", 10));
//        memberRepository.save(new Member("member3", 10));
//        memberRepository.save(new Member("member4", 10));
//        memberRepository.save(new Member("member5", 10));
//
//        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
//
//        // when
//        Slice<Member> page = memberRepository.findByAge(10, pageRequest);
////        카운트 쿼리가 같이 나간다.
////        long totalCount = memberRepository.totalCount(age);
//
//        // then
//        List<Member> contents = page.getContent();
//        for (Member member : contents) {
//            System.out.println("member = " + member);
//        }
//
//        assertThat(contents).hasSize(3);
////        assertThat(page.getTotalElements()).isEqualTo(5); // 없는 기능이다.
//        assertThat(page.getNumber()).isEqualTo(0);
////        assertThat(page.getTotalPages()).isEqualTo(2);    // 없는 기능이다.
//        assertThat(page.isFirst()).isTrue();
//        assertThat(page.hasNext()).isTrue();
//    }

}