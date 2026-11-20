package com.proga.notification_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponse {
    private String latestVersion;
    private int latestVersionCode;
    private String minRequiredVersion;
    private boolean forceUpdate;
    private String downloadUrl;
    private String releaseNotes;
    private String releaseDate;
}
