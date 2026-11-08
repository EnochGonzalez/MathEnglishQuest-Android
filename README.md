# Math & English Quest

**Versión 26.1.1** · Aplicación Android educativa para niños que combina **matemáticas** y **vocabulario en inglés**,
con un panel para el maestro. Desarrollada en **Kotlin**, **Jetpack Compose** y arquitectura **MVVM**.

> Proyecto integrador de *Desarrollo de Aplicaciones Móviles* — Universidad Politécnica del Estado de Chiapas.

## Propósito

Que un estudiante practique operaciones matemáticas y palabras en inglés de forma divertida, y que su maestro pueda ver
**evidencia de su trabajo**: en el módulo del Dado, el estudiante demuestra el procedimiento con una foto, y el maestro la
consulta desde su clase.

## Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Registro e inicio de sesión** | Roles *estudiante*, *maestro* e *invitado*. Cuentas con Firebase Authentication. El estudiante se une a una clase con un **código** o escaneando su **QR**. |
| **Dado** | Genera ejercicios con jerarquía de operaciones. Modos Directo, Normal y Rush; cuatro dificultades que se desbloquean por rachas; vidas, comodín y progreso guardado. Desde cierta dificultad pide la **foto del procedimiento**. |
| **Cómo se resuelve** | Muestra paso a paso cada operación como en el cuaderno: sumas y restas en columna con acarreos, multiplicación y **división en forma de casita**. Incluye decimales y negativos, y una calculadora para probar otras operaciones. |
| **Portafolio** | El estudiante ve sus evidencias agrupadas por día, con filtro por dificultad. |
| **Mi Clase** (maestro) | Código de clase y su **QR**, lista de estudiantes con búsqueda y orden, y el portafolio de cada uno en solo lectura. |
| **Diccionario** | Busca una palabra en inglés en una **API externa**, la traduce al español en el celular, reproduce su pronunciación y guarda el historial para verlo sin internet. |
| **Números** y **Frases** | Práctica de números y frases en inglés con voz; en Frases se puede responder hablando. |
| **Tema y sonido** | Tema claro y oscuro, y una pantalla de inicio con sonido original. |

## Integraciones del dispositivo

1. **Cámara y galería** — foto de evidencia del procedimiento (`TakePicture`, `PickVisualMedia`, `FileProvider`). Se comprime antes de guardarla.
2. **Audio** — sonido de inicio (`MediaPlayer`), voz en inglés (`TextToSpeech`), reconocimiento de voz y pronunciación descargada del diccionario.
3. **QR** — el maestro genera el QR de su clase (**ZXing**) y el estudiante lo escanea al registrarse (**Google Code Scanner**, que no necesita permiso de cámara).

La app solo declara el permiso `INTERNET`.

## API y almacenamiento

