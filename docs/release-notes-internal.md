# Release interno 1.0.0

Fecha de preparacion: 2026-04-23

## Artefacto

```text
app/build/outputs/bundle/release/app-release.aab
```

## Version

- `applicationId`: `com.torresagro.app`
- `versionCode`: `1`
- `versionName`: `1.0.0`
- `targetSdk`: `35`
- `minSdk`: `26`

## Certificado local de release

Agregar estas huellas en Firebase si se prueba con el AAB/APK firmado localmente.

- SHA-1: `2B:A9:8E:4C:F8:46:6B:62:B0:0F:DC:21:91:0A:06:E6:E5:2C:4C:B6`
- SHA-256: `EC:A8:7A:B1:91:6D:7A:F9:A1:17:06:80:0C:66:7D:C6:F2:AB:7B:6D:75:71:BB:A3:5F:DC:90:83:DA:3A:95:2F`

Si se habilita Play App Signing, Firebase tambien debe recibir el SHA-1/SHA-256 del certificado que muestra Play Console.

## Validaciones locales

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileReleaseKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintVitalRelease
.\gradlew.bat :app:bundleRelease
```

## Notas para testers

- Validar login con Google despues de configurar SHA en Firebase.
- Crear datos con una cuenta, cerrar sesion, iniciar de nuevo y confirmar sincronizacion.
- Probar mapa con ubicacion encendida, apagada y permisos denegados.
- Probar crear/editar/eliminar parcela, tarea, actividad, monitoreo e inventario.
- Probar generacion y compartir de PDF.
- Probar sin internet y con internet intermitente.
