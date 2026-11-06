# Scripts de implementación del modelo lógico al modelo físico

Orden de ejecución recomendado sobre una base de datos PostgreSQL vacía.

## 1. `ddl/` — Scripts de creación de objetos

Ejecutar en este orden, conectado como `admin_conjunto` (o el rol dueño del esquema):

1. `DDL_PostgreSQL_conjunto_residencial.sql` — crea las 21 tablas, llaves primarias, foráneas y el modelo ISA (herencia de Persona).
2. `DDL restricciones RF04 y S9.sql` — agrega las restricciones de negocio: un solo contrato de arriendo VIGENTE por casa (RF-04) y no permitir reservas de salón con horario cruzado (S-9).

```bash
psql -U admin_conjunto -d conjunto_residencial -f ddl/DDL_PostgreSQL_conjunto_residencial.sql
psql -U admin_conjunto -d conjunto_residencial -f "ddl/DDL restricciones RF04 y S9.sql"
```

## 2. `dml/` — Scripts DML: inserción de registros y consultas

DML (Data Manipulation Language) incluye tanto `INSERT` como `SELECT`. Por eso ambos viven en la misma carpeta, en dos bloques:

**Inserción de datos de prueba** (en orden, cada uno depende de tablas pobladas por el anterior):
```bash
psql -U admin_conjunto -d conjunto_residencial -f dml/01_personas_casas.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/02_parqueadero.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/03_salon_comunal.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/04_sanciones.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/05_finanzas.sql
```

**Consultas de verificación** (`SELECT`, no modifican nada — se corren después de cargar los datos anteriores):
- `06_consulta_personas_casas.sql` — residentes por casa, casas desocupadas, posesión de propietarios, teléfonos.
- `07_consulta_parqueadero.sql` — celdas disponibles, vehículos por persona, celdas asignadas.
- `08_consulta_salon_comunal.sql` — reservas confirmadas del salón comunal.
- `09_consulta_sanciones.sql` — sanciones registradas y su valor por casa.
- `10_consulta_finanzas.sql` — facturas pendientes (usando `factura_con_total`), historial de pagos, detalle de factura, morosidad por casa.

```bash
psql -U admin_conjunto -d conjunto_residencial -f dml/06_consulta_personas_casas.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/07_consulta_parqueadero.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/08_consulta_salon_comunal.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/09_consulta_sanciones.sql
psql -U admin_conjunto -d conjunto_residencial -f dml/10_consulta_finanzas.sql
```

## 3. `varios/` — Scripts varios (roles, permisos y ajustes del modelo físico)

Ejecutar después de que el esquema y los datos ya existen:

1. `01_roles_y_permisos.sql` — crea/ajusta el rol `app_backend` que usa el aplicativo web (permisos CRUD, sin DDL ni `TRUNCATE`). **Ejecutar como superusuario (`postgres`)**, no como `admin_conjunto`.
2. `02_vista_factura_total.sql` — reemplaza la columna `factura.total` (atributo derivado, riesgo de quedar desincronizado) por la vista `factura_con_total`, que lo calcula sumando `detalle_factura`. Ejecutar como `admin_conjunto`.

```bash
sudo -u postgres psql -d conjunto_residencial -f varios/01_roles_y_permisos.sql
psql -U admin_conjunto -d conjunto_residencial -f varios/02_vista_factura_total.sql
```

> **Importante:** después de ejecutar `02_vista_factura_total.sql`, el backend debe consultar `factura_con_total` en lugar de `factura` para obtener el total — la tabla `factura` ya no tiene esa columna.
