// claim-command Edge Function
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
    const { id } = body
    if (!id) {
      return json({ error: "bad_request", detail: "missing id" }, 400)
    }

    // Load command
    const { data: cmd, error } = await supabase
      .from("pending_commands")
      .select("*")
      .eq("id", id)
      .single()

    if (error || !cmd) {
      return json({ error: "command_not_found", detail: "no such command" }, 404)
    }

    // Ownership check via device_info.user_id
    const { data: device } = await supabase
      .from("device_info")
      .select("user_id")
      .eq("device_id", cmd.device_id)
      .single()

    if (!device || device.user_id !== user.id) {
      return json({ error: "device_not_owned", detail: "device does not belong to caller" }, 403)
    }

    if (cmd.status !== "pending" && cmd.status !== "delivered") {
      return json({ error: "already_claimed", detail: `status=${cmd.status}` }, 409)
    }

    if (cmd.expires_at && new Date(cmd.expires_at) < new Date()) {
      await supabase.from("pending_commands").update({ status: "expired" }).eq("id", id)
      return json({ error: "expired", detail: "command expired" }, 410)
    }

    // Claim → delivered
    const { data: updated, error: updErr } = await supabase
      .from("pending_commands")
      .update({
        status: "delivered",
        delivered_at: new Date().toISOString(),
      })
      .eq("id", id)
      .select()
      .single()

    if (updErr) {
      return json({ error: "claim_failed", detail: updErr.message }, 500)
    }

    return json({
      id: updated.id,
      device_id: updated.device_id,
      status: "delivered",
      ack_nonce: updated.ack_nonce,
      command_code: updated.command_code,
      params: updated.params,
    })
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
