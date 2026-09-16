# ProntoCheck - Mapa general de arquitectura

Este documento explica como esta organizada la app y por que conviene seguir el
patron MVVM en las pantallas nuevas o refactorizadas.

## Flujo general

```text
Usuario
  |
  v
Activity / XML / Adapter
  |
  v
ViewModel
  |
  v
UseCase, cuando hay reglas de negocio complejas
  |
  v
Repository
  |
  v
SupabaseApi / RetrofitClient
  |
  v
Supabase
```

## Responsabilidades por capa

### UI

Ruta principal: `app/src/main/java/com/example/prontocheck/ui`

La UI contiene `Activity`, `Adapter` y bindings XML. Su responsabilidad es
mostrar datos, escuchar clicks, pedir permisos de Android y navegar.

Ventaja de hacerlo asi: la pantalla no queda mezclada con llamadas de red ni
reglas de negocio, por lo que es mas facil corregir bugs visuales sin tocar la
logica.

### ViewModel

Los `ViewModel` mantienen estado observable con `LiveData` y ejecutan acciones
en `viewModelScope`.

Ventaja de hacerlo asi: el estado sobrevive mejor a rotaciones o recreaciones
de pantalla, y la logica se puede probar sin depender directamente de Android UI.

### Repository

Los `Repository` encapsulan el acceso a datos. En este proyecto normalmente
reciben `SupabaseApi` y exponen funciones mas claras para la app.

Ventaja de hacerlo asi: los filtros de Supabase, endpoints y detalles de red
quedan en un solo lugar. Si cambia la API, no hay que buscar llamadas Retrofit
regadas en Activities.

### Data

Ruta principal: `app/src/main/java/com/example/prontocheck/data`

Aqui viven los modelos, Retrofit y helpers tecnicos como ML. Esta capa debe
representar los datos y servicios externos.

### Domain

Ruta principal: `app/src/main/java/com/example/prontocheck/domain`

Aqui van los casos de uso cuando una regla de negocio ya no cabe de forma limpia
en un ViewModel. El ejemplo actual es `GetReporteUseCase`.

## Modulos principales

```text
com.example.prontocheck
  data
    di                 -> AppDependencies, contenedor manual de dependencias
    ml                 -> FaceNet / TensorFlow Lite
    model              -> Empleado, Incidencia, Producto, PuntoAcceso, etc.
    network            -> RetrofitClient y SupabaseApi
    repository         -> Repositorios compartidos
  domain
    usecase            -> Reglas de negocio reutilizables
  ui
    AltaEmpleado       -> Alta con camara y rostro
    AuthManager        -> BaseActivity y validacion de sesion
    cuadrante          -> Cuadrante mensual con ViewModel
    dashboard          -> Menu principal y resumen
    Incidencias        -> MVVM refactorizado
    gestionUbicaciones -> MVVM refactorizado
    login              -> Login con ViewModel
    productos          -> Inventario con ViewModel y Repository
    reporte            -> Reportes con ViewModel y UseCase
```

## Patron recomendado para cada pantalla

```text
MiPantallaActivity.kt
MiPantallaViewModel.kt
MiPantallaViewModelFactory.kt
MiPantallaRepository.kt
MiPantallaAdapter.kt, si usa RecyclerView
```

La Activity debe evitar:

- Llamar `RetrofitClient.instance` directamente.
- Construir filtros de Supabase como `eq.valor`.
- Tener `lifecycleScope.launch` para operaciones de negocio.
- Decidir reglas como estados, validaciones complejas o transformaciones de IDs.

El ViewModel debe:

- Validar entradas.
- Exponer `LiveData<Resource<T>>`.
- Usar `viewModelScope.launch`.
- Pedir datos al Repository.

El Repository debe:

- Llamar a `SupabaseApi`.
- Construir filtros de Supabase.
- Mantener detalles de red fuera de la UI.

## Refactor aplicado

Se refactorizaron:

- `di/AppDependencies`: centraliza la creacion de repositories para que las
  Activities no dependan directamente de `RetrofitClient`.
- `GestionPersonal`: paginacion y carga de empleados viven en
  `GestionPersonalViewModel` y `EmpleadoRepository`.
- `Cuadrante`: la carga por residencial vive en `CuadranteViewModel` y
  `CuadranteRepository`.
- `RelojActivity`: la pantalla conserva camara/GPS/FaceNet, pero puntos,
  empleados y registro de asistencia pasan por `RelojViewModel` y
  `RelojRepository`.
- `ui/Incidencias`: la Activity ya no registra, busca empleados ni actualiza
  estados directamente en Supabase. Ahora delega en `IncidenciasViewModel` y
  `IncidenciasRepository`.
- `ui/gestionUbicaciones`: la Activity conserva GPS, permisos y dialogos, pero
  cargar, guardar y eliminar puntos vive en `GestionUbicacionesViewModel` y
  `GestionUbicacionesRepository`.
- `Inventario`: se agrego una factory propia para eliminar la factory anonima y
  mantener consistencia MVVM.
- `EditarEmpleado`: actualizar y eliminar usan `EmpleadoRepository` por medio de
  `RegistroEmpleadoViewModel`.

## Pendientes sanos

Algunas piezas siguen viviendo en Activities porque son responsabilidades de UI o
Android framework:

- CameraX, permisos y ML Kit en `AltaEmpleadoActivity` y `RelojActivity`.
- Construccion visual de la tabla en `CuadranteActivity`.
- Exportacion de CSV en `ReporteActivity`.

Si la app crece mas, el siguiente paso natural seria migrar `AppDependencies` a
Hilt y mover helpers de camara/exportacion a clases pequenas reutilizables.
