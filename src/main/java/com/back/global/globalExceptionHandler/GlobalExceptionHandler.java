package com.back.global.globalExceptionHandler;

import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형태로 처리
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 400 Bad Request - 잘못된 요청 파라미터
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        
        log.warn("Bad Request: {} at {}", e.getMessage(), request.getRequestURI());
        
        return ResponseEntity.badRequest()
                .body(RsData.of(
                        "400",
                        e.getMessage(), 
                        null
                ));
    }

    /**
     * 400 Bad Request - 유효성 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검증에 실패했습니다.");
        
        log.warn("Validation Error: {} at {}", errorMessage, request.getRequestURI());
        
        return ResponseEntity.badRequest()
                .body(RsData.of(
                        "400",
                        errorMessage, 
                        null
                ));
    }

    /**
     * 400 Bad Request - 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RsData<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        String message = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());
        
        log.warn("Type Mismatch: {} at {}", message, request.getRequestURI());
        
        return ResponseEntity.badRequest()
                .body(RsData.of(
                        "400",
                        message, 
                        null
                ));
    }

    /**
     * 404 Not Found - 리소스를 찾을 수 없음
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handleNoSuchElementException(
            NoSuchElementException e, HttpServletRequest request) {
        
        log.warn("Not Found: {} at {}", e.getMessage(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RsData.of(
                        "404",
                        "요청한 리소스를 찾을 수 없습니다.", 
                        null
                ));
    }

    /**
     * 500 Internal Server Error - 예상치 못한 서버 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleGeneralException(
            Exception e, HttpServletRequest request) {
        
        log.error("Internal Server Error: {} at {}", e.getMessage(), request.getRequestURI(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RsData.of(
                        "500",
                        "서버 내부 오류가 발생했습니다.", 
                        null
                ));
    }

    /**
     * 커스텀 예외 클래스 - 비즈니스 로직 오류
     * 1. 계정이 잠겨있는 경우
     * 2. 잔액이 부족한 경우
     * 3. 이미 신청한 작가인 경우
     * 4. 주문을 취소할 수 없는 상태인 경우
     */
    @Getter
    public static class BusinessException extends RuntimeException {
        private final String code;
        
        public BusinessException(String code, String message) {
            super(message);
            this.code = code;
        }
    }

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RsData<Void>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {

        log.warn("Business Exception: {} - {} at {}", e.getCode(), e.getMessage(), request.getRequestURI());

        return ResponseEntity.badRequest()
                .body(RsData.of(
                        e.getCode(),
                        e.getMessage(),
                        null
                ));
    }

    /**
     * ServiceException 처리 - 비즈니스 로직 예외를 적절한 HTTP 상태 코드로 변환
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handleServiceException(ServiceException ex, HttpServletRequest request) {

        log.warn("ServiceException: {} - {} at {}",
                ex.getResultCode(), ex.getMsg(), request.getRequestURI());

        return new ResponseEntity<>(
                ex.getRsData(),
                ResponseEntity.status(ex.getRsData().statusCode()).build().getStatusCode()
        );
    }
}
