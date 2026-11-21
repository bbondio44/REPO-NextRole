# Autoevaluación técnica y matching — catálogo y matriz vigentes

**Estado**: Referencia definitiva de NextRole V2 para 31 preguntas y requisitos técnicos de seis roles.

## Prioridad de las fuentes

1. Decisiones explícitas y definitivas aprobadas para NextRole V2 y consolidadas aquí.
2. Decisiones funcionales previamente aprobadas para V2.
3. La especificación funcional, siempre que no contradiga decisiones aprobadas.
4. PDF originales como antecedentes históricos; no sobrescriben aprobaciones posteriores.

Este catálogo y matriz corresponden a NextRole V2 y reemplazan la matriz histórica V1 como referencia vigente.

## Reglas funcionales aprobadas

- Una autoevaluación compartida de 31 competencias; cada una se responde una vez con nivel 0–4. Nivel 0 es válido y distinto de no respondida.
- Seis roles: Backend, Frontend, Full Stack, QA, Data Analyst y Cloud.
- Para competencias relevantes, los niveles esperados admitidos son 1–4. La matriz V2 actualmente aprobada conserva sus asignaciones existentes, todas con nivel esperado 1–3.
- Esencial = peso 2; complementaria = peso 1; no relevante = excluida.
- Aporte individual = **min(nivel declarado / nivel esperado, 1)**. Porcentaje por rol = **100 × suma(peso × aporte) / suma(pesos)** sobre competencias relevantes.
- Superar el nivel esperado no suma puntos. Matching mide solo preparación técnica, no intereses, objetivos ni proyectos, y requiere las 31 respuestas.
- Los porcentajes se ordenan con precisión completa y se muestran redondeados a un decimal. En empate exacto, el orden se aleatoriza una vez y se persiste.
- Matching y Skill Gap son deterministas. La IA posterior podrá explicar resultados estructurados, pero no alterar porcentajes, brechas ni prioridades calculados por Java.
- Respuestas incompletas se guardan automáticamente, persisten asociadas al usuario, se recuperan al volver y son editables durante el intento. Las evaluaciones finalizadas son inmutables.
- Reevaluarse crea un intento nuevo precargado con la última evaluación completa si existe. Se conservan intentos finalizados, resultados y versiones asociadas; un incompleto no reemplaza el perfil vigente.
- Resultados son autoevaluaciones, no certificaciones. No se exige empleo; colaboración admite proyectos académicos, personales o independientes con otras personas.

## Cuestionario de autoevaluación

El catálogo vigente contiene una pregunta por competencia y cinco opciones de respuesta, niveles 0 a 4. Se preservan las redacciones aprobadas.

### Pregunta 1 — Fundamentos de programación

**Pregunta:** ¿Qué experiencia tenés utilizando los fundamentos de programación para desarrollar y modificar programas?

- **Nivel 0:** No conozco conceptos como variables, condiciones, ciclos o funciones.
- **Nivel 1:** Comprendo para qué sirven estos elementos y puedo reconocer cómo se utilizan en programas sencillos.
- **Nivel 2:** Puedo escribir o modificar programas sencillos utilizando variables, condiciones, ciclos y funciones, comprendiendo su funcionamiento.
- **Nivel 3:** Tengo experiencia desarrollando programas para resolver problemas habituales, organizando la lógica en funciones e identificando y corrigiendo errores.
- **Nivel 4:** Puedo analizar diferentes formas de resolver un problema, identificar errores lógicos, mejorar la organización del código y justificar las decisiones tomadas.

### Pregunta 2 — Algoritmos y resolución eficiente de problemas

**Pregunta:** ¿Qué experiencia tenés diseñando o utilizando algoritmos para procesar información y resolver problemas, eligiendo soluciones adecuadas según su funcionamiento y eficiencia?

- **Nivel 0:** No conozco qué es un algoritmo ni cómo se utiliza para resolver problemas.
- **Nivel 1:** Comprendo qué es un algoritmo y cómo se utiliza para realizar tareas como buscar, recorrer u ordenar información.
- **Nivel 2:** Puedo implementar o adaptar algoritmos sencillos para procesar información y resolver problemas, comprendiendo cómo funcionan.
- **Nivel 3:** Puedo seleccionar diferentes soluciones algorítmicas según el problema, comprobar sus resultados y reconocer diferencias de eficiencia.
- **Nivel 4:** Puedo comparar algoritmos, analizar su complejidad y optimizar soluciones según el volumen de datos, los recursos disponibles y los requisitos.

