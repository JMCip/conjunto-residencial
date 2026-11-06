-- Sanciones activas, con la persona o casa sancionada
SELECT id_sancion, descripcion, valor, fecha
FROM sancion
ORDER BY fecha DESC;

-- Total de dinero en sanciones por casa (si sancion esta asociada a detalle_factura)
SELECT f.casa_numero, f.casa_manzana, SUM(d.valor) AS total_sanciones
FROM detalle_factura d
JOIN factura f ON f.numero_factura = d.factura_numero_factura
WHERE d.sancion_id_sancion IS NOT NULL
GROUP BY f.casa_numero, f.casa_manzana;
