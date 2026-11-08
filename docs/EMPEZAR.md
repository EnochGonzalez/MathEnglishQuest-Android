# Cómo empezar (guía para el equipo)

Estado del proyecto al **8 de noviembre de 2026**: la app ya usa Firebase de verdad
(Authentication, Firestore y Storage) y el esquema está documentado en
[`firestore.md`](firestore.md).

## 1. Lo que ya está hecho

| Pieza | Estado |
|---|---|
| Proyecto Firebase `mathenglishquest` | Creado, plan Blaze, Firestore y Storage activos |
| Login y registro | Firebase Authentication (correo y contraseña) |
| Perfiles y clases | Firestore `usuarios/{uid}` y `clases/{codigo}` |
| Mi Clase del maestro | Lee los estudiantes de Firestore en tiempo real |
| Evidencias (fotos) | Storage `evidencias/{uid}/archivo.jpg` + `clases/{codigo}/evidencias/{id}` |
| Progreso del Dado | Firestore `progreso/{uid}` (privado) |
| Reglas de seguridad | `firebase/firestore.rules` y `firebase/storage.rules` |

## 2. Para correr la app en tu computadora

1. **Pide el `google-services.json`** (no está en el repo por seguridad). Va en
   `app/google-services.json`. También puedes descargarlo tú desde
   *Configuración del proyecto → Tus apps → Android → Descargar google-services.json*.
2. Necesitas **JDK 21 o 25** y el **Android SDK** con la plataforma que pide el
   `app/build.gradle.kts`. Android Studio lo resuelve solo.
3. Abre el proyecto con Android Studio y dale *Run*.

Sin el `google-services.json` el proyecto **no compila** (el plugin de Google lo exige).

## 3. Para entrar al proyecto de Firebase

Pídele a Enoch que te agregue en la consola:
*Configuración del proyecto → Usuarios y permisos → Agregar miembro*.
Con rol **Editor** puedes ver Firestore, Storage y Authentication.

> Las reglas de seguridad **no aplican** al SDK de administrador: la API puede leer y
> escribir todo. Eso es justo lo que necesitas para el backend.

## 4. Lo que la API necesita saber

La app habla **directo con Firebase**. La API no es obligatoria para que la app funcione,
pero sí para lo que sigue (reportes, tareas, etc.).

**Validar quién llama**: el cliente manda el ID token de Firebase y tú lo verificas:

```js
const { initializeApp, applicationDefault } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
initializeApp({ credential: applicationDefault() });

const decoded = await getAuth().verifyIdToken(idToken);
// decoded.uid    -> UID del usuario
// decoded.email
```

**Enriquecer el perfil** (opcional): guarda el rol y la clase en *custom claims* para que
las reglas y la API no tengan que leer `usuarios`:

```js
await getAuth().setCustomUserClaims(uid, { rol: 'maestro', claseId: 'ABCDEF' });
```

**Consultas útiles**

```js
// Estudiantes de una clase
db.collection('usuarios').where('claseId', '==', codigo).where('rol', '==', 'estudiante');

// Evidencias de un estudiante
db.collection('clases').doc(codigo).collection('evidencias').where('uid', '==', uid);

// Progreso
db.collection('progreso').doc(uid).get();
```

**Fotos**: `admin.storage().bucket().file(rutaStorage)` o enlaces firmados con
`file.getSignedUrl()`.

## 5. Pendientes conocidos

- **Reintentar subidas sin internet.** Hoy, si un estudiante guarda una foto sin conexión,
  queda solo en su teléfono (`firebaseId = null` en Room) y no se vuelve a subir sola.
- **Borrar fotos**: las reglas lo prohíben desde la app (`allow update, delete: if false`).
  Si se necesita, se hace desde la API con el SDK de administrador.
- **Notificaciones / reportes** para el maestro.
- El **contador** `totalEvidencias` se incrementa al subir; si se borra una foto desde la
  API hay que descontarlo a mano.

## 6. Reglas: si cambias el esquema

Cada cambio en `firebase/*.rules` **hay que publicarlo a mano** en la consola:

| Archivo | Dónde |
|---|---|
| `firebase/firestore.rules` | Firestore Database → Reglas |
| `firebase/storage.rules` | Storage → Reglas |

Ojo con una limitación de Firestore: en una **consulta** (listado), una regla no puede usar
`get()` sobre los documentos devueltos. Por eso las evidencias viven dentro de la clase:
así la regla compara la **ruta** con el perfil (`miClaseId() == codigo`), que sí funciona
para consultas.

## 7. Dónde está cada cosa

```
app/src/main/java/com/upchiapas/mathenglishquest/
  data/remote/          Firebase: Auth, Firestore, Storage y los modelos *Doc
  data/repository/      Reglas de negocio (Auth, Class, Evidence, Dado)
  data/local/           Room: copia local que pinta la interfaz
  di/AppContainer.kt    Inyección de dependencias a mano
firebase/               Reglas de Firestore y Storage
docs/                   Este documento y el esquema de datos
```
