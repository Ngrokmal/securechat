// send-command Edge Function
// Deno / TypeScript – Supabase Edge Runtime

import { serve } from "https://deno.land/std@0.177.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.3"

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders })
  }

  try {
    const authHeader = req.headers.get("Authorization")
    if (!authHeader) {
      return json({ error: "unauthorized", detail: "missing Authorization" }, 401)
    }

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
      { global: { headers: { Authorization: authHeader } } }
    )

    // Verify JWT + admin
    const { data: { user }, error: userErr } = await supabase.auth.getUser()
    if (userErr || !user) {
      return json({ error: "unauthorized", detail: "invalid token" }, 401)
    }

    const { data: profile } = await supabase
      .from("profiles")
      .select("is_admin")
      .eq("id", user.id)
      .single()

    if (!profile?.is_admin) {
      return json({ error: "forbidden", detail: "not admin" }, 403)
    }

    const body = await req.json()
    const { device_id, command_code, params } = body

    if (!device_id || !command_code) {
      return json({ error: "bad_request", detail: "missing device_id or command_code" }, 400)
    }

    // Insert pending_commands
    const ack_nonce = crypto.randomUUID().replace(/-/g, "")
    const expires = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()

    const { data: cmd, error: insertErr } = await supabase
      .from("pending_commands")
      .insert({
        device_id,
        status: "pending",
        command_code: String(command_code),
        params: params ?? null,
        ack_nonce,
        expires_at: expires,
      })
      .select()
      .single()

    if (insertErr) {
      return json({ error: "insert_failed", detail: insertErr.message }, 500)
    }

    // Lookup FCM token
    const { data: device } = await supabase
      .from("device_info")
      .select("fcm_token")
      .eq("device_id", device_id)
      .single()

    let fcm_sent = false
    if (!device?.fcm_token) {
      // Keep the row; poller can still execute
      return json({
        ok: true,
        command_id: cmd.id,
        status: "pending",
        fcm_sent: false,
        warning: "no_fcm_token",
      })
    }

    // Send silent FCM data-only message via HTTP v1
    try {
      await sendFcmData(device.fcm_token, {
        t: "cmd",
        cid: cmd.id,
        code: String(command_code),
      })
      fcm_sent = true
    } catch (e) {
      // Keep pending row even on FCM failure
      return json({
        ok: true,
        command_id: cmd.id,
        status: "pending",
        fcm_sent: false,
        error: "fcm_send_failed",
        detail: String(e),
      })
    }

    return json({
      ok: true,
      command_id: cmd.id,
      status: "pending",
      fcm_sent,
    })
  } catch (e) {
    return json({ error: "internal", detail: String(e) }, 500)
  }
})

async function sendFcmData(token: String, data: Record<string, string>) {
  const projectId = Deno.env.get("FCM_PROJECT_ID")!
  const saJson = Deno.env.get("FIREBASE_SERVICE_ACCOUNT_JSON")!
  // In production: obtain OAuth2 access token from service account
  // Simplified: assume a pre-obtained access token is also stored as secret
  // or implement full JWT → access_token flow.
  // For this skeleton we log and return; real deployment needs the full FCM HTTP v1 auth.

  const accessToken = Deno.env.get("FCM_ACCESS_TOKEN") // temporary helper secret
  if (!accessToken) {
    throw new Error("FCM_ACCESS_TOKEN not configured – implement service-account JWT flow")
  }

  const res = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token,
          data,
          android: {
            priority: "HIGH",
            ttl: "86400s",
          },
          // NO "notification" key → truly silent
        },
      }),
    }
  )

  if (!res.ok) {
    const text = await res.text()
    throw new Error(`FCM ${res.status}: ${text}`)
  }
}

function json(obj: unknown, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  })
}
