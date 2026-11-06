-- DML 04: Sanciones

BEGIN;

-- SANCION
INSERT INTO sancion (fecha_incidente, fecha_notificacion, descripcion, descargos, estado_proceso, valor, tipo, casa_numero, casa_manzana) VALUES
('2026-08-02', '2026-08-05', 'Ruido excesivo después de las 10 p. m.',
 'El residente manifiesta que la reunión terminó antes de las 11 p. m.', 'CONFIRMADA', 150000.00, 'RUIDO', 5, 1),
('2026-09-08', '2026-09-10', 'Vehículo parqueado en zona de visitantes por más de 24 horas',
 'Sin descargos presentados dentro del plazo', 'CONFIRMADA', 100000.00, 'PARQUEO_INDEBIDO', 2, 1),
('2026-10-12', '2026-10-15', 'Mascota sin correa en zonas comunes',
 'El propietario indica que la mascota estaba con correa', 'EN_DESCARGOS', NULL, 'MASCOTAS', 3, 1),
('2026-07-18', '2026-07-20', 'Daño a la puerta del salón comunal',
 'Se aceptan los descargos: el daño fue causado por un tercero', 'ANULADA', NULL, 'DANO_ZONAS_COMUNES', 4, 2),
('2026-09-29', '2026-10-02', 'Daño en jardinera de zona común',
 'El arrendatario acepta la falta y se compromete a pagar', 'CONFIRMADA', 250000.00, 'DANO_ZONAS_COMUNES', 4, 2);

COMMIT;
