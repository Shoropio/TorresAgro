# Setup de Firebase

## Estado actual

La app usa Firebase para autenticacion y sincronizacion cloud por usuario.

- Firebase Authentication con Google Sign-In.
- Cloud Firestore para datos de usuario.
- Firebase Storage reservado para fotos, aunque la app actualmente conserva fotos como URI local para evitar fallos en planes sin Storage habilitado.
- Bootstrap desde `local.properties` y `google-services.json`.

## Configuracion requerida

Agregar estas claves en `local.properties`:

```properties
firebase.apiKey=TU_API_KEY
firebase.appId=TU_APP_ID
firebase.projectId=TU_PROJECT_ID
firebase.storageBucket=TU_STORAGE_BUCKET
firebase.gcmSenderId=TU_GCM_SENDER_ID
firebase.webClientId=TU_WEB_CLIENT_ID
```

Servicios a habilitar:

- Authentication: proveedor Google.
- Cloud Firestore.
- Firebase Storage si se habilita subida remota de fotos.

## SHA para Google Sign-In

En Firebase Console, agregar el SHA-1 y SHA-256 del certificado usado para release. Si se usa Play App Signing, tambien agregar los certificados que Play Console muestre para firma de app.

Estado actual:

- El `SHA-1` del keystore local de release ya fue agregado al Android app de Firebase.
- Sigue pendiente agregar el certificado de `Play App Signing` cuando aparezca en Play Console despues de configurar el track.

Comando local:

```powershell
keytool -list -v -keystore release-keystore.jks -alias torresagro-release
```

## Rutas de datos

Firestore:

- `users/{uid}/parcels`
- `users/{uid}/tasks`
- `users/{uid}/activities`
- `users/{uid}/observations`
- `users/{uid}/inventory`

Storage reservado:

- `activities/{uid}/...`
- `observations/{uid}/...`

## Reglas

El repo incluye reglas iniciales listas para desplegar:

- `firestore.rules`
- `storage.rules`
- `firebase.json`

Despliegue:

```powershell
firebase deploy --only firestore:rules,storage
```

Estado actual:

- `firestore.rules` ya fue desplegado correctamente.
- `storage.rules` sigue pendiente porque Firebase Storage aun no esta inicializado en el proyecto `torresagro`.

## Archivos principales

- `app/src/main/java/com/torresagro/app/data/firebase/FirebaseBootstrap.kt`
- `app/src/main/java/com/torresagro/app/data/firebase/FirebaseAuthManager.kt`
- `app/src/main/java/com/torresagro/app/data/firebase/FirebaseSyncGateway.kt`