### Pregunta 3 — Estructuras de datos

**Pregunta:** ¿Qué experiencia tenés seleccionando y utilizando estructuras de datos para organizar y procesar información?

- **Nivel 0:** No conozco las estructuras de datos.
- **Nivel 1:** Comprendo para qué sirven estructuras como arreglos, listas, conjuntos, pilas, colas o mapas.
- **Nivel 2:** Puedo utilizar estructuras de datos habituales, como listas, conjuntos y mapas, para almacenar, buscar y procesar información, comprendiendo sus diferencias básicas.
- **Nivel 3:** Puedo seleccionar y utilizar diferentes estructuras de datos según las necesidades de una aplicación, comprendiendo cómo permiten almacenar, buscar y modificar información.
- **Nivel 4:** Puedo comparar estructuras de datos, identificar sus ventajas y limitaciones y justificar cuál resulta más adecuada según las operaciones y el rendimiento esperado.

### Pregunta 4 — Programación orientada a objetos

**Pregunta:** ¿Qué experiencia tenés utilizando programación orientada a objetos para modelar y desarrollar aplicaciones?

- **Nivel 0:** No conozco la programación orientada a objetos.
- **Nivel 1:** Comprendo conceptos como clases, objetos, atributos y métodos, y para qué se utilizan.
- **Nivel 2:** Puedo crear clases y objetos sencillos, aplicar encapsulamiento y establecer relaciones básicas entre ellos.
- **Nivel 3:** Puedo modelar y desarrollar aplicaciones utilizando encapsulamiento, interfaces, herencia o polimorfismo cuando corresponde, comprendiendo el propósito de cada decisión.
- **Nivel 4:** Puedo evaluar diferentes diseños orientados a objetos, identificar problemas de responsabilidad, cohesión o acoplamiento y mejorar el modelo utilizando relaciones y mecanismos adecuados.

### Pregunta 5 — Principios de diseño y arquitectura básica

**Pregunta:** ¿Qué experiencia tenés organizando la estructura de una aplicación para facilitar su desarrollo, mantenimiento y evolución?

- **Nivel 0:** No conozco principios para organizar o diseñar software.
- **Nivel 1:** Comprendo conceptos como modularidad, separación de responsabilidades y organización por capas.
- **Nivel 2:** Puedo organizar una aplicación sencilla en clases, funciones o módulos, diferenciando sus responsabilidades principales.
- **Nivel 3:** Puedo organizar una aplicación en componentes o capas con responsabilidades definidas, comprendiendo por qué resulta conveniente esa estructura.
- **Nivel 4:** Puedo evaluar alternativas de diseño, identificar problemas de acoplamiento o mantenimiento, justificar decisiones arquitectónicas básicas y mejorar la estructura de una aplicación.

### Pregunta 6 — Consultas SQL

**Pregunta:** ¿Qué experiencia tenés utilizando SQL para consultar, relacionar y modificar información en bases de datos relacionales?

- **Nivel 0:** No conozco SQL ni para qué se utiliza.
- **Nivel 1:** Comprendo qué es SQL y cómo permite consultar y modificar información almacenada en tablas.
- **Nivel 2:** Puedo realizar consultas sencillas para obtener, filtrar o modificar información y comprender los resultados obtenidos.
- **Nivel 3:** Tengo experiencia utilizando SQL para resolver necesidades de una aplicación, relacionar información de distintas tablas y verificar que las consultas produzcan los resultados esperados.
- **Nivel 4:** Puedo evaluar diferentes formas de resolver una consulta, identificar errores o problemas de rendimiento y mejorar las soluciones según las necesidades de la aplicación.

### Pregunta 7 — Modelado de bases de datos

**Pregunta:** ¿Qué experiencia tenés diseñando la estructura de una base de datos relacional a partir de los requisitos de una aplicación?

