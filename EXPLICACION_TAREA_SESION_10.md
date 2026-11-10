# Tarea Sesión 10: actualización en segundo plano y revisión de seguridad

La Pokédex ahora puede actualizar su lista de PokéAPI en segundo plano con **WorkManager**, y el proyecto
tiene una revisión de seguridad con un checklist y dos mejoras aplicadas. Lo que se probó con pruebas
automáticas, lo que falta probar a mano y los límites están al final.

## Cómo está organizado

```
Pantalla (SyncPanel) ─ PokeSyncViewModel ─ PokemonSyncScheduler (contrato, dominio)
                                          └─ SyncMetadataStore   (contrato, dominio)

WorkManagerPokemonSyncScheduler ─ WorkManager ─ PokeWorkerFactory ─ PokeSyncWorker
                                                                     ├─ RefreshPokemonListUseCase ─ PokedexRepository ─ PokeApi ─ Room
                                                                     └─ SyncMetadataStore (DataStore: fecha del último éxito)
```

| Capa | Archivo | Responsabilidad |
|---|---|---|
| Dominio | `RefreshPokemonListUseCase` | Actualiza la lista. Lo comparten el ViewModel (refresco manual) y el Worker |
| Dominio | `PokemonSyncScheduler`, `SyncState` | Contrato para pedir, cancelar y observar el trabajo |
| Dominio | `SyncMetadataStore` | Contrato para guardar/leer la fecha del último éxito |
| Datos | `PokeSyncWorker` | Ejecuta la actualización y decide éxito, reintento o fallo |
| Datos | `PokeWorkerFactory` | Construye el Worker con sus dependencias (Koin) |
| Datos | `WorkManagerPokemonSyncScheduler` | Configura la solicitud y traduce `WorkInfo` a `SyncState` |
| Datos | `DataStoreSyncMetadataStore` | Guarda la fecha con DataStore Preferences |
| Presentación | `PokeSyncViewModel`, `SyncPanel` | Muestran estado y fecha; botones "Actualizar" y "Cancelar" |

**La lista sigue saliendo siempre de Room.** El Worker solo escribe en Room; la pantalla se actualiza sola
porque observa la tabla. WorkManager no entrega datos a la pantalla.

## Cómo funciona la actualización

- **Trabajo único:** `enqueueUniqueWork("poke-cache-sync", KEEP, ...)`. Varias pulsaciones dejan un solo
  trabajo mientras haya uno pendiente o en ejecución. Al terminar, una solicitud nueva puede empezar.
- **Conectividad:** restricción `NetworkType.CONNECTED`. Sin red el trabajo queda en espera
  (`ENQUEUED`) y la caché anterior sigue disponible.
- **Backoff:** exponencial, desde 30 s (30, 60, 120...).
- **Límite de intentos:** `runAttemptCount < 3` permite los intentos 0, 1, 2 y 3 = **4 en total**.
- **Qué se reintenta** (`AppError.isRetryable()`):

| Resultado del repositorio | Decisión |
|---|---|
| Éxito | Guarda la fecha y `success` |
| `NoConnection`, `Timeout` | `retry` (hasta el límite) |
| `Http` 429 o 500–599 | `retry` (hasta el límite) |
| `Http` 4xx, `Parsing`, `Unknown` | `failure`, sin reintento |
| Cancelación | Se propaga a WorkManager; no se convierte en éxito ni en fallo |

- **Idempotencia:** Room usa `@Upsert` por id. Repetir la actualización reemplaza filas, no las duplica,
  y nunca borra lo que ya estaba guardado.
- **Estado visible:** `SyncPanel` muestra el estado (sin trabajo, en espera, en ejecución, completada,
  falló, cancelada) y la fecha del último éxito. El estado no vive en el ViewModel: se reconstruye desde
  WorkManager y DataStore, por eso se recupera tras rotar o recrear la app.
- **Inicialización:** `StoreApplication` implementa `Configuration.Provider` para dar a WorkManager la
  fábrica de Koin, y el manifest quita **solo** `WorkManagerInitializer` (los demás inicializadores se
  conservan).

## Criterios de la tarea