**API externa:** [Free Dictionary API](https://dictionaryapi.dev) (`https://api.dictionaryapi.dev/api/v2/entries/en/{palabra}`),
consumida con **Retrofit** + Gson. Maneja carga, palabra inexistente (404), sin conexión, error del servidor y reintento.
La **traducción al español** se hace dentro del celular con **ML Kit Translate** (la primera vez baja el idioma, unos 30 MB).

**Almacenamiento:**

| Qué | Dónde |
|---|---|
| Cuentas, clases, progreso y evidencias | **Firebase**: Authentication, Firestore y Storage (ver [`docs/firestore.md`](docs/firestore.md)) |
| Copia local de usuarios y evidencias | **Room** (para que la app funcione sin internet) |
| Sesión, progreso del Dado, historial y traducciones del diccionario | **SharedPreferences** |
| Fotos de evidencia y audios de pronunciación | Archivos en la carpeta privada de la app |

## Arquitectura MVVM

```
Pantalla (Compose)  ──eventos──▶  ViewModel  ──▶  Repository  ──▶  Fuentes de datos
      ▲                               │                              (Retrofit, Room, Firebase,
      └──────── StateFlow<UiState> ◀──┘                               SharedPreferences, archivos)
```

Las pantallas solo dibujan un `UiState` y envían eventos; **no** hacen peticiones HTTP, consultas SQL ni acceden a archivos.
Los ViewModels coordinan con corrutinas y `StateFlow`. Los repositorios deciden de dónde sale cada dato y lo convierten
a modelos propios. Las dependencias se entregan desde un contenedor central (`AppContainer`) y una fábrica de ViewModels
(`AppViewModelFactory`), sin crear servicios dentro de las pantallas.

```
app/src/main/java/com/upchiapas/mathenglishquest/
├── ui/           screens/ (una carpeta por módulo: Screen, ViewModel, UiState), components/, navigation/, theme/
├── domain/       reglas puras sin Android: dado, howto (procedimientos), dictionary, clase, portfolio
├── data/         local/ (Room, SharedPreferences), remote/ (Firebase, Retrofit, ML Kit), repository/, model/, security/
├── integration/  qr/ (generar y escanear QR)
├── di/           AppContainer (provisión de dependencias)
└── utils/        sonido, voz, compresión de imágenes
```

**Ejemplo del flujo completo (Diccionario):**
`DictionaryScreen` → `DictionaryViewModel` (`DictionaryUiState`) → `DictionaryRepository` →
`RetrofitDictionaryRemote` / `DictionaryApi` (red), `DictionaryPrefsStore` (historial), `PronunciationFileCache` (audio),
`MlKitTextTranslator` (traducción).

## Cómo ejecutar

**Requisitos:** Android Studio compatible con AGP 9.3, Android SDK 37, dispositivo o emulador con **Android 7.0 (API 24) o superior**
y **Google Play Services**, e internet.

1. Clona el repositorio y cambia a la rama de código: `git switch develop`.
2. Agrega `app/google-services.json` (no se versiona). Se descarga desde la consola de Firebase:
   *Configuración del proyecto → Tus apps → Android → `google-services.json`*.
3. Abre el proyecto en Android Studio, haz **Sync** y pulsa **Run**.
4. Crea una cuenta de **maestro**; se te mostrará el **código y el QR de la clase**. Luego registra un **estudiante** con ese código o escaneando el QR.

**APK instalable:** en la sección [Releases](../../releases) o en `app/build/outputs/apk/debug/app-debug.apk` (`./gradlew assembleDebug`).

Antes de usar Firebase, publica las reglas de `firebase/firestore.rules` y `firebase/storage.rules` en la consola.
Más detalles en [`docs/EMPEZAR.md`](docs/EMPEZAR.md).

## Pruebas

Más de 160 pruebas unitarias (lógica del Dado, procedimientos de "Cómo se resuelve", repositorios, mapeo de la API, contenido del QR):

```
./gradlew testDebugUnitTest
```

## Evidencias

Capturas de los recorridos principales y de las integraciones: carpeta [`docs/evidencias/`](docs/evidencias/LEEME.md).

## Ramas y versiones

Se sigue la *Política de gestión de repositorios y control de versiones en GitHub* del curso:

- `main`: solo la estructura inicial (cascarón), sin código funcional.
- `develop`: código funcional integrado. De aquí salen las versiones.
- `feature/*`: cada cambio, que entra a `develop` mediante Pull Request.

Las versiones usan el formato `AA.M.m` (año, cambio mayor, cambio menor), por ejemplo `26.1.1`, y se publican como *releases* de GitHub.

## Equipo

| Integrante | Contribución |
|---|---|
| **Santos Enoch González Pérez** | Diseño visual y tema, módulo del Dado, "Cómo se resuelve", portafolio y panel del maestro, Diccionario con API externa y traducción, integración del QR, voz compartida, control de versiones y documentación. |
| **Leonardo** (GitHub: `Leonardo251207`) | Integración con Firebase (Authentication, Firestore y Storage), sincronización de usuarios, evidencias y progreso, módulos de Números y Frases, y documentación del esquema de datos. |

## Créditos

[Free Dictionary API](https://dictionaryapi.dev), [ML Kit](https://developers.google.com/ml-kit), [ZXing](https://github.com/zxing/zxing),
[Retrofit](https://square.github.io/retrofit/), Firebase y Jetpack Compose.
