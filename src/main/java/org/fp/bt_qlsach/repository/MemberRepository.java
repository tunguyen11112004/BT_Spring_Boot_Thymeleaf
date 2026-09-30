package org.fp.bt_qlsach.repository;

import org.fp.bt_qlsach.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberCodeIgnoreCase(String memberCode);

    List<Member> findAllByOrderByFullNameAsc();
}
