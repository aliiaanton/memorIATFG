export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  supabaseUrl: import.meta.env.VITE_SUPABASE_URL || '',
  supabaseAnonKey: import.meta.env.VITE_SUPABASE_ANON_KEY || '',
  apkDownloadUrl: import.meta.env.VITE_APK_DOWNLOAD_URL || '/downloads/memoria.apk',
};

export const pricingPlans = [
  {
    name: 'Basico',
    price: '9,99 EUR/mes',
    description: 'Para una familia que quiere probar memorIA en casa.',
    features: ['1 paciente', 'Modo paciente por voz', 'Diario de sesiones', 'Alertas basicas'],
  },
  {
    name: 'Familiar',
    price: '19,99 EUR/mes',
    description: 'Para hogares con mas seguimiento y configuracion.',
    features: ['Hasta 3 pacientes', 'Reglas conversacionales', 'Recuerdos seguros', 'Vinculacion de tablets'],
    featured: true,
  },
  {
    name: 'Profesional',
    price: 'Consultar',
    description: 'Para centros o cuidadores con varios pacientes.',
    features: ['Pacientes ampliados', 'Soporte de despliegue', 'Informes de actividad', 'Configuracion asistida'],
  },
];
