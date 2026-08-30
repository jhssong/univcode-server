package com.jhssong.univcodeserver.application.resend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendMetricsResponse(Map<String, Object> totals) {}
