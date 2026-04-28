# Android

Aplicacion Android nativa del proyecto memorIA.

Tecnologia prevista:

- Kotlin.
- Jetpack Compose.
- Material 3.
- Supabase Auth.
- SpeechRecognizer.
- TextToSpeech.

Modos de la aplicacion:

- Modo cuidador.
- Modo paciente.

Responsabilidades:

- Login y navegacion principal.
- Panel del cuidador con CRUD, alertas, diario y controles de sesion.
- Terminal del paciente con vinculacion por codigo, estados grandes, voz y texto minimo.

## Ejecucion local

Abrir la carpeta `android/` desde Android Studio.

Tambien se puede compilar desde terminal usando el Gradle descargado en la maquina:

```powershell
& "C:\Users\USUARIO\.gradle\wrapper\dists\gradle-9.1.0-bin\9agqghryom9wkf8r80qlhnts3\gradle-9.1.0\bin\gradle.bat" :app:assembleDebug
```

APK generado:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