- **Nivel 0:** No conozco cómo se diseña una base de datos.
- **Nivel 1:** Comprendo qué son tablas, columnas, claves primarias y relaciones entre entidades.
- **Nivel 2:** Puedo diseñar tablas y relaciones sencillas, identificando entidades, atributos y claves.
- **Nivel 3:** Puedo diseñar un modelo relacional que represente las entidades, relaciones y restricciones de una aplicación, utilizando claves primarias y foráneas adecuadamente.
- **Nivel 4:** Puedo evaluar diferentes alternativas de modelado, identificar problemas de redundancia o integridad, aplicar normalización cuando corresponde y justificar decisiones según los requisitos.

### Pregunta 8 — Consumo de APIs REST

**Pregunta:** ¿Qué experiencia tenés integrando aplicaciones con servicios externos mediante APIs REST?

- **Nivel 0:** No conozco qué es una API.
- **Nivel 1:** Comprendo qué es una API REST y cómo permite intercambiar información entre aplicaciones mediante HTTP.
- **Nivel 2:** Puedo realizar solicitudes a una API existente, enviar parámetros sencillos y utilizar las respuestas obtenidas.
- **Nivel 3:** Puedo integrar APIs en una aplicación, enviar información, procesar respuestas y manejar errores habituales.
- **Nivel 4:** Puedo evaluar diferentes alternativas de integración, gestionar autenticación, paginación y fallos de comunicación, y diseñar soluciones robustas según las necesidades de la aplicación.

### Pregunta 9 — Desarrollo de APIs REST

**Pregunta:** ¿Qué experiencia tenés desarrollando APIs REST para exponer funcionalidades y datos de una aplicación?

- **Nivel 0:** No conozco cómo se desarrolla una API.
- **Nivel 1:** Comprendo qué son endpoints, métodos HTTP y respuestas JSON, y para qué se utilizan.
- **Nivel 2:** Puedo desarrollar endpoints sencillos para consultar o modificar información y comprender cómo se procesan las solicitudes.
- **Nivel 3:** Tengo experiencia desarrollando APIs REST con operaciones de consulta y modificación de datos, validaciones y respuestas HTTP adecuadas.
- **Nivel 4:** Puedo diseñar APIs con contratos claros, organizar responsabilidades, gestionar errores consistentemente y justificar decisiones para facilitar su integración y mantenimiento.

### Pregunta 10 — Implementación de reglas de negocio

**Pregunta:** ¿Qué experiencia tenés interpretando requisitos e implementando las reglas que determinan el comportamiento de una aplicación?

- **Nivel 0:** No conozco qué son las reglas de negocio.
- **Nivel 1:** Comprendo que las aplicaciones deben cumplir condiciones y restricciones definidas por sus requisitos.
- **Nivel 2:** Puedo implementar reglas sencillas, como validaciones o cálculos, y comprender cómo afectan al comportamiento de una aplicación.
- **Nivel 3:** Puedo interpretar requisitos e implementar reglas que involucran diferentes condiciones, entidades y operaciones, comprobando que el comportamiento sea correcto.
- **Nivel 4:** Puedo analizar reglas de mayor complejidad, identificar inconsistencias o casos límite y organizar la lógica para mantener la coherencia de los datos y facilitar cambios futuros.

### Pregunta 11 — Desarrollo de interfaces web

**Pregunta:** ¿Qué experiencia tenés desarrollando y adaptando interfaces web para que los usuarios puedan interactuar con una aplicación?

- **Nivel 0:** No conozco cómo se desarrolla una interfaz web.
- **Nivel 1:** Comprendo para qué sirven HTML, CSS y JavaScript, y cómo se relacionan para construir interfaces.
- **Nivel 2:** Puedo crear o modificar interfaces sencillas con elementos visuales e interacciones básicas, comprendiendo su funcionamiento.
- **Nivel 3:** Tengo experiencia desarrollando y adaptando interfaces con formularios, componentes reutilizables e integración con servicios, verificando su funcionamiento en diferentes tamaños de pantalla.
- **Nivel 4:** Puedo evaluar la organización y calidad de una interfaz, identificar problemas de accesibilidad, interacción, adaptación a dispositivos o mantenimiento y mejorar su estructura según los requisitos.

### Pregunta 12 — Gestión de estado

**Pregunta:** ¿Qué experiencia tenés gestionando los datos y cambios de estado que determinan el comportamiento de una interfaz?

