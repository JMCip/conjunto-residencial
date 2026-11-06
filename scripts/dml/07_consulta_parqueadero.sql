-- Celdas de parqueadero disponibles
SELECT id_celda, tipo
FROM celda_parqueadero
WHERE estado = 'DISPONIBLE';

-- Vehiculos registrados por persona
SELECT p.primer_nombre, p.primer_apellido, v.placa, v.marca, v.modelo, v.tipo
FROM persona p
JOIN vehiculo v ON v.persona_numero_documento = p.numero_documento AND v.persona_tipo_documento = p.tipo_documento;

-- Celdas asignadas actualmente y a que casa
SELECT cp.id_celda, cp.tipo, ac.casa_numero, ac.casa_manzana
FROM asignacion_celda ac
JOIN celda_parqueadero cp ON cp.id_celda = ac.celda_parqueadero_id_celda;
