package org.fp.bt_qlsach.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberForm {

    @NotBlank(message = "Nhập mã độc giả")
    @Size(max = 30, message = "Mã độc giả tối đa 30 ký tự")
    private String memberCode;

    @NotBlank(message = "Nhập họ tên")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    public static MemberForm from(org.fp.bt_qlsach.entity.Member member) {
        MemberForm form = new MemberForm();
        form.setMemberCode(member.getMemberCode());
        form.setFullName(member.getFullName());
        form.setEmail(member.getEmail());
        form.setPhone(member.getPhone());
        return form;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
