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
- `usuarios where claseId == miCodigo and rol == "estudiante"` ordenado por `nombreCompleto`.
- La app escucha ese query en tiempo real y copia los estudiantes a Room (caché local).
- No requiere índice compuesto: se filtra por `claseId` y el rol se descarta en el cliente.

## Reglas de seguridad

Están en `firebase/firestore.rules`. Se publican en
**Firestore Database → Reglas**. Resumen: todo exige sesión; cada quien escribe solo su
perfil; el rol y el correo no se pueden cambiar; se puede leer a quien comparte tu `claseId`.

Sobre `clases/{codigo}`: **leer un código concreto está permitido sin sesión** (para que el
estudiante sepa si el código existe antes de crear su cuenta), pero **listar** la colección
exige sesión, así que no se pueden descubrir códigos ajenos.

## Para la API

Con el SDK de administrador (`firebase-admin`, Node/Java/Python) se tiene acceso total y
**no** se evalúan las reglas, así que la API puede:

- Validar el token del cliente: `admin.auth().verifyIdToken(idToken)`.
- Leer/escribir `usuarios` y `clases` sin restricciones.
- Más adelante: `evidencias` y el progreso del Dado.

## Cómo está implementado en la app

| Pieza | Clase |
|---|---|
| Cuentas y contraseñas | `FirebaseAuthDataSource` (Firebase Authentication) |
| Perfiles y clases | `FirestoreDataSource` (`usuarios`, `clases`) |
| Copia local para la interfaz | `UsuarioMirror` → Room (`usuarios.firebaseUid`) |
| Login / registro | `AuthRepository` |
| Mi Clase | `ClassRepository`: escucha la clase en Firestore y la copia a Room |

Room sigue guardando **evidencias** y la app sigue funcionando sin internet con la última copia.
Las cuentas creadas antes de Firebase (solo en Room) ya no pueden entrar: hay que registrarse
otra vez, porque su contraseña nunca salió del teléfono.

## Pendiente (siguientes entregas)

- `evidencias/{id}` + fotos en **Firebase Storage** (hoy son locales en Room).
- Progreso del Dado en la nube (hoy en SharedPreferences).
