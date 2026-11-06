-- DML 02: Parqueadero

BEGIN;

-- VEHICULO
INSERT INTO vehiculo (placa, marca, modelo, tipo, persona_tipo_documento, persona_numero_documento) VALUES
('ABC123', 'Mazda',     2022, 'CARRO', 'CC', '1010101010'),
('XYZ789', 'Renault',   2019, 'CARRO', 'CC', '1030303030'),
('DEF456', 'Chevrolet', 2021, 'CARRO', 'CC', '1050505050'),
('GHI12F', 'Yamaha',    2023, 'MOTO',  'CC', '1121212121'),
('JKL345', 'Toyota',    2020, 'CARRO', 'CE', 'E4581236'),
('MNO678', 'Kia',       2024, 'CARRO', 'CC', '1131313131'),
('PQR90G', 'Honda',     2022, 'MOTO',  'CC', '1101010101'),
('STU234', 'Nissan',    2018, 'CARRO', 'CC', '1141414141'),
('VWX567', 'Hyundai',   2023, 'CARRO', 'CC', '1181818181');

-- CELDA_PARQUEADERO (estado: OCUPADA si tiene un parqueo abierto)
INSERT INTO celda_parqueadero (tipo, estado) VALUES
('PROPIA',    'OCUPADA'),     -- 1
('PROPIA',    'DESOCUPADA'),  -- 2
('PROPIA',    'OCUPADA'),     -- 3
('PROPIA',    'OCUPADA'),     -- 4
('PROPIA',    'DESOCUPADA'),  -- 5
('PROPIA',    'DESOCUPADA'),  -- 6
('PROPIA',    'DESOCUPADA'),  -- 7
('PROPIA',    'OCUPADA'),     -- 8
('PROPIA',    'DESOCUPADA'),  -- 9
('VISITANTE', 'DESOCUPADA'),  -- 10
('VISITANTE', 'OCUPADA'),     -- 11
('VISITANTE', 'DESOCUPADA'),  -- 12
('PROPIA',    'DESOCUPADA'),  -- 13
('ADICIONAL', 'DESOCUPADA'),  -- 14
('ADICIONAL', 'DESOCUPADA');  -- 15 (libre, se puede solicitar)

-- ASIGNACION_CELDA
INSERT INTO asignacion_celda (fecha_inicio, fecha_fin, casa_numero, casa_manzana, celda_parqueadero_id_celda) VALUES
('2015-04-01', NULL,         1, 1, 1),   -- 1
('2016-08-01', NULL,         2, 1, 2),   -- 2
('2015-01-01', NULL,         4, 1, 3),   -- 3
('2023-02-05', NULL,         5, 1, 4),   -- 4
('2019-10-01', NULL,         1, 2, 5),   -- 5
('2025-06-05', NULL,         4, 2, 6),   -- 6
('2016-04-10', '2024-06-30', 3, 2, 7),   -- 7 (historial: propia anterior de 3/2)
('2024-01-20', NULL,         3, 1, 8),   -- 8
('2024-07-05', NULL,         3, 2, 9),   -- 9
('2015-03-01', NULL,         2, 2, 13),  -- 10 propia de 2/2
('2024-07-10', NULL,         5, 2, 7),   -- 11 propia de 5/2
('2026-01-01', NULL,         1, 1, 14);  -- 12 adicional de 1/1

-- PARQUEO
INSERT INTO parqueo (fecha_hora_entrada, fecha_hora_salida, vehiculo_placa, celda_parqueadero_id_celda) VALUES
('2026-10-28 07:30:00', '2026-10-28 18:45:00', 'ABC123', 1),
('2026-10-29 06:50:00', NULL,                  'ABC123', 1),
('2026-10-28 08:10:00', '2026-10-28 19:20:00', 'XYZ789', 2),
('2026-10-29 07:05:00', NULL,                  'DEF456', 3),
('2026-10-28 20:00:00', NULL,                  'GHI12F', 8),
('2026-10-27 14:00:00', '2026-10-27 17:30:00', 'VWX567', 10),
('2026-10-28 15:20:00', NULL,                  'VWX567', 11),
('2026-10-29 06:30:00', NULL,                  'MNO678', 4),
('2026-10-28 09:00:00', '2026-10-28 13:00:00', 'STU234', 6);

COMMIT;