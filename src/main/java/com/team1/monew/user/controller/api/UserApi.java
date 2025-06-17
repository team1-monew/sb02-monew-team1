package com.team1.monew.user.controller.api;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name="사용자 관리", description = "사용자 관련 API")
public interface UserApi {

    @Operation(summary = "회원가입", description = "이메일과 닉네임을 이용해 사용자를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "409", description = "이메일 중복",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    })
    @PostMapping
    ResponseEntity<UserDto> create(@RequestBody @Valid UserRegisterRequest userRegisterRequest);

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 이용하여 로그인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    })
    @PostMapping("/login")
    ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest userLoginRequest);

    @Operation(summary = "유저 정보 수정", description = "닉네임 등 사용자 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "403", description = "사용자 정보 수정 권한 없음",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    })
    @PatchMapping("/{userId}")
    ResponseEntity<UserDto> update(
        @Parameter(description = "사용자 ID", required = true, in = ParameterIn.PATH)
        @PathVariable Long userId,
        @RequestBody @Valid UserUpdateRequest userUpdateRequest
    );

    @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(
        @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    );

    @Operation(summary = "사용자 물리 삭제", description = "사용자를 DB에서 완전히 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{userId}/hard")
    ResponseEntity<Void> deleteHard(
        @Parameter(description = "사용자 ID", required = true, in = ParameterIn.PATH)
        @PathVariable Long userId
    );
}