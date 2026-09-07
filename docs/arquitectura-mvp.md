# Arquitectura MVP de Torres Agro

## Objetivo del MVP

Entregar una app Android simple, clara y util para pequenos y medianos productores, con foco en:

- planificacion de labores
- registro por parcela
- control basico de insumos y costos
- consulta parcial sin internet

## Capas

### UI

- Jetpack Compose
- Navegacion inferior para Inicio, Parcelas, Tareas, Inventario y Reportes
- Pantallas futuras ya definidas: Splash, Acceso rapido, Registro de actividad, Configuracion

### Presentacion

- MVVM
- `AppViewModel` central para el MVP
- `StateFlow<AppUiState>` como fuente unica de estado

### Datos

- Room como base local principal
- `sync_queue` para cambios pendientes cuando no haya internet
- `SyncGateway` como contrato para Firebase o backend REST

## Modelo de datos

### Parcela

- id
- nombre
- ubicacion
- area
- cultivo
- variedad
- fecha de siembra
- fecha estimada de cosecha
- latitud y longitud

### Tarea agricola

- id
- parcela
- tipo de tarea
- titulo
- fecha objetivo
- prioridad
- recordatorio
- estado

### Registro de actividad

- id
- parcela
- tipo
- fecha
- costo
- cantidad
- observaciones
- foto

### Observacion de monitoreo

- id
- parcela
- fecha
- etapa
- estado general
- sintomas
- recomendacion general

### Inventario

- id
- nombre
- categoria
- existencia
- unidad
- minimo

### Cosecha / rentabilidad

- parcela
- cultivo
- kilos cosechados
- costo acumulado
- ingreso estimado
- utilidad

## Navegacion recomendada

1. `SplashScreen`
2. `QuickAccessScreen`
3. `HomeScreen`
4. `ParcelsScreen`
5. `ParcelDetailScreen`
6. `TasksScreen`
7. `ActivityFormScreen`
8. `InventoryScreen`
9. `ReportsScreen`
10. `SettingsScreen`

## Estrategia offline

- guardar todo primero en Room
- marcar cambios pendientes en `sync_queue`
- sincronizar con `WorkManager` al detectar conectividad
- mostrar al usuario cuales registros siguen pendientes

## Version avanzada

- multiples perfiles de usuario
- roles asesor/productor
- sincronizacion selectiva
- clima por coordenadas reales
- mapas de parcelas
- exportacion PDF/Excel
- panel de recomendaciones por cultivo y sintomas frecuentes
