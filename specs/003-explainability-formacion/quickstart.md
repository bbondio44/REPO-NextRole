# Guía de validación — Explainability y Formación

Esta guía define verificaciones para la implementación posterior. Plan no añade código ni realiza llamadas reales al proveedor.

## Pruebas locales automatizadas

Desde la raíz:

```powershell
mvn -f backend/pom.xml test
npm --prefix frontend test
npm --prefix frontend run build
npm --prefix frontend run lint
git diff --check
```

Las pruebas de integración usan Testcontainers/PostgreSQL desechable y un generador falso; no requieren API key, no acceden a OpenRouter ni consumen tokens. Validar como mínimo: V7 aditiva desde V6; histórico Q1 y Q2/M1; schema/priority/context; compatibilidad reproducible del contexto Java con el contrato experimental Python (incluidos nombres, tipos, niveles, descripciones, clasificación/importancia, brechas, prioridad, porcentaje y descripción de rol, excluyendo metadatos locales); ownership; concurrencia; persistencia/reutilización; timeout, JSON inválido y reintento manual; navegación y estados UI. No probar cuotas internas porque no existen.

## Validación manual posterior

Con backend/frontend locales, recorrer ranking V2 actual y un intento histórico hacia Formación, regresar al ranking exacto, probar diagnóstico ya persistido, sin brechas, estado FAILED y reintento manual. Comprobar el ciclo: NOT_STARTED solicita una única generación; GENERATING consulta GET; SUCCEEDED recupera snapshots y diagnosis; FAILED espera acción del usuario. Remontar, volver a entrar, recargar o recuperar foco no inicia retry. Revisar los seis SVG/paletas cuando hayan sido entregados, contraste AA, teclado, lector de pantalla y movimiento reducido. La captura mobile/desktop no se considera validada por un build.

## Prueba con OpenRouter

Requiere aprobación expresa posterior. Configurar `OPENROUTER_API_KEY` únicamente en entorno secreto del backend. Antes de cualquier llamada, verificar que endpoint/modelo exacto sigan habilitados, que `provider.allow_fallbacks=false`, que no exista otro modelo en request y que no haya retry en cliente/proveedor. Registrar un caso controlado, hash del prompt/context, modelo solicitado/servido, uso, latencia, costo si informado y código de resultado. Detener ante disponibilidad no gratuita o respuesta inesperada; no fallback ni retry automático. Esta etapa no forma parte de las pruebas anteriores.
