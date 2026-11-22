# Guía de GitHub Spec Kit y Codex CLI — NextRole V2

Esta guía explica cómo utilizar **GitHub Spec Kit junto con Codex CLI** para organizar el desarrollo de NextRole V2.

Su objetivo es facilitar el **trabajo en equipo y el desarrollo consistente de funcionalidades**, permitiendo que todos los integrantes compartan requisitos, decisiones técnicas y criterios de aceptación. Así se reducen las contradicciones, el trabajo duplicado y los problemas al integrar cambios realizados por diferentes desarrolladores.

## 1. ¿Qué es GitHub Spec Kit?

**GitHub Spec Kit** es una herramienta que permite aplicar el enfoque de **desarrollo guiado por especificaciones (SDD)** con asistentes de inteligencia artificial como Codex.

En lugar de pedirle directamente a la IA que programe una funcionalidad, Spec Kit organiza el desarrollo en etapas documentadas:

1. **Constitución:** establece las reglas generales que deben respetarse en el proyecto.
2. **Especificación (`spec.md`):** define qué debe hacer la funcionalidad y cuáles son sus requisitos.
3. **Aclaraciones:** resuelve dudas y decisiones pendientes.
4. **Plan (`plan.md`):** establece cómo se implementará técnicamente.
5. **Tareas (`tasks.md`):** divide el trabajo en pasos concretos y verificables.
6. **Implementación:** Codex desarrolla la funcionalidad siguiendo los documentos aprobados.
7. **Verificación:** se comprueba que el resultado coincida con lo especificado.

**Spec Kit organiza y documenta el proceso; Codex ayuda a ejecutarlo.** El equipo sigue siendo responsable de revisar y aprobar las decisiones.

### Ejemplo en NextRole

Para desarrollar el sistema de autoevaluación y recomendación de roles IT, primero se definieron las competencias evaluables, los niveles de conocimiento, las reglas de afinidad y los criterios de aceptación.

Estas decisiones quedaron documentadas en `specs/001-autoevaluacion-matching/`, permitiendo implementar, probar y mejorar la funcionalidad sin redefinir sus reglas en cada sesión.

De esta manera, distintos integrantes pueden continuar el desarrollo utilizando una misma referencia, evitando implementaciones inconsistentes.