| Criterio | Dónde está | Cómo se comprueba |
|---|---|---|
| Trabajo único con CONNECTED, backoff y límite (40%) | `WorkManagerPokemonSyncScheduler`, `PokeSyncWorker` | `WorkManagerSchedulerTest` (un solo trabajo, `CONNECTED`, espera sin red, cancelar), `PokeSyncWorkerTest` (retry, límite en el intento 3, error permanente, cancelación), `RetryPolicyTest` |
| Caché durable, estado visible, recuperación (20%) | Room + `SyncPanel` + DataStore | `PokeSyncViewModelTest` y prueba manual de recreación (ver abajo) |
| Checklist y dos mejoras de seguridad verificadas (25%) | Secciones siguientes | Evidencia en cada fila |
| Pruebas reproducibles y límites (15%) | Secciones "Cómo ejecutar las pruebas" y "Límites conocidos" | — |

## Revisión de seguridad (basada en OWASP MASVS)

Es una revisión de clase sobre una muestra de controles. **No certifica la aplicación.**

### Inventario de datos

| Dato | Dónde | Sensibilidad | Retención / borrado |
|---|---|---|---|
| Lista y fichas de Pokémon | Room (`pokedex.db`) | Pública | Se reemplaza con cada refresh; se borra al desinstalar o borrar datos |
| Favoritos | Room (tabla `favorites`) | Preferencia del usuario, no sensible | Hasta que el usuario los quite, desinstale o borre datos |
| Fecha del último éxito | DataStore (`sync_metadata`) | Pública | Igual que lo anterior |
| Credenciales / tokens | **No existen** | — | La feature `auth` aún es un esqueleto vacío (`AuthApi`, `AuthRepositoryImpl` sin código) |

### Mejoras aplicadas

**Mejora 1: bloquear HTTP sin cifrar (MASVS-NETWORK)**

| | |
|---|---|
| Riesgo | Si alguna URL pasara a `http://`, los datos viajarían sin cifrar y podrían leerse o alterarse en la red |
| Corrección | `res/xml/network_security_config.xml` con `cleartextTrafficPermitted="false"`, referenciado en el manifest. No añade certificados propios ni acepta ninguno extra |
| Evidencia | La configuración está en el manifest. La URL base de PokéAPI es `https://pokeapi.co/api/v2/` |
| Prueba posterior | **Pendiente de ejecutar a mano:** cambiar temporalmente la URL base a `http://` y comprobar que la petición falla (error de cleartext en Logcat); volver a `https://` |

**Mejora 2: R8 activado en release (MASVS-CODE)**

| | |
|---|---|
| Riesgo | Un release sin optimizar conserva código sin uso y nombres legibles, lo que facilita la ingeniería inversa. R8 solo dificulta la lectura casual: **no protege secretos** |
| Corrección | `optimization { enable = true }` en el `buildType` release (DSL de AGP 9.x) |
| Evidencia | `./gradlew :app:assembleRelease` compila con `minifyReleaseWithR8` y genera `mapping.txt` en `app/build/outputs/mapping/release/`. El release firmado con la clave de debug se instaló en un emulador y arrancó hasta la pantalla de login sin errores en Logcat |
| Prueba posterior | **Pendiente de ejecutar a mano:** con el release instalado, iniciar sesión, abrir la Pokédex y pulsar "Actualizar en segundo plano" para comprobar que Retrofit, la serialización y el Worker funcionan ofuscados |

### Checklist

