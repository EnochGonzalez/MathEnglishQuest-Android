# Base de datos en Firebase (Firestore)

Este documento es el **contrato de datos** entre la app Android y la API. Si algo cambia aquí,
tiene que cambiar en los dos lados.

- Proyecto: `MathEnglishQuest` en https://console.firebase.google.com
- Paquete Android: `com.upchiapas.mathenglishquest`
- Base de datos: **Cloud Firestore** (modo producción, región `us-central1`)
- Autenticación: **Firebase Authentication** con proveedor **Correo/Contraseña**

## Configuración local

El archivo `app/google-services.json` lo genera la consola de Firebase y **no se sube al repo**
(está en `.gitignore`). Cada quien lo descarga de la consola y lo deja en `app/`.

## Colecciones

### `usuarios/{uid}`

El id del documento es el **UID de Firebase Auth**, no un id numérico. Por eso nadie escribe
contraseñas: Auth las guarda cifradas y la app solo recibe el UID.

| Campo | Tipo | Descripción |
|---|---|---|
| `nombreCompleto` | string | Nombre y dos apellidos |
| `correo` | string | Siempre en minúsculas; igual al de Auth |
| `rol` | string | `"estudiante"` o `"maestro"` |
| `claseId` | string \| null | Maestro: su código. Estudiante: el código al que se unió |
| `creadoEn` | timestamp | Se pone al crear |
| `actualizadoEn` | timestamp | Se actualiza en cada escritura |

Ejemplo:

```json
{
  "nombreCompleto": "Leonardo Pérez Gómez",
  "correo": "253425@it2id.upchiapas.edu.mx",
  "rol": "estudiante",
  "claseId": "K7M2QP",
  "creadoEn": "2026-11-08T18:00:00Z",
  "actualizadoEn": "2026-11-08T18:00:00Z"
}
```

### `clases/{codigo}`

