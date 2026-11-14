package com.suryawebstudio.mark.tools;

import org.json.JSONObject;

public final class ToolTypes {
    private ToolTypes() {}

    public enum RiskLevel { SAFE, CONFIRM, DANGEROUS }

    public static final class ValidationResult {
        public final boolean valid;
        public final String error;
        private ValidationResult(boolean valid, String error) { this.valid = valid; this.error = error; }
        public static ValidationResult valid() { return new ValidationResult(true, null); }
        public static ValidationResult invalid(String error) { return new ValidationResult(false, error); }
    }

    public static final class VerificationResult {
        public final boolean verified;
        public final String message;
        public VerificationResult(boolean verified, String message) { this.verified = verified; this.message = message; }
    }

    public abstract static class ToolResult {
        public static final class Success extends ToolResult {
            public final JSONObject data; public final boolean verified;
            public Success(JSONObject data, boolean verified) { this.data = data; this.verified = verified; }
        }
        public static final class NeedsConfirmation extends ToolResult {
            public final String summary; public final JSONObject payload;
            public NeedsConfirmation(String summary, JSONObject payload) { this.summary = summary; this.payload = payload; }
        }
        public static final class NeedsPermission extends ToolResult {
            public final String permission;
            public NeedsPermission(String permission) { this.permission = permission; }
        }
        public static final class Unsupported extends ToolResult {
            public final String reason; public final String fallbackAction;
            public Unsupported(String reason, String fallbackAction) { this.reason = reason; this.fallbackAction = fallbackAction; }
        }
        public static final class Failure extends ToolResult {
            public final String error;
            public Failure(String error) { this.error = error; }
        }
    }
}
