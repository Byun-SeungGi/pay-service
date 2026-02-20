package com.pay.user.application.port.in;

import com.pay.user.adapter.in.web.v1.dto.UserProfileResponseDto;


public interface UserQueryUseCase {

    UserProfileResponseDto getMyProfile(String email);
}