| Área | Estado | Justificación y evidencia |
|---|---|---|
| Datos | Cumple | Inventario arriba: solo datos públicos y preferencias no sensibles |
| Persistencia | Cumple | La lista se lee de Room y se conserva sin red (`PokedexRepositoryImplTest`: un refresh fallido no borra lo guardado). El refresh usa `Upsert` |
| Borrado | Pendiente | No hay botón para limpiar la caché; el usuario puede hacerlo desde Ajustes del sistema. Los datos son públicos |
| Backups | Pendiente | `allowBackup="true"` con las reglas de plantilla vacías (`backup_rules.xml`, `data_extraction_rules.xml`): Android respalda Room y DataStore. Al ser datos no sensibles no es grave, pero falta decidir explícitamente qué respaldar |
| Red (TLS) | Cumple | HTTPS en PokéAPI; mejora 1 bloquea HTTP; no se desactiva la validación de certificados ni se instala un `TrustManager` propio |
| Pinning | No aplica | Es una API pública ajena; el pinning tiene coste operativo (rotación de claves) y no se justifica aquí |
| Sesión (401/403, logout) | No aplica | No hay sesión ni tokens: PokéAPI es pública y `auth` está sin implementar |
| Permisos | Cumple | Solo `INTERNET`, que no requiere diálogo en tiempo de ejecución |
| Componentes exportados | Cumple | Solo `MainActivity` (`exported="true"`, necesario por el launcher). El proveedor de AndroidX Startup es `exported="false"` |
| Entradas (deep links) | No aplica | La app no declara deep links ni App Links |
| Logs | Cumple | El logging `BODY` de OkHttp solo se activa en `BuildConfig.DEBUG`; el release no registra cuerpos. Los únicos `println` imprimen textos fijos de botones |
| Screenshots / clipboard | No aplica | No hay pantallas con datos sensibles; bloquear capturas del catálogo público solo perjudicaría el uso |
| Secretos en el APK | Cumple | No hay claves de API ni secretos en el código. PokéAPI no requiere clave |
| Release (R8) | Parcial | Mejora 2 aplicada y compila; falta la prueba manual del flujo completo ofuscado |
| Background (unicidad, idempotencia, cancelación, retry) | Cumple | Ver pruebas automáticas arriba |

## Cómo ejecutar las pruebas

```
./gradlew testDebugUnitTest            # JVM: RetryPolicyTest y PokeSyncViewModelTest, además de las anteriores
./gradlew connectedDebugAndroidTest    # emulador o dispositivo: PokeSyncWorkerTest y WorkManagerSchedulerTest, además de las anteriores
```

Solo las pruebas de esta sesión:

```
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.jsegomez.store.features.pokedex.data.sync
```

Estado de la última ejecución: 8 pruebas de JVM nuevas y 12 instrumentadas nuevas, todas en verde.
Al terminar, `connectedDebugAndroidTest` desinstala la app del emulador, así que habrá que iniciar sesión de nuevo.

## Recorrido del video

Grabar con el emulador o un dispositivo, instalando la app y entrando a la Pokédex:

1. **Sin red:** activar el modo avión y pulsar "Actualizar en segundo plano". El estado queda "en espera"
   y la lista guardada sigue visible. Desactivar el modo avión: pasa a "en ejecución" y luego "completada",
   con la fecha actualizada.
2. **Pulsaciones repetidas:** con el modo avión, pulsar varias veces. El botón queda deshabilitado y en el
   Background Task Inspector (Android Studio) hay un solo trabajo `poke-cache-sync`.
3. **Retry:** con el modo avión activo, el inspector muestra que el trabajo espera y reintenta con
   backoff. Hoy no hay un botón de simulación de fallo (el material lo menciona como opcional en debug).
4. **Recreación:** con el trabajo en espera, rotar la pantalla y/o ejecutar
   `adb shell am kill com.jsegomez.store` y volver a abrir. El panel reconstruye el estado y la fecha.
5. **Cancelar:** con el trabajo en espera, pulsar "Cancelar": el estado pasa a "cancelada", no a éxito.

## Límites conocidos

- **Caché y fecha son almacenes distintos** (Room y DataStore). Si el proceso muere entre las dos
  escrituras, puede quedar catálogo nuevo con fecha anterior. Repetir la actualización lo corrige.
- **`CONNECTED` no garantiza acceso a PokéAPI.** Por eso el Worker clasifica los errores y reintenta.
- **No se reintentan los fallos TLS:** `safeApiCall` los convierte en `Unknown`, que se trata como permanente.
- **Las pruebas no reemplazan las pruebas reales:** `TestListenableWorkerBuilder` y el WorkManager de
  prueba verifican la lógica, no cómo Android programa el trabajo con red real, batería o process death.
  Eso se comprueba con el recorrido del video.
- **Solo se actualiza la primera página** (20 Pokémon, `PAGE_SIZE`), igual que el refresco manual.
- **Bloquear HTTP afecta a Productos:** el formulario de registro de producto acepta imágenes que
  empiecen con `http://`; con el bloqueo activo esas imágenes no cargarán. Las de la Pokédex y los datos de
  ejemplo usan HTTPS.
- **El release se probó solo hasta el login.** El flujo completo ofuscado queda como prueba manual pendiente.
- **Revisión de seguridad parcial:** cubre una muestra de controles de MASVS. Backups y borrado de la caché
  quedan pendientes.
