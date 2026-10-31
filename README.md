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

## Pendiente / simplificaciones conocidas

- Branding: se usa un logo y una mascota placeholder en SVG (los assets reales
  del personaje 3D viven en Figma / el Workspace, no como archivos sueltos).
- El modelo de afinidad usa solo la dimensión de competencias (50% intereses +
  20% evidencia de GitHub del doc todavía no están implementados en esta capa).
- No hay persistencia real ni autenticación con JWT: es pura simulación en
  `localStorage` para poder demostrar el flujo sin backend.
