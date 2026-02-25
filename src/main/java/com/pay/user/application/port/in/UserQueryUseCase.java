package com.pay.user.application.port.in;

public interface UserQueryUseCase {

    UserProfileResult getMyProfile(String email);
}
