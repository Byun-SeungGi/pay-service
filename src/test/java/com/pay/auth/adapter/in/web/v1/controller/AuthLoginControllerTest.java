package com.pay.auth.adapter.in.web.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.auth.adapter.in.web.v1.dto.AuthLoginRequestDto;
import com.pay.auth.application.port.in.AuthLoginCommand;
import com.pay.auth.application.service.AuthService;
import com.pay.global.constant.ApiUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.print.attribute.standard.Media;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthLoginController.class)
class AuthLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("로그인이 성공하면 200응답, JWT 토큰 반환")
    @WithMockUser
    void login_success() throws Exception {

        AuthLoginRequestDto request = new AuthLoginRequestDto("gowawa@naver.com", "q!w2e3r4t5");
        String fakeToken = "mock.jwt.token";

        given(authService.login(any(AuthLoginCommand.class))).willReturn(fakeToken);

        mockMvc.perform(post(ApiUrl.LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("message").value("로그인 성공"))
                .andExpect(jsonPath("data.accessToken").value(fakeToken));
    }

    @Test
    @DisplayName("이메일 형식이 잘못되면 400 에러발생")
    @WithMockUser
    void login_api_fail_invalid_email() throws Exception {
        AuthLoginRequestDto request = new AuthLoginRequestDto("invalid-email", "password123!");

        mockMvc.perform(post(ApiUrl.LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}