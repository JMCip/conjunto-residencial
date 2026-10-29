
-- DDL PostgreSQL — Proyecto "Gestión de un Conjunto Residencial"
-- El script de Oracle SQL Developer Data Modeler se paso a PostgreSQL

-- ----------------------------------------------------------------------------
-- TABLAS
-- ----------------------------------------------------------------------------

CREATE TABLE persona (
    tipo_documento    VARCHAR(10)  NOT NULL,
    numero_documento  VARCHAR(20)  NOT NULL,
    correo            VARCHAR(150),
    primer_nombre     VARCHAR(20)  NOT NULL,
    segundo_nombre    VARCHAR(20),
    primer_apellido   VARCHAR(20)  NOT NULL,
    segundo_apellido  VARCHAR(20),
    CONSTRAINT persona_pk PRIMARY KEY (tipo_documento, numero_documento)
);

CREATE TABLE propietario (
    tipo_documento    VARCHAR(10)  NOT NULL,
    numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT propietario_pk PRIMARY KEY (tipo_documento, numero_documento)
);

CREATE TABLE arrendatario (
    tipo_documento    VARCHAR(10)  NOT NULL,
    numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT arrendatario_pk PRIMARY KEY (tipo_documento, numero_documento)
);

CREATE TABLE residente (
    tipo_documento    VARCHAR(10)  NOT NULL,
    numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT residente_pk PRIMARY KEY (tipo_documento, numero_documento)
);

CREATE TABLE visitante (
    tipo_documento    VARCHAR(10)  NOT NULL,
    numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT visitante_pk PRIMARY KEY (tipo_documento, numero_documento)
);

CREATE TABLE telefono (
    telefono                  NUMERIC(20)  NOT NULL,
    persona_tipo_documento    VARCHAR(10)  NOT NULL,
    persona_numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT telefono_pk PRIMARY KEY (telefono)
);

CREATE TABLE vehiculo (
    persona_tipo_documento    VARCHAR(10)  NOT NULL,
    persona_numero_documento  VARCHAR(20)  NOT NULL,
    placa                     VARCHAR(10)  NOT NULL,
    marca                     VARCHAR(20),
    modelo                    INTEGER,
    tipo                      VARCHAR(10)  NOT NULL,
    CONSTRAINT vehiculo_pk PRIMARY KEY (placa)
);

CREATE TABLE casa (
    numero                NUMERIC  NOT NULL,
    manzana               NUMERIC  NOT NULL,
    area                  NUMERIC(8,2)  NOT NULL,
    estado                VARCHAR(15)  NOT NULL,
    numero_habitaciones   INTEGER,
    CONSTRAINT casa_pk PRIMARY KEY (numero, manzana)
);

CREATE TABLE posesion (
    id_posesion                   INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    porcentaje                    NUMERIC(5,2)  NOT NULL,
    fecha_compra                  DATE  NOT NULL,
    fecha_venta                   DATE,
    casa_numero                   NUMERIC  NOT NULL,
    casa_manzana                  NUMERIC  NOT NULL,
    propietario_tipo_documento    VARCHAR(10)  NOT NULL,
    propietario_numero_documento  VARCHAR(20)  NOT NULL,
    CONSTRAINT posesion_pk PRIMARY KEY (id_posesion)
);

CREATE TABLE arriendo (
    id_contrato                    INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    cuota_mensual                  NUMERIC(12,2)  NOT NULL,
    fecha_fin                      DATE,
    fecha_inicio                   DATE  NOT NULL,
    estado_contrato                VARCHAR(20)  NOT NULL,
    arrendatario_tipo_documento    VARCHAR(10)  NOT NULL,
    arrendatario_numero_documento  VARCHAR(20)  NOT NULL,
    casa_numero                    NUMERIC  NOT NULL,
    casa_manzana                   NUMERIC  NOT NULL,
    propietario_tipo_documento     VARCHAR(10)  NOT NULL,
    propietario_numero_documento   VARCHAR(20)  NOT NULL,
    CONSTRAINT arriendo_pk PRIMARY KEY (id_contrato)
);

CREATE TABLE residencia (
    id_residencia               INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_ingreso                DATE  NOT NULL,
    fecha_salida                 DATE,
    residente_tipo_documento     VARCHAR(10)  NOT NULL,
    residente_numero_documento   VARCHAR(20)  NOT NULL,
    casa_numero                  NUMERIC  NOT NULL,
    casa_manzana                 NUMERIC  NOT NULL,
    CONSTRAINT residencia_pk PRIMARY KEY (id_residencia)
);

