# Checklist Play Console

## Antes de subir el AAB

- Confirmar `applicationId`: `com.torresagro.app`.
- Confirmar nombre publico: TorresAgro.
- Subir `app/build/outputs/bundle/release/app-release.aab` a Internal Testing.
- Activar Play App Signing y copiar SHA-1/SHA-256 de Play Console a Firebase.
- Agregar tambien SHA local de release si se prueba instalando fuera de Play.
- Descargar nuevo `google-services.json` si Firebase cambia por nuevos certificados.

## Ficha de tienda

- Descripcion corta.
- Descripcion completa.
- Icono final 512x512.
- Feature graphic.
- Capturas de telefono.
- Categoria: productividad/empresa/agricultura, segun disponibilidad de Play.
- Email de soporte.
- Politica de privacidad publica en URL HTTPS.

## Data Safety

Declarar, segun configuracion final:

- Ubicacion aproximada/precisa: usada para funcionalidad de parcelas y mapas.
- Fotos: si se adjuntan evidencias.
- Identificador de usuario: Firebase Auth.
- Datos introducidos por el usuario: parcelas, tareas, inventario, actividades, observaciones.
- Datos se transmiten cifrados por HTTPS/Firebase.
- Eliminacion de datos: definir canal operativo antes de publicar publicamente.

## Prueba interna

Validar en al menos:

- Android 8/9, 10/11, 13, 14/15.
- Telefono con Google Play Services actualizado.
- Telefono con ubicacion desactivada.
- Sin internet, internet intermitente y Wi-Fi estable.
- Login, cerrar sesion, volver a iniciar.
- Crear/editar/eliminar parcela, tarea, actividad, monitoreo e inventario.
- Mapear parcela y centrar ubicacion actual repetidamente.
- Generar y compartir PDF.

## Criterio para pasar a produccion publica

- Sin crashes en internal testing.
- Reglas Firebase desplegadas.
- Politica de privacidad publicada.
- SHA de Play App Signing configurado en Firebase.
- QA de sincronizacion completado con al menos dos cuentas.
