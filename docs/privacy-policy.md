# Politica de privacidad base

Ultima actualizacion: 2026-04-23

TorresAgro ayuda a productores y equipos de campo a registrar parcelas, labores, inventario, monitoreos, clima y reportes agronomicos.

## Datos que puede tratar la app

- Datos de cuenta: identificador de usuario autenticado con Google mediante Firebase Authentication.
- Datos de operacion agricola: parcelas, cultivos, variedades, fechas de siembra, tareas, actividades, inventario, observaciones, costos y notas.
- Ubicacion: coordenadas de parcelas y ubicacion actual cuando el usuario decide capturarla o centrar el mapa.
- Fotos: imagenes agregadas por el usuario en actividades o monitoreos. En la version actual se conservan como referencias locales salvo que se habilite Storage remoto.
- Datos tecnicos: clima, humedad estimada, recomendaciones y alertas generadas a partir de datos locales y servicios externos.

## Finalidad

Los datos se usan para:

- organizar el trabajo agricola;
- sincronizar informacion entre sesiones/dispositivos del mismo usuario;
- generar alertas y recomendaciones de apoyo a decisiones;
- producir reportes de parcela;
- mejorar continuidad offline/local-first.

## Servicios de terceros

La app puede usar:

- Firebase Authentication y Cloud Firestore para cuenta y sincronizacion;
- Firebase Storage si se habilita subida de fotos;
- servicios de clima/agricultura configurados por clave API;
- mapas OpenStreetMap/OSMDroid y fuentes de tiles configuradas en la app.

## Permisos

- Ubicacion: capturar coordenadas de parcelas y centrar el mapa en la ubicacion actual.
- Camara: adjuntar evidencia fotografica a labores o monitoreos.
- Notificaciones: recordatorios locales de tareas.
- Internet: sincronizacion, clima y mapas.

## Conservacion y eliminacion

Los datos locales permanecen en el dispositivo hasta que el usuario los elimine o desinstale la app. Los datos sincronizados en Firebase deben eliminarse desde la cuenta/proyecto correspondiente cuando el usuario solicite eliminacion o cierre definitivo.

## Seguridad

El acceso cloud debe estar protegido por reglas de Firebase que limiten cada ruta a su `uid`. El repo incluye reglas base en `firestore.rules` y `storage.rules`.

## Contacto

Responsable: Shoropio Corporation

Correo de soporte: pendiente de definir antes de publicar en Google Play.
