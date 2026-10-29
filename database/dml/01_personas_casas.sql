-- DML 01: Personas y casas
-- Ejecutar después de 00_reiniciar.sql, como admin_conjunto.

BEGIN;

-- PERSONA
INSERT INTO persona (tipo_documento, numero_documento, correo, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) VALUES
('CC',  '1010101010', 'carlos.gomez@correo.com',     'Carlos',   'Andrés',   'Gómez',     'Ruiz'),
('CC',  '1020202020', 'maria.lopez@correo.com',      'María',    'Fernanda', 'López',     'Díaz'),
('CC',  '1030303030', 'jorge.martinez@correo.com',   'Jorge',    'Luis',     'Martínez',  'Peña'),
('CC',  '1040404040', 'ana.rodriguez@correo.com',    'Ana',      'Lucía',    'Rodríguez', 'Torres'),
('CC',  '1050505050', 'pedro.sanchez@correo.com',    'Pedro',    'Antonio',  'Sánchez',   'Rojas'),
('CC',  '1060606060', 'laura.herrera@correo.com',    'Laura',    'Valentina','Herrera',   'Castro'),
('CE',  'E4581236',   'diego.vargas@correo.com',     'Diego',    'Alejandro','Vargas',    'Moreno'),
('CC',  '1080808080', 'sofia.ramirez@correo.com',    'Sofía',    'Isabel',   'Ramírez',   'Ortiz'),
('CC',  '1090909090', 'andres.castillo@correo.com',  'Andrés',   'Felipe',   'Castillo',  'Mejía'),
('CC',  '1101010101', 'camila.moreno@correo.com',    'Camila',   'Andrea',   'Moreno',    'Silva'),
('CC',  '1111111111', 'ricardo.pena@correo.com',     'Ricardo',  'José',     'Peña',      'Vargas'),
('CC',  '1121212121', 'juliana.torres@correo.com',   'Juliana',  'Marcela',  'Torres',    'Gil'),
('CC',  '1131313131', 'santiago.ruiz@correo.com',    'Santiago', 'David',    'Ruiz',      'Cárdenas'),
('CC',  '1141414141', 'natalia.diaz@correo.com',     'Natalia',  'Paola',    'Díaz',      'Rincón'),
('TI',  '1151515151', NULL,                          'Mateo',    NULL,       'Gómez',     'López'),
('CC',  '1161616161', 'paula.sanchez@correo.com',    'Paula',    'Andrea',   'Sánchez',   'Rojas'),
('CC',  '1171717171', 'daniel.torres@correo.com',    'Daniel',   'Esteban',  'Torres',    'Gil'),
('CC',  '1181818181', 'esteban.rojas@correo.com',    'Esteban',  'Camilo',   'Rojas',     'Ariza'),
('CC',  '1191919191', 'valeria.mendoza@correo.com',  'Valeria',  'Sofía',    'Mendoza',   'Cruz'),
('PAS', 'AB123456',   'lucas.fernandez@correo.com',  'Lucas',    'Martín',   'Fernández', NULL);

-- PROPIETARIO
INSERT INTO propietario (tipo_documento, numero_documento) VALUES
('CC', '1010101010'),
('CC', '1020202020'),
('CC', '1030303030'),
('CC', '1040404040'),
('CC', '1050505050'),
('CC', '1060606060'),
('CE', 'E4581236'),
('CC', '1080808080'),
('CC', '1090909090'),
('CC', '1101010101'),
('CC', '1111111111');

-- ARRENDATARIO
INSERT INTO arrendatario (tipo_documento, numero_documento) VALUES
('CC', '1121212121'),
('CC', '1131313131'),
('CC', '1141414141');

-- RESIDENTE
INSERT INTO residente (tipo_documento, numero_documento) VALUES
('CC', '1010101010'),
('CC', '1020202020'),
('CC', '1030303030'),
('CC', '1050505050'),
('CE', 'E4581236'),
('CC', '1080808080'),
('CC', '1090909090'),
('CC', '1101010101'),
('CC', '1121212121'),
('CC', '1131313131'),
('CC', '1141414141'),
('TI', '1151515151'),
('CC', '1161616161'),
('CC', '1171717171');

-- VISITANTE
INSERT INTO visitante (tipo_documento, numero_documento) VALUES
('CC',  '1181818181'),
('CC',  '1191919191'),
('PAS', 'AB123456'),
('CC',  '1030303030');

-- TELEFONO
INSERT INTO telefono (telefono, persona_tipo_documento, persona_numero_documento) VALUES
(3001112233, 'CC',  '1010101010'),
(6014567890, 'CC',  '1010101010'),
(3002223344, 'CC',  '1020202020'),
(3103334455, 'CC',  '1030303030'),
(3114445566, 'CC',  '1040404040'),
(3125556677, 'CC',  '1050505050'),
(3136667788, 'CC',  '1060606060'),
(3147778899, 'CE',  'E4581236'),
(3158889900, 'CC',  '1080808080'),
(3169990011, 'CC',  '1090909090'),
(3170001122, 'CC',  '1101010101'),
(3181112233, 'CC',  '1111111111'),
(3192223344, 'CC',  '1121212121'),
(3203334455, 'CC',  '1131313131'),
(3214445566, 'CC',  '1141414141'),
(3225556677, 'CC',  '1161616161'),
(3236667788, 'CC',  '1171717171'),
(3247778899, 'CC',  '1181818181'),
(3258889900, 'CC',  '1191919191'),
(3269990011, 'PAS', 'AB123456');

