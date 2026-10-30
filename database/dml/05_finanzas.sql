-- DML 05: Finanzas

BEGIN;

-- CONCEPTO_COBRO
INSERT INTO concepto_cobro (nombre, descripcion, valor_base) VALUES
('Administración',    'Cuota mensual de administración del conjunto',         350000.00),
('Parqueadero',       'Uso mensual de celda de parqueadero asignada',          60000.00),
('Salón comunal',     'Alquiler del salón comunal (valor por hora)',           50000.00),
('Sanción',           'Multa por incumplimiento del manual de convivencia',  100000.00),
('Intereses de mora', 'Intereses por factura vencida sin pagar',               15000.00);

-- FACTURA
INSERT INTO factura (periodo, fecha_emision, fecha_vencimiento, estado, total, casa_numero, casa_manzana) VALUES
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 1, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 2, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 3, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 4, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 5, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 1, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 2, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 3, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    NULL, 4, 2),
('2026-09', '2026-09-01', '2026-09-30', 'VENCIDA',   NULL, 5, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    NULL, 1, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', NULL, 2, 1),
('2026-10', '2026-10-01', '2026-10-31', 'ABONADA',   NULL, 3, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    NULL, 4, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    NULL, 5, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', NULL, 1, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    NULL, 2, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    NULL, 3, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', NULL, 4, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', NULL, 5, 2);

-- DETALLE_FACTURA
INSERT INTO detalle_factura (factura_numero_factura, numero_linea, valor, concepto_cobro_id_concepto, asignacion_celda_id_asignacion, reserva_salon_id_reserva, sancion_id_sancion) VALUES
-- Periodo 2026-09
( 1, 1, 350000.00, 1, NULL, NULL, NULL),
( 1, 2,  60000.00, 2, 1,    NULL, NULL),
( 2, 1, 350000.00, 1, NULL, NULL, NULL),
( 2, 2,  60000.00, 2, 2,    NULL, NULL),
( 3, 1, 350000.00, 1, NULL, NULL, NULL),
( 3, 2,  30000.00, 2, 8,    NULL, NULL),
( 4, 1, 350000.00, 1, NULL, NULL, NULL),
( 4, 2,  60000.00, 2, 3,    NULL, NULL),
( 5, 1, 350000.00, 1, NULL, NULL, NULL),
( 5, 2,  60000.00, 2, 4,    NULL, NULL),
( 5, 3, 150000.00, 4, NULL, NULL, 1),
( 6, 1, 350000.00, 1, NULL, NULL, NULL),
( 6, 2,  60000.00, 2, 5,    NULL, NULL),
( 7, 1, 350000.00, 1, NULL, NULL, NULL),
( 8, 1, 350000.00, 1, NULL, NULL, NULL),
( 8, 2,  30000.00, 2, 9,    NULL, NULL),
( 9, 1, 350000.00, 1, NULL, NULL, NULL),
( 9, 2,  60000.00, 2, 6,    NULL, NULL),
(10, 1, 350000.00, 1, NULL, NULL, NULL),
-- Periodo 2026-10
(11, 1, 350000.00, 1, NULL, NULL, NULL),
(11, 2,  60000.00, 2, 1,    NULL, NULL),
(11, 3, 300000.00, 3, NULL, 1,    NULL),
(12, 1, 350000.00, 1, NULL, NULL, NULL),
(12, 2,  60000.00, 2, 2,    NULL, NULL),
(12, 3, 100000.00, 4, NULL, NULL, 2),
(13, 1, 350000.00, 1, NULL, NULL, NULL),
(13, 2,  30000.00, 2, 8,    NULL, NULL),
(13, 3, 250000.00, 3, NULL, 2,    NULL),
(14, 1, 350000.00, 1, NULL, NULL, NULL),
(14, 2,  60000.00, 2, 3,    NULL, NULL),
(15, 1, 350000.00, 1, NULL, NULL, NULL),
(15, 2,  60000.00, 2, 4,    NULL, NULL),
(16, 1, 350000.00, 1, NULL, NULL, NULL),
(16, 2,  60000.00, 2, 5,    NULL, NULL),
(17, 1, 350000.00, 1, NULL, NULL, NULL),
(18, 1, 350000.00, 1, NULL, NULL, NULL),
(18, 2,  30000.00, 2, 9,    NULL, NULL),
(19, 1, 350000.00, 1, NULL, NULL, NULL),
(19, 2,  60000.00, 2, 6,    NULL, NULL),
(19, 3, 250000.00, 4, NULL, NULL, 5),
(20, 1, 350000.00, 1, NULL, NULL, NULL),
(20, 2,  15000.00, 5, NULL, NULL, NULL);

-- PAGO
INSERT INTO pago (fecha, valor, metodo_pago, factura_numero_factura, persona_tipo_documento, persona_numero_documento) VALUES
('2026-09-10', 410000.00, 'TRANSFERENCIA',  1, 'CC', '1010101010'),
('2026-09-12', 410000.00, 'PSE',            2, 'CC', '1030303030'),
('2026-09-08', 380000.00, 'EFECTIVO',       3, 'CC', '1121212121'),
('2026-09-14', 410000.00, 'TARJETA',        4, 'CC', '1050505050'),
('2026-09-15', 560000.00, 'TRANSFERENCIA',  5, 'CC', '1131313131'),
('2026-09-05', 410000.00, 'PSE',            6, 'CE', 'E4581236'),
('2026-09-11', 350000.00, 'TRANSFERENCIA',  7, 'CC', '1080808080'),
('2026-09-13', 380000.00, 'PSE',            8, 'CC', '1101010101'),
('2026-09-09', 410000.00, 'TRANSFERENCIA',  9, 'CC', '1141414141'),
('2026-10-12', 710000.00, 'TRANSFERENCIA', 11, 'CC', '1020202020'),
('2026-10-10', 300000.00, 'EFECTIVO',      13, 'CC', '1121212121'),
('2026-10-08', 410000.00, 'PSE',           14, 'CC', '1050505050'),
('2026-10-14', 410000.00, 'TRANSFERENCIA', 15, 'CC', '1131313131'),
('2026-10-09', 350000.00, 'PSE',           17, 'CC', '1080808080'),
('2026-10-11', 200000.00, 'PSE',           18, 'CC', '1101010101'),
('2026-10-20', 180000.00, 'EFECTIVO',      18, 'CC', '1101010101');

-- TOTAL DE CADA FACTURA
UPDATE factura f
SET total = (SELECT SUM(d.valor)
             FROM detalle_factura d
             WHERE d.factura_numero_factura = f.numero_factura);

COMMIT;

