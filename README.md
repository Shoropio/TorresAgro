# TorresAgro

TorresAgro es una app Android nativa para gestion agricola en campo, pensada para pequenos y medianos productores que necesitan registrar parcelas, actividades, monitoreo, clima, inventario y tareas desde el telefono, con enfoque local-first y soporte parcial offline.

## Resumen

- Plataforma: Android
- Stack: Kotlin, Jetpack Compose, MVVM, Room, WorkManager, Navigation Compose
- Estado actual: compila en `debug` y `release`
- Version actual: `1.0.0`
- Cultivos incluidos en catalogo base: yuca, camote, name, maiz, ayote, pepino, sandia, melon y platano

## Caracteristicas principales

### Experiencia de app

- UI/UX renovada con Material 3
- Navegacion inferior para modulos principales
- Dashboard con metricas rapidas, alertas, clima y recomendaciones
- Estados vacios, tarjetas de accion y formularios unificados
- Flujo optimizado para captura rapida desde campo

### Inicio y dashboard

- Resumen de parcelas activas
- Resumen de alertas detectadas
- Resumen de tareas abiertas
- Clima por ubicacion actual o por parcela
- Recomendaciones agronomicas destacadas
- Accesos rapidos a mapa, nueva parcela y nueva actividad
- Pull to refresh para actualizar datos visibles

### Parcelas

- Crear parcela
- Editar parcela
- Eliminar parcela
- Registrar:
  - nombre
  - ubicacion
  - tamano
  - cultivo
  - variedad
  - fecha de siembra
  - coordenadas GPS
  - poligono de parcela
- Calculo automatico de fecha esperada de cosecha
- Vista de detalle por parcela
- Vista previa del poligono en mapa

### Mapa agricola

- Visualizacion de parcelas sobre mapa
- Coloreado de parcelas por vigor estimado
- Apertura de detalle desde el mapa
- Vista de leyenda NDVI

### Actividades de campo

- Crear actividad
- Editar actividad
- Eliminar actividad
- Tipos soportados:
  - siembra
  - riego
  - fertilizacion
  - fumigacion
  - deshierbe
  - cosecha
  - mano de obra
- Registro de:
  - fecha
  - costo
  - cantidad
  - observaciones
  - foto desde camara
  - foto desde galeria
- Visualizacion de historial de actividades por parcela

### Tareas y calendario agricola

- Crear tarea
- Editar tarea
- Eliminar tarea
- Marcar tarea como completada
- Definir:
  - parcela
  - titulo
  - fecha
  - tipo de tarea
  - prioridad
  - recordatorio local
  - estado
- Programacion de recordatorios con WorkManager
- Cancelacion automatica de recordatorios al completar o eliminar

### Monitoreo agronomico

- Crear monitoreo
- Editar monitoreo
- Eliminar monitoreo
- Registrar:
  - parcela
  - fecha
  - etapa del cultivo
  - estado general
  - sintomas observados
  - recomendacion
  - foto de evidencia
- Generacion automatica de recomendacion base segun sintomas y estado
- Historial de observaciones por parcela

### Clima y datos agronomicos

- Consulta de clima por coordenadas
- Consulta de clima por parcela
- Pronostico extendido visible en detalle de parcela
- Humedad de suelo desde Open-Meteo Agriculture
- Historico basico de precipitacion y temperatura
- Espacio preparado para prediccion de plagas

### Alertas y recomendaciones

- Centro de alertas
- Alertas por severidad
- Recomendaciones mostradas como tarjetas accionables
- Enlace rapido desde alerta hacia contexto de parcela

### Inventario

- Registro y edicion de insumos
- Indicadores de stock
- Alerta visual de bajo inventario
- Vista resumida de unidades registradas y items activos

### Reportes

- Generacion de reporte PDF por parcela
- Resumen de cultivo, ubicacion y tamano
- Indicadores agronomicos disponibles
- Historico simple de los ultimos dias
- Compartir PDF desde Android

### Offline y persistencia

- Base de datos local con Room
- Datos semilla iniciales
- Cola local de sincronizacion
- Enfoque local-first
- Persistencia de entidades principales
- Soporte para reintento de sincronizacion en trabajos futuros

### Firebase e integracion

- Firebase Auth
- Firestore
- Firebase Storage preparado para fotos si se habilita en el proyecto
- Bootstrap preparado desde `local.properties`

