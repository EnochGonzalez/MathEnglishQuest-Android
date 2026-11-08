# Math & English Quest (Android)

App educativa que combina matemáticas y vocabulario en inglés: módulo del Dado, números,
frases y portafolio de evidencias, con panel para el maestro.

La información se guarda en **Firebase** (Authentication, Firestore y Storage) y se cachea
en el teléfono con Room para que la app funcione sin internet.

## Documentación

- [`docs/EMPEZAR.md`](docs/EMPEZAR.md) — cómo correr el proyecto y qué necesita la API.
- [`docs/firestore.md`](docs/firestore.md) — esquema de datos y reglas de seguridad.

## Compilar

```bash
# Requiere app/google-services.json (no se versiona)
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Reglas de Firebase

Antes de usar la app, publica las reglas en la consola:

| Archivo | Dónde |
|---|---|
| `firebase/firestore.rules` | Firestore Database → Reglas |
| `firebase/storage.rules` | Storage → Reglas |
