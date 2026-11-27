-- Publish an immutable questionnaire snapshot with the ten approved copy changes.
-- Q1, pinned attempts, and matrix M1 remain unchanged.
DO $$
DECLARE
    q1_id UUID;
    question_count INTEGER;
    option_count INTEGER;
    matrix_count INTEGER;
BEGIN
    SELECT id INTO q1_id
      FROM assessment_questionnaire_versions
     WHERE version_code = 'Q1';

    IF q1_id IS NULL THEN
        RAISE EXCEPTION 'Cannot publish Q2: questionnaire Q1 is missing';
    END IF;

    SELECT count(*) INTO question_count
      FROM assessment_questions
     WHERE questionnaire_version_id = q1_id;
    SELECT count(*) INTO option_count
      FROM assessment_answer_options
     WHERE questionnaire_version_id = q1_id;
    SELECT count(*) INTO matrix_count
      FROM assessment_role_requirements r
      JOIN assessment_matrix_versions v ON v.id = r.matrix_version_id
     WHERE v.version_code = 'M1';

    IF question_count <> 31 OR option_count <> 155 OR matrix_count <> 186 THEN
        RAISE EXCEPTION 'Cannot publish Q2: expected Q1 31/155 and M1 186; found %/% and %',
            question_count, option_count, matrix_count;
    END IF;
END $$;

UPDATE assessment_questionnaire_versions
   SET is_current = FALSE
 WHERE version_code = 'Q1' AND is_current = TRUE;

INSERT INTO assessment_questionnaire_versions(id, version_code, published_at, is_current)
VALUES ('00000000-0000-0000-0000-000000000003', 'Q2', now(), TRUE);

INSERT INTO assessment_questions(questionnaire_version_id, competency_code, position, title, question_text)
SELECT '00000000-0000-0000-0000-000000000003', competency_code, position, title, question_text
  FROM assessment_questions
 WHERE questionnaire_version_id = '00000000-0000-0000-0000-000000000001'
 ORDER BY position;

INSERT INTO assessment_answer_options(questionnaire_version_id, competency_code, level, description)
SELECT '00000000-0000-0000-0000-000000000003', competency_code, level,
       CASE
           WHEN competency_code = 'rest-api-consumption' AND level = 0
               THEN 'No comprendo cómo se realizan solicitudes a una API REST existente ni cómo se utiliza la información que devuelve.'
           WHEN competency_code = 'rest-api-consumption' AND level = 1
               THEN 'Comprendo cómo una aplicación realiza solicitudes HTTP a una API REST existente y utiliza las respuestas obtenidas.'
           WHEN competency_code = 'rest-api-development' AND level = 1
               THEN 'Comprendo cómo funciona una API REST desde la perspectiva de su desarrollo y qué función cumplen los endpoints, los métodos HTTP y las respuestas JSON.'
           WHEN competency_code = 'api-testing' AND level = 1
               THEN 'Comprendo cómo se verifica el comportamiento de una API y qué aspectos se deben comprobar al probar sus endpoints.'
           WHEN competency_code = 'api-testing' AND level = 2
               THEN 'Puedo enviar solicitudes sencillas a una API utilizando herramientas como Postman y comprobar si la respuesta coincide con un resultado esperado simple.'
           WHEN competency_code = 'programming-fundamentals' AND level = 3
               THEN 'Tengo experiencia desarrollando programas para resolver problemas habituales, organizando la lógica en funciones y verificando que los resultados obtenidos sean los esperados.'
           WHEN competency_code = 'programming-fundamentals' AND level = 4
               THEN 'Puedo comparar diferentes formas de resolver un problema, evaluar la lógica y organización del código, y justificar las decisiones de implementación tomadas.'
           WHEN competency_code = 'object-oriented-programming' AND level = 4
               THEN 'Puedo evaluar el diseño de clases y objetos, identificar problemas en la distribución de responsabilidades, cohesión o acoplamiento, y mejorar el modelo utilizando abstracciones y relaciones adecuadas.'
           WHEN competency_code = 'web-interface-development' AND level = 3
               THEN 'Tengo experiencia desarrollando y adaptando interfaces con formularios, componentes reutilizables e interacciones, verificando su funcionamiento y visualización en diferentes tamaños de pantalla.'
           WHEN competency_code = 'data-cleaning-preparation' AND level = 2
               THEN 'Puedo identificar problemas sencillos, como duplicados, valores faltantes o formatos incorrectos, y aplicar un tratamiento apropiado con herramientas de análisis de datos.'
           ELSE description
       END
  FROM assessment_answer_options
 WHERE questionnaire_version_id = '00000000-0000-0000-0000-000000000001'
 ORDER BY competency_code, level;