- **Nivel 0:** No conozco qué significa gestionar el estado de una aplicación.
- **Nivel 1:** Comprendo que una interfaz puede cambiar según los datos y las acciones del usuario.
- **Nivel 2:** Puedo implementar estados sencillos, como actualizar formularios o mostrar y ocultar elementos, comprendiendo cómo afectan a la interfaz.
- **Nivel 3:** Puedo gestionar el estado de componentes, formularios y datos obtenidos de APIs, manteniendo la interfaz sincronizada con la información.
- **Nivel 4:** Puedo evaluar distintas estrategias de gestión de estado, organizar flujos de datos compartidos, identificar inconsistencias y mejorar el comportamiento o rendimiento de la aplicación.

### Pregunta 13 — Depuración y resolución de errores

**Pregunta:** ¿Qué nivel de capacidad tenés para investigar y corregir errores en el código de una aplicación?

- **Nivel 0:** No sé cómo investigar errores de programación.
- **Nivel 1:** Puedo reconocer mensajes de error, pero necesito ayuda para comprenderlos.
- **Nivel 2:** Puedo interpretar errores sencillos y realizar correcciones con orientación.
- **Nivel 3:** Puedo reproducir errores, investigar sus causas y corregir problemas habituales, verificando los resultados.
- **Nivel 4:** Puedo investigar errores complejos, utilizar herramientas de depuración y comprobar que las correcciones no generan nuevos problemas.

### Pregunta 14 — Git y control de versiones

**Pregunta:** ¿Qué experiencia tenés utilizando Git para gestionar cambios y colaborar en proyectos de software?

- **Nivel 0:** No conozco Git ni los sistemas de control de versiones.
- **Nivel 1:** Comprendo para qué sirve Git y conceptos como repositorios, commits y ramas.
- **Nivel 2:** Puedo registrar cambios, trabajar con repositorios y realizar operaciones básicas de control de versiones.
- **Nivel 3:** Puedo gestionar ramas, commits, merges y conflictos habituales durante el desarrollo de un proyecto.
- **Nivel 4:** Puedo seleccionar estrategias de trabajo con ramas, mantener un historial claro, resolver conflictos más complejos y recuperar cambios cuando aparecen problemas.

### Pregunta 15 — Integración y colaboración en proyectos

**Pregunta:** ¿Qué nivel de capacidad tenés para integrar tu trabajo con el de otras personas en un proyecto tecnológico?

**Aclaración:** La colaboración puede acreditarse mediante proyectos académicos, personales o independientes realizados con otras personas; no se exige empleo profesional.

- **Nivel 0:** No tengo conocimientos ni experiencia sobre trabajo colaborativo en proyectos.
- **Nivel 1:** Participé de manera limitada en un proyecto grupal, realizando tareas sencillas con orientación de otros integrantes.
- **Nivel 2:** Tengo experiencia trabajando en equipo sobre tareas asignadas, comunicando avances y siguiendo acuerdos del equipo.
- **Nivel 3:** Tengo experiencia trabajando en proyectos grupales, integrando componentes desarrollados por distintos integrantes, coordinando dependencias y resolviendo problemas habituales en conjunto.
- **Nivel 4:** Tengo experiencia práctica colaborando activamente en integraciones, detectando incompatibilidades, resolviendo problemas entre componentes y proponiendo mejoras para facilitar el trabajo conjunto.

### Pregunta 16 — Docker y contenedores

**Pregunta:** ¿Qué experiencia tenés utilizando contenedores para ejecutar y configurar aplicaciones y sus servicios?

- **Nivel 0:** No conozco Docker ni el concepto de contenedores.
- **Nivel 1:** Comprendo para qué sirven los contenedores y cómo permiten ejecutar aplicaciones en entornos configurados.
- **Nivel 2:** Puedo ejecutar contenedores existentes y realizar configuraciones sencillas para utilizar aplicaciones o servicios.
- **Nivel 3:** Tengo experiencia configurando y utilizando contenedores en proyectos, creando imágenes con Dockerfile y trabajando con Docker Compose.
- **Nivel 4:** Puedo resolver problemas de configuración, conectividad o ejecución de contenedores y optimizar su uso de forma autónoma.

### Pregunta 17 — Fundamentos y tipos de testing

**Pregunta:** ¿Qué nivel de conocimiento y aplicación tenés sobre los fundamentos y tipos de pruebas de software?

