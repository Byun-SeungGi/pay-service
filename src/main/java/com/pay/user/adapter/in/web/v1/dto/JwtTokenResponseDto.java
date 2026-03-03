package com.pay.user.adapter.in.web.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponseDto {

    @Schema(description = "발급된 Access Token")
    private String accessToken;
}
