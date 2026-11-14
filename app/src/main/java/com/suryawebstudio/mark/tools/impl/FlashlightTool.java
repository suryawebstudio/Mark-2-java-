package com.suryawebstudio.mark.tools.impl;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import com.suryawebstudio.mark.tools.Tool;
import com.suryawebstudio.mark.tools.ToolTypes;

import org.json.JSONObject;

public final class FlashlightTool implements Tool {
    private final CameraManager cameraManager;
    private String cameraId;
    private volatile Boolean lastTorchState;

    public FlashlightTool(Context context) {
        cameraManager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Boolean flash = cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (Boolean.TRUE.equals(flash)) { cameraId = id; break; }
            }
        } catch (Exception ignored) { cameraId = null; }
        if (cameraId != null) {
            cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                @Override public void onTorchModeChanged(String id, boolean enabled) {
                    if (id.equals(cameraId)) lastTorchState = enabled;
                }
            }, null);
        }
    }

    public String getName() { return "flashlight"; }
    public String getDescription() { return "Turn the device flashlight on or off."; }
    public JSONObject getParametersSchema() {
        try {
            return new JSONObject().put("type", "object").put("required", new org.json.JSONArray().put("enabled"));
        } catch (Exception error) {
            return new JSONObject();
        }
    }
    public ToolTypes.RiskLevel getRiskLevel() { return ToolTypes.RiskLevel.SAFE; }
    public String[] getRequiredPermissions() { return new String[0]; }
    public ToolTypes.ValidationResult validate(JSONObject args) {
        return args.has("enabled") && args.opt("enabled") instanceof Boolean
                ? ToolTypes.ValidationResult.valid()
                : ToolTypes.ValidationResult.invalid("flashlight.enabled must be a boolean.");
    }

    public ToolTypes.ToolResult execute(JSONObject args) {
        if (cameraId == null) return new ToolTypes.ToolResult.Unsupported("No camera flashlight is available.", null);
        try {
            cameraManager.setTorchMode(cameraId, args.getBoolean("enabled"));
            JSONObject data = new JSONObject().put("requested", args.getBoolean("enabled"));
            return new ToolTypes.ToolResult.Success(data, false);
        } catch (Exception error) { return new ToolTypes.ToolResult.Failure("Android could not change the flashlight: " + error.getClass().getSimpleName()); }
    }

    public ToolTypes.VerificationResult verify(JSONObject args, ToolTypes.ToolResult result) {
        if (!(result instanceof ToolTypes.ToolResult.Success)) return new ToolTypes.VerificationResult(false, "Flashlight change failed.");
        boolean expected = args.optBoolean("enabled");
        boolean verified = lastTorchState != null && lastTorchState == expected;
        return new ToolTypes.VerificationResult(verified, verified ? "Torch callback confirmed the requested state." : "Torch callback did not confirm the requested state.");
    }
}
