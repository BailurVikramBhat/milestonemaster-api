package com.vikrambhat.milestonemaster.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(int status, String message, String uri, Map<String, String> errors) {
    public static ApiErrorResponse error(int status, String message, String uri) {
        return new ApiErrorResponse(status, message, uri, null);
    }
    public static ApiErrorResponse validationErrors(int status, String message, String uri, Map<String, String> validations) {
        return new ApiErrorResponse(status, message, uri, validations);
    }
}
