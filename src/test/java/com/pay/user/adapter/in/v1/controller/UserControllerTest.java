package com.pay.user.adapter.in.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.global.constant.ApiUrl;
import com.pay.user.adapter.in.web.v1.controller.UserController;
import com.pay.user.adapter.in.web.v1.dto.UserProfileResponseDto;
import com.pay.user.adapter.in.web.v1.dto.UserSignUpRequestDto;
import com.pay.user.application.port.in.SignUpUseCase;
import com.pay.user.application.port.in.UserProfileResult;
import com.pay.user.application.port.in.UserQueryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignUpUseCase signUpUseCase;

    @MockBean
    private UserQueryUseCase userQueryUseCase;

    @Test
    @DisplayName("회원가입 요청 성공")
    @WithMockUser
    void signUp_success() throws Exception {

        UserSignUpRequestDto request = new UserSignUpRequestDto("test22@email.com", "password123!@#", "010-1237-5678");

        given(signUpUseCase.signUp(any())).willReturn(1L);

        mockMvc.perform(post(ApiUrl.SIGN_UP)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data").value(1L))
                .andExpect(jsonPath("message").value("회원가입에 성공했습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 필수값 누락 (Validation)")
    @WithMockUser
    void signUp_fail_validation() throws Exception {

        UserSignUpRequestDto emptyRequest = new UserSignUpRequestDto("", "", "");

        mockMvc.perform(post(ApiUrl.SIGN_UP)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser(username = "gowawa@naver.com")
    void getMyProfile_success() throws Exception {

        UserProfileResult response = new UserProfileResult("gowawa@naver.com", "010-1234-1234");

        given(userQueryUseCase.getMyProfile("gowawa@naver.com")).willReturn(response);

        mockMvc.perform(get(ApiUrl.ME)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("내 정보 조회 성공"))
                .andExpect(jsonPath("data.email").value("gowawa@naver.com"))
                .andExpect(jsonPath("data.phoneNumber").value("010-1234-1234"));
    }

}