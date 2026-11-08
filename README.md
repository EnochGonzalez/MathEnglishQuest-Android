# Math & English Quest — estructura inicial

Aplicación Android educativa (Kotlin, Jetpack Compose y arquitectura MVVM) con módulos de matemáticas
y de inglés para niños.

> **Esta rama (`main`) solo contiene el cascarón del proyecto**: la distribución de carpetas, sin código funcional.
> Así lo establece la *Política de gestión de repositorios y control de versiones en GitHub* del curso.

## ¿Dónde está el código?

| Rama | Contenido |
|------|-----------|
| `main` | Estructura inicial del proyecto (esta rama). Sin código funcional. |
| `develop` | Código funcional integrado. Aquí está la aplicación que se compila y se ejecuta. |
| `feature/*` | Cambios en desarrollo. Cada uno entra a `develop` mediante Pull Request. |

Los releases (versiones estables, por ejemplo `v26.1.1`) se publican **desde `develop`**.

## Organización de carpetas

```
app/src/main/java/com/upchiapas/mathenglishquest/
├── data/        local (Room, SharedPreferences), remote (Firebase y API con Retrofit), repository, model, security
├── di/          contenedor de dependencias
├── domain/      reglas de negocio puras (sin Android)
├── ui/          components, navigation, screens (una carpeta por pantalla), theme
└── utils/       ayudas de audio, imágenes, etc.
app/src/main/res/   drawable, font, raw, values, xml
app/src/test/       pruebas unitarias
docs/               documentación y evidencias
firebase/           reglas de seguridad de Firestore y Storage
```

Para compilar y ejecutar la aplicación, cambia a la rama `develop`.
