# Guía para usar GitHub Spec Kit con una IA

Esta guía explica cómo utilizar **GitHub Spec Kit con Codex CLI** en NextRole V2, desde la instalación hasta la implementación y revisión de una funcionalidad. Está pensada para integrantes que todavía no conocen el desarrollo guiado por especificaciones.

**¿Por qué usarlo en equipo?** Permite que todos trabajen sobre requisitos, decisiones y criterios de aceptación compartidos. Así, las funcionalidades mantienen coherencia entre sí, se reducen las interpretaciones diferentes y es más sencillo continuar el trabajo de otra persona o retomar una sesión con la IA.


## 1. Qué es Spec Kit

**GitHub Spec Kit** es una herramienta para organizar el desarrollo de software mediante **Specification-Driven Development (SDD)**, es decir, definir y revisar qué se quiere construir antes de empezar a programar. Puede utilizarse con asistentes de IA como Codex CLI.

En lugar de pedirle directamente a la IA que implemente una idea, el trabajo se organiza en etapas:

1. **Constitución:** reglas permanentes que la IA debe respetar en el proyecto.
2. **Especificación (`spec.md`):** qué problema se quiere resolver y qué comportamiento se espera.
3. **Aclaraciones:** preguntas para resolver requisitos ambiguos.
4. **Plan (`plan.md`):** cómo se implementará técnicamente.
5. **Tareas (`tasks.md`):** pasos concretos, ordenados y verificables.
6. **Implementación:** la IA ejecuta las tareas aprobadas y se realizan pruebas.
7. **Convergencia:** se comprueba que el código final coincida con lo especificado.

**Spec Kit organiza el proceso; Codex ayuda a ejecutarlo; el equipo revisa y aprueba las decisiones.** Los documentos quedan en el repositorio para que cualquier integrante pueda consultarlos, sin depender de lo que una IA recuerde de una conversación anterior.

### Ejemplo en NextRole

Para la **autoevaluación y recomendación de roles IT**, antes de programar se definieron las competencias, los niveles de respuesta, las reglas del matching y los criterios para finalizar una evaluación. Los documentos de esa funcionalidad están en `specs/001-autoevaluacion-matching/`.

Así, si otro integrante mejora la interfaz del cuestionario, puede consultar las reglas ya aprobadas y evitar modificar accidentalmente el cálculo de afinidad o la persistencia de respuestas.

## 2. Archivos principales de Spec Kit en NextRole

| Archivo o carpeta                              | Para qué sirve                                          |
| ---------------------------------------------- | -------------------------------------------------------- |
| `AGENTS.md`                                  | Instrucciones de trabajo que debe consultar Codex.       |
| `CHECKPOINTS.md`                             | Referencias y puntos de control para retomar el trabajo. |
| `progress/current.md`                        | Estado y decisiones recientes de la implementación.     |
| `.specify/memory/constitution.md`            | Principios y reglas del proyecto.                        |
| `.specify/`                                  | Recursos y configuración de Spec Kit.                   |
| `specs/`                                     | Especificaciones por funcionalidad.                      |
| `specs/001-autoevaluacion-matching/spec.md`  | Requisitos de autoevaluación y matching de V2.          |
| `specs/001-autoevaluacion-matching/plan.md`  | Plan técnico de esa funcionalidad.                      |
| `specs/001-autoevaluacion-matching/tasks.md` | Tareas y seguimiento de esa funcionalidad.               |
| `docs/GUIA_GIT_NEXTROLE.md`                  | Guía de trabajo con Git y GitHub.                       |

> Los archivos de seguimiento ayudan a retomar sesiones, pero no reemplazan a la especificación aprobada. Antes de actuar, Codex debe revisar los documentos relevantes y el estado actual de Git.

## 3. Terminal y chat de la IA no son lo mismo

| Terminal                       | Chat de Codex          |
| ------------------------------ | ---------------------- |
| `git status`                 | `$speckit-specify`   |
| `git switch develop-v2`      | `$speckit-clarify`   |
| `specify version`            | `$speckit-plan`      |
| `specify integration status` | `$speckit-tasks`     |
| `codex`                      | `$speckit-implement` |