- **Nivel 0:** No conozco qué es el testing de software.
- **Nivel 1:** Comprendo para qué sirven las pruebas de software y por qué son importantes para detectar errores.
- **Nivel 2:** Puedo reconocer diferentes tipos de pruebas, como funcionales, de integración o regresión, y comprender cuándo pueden utilizarse.
- **Nivel 3:** Tengo experiencia aplicando diferentes tipos de pruebas según las funcionalidades y necesidades de una aplicación.
- **Nivel 4:** Puedo seleccionar y justificar los tipos de pruebas más adecuados según los requisitos, riesgos y características del sistema.

### Pregunta 18 — Diseño de casos de prueba

**Pregunta:** ¿Qué experiencia tenés diseñando casos de prueba para verificar que una aplicación cumpla con sus requisitos?

- **Nivel 0:** No conozco qué es un caso de prueba ni cómo se diseña.
- **Nivel 1:** Comprendo qué es un caso de prueba y cómo se utiliza para verificar el funcionamiento de una aplicación.
- **Nivel 2:** Puedo diseñar casos de prueba sencillos a partir de requisitos o funcionalidades, identificando las acciones a realizar y los resultados esperados.
- **Nivel 3:** Tengo experiencia diseñando casos de prueba para distintas funcionalidades, contemplando escenarios habituales, errores posibles y casos límite.
- **Nivel 4:** Tengo experiencia diseñando conjuntos de casos de prueba, identificando escenarios no contemplados, priorizando pruebas según los riesgos y mejorando su cobertura.

### Pregunta 19 — Ejecución y documentación de pruebas

**Pregunta:** ¿Qué experiencia tenés ejecutando pruebas y documentando sus resultados para verificar el funcionamiento de una aplicación?

- **Nivel 0:** No conozco cómo se ejecutan ni documentan pruebas de software.
- **Nivel 1:** Comprendo cómo se ejecuta una prueba y por qué es importante registrar sus resultados.
- **Nivel 2:** Puedo ejecutar pruebas sencillas siguiendo casos definidos y registrar si los resultados coinciden con los esperados.
- **Nivel 3:** Tengo experiencia ejecutando pruebas sobre distintas funcionalidades, documentando resultados, identificando comportamientos incorrectos y verificando correcciones.
- **Nivel 4:** Tengo experiencia organizando y ejecutando conjuntos de pruebas, manteniendo registros claros, realizando pruebas de regresión y proponiendo mejoras al proceso de validación.

### Pregunta 20 — Gestión de defectos

**Pregunta:** ¿Qué experiencia tenés identificando, reportando y realizando el seguimiento de defectos encontrados durante las pruebas de software?

- **Nivel 0:** No conozco cómo se identifican ni reportan defectos de software.
- **Nivel 1:** Comprendo qué es un defecto y por qué es importante comunicarlo y documentarlo.
- **Nivel 2:** Puedo identificar y reportar defectos sencillos, describiendo el problema, los pasos para reproducirlo y el resultado esperado.
- **Nivel 3:** Tengo experiencia registrando defectos con información suficiente para reproducirlos, realizando su seguimiento y verificando que las correcciones resuelvan los problemas detectados.
- **Nivel 4:** Tengo experiencia gestionando defectos de diferentes características, evaluando su impacto, colaborando con desarrolladores para facilitar su resolución y verificando que las correcciones no introduzcan nuevos problemas.

### Pregunta 21 — Pruebas de APIs

**Pregunta:** ¿Qué experiencia tenés probando APIs para verificar que sus endpoints funcionen correctamente?

- **Nivel 0:** No conozco cómo se prueba una API.
- **Nivel 1:** Comprendo qué es una API y cómo se pueden verificar sus solicitudes y respuestas.
- **Nivel 2:** Puedo realizar solicitudes sencillas a una API, utilizando herramientas como Postman, e interpretar las respuestas obtenidas.
- **Nivel 3:** Tengo experiencia probando endpoints de APIs, verificando métodos HTTP, códigos de respuesta, datos devueltos y comportamientos ante entradas válidas e inválidas.
- **Nivel 4:** Tengo experiencia diseñando y ejecutando pruebas de APIs que contemplan autenticación, validaciones, errores y distintos escenarios de integración, identificando problemas y proponiendo mejoras en su funcionamiento.

### Pregunta 22 — Análisis exploratorio de datos

**Pregunta:** ¿Qué nivel de capacidad tenés para explorar conjuntos de datos e identificar información relevante?

