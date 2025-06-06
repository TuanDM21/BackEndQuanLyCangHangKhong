package com.project.quanlycanghangkhong.dto.response.attachment;

import com.project.quanlycanghangkhong.dto.AttachmentDTO;
import com.project.quanlycanghangkhong.dto.response.ApiResponseCustom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "API response for updating attachment", required = true)
public class ApiUpdateAttachmentResponse extends ApiResponseCustom<AttachmentDTO> {
    public ApiUpdateAttachmentResponse(String message, int statusCode, AttachmentDTO data, boolean success) {
        super(message, statusCode, data, success);
    }
}