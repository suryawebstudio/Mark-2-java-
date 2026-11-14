const SYSTEM_INSTRUCTION = "You are MARK, a personal AI assistant. Be concise, honest, and helpful. Act only through allowlisted tools when tools are provided. Never claim an action succeeded without verified=true.";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Cache-Control": "no-store"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: { ...corsHeaders, "Content-Type": "application/json" } });
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: corsHeaders });
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/") return json({ status: "online", assistant: "MARK", version: "live-voice-1" });

    if (request.method === "POST" && url.pathname === "/live-token") {
      if (!env.GEMINI_API_KEY) return json({ error: "GEMINI_API_KEY secret is missing." }, 500);
      try {
        const now = Date.now();
        const tokenResponse = await fetch("https://generativelanguage.googleapis.com/v1beta/auth_tokens", {
          method: "POST",
          headers: { "Content-Type": "application/json", "x-goog-api-key": env.GEMINI_API_KEY },
          body: JSON.stringify({
            uses: 1,
            expireTime: new Date(now + 30 * 60 * 1000).toISOString(),
            newSessionExpireTime: new Date(now + 60 * 1000).toISOString(),
            liveConnectConstraints: {
              model: "models/gemini-3.1-flash-live-preview",
              config: { responseModalities: ["AUDIO"], sessionResumption: {} }
            }
          })
        });
        const tokenData = await tokenResponse.json();
        if (!tokenResponse.ok || !tokenData.name) return json({ error: tokenData.error?.message || "Gemini could not issue a Live token." }, tokenResponse.status || 502);
        return json({ token: tokenData.name, model: "models/gemini-3.1-flash-live-preview", expiresInSeconds: 1800 });
      } catch (error) {
        return json({ error: "Live token request failed." }, 502);
      }
    }

    return json({ error: "Endpoint not found." }, 404);
  }
};
