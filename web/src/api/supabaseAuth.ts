import { config } from '../config';
import type { AuthResult, AuthSession } from '../types';

type SupabaseSessionResponse = {
  access_token?: string;
  refresh_token?: string;
  user?: {
    id?: string;
    email?: string;
    user_metadata?: {
      full_name?: string;
      name?: string;
    };
  };
  session?: SupabaseSessionResponse | null;
};

export class SupabaseAuthClient {
  async signIn(email: string, password: string): Promise<AuthSession> {
    const response = await this.request('/auth/v1/token?grant_type=password', {
      email,
      password,
    });
    return parseRequiredSession(response);
  }

  async signUp(email: string, password: string, fullName: string): Promise<AuthResult> {
    const validation = passwordValidationError(password);
    if (validation) {
      throw new Error(validation);
    }
    const response = await this.request('/auth/v1/signup', {
      email,
      password,
      data: fullName.trim() ? { full_name: fullName.trim() } : {},
    });
    const session = parseOptionalSession(response);
    return session ? { type: 'authenticated', session } : { type: 'pending-confirmation', email };
  }

  async recoverPassword(email: string): Promise<void> {
    await this.request('/auth/v1/recover', { email });
  }

  private async request(path: string, body: Record<string, unknown>): Promise<SupabaseSessionResponse> {
    ensureConfigured();
    const response = await fetch(`${config.supabaseUrl.replace(/\/$/, '')}${path}`, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        apikey: config.supabaseAnonKey,
      },
      body: JSON.stringify(body),
    });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(parseAuthError(text, response.status));
    }
    return text ? (JSON.parse(text) as SupabaseSessionResponse) : {};
  }
}

export function passwordValidationError(password: string): string | null {
  if (password.length < 8) return 'La contrasena debe tener al menos 8 caracteres.';
  if (!/[A-Z]/.test(password)) return 'La contrasena debe incluir al menos una mayuscula.';
  if (!/[0-9]/.test(password)) return 'La contrasena debe incluir al menos un numero.';
  if (!/[^A-Za-z0-9]/.test(password)) return 'La contrasena debe incluir al menos un caracter especial.';
  return null;
}

function parseRequiredSession(response: SupabaseSessionResponse): AuthSession {
  const session = parseOptionalSession(response);
  if (!session) {
    throw new Error('No se pudo iniciar sesion. Revisa tus datos.');
  }
  return session;
}

function parseOptionalSession(response: SupabaseSessionResponse): AuthSession | null {
  const source = response.session || response;
  if (!source.access_token) {
    return null;
  }
  const user = response.user || source.user;
  return {
    accessToken: source.access_token,
    refreshToken: source.refresh_token || null,
    userId: user?.id || null,
    email: user?.email || null,
    fullName: user?.user_metadata?.full_name || user?.user_metadata?.name || null,
  };
}

function ensureConfigured() {
  if (!config.supabaseUrl || !config.supabaseAnonKey) {
    throw new Error('El acceso con cuenta no esta configurado.');
  }
}

function parseAuthError(responseText: string, status: number) {
  let message = '';
  try {
    const parsed = JSON.parse(responseText) as { msg?: string; message?: string; error_description?: string };
    message = parsed.msg || parsed.message || parsed.error_description || '';
  } catch {
    message = responseText;
  }
  if (status === 400 || status === 401) return 'Email o contrasena incorrectos.';
  if (status === 429) return 'Demasiados intentos. Espera un momento.';
  return message || 'No se pudo completar la autenticacion.';
}

export const supabaseAuthClient = new SupabaseAuthClient();
