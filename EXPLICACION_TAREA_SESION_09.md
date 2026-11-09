# Tarea Sesión 9: favoritos disponibles sin conexión

Pokédex con PokéAPI, Room y Koin. Esta tarea agrega favoritos que se marcan desde el detalle, un filtro
"Solo favoritos" en la lista, y los conserva tras reiniciar la app y tras refrescar los datos.

Para llegar ahí el proyecto primero se volvió offline-first (la tarea parte de una base con Room y el
proyecto no la tenía): lista, fichas y estadísticas se guardan en Room (v1), la ficha registra cuándo se
descargó (v2) y los favoritos se guardan en su propia tabla (v3).

## Cómo está organizado

Todo el Pokédex vive en `features/pokedex`, dividido en tres capas:

| Capa | Qué contiene |
|---|---|
| `data/local` | Room: entidades, DAO, base de datos y migraciones |
| `data` y `data/mapper` | Repositorios y conversión entre API, base de datos y modelos de la app |
| `domain` | Contratos (`PokedexRepository`, `FavoriteRepository`), modelos y casos de uso |
| `presentation` | ViewModels y pantallas de Compose |

**La pantalla nunca lee directamente de la red: lee siempre de Room.** Hay dos recorridos separados:

```
LEER:        Room -> DAO (Flow) -> repositorio -> ViewModel -> pantalla
ACTUALIZAR:  ViewModel -> repositorio -> PokéAPI -> mapper -> Room -> (Room avisa y la pantalla se actualiza sola)
```

Así, si la red falla, la pantalla conserva lo guardado y solo muestra un aviso con "Reintentar".

## Esquema de la base de datos (`pokedex.db`)

| Versión | Cambio | Migración |
|---|---|---|
| v1 | Tablas `pokemon` (lista), `pokemon_details` (ficha) y `pokemon_stats` (estadísticas, relación 1 a N) | — |
| v2 | Columna `lastUpdated` en `pokemon_details` (0 = fecha desconocida) | `MIGRATION_1_2` |
| v3 | Tabla `favorites` (solo `pokemonId`) | `MIGRATION_2_3` |

Los JSON de cada versión están en `app/schemas/` y las pruebas de migración los usan.
Las migraciones están en `PokedexMigrations.kt`; la lista `ALL_MIGRATIONS` es la que usan la app y las pruebas.

## Cómo funcionan los favoritos

- **Marcar y quitar:** el corazón del detalle llama a `FavoriteDao.toggle`, que consulta y escribe dentro de
  una sola transacción. El corazón se actualiza solo porque observa la tabla.
- **Filtro:** el chip "Solo favoritos" no vuelve a descargar nada. Solo cambia qué se lee de Room: la
  consulta `observeFavoritePokemon` une `favorites` con `pokemon` y devuelve resúmenes en el mismo formato que
  la lista. Si no hay favoritos se muestra un estado propio ("Aún no tienes favoritos"), distinto de "cargando".
- **Solo local:** los favoritos no se envían a ningún servidor. PokéAPI solo ofrece lectura (GET).

## Decisión de diseño: `favorites` sin clave foránea

La caché se puede reconstruir descargándola otra vez; los favoritos no, porque los creó el usuario.
Si `favorites` tuviera una clave foránea hacia `pokemon` con `ON DELETE CASCADE`, borrar o reemplazar un
resumen de la caché eliminaría también el favorito. Por eso la tabla es independiente.

- **Comportamiento adoptado:** ninguna operación sobre la caché puede borrar un favorito. El refresh usa
  `Upsert` (nunca borra filas) y, aunque se borrara toda la caché, los favoritos seguirían guardados.
- **Costo:** se pierde la garantía de que el Pokémon exista en `pokemon`. Un favorito sin resumen en la caché
  no aparece en el filtro hasta que ese resumen vuelva a descargarse (sigue marcado). Es poco probable, porque
  solo se puede marcar desde el detalle y al guardar una ficha también se guarda su resumen.

