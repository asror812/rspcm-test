package org.example.rspcm.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodes {

    InvalidParams(400, "INVALID_PARAMS"),
    BadRequest(400, "BAD_REQUEST"),
    Unauthorized(401, "UNAUTHORIZED"),
    Forbidden(403, "FORBIDDEN"),
    NotFound(404, "NOT_FOUND"),
    InternalServerError(500, "INTERNAL_SERVER_ERROR"),
    AlreadyExists(409, "AlreadyExists");

    private final int statusCode;
    private final String name;
}
