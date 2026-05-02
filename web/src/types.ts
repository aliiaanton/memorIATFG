export type AuthSession = {
  accessToken: string;
  refreshToken?: string | null;
  userId?: string | null;
  email?: string | null;
  fullName?: string | null;
};

export type AuthResult =
  | { type: 'authenticated'; session: AuthSession }
  | { type: 'pending-confirmation'; email: string };

export type CaregiverProfile = {
  id: string;
  fullName: string;
};

export type Patient = {
  id: string;
  fullName: string;
  preferredName?: string | null;
  birthYear?: number | null;
  relationship?: string | null;
  notes?: string | null;
};

export type LoopRule = {
  id: string;
  question: string;
  answer: string;
  active: boolean;
};

export type DangerousTopic = {
  id: string;
  term: string;
  redirectHint?: string | null;
  active: boolean;
};

export type SafeMemory = {
  id: string;
  title: string;
  content: string;
  active: boolean;
};

export type PatientDevice = {
  id: string;
  deviceIdentifier: string;
  deviceName?: string | null;
  linkedAt?: string | null;
};

export type PairingCode = {
  code: string;
};

export type Session = {
  id: string;
  status: 'waiting' | 'active' | 'paused' | 'ended' | string;
  startedAt?: string | null;
  endedAt?: string | null;
  createdAt?: string | null;
};

export type ConversationMessage = {
  sender: string;
  content: string;
  createdAt?: string | null;
};

export type SessionEvent = {
  eventType: string;
  description: string;
  createdAt?: string | null;
};

export type Alert = {
  id?: string | null;
  sessionId?: string | null;
  title: string;
  message: string;
  severity?: string | null;
  readAt?: string | null;
  createdAt?: string | null;
};
