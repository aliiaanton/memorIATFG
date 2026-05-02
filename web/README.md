# memorIA Web

Panel web responsive para cuidadores y pagina publica de memorIA.

## Requisitos

- Node.js 20.19 o superior.
- Backend Spring Boot accesible desde la URL configurada.
- Supabase Auth configurado si se usa login real.

## Configuracion

Copia `.env.example` a `.env` y ajusta:

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SUPABASE_URL=
VITE_SUPABASE_ANON_KEY=
VITE_APK_DOWNLOAD_URL=/downloads/memoria.apk
```

## Ejecucion

```bash
npm install
npm run dev
```

URL local por defecto:

```text
http://localhost:5173
```

## Build

```bash
npm run build
```

La salida de produccion queda en `dist/`.
