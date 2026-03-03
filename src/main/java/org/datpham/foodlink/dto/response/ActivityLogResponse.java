package org.datpham.foodlink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ActivityLogResponse {
    private String id;
    private String action;
    private String entityType;
    private String entityId;
    private String description;
    private String performedBy;
    private LocalDateTime createdAt;
}
