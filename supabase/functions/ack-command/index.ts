// ack-command Edge Function
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

    const { data: { user }, error: userErr } = await supabase.auth.getUser()
    if (userErr || !user) {
      return json({ error: "unauthorized", detail: "invalid token" }, 401)
    }

    const body = await req.json()
    const { id, ack_nonce, success, message, error_code, error_detail, payload } = body

    if (!id || !ack_nonce) {
      return json({ error: "bad_request", detail: "missing id or ack_nonce" }, 400)
    }

    const { data: cmd, error } = await supabase
      .from("pending_commands")
      .select("*")
      .eq("id", id)
      .single()

    if (error || !cmd) {
      return json({ error: "command_not_found", detail: "no such command" }, 404)
    }

    // Ownership
    const { data: device } = await supabase
      .from("device_info")
      .select("user_id")
      .eq("device_id", cmd.device_id)
      .single()

    if (!device || device.user_id !== user.id) {
      return json({ error: "device_not_owned", detail: "device does not belong to caller" }, 403)
    }

    if (cmd.ack_nonce !== ack_nonce) {
      return json({ error: "nonce_mismatch", detail: "ack_nonce does not match" }, 409)
    }

    if (cmd.status === "executed" || cmd.status === "failed") {
      return json({ error: "already_finalized", detail: `status=${cmd.status}` }, 409)
    }

    const finalStatus = success ? "executed" : "failed"

    await supabase
      .from("pending_commands")
      .update({
        status: finalStatus,
        result_message: message ?? null,
        error_code: error_code ?? null,
        error_detail: error_detail ?? null,
        finalized_at: new Date().toISOString(),
      })
      .eq("id", id)

    // Insert command_results
    await supabase.from("command_results").insert({
      pending_command_id: id,
      device_id: cmd.device_id,
      command_code: cmd.command_code,
      success: !!success,
      message: message ?? null,
      error_code: error_code ?? null,
      error_detail: error_detail ?? null,
      payload: payload ? (typeof payload === "string" ? { raw: payload } : payload) : null,
    })

    return json({ ok: true, status: finalStatus })
  } catch (e) {
    return json({ error: "internal", detail: String(e) }, 500)
  }
})

function json(obj: unknown, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  })
}
