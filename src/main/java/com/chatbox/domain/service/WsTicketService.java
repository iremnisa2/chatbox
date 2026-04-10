package com.chatbox.domain.service;

public interface WsTicketService {

    IssueResponse issueForUsername(String username);

   ValidationResult validateAndConsume(String ticket);

    record IssueResponse(String ticket, int expiresInSeconds) {}

    record ValidationResult(boolean valid, String reason, String username, long issuedAtEpochMs) {
       public static ValidationResult valid(String username, long issuedAtEpochMs) {
            return new ValidationResult(true, "OK", username, issuedAtEpochMs);
        }
       public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason, null, -1);
        }
    }
}