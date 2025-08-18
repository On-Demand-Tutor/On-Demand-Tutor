package com.example.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999999,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_EXISTED(1011555,"User  not existeddd",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(2222222,"Usser Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(22222223,"you have not permission",HttpStatus.FORBIDDEN),
    INVALID_KEY(1144444,"mày nhập sai các key ngay bên trên",HttpStatus.BAD_REQUEST),
    USER_NOT_IN_COMPETITION(1555901, "User is not part of this competition", HttpStatus.FORBIDDEN);


    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;


    ErrorCode(int code, String message,HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode=httpStatusCode;
    }
}
