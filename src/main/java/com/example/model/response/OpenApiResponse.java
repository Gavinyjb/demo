package com.example.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "统一响应格式")
public class OpenApiResponse<T> {
    
    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "是否成功")
    private boolean success = true;

    public OpenApiResponse(T data, String requestId) {
        this.data = data;
        this.requestId = requestId;
    }

    public static <T> OpenApiResponse<T> success(T data, String requestId) {
        return new OpenApiResponse<>(data, requestId);
    }
} 