package com.suryawebstudio.mark.tools.impl;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.suryawebstudio.mark.tools.Tool;
import com.suryawebstudio.mark.tools.ToolTypes;

import org.json.JSONObject;

public final class BatteryInfoTool implements Tool {
    private final Context context;
    public BatteryInfoTool(Context context) { this.context = context.getApplicationContext(); }
    public String getName() { return "battery_info"; }
    public String getDescription() { return "Read the current battery percentage and charging state."; }
    public JSONObject getParametersSchema() { return new JSONObject(); }
    public ToolTypes.RiskLevel getRiskLevel() { return ToolTypes.RiskLevel.SAFE; }
    public String[] getRequiredPermissions() { return new String[0]; }
    public ToolTypes.ValidationResult validate(JSONObject args) { return ToolTypes.ValidationResult.valid(); }

    public ToolTypes.ToolResult execute(JSONObject args) {
        Intent intent = context.registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return new ToolTypes.ToolResult.Failure("Battery status is unavailable.");
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (level < 0 || scale <= 0) return new ToolTypes.ToolResult.Failure("Battery percentage is unavailable.");
        JSONObject data = new JSONObject();
        try {
            data.put("percent", Math.round(level * 100f / scale));
            data.put("charging", status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
            return new ToolTypes.ToolResult.Success(data, true);
        } catch (Exception error) { return new ToolTypes.ToolResult.Failure("Could not format battery status."); }
    }

    public ToolTypes.VerificationResult verify(JSONObject args, ToolTypes.ToolResult result) {
        return new ToolTypes.VerificationResult(result instanceof ToolTypes.ToolResult.Success, "Battery status read back from Android.");
    }
}
