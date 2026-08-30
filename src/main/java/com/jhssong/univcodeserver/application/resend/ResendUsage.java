package com.jhssong.univcodeserver.application.resend;

public record ResendUsage(boolean available, long sent, long delivered, long bounced, long opened, long complained) {
    public static ResendUsage unavailable() {
        return new ResendUsage(false, 0, 0, 0, 0, 0);
    }
}