CREATE TABLE visita (
    id_visita                   INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_entrada                DATE  NOT NULL,
    fecha_salida                 DATE,
    visitante_tipo_documento     VARCHAR(10)  NOT NULL,
    visitante_numero_documento   VARCHAR(20)  NOT NULL,
    casa_numero                  NUMERIC  NOT NULL,
    casa_manzana                 NUMERIC  NOT NULL,
    CONSTRAINT visita_pk PRIMARY KEY (id_visita)
);

CREATE TABLE celda_parqueadero (
    id_celda   INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    tipo       VARCHAR(20)  NOT NULL,
    estado     VARCHAR(20)  NOT NULL,
    CONSTRAINT celda_parqueadero_pk PRIMARY KEY (id_celda)
);

CREATE TABLE asignacion_celda (
    id_asignacion                 INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_inicio                  DATE  NOT NULL,
    fecha_fin                     DATE,
    casa_numero                   NUMERIC  NOT NULL,
    casa_manzana                  NUMERIC  NOT NULL,
    celda_parqueadero_id_celda    INTEGER  NOT NULL,
    CONSTRAINT asignacion_celda_pk PRIMARY KEY (id_asignacion)
);

CREATE TABLE parqueo (
    id_parqueo                  INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_hora_entrada           TIMESTAMP  NOT NULL,
    fecha_hora_salida            TIMESTAMP,
    vehiculo_placa                VARCHAR(10)  NOT NULL,
    celda_parqueadero_id_celda    INTEGER  NOT NULL,
    CONSTRAINT parqueo_pk PRIMARY KEY (id_parqueo)
);

CREATE TABLE reserva_salon (
    id_reserva       INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_reserva     DATE  NOT NULL,
    hora_inicio       TIMESTAMP  NOT NULL,
    hora_final        TIMESTAMP  NOT NULL,
    valor_deposito    NUMERIC(12,2)  NOT NULL,
    valor_por_hora    NUMERIC(12,2)  NOT NULL,
    estado            VARCHAR(20)  NOT NULL,
    casa_numero       NUMERIC  NOT NULL,
    casa_manzana      NUMERIC  NOT NULL,
    CONSTRAINT reserva_salon_pk PRIMARY KEY (id_reserva)
);

CREATE TABLE sancion (
    id_sancion           INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_notificacion    DATE  NOT NULL,
    fecha_incidente       DATE  NOT NULL,
    descargos             VARCHAR(500)  NOT NULL,
    estado_proceso        VARCHAR(20)  NOT NULL,
    descripcion           VARCHAR(255)  NOT NULL,
    valor                 NUMERIC(12,2),
    tipo                  VARCHAR(30)  NOT NULL,
    casa_numero           NUMERIC  NOT NULL,
    casa_manzana          NUMERIC  NOT NULL,
    CONSTRAINT sancion_pk PRIMARY KEY (id_sancion)
);

CREATE TABLE concepto_cobro (
    id_concepto   INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    descripcion    VARCHAR(255)  NOT NULL,
    nombre         VARCHAR(100)  NOT NULL,
    valor_base     NUMERIC(12,2)  NOT NULL,
    CONSTRAINT concepto_cobro_pk PRIMARY KEY (id_concepto)
);

CREATE TABLE factura (
    numero_factura       INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha_vencimiento     DATE  NOT NULL,
    estado                VARCHAR(20)  NOT NULL,
    periodo               VARCHAR(10)  NOT NULL,
    fecha_emision         DATE  NOT NULL,
    total                 NUMERIC(12,2),
    casa_numero           NUMERIC  NOT NULL,
    casa_manzana          NUMERIC  NOT NULL,
    CONSTRAINT factura_pk PRIMARY KEY (numero_factura)
);

CREATE TABLE detalle_factura (
    numero_linea                     INTEGER  NOT NULL,
    valor                            NUMERIC(12,2)  NOT NULL,
    asignacion_celda_id_asignacion   INTEGER,
    factura_numero_factura           INTEGER  NOT NULL,
    reserva_salon_id_reserva         INTEGER,
    sancion_id_sancion               INTEGER,
    concepto_cobro_id_concepto       INTEGER  NOT NULL,
    CONSTRAINT detalle_factura_pk PRIMARY KEY (numero_linea, factura_numero_factura)
);

CREATE TABLE pago (
    id_pago                    INTEGER  GENERATED ALWAYS AS IDENTITY NOT NULL,
    fecha                       DATE  NOT NULL,
    valor                       NUMERIC(12,2)  NOT NULL,
    metodo_pago                 VARCHAR(20)  NOT NULL,
    factura_numero_factura      INTEGER  NOT NULL,
    persona_tipo_documento      VARCHAR(10)  NOT NULL,
    persona_numero_documento    VARCHAR(20)  NOT NULL,
    CONSTRAINT pago_pk PRIMARY KEY (id_pago)
);