- **Nivel 0:** No conozco cómo se realiza un análisis exploratorio de datos.
- **Nivel 1:** Comprendo para qué sirve explorar datos y cómo permite identificar características, patrones o tendencias.
- **Nivel 2:** Puedo explorar conjuntos de datos sencillos, utilizando filtros, agrupaciones y estadísticas descriptivas para obtener información básica.
- **Nivel 3:** Tengo experiencia realizando análisis exploratorios sobre conjuntos de datos, identificando patrones, tendencias, valores atípicos y relaciones relevantes.
- **Nivel 4:** Tengo experiencia analizando conjuntos de datos de mayor complejidad, seleccionando técnicas de exploración adecuadas, contrastando hallazgos y justificando las conclusiones obtenidas.

### Pregunta 23 — Limpieza y preparación de datos

**Pregunta:** ¿Qué nivel de capacidad tenés para identificar y corregir problemas en conjuntos de datos?

- **Nivel 0:** No conozco cómo se limpian o preparan conjuntos de datos.
- **Nivel 1:** Comprendo que los datos pueden contener errores, valores faltantes, duplicados o formatos inconsistentes que afectan su análisis.
- **Nivel 2:** Puedo identificar y corregir problemas sencillos, como registros duplicados, valores faltantes o formatos incorrectos, utilizando herramientas de análisis de datos.
- **Nivel 3:** Tengo experiencia limpiando y preparando conjuntos de datos, identificando inconsistencias, aplicando transformaciones y verificando que la información sea adecuada para su análisis.
- **Nivel 4:** Tengo experiencia preparando conjuntos de datos de mayor complejidad, evaluando distintas estrategias de limpieza, identificando problemas de calidad y justificando las transformaciones realizadas.

### Pregunta 24 — Visualización de datos y dashboards

**Pregunta:** ¿Qué nivel de capacidad tenés para representar información mediante gráficos y dashboards?

- **Nivel 0:** No conozco herramientas ni técnicas de visualización de datos.
- **Nivel 1:** Comprendo para qué sirven los gráficos y dashboards y cómo permiten presentar información.
- **Nivel 2:** Puedo crear gráficos sencillos a partir de conjuntos de datos y comprender qué información representan.
- **Nivel 3:** Tengo experiencia creando visualizaciones y dashboards para presentar indicadores, comparar resultados e identificar tendencias.
- **Nivel 4:** Puedo seleccionar visualizaciones adecuadas según los datos y objetivos, organizar dashboards e identificar y corregir problemas que dificultan la interpretación de la información.

### Pregunta 25 — Estadística aplicada

**Pregunta:** ¿Qué nivel de conocimiento y aplicación tenés sobre conceptos estadísticos para analizar e interpretar datos?

- **Nivel 0:** No conozco conceptos básicos de estadística.
- **Nivel 1:** Comprendo conceptos como promedio, mediana, porcentajes y variabilidad.
- **Nivel 2:** Puedo aplicar medidas estadísticas sencillas para resumir conjuntos de datos e interpretar sus resultados.
- **Nivel 3:** Tengo experiencia utilizando técnicas estadísticas descriptivas para analizar datos, comparar resultados e identificar tendencias o diferencias relevantes.
- **Nivel 4:** Puedo seleccionar métodos estadísticos adecuados, interpretar sus resultados, reconocer limitaciones y evitar conclusiones incorrectas a partir de los datos.

### Pregunta 26 — Interpretación de métricas de negocio

**Pregunta:** ¿Qué nivel de capacidad tenés para interpretar indicadores y relacionarlos con objetivos de negocio?

- **Nivel 0:** No conozco qué son las métricas o indicadores de negocio.
- **Nivel 1:** Comprendo qué son los indicadores de negocio y cómo permiten evaluar resultados en relación con determinados objetivos.
- **Nivel 2:** Puedo interpretar indicadores sencillos, identificar variaciones y explicar qué representan sus resultados.
- **Nivel 3:** Tengo experiencia analizando métricas de negocio, relacionándolas con objetivos, identificando tendencias y comunicando conclusiones basadas en los datos.
- **Nivel 4:** Tengo experiencia evaluando diferentes indicadores, identificando relaciones y posibles causas de variaciones, contrastando interpretaciones y presentando conclusiones fundamentadas que contribuyan a la toma de decisiones.

