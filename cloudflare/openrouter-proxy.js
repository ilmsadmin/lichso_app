export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "Method not allowed" }, 405)
    }

    if (!env.OPENROUTER_API_KEY) {
      return json({ error: "Server misconfigured: missing OPENROUTER_API_KEY" }, 500)
    }

    let payload
    try {
      payload = await request.json()
    } catch {
      return json({ error: "Invalid JSON body" }, 400)
    }

    if (!Array.isArray(payload?.messages) || payload.messages.length === 0) {
      return json({ error: "messages is required" }, 400)
    }

    const openRouterRes = await fetch("https://openrouter.ai/api/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.OPENROUTER_API_KEY}`,
        "Content-Type": "application/json",
        "HTTP-Referer": env.OPENROUTER_HTTP_REFERER || "https://lichso.app",
        "X-Title": env.OPENROUTER_APP_TITLE || "Lich So - Lich Van Nien"
      },
      body: JSON.stringify({
        model: payload.model || "google/gemini-2.5-flash",
        messages: payload.messages,
        max_tokens: payload.max_tokens ?? 2048,
        temperature: payload.temperature ?? 0.7
      })
    })

    const text = await openRouterRes.text()
    return new Response(text, {
      status: openRouterRes.status,
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "Cache-Control": "no-store"
      }
    })
  }
}

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store"
    }
  })
}
