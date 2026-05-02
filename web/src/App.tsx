import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, Navigate, NavLink, Route, Routes, useNavigate } from 'react-router-dom';
import {
  Activity,
  AlertTriangle,
  Bell,
  BookOpen,
  Brain,
  Check,
  ChevronRight,
  Download,
  HeartPulse,
  Home,
  Link as LinkIcon,
  LogOut,
  Menu,
  Mic,
  Pause,
  Play,
  Plus,
  RefreshCcw,
  Shield,
  Smartphone,
  Trash2,
  UserRound,
  X,
} from 'lucide-react';
import { apiClient, type PatientFormInput } from './api/apiClient';
import { config, pricingPlans } from './config';
import { useAuth } from './state/auth';
import type {
  Alert,
  ConversationMessage,
  DangerousTopic,
  LoopRule,
  Patient,
  PatientDevice,
  SafeMemory,
  Session,
  SessionEvent,
} from './types';

export function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/caregiver/*"
        element={
          <ProtectedRoute>
            <CaregiverApp />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function LandingPage() {
  const [menuOpen, setMenuOpen] = useState(false);
  const navItems = [
    { href: '#funciona', label: 'Como funciona' },
    { href: '#cuidador', label: 'Cuidador' },
    { href: '#precios', label: 'Precios' },
    { href: '#descarga', label: 'Descarga' },
  ];

  return (
    <div className="public-shell">
      <header className="public-header">
        <Link to="/" className="brand-link" aria-label="memorIA inicio">
          <span className="brand-mark">
            <HeartPulse size={22} />
          </span>
          <span>memorIA</span>
        </Link>
        <button className="icon-button mobile-only" type="button" onClick={() => setMenuOpen((value) => !value)}>
          {menuOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
        <nav className={`public-nav ${menuOpen ? 'open' : ''}`}>
          {navItems.map((item) => (
            <a key={item.href} href={item.href} onClick={() => setMenuOpen(false)}>
              {item.label}
            </a>
          ))}
          <Link className="button ghost compact" to="/login">
            Acceder
          </Link>
        </nav>
      </header>

      <main>
        <section className="hero-section">
          <img className="hero-image" src="/assets/hero-care.png" alt="Cuidador acompanando a una persona mayor" />
          <div className="hero-overlay" />
          <div className="hero-content">
            <p className="eyebrow">Teleasistencia inteligente para familias</p>
            <h1>memorIA</h1>
            <p className="hero-copy">
              Una app para acompanar conversaciones, configurar respuestas seguras y dar tranquilidad al cuidador.
            </p>
            <div className="hero-actions">
              <a className="button primary" href={config.apkDownloadUrl}>
                <Download size={18} />
                Descargar app
              </a>
              <Link className="button light" to="/login">
                Acceder como cuidador
              </Link>
            </div>
          </div>
        </section>

        <section id="funciona" className="section-band">
          <SectionHeader
            eyebrow="Flujo completo"
            title="Del perfil del paciente a una sesion supervisada"
            text="memorIA une una terminal sencilla para el paciente con un panel de control para el cuidador."
          />
          <div className="feature-grid">
            <FeatureCard icon={<UserRound />} title="Configura el perfil" text="Datos basicos, notas y preferencias de voz." />
            <FeatureCard icon={<Brain />} title="Define la IA segura" text="Bucles, temas delicados y recuerdos positivos." />
            <FeatureCard icon={<Smartphone />} title="Vincula la tablet" text="Codigo temporal para conectar el modo paciente." />
            <FeatureCard icon={<Activity />} title="Supervisa sesiones" text="Control, diario, eventos y alertas desde el panel." />
          </div>
        </section>

        <section id="cuidador" className="section-band muted-band">
          <div className="split-layout">
            <div>
              <p className="eyebrow">Modo cuidador</p>
              <h2>Un panel pensado para actuar rapido</h2>
              <p>
                Desde la web puedes preparar una sesion, revisar alertas y consultar el transcript sin depender del
                movil. La informacion se organiza para escanearla en segundos.
              </p>
              <ul className="check-list">
                <li>Pacientes y configuracion IA.</li>
                <li>Sesiones con iniciar, pausar y finalizar.</li>
                <li>Diario de mensajes, eventos y alertas.</li>
              </ul>
            </div>
            <div className="dashboard-preview" aria-label="Vista previa del panel cuidador">
              <div className="preview-topbar" />
              <div className="preview-grid">
                <span />
                <span />
                <span />
                <span />
              </div>
              <div className="preview-lines">
                <span />
                <span />
                <span />
              </div>
            </div>
          </div>
        </section>

        <section className="section-band">
          <SectionHeader
            eyebrow="Seguridad"
            title="La IA no decide sola"
            text="Primero se aplican reglas del cuidador. La IA solo responde cuando no hay una regla configurada aplicable."
          />
          <div className="feature-grid three">
            <FeatureCard icon={<Shield />} title="Temas delicados" text="Redireccion suave ante palabras configuradas." />
            <FeatureCard icon={<BookOpen />} title="Recuerdos seguros" text="Temas positivos para orientar la conversacion." />
            <FeatureCard icon={<Bell />} title="Alertas" text="Avisos visibles para revisar despues de la sesion." />
          </div>
        </section>

        <section id="precios" className="section-band muted-band">
          <SectionHeader
            eyebrow="Precios"
            title="Planes editables para la demo"
            text="Los importes se pueden cambiar antes de publicar o defender el proyecto."
          />
          <div className="pricing-grid">
            {pricingPlans.map((plan) => (
              <article className={`price-card ${plan.featured ? 'featured' : ''}`} key={plan.name}>
                <h3>{plan.name}</h3>
                <p className="price">{plan.price}</p>
                <p>{plan.description}</p>
                <ul>
                  {plan.features.map((feature) => (
                    <li key={feature}>
                      <Check size={16} />
                      {feature}
                    </li>
                  ))}
                </ul>
              </article>
            ))}
          </div>
        </section>

        <section id="descarga" className="download-band">
          <div>
            <p className="eyebrow">Descarga</p>
            <h2>Instala memorIA en la tablet del paciente</h2>
            <p>El enlace del APK es configurable para que puedas actualizarlo sin tocar el codigo.</p>
          </div>
          <a className="button primary" href={config.apkDownloadUrl}>
            <Download size={18} />
            Descargar APK
          </a>
        </section>
      </main>
    </div>
  );
}

function LoginPage() {
  const { isAuthenticated, signIn, signUp, recoverPassword } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<'login' | 'signup' | 'recover'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/caregiver', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setStatus('');
    try {
      if (mode === 'recover') {
        await recoverPassword(email.trim());
        setStatus('Te hemos enviado las instrucciones de recuperacion.');
      } else if (mode === 'signup') {
        const result = await signUp(email.trim(), password, fullName.trim());
        if (result.type === 'pending-confirmation') {
          setStatus('Revisa tu correo para confirmar la cuenta.');
        } else {
          navigate('/caregiver', { replace: true });
        }
      } else {
        await signIn(email.trim(), password);
        navigate('/caregiver', { replace: true });
      }
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'No se pudo completar la accion.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <Link to="/" className="brand-link">
        <span className="brand-mark">
          <HeartPulse size={22} />
        </span>
        <span>memorIA</span>
      </Link>
      <section className="auth-panel">
        <div>
          <p className="eyebrow">Acceso cuidador</p>
          <h1>{mode === 'signup' ? 'Crear cuenta' : mode === 'recover' ? 'Recuperar acceso' : 'Iniciar sesion'}</h1>
          <p>Entra al panel para configurar pacientes, sesiones y alertas.</p>
        </div>
        <form className="stack-form" onSubmit={onSubmit}>
          {mode === 'signup' && (
            <label>
              Nombre completo
              <input value={fullName} onChange={(event) => setFullName(event.target.value)} required />
            </label>
          )}
          <label>
            Email
            <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </label>
          {mode !== 'recover' && (
            <label>
              Contrasena
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                minLength={8}
              />
            </label>
          )}
          {status && <p className="form-status">{status}</p>}
          <button className="button primary fill" type="submit" disabled={loading}>
            {loading ? 'Procesando...' : mode === 'signup' ? 'Crear cuenta' : mode === 'recover' ? 'Enviar correo' : 'Entrar'}
          </button>
        </form>
        <div className="auth-links">
          <button type="button" onClick={() => setMode('login')}>
            Iniciar sesion
          </button>
          <button type="button" onClick={() => setMode('signup')}>
            Crear cuenta
          </button>
          <button type="button" onClick={() => setMode('recover')}>
            Recuperar contrasena
          </button>
        </div>
      </section>
    </main>
  );
}

function CaregiverApp() {
  const { signOut, session } = useAuth();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  return (
    <div className="caregiver-shell">
      <aside className={`sidebar ${mobileNavOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          <span className="brand-mark">
            <HeartPulse size={20} />
          </span>
          <span>memorIA</span>
        </div>
        <nav>
          <NavLink to="/caregiver" end onClick={() => setMobileNavOpen(false)}>
            <Home size={18} />
            Inicio
          </NavLink>
        </nav>
        <button className="sidebar-exit" type="button" onClick={signOut}>
          <LogOut size={18} />
          Salir
        </button>
      </aside>
      <header className="caregiver-topbar">
        <button className="icon-button mobile-only" type="button" onClick={() => setMobileNavOpen((value) => !value)}>
          {mobileNavOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
        <div>
          <p>Panel cuidador</p>
          <strong>{session?.email || 'Cuidador'}</strong>
        </div>
      </header>
      <main className="caregiver-main">
        <Routes>
          <Route path="/" element={<CaregiverDashboard />} />
          <Route path="*" element={<Navigate to="/caregiver" replace />} />
        </Routes>
      </main>
    </div>
  );
}

function CaregiverDashboard() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [profileName, setProfileName] = useState('');
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('');

  const selectedPatient = patients.find((patient) => patient.id === selectedPatientId) || null;

  async function loadBaseData() {
    setLoading(true);
    try {
      const [profile, patientList, alertList] = await Promise.all([
        apiClient.getProfile().catch(() => null),
        apiClient.listPatients(),
        apiClient.listAlerts().catch(() => []),
      ]);
      setProfileName(profile?.fullName || '');
      setPatients(patientList);
      setAlerts(alertList);
      setSelectedPatientId((current) => current || patientList[0]?.id || '');
      setStatus('');
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'No se pudo cargar el panel.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadBaseData();
  }, []);

  return (
    <div className="dashboard-layout">
      <section className="dashboard-head">
        <div>
          <p className="eyebrow">Cuidador</p>
          <h1>{profileName ? `Hola, ${profileName}` : 'Panel de control'}</h1>
          <p>Gestiona perfiles, reglas y sesiones desde una vista responsive.</p>
        </div>
        <button className="button ghost" type="button" onClick={() => void loadBaseData()}>
          <RefreshCcw size={18} />
          Actualizar
        </button>
      </section>

      {status && <p className="notice error">{status}</p>}
      {loading ? (
        <p className="notice">Cargando panel...</p>
      ) : (
        <>
          <SummaryStrip patients={patients} alerts={alerts} selectedPatient={selectedPatient} />
          <div className="care-grid">
            <PatientPanel
              patients={patients}
              selectedPatientId={selectedPatientId}
              onSelectPatient={setSelectedPatientId}
              onPatientsChange={(nextPatients) => {
                setPatients(nextPatients);
                if (!nextPatients.some((patient) => patient.id === selectedPatientId)) {
                  setSelectedPatientId(nextPatients[0]?.id || '');
                }
              }}
            />
            <section className="work-column">
              {selectedPatient ? (
                <>
                  <AiConfigPanel patient={selectedPatient} />
                  <DevicesPanel patient={selectedPatient} />
                  <SessionsPanel patient={selectedPatient} />
                </>
              ) : (
                <EmptyState title="Crea un paciente" text="Despues podras configurar sesiones y reglas." />
              )}
            </section>
            <AlertsPanel alerts={alerts} onAlertsChange={setAlerts} />
          </div>
        </>
      )}
    </div>
  );
}

function SummaryStrip({
  patients,
  alerts,
  selectedPatient,
}: {
  patients: Patient[];
  alerts: Alert[];
  selectedPatient: Patient | null;
}) {
  const unreadAlerts = alerts.filter((alert) => !alert.readAt).length;
  return (
    <div className="summary-strip">
      <Metric icon={<UserRound />} label="Pacientes" value={patients.length.toString()} />
      <Metric icon={<Bell />} label="Alertas pendientes" value={unreadAlerts.toString()} />
      <Metric icon={<Activity />} label="Paciente activo" value={selectedPatient?.preferredName || selectedPatient?.fullName || '-'} />
    </div>
  );
}

function PatientPanel({
  patients,
  selectedPatientId,
  onSelectPatient,
  onPatientsChange,
}: {
  patients: Patient[];
  selectedPatientId: string;
  onSelectPatient: (patientId: string) => void;
  onPatientsChange: (patients: Patient[]) => void;
}) {
  const [form, setForm] = useState<PatientFormInput>(emptyPatientForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [status, setStatus] = useState('');

  async function savePatient(event: FormEvent) {
    event.preventDefault();
    setStatus('');
    try {
      const saved = editingId ? await apiClient.updatePatient(editingId, form) : await apiClient.createPatient(form);
      const nextPatients = editingId
        ? patients.map((patient) => (patient.id === saved.id ? saved : patient))
        : [saved, ...patients];
      onPatientsChange(nextPatients);
      onSelectPatient(saved.id);
      setForm(emptyPatientForm);
      setEditingId(null);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'No se pudo guardar el paciente.');
    }
  }

  async function deletePatient(patientId: string) {
    setStatus('');
    try {
      await apiClient.deletePatient(patientId);
      onPatientsChange(patients.filter((patient) => patient.id !== patientId));
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'No se pudo eliminar.');
    }
  }

  function editPatient(patient: Patient) {
    setEditingId(patient.id);
    setForm({
      fullName: patient.fullName,
      preferredName: patient.preferredName || '',
      birthYear: patient.birthYear || null,
      relationship: patient.relationship || '',
      notes: patient.notes || '',
    });
  }

  return (
    <section className="panel">
      <PanelTitle icon={<UserRound />} title="Pacientes" action={<Plus size={18} />} />
      <div className="patient-list">
        {patients.map((patient) => (
          <article
            className={`select-card ${patient.id === selectedPatientId ? 'selected' : ''}`}
            key={patient.id}
            onClick={() => onSelectPatient(patient.id)}
          >
            <div>
              <strong>{patient.preferredName || patient.fullName}</strong>
              <span>{patient.fullName}</span>
            </div>
            <button
              className="plain-icon"
              type="button"
              onClick={(event) => {
                event.stopPropagation();
                editPatient(patient);
              }}
            >
              <ChevronRight size={18} />
            </button>
          </article>
        ))}
      </div>
      <form className="stack-form compact-form" onSubmit={savePatient}>
        <input
          placeholder="Nombre completo"
          value={form.fullName}
          onChange={(event) => setForm({ ...form, fullName: event.target.value })}
          required
        />
        <input
          placeholder="Nombre preferido"
          value={form.preferredName}
          onChange={(event) => setForm({ ...form, preferredName: event.target.value })}
          required
        />
        <div className="form-row">
          <input
            placeholder="Ano nacimiento"
            type="number"
            value={form.birthYear || ''}
            onChange={(event) => setForm({ ...form, birthYear: Number(event.target.value) || null })}
          />
          <input
            placeholder="Relacion"
            value={form.relationship || ''}
            onChange={(event) => setForm({ ...form, relationship: event.target.value })}
          />
        </div>
        <textarea
          placeholder="Notas para la IA"
          value={form.notes || ''}
          onChange={(event) => setForm({ ...form, notes: event.target.value })}
        />
        {status && <p className="form-status">{status}</p>}
        <div className="button-row">
          <button className="button primary" type="submit">
            {editingId ? 'Actualizar' : 'Crear'}
          </button>
          {editingId && (
            <button
              className="button ghost"
              type="button"
              onClick={() => {
                setEditingId(null);
                setForm(emptyPatientForm);
              }}
            >
              Cancelar
            </button>
          )}
        </div>
      </form>
      {selectedPatientId && (
        <button className="danger-button" type="button" onClick={() => void deletePatient(selectedPatientId)}>
          <Trash2 size={16} />
          Eliminar seleccionado
        </button>
      )}
    </section>
  );
}

function AiConfigPanel({ patient }: { patient: Patient }) {
  const [loopRules, setLoopRules] = useState<LoopRule[]>([]);
  const [topics, setTopics] = useState<DangerousTopic[]>([]);
  const [memories, setMemories] = useState<SafeMemory[]>([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(false);

  async function loadConfig(options: { clearBeforeLoad?: boolean } = {}) {
    if (options.clearBeforeLoad) {
      setLoopRules([]);
      setTopics([]);
      setMemories([]);
      setStatus(`Cargando configuracion de ${patient.preferredName || patient.fullName}...`);
    }
    setLoading(true);
    try {
      const [rules, dangerousTopics, safeMemories] = await Promise.all([
        apiClient.listLoopRules(patient.id),
        apiClient.listDangerousTopics(patient.id),
        apiClient.listSafeMemories(patient.id),
      ]);
      setLoopRules(rules);
      setTopics(dangerousTopics);
      setMemories(safeMemories);
      setStatus('');
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'No se pudo cargar la configuracion.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    setLoopRules([]);
    setTopics([]);
    setMemories([]);
    setStatus(`Cargando configuracion de ${patient.preferredName || patient.fullName}...`);
    setLoading(true);

    Promise.all([
      apiClient.listLoopRules(patient.id),
      apiClient.listDangerousTopics(patient.id),
      apiClient.listSafeMemories(patient.id),
    ])
      .then(([rules, dangerousTopics, safeMemories]) => {
        if (cancelled) return;
        setLoopRules(rules);
        setTopics(dangerousTopics);
        setMemories(safeMemories);
        setStatus('');
      })
      .catch((error) => {
        if (cancelled) return;
        setStatus(error instanceof Error ? error.message : 'No se pudo cargar la configuracion.');
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [patient.id]);

  return (
    <section className="panel">
      <PanelTitle icon={<Brain />} title="Configuracion IA" />
      {status && <p className="form-status">{status}</p>}
      {loading && !status && <p className="form-status">Cargando configuracion...</p>}
      <ConfigList
        title="Bucles"
        items={loopRules}
        primaryKey="question"
        secondaryKey="answer"
        firstPlaceholder="Pregunta repetida"
        secondPlaceholder="Respuesta segura"
        onCreate={async (first, second) => {
          await apiClient.createLoopRule(patient.id, first, second);
          await loadConfig();
        }}
        onDelete={async (id) => {
          await apiClient.deleteLoopRule(patient.id, id);
          await loadConfig();
        }}
      />
      <ConfigList
        title="Temas delicados"
        items={topics}
        primaryKey="term"
        secondaryKey="redirectHint"
        firstPlaceholder="Termino"
        secondPlaceholder="Redireccion"
        onCreate={async (first, second) => {
          await apiClient.createDangerousTopic(patient.id, first, second);
          await loadConfig();
        }}
        onDelete={async (id) => {
          await apiClient.deleteDangerousTopic(patient.id, id);
          await loadConfig();
        }}
      />
      <ConfigList
        title="Recuerdos seguros"
        items={memories}
        primaryKey="title"
        secondaryKey="content"
        firstPlaceholder="Titulo"
        secondPlaceholder="Contenido"
        onCreate={async (first, second) => {
          await apiClient.createSafeMemory(patient.id, first, second);
          await loadConfig();
        }}
        onDelete={async (id) => {
          await apiClient.deleteSafeMemory(patient.id, id);
          await loadConfig();
        }}
      />
    </section>
  );
}

type ConfigItem = { id: string; active: boolean; [key: string]: unknown };

function ConfigList<T extends ConfigItem>({
  title,
  items,
  primaryKey,
  secondaryKey,
  firstPlaceholder,
  secondPlaceholder,
  onCreate,
  onDelete,
}: {
  title: string;
  items: T[];
  primaryKey: keyof T;
  secondaryKey: keyof T;
  firstPlaceholder: string;
  secondPlaceholder: string;
  onCreate: (first: string, second: string) => Promise<void>;
  onDelete: (id: string) => Promise<void>;
}) {
  const [first, setFirst] = useState('');
  const [second, setSecond] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    try {
      await onCreate(first.trim(), second.trim());
      setFirst('');
      setSecond('');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="config-block">
      <h3>{title}</h3>
      <form className="inline-form" onSubmit={submit}>
        <input value={first} onChange={(event) => setFirst(event.target.value)} placeholder={firstPlaceholder} required />
        <input value={second} onChange={(event) => setSecond(event.target.value)} placeholder={secondPlaceholder} required />
        <button className="button ghost compact" type="submit" disabled={busy}>
          Anadir
        </button>
      </form>
      <div className="mini-list">
        {items.length === 0 && <span className="muted-text">Sin elementos configurados.</span>}
        {items.map((item) => (
          <article key={item.id}>
            <div>
              <strong>{String(item[primaryKey] || '')}</strong>
              <span>{String(item[secondaryKey] || '')}</span>
            </div>
            <button className="plain-icon" type="button" onClick={() => void onDelete(item.id)}>
              <Trash2 size={16} />
            </button>
          </article>
        ))}
      </div>
    </div>
  );
}

function DevicesPanel({ patient }: { patient: Patient }) {
  const [devices, setDevices] = useState<PatientDevice[]>([]);
  const [pairingCode, setPairingCode] = useState('');

  async function loadDevices() {
    setDevices(await apiClient.listPatientDevices(patient.id));
  }

  useEffect(() => {
    void loadDevices();
    setPairingCode('');
  }, [patient.id]);

  return (
    <section className="panel">
      <PanelTitle icon={<LinkIcon />} title="Vinculacion" />
      <div className="button-row">
        <button
          className="button primary"
          type="button"
          onClick={async () => {
            const result = await apiClient.createPairingCode(patient.id);
            setPairingCode(result.code);
          }}
        >
          Generar codigo
        </button>
        {pairingCode && <strong className="pairing-code">{pairingCode}</strong>}
      </div>
      <div className="mini-list">
        {devices.length === 0 && <span className="muted-text">Sin dispositivos vinculados.</span>}
        {devices.map((device) => (
          <article key={device.id}>
            <div>
              <strong>{device.deviceName || 'Tablet paciente'}</strong>
              <span>{device.deviceIdentifier}</span>
            </div>
            <button
              className="plain-icon"
              type="button"
              onClick={async () => {
                await apiClient.unlinkPatientDevice(patient.id, device.id);
                await loadDevices();
              }}
            >
              <Trash2 size={16} />
            </button>
          </article>
        ))}
      </div>
    </section>
  );
}

function SessionsPanel({ patient }: { patient: Patient }) {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState('');
  const [transcript, setTranscript] = useState<ConversationMessage[]>([]);
  const [events, setEvents] = useState<SessionEvent[]>([]);

  const selectedSession = sessions.find((session) => session.id === selectedSessionId) || null;

  async function loadSessions() {
    const nextSessions = await apiClient.listSessions(patient.id);
    setSessions(nextSessions);
    setSelectedSessionId((current) => current || nextSessions[0]?.id || '');
  }

  async function loadSessionDetail(sessionId = selectedSessionId) {
    if (!sessionId) return;
    const [messages, sessionEvents] = await Promise.all([apiClient.listTranscript(sessionId), apiClient.listEvents(sessionId)]);
    setTranscript(messages);
    setEvents(sessionEvents);
  }

  useEffect(() => {
    setSelectedSessionId('');
    setTranscript([]);
    setEvents([]);
    void loadSessions();
  }, [patient.id]);

  useEffect(() => {
    void loadSessionDetail();
    const interval = window.setInterval(() => {
      void loadSessionDetail();
    }, 5000);
    return () => window.clearInterval(interval);
  }, [selectedSessionId]);

  async function createSession() {
    const session = await apiClient.createSession(patient.id);
    const nextSessions = [session, ...sessions];
    setSessions(nextSessions);
    setSelectedSessionId(session.id);
  }

  async function command(commandName: 'start' | 'pause' | 'resume' | 'end') {
    if (!selectedSession) return;
    const updated = await apiClient.sessionCommand(selectedSession.id, commandName);
    setSessions((current) => current.map((session) => (session.id === updated.id ? updated : session)));
    await loadSessionDetail(updated.id);
  }

  return (
    <section className="panel wide-panel">
      <PanelTitle icon={<Mic />} title="Terminal cuidador" />
      <div className="session-toolbar">
        <button className="button primary" type="button" onClick={() => void createSession()}>
          Crear sesion
        </button>
        <select value={selectedSessionId} onChange={(event) => setSelectedSessionId(event.target.value)}>
          <option value="">Selecciona una sesion</option>
          {sessions.map((session) => (
            <option key={session.id} value={session.id}>
              {sessionStatus(session.status)} - {formatDate(session.createdAt)}
            </option>
          ))}
        </select>
      </div>
      {selectedSession ? (
        <>
          <div className="session-actions">
            <StatusPill status={selectedSession.status} />
            <button className="button ghost compact" type="button" onClick={() => void command('start')}>
              <Play size={16} />
              Iniciar
            </button>
            <button className="button ghost compact" type="button" onClick={() => void command('pause')}>
              <Pause size={16} />
              Pausar
            </button>
            <button className="button ghost compact" type="button" onClick={() => void command('resume')}>
              Reanudar
            </button>
            <button className="button ghost compact" type="button" onClick={() => void command('end')}>
              Finalizar
            </button>
          </div>
          <div className="terminal-grid">
            <TranscriptPanel transcript={transcript} />
            <EventsPanel events={events} />
          </div>
        </>
      ) : (
        <EmptyState title="Sin sesion seleccionada" text="Crea una sesion para activar la terminal." />
      )}
    </section>
  );
}

function TranscriptPanel({ transcript }: { transcript: ConversationMessage[] }) {
  return (
    <div className="terminal-panel">
      <h3>Transcript</h3>
      <div className="timeline">
        {transcript.length === 0 && <span className="muted-text">Todavia no hay mensajes.</span>}
        {transcript.map((message, index) => (
          <article className={`message ${message.sender}`} key={`${message.createdAt || index}-${message.sender}`}>
            <span>{senderLabel(message.sender)}</span>
            <p>{message.content}</p>
          </article>
        ))}
      </div>
    </div>
  );
}

function EventsPanel({ events }: { events: SessionEvent[] }) {
  return (
    <div className="terminal-panel">
      <h3>Eventos</h3>
      <div className="timeline compact-timeline">
        {events.length === 0 && <span className="muted-text">Sin eventos registrados.</span>}
        {events.map((event, index) => (
          <article key={`${event.createdAt || index}-${event.eventType}`}>
            <strong>{event.description}</strong>
            <span>{formatDate(event.createdAt)}</span>
          </article>
        ))}
      </div>
    </div>
  );
}

function AlertsPanel({ alerts, onAlertsChange }: { alerts: Alert[]; onAlertsChange: (alerts: Alert[]) => void }) {
  async function markRead(alert: Alert) {
    if (!alert.id) return;
    const updated = await apiClient.markAlertRead(alert.id);
    onAlertsChange(alerts.map((item) => (item.id === updated.id ? updated : item)));
  }

  return (
    <section className="panel alerts-panel">
      <PanelTitle icon={<AlertTriangle />} title="Alertas" />
      <div className="mini-list">
        {alerts.length === 0 && <span className="muted-text">Sin alertas.</span>}
        {alerts.map((alert, index) => (
          <article className={alert.readAt ? 'read' : ''} key={alert.id || index}>
            <div>
              <strong>{alert.title}</strong>
              <span>{alert.message}</span>
            </div>
            {!alert.readAt && alert.id && (
              <button className="plain-icon" type="button" onClick={() => void markRead(alert)}>
                <Check size={16} />
              </button>
            )}
          </article>
        ))}
      </div>
    </section>
  );
}

function SectionHeader({ eyebrow, title, text }: { eyebrow: string; title: string; text: string }) {
  return (
    <div className="section-header">
      <p className="eyebrow">{eyebrow}</p>
      <h2>{title}</h2>
      <p>{text}</p>
    </div>
  );
}

function FeatureCard({ icon, title, text }: { icon: React.ReactNode; title: string; text: string }) {
  return (
    <article className="feature-card">
      <span>{icon}</span>
      <h3>{title}</h3>
      <p>{text}</p>
    </article>
  );
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <article className="metric-card">
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </article>
  );
}

function PanelTitle({ icon, title, action }: { icon: React.ReactNode; title: string; action?: React.ReactNode }) {
  return (
    <div className="panel-title">
      <span>{icon}</span>
      <h2>{title}</h2>
      {action && <span className="panel-action">{action}</span>}
    </div>
  );
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return (
    <div className="empty-state">
      <strong>{title}</strong>
      <p>{text}</p>
    </div>
  );
}

function StatusPill({ status }: { status: string }) {
  return <span className={`status-pill ${status}`}>{sessionStatus(status)}</span>;
}

const emptyPatientForm: PatientFormInput = {
  fullName: '',
  preferredName: '',
  birthYear: null,
  relationship: '',
  notes: '',
};

function sessionStatus(status: string) {
  const labels: Record<string, string> = {
    waiting: 'en espera',
    active: 'activa',
    paused: 'pausada',
    ended: 'terminada',
  };
  return labels[status] || status;
}

function senderLabel(sender: string) {
  const labels: Record<string, string> = {
    patient: 'Paciente',
    ai: 'IA',
    rule: 'Regla',
    cognitive: 'Estimulo',
  };
  return labels[sender] || sender;
}

function formatDate(value?: string | null) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('es-ES', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}