### Pregunta 27 — Fundamentos de Cloud Computing

**Pregunta:** ¿Qué nivel de conocimiento y experiencia tenés utilizando servicios de computación en la nube, como AWS, Azure o Google Cloud?

- **Nivel 0:** No conozco qué es la computación en la nube.
- **Nivel 1:** Comprendo qué es cloud computing y para qué se utilizan sus principales tipos de servicios.
- **Nivel 2:** Conozco los servicios básicos de una plataforma cloud y puedo realizar configuraciones o tareas sencillas con orientación.
- **Nivel 3:** Tengo experiencia utilizando y configurando servicios cloud para desplegar aplicaciones, almacenar información o administrar recursos básicos.
- **Nivel 4:** Tengo experiencia utilizando diferentes servicios cloud, configurando recursos de manera autónoma, resolviendo problemas habituales y tomando decisiones básicas de seguridad, disponibilidad y costos.

### Pregunta 28 — Linux y sistemas operativos

**Pregunta:** ¿Qué nivel de conocimiento y aplicación tenés sobre Linux y la administración básica de sistemas operativos?

- **Nivel 0:** No conozco Linux ni conceptos básicos de administración de sistemas operativos.
- **Nivel 1:** Reconozco conceptos como archivos, directorios, usuarios, permisos y procesos.
- **Nivel 2:** Puedo utilizar comandos básicos para navegar por directorios, gestionar archivos y consultar información del sistema.
- **Nivel 3:** Tengo experiencia utilizando Linux para administrar archivos, permisos, procesos y servicios, y realizar configuraciones habituales.
- **Nivel 4:** Tengo experiencia administrando entornos Linux, diagnosticando problemas de permisos, procesos o servicios y resolviendo incidentes habituales del sistema.

### Pregunta 29 — Fundamentos de redes

**Pregunta:** ¿Qué nivel de conocimiento y aplicación tenés sobre redes y comunicación entre sistemas?

- **Nivel 0:** No conozco los fundamentos de redes.
- **Nivel 1:** Reconozco conceptos básicos como direcciones IP, puertos y protocolos de comunicación.
- **Nivel 2:** Comprendo cómo se comunican los sistemas a través de una red y puedo interpretar situaciones sencillas de conectividad.
- **Nivel 3:** Tengo experiencia utilizando herramientas de red para verificar conexiones, realizar pruebas de comunicación e identificar problemas habituales.
- **Nivel 4:** Tengo experiencia configurando conexiones de red, interpretando diagnósticos y resolviendo problemas habituales de comunicación entre sistemas.

### Pregunta 30 — Automatización de tareas mediante scripts (Bash, PowerShell o Python)

**Pregunta:** ¿Qué nivel de experiencia tenés creando pequeños programas o scripts para automatizar tareas repetitivas?

- **Nivel 0:** No sé qué es un script ni cómo se utiliza.
- **Nivel 1:** Entiendo qué es un script y para qué sirve, pero todavía no sé crear uno.
- **Nivel 2:** Puedo ejecutar y modificar scripts sencillos para automatizar tareas básicas.
- **Nivel 3:** Puedo crear scripts para automatizar tareas concretas, como procesar archivos, ejecutar comandos o realizar verificaciones.
- **Nivel 4:** Puedo desarrollar, depurar y mejorar scripts de forma autónoma, incluyendo manejo de errores y situaciones imprevistas.

### Pregunta 31 — Monitoreo y diagnóstico de infraestructura

**Pregunta:** ¿Qué nivel de capacidad tenés para supervisar el funcionamiento de sistemas e identificar problemas de infraestructura?

- **Nivel 0:** No conozco herramientas ni conceptos de monitoreo de sistemas.
- **Nivel 1:** Comprendo para qué sirve monitorear recursos como CPU, memoria, almacenamiento o disponibilidad.
- **Nivel 2:** Puedo consultar métricas y registros sencillos para identificar el estado de un sistema.
- **Nivel 3:** Tengo experiencia utilizando herramientas de monitoreo, interpretando métricas y registros e identificando problemas habituales de rendimiento o disponibilidad.
- **Nivel 4:** Tengo experiencia configurando monitoreo y alertas, investigando incidentes y proponiendo mejoras para prevenir o resolver problemas de infraestructura.

## Matriz de requisitos por rol

