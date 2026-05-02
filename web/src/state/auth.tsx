import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { apiClient } from '../api/apiClient';
import { supabaseAuthClient } from '../api/supabaseAuth';
import type { AuthResult, AuthSession } from '../types';

type AuthContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, fullName: string) => Promise<AuthResult>;
  recoverPassword: (email: string) => Promise<void>;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);
const STORAGE_KEY = 'memoria.web.auth';
const PENDING_PROFILE_NAME_KEY = 'memoria.web.pendingProfileName';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => loadSession());

  useEffect(() => {
    apiClient.setAccessToken(session?.accessToken || null);
    if (session) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [session]);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: Boolean(session?.accessToken),
      signIn: async (email, password) => {
        const nextSession = await supabaseAuthClient.signIn(email, password);
        await syncCaregiverProfile(nextSession, false);
        setSession(nextSession);
      },
      signUp: async (email, password, fullName) => {
        const result = await supabaseAuthClient.signUp(email, password, fullName);
        const cleanName = fullName.trim();

        if (result.type === 'authenticated') {
          const sessionWithName: AuthSession = {
            ...result.session,
            fullName: cleanName || result.session.fullName || null,
          };
          await syncCaregiverProfile(sessionWithName, true);
          setSession(sessionWithName);
          return { ...result, session: sessionWithName };
        }

        if (cleanName) {
          localStorage.setItem(PENDING_PROFILE_NAME_KEY, cleanName);
        }
        return result;
      },
      recoverPassword: (email) => supabaseAuthClient.recoverPassword(email),
      signOut: () => setSession(null),
    }),
    [session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

async function syncCaregiverProfile(session: AuthSession, overwrite: boolean) {
  apiClient.setAccessToken(session.accessToken);
  const metadataName = session.fullName?.trim();
  const pendingName = localStorage.getItem(PENDING_PROFILE_NAME_KEY)?.trim();
  const profileName = metadataName || pendingName;

  if (!profileName) {
    return;
  }

  if (!overwrite) {
    try {
      const currentProfile = await apiClient.getProfile();
      const currentName = currentProfile.fullName?.trim();
      if (currentName && currentName !== 'Cuidador') {
        localStorage.removeItem(PENDING_PROFILE_NAME_KEY);
        return;
      }
    } catch {
      // If the profile cannot be loaded, try to save the known registration name below.
    }
  }

  try {
    await apiClient.saveProfile(profileName);
    localStorage.removeItem(PENDING_PROFILE_NAME_KEY);
  } catch {
    // Authentication should still succeed; the dashboard can retry loading the profile.
  }
}

function loadSession(): AuthSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as AuthSession;
    return parsed.accessToken ? parsed : null;
  } catch {
    return null;
  }
}
