package com.pay.domain;

/**
 * 순수 도메인 객체
 * - 외부 인프라적인 코드, User 관계없는 로직 / 순수 User와 관련된 비즈니스 로직만 처리 => 순수 도메인 객체
 */
public class User {

    private String name;
    private String password;
    private String phone;
    private String email;

    public void validatePassword(String password) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
