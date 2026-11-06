BEGIN;

--CONCEPTO COBRO
INSERT INTO concepto_cobro (nombre, descripcion, valor_base) VALUES
('Administración', 'Cuota mensual de administración del conjunto', 350000.00),
('Parqueadero adicional', 'Cuota mensual por celda de parqueadero adicional', 60000.00),
('Salón comunal', 'Alquiler del salón comunal (valor por hora)', 50000.00),
('Sanción', 'Multa por incumplimiento del manual de convivencia', 100000.00),
('Intereses de mora', 'Intereses por factura vencida sin pagar', 15000.00);

--FACTURA
INSERT INTO factura (periodo, fecha_emision, fecha_vencimiento, estado, casa_numero, casa_manzana) VALUES
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    1, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    2, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    3, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    4, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    5, 1),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    1, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    2, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    3, 2),
('2026-09', '2026-09-01', '2026-09-30', 'PAGADA',    4, 2),
('2026-09', '2026-09-01', '2026-09-30', 'VENCIDA',   5, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    1, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', 2, 1),
('2026-10', '2026-10-01', '2026-10-31', 'ABONADA',   3, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    4, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    5, 1),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', 1, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    2, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PAGADA',    3, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', 4, 2),
('2026-10', '2026-10-01', '2026-10-31', 'PENDIENTE', 5, 2);



-- DETALLE_FACTURA
INSERT INTO detalle_factura (factura_numero_factura, numero_linea, valor, concepto_cobro_id_concepto, asignacion_celda_id_asignacion, reserva_salon_id_reserva, sancion_id_sancion) VALUES
-- Periodo 2026-09
( 1, 1, 350000.00, 1, NULL, NULL, NULL),
( 1, 2,  60000.00, 2, 12,   NULL, NULL),   -- parqueadero adicional de 1/1
( 2, 1, 350000.00, 1, NULL, NULL, NULL),
( 3, 1, 350000.00, 1, NULL, NULL, NULL),
( 4, 1, 350000.00, 1, NULL, NULL, NULL),
( 5, 1, 350000.00, 1, NULL, NULL, NULL),
( 5, 2, 150000.00, 4, NULL, NULL, 1),
( 6, 1, 350000.00, 1, NULL, NULL, NULL),
( 7, 1, 350000.00, 1, NULL, NULL, NULL),
( 8, 1, 350000.00, 1, NULL, NULL, NULL),
( 9, 1, 350000.00, 1, NULL, NULL, NULL),
(10, 1, 350000.00, 1, NULL, NULL, NULL),
-- Periodo 2026-10
(11, 1, 350000.00, 1, NULL, NULL, NULL),
(11, 2,  60000.00, 2, 12,   NULL, NULL),
(11, 3, 300000.00, 3, NULL, 1,    NULL),
(12, 1, 350000.00, 1, NULL, NULL, NULL),
(12, 2, 100000.00, 4, NULL, NULL, 2),
(13, 1, 350000.00, 1, NULL, NULL, NULL),
(13, 2, 250000.00, 3, NULL, 2,    NULL),
(14, 1, 350000.00, 1, NULL, NULL, NULL),
(15, 1, 350000.00, 1, NULL, NULL, NULL),
(16, 1, 350000.00, 1, NULL, NULL, NULL),
(17, 1, 350000.00, 1, NULL, NULL, NULL),
(18, 1, 350000.00, 1, NULL, NULL, NULL),
(19, 1, 350000.00, 1, NULL, NULL, NULL),
(19, 2, 250000.00, 4, NULL, NULL, 5),
(20, 1, 350000.00, 1, NULL, NULL, NULL),
(20, 2,  15000.00, 5, NULL, NULL, NULL);

--PAGO
INSERT INTO pago (fecha, valor, metodo_pago, factura_numero_factura, persona_tipo_documento, persona_numero_documento) VALUES
('2026-09-10', 410000.00, 'TRANSFERENCIA',  1, 'CC', '1010101010'),
('2026-09-12', 350000.00, 'PSE',            2, 'CC', '1030303030'),
('2026-09-08', 350000.00, 'EFECTIVO',       3, 'CC', '1121212121'),
('2026-09-14', 350000.00, 'TARJETA',        4, 'CC', '1050505050'),
('2026-09-15', 500000.00, 'TRANSFERENCIA',  5, 'CC', '1131313131'),
('2026-09-05', 350000.00, 'PSE',            6, 'CE', 'E4581236'),
('2026-09-11', 350000.00, 'TRANSFERENCIA',  7, 'CC', '1080808080'),
('2026-09-13', 350000.00, 'PSE',            8, 'CC', '1101010101'),
('2026-09-09', 350000.00, 'TRANSFERENCIA',  9, 'CC', '1141414141'),
('2026-10-12', 710000.00, 'TRANSFERENCIA', 11, 'CC', '1020202020'),
('2026-10-10', 300000.00, 'EFECTIVO',      13, 'CC', '1121212121'),
('2026-10-08', 350000.00, 'PSE',           14, 'CC', '1050505050'),
('2026-10-14', 350000.00, 'TRANSFERENCIA', 15, 'CC', '1131313131'),
('2026-10-09', 350000.00, 'PSE',           17, 'CC', '1080808080'),
('2026-10-11', 200000.00, 'PSE',           18, 'CC', '1101010101'),
('2026-10-20', 150000.00, 'EFECTIVO',      18, 'CC', '1101010101');


COMMIT;