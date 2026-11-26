# Quickstart de validación: Skill Gap V2

Guía para validar después de implementar el servicio/API. Estas pruebas no se consideran ejecutadas hasta registrar sus resultados.

## Requisitos previos

- JDK 21 y Maven disponibles.
- Docker en ejecución para Testcontainers/PostgreSQL.
- Usuario autenticado con un intento V2 completado.

## Pruebas automatizadas

Desde la raíz:

```powershell
mvn -f backend/pom.xml -Dtest=SkillGapV2ServiceTest,SkillGapV2ApiIntegrationTest,AssessmentHistoryIntegrationTest test
mvn -f backend/pom.xml test
git diff --check
```

`AssessmentHistoryIntegrationTest` valida T025: unicidad persistida del borrador y que un borrador ya creado conserve cuestionario/matriz fijados cuando aparecen versiones actuales nuevas. La validación histórica de Skill Gap agrega que consultas de intentos completados continúan usando su matriz fijada. No inferir el cierre de T025 por inspección ni por pruebas no ejecutadas.

## Escenarios de servicio y API

1. Para un intento propio completado, consultar cualquier rol V2 y comprobar códigos/nombres, niveles, importancia y fórmula exacta.
2. Comprobar nivel 0, brecha positiva, competencia cubierta con brecha cero y exclusión de `NOT_RELEVANT`.
3. Probar dos intentos y versiones de matriz; publicar una versión actual posterior y verificar que los resultados del intento histórico no cambien.
4. Verificar respuestas `404`/`409` para usuario no autenticado, intento ajeno/no existente, borrador/descartado, rol inválido y dato histórico inconsistente.
5. Verificar que una matriz/rol sin requisitos relevantes retorna lista vacía, sin sustituir datos.
6. Confirmar que la respuesta no incluye descripción, texto de pregunta, prioridad, porcentajes ni matching y que la consulta no persiste diagnósticos ni altera intentos.

No se requiere validación visual o manual frontend para 002: las pantallas y la navegación específica V2 están diferidas. Las funcionalidades V1 no deben cambiar.

## Validación ejecutada

**2026-11-26** — `mvn -f backend/pom.xml test`: correcto, 38 pruebas, 0 fallos, 0 errores y 0 omitidas. La ejecución usó Testcontainers con Docker Desktop 28.5.1 y PostgreSQL 16.11. Incluyó `AssessmentHistoryIntegrationTest` (2 pruebas, incluida T025), `SkillGapV2ServiceTest` (4) y `SkillGapV2ApiIntegrationTest` (4), además del resto de la suite.

La validación fue técnica y automatizada. No se implementó UI V2 ni se requiere validación visual para esta especificación. `git diff --check` se registra por separado en `progress/current.md` una vez revisados todos los cambios locales.