## Modulos funcionales

- `Home`
- `Parcels`
- `Parcel Detail`
- `Agri Map`
- `Tasks`
- `Inventory`
- `Intelligence`
- `Alerts`
- `Settings`
- Formularios de:
  - parcela
  - actividad
  - tarea
  - monitoreo

## Arquitectura

- Kotlin
- Jetpack Compose
- MVVM
- Room
- WorkManager
- Navigation Compose
- Repositorios para acceso a datos
- Servicios separados para clima, agronomia, reportes y sincronizacion

## Estructura principal

- `app/src/main/java/com/torresagro/app/MainActivity.kt`
- `app/src/main/java/com/torresagro/app/ui/TorresAgroApp.kt`
- `app/src/main/java/com/torresagro/app/ui/screen/Screens.kt`
- `app/src/main/java/com/torresagro/app/ui/screen/FormsScreens.kt`
- `app/src/main/java/com/torresagro/app/ui/viewmodel/AppViewModel.kt`
- `app/src/main/java/com/torresagro/app/data/local/`
- `app/src/main/java/com/torresagro/app/data/repository/`
- `app/src/main/java/com/torresagro/app/data/weather/`
- `app/src/main/java/com/torresagro/app/data/agri/`
- `app/src/main/java/com/torresagro/app/data/report/`
- `app/src/main/java/com/torresagro/app/domain/model/`

## Base de datos local

Tablas actuales:

1. `parcels`
2. `crop_tasks`
3. `activity_records`
4. `crop_observations`
5. `inventory_items`
6. `harvest_records`
7. `sync_queue`
8. `weather_cache`
9. `agri_data_cache`

## Requisitos

- Android Studio con soporte Kotlin/Compose
- JDK 17
- Android SDK 35
- Dispositivo o emulador Android
- `local.properties` con SDK path y, si aplica, claves Firebase / clima

## Configuracion local

La app lee parametros sensibles desde `local.properties`.

Valores contemplados:

- `sdk.dir`
- `firebase.apiKey`
- `firebase.appId`
- `firebase.projectId`
- `firebase.storageBucket`
- `firebase.gcmSenderId`
- `firebase.webClientId`
- `openWeatherApiKey`
- `visualCrossingApiKey`
- `release.keystoreFile`
- `release.keystorePassword`
- `release.keyAlias`
- `release.keyPassword`

## Comandos de build

### Debug

```powershell
.\gradlew.bat assembleDebug
```

Salida esperada:

- `app/build/outputs/apk/debug/app-debug.apk`

### Release APK

```powershell
.\gradlew.bat assembleRelease
```

Salida esperada:

- `app/build/outputs/apk/release/`

### Release Bundle

```powershell
.\gradlew.bat bundleRelease
```

Salida esperada:

- `app/build/outputs/bundle/release/`

## Releases

- el proyecto ya cuenta con una configuracion de firma de produccion automatizada en Gradle
- se ha generado un keystore inicial (`release-keystore.jks`) y se han configurado los secretos en `local.properties`
- la generacion de `bundleRelease` produce ahora un artifact firmado y listo para subir a Play Console

### Checklist real para produccion

- [x] configurar keystore de release
- [x] cablear `signingConfig` en `app/build.gradle.kts`
- [ ] validar `applicationId`, nombre final y branding
- [x] generar AAB firmado
- [x] configurar ofuscación y reducción de código (R8/ProGuard)
- [ ] correr QA de campo en varios dispositivos
- [ ] publicar politica de privacidad
- [ ] preparar ficha de Play Console
- [ ] subir capturas, icono final y textos de tienda
- [ ] desplegar reglas Firebase incluidas en el repo
- [ ] configurar SHA de Play App Signing en Firebase

## Documentacion adicional

- `docs/arquitectura-mvp.md`
- `docs/firebase-setup.md`
- `docs/production-readiness.md`
- `docs/privacy-policy.md`
- `docs/play-console-checklist.md`

## Estado actual del producto

TorresAgro ya funciona como base operativa local para captura de informacion en campo y seguimiento de parcelas, con una capa visual mucho mas madura y flujos de formulario mas consistentes.

El siguiente salto natural ya no es de estructura interna, sino de operacion real:

- firma release
- QA de campo
- endurecimiento legal y de privacidad
- publicacion de version firmada
