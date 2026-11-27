-- Explainability foundations. V1-V6 and the V1 roles catalog remain unchanged.

ALTER TABLE assessment_roles ADD COLUMN description TEXT;

UPDATE assessment_roles
   SET name = CASE id
       WHEN 'backend-dev-jr' THEN 'Backend Developer'
       WHEN 'frontend-dev-jr' THEN 'Frontend Developer'
       WHEN 'fullstack-dev-jr' THEN 'Full Stack Developer'
       WHEN 'qa-jr' THEN 'QA'
       WHEN 'data-analyst-jr' THEN 'Data Analyst'
       WHEN 'cloud-jr' THEN 'Cloud'
       ELSE name
   END,
       description = CASE id
       WHEN 'backend-dev-jr' THEN 'Se encarga de desarrollar y mantener la lógica interna de las aplicaciones. Sus responsabilidades incluyen implementar reglas de negocio, desarrollar APIs, gestionar bases de datos e integrar servicios externos. Contribuye a que los sistemas procesen información de manera segura, eficiente y confiable.'
       WHEN 'frontend-dev-jr' THEN 'Se encarga de desarrollar las interfaces visuales e interactivas de las aplicaciones. Sus responsabilidades incluyen construir componentes, integrar APIs y gestionar el comportamiento de la interfaz. Busca garantizar una experiencia de usuario intuitiva, accesible, eficiente y adaptable a diferentes dispositivos.'
       WHEN 'fullstack-dev-jr' THEN 'Participa en el desarrollo integral de aplicaciones, trabajando tanto en sus interfaces como en su lógica interna. Sus responsabilidades incluyen construir funcionalidades, desarrollar e integrar APIs, gestionar bases de datos y conectar los distintos componentes del sistema. Su visión integral le permite comprender cómo interactúan las diferentes capas de una aplicación.'
       WHEN 'qa-jr' THEN 'Se encarga de contribuir a la calidad del software mediante la planificación, el diseño y la ejecución de pruebas. Sus responsabilidades incluyen verificar funcionalidades, identificar y documentar defectos, validar correcciones y comprobar que las aplicaciones cumplan sus requisitos. Colabora con los equipos de desarrollo para mejorar la confiabilidad del producto y prevenir errores.'
       WHEN 'data-analyst-jr' THEN 'Se encarga de transformar datos en información útil para la toma de decisiones. Sus responsabilidades incluyen obtener, limpiar, procesar y analizar datos, identificar patrones e interpretar indicadores de negocio. Utiliza técnicas estadísticas y herramientas de visualización para comunicar resultados, fundamentar conclusiones y contribuir a la resolución de problemas.'
       WHEN 'cloud-jr' THEN 'Se encarga de implementar, administrar y mantener infraestructura y servicios en la nube. Sus responsabilidades incluyen configurar recursos de procesamiento, almacenamiento y redes, automatizar tareas operativas y monitorear el funcionamiento de los sistemas. Contribuye a que las soluciones sean seguras, confiables, escalables y eficientes en el uso de recursos.'
       ELSE NULL
   END;

ALTER TABLE assessment_roles ALTER COLUMN description SET NOT NULL;

CREATE TABLE explainability_diagnostics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES assessment_attempts(id),
    role_id VARCHAR(100) NOT NULL REFERENCES assessment_roles(id),
    status VARCHAR(16) NOT NULL CHECK (status IN ('GENERATING', 'SUCCEEDED', 'FAILED')),
    generation_token UUID,
    lease_until TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    role_name_snapshot VARCHAR(180),
    role_description_snapshot TEXT,
    diagnosis_content JSONB,
    provider VARCHAR(40),
    model_requested VARCHAR(180),
    model_served VARCHAR(180),
    prompt_version VARCHAR(80),
    prompt_hash CHAR(64),
    context_hash CHAR(64),
    duration_ms BIGINT CHECK (duration_ms IS NULL OR duration_ms >= 0),
    input_tokens BIGINT CHECK (input_tokens IS NULL OR input_tokens >= 0),
    output_tokens BIGINT CHECK (output_tokens IS NULL OR output_tokens >= 0),
    reasoning_tokens BIGINT CHECK (reasoning_tokens IS NULL OR reasoning_tokens >= 0),
    provider_cost NUMERIC CHECK (provider_cost IS NULL OR provider_cost >= 0),
    failure_code VARCHAR(60),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    succeeded_at TIMESTAMPTZ,
    CONSTRAINT uq_explainability_attempt_role UNIQUE (attempt_id, role_id),
    CONSTRAINT ck_explainability_diagnosis_object
        CHECK (diagnosis_content IS NULL OR jsonb_typeof(diagnosis_content) = 'object'),
    CONSTRAINT ck_explainability_status_payload CHECK (
        (status = 'GENERATING' AND generation_token IS NOT NULL AND lease_until IS NOT NULL
            AND diagnosis_content IS NULL AND succeeded_at IS NULL)
        OR (status = 'FAILED' AND generation_token IS NOT NULL AND lease_until IS NULL
            AND diagnosis_content IS NULL AND succeeded_at IS NULL)
        OR (status = 'SUCCEEDED' AND generation_token IS NOT NULL AND lease_until IS NULL AND diagnosis_content IS NOT NULL
            AND role_name_snapshot IS NOT NULL AND length(trim(role_name_snapshot)) > 0
            AND role_description_snapshot IS NOT NULL AND length(trim(role_description_snapshot)) > 0
            AND provider IS NOT NULL AND model_requested IS NOT NULL AND prompt_version IS NOT NULL
            AND prompt_hash IS NOT NULL AND context_hash IS NOT NULL AND succeeded_at IS NOT NULL
            AND failure_code IS NULL)
    )
);

CREATE INDEX ix_explainability_status_lease
    ON explainability_diagnostics(status, lease_until)
    WHERE status = 'GENERATING';
