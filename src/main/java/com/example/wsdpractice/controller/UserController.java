package com.example.wsdpractice.controller;

import com.example.wsdpractice.dto.ApiResponse;
import com.example.wsdpractice.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // Controller에서 데이터 관리
    private final Map<Long, UserDto> store = new ConcurrentHashMap<>();
    private final Map<String, UserDto> userIdIndex = new ConcurrentHashMap<>(); // userId로 검색용
    private final AtomicLong sequence = new AtomicLong(1);

    // POST 1: 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody UserDto user) {
        // 유효성 검사
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("사용자 ID는 필수입니다"));
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("비밀번호는 필수입니다"));
        }

        // 중복 확인
        if (userIdIndex.containsKey(user.getUserId())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("이미 존재하는 사용자 ID입니다"));
        }

        // 데이터 생성
        user.setId(sequence.getAndIncrement());
        store.put(user.getId(), user);
        userIdIndex.put(user.getUserId(), user);

        // 응답 시 비밀번호 제거
        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // POST 2: 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestHeader(value = "X-USER-ID") String userId,
            @RequestHeader(value = "X-USER-PW") String password) {

        // 사용자 조회
        UserDto user = userIdIndex.get(userId);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("사용자를 찾을 수 없습니다"));
        }

        // 비밀번호 확인
        if (!user.getPassword().equals(password)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("비밀번호가 일치하지 않습니다"));
        }

        // 로그인 성공
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        result.put("message", "로그인 성공");

        return ResponseEntity
                .ok(ApiResponse.success(result));
    }

    // GET 1: 전체 회원 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = new ArrayList<>();

        // 비밀번호 제거하고 반환
        for (UserDto user : store.values()) {
            UserDto response = new UserDto();
            response.setId(user.getId());
            response.setUserId(user.getUserId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            users.add(response);
        }

        return ResponseEntity
                .ok(ApiResponse.success(users));
    }

    // GET 2: 특정 회원 조회 - id로
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = store.get(id);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ID " + id + "에 해당하는 사용자를 찾을 수 없습니다"));
        }

        // 비밀번호 제거
        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    // PUT 1: 회원 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto updateDto) {

        UserDto user = store.get(id);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ID " + id + "에 해당하는 사용자를 찾을 수 없습니다"));
        }

        // 필드별 업데이트
        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }
        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }

        // 비밀번호 제거하고 반환
        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    // PUT 2: 비밀번호 변경
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long id,
            @RequestHeader(value = "X-OLD-PW") String oldPassword,
            @RequestHeader(value = "X-NEW-PW") String newPassword) {

        UserDto user = store.get(id);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ID " + id + "에 해당하는 사용자를 찾을 수 없습니다"));
        }

        // 기존 비밀번호 확인
        if (!user.getPassword().equals(oldPassword)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("기존 비밀번호가 일치하지 않습니다"));
        }

        // 새 비밀번호 유효성 검사
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("새 비밀번호를 입력해주세요"));
        }

        // 비밀번호 변경
        user.setPassword(newPassword);

        return ResponseEntity
                .ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다"));
    }
    
    // DELETE 1: 특정 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        UserDto removed = store.remove(id);

        if (removed == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("ID " + id + "에 해당하는 사용자를 찾을 수 없습니다"));
        }

        // userIdIndex에서도 제거
        userIdIndex.remove(removed.getUserId());

        return ResponseEntity
                .ok(ApiResponse.success("사용자가 성공적으로 삭제되었습니다"));
    }

    // DELETE 2: 전체 회원 삭제
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAllUsers() {
        try {
            int count = store.size();
            store.clear();
            userIdIndex.clear();

            Map<String, Integer> result = new HashMap<>();
            result.put("deletedCount", count);

            return ResponseEntity
                    .ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