Los comandos `$speckit-*` se envían **como mensajes dentro de Codex**, no como comandos de PowerShell.

## 4. Requisitos e instalación

Cada integrante necesita Git, una copia del repositorio, una terminal, Codex CLI y las herramientas requeridas por la versión de Spec Kit utilizada. Se recomienda `uv` para administrar herramientas Python.

Documentación oficial:

- [GitHub Spec Kit](https://github.com/github/spec-kit)
- [Instalación de Spec Kit](https://github.com/github/spec-kit/blob/main/docs/installation.md)
- [Versiones de Spec Kit](https://github.com/github/spec-kit/releases/latest)
- [Instalación de uv](https://docs.astral.sh/uv/getting-started/installation/)
- [Codex CLI](https://developers.openai.com/codex/cli)

### Instalar uv (Windows PowerShell)

```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
uv --version
```

En macOS o Linux:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
uv --version
```

### Instalar Spec Kit

Consultar la versión publicada compatible con el repositorio. Por ejemplo, sustituyendo `vX.Y.Z` por la versión elegida:

```bash
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git@vX.Y.Z
specify version
specify check
```

También existe la instalación desde PyPI: `uv tool install specify-cli`.

### Instalar Codex CLI

Seguir las instrucciones oficiales de [Codex CLI](https://developers.openai.com/codex/cli) para el sistema operativo. Comprobar:

```bash
codex --version
```

## 5. Preparar NextRole después de clonarlo

Desde la raíz del repositorio:

```bash
git status
git branch --show-current
specify version
specify integration status
```

Revisar que estén disponibles `AGENTS.md`, `.specify/` y la especificación con la que se va a trabajar. Si la integración de Codex no está disponible, revisar su configuración antes de intentar instalar o sobrescribir archivos.

**No ejecutar `specify init --here --force` como paso habitual.** Puede reemplazar archivos existentes.

## 6. Ramas de Git y especificaciones: cómo se relacionan

En NextRole, `main` conserva la V1 consolidada y **`develop-v2` es la base de desarrollo de V2**. Para nuevas funcionalidades, el flujo previsto es crear una rama específica a partir de `develop-v2`, implementar y validar, y luego integrar mediante Pull Request.

La guía de referencia para los comandos de Git es [`GUIA_GIT_NEXTROLE.md`](./GUIA_GIT_NEXTROLE.md).

Ejemplo ilustrativo para una **nueva funcionalidad**:

```bash
git switch develop-v2
git pull origin develop-v2
git switch -c feature/historial-evaluaciones
git status
codex
```

> Antes de cambiar de rama, comprobar `git status` y preservar los cambios locales. **No crear una nueva rama ni volver a ejecutar `$speckit-specify` si la tarea consiste en continuar una especificación ya existente**, salvo decisión expresa del equipo.

**Importante:** una rama de Git y una carpeta dentro de `specs/` son conceptos distintos. Cambiar de rama no significa automáticamente que Codex haya seleccionado la especificación correcta.

## 7. Flujo recomendado para desarrollar una funcionalidad

### Paso 1. Identificar la funcionalidad

Determinar si se trata de una funcionalidad nueva o de continuar una existente. En NextRole ya existe:

```text
specs/
└── 001-autoevaluacion-matching/
    ├── spec.md
    ├── plan.md
    └── tasks.md
```

Si se va a continuar la autoevaluación o el matching, **leer primero esos documentos** y el progreso actual. No crear una segunda especificación para la misma funcionalidad sin motivo.

### Paso 2. Abrir Codex desde la raíz

```bash
codex
```

Pedirle que lea `AGENTS.md`, `CHECKPOINTS.md`, `progress/current.md`, la constitución y los documentos de la funcionalidad antes de proponer cambios.

### Paso 3. Crear una especificación (solo si es nueva)

En el **chat de Codex**:

```text
$speckit-specify Crear una funcionalidad para consultar el historial de autoevaluaciones de NextRole V2. Definir qué información ve el usuario, cómo accede a evaluaciones anteriores y los criterios de aceptación. No asumir decisiones pendientes.
```

Este paso define **qué** debe ocurrir, no todos los detalles técnicos. El equipo revisa el `spec.md` resultante.

### Paso 4. Aclarar dudas

```text
$speckit-clarify
```

Ejemplo: ¿el usuario puede consultar evaluaciones anteriores sin iniciar una nueva? ¿Qué datos deben verse en el historial? No convertir respuestas hipotéticas en requisitos definitivos.

### Paso 5. Generar el plan

```text
$speckit-plan
```

El plan debe respetar la arquitectura y las reglas existentes. Por ejemplo, si la funcionalidad necesita persistencia, revisar primero el modelo de datos **vigente de V2**, sin asumir que las migraciones de V1 son el diseño definitivo.

### Paso 6. Revisar con un checklist (opcional)

```text
$speckit-checklist
```

Útil para revisar reglas de negocio, errores, seguridad, persistencia y criterios de aceptación.

### Paso 7. Generar tareas

```text
$speckit-tasks
```

Las tareas deben ser pequeñas, ordenadas y comprobables. Revisarlas antes de comenzar la implementación.

### Paso 8. Analizar coherencia

```text
$speckit-analyze
```

Revisar contradicciones entre `spec.md`, `plan.md` y `tasks.md`. Corregir los problemas importantes antes de implementar.

### Paso 9. Implementar

```text
$speckit-implement
```

Codex ejecuta las tareas acordadas. No debe modificar funcionalidades ajenas sin justificación ni introducir cambios de backend, base de datos o matching cuando el pedido es solamente visual.

### Paso 10. Verificar convergencia

```text
$speckit-converge
```

Comprobar que el código cumple lo especificado y que no queden tareas necesarias. Si se detectan diferencias, corregirlas y repetir las verificaciones.

### Paso 11. Revisar antes de publicar

```bash
git status
git diff
git diff --check
```

Ejecutar las pruebas pertinentes y seguir [`GUIA_GIT_NEXTROLE.md`](./GUIA_GIT_NEXTROLE.md) para preparar commits y Pull Requests. **Codex no debe hacer commit ni push sin autorización explícita.**

## 8. Ejemplo real: autoevaluación y matching de NextRole V2

El trabajo existente en `specs/001-autoevaluacion-matching/` ilustra el flujo:

| Etapa           | Ejemplo en NextRole                                                                                                                                          |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Especificación | Definir el cuestionario de 31 competencias, niveles de respuesta y comportamiento de preguntas pendientes.                                                   |
| Plan            | Diseñar la integración entre interfaz, guardado de respuestas y cálculo de afinidad.                                                                      |
| Tareas          | Dividir la construcción del cuestionario, persistencia, finalización y matching en pasos verificables.                                                     |
| Implementación | Construir la interfaz, guardar respuestas y ejecutar el cálculo de recomendaciones.                                                                         |
| Verificación   | Comprobar que nivel 0 es una respuesta válida, que las preguntas sin responder no se confunden con nivel 0 y que no se finaliza una evaluación incompleta. |

La evaluación reúne cuatro áreas temáticas: **Desarrollo de Software, QA Testing, Análisis de Datos e Infraestructura y Cloud**. No deben confundirse con los **seis roles profesionales** utilizados en el matching. Las competencias compartidas se responden una sola vez.

### Ejemplo de una mejora sobre la funcionalidad existente

Para un ajuste visual como «reemplazar los bloques numerados por una barra de progreso», no es necesario recrear toda la especificación ni rediseñar el matching. El procedimiento recomendado es:

1. Revisar `spec.md`, `plan.md`, `tasks.md` y `progress/current.md`.
2. Identificar qué requisitos ya se cumplen y qué cambio visual se solicita.
3. Documentar el ajuste donde corresponda, sin alterar reglas aprobadas.
4. Implementar únicamente el alcance acordado.
5. Probar que la barra contabilice respuestas guardadas, incluido el nivel 0, y que navegar sin responder no incremente el progreso.
6. Revisar el resultado visual y funcional **antes** de autorizar un commit.

Esto evita repetir trabajo ya implementado y reduce el riesgo de alterar reglas de negocio por un cambio de UX.

## 9. Comandos más comunes

### Dentro del chat de Codex

| Comando                   | Uso                                                               |
| ------------------------- | ----------------------------------------------------------------- |
| `$speckit-constitution` | Crear o actualizar las reglas generales; no usar para cada tarea. |
| `$speckit-specify`      | Definir requisitos de una funcionalidad.                          |
| `$speckit-clarify`      | Resolver ambigüedades.                                           |
| `$speckit-plan`         | Diseñar la implementación.                                      |
| `$speckit-checklist`    | Revisar requisitos críticos.                                     |
| `$speckit-tasks`        | Crear tareas verificables.                                        |
| `$speckit-analyze`      | Comprobar consistencia documental.                                |
| `$speckit-implement`    | Implementar las tareas.                                           |
| `$speckit-converge`     | Verificar diferencias entre documentación e implementación.     |

La disponibilidad exacta de los skills depende de la integración instalada. Comprobarla antes de usarlos.

### En la terminal

| Comando                        | Uso                                   |
| ------------------------------ | ------------------------------------- |
| `specify version`            | Ver la versión instalada.            |
| `specify check`              | Revisar requisitos del entorno.       |
| `specify integration status` | Comprobar la integración del agente. |
| `specify self check`         | Consultar actualizaciones.            |
| `specify integration list`   | Ver integraciones disponibles.        |
| `git status`                 | Revisar cambios y rama actual.        |

## 10. Reglas de trabajo para el equipo

- Revisar cada documento antes de avanzar al siguiente paso.
- La constitución contiene reglas acordadas, no suposiciones de la IA.
- No implementar una funcionalidad nueva antes de revisar su especificación, plan y tareas.
- No confundir mejoras de interfaz con cambios en la lógica de negocio.
- Preservar los cambios locales y archivos ajenos a la tarea.
- No subir secretos, credenciales, tokens ni archivos `.env` con datos privados.
- Ejecutar pruebas y revisar el `git diff` antes de aprobar los cambios.
- Mantener actualizados los documentos de seguimiento cuando corresponda.
- No autorizar automáticamente commits, pushes ni Pull Requests.
- Consultar la guía de Git para el flujo de integración a `develop-v2`.

## 11. Problemas frecuentes

**Codex no reconoce `$speckit-*`:** comprobar que se abrió desde la raíz, revisar los archivos de integración y ejecutar `specify integration status`. Reiniciar Codex si se instalaron skills recientemente.

**La terminal no reconoce `specify` o `codex`:** comprobar la instalación y el `PATH`; cerrar y volver a abrir la terminal.

**Codex quiere crear una nueva especificación para una tarea existente:** señalar la carpeta `specs/001-autoevaluacion-matching/` y pedir que revise el trabajo previo antes de proponer archivos nuevos.

**Hay cambios sin commit:** no descartarlos ni cambiar de rama a ciegas. Revisar `git status`, identificar qué pertenece a la tarea y conservar los archivos ajenos.

**La implementación no coincide con la especificación:** detener la publicación, comparar documentos y código, corregir y repetir pruebas.

## 12. Actualizar Spec Kit de forma segura

La herramienta instalada y los archivos de integración del repositorio pueden requerir actualizaciones separadas. Primero preservar los cambios locales y revisar:

```bash
specify self check
specify self upgrade --dry-run
specify integration status
git status
```

Cualquier actualización de la integración debe realizarse de manera coordinada y revisando el diff. No usar opciones forzadas sin conocer qué archivos se sobrescribirán.

## 13. Referencias oficiales

- [Documentación oficial de GitHub Spec Kit](https://github.github.io/spec-kit/)
- [Repositorio de Spec Kit](https://github.com/github/spec-kit)
- [Guía de inicio de SDD](https://github.com/github/spec-kit/blob/main/docs/quickstart.md)
- [Documentación de Codex CLI](https://developers.openai.com/codex/cli)
- [Guía de Git de NextRole](./GUIA_GIT_NEXTROLE.md)

---

**NextRole — Potencia tu futuro IT**
