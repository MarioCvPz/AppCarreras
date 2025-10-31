# AppCarreras

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-API_26%2B-3DDC84?logo=android&logoColor=white)
![Material](https://img.shields.io/badge/Material_Design-3-blue?logo=material-design&logoColor=white)
![Room](https://img.shields.io/badge/Room-2.6.1-FF6F00?logo=android&logoColor=white)
![Coroutines](https://img.shields.io/badge/Kotlin_Coroutines-lifecycleScope-0095D5?logo=kotlin&logoColor=white)
![ViewPager2](https://img.shields.io/badge/ViewPager2-Tabs-blueviolet)
![ViewBinding](https://img.shields.io/badge/ViewBinding-On-success)

Aplicación Android escrita en Kotlin para gestionar campeonatos de automovilismo, sus carreras asociadas y los vehículos participantes. AppCarreras centraliza el ciclo de vida de un torneo: alta de campeonatos, planificación de carreras, control de coches, registro de incidencias en pista y exportación de reportes.

## Tabla de contenidos
- [Resumen](#resumen)
- [Arquitectura y fundamentos técnicos](#arquitectura-y-fundamentos-técnicos)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Características clave](#características-clave)
- [Instalación y compilación](#instalación-y-compilación)
- [Capturas de pantalla](#capturas-de-pantalla)
- [Contribución](#contribución)
- [Licencia](#licencia)
- [Autor](#autor)

## Resumen
AppCarreras es una herramienta de gestión interna para escuderías o clubes que organizan torneos. Permite administrar torneos, registrar coches con distintos estados operativos, programar carreras, anotar incidencias con penalizaciones y exportar la información en formato CSV para análisis externo.

## Arquitectura y fundamentos técnicos
La app sigue una arquitectura en capas simple organizada por paquetes funcionales:

| Capa | Ubicación | Descripción |
|------|-----------|-------------|
| **Datos** | `data/entity`, `data/dao`, `data/database` | Modelo persistente definido con entidades Room (`TorneoEntity`, `CarreraEntity`, `CocheEntity`, `IncidenciaEntity`), DAO especializados y `AppDatabase` con `RoomDatabase`. `DatabaseProvider` centraliza la inicialización con `fallbackToDestructiveMigration`. |
| **Dominio / Lógica** | Integrada en actividades y fragmentos | Operaciones orquestadas con corrutinas (`lifecycleScope` + `Dispatchers.IO`) para mantener la UI reactiva y evitar bloqueos. Validaciones de negocio como unicidad de dorsales, conteo de coches por torneo y composición de informes CSV. |
| **Interfaz** | `ui/*` | Actividades (`MainActivity`, `TorneoDetailActivity`, `RaceDetailActivity`, etc.), fragmentos (`CarsFragment`, `RacesFragment`, `RaceCarsFragment`, `RaceIncidentsFragment`) y adaptadores RecyclerView para listar campeonatos, carreras, coches e incidencias. Se emplean `ViewPager2` y `TabLayoutMediator` para navegación tabulada. |

### Dependencias principales
- **AndroidX & Material**: Compatibilidad moderna (`appcompat`, `core-ktx`, `activity`) y componentes visuales basados en Material Design.
- **Room 2.6.1**: Persistencia local con DAO, relaciones y migraciones destructivas automáticas para desarrollo.
- **Kotlin Coroutines**: Uso de `lifecycleScope`, `Dispatchers.IO` y `withContext` para operaciones asíncronas y acceso a base de datos sin bloquear la UI.
- **ViewBinding**: Generación de enlaces tipados para vistas (`ActivityMainBinding`, `FragmentCarsBinding`, etc.).
- **ViewPager2 + TabLayout**: Navegación por pestañas entre coches/carreras e incidencias.
- **Storage Access Framework** (`DocumentFile`, `OpenDocumentTree`): Selección y persistencia de directorios para exportar archivos CSV.
- **Apache POI (poi-ooxml)**: Incluido para futuras exportaciones avanzadas en formato Excel OOXML.

## Estructura del proyecto
```text
AppCarreras/
├── app/
│   ├── build.gradle.kts        # Configuración del módulo Android (SDK 26-36, dependencias)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/appcarreras/
│       │   ├── data/
│       │   │   ├── dao/         # DAO de Room para torneos, carreras, coches e incidencias
│       │   │   ├── entity/      # Entidades Room con claves foráneas y relaciones
│       │   │   └── database/    # Definición de AppDatabase y proveedor singleton
│       │   └── ui/
│       │       ├── main/        # Pantalla principal con listado de campeonatos
│       │       ├── torneo/      # Detalle de torneo, ViewPager y modelo de campeonatos
│       │       ├── cars/        # Gestión y alta de coches, estados por color
│       │       ├── races/       # Programación y listado de carreras
│       │       ├── racedetail/  # Tabs de detalle de carrera, incidencias, exportación CSV
│       │       └── incidents/   # Alta de incidencias con penalizaciones
│       └── res/                 # Layouts, temas Material, strings, drawables
├── gradle/                      # Catálogo de versiones y wrapper
├── build.gradle.kts             # Plugins a nivel de proyecto
└── settings.gradle.kts          # Declaración de módulos
```

## Características clave
- **Gestión de torneos**: Alta, búsqueda y listado de campeonatos con conteo dinámico de coches asociados.
- **Administración de coches**: Registro de vehículos por torneo o carrera, control visual del estado (verde/amarillo/rojo) y validación de dorsales únicos.
- **Planificación de carreras**: Creación de carreras con selector de fecha y navegación rápida desde la ficha del torneo.
- **Control de incidencias**: Registro de incidentes por dorsal con hora automática, tipo de acción y vueltas de penalización.
- **Detalle de carrera unificado**: Tabs para visualizar coches inscritos e incidencias en una misma pantalla con acciones rápidas desde menú flotante.
- **Exportación a CSV**: Generación de reportes de incidencias con selección de directorio persistente mediante el Storage Access Framework.
- **Experiencia Material Design**: Uso de `AppBar`, `FloatingActionButton`, `TextInputLayout`, diálogos personalizados y soporte `enableEdgeToEdge`.

## Instalación y compilación
1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/<tu-usuario>/AppCarreras.git
   cd AppCarreras
   ```
2. **Abrir en Android Studio** (Hedgehog o superior).
    - `File > Open...` y selecciona la carpeta raíz del proyecto.
    - Espera a que Gradle sincronice las dependencias.
3. **Configurar un dispositivo**
    - Dispositivo físico con Android 8.0 (API 26) o superior, o un emulador configurado en el AVD Manager.
4. **Compilar y ejecutar**
    - Usa el botón _Run_ ▶️ para lanzar la aplicación en el dispositivo seleccionado.

## Capturas de pantalla
> Sustituye los placeholders con tus imágenes reales en `docs/screenshots/`.

| Pantalla | Vista |
|----------|-------|
| Inicio | ![Inicio](docs/screenshots/home.png) |
| Detalle de torneo | ![Detalle de torneo](docs/screenshots/torneo.png) |
| Detalle de carrera | ![Detalle de carrera](docs/screenshots/race.png) |

## Contribución
1. Haz un fork del repositorio.
2. Crea una rama para tu feature o corrección: `git checkout -b feature/nueva-funcionalidad`.
3. Asegúrate de que las pruebas y verificaciones relevantes se ejecutan correctamente.
4. Envía un Pull Request describiendo los cambios propuestos.

## Licencia
Este proyecto está disponible bajo la licencia MIT. Consulta el archivo `LICENSE` para más información.

## Autor
Desarrollado por **AppCarreras Team**. Para consultas o soporte, abre un issue en el repositorio o contacta a `tu.email@dominio.com`.