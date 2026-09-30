package org.fp.bt_qlsach.service;

import org.fp.bt_qlsach.entity.Member;
import org.fp.bt_qlsach.util.BusinessException;
import org.fp.bt_qlsach.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public Member get(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy độc giả."));
    }

    @Transactional
    public Member create(String memberCode, String fullName, String email, String phone) {
        String code = memberCode.trim();
        if (memberRepository.findByMemberCodeIgnoreCase(code).isPresent()) {
            throw new BusinessException("Mã độc giả đã tồn tại.");
        }
        Member member = new Member();
        member.setMemberCode(code);
        member.setFullName(fullName.trim());
        member.setEmail(blankToNull(email));
        member.setPhone(blankToNull(phone));
        return memberRepository.save(member);
    }

    @Transactional
    public Member update(Long id, String memberCode, String fullName, String email, String phone) {
        Member member = get(id);
        String code = memberCode.trim();
        memberRepository.findByMemberCodeIgnoreCase(code)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessException("Mã độc giả đã tồn tại.");
                });
        member.setMemberCode(code);
        member.setFullName(fullName.trim());
        member.setEmail(blankToNull(email));
        member.setPhone(blankToNull(phone));
        return memberRepository.save(member);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
