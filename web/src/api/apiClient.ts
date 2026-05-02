import { config } from '../config';
import type {
  Alert,
  CaregiverProfile,
  ConversationMessage,
  DangerousTopic,
  LoopRule,
  PairingCode,
  Patient,
  PatientDevice,
  SafeMemory,
  Session,
  SessionEvent,
} from '../types';

type JsonBody = Record<string, unknown>;

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
  }
}

export class ApiClient {
  private accessToken: string | null = null;

  setAccessToken(token: string | null) {
    this.accessToken = token?.trim() || null;
  }

  health() {
    return this.request<{ status: string }>('GET', '/health');
  }

  getProfile() {
    return this.request<CaregiverProfile>('GET', '/me/profile');
  }

  saveProfile(fullName: string) {
    return this.request<CaregiverProfile>('PUT', '/me/profile', { fullName });
  }

  listPatients() {
    return this.request<Patient[]>('GET', '/patients');
  }

  createPatient(input: PatientFormInput) {
    return this.request<Patient>('POST', '/patients', patientPayload(input));
  }

  updatePatient(patientId: string, input: PatientFormInput) {
    return this.request<Patient>('PUT', `/patients/${patientId}`, patientPayload(input));
  }

  deletePatient(patientId: string) {
    return this.request<void>('DELETE', `/patients/${patientId}`);
  }

  listLoopRules(patientId: string) {
    return this.request<LoopRule[]>('GET', `/patients/${patientId}/loop-rules`);
  }

  createLoopRule(patientId: string, question: string, answer: string) {
    return this.request<LoopRule>('POST', `/patients/${patientId}/loop-rules`, {
      question,
      answer,
      active: true,
    });
  }

  updateLoopRule(patientId: string, rule: LoopRule) {
    return this.request<LoopRule>('PUT', `/patients/${patientId}/loop-rules/${rule.id}`, rule);
  }

  deleteLoopRule(patientId: string, ruleId: string) {
    return this.request<void>('DELETE', `/patients/${patientId}/loop-rules/${ruleId}`);
  }

  listDangerousTopics(patientId: string) {
    return this.request<DangerousTopic[]>('GET', `/patients/${patientId}/dangerous-topics`);
  }

  createDangerousTopic(patientId: string, term: string, redirectHint: string) {
    return this.request<DangerousTopic>('POST', `/patients/${patientId}/dangerous-topics`, {
      term,
      redirectHint,
      active: true,
    });
  }

  updateDangerousTopic(patientId: string, topic: DangerousTopic) {
    return this.request<DangerousTopic>('PUT', `/patients/${patientId}/dangerous-topics/${topic.id}`, topic);
  }

  deleteDangerousTopic(patientId: string, topicId: string) {
    return this.request<void>('DELETE', `/patients/${patientId}/dangerous-topics/${topicId}`);
  }

  listSafeMemories(patientId: string) {
    return this.request<SafeMemory[]>('GET', `/patients/${patientId}/safe-memories`);
  }

  createSafeMemory(patientId: string, title: string, content: string) {
    return this.request<SafeMemory>('POST', `/patients/${patientId}/safe-memories`, {
      title,
      content,
      active: true,
    });
  }

  updateSafeMemory(patientId: string, memory: SafeMemory) {
    return this.request<SafeMemory>('PUT', `/patients/${patientId}/safe-memories/${memory.id}`, memory);
  }

  deleteSafeMemory(patientId: string, memoryId: string) {
    return this.request<void>('DELETE', `/patients/${patientId}/safe-memories/${memoryId}`);
  }

  createPairingCode(patientId: string) {
    return this.request<PairingCode>('POST', `/patients/${patientId}/pairing-codes`);
  }

  listPatientDevices(patientId: string) {
    return this.request<PatientDevice[]>('GET', `/patients/${patientId}/devices`);
  }

  unlinkPatientDevice(patientId: string, deviceId: string) {
    return this.request<void>('DELETE', `/patients/${patientId}/devices/${deviceId}`);
  }

  createSession(patientId: string) {
    return this.request<Session>('POST', `/patients/${patientId}/sessions`);
  }

  listSessions(patientId: string) {
    return this.request<Session[]>('GET', `/patients/${patientId}/sessions`);
  }

  sessionCommand(sessionId: string, command: 'start' | 'pause' | 'resume' | 'end') {
    return this.request<Session>('POST', `/sessions/${sessionId}/${command}`);
  }

  listTranscript(sessionId: string) {
    return this.request<ConversationMessage[]>('GET', `/sessions/${sessionId}/transcript`);
  }

  listEvents(sessionId: string) {
    return this.request<SessionEvent[]>('GET', `/sessions/${sessionId}/events`);
  }

  listAlerts() {
    return this.request<Alert[]>('GET', '/alerts');
  }

  markAlertRead(alertId: string) {
    return this.request<Alert>('POST', `/alerts/${alertId}/read`);
  }

  private async request<T>(method: string, path: string, body?: JsonBody): Promise<T> {
    const headers = new Headers({ Accept: 'application/json' });
    if (this.accessToken) {
      headers.set('Authorization', `Bearer ${this.accessToken}`);
    }
    if (body) {
      headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(`${config.apiBaseUrl}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });

    const text = await response.text();
    if (!response.ok) {
      throw new ApiError(parseApiError(text, response.status), response.status);
    }
    if (!text) {
      return undefined as T;
    }
    return JSON.parse(text) as T;
  }
}

export type PatientFormInput = {
  fullName: string;
  preferredName: string;
  birthYear?: number | null;
  relationship?: string;
  notes?: string;
};

function patientPayload(input: PatientFormInput): JsonBody {
  return {
    fullName: input.fullName.trim(),
    preferredName: input.preferredName.trim(),
    birthYear: input.birthYear || null,
    relationship: input.relationship?.trim() || '',
    notes: input.notes?.trim() || '',
    textSize: 'large',
    ttsSpeed: 1,
  };
}

function parseApiError(responseText: string, status: number) {
  if (!responseText) {
    return status >= 500 ? 'El backend no esta disponible ahora.' : 'No se pudo completar la operacion.';
  }
  try {
    const parsed = JSON.parse(responseText) as { message?: string; error?: string };
    return parsed.message || parsed.error || responseText;
  } catch {
    return responseText;
  }
}

export const apiClient = new ApiClient();
