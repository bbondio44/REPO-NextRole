-- La migración inicial define token_hash como CHAR(64), pero el campo
-- Java es String (VARCHAR en Hibernate). Mantener V1 inmutable y corregir
-- el esquema de forma incremental para bases ya migradas.
ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE VARCHAR(64);