-- ----------------------------------------------------------------------------
-- LLAVES FORÁNEAS
-- ----------------------------------------------------------------------------

ALTER TABLE arrendatario
    ADD CONSTRAINT arrendatario_persona_fk FOREIGN KEY (tipo_documento, numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE propietario
    ADD CONSTRAINT propietario_persona_fk FOREIGN KEY (tipo_documento, numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE residente
    ADD CONSTRAINT residente_persona_fk FOREIGN KEY (tipo_documento, numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE visitante
    ADD CONSTRAINT visitante_persona_fk FOREIGN KEY (tipo_documento, numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE telefono
    ADD CONSTRAINT telefono_persona_fk FOREIGN KEY (persona_tipo_documento, persona_numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE vehiculo
    ADD CONSTRAINT vehiculo_persona_fk FOREIGN KEY (persona_tipo_documento, persona_numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);

ALTER TABLE posesion
    ADD CONSTRAINT posesion_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE posesion
    ADD CONSTRAINT posesion_propietario_fk FOREIGN KEY (propietario_tipo_documento, propietario_numero_documento)
    REFERENCES propietario (tipo_documento, numero_documento);

ALTER TABLE arriendo
    ADD CONSTRAINT arriendo_arrendatario_fk FOREIGN KEY (arrendatario_tipo_documento, arrendatario_numero_documento)
    REFERENCES arrendatario (tipo_documento, numero_documento);

ALTER TABLE arriendo
    ADD CONSTRAINT arriendo_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE arriendo
    ADD CONSTRAINT arriendo_propietario_fk FOREIGN KEY (propietario_tipo_documento, propietario_numero_documento)
    REFERENCES propietario (tipo_documento, numero_documento);

ALTER TABLE residencia
    ADD CONSTRAINT residencia_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE residencia
    ADD CONSTRAINT residencia_residente_fk FOREIGN KEY (residente_tipo_documento, residente_numero_documento)
    REFERENCES residente (tipo_documento, numero_documento);

ALTER TABLE visita
    ADD CONSTRAINT visita_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE visita
    ADD CONSTRAINT visita_visitante_fk FOREIGN KEY (visitante_tipo_documento, visitante_numero_documento)
    REFERENCES visitante (tipo_documento, numero_documento);

ALTER TABLE asignacion_celda
    ADD CONSTRAINT asignacion_celda_celda_fk FOREIGN KEY (celda_parqueadero_id_celda)
    REFERENCES celda_parqueadero (id_celda);

ALTER TABLE asignacion_celda
    ADD CONSTRAINT asignacion_celda_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE parqueo
    ADD CONSTRAINT parqueo_celda_fk FOREIGN KEY (celda_parqueadero_id_celda)
    REFERENCES celda_parqueadero (id_celda);

ALTER TABLE parqueo
    ADD CONSTRAINT parqueo_vehiculo_fk FOREIGN KEY (vehiculo_placa)
    REFERENCES vehiculo (placa);

ALTER TABLE reserva_salon
    ADD CONSTRAINT reserva_salon_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE sancion
    ADD CONSTRAINT sancion_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE factura
    ADD CONSTRAINT factura_casa_fk FOREIGN KEY (casa_numero, casa_manzana)
    REFERENCES casa (numero, manzana);

ALTER TABLE detalle_factura
    ADD CONSTRAINT detalle_factura_factura_fk FOREIGN KEY (factura_numero_factura)
    REFERENCES factura (numero_factura);

ALTER TABLE detalle_factura
    ADD CONSTRAINT detalle_factura_asignacion_fk FOREIGN KEY (asignacion_celda_id_asignacion)
    REFERENCES asignacion_celda (id_asignacion);

ALTER TABLE detalle_factura
    ADD CONSTRAINT detalle_factura_reserva_fk FOREIGN KEY (reserva_salon_id_reserva)
    REFERENCES reserva_salon (id_reserva);

ALTER TABLE detalle_factura
    ADD CONSTRAINT detalle_factura_sancion_fk FOREIGN KEY (sancion_id_sancion)
    REFERENCES sancion (id_sancion);

ALTER TABLE detalle_factura
    ADD CONSTRAINT detalle_factura_concepto_fk FOREIGN KEY (concepto_cobro_id_concepto)
    REFERENCES concepto_cobro (id_concepto);

ALTER TABLE pago
    ADD CONSTRAINT pago_factura_fk FOREIGN KEY (factura_numero_factura)
    REFERENCES factura (numero_factura);

ALTER TABLE pago
    ADD CONSTRAINT pago_persona_fk FOREIGN KEY (persona_tipo_documento, persona_numero_documento)
    REFERENCES persona (tipo_documento, numero_documento);
