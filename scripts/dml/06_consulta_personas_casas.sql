-- Listado de residentes con la casa donde viven, via el contrato de arriendo vigente
SELECT p.primer_nombre, p.primer_apellido, c.numero, c.manzana
FROM persona p
JOIN arriendo a ON a.arrendatario_numero_documento = p.numero_documento AND a.arrendatario_tipo_documento = p.tipo_documento
JOIN casa c ON c.numero = a.casa_numero AND c.manzana = a.casa_manzana
WHERE a.estado_contrato = 'VIGENTE';

-- Casas desocupadas (sin contrato de arriendo vigente)
SELECT numero, manzana, area, numero_habitaciones
FROM casa
WHERE estado = 'DESOCUPADA';

-- Propietarios y el porcentaje de posesion de cada casa que tienen
SELECT p.primer_nombre, p.primer_apellido, po.casa_numero, po.casa_manzana, po.porcentaje
FROM persona p
JOIN posesion po ON po.propietario_numero_documento = p.numero_documento AND po.propietario_tipo_documento = p.tipo_documento
WHERE po.fecha_venta IS NULL;

-- Telefonos registrados por persona
SELECT p.primer_nombre, p.primer_apellido, t.telefono
FROM persona p
JOIN telefono t ON t.persona_numero_documento = p.numero_documento AND t.persona_tipo_documento = p.tipo_documento;