-- CASA
INSERT INTO casa (numero, manzana, area, estado, numero_habitaciones) VALUES
(1, 1, 120.50, 'HABITADA',   3),
(2, 1, 120.50, 'HABITADA',   3),
(3, 1,  98.00, 'ARRENDADA',  2),
(4, 1, 150.75, 'HABITADA',   4),
(5, 1,  98.00, 'ARRENDADA',  2),
(1, 2, 135.00, 'HABITADA',   3),
(2, 2, 135.00, 'HABITADA',   3),
(3, 2, 110.25, 'HABITADA',   3),
(4, 2, 150.75, 'ARRENDADA',  4),
(5, 2, 110.25, 'DESOCUPADA', 3);

-- POSESION
INSERT INTO posesion (porcentaje, fecha_compra, fecha_venta, casa_numero, casa_manzana, propietario_tipo_documento, propietario_numero_documento) VALUES
( 50.00, '2015-03-10', NULL,         1, 1, 'CC', '1010101010'),
( 50.00, '2015-03-10', NULL,         1, 1, 'CC', '1020202020'),
(100.00, '2016-07-22', NULL,         2, 1, 'CC', '1030303030'),
(100.00, '2017-01-15', NULL,         3, 1, 'CC', '1040404040'),
(100.00, '2014-11-05', NULL,         4, 1, 'CC', '1050505050'),
(100.00, '2018-05-30', NULL,         5, 1, 'CC', '1060606060'),
(100.00, '2019-09-12', NULL,         1, 2, 'CE', 'E4581236'),
(100.00, '2015-02-18', NULL,         2, 2, 'CC', '1080808080'),
(100.00, '2016-04-01', '2024-06-30', 3, 2, 'CC', '1090909090'),
(100.00, '2024-07-01', NULL,         3, 2, 'CC', '1101010101'),
(100.00, '2020-08-14', NULL,         4, 2, 'CC', '1111111111'),
(100.00, '2021-10-03', NULL,         5, 2, 'CC', '1030303030');

-- ARRIENDO
INSERT INTO arriendo (cuota_mensual, fecha_inicio, fecha_fin, estado_contrato, arrendatario_tipo_documento, arrendatario_numero_documento, casa_numero, casa_manzana, propietario_tipo_documento, propietario_numero_documento) VALUES
(2500000.00, '2023-02-01', '2025-01-31', 'FINALIZADO', 'CC', '1141414141', 5, 1, 'CC', '1060606060'),
(2800000.00, '2025-03-01', '2027-02-28', 'VIGENTE',    'CC', '1131313131', 5, 1, 'CC', '1060606060'),
(2600000.00, '2024-01-15', '2026-12-31', 'VIGENTE',    'CC', '1121212121', 3, 1, 'CC', '1040404040'),
(3000000.00, '2025-06-01', '2027-05-31', 'VIGENTE',    'CC', '1141414141', 4, 2, 'CC', '1111111111');

-- RESIDENCIA
INSERT INTO residencia (fecha_ingreso, fecha_salida, residente_tipo_documento, residente_numero_documento, casa_numero, casa_manzana) VALUES
('2015-03-15', NULL,         'CC', '1010101010', 1, 1),
('2015-03-15', NULL,         'CC', '1020202020', 1, 1),
('2016-02-20', NULL,         'TI', '1151515151', 1, 1),
('2016-08-01', NULL,         'CC', '1030303030', 2, 1),
('2024-01-20', NULL,         'CC', '1121212121', 3, 1),
('2024-01-20', NULL,         'CC', '1171717171', 3, 1),
('2014-12-01', NULL,         'CC', '1050505050', 4, 1),
('2014-12-01', NULL,         'CC', '1161616161', 4, 1),
('2023-02-05', '2025-01-31', 'CC', '1141414141', 5, 1),
('2025-03-05', NULL,         'CC', '1131313131', 5, 1),
('2019-10-01', NULL,         'CE', 'E4581236',   1, 2),
('2015-03-01', NULL,         'CC', '1080808080', 2, 2),
('2016-04-10', '2024-06-30', 'CC', '1090909090', 3, 2),
('2024-07-05', NULL,         'CC', '1101010101', 3, 2),
('2025-06-05', NULL,         'CC', '1141414141', 4, 2);

-- VISITA
INSERT INTO visita (fecha_entrada, fecha_salida, visitante_tipo_documento, visitante_numero_documento, casa_numero, casa_manzana) VALUES
('2026-09-14', '2026-09-14', 'CC',  '1030303030', 1, 1),
('2026-10-20', '2026-10-20', 'CC',  '1181818181', 1, 1),
('2026-10-22', '2026-10-22', 'CC',  '1191919191', 3, 1),
('2026-10-25', '2026-10-27', 'PAS', 'AB123456',   4, 2),
('2026-10-28', NULL,         'CC',  '1181818181', 2, 2);

COMMIT;
