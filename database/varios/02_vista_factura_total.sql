-- =====================================================================
-- Quita el atributo derivado factura.total (riesgo de quedar
-- desincronizado) y lo reemplaza por una vista que lo calcula al vuelo
-- sumando las lineas de detalle_factura.
-- Ejecutar como admin_conjunto (dueno de las tablas).
-- =====================================================================

ALTER TABLE factura DROP COLUMN total;

CREATE VIEW factura_con_total AS
SELECT f.*,
       COALESCE(SUM(d.valor), 0) AS total
FROM factura f
LEFT JOIN detalle_factura d ON d.factura_numero_factura = f.numero_factura
GROUP BY f.numero_factura, f.fecha_vencimiento, f.estado, f.periodo, f.fecha_emision, f.casa_numero, f.casa_manzana;

GRANT SELECT ON factura_con_total TO app_backend;