**E1–E4**: esencial con nivel esperado. **C1–C4**: complementaria con nivel esperado. **—**: no considerada. Pesos: esencial 2; complementaria 1. La matriz V2 vigente utiliza actualmente niveles esperados 1–3; la elegibilidad de nivel 4 no modifica sus asignaciones.

| Nº | Competencia | Backend | Frontend | Full Stack | QA | Data Analyst | Cloud |
|---:|---|---:|---:|---:|---:|---:|---:|
| 1 | Fundamentos de programación | E3 | E3 | E3 | C2 | C1 | C2 |
| 2 | Algoritmos y resolución eficiente de problemas | E2 | E2 | E2 | C1 | C1 | C1 |
| 3 | Estructuras de datos | E3 | E2 | E3 | — | C2 | — |
| 4 | Programación orientada a objetos | E3 | C2 | E3 | — | — | — |
| 5 | Principios de diseño y arquitectura básica | E2 | E2 | E2 | — | — | C2 |
| 6 | Consultas SQL | E3 | — | E2 | C1 | E3 | C1 |
| 7 | Modelado de bases de datos | E3 | — | E3 | — | C2 | C1 |
| 8 | Consumo de APIs REST | E2 | E3 | E3 | E2 | C1 | C1 |
| 9 | Desarrollo de APIs REST | E3 | C1 | E3 | C1 | — | — |
| 10 | Implementación de reglas de negocio | E3 | C2 | E3 | C2 | C1 | — |
| 11 | Desarrollo de interfaces web | — | E3 | E3 | C1 | — | — |
| 12 | Gestión de estado | — | E3 | E3 | — | — | — |
| 13 | Depuración y resolución de errores | E3 | E3 | E3 | C2 | C2 | E2 |
| 14 | Git y control de versiones | E3 | E3 | E3 | C2 | C1 | C2 |
| 15 | Integración y colaboración en proyectos | E2 | E2 | E2 | E2 | E2 | E2 |
| 16 | Docker y contenedores | C2 | C1 | C2 | — | — | E2 |
| 17 | Fundamentos y tipos de testing | E2 | E2 | E2 | E3 | — | C1 |
| 18 | Diseño de casos de prueba | C1 | C1 | C1 | E3 | — | — |
| 19 | Ejecución y documentación de pruebas | C1 | C1 | C1 | E3 | C1 | C1 |
| 20 | Gestión de defectos | C1 | C1 | C1 | E3 | — | C1 |
| 21 | Pruebas de APIs | E2 | C2 | E2 | E3 | — | C1 |
| 22 | Análisis exploratorio de datos | — | — | — | — | E3 | C1 |
| 23 | Limpieza y preparación de datos | — | — | — | — | E3 | — |
| 24 | Visualización de datos y dashboards | — | — | — | — | E3 | C1 |
| 25 | Estadística aplicada | — | — | — | — | E3 | — |
| 26 | Interpretación de métricas de negocio | C1 | C1 | C1 | C1 | E3 | C1 |
| 27 | Fundamentos de Cloud Computing | C1 | C1 | C1 | — | C1 | E3 |
| 28 | Linux y sistemas operativos | C1 | — | C1 | — | — | E3 |
| 29 | Fundamentos de redes | C1 | — | C1 | C1 | — | E3 |
| 30 | Automatización de tareas mediante scripts (Bash, PowerShell o Python) | C1 | — | C1 | C1 | C2 | E3 |
| 31 | Monitoreo y diagnóstico de infraestructura | C2 | — | C1 | C1 | — | E3 |

## Cambios aprobados consolidados

- Se incorpora la matriz definitiva de 31 competencias y seis roles para NextRole V2.
- Se preservan las modificaciones aprobadas de las competencias 2, 3, 15, 16 y 30; las demás preguntas no se alteran.
- Esta matriz V2 sustituye la matriz histórica V1 como referencia vigente.

## Diferencias documentales conocidas

- La propuesta histórica menciona perfiles adicionales y rotula su matriz como V1; el alcance definitivo V2 es de seis roles.
- Los niveles esperados admitidos para competencias relevantes son 1–4. Esta matriz definitiva mantiene sus asignaciones aprobadas actuales, que solo usan niveles 1–3.
- Ante contradicciones, prevalecen las decisiones V2 y este catálogo/matriz vigentes sobre los PDF.
