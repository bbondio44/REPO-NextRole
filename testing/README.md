# Testing y evidencia

## Comandos disponibles

Desde la raíz del repositorio:

```powershell
mvn -f backend/pom.xml test
npm --prefix frontend test
npm --prefix frontend run build
npm --prefix frontend run lint
git diff --check
```

El backend usa JUnit/Spring y Testcontainers para integración con PostgreSQL; Docker debe estar disponible para esos casos. El frontend usa Vitest y Testing Library. `build` ejecuta chequeos de TypeScript y Vite; `lint` ejecuta Oxlint.

## Elegir validaciones

- Ejecutá pruebas dirigidas para el comportamiento modificado, además de contratos consumidores/proveedores afectados.
- Ejecutá ambas suites cuando cambie un flujo compartido entre frontend y backend o una regla de negocio persistida.
- Para migraciones, validar sobre una base desechable/Testcontainers; nunca apuntar una prueba destructiva a datos locales o de usuario.
- Build y lint son checks separados de las pruebas. La validación visual/manual también es separada.
- En documentación exclusiva, comprobá links, comandos y consistencia; ejecutá `git diff --check`, sin repetir suites de aplicación.

## Registro

`specs/001-autoevaluacion-matching/quickstart.md` contiene escenarios manuales para el flujo V2. Registrar en `progress/current.md` fecha, comando exacto, resultado, advertencias, escenarios manuales efectivamente revisados y pendientes. Un test automatizado no cierra por sí solo una validación visual o manual.
