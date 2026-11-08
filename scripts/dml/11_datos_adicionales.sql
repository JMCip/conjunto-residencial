-- =====================================================================
-- Datos adicionales
-- =====================================================================
ROLLBACK;
BEGIN;

-- 1. Personas nuevas (varios roles via ISA)
INSERT INTO persona (tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) VALUES
('CC','2000000001','carlos.ruiz@mail.com','Carlos',NULL,'Ruiz','Mendez'),
('CC','2000000002','laura.vega@mail.com','Laura','Patricia','Vega','Soto'),
('CC','2000000003','andres.pena@mail.com','Andres',NULL,'Peña',NULL),
('CC','2000000004','monica.rios@mail.com','Monica','Alejandra','Rios','Cardenas'),
('CC','2000000005','felipe.gomez@mail.com','Felipe',NULL,'Gomez','Torres'),
('CC','2000000006','diana.castro@mail.com','Diana',NULL,'Castro','Leon'),
('CC','2000000007','sergio.molina@mail.com','Sergio','David','Molina','Prieto'),
('CC','2000000008','paula.jimenez@mail.com','Paula',NULL,'Jimenez','Ortiz');

INSERT INTO propietario (tipo_documento, numero_documento) VALUES
('CC','2000000001'), ('CC','2000000003'), ('CC','2000000005'), ('CC','2000000007');

INSERT INTO arrendatario (tipo_documento, numero_documento) VALUES
('CC','2000000002'), ('CC','2000000004'), ('CC','2000000006'), ('CC','2000000008');

INSERT INTO residente (tipo_documento, numero_documento) VALUES
('CC','2000000002'), ('CC','2000000004'), ('CC','2000000006'), ('CC','2000000008');

INSERT INTO visitante (tipo_documento, numero_documento) VALUES
('CC','2000000003'), ('CC','2000000005');

-- 2. Casas nuevas
INSERT INTO casa (numero, manzana, area, numero_habitaciones) VALUES
(10, 2, 95.0, 2),
(11, 2, 130.0, 3),
(12, 2, 110.5, 3),
(13, 2, 88.0, 2);

-- 3. Posesion (propietarios sobre las casas nuevas)
INSERT INTO posesion (porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana, propietario_tipo_documento, propietario_numero_documento) VALUES
(100, '2023-03-10', NULL, 10, 2, 'CC','2000000001'),
(100, '2022-07-22', NULL, 11, 2, 'CC','2000000003'),
(100, '2024-01-15', NULL, 12, 2, 'CC','2000000005'),
(100, '2021-11-05', NULL, 13, 2, 'CC','2000000007');

-- 4. Arriendos (una sola casa con contrato VIGENTE cada una, sin chocar con RF-04)
INSERT INTO arriendo (cuota_mensual, fecha_inicio, fecha_fin, estado_contrato, arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana, propietario_tipo_documento, propietario_numero_documento) VALUES
(850000, '2026-02-01', NULL, 'VIGENTE',   'CC','2000000002', 10, 2, 'CC','2000000001'),
(950000, '2026-04-01', NULL, 'VIGENTE',   'CC','2000000004', 11, 2, 'CC','2000000003'),
(700000, '2024-06-01', '2025-05-30', 'FINALIZADO', 'CC','2000000006', 13, 2, 'CC','2000000007'),
(780000, '2026-06-01', NULL, 'VIGENTE',   'CC','2000000008', 13, 2, 'CC','2000000007');

-- 5. Vehiculos
INSERT INTO vehiculo (persona_tipo_documento, persona_numero_documento, placa, marca, modelo, tipo) VALUES
('CC','2000000002','ZXY200','Renault',2021,'Carro'),
('CC','2000000004','KLM900','Yamaha',2022,'Moto'),
('CC','2000000008','QWE555','Chevrolet',2020,'Carro');

-- 6. Celdas de parqueadero
INSERT INTO celda_parqueadero (tipo, estado) VALUES
('Carro','OCUPADA'), ('Carro','DISPONIBLE'), ('Moto','OCUPADA');

INSERT INTO asignacion_celda (fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda) VALUES
('2026-02-01', NULL, 10, 2, 16),
('2026-04-01', NULL, 11, 2, 18);

-- 7. Telefonos
INSERT INTO telefono (telefono, persona_tipo_documento, persona_numero_documento) VALUES
(3011112222, 'CC','2000000001'),
(3022223333, 'CC','2000000002'),
(3033334444, 'CC','2000000004'),
(3044445555, 'CC','2000000008');

-- 8. Reservas de salon (horario sin cruce con lo ya existente)
INSERT INTO reserva_salon (fecha_reserva, hora_inicio, hora_final, valor_deposito, valor_por_hora, estado, casa_numero, casa_manzana) VALUES
('2026-12-05','2026-12-05 10:00','2026-12-05 13:00', 50000, 20000, 'CONFIRMADA', 10, 2),
('2026-12-10','2026-12-10 16:00','2026-12-10 19:00', 50000, 20000, 'CONFIRMADA', 11, 2);

-- 9. Facturas (sin la columna total, se calcula con la vista factura_con_total)
INSERT INTO factura (fecha_vencimiento, estado, periodo, fecha_emision, casa_numero, casa_manzana) VALUES
('2026-12-15','PENDIENTE','2026-12','2026-12-01', 10, 2),
('2026-12-15','PAGADA',   '2026-12','2026-12-01', 11, 2);

INSERT INTO detalle_factura (numero_linea, valor, factura_numero_factura, concepto_cobro_id_concepto) VALUES
(1, 250000, 24, 1),
(1, 250000, 25, 1);

-- 10. Sancion de ejemplo (columnas reales de tu tabla sancion)
INSERT INTO sancion (fecha_notificacion, fecha_incidente, descargos, estado_proceso, descripcion, valor, tipo, casa_numero, casa_manzana) VALUES
('2026-11-02', '2026-11-01', 'El residente alega que la fiesta termino antes de las 10pm, sin pruebas aportadas.', 'EN_PROCESO', 'Ruido excesivo despues de las 10pm', 80000, 'RUIDO', 13, 2);

COMMIT;

SELECT numero_factura, casa_numero, casa_manzana FROM factura ORDER BY numero_factura DESC LIMIT 2;