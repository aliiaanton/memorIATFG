-- memorIA MVP - Reparacion no destructiva del esquema Supabase
-- Ejecutar en Supabase SQL Editor si el backend devuelve 500 al listar o crear pacientes.
-- No borra datos: solo anade columnas esperadas por el backend y crea el cuidador demo.

create extension if not exists pgcrypto;

-- Perfil del cuidador usado por la demo local cuando APP_SECURITY_ENABLED=false.
alter table public.caregiver_profiles add column if not exists auth_user_id uuid;
alter table public.caregiver_profiles add column if not exists full_name text;
alter table public.caregiver_profiles add column if not exists created_at timestamptz default now();
alter table public.caregiver_profiles add column if not exists updated_at timestamptz default now();

update public.caregiver_profiles
set full_name = 'Cuidador demo'
where full_name is null or trim(full_name) = '';

alter table public.caregiver_profiles alter column full_name set default 'Cuidador demo';
alter table public.caregiver_profiles alter column full_name set not null;

insert into public.caregiver_profiles (id, full_name)
values ('00000000-0000-0000-0000-000000000001', 'Cuidador demo')
on conflict (id) do update
set full_name = coalesce(nullif(public.caregiver_profiles.full_name, ''), excluded.full_name);

-- Columnas esperadas por el CRUD de pacientes.
alter table public.patients add column if not exists full_name text;
alter table public.patients add column if not exists preferred_name text;
alter table public.patients add column if not exists birth_year integer;
alter table public.patients add column if not exists relationship text;
alter table public.patients add column if not exists notes text;
alter table public.patients add column if not exists text_size text default 'normal';
alter table public.patients add column if not exists tts_speed numeric(3,2) default 1.00;
alter table public.patients add column if not exists created_at timestamptz default now();
alter table public.patients add column if not exists updated_at timestamptz default now();

update public.patients
set full_name = 'Paciente sin nombre'
where full_name is null or trim(full_name) = '';

update public.patients
set text_size = 'normal'
where text_size is null or text_size not in ('normal', 'large');

update public.patients
set tts_speed = 1.00
where tts_speed is null or tts_speed < 0.50 or tts_speed > 1.50;

alter table public.patients alter column full_name set not null;
alter table public.patients alter column text_size set default 'normal';
alter table public.patients alter column text_size set not null;
alter table public.patients alter column tts_speed set default 1.00;
alter table public.patients alter column tts_speed set not null;

-- Indice basico usado por las consultas del backend.
create index if not exists idx_patients_caregiver on public.patients(caregiver_id);
