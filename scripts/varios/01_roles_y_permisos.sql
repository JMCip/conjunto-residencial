-- =====================================================================
-- Rol de aplicacion (backend) con permisos CRUD
-- Ejecutar como superusuario (postgres).
-- =====================================================================

-- Si el rol ya existe con otro nombre (ej. app_lectura), renombrarlo:
-- ALTER ROLE app_lectura RENAME TO app_backend;

-- Si no existe aun, crearlo desde cero:
-- CREATE ROLE app_backend LOGIN PASSWORD 'cambiar_esta_clave';

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_backend;

ALTER DEFAULT PRIVILEGES FOR ROLE admin_conjunto IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_backend;
