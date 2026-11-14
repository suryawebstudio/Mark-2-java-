package com.suryawebstudio.mark.tools;

import org.json.JSONObject;

public interface Tool {
    String getName();
    String getDescription();
    JSONObject getParametersSchema();
    ToolTypes.RiskLevel getRiskLevel();
    String[] getRequiredPermissions();
    ToolTypes.ValidationResult validate(JSONObject args);
    ToolTypes.ToolResult execute(JSONObject args);
    ToolTypes.VerificationResult verify(JSONObject args, ToolTypes.ToolResult result);
}
