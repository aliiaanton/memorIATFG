-- memorIA MVP - Supabase schema
-- Run this script in the Supabase SQL editor after creating the project.

create extension if not exists pgcrypto;

-- Updated-at helper
create or replace function public.set_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

-- Caregiver profile.
-- auth_user_id is optional during the local MVP phase. When Supabase Auth is fully integrated,
-- it can be populated with auth.users(id).
create table if not exists public.caregiver_profiles (
  id uuid primary key default gen_random_uuid(),
  auth_user_id uuid unique references auth.users(id) on delete set null,
  full_name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.caregiver_profiles drop constraint if exists caregiver_profiles_id_fkey;
alter table public.caregiver_profiles add column if not exists auth_user_id uuid;

create table if not exists public.patients (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  full_name text not null,
  preferred_name text,
  birth_year integer,
  relationship text,
  notes text,
  text_size text not null default 'normal' check (text_size in ('normal', 'large')),
  tts_speed numeric(3,2) not null default 1.00 check (tts_speed >= 0.50 and tts_speed <= 1.50),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.pairing_codes (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  code text not null unique,
  expires_at timestamptz not null,
  consumed_at timestamptz,
  created_at timestamptz not null default now()
);

create table if not exists public.patient_devices (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  device_identifier text not null,
  device_name text,
  linked_at timestamptz not null default now(),
  revoked_at timestamptz,
  unique (patient_id, device_identifier)
);

create table if not exists public.loop_rules (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  question text not null,
  answer text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.dangerous_topics (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  term text not null,
  redirect_hint text,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.safe_memories (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  title text not null,
  content text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.conversation_sessions (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  status text not null default 'waiting' check (status in ('waiting', 'active', 'paused', 'ended', 'error')),
  started_at timestamptz,
  ended_at timestamptz,
  last_event_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.conversation_messages (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  session_id uuid not null references public.conversation_sessions(id) on delete cascade,
  sender text not null check (sender in ('patient', 'system', 'ai', 'rule')),
  content text not null,
  created_at timestamptz not null default now()
);

create table if not exists public.session_events (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  session_id uuid references public.conversation_sessions(id) on delete cascade,
  event_type text not null,
  description text not null,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create table if not exists public.alerts (
  id uuid primary key default gen_random_uuid(),
  caregiver_id uuid not null references public.caregiver_profiles(id) on delete cascade,
  patient_id uuid not null references public.patients(id) on delete cascade,
  session_id uuid references public.conversation_sessions(id) on delete cascade,
  alert_type text not null,
  severity text not null default 'warning' check (severity in ('info', 'warning', 'critical')),
  title text not null,
  message text not null,
  read_at timestamptz,
  created_at timestamptz not null default now()
);

-- Triggers
drop trigger if exists set_caregiver_profiles_updated_at on public.caregiver_profiles;
create trigger set_caregiver_profiles_updated_at
before update on public.caregiver_profiles
for each row execute function public.set_updated_at();

drop trigger if exists set_patients_updated_at on public.patients;
create trigger set_patients_updated_at
before update on public.patients
for each row execute function public.set_updated_at();

drop trigger if exists set_loop_rules_updated_at on public.loop_rules;
create trigger set_loop_rules_updated_at
before update on public.loop_rules
for each row execute function public.set_updated_at();

drop trigger if exists set_dangerous_topics_updated_at on public.dangerous_topics;
create trigger set_dangerous_topics_updated_at
before update on public.dangerous_topics
for each row execute function public.set_updated_at();

drop trigger if exists set_safe_memories_updated_at on public.safe_memories;
create trigger set_safe_memories_updated_at
before update on public.safe_memories
for each row execute function public.set_updated_at();

drop trigger if exists set_conversation_sessions_updated_at on public.conversation_sessions;
create trigger set_conversation_sessions_updated_at
before update on public.conversation_sessions
for each row execute function public.set_updated_at();

-- Indexes
create index if not exists idx_patients_caregiver on public.patients(caregiver_id);
create index if not exists idx_pairing_codes_code on public.pairing_codes(code);
create index if not exists idx_patient_devices_patient on public.patient_devices(patient_id);
create index if not exists idx_loop_rules_patient on public.loop_rules(patient_id);
create index if not exists idx_dangerous_topics_patient on public.dangerous_topics(patient_id);
create index if not exists idx_safe_memories_patient on public.safe_memories(patient_id);
create index if not exists idx_sessions_patient_created on public.conversation_sessions(patient_id, created_at desc);
create index if not exists idx_messages_session_created on public.conversation_messages(session_id, created_at);
create index if not exists idx_events_session_created on public.session_events(session_id, created_at);
create index if not exists idx_alerts_caregiver_created on public.alerts(caregiver_id, created_at desc);

-- Row Level Security
alter table public.caregiver_profiles enable row level security;
alter table public.patients enable row level security;
alter table public.pairing_codes enable row level security;
alter table public.patient_devices enable row level security;
alter table public.loop_rules enable row level security;
alter table public.dangerous_topics enable row level security;
alter table public.safe_memories enable row level security;
alter table public.conversation_sessions enable row level security;
alter table public.conversation_messages enable row level security;
alter table public.session_events enable row level security;
alter table public.alerts enable row level security;

-- Caregiver profile policies
drop policy if exists "caregiver profile select own" on public.caregiver_profiles;
create policy "caregiver profile select own"
on public.caregiver_profiles for select
using (auth.uid() = auth_user_id);

drop policy if exists "caregiver profile insert own" on public.caregiver_profiles;
create policy "caregiver profile insert own"
on public.caregiver_profiles for insert
with check (auth.uid() = auth_user_id);

drop policy if exists "caregiver profile update own" on public.caregiver_profiles;
create policy "caregiver profile update own"
on public.caregiver_profiles for update
using (auth.uid() = auth_user_id)
with check (auth.uid() = auth_user_id);

-- Generic caregiver-owned table policies.
drop policy if exists "patients caregiver access" on public.patients;
create policy "patients caregiver access"
on public.patients for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "pairing codes caregiver access" on public.pairing_codes;
create policy "pairing codes caregiver access"
on public.pairing_codes for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "patient devices caregiver access" on public.patient_devices;
create policy "patient devices caregiver access"
on public.patient_devices for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "loop rules caregiver access" on public.loop_rules;
create policy "loop rules caregiver access"
on public.loop_rules for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "dangerous topics caregiver access" on public.dangerous_topics;
create policy "dangerous topics caregiver access"
on public.dangerous_topics for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "safe memories caregiver access" on public.safe_memories;
create policy "safe memories caregiver access"
on public.safe_memories for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "sessions caregiver access" on public.conversation_sessions;
create policy "sessions caregiver access"
on public.conversation_sessions for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "messages caregiver access" on public.conversation_messages;
create policy "messages caregiver access"
on public.conversation_messages for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "events caregiver access" on public.session_events;
create policy "events caregiver access"
on public.session_events for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));

drop policy if exists "alerts caregiver access" on public.alerts;
create policy "alerts caregiver access"
on public.alerts for all
using (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
))
with check (exists (
  select 1 from public.caregiver_profiles cp
  where cp.id = caregiver_id and cp.auth_user_id = auth.uid()
));