El id del documento **es** el código de 6 caracteres (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`,
sin letras que se confunden). Al ser id de documento, Firestore impide duplicados: eso
reemplaza la validación de "código existente" que antes hacía Room.

| Campo | Tipo | Descripción |
|---|---|---|
| `codigo` | string | El mismo valor que el id del documento |
| `maestroUid` | string | UID del maestro dueño |
| `maestroNombre` | string | Para mostrarlo sin leer `usuarios` |
| `creadoEn` | timestamp | |

Ejemplo:

```json
{
  "codigo": "K7M2QP",
  "maestroUid": "aB3dE5gH7jK9mN1pQ2rS3",
  "maestroNombre": "Enoch González Pérez",
  "creadoEn": "2026-11-08T17:00:00Z"
}
```

### `clases/{codigo}/evidencias/{id}`

Ficha de una foto del portafolio. **Van dentro de la clase** (`clases/{codigo}/evidencias`),
no en una colección raíz. Es a propósito: en una **consulta**, Firestore no puede evaluar
reglas que usan `get()` sobre los documentos devueltos; al llevar la clase en la ruta, la
regla compara la ruta con el perfil y el maestro sí puede listar las evidencias de su clase.

El id lo genera Firestore y **la imagen no va aquí**: vive en Firebase Storage
(`evidencias/{uid}/{timestamp}.jpg`).

| Campo | Tipo | Descripción |
|---|---|---|
| `uid` | string | Estudiante dueño de la foto |
| `claseId` | string | Copia del código de la clase |
| `rutaStorage` | string | `evidencias/{uid}/1730000000000.jpg` |
| `fecha` | timestamp | |
| `dificultad` | string | `"Fácil"`, `"Medio"`, ... |
| `categorias` | string | Operaciones separadas por `\|` |
| `ejercicio` | string | El ejercicio tal como lo vio el estudiante |

Además, `usuarios/{uid}` lleva dos campos que mantiene la app al subir una foto (con
`FieldValue.increment`), para que el panel del maestro muestre el total sin descargar fotos:

| Campo | Tipo |
|---|---|
| `totalEvidencias` | number |
| `ultimaEvidenciaEn` | timestamp |

### `progreso/{uid}`

Estado del módulo del Dado. Es privado de cada estudiante.

| Campo | Tipo |
|---|---|
| `halfHearts` | number (medios corazones) |
| `streak` | number |
| `maxUnlockedLevel` | number |
| `currentLevel` | number |
| `mode` | string (`DIRECTO`, `NORMAL`, `RUSH`) |
| `hadProgressedBeyondEasy` | boolean |
| `allowNegatives` | boolean |
| `actualizadoEn` | timestamp |

## Flujos
**Registro de maestro**
1. `Auth.createUserWithEmailAndPassword(correo, password)` → obtiene `uid`.
2. Se genera un código que no exista como documento en `clases`.
3. Se crea `clases/{codigo}` con `maestroUid = uid`.
4. Se crea `usuarios/{uid}` con `rol = "maestro"` y `claseId = codigo`.

**Registro de estudiante**
1. Se lee `clases/{codigo}`: si no existe, el código es inválido.
2. `Auth.createUserWithEmailAndPassword(...)` → `uid`.
3. Se crea `usuarios/{uid}` con `rol = "estudiante"` y `claseId = codigo`.

**Mi Clase (maestro)**
- `usuarios where claseId == miCodigo` ordenado por `nombreCompleto` (el rol se filtra en el cliente).
- La app escucha ese query en tiempo real y copia los estudiantes a Room (caché local).
- El conteo de evidencias sale de `totalEvidencias` del perfil, no de leer las fotos.

**Portafolio del estudiante**
1. La foto se comprime a ~1280 px de lado.
2. Se sube a Storage en `evidencias/{uid}/{timestamp}.jpg`.
3. Se crea el documento en `clases/{codigo}/evidencias`.
4. Se suma 1 a `usuarios/{uid}.totalEvidencias` con `FieldValue.increment`.
5. Se guarda la copia local en Room (la pantalla pinta desde ahí).

**Portafolio visto por el maestro**
- Consulta `clases/{miCodigo}/evidencias where uid == <uid del estudiante>`.
- Copia las fichas a Room y **descarga la foto a la caché local** la primera vez que se ve.

## Reglas de seguridad

Hay **dos** archivos y se publican por separado:

| Archivo | Dónde se pega |
|---|---|
| `firebase/firestore.rules` | Firestore Database → Reglas |
| `firebase/storage.rules` | Storage → Reglas |

Resumen de Firestore: todo exige sesión; cada quien escribe solo su perfil; el rol y el
correo no se pueden cambiar; se puede leer a quien comparte tu `claseId`; las evidencias las
crea solo su dueño y las lee su clase; el progreso es privado.

Sobre `clases/{codigo}`: **leer un código concreto está permitido sin sesión** (para que el
estudiante sepa si el código existe antes de crear su cuenta), pero **listar** la colección
exige sesión, así que no se pueden descubrir códigos ajenos.

Sobre Storage: solo el dueño sube fotos a su carpeta (máximo 5 MB y solo `image/*`).
Cualquier usuario con sesión puede leerlas, porque el maestro necesita ver las fotos de sus
estudiantes y las reglas de Storage no pueden consultar Firestore.

## Para la API

Con el SDK de administrador (`firebase-admin`, Node/Java/Python) se tiene acceso total y
**no** se evalúan las reglas, así que la API puede:

- Validar el token del cliente: `admin.auth().verifyIdToken(idToken)`.
- Leer/escribir `usuarios`, `clases`, `evidencias` y `progreso` sin restricciones.
- Generar enlaces firmados de las fotos de Storage.

## Cómo está implementado en la app

| Pieza | Clase |
|---|---|
| Cuentas y contraseñas | `FirebaseAuthDataSource` (Firebase Authentication) |
| Perfiles y clases | `FirestoreDataSource` (`usuarios`, `clases`) |
| Evidencias | `StorageDataSource` (fotos) + `FirestoreDataSource` (`evidencias`) |
| Progreso del Dado | `DadoProgressRepository` (`progreso/{uid}` + SharedPreferences) |
| Copia local para la interfaz | `UsuarioMirror` y `EvidenceRepository` → Room |
| Login / registro | `AuthRepository` |
| Mi Clase | `ClassRepository`: escucha la clase en Firestore y la copia a Room |

Room es la **copia local** de todo lo que pinta la pantalla: la app funciona sin internet
con lo último descargado. Las fotos se descargan a la carpeta privada la primera vez que se
ven y ya no se vuelven a bajar.

Las cuentas creadas antes de Firebase (solo en Room) ya no pueden entrar: hay que registrarse
otra vez, porque su contraseña nunca salió del teléfono.

## Pendiente (siguientes entregas)

- Volver a subir las fotos que se guardaron sin internet (hoy quedan marcadas con
  `firebaseId = null` en Room y solo se ven en ese celular).
- Notificaciones o reportes para el maestro.