## Otras decisiones

- **Tipos y habilidades como JSON** dentro de `pokemon_details`: son listas pequeñas que siempre se leen
  completas. Las estadísticas sí van en tabla propia, con una columna `position` porque Room no garantiza el
  orden de las filas relacionadas.
- **Guardado "todo o nada":** `PokemonDetailDao.saveDetail` guarda resumen, ficha y estadísticas en una
  transacción; si algo falla se conserva la ficha anterior.
- **Migraciones y datos:** nunca se usa `fallbackToDestructiveMigration()`, porque borraría fichas y favoritos.

## Cómo se cumple cada criterio

| Criterio | Dónde está | Cómo se comprueba |
|---|---|---|
| Agregar y quitar favoritos (3) | `FavoriteDao.toggle`, botón del detalle | `FavoriteDaoTest.toggle_marksThePokemon_andTogglingAgainUnmarksIt`, `FavoriteUseCasesTest` |
| Filtro "Solo favoritos" (2) | `PokedexViewModel` y `PokedexScreen` | `PokedexViewModelTest.onlyFavorites_*` (incluye que no llama a la API) y `FavoriteDaoTest` |
| Conservación al reiniciar (2) | Base en archivo `pokedex.db` | `FavoritePersistenceTest.favorites_surviveClosingAndReopeningTheDatabase` y prueba manual en el dispositivo |
| Conservación ante refresh (2) | Tabla separada y `Upsert` | `FavoriteDaoTest.favorites_surviveARefreshOfTheList`, `..._surviveARefreshOfTheDetail`, `..._surviveWipingTheWholeCache` y `FavoritePersistenceTest.favorites_surviveARefreshAfterReopeningTheDatabase` |
| Migración sin pérdida (1) | `MIGRATION_2_3` | `PokedexMigrationTest.migrate2To3_keepsEverythingSaved_andAddsAnEmptyFavoritesTable` y `migrate1To3_runsBothMigrationsInOrder` |

La prueba de migración parte de una base v2 con ficha, estadísticas y una fecha `lastUpdated` real,
la migra con `MigrationTestHelper`, valida el esquema contra `3.json` y comprueba que todo sigue igual.
También se verificó con una actualización real: se instaló la v2, se descargaron fichas y se instaló la
v3 encima, sin desinstalar. Las fichas, sus estadísticas y su `lastUpdated` se conservaron.

La prueba de persistencia usa una base en archivo (no en memoria): escribe, cierra la base y abre otra
instancia sobre el mismo archivo. Cerrar la base no es lo mismo que matar el proceso del sistema; eso se
comprueba a mano en el video (paso 4).

## Cómo ejecutar las pruebas

```
./gradlew testDebugUnitTest            # pruebas de JVM
./gradlew connectedDebugAndroidTest    # pruebas con Room real; requiere un emulador o dispositivo
```

La primera vez que se sube la versión del esquema, la prueba de migración puede fallar porque el JSON nuevo
aún no estaba en los assets de prueba; al volver a ejecutarla pasa.

## Recorrido del video (hasta 2 minutos)

1. Abrir el Pokédex con internet y entrar al detalle de dos Pokémon, marcando cada uno con el corazón.
2. Activar "Solo favoritos": aparecen solo esos dos.
3. Activar el modo avión.
4. Forzar la detención de la app desde Ajustes y volver a abrirla (sin borrar datos). Con el filtro activo
   siguen los dos favoritos, y sin el filtro se ve la lista guardada con el aviso "Sin conexión".
5. Restaurar la red y tocar "Reintentar": el refresh termina y los favoritos siguen marcados.

## Límites conocidos

- El filtro no se recuerda: al reabrir la app empieza apagado (los favoritos sí persisten).
- Las imágenes sin conexión dependen de la caché de Coil; solo se garantiza el contenido guardado en Room.
- Si la escritura de un favorito fallara (por ejemplo, disco lleno), no se muestra un mensaje de error.
