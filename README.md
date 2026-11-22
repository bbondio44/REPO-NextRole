# NextRole — Potencia tu futuro IT

Proyecto universitario de desarrollo de software orientado a la creación de una plataforma de orientación profesional para personas interesadas en iniciar o desarrollar su carrera en el sector tecnológico.

## Objetivo

Desarrollar una plataforma que permita evaluar competencias técnicas, identificar roles IT compatibles con el perfil del usuario y recomendar recursos de formación para reducir las brechas de conocimientos.

## Funcionalidades principales

La plataforma contempla:

- Registro e inicio de sesión.
- Gestión del perfil académico y técnico.
- Autoevaluación de competencias.
- Recomendación de roles IT según afinidad.
- Exploración de roles y sus requisitos.
- Identificación de brechas de competencias.
- Recomendación de recursos de formación.

## Versiones del proyecto

**NextRole V1:** primera versión funcional integrada de la plataforma, con frontend, backend y base de datos. Se conserva como versión consolidada en la rama `main`.

**NextRole V2:** evolución del proyecto enfocada en mejorar la autoevaluación de competencias, el motor de matching, la recomendación de roles IT y la experiencia de usuario. Su desarrollo se realiza en la rama `develop-v2`.

## Ramas del repositorio

El repositorio utiliza distintas ramas para conservar las versiones del proyecto y organizar su evolución.

| Rama | Descripción |
|---|---|
| `main` | Rama principal que contiene NextRole V1 consolidada. |
| `develop-v2` | Rama base de desarrollo e integración de NextRole V2. |
| `develop` | Rama histórica utilizada durante el desarrollo inicial de V1. |
| `Frontend-beta` | Rama utilizada para desarrollar y probar las primeras versiones del frontend. |
| `feature/backend-mvp-core` | Rama utilizada para implementar el backend inicial con Spring Boot. |
| `integration/nextrole-v2` | Rama histórica utilizada para integrar frontend, backend y documentación antes de consolidar V1. |

Las nuevas funcionalidades de V2 se desarrollan tomando `develop-v2` como referencia y se integran mediante Pull Requests una vez implementadas y validadas.

## Guías de trabajo

Para facilitar la colaboración y mantener un desarrollo consistente, NextRole V2 establece procedimientos compartidos para el uso de Git y el desarrollo de funcionalidades mediante inteligencia artificial.

- [Cómo trabajar con Git y GitHub para subir cambios](docs/GUIA_GIT_NEXTROLE.md)
- [Guía para usar GitHub Spec Kit con una IA](docs/GUIA_SDD_NEXTROLE.md)

---

**NextRole — Potencia tu futuro IT**
