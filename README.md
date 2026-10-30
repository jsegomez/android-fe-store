# Store

App Android con Jetpack Compose que muestra una Pokédex (listado y detalle) usando PokéAPI. Las dependencias se inyectan con Koin. En el detalle se puede marcar un Pokémon como favorito.

## Módulos de Koin

Están en la carpeta `di/`, uno por capa:

- `networkModule`: Json, OkHttp, Retrofit y `PokeApi`.
- `repositoryModule`: los repositorios (Pokédex, productos y favoritos).
- `useCaseModule`: los casos de uso de favoritos.
- `viewModelModule`: los ViewModels.

Todos se cargan en `StoreApplication`.

## Quién depende de quién

```
PokedexViewModel
  └─ PokedexRepository ─ PokeApi ─ Retrofit ─ OkHttp, Json, URL base

PokemonDetailViewModel
  ├─ PokedexRepository
  ├─ ObserveIsFavoriteUseCase ─ FavoriteRepository
  └─ ToggleFavoriteUseCase ───── FavoriteRepository
```

## Ciclo de vida

- `single` para Retrofit, OkHttp, Json y los repositorios. Se crean una vez y se comparten. En los repositorios en memoria es obligatorio, porque guardan datos: con `factory` cada pantalla tendría su propia lista.
- `factory` para los casos de uso. No guardan nada y son baratos de crear.
- `viewModel` para los ViewModels, así Koin los ata a la pantalla y sobreviven a una rotación.

## Qualifier

Usé `named("pokeBaseUrl")` para la URL base de Retrofit. Un `String` a secas es ambiguo en Koin, y si más adelante otra API (como la de auth) necesita su propia URL, no sabría cuál entregar. Hoy solo hay una, pero así queda listo.

## Favoritos

`FavoriteRepository` guarda los ids en memoria. El ViewModel del detalle no lo usa directo, usa los dos casos de uso: uno para saber si el Pokémon es favorito y otro para marcarlo o desmarcarlo.

Sobre qué se comparte y cuánto dura: se comparte una sola instancia del repositorio entre todas las pantallas, y dura mientras el proceso de la app esté vivo. Si se cierra la app, se pierden los favoritos.

Los tests están en `FavoriteUseCasesTest` y se corren con:

```
./gradlew testDebugUnitTest
```
