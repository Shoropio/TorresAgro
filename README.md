# Torres Agro

Aplicacion Android para pequenos y medianos productores de yuca, camote, name, maiz y otros cultivos, pensada para trabajo de campo, uso simple y soporte parcial offline.

## Estado actual

La app ya compila y genera APK debug.

APK actual:

- `app/build/outputs/apk/debug/app-debug.apk`

## Implementado

### Inicio y panel principal

- Resumen de cultivos activos
- Resumen de tareas pendientes
- Alertas rapidas del calendario
- Clima mostrado en el panel
- Consulta de clima real por ubicacion de parcela
- Fallback local cuando no hay internet

### Parcelas

- Crear parcela
- Editar parcela
- Eliminar parcela
- Guardar nombre, ubicacion, tamano, cultivo, variedad y fecha de siembra
- Captura de coordenadas GPS del dispositivo
- Calculo automatico de fecha estimada de cosecha
- Vista de detalle por parcela

### Actividades

- Crear actividad
- Editar actividad
- Eliminar actividad
- Tipos incluidos: siembra, riego, fertilizacion, fumigacion, deshierbe, cosecha y mano de obra
- Registro de fecha, costo, cantidad y observaciones
- Foto desde camara
- Foto desde galeria
- Visualizacion de foto en historial de parcela

### Calendario agricola y tareas

- Crear tarea
- Editar tarea
- Eliminar tarea
- Marcar tarea como realizada
- Tipo de tarea, prioridad y fecha
- Recordatorio local por tarea
- Programacion de recordatorios con WorkManager
- Cancelacion automatica de recordatorios al completar o borrar tareas

### Monitoreo por visita

- Crear monitoreo
- Editar monitoreo
- Eliminar monitoreo
- Registrar etapa del cultivo y estado general
- Registrar sintomas observados
- Generar recomendacion basica segun sintomas y estado
- Adjuntar foto de monitoreo
- Visualizar monitoreo en detalle de parcela

### Inventario

- Vista de inventario e insumos
- Alerta visual de bajo stock

### Reportes

- Vista simple de produccion y rentabilidad
- Comparacion basica por cultivo/parcela

### Offline / datos locales

- Base local con Room
- Datos semilla iniciales
- Cola de sincronizacion local (`sync_queue`)
- Firebase Auth anonimo real
- Sincronizacion real con Firestore
- Subida real de fotos a Firebase Storage

## Arquitectura

- Kotlin
- Jetpack Compose
- MVVM
- Room
- WorkManager
- Navegacion con Navigation Compose

Estructura principal:

- `app/src/main/java/com/torresagro/app/ui/`
- `app/src/main/java/com/torresagro/app/ui/viewmodel/`
- `app/src/main/java/com/torresagro/app/data/local/`
- `app/src/main/java/com/torresagro/app/data/repository/`
- `app/src/main/java/com/torresagro/app/data/weather/`
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

## Flujo de pantallas

1. Splash
2. Acceso rapido
3. Inicio
4. Parcelas
5. Detalle de parcela
6. Nueva/editar parcela
7. Nueva/editar actividad
8. Nueva/editar tarea
9. Nuevo/editar monitoreo
10. Inventario
11. Reportes

## Cultivos y ejemplos incluidos

- Yuca variedad `Valencia`
- Camote variedad `Beauregard`
- Name variedad `Diamantes`
- Maiz variedad `ICTA amarillo`

## Recomendaciones agronomicas generales

- Yuca: usar estacas sanas, buen drenaje y control temprano de malezas.
- Camote: mantener humedad uniforme y evitar encharcamiento.
- Name: vigilar hojas amarillas, drenaje y sanidad del material de siembra.
- Maiz: dividir fertilizacion por etapa y revisar malezas al inicio.

Estas recomendaciones son generales y no sustituyen acompanamiento tecnico local.

## Archivos clave

- `app/src/main/java/com/torresagro/app/MainActivity.kt`
- `app/src/main/java/com/torresagro/app/ui/TorresAgroApp.kt`
- `app/src/main/java/com/torresagro/app/ui/screen/FormsScreens.kt`
- `app/src/main/java/com/torresagro/app/ui/screen/Screens.kt`
- `app/src/main/java/com/torresagro/app/ui/viewmodel/AppViewModel.kt`
- `app/src/main/java/com/torresagro/app/data/local/entity/Entities.kt`
- `app/src/main/java/com/torresagro/app/data/repository/RoomAgroRepository.kt`
- `app/src/main/java/com/torresagro/app/data/firebase/FirebaseSyncGateway.kt`
- `app/src/main/java/com/torresagro/app/data/weather/WeatherService.kt`
- `app/src/main/java/com/torresagro/app/data/local/util/TaskReminderScheduler.kt`
- `docs/arquitectura-mvp.md`
- `docs/firebase-setup.md`
- `docs/production-readiness.md`

## Build local

- `gradlew.bat assembleDebug`

Salida esperada:

- `app/build/outputs/apk/debug/app-debug.apk`

## Pendiente para produccion completa

Lo que falta ya no es tanto estructura de app local, sino cierre de producto y operacion:

- Autenticacion opcional de usuario
- Mapa por parcela
- Exportacion y respaldo en nube
- Politica de privacidad y manejo formal de permisos
- Pruebas de campo y QA en varios dispositivos
- Firma release
- Configuracion final para Play Store
