package com.suryawebstudio.mark.ai;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class LiveTokenClient {
    public interface CallbackResult { void onSuccess(String token, String model); void onFailure(String message); }
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final String endpoint;

    public LiveTokenClient(String endpoint) { this.endpoint = endpoint; }

    public void request(CallbackResult callback) {
        Request request = new Request.Builder().url(endpoint).post(RequestBody.create("{}", JSON)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException error) { callback.onFailure("MARK backend unavailable: " + error.getMessage()); }
            @Override public void onResponse(Call call, Response response) {
                try (Response ignored = response) {
                    String body = response.body() == null ? "{}" : response.body().string();
                    JSONObject data = new JSONObject(body);
                    if (!response.isSuccessful() || !data.has("token")) { callback.onFailure(data.optString("error", "MARK backend did not return a Live token.")); return; }
                    callback.onSuccess(data.getString("token"), data.optString("model", "models/gemini-3.1-flash-live-preview"));
                } catch (Exception error) { callback.onFailure("Invalid Live token response."); }
            }
        });
    }
}
