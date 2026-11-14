package com.suryawebstudio.mark.executor;

import android.content.Context;
import android.content.pm.PackageManager;

import com.suryawebstudio.mark.tools.Tool;
import com.suryawebstudio.mark.tools.ToolRegistry;
import com.suryawebstudio.mark.tools.ToolTypes;

import org.json.JSONObject;

public final class TaskExecutor {
    private final ToolRegistry registry;

    public TaskExecutor(ToolRegistry registry) { this.registry = registry; }

    public ToolTypes.ToolResult execute(Context context, String toolName, JSONObject args, boolean confirmed, String confirmationHash) {
        Tool tool = registry.find(toolName);
        if (tool == null) return new ToolTypes.ToolResult.Failure("Unknown tool rejected: " + toolName);

        ToolTypes.ValidationResult validation = tool.validate(args);
        if (!validation.valid) return new ToolTypes.ToolResult.Failure(validation.error);

        if (tool.getRiskLevel() != ToolTypes.RiskLevel.SAFE) {
            String expectedHash = ConfirmationHasher.sha256(toolName, args.toString());
            if (!confirmed || !expectedHash.equals(confirmationHash)) {
                return new ToolTypes.ToolResult.NeedsConfirmation("Approval required for " + toolName, args);
            }
        }

        for (String permission : tool.getRequiredPermissions()) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return new ToolTypes.ToolResult.NeedsPermission(permission);
            }
        }

        ToolTypes.ToolResult result = tool.execute(args);
        ToolTypes.VerificationResult verification = tool.verify(args, result);
        if (result instanceof ToolTypes.ToolResult.Success) {
            ToolTypes.ToolResult.Success success = (ToolTypes.ToolResult.Success) result;
            return new ToolTypes.ToolResult.Success(success.data, verification.verified);
        }
        return result;
    }
}
