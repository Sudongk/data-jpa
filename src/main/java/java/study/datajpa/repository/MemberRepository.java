package java.study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.study.datajpa.dto.MemberDto;
import java.study.datajpa.entity.Member;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /*
     * 컨디션을 안 넣으면 전체조회가 된다.
     * find...By // ...에는 뭐가 들어가도 상관 없다.
     */
    List<Member> findHelloBy();

    List<Member> findTop3by();

    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("SELECT m FROM Member m WHERE m.username =:username AND m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("SELECT m.username FROM Member m")
    List<String> findUsernameList();

    // 마치 생성자를 통해 객체를 만드는 것 같은 문법으로 보인다
    @Query("SELECT new java.study.datajpa.dto.MemberDto(m.id, m.username, t.name) " +
            "FROM Member m JOIN m.team t")
    List<MemberDto> findMemberDto();

    @Query("SELECT m FROM Member m WHERE m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username);

    Member findOneByUsername(String username);

    Optional<Member> findOptionalByUsername(String username);

    @Query(value = "SELECT m FROM Member m",
            countQuery = "SELECT count(m) from Member m")
        // 카운트 쿼리는 실제 쿼리에 비해 훨씬 단순하기 때문에
        // 이런 경우를 위해 카운트 쿼리의 분리를 제공한다.
    Page<Member> findByAge(int age, Pageable pageable);

//    Slice<Member> findByAge(int age, Pageable pageable);

}