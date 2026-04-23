# Produccion y hardening

## Cerrado en el repo

- Target Android 15 (`targetSdk = 35`, `compileSdk = 35`).
- AAB release firmado generado localmente.
- R8/ProGuard activo en release con reglas base.
- `usesCleartextTraffic=false`.
- Permiso legacy `WRITE_EXTERNAL_STORAGE` eliminado.
- Backup y device-transfer restringidos para base local, preferencias y archivos sensibles.
- Room sin migracion destructiva.
- Schema Room exportado en `app/schemas`.
- Migraciones `1..7 -> 8` agregadas para conservar datos y asignar `userId` cuando falte.
- Firebase Analytics retirado hasta hacer upgrade completo de Kotlin/dependencias.
- Reglas base de Firestore/Storage incluidas en el repo.
- Documentos base de privacidad y checklist Play Console agregados.

## Validaciones locales recientes

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:compileReleaseKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintVitalRelease
.\gradlew.bat :app:bundleRelease
```

Artefacto:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Pendiente externo

- Subir AAB a Internal Testing en Play Console.
- Configurar Play App Signing.
- Agregar SHA-1/SHA-256 de Play App Signing en Firebase.
- Desplegar `firestore.rules` y `storage.rules`.
- Publicar politica de privacidad en una URL HTTPS.
- Completar Data Safety de Google Play.
- Ejecutar QA de campo en dispositivos reales.
- Rotar claves si `local.properties` o `google-services.json` fueron compartidos fuera del equipo.

## Pendiente tecnico recomendado

- Agregar Crashlytics cuando el stack Kotlin/Firebase este alineado.
- Ampliar pruebas unitarias del motor agronomico y sincronizacion.
- Agregar tests de migracion Room con `MigrationTestHelper`.
- Implementar eliminacion/exportacion de datos de usuario.
- Decidir estrategia final de subida remota de fotos.
