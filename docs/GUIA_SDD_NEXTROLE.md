# SDD y continuidad de trabajo en NextRole

Esta guía explica cómo trabajar con Codex u otros agentes sin depender del historial del chat. Las reglas permanentes y de seguridad están en [`AGENTS.md`](../AGENTS.md); la constitución está en [`../.specify/memory/constitution.md`](../.specify/memory/constitution.md).

## Recuperar o iniciar una sesión

1. Confirmar raíz y estado: `git status --short --branch`, `git log -5 --oneline --decorate`, `git diff` y `git diff --cached`.
2. Identificar cambios locales, archivos no rastreados y configuraciones que deban preservarse. No asumir que coinciden con el checkpoint documentado.
3. Leer la constitución, `progress/current.md` y la especificación vigente relacionada. Para una tarea de `001-autoevaluacion-matching`, leer su `spec.md`, `plan.md`, `tasks.md` y, según el alcance, `contracts/`, `data-model.md`, `research.md` y `quickstart.md`.
4. Comparar esos documentos con el código, las pruebas y el estado real de Git. `progress/history.md` sirve para rastrear decisiones y validaciones previas; no reemplaza la especificación vigente.
5. Si la sesión se interrumpió, continuar desde los archivos y diffs actuales. No repetir cambios ya hechos ni pruebas que sigan siendo válidas para el código sin cambios.

## Flujo proporcional

Para una funcionalidad importante:

1. **Specify y Clarify**: describir problema, alcance, estados, restricciones y criterios verificables; registrar decisiones aprobadas y sustituidas.
2. **Plan, Tasks y Analyze**: definir dependencias, contratos afectados, alternativas técnicas compatibles y tareas trazables; revisar coherencia entre los artefactos antes de programar.
3. **Implement**: realizar los cambios autorizados siguiendo arquitectura y límites de alcance.
4. **Validar técnicamente**: ejecutar las pruebas, build, lint y comprobaciones de contrato que correspondan al cambio.
5. **Validar manualmente**: realizar solo los escenarios realmente comprobados y dejar abiertos los demás.
6. **Cerrar**: reconciliar tareas, quickstart y `progress/current.md`; revisar el diff. Commit y push requieren autorización explícita.

Una corrección pequeña puede usar la especificación vigente, una clarificación puntual si cambia el comportamiento y tareas enfocadas. No hace falta regenerar todos los artefactos ni repetir suites completas cuando no hay cambios que las afecten. Si el usuario autorizó análisis, implementación y validación en una sola solicitud, avanzar entre esas etapas sin pedir permiso de nuevo; detenerse solo ante una contradicción material, decisión funcional nueva o bloqueo real. La aprobación funcional no sustituye los permisos técnicos del sandbox.

## Trabajo en paralelo

Usar paralelo solo cuando las tareas sean independientes y se puedan asignar límites de archivos claros. Preferir ramas/worktrees separados para cambios simultáneos; no permitir que varias instancias editen el mismo `spec.md`, `tasks.md` o `progress/current.md`. Una persona o agente integrador reconcilia decisiones y progreso, revisa los diffs y ejecuta las comprobaciones afectadas antes de integrar. Para una corrección pequeña o trabajo sobre los mismos archivos, trabajar secuencialmente es más simple.

## Fuente de verdad y especificaciones futuras

- La constitución define principios permanentes; `AGENTS.md`, cómo debe trabajar el agente.
- La especificación aprobada define comportamiento de producto. Un cambio aprobado sustituye la decisión anterior; conservar una nota histórica de qué cambió, cuándo y qué artefactos se actualizaron.
- El plan define enfoque técnico; `tasks.md` conserva el estado de tareas; contratos y modelo de datos definen interfaces e invariantes; quickstart conserva escenarios de validación.
- `001-autoevaluacion-matching` es la especificación base del flujo de evaluación, matching y su integración vigente en Inicio, Habilidades, Roles y la entrada general a Formación. No seguir ampliándola para módulos mayores independientes.
- Futuras funcionalidades importantes deberían tener especificaciones separadas para Skill Gap V2, Formación/Learning y diagnóstico personalizado con IA. Son agrupaciones preliminares, no definen numeración, arquitectura ni alcance. Cada nueva especificación debe referenciar contratos y decisiones existentes; no crearla hasta que el equipo autorice iniciar esa funcionalidad.

## Cierre y recuperación de interrupciones

Al cerrar una tarea, dejar en `progress/current.md`: rama y checkpoint conocidos, trabajo realizado, próximo paso, validaciones con resultados exactos, revisiones manuales pendientes, bloqueos/decisiones abiertas y archivos locales que deben conservarse. Mantener los detalles antiguos en `progress/history.md`, sin presentar notas históricas como estado actual. Actualizar checks de `tasks.md` solo con evidencia suficiente.

Si se interrumpe el agente, la siguiente sesión inspecciona primero Git y los archivos actuales, coteja el checkpoint con el diff y retoma solo el trabajo pendiente. No reconstruye el proyecto ni repite validaciones completas por rutina.

## Helpers de Spec Kit en PowerShell

Algunas instalaciones de PowerShell bloquean scripts por su política de ejecución. Inspeccionar el helper y sus dependencias antes de ejecutarlo. Si la política impide correr el helper, se puede solicitar autorización técnica del sandbox para ejecutar ese script puntual con una política temporal del proceso:

```powershell
pwsh -ExecutionPolicy Bypass -File .specify/scripts/powershell/check-prerequisites.ps1 -Json -RequireSpec -RequireTasks -IncludeTasks
```

La opción se aplica solo al proceso invocado; no ejecutar `Set-ExecutionPolicy`, no cambiar una política de usuario o equipo y no usar el bypass para scripts de origen desconocido. Si el helper sigue bloqueado o no está disponible, continuar con una inspección manual documentando la limitación; no declarar que se ejecutó.
