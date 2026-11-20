# NextRole — Frontend

Primera versión funcional del frontend de **NextRole** ("Potencia tu futuro IT"),
construida a partir del Workspace del proyecto (branding, arquitectura y modelo
de datos) y de los wireframes de Figma (`wireframe_AF1.png` a `AF5.png` en la
carpeta de la tesis).

## Cómo correrlo

```bash
npm install
npm run dev       # http://localhost:5173
```

Otros comandos: `npm run build` (compila y tipa), `npm run lint`, `npm run preview`.

## Qué hace hoy

Todavía **no hay backend**. El flujo completo funciona de punta a punta con
datos simulados en el cliente:

1. Registro / Login (simulados, guardan la "cuenta" en `localStorage`).
2. Onboarding de perfil académico.
3. Autoevaluación de habilidades (catálogo de 15 competencias en 9 categorías).
4. Resumen de habilidades por área.
5. Roles IT sugeridos, ordenados por afinidad (catálogo de 8 roles del doc).
6. Detalle de un rol + requisitos.
7. Diagnóstico de brechas de competencias.
8. Formación recomendada para cerrar esas brechas.
9. Perfil de cuenta / cerrar sesión.

## Dónde está "la lógica de negocio" (y por qué importa)

Toda la lógica que en producción vivirá en el backend (Spring Boot, según el
doc) está aislada en `src/services/`:

- `mockData.ts` — catálogo de competencias, roles, requisitos por rol y
  recursos de formación (semilla de datos, hoy en memoria).
- `storage.ts` — simula la persistencia (hoy `localStorage`, en producción la
  base PostgreSQL vía API REST).
- `api.ts` — las funciones que consume la UI (`registerAccount`, `login`,
  `getRoleMatching`, `getSkillGap`, `getRecommendedResources`, etc.). Implementan
  las mismas reglas que describe la Guía de Desarrollo (afinidad ponderada,
  brecha = máximo(0, requerido − actual), niveles "no evaluados" distintos de
  "nivel 0", etc.)

**Para conectar el backend real**: solo hay que reescribir el cuerpo de las
funciones de `api.ts` para que hagan `fetch`/`axios` contra los endpoints REST,
manteniendo las mismas firmas. Ninguna pantalla debería necesitar cambios,
porque todas usan TanStack Query sobre esas funciones.

## Estructura

Sigue la organización propuesta en el documento "Guía de Desarrollo NextRole"
(sección 7.2):

```
src/
  app/            # providers, router, guards de sesión
  components/ui/  # Button, TextField, Card, Badge, LevelPicker, etc.
  components/layout/
  features/       # auth, profile, home, assessment, roles, skill-gap, learning
  services/       # mockData, storage, api (futura capa de integración)
  types/          # tipos de dominio (Skill, Role, SkillGapItem, etc.)
```

## Ramas del repositorio

El repositorio de NextRole utiliza distintas ramas para conservar las versiones del proyecto y organizar su evolución.

| Rama | Descripción |
|---|---|
| `main` | Rama principal. Contiene NextRole V1 consolidada, incluyendo frontend, backend y documentación. |
| `develop-v2` | Rama de desarrollo de NextRole V2. Se utiliza para incorporar mejoras y nuevas funcionalidades sin afectar la versión estable. |
| `develop` | Rama de desarrollo utilizada durante la construcción inicial de V1. Se conserva como referencia histórica. |
| `Frontend-beta` | Rama utilizada para desarrollar y probar las primeras versiones del frontend. |
| `feature/backend-mvp-core` | Rama utilizada para implementar el backend inicial de NextRole con Spring Boot. |
| `integration/nextrole-v2` | Rama utilizada para consolidar el frontend actualizado, el backend y la documentación antes de integrarlos en `main` mediante la Pull Request #4. |

### Versiones del proyecto

- **V1 (`main`):** versión consolidada del proyecto, con frontend React, backend Spring Boot, PostgreSQL y documentación.
- **V2 (`develop-v2`):** evolución del proyecto, enfocada en mejorar la autoevaluación de competencias, la recomendación de roles IT, el motor de matching y el diagnóstico de brechas.

La V2 se desarrollará de manera independiente, utilizando GitHub Spec Kit y Codex para organizar las especificaciones, la planificación y la implementación.

### Flujo de trabajo

Las nuevas funcionalidades de V2 se desarrollarán en ramas específicas creadas a partir de `develop-v2`. Una vez implementadas y validadas, se integrarán mediante Pull Requests.

Cuando V2 alcance una versión estable, sus cambios podrán integrarse en `main`, conservando el historial de versiones anteriores.



## Pendiente / simplificaciones conocidas

- Branding: se usa un logo y una mascota placeholder en SVG (los assets reales
  del personaje 3D viven en Figma / el Workspace, no como archivos sueltos).
- El modelo de afinidad usa solo la dimensión de competencias (50% intereses +
  20% evidencia de GitHub del doc todavía no están implementados en esta capa).
- No hay persistencia real ni autenticación con JWT: es pura simulación en
  `localStorage` para poder demostrar el flujo sin backend.
