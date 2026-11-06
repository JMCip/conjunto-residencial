-- Facturas pendientes de pago, con su total calculado (vista factura_con_total)
SELECT numero_factura, casa_numero, casa_manzana, fecha_vencimiento, total
FROM factura_con_total
WHERE estado = 'PENDIENTE'
ORDER BY fecha_vencimiento;

-- Historial de pagos de una persona (cambiar el numero de documento)
SELECT pa.fecha, pa.valor, pa.metodo_pago, pa.factura_numero_factura
FROM pago pa
WHERE pa.persona_numero_documento = '1000000002'
ORDER BY pa.fecha DESC;

-- Detalle linea por linea de una factura especifica (cambiar el numero de factura)
SELECT df.numero_linea, df.valor, cc.nombre AS concepto
FROM detalle_factura df
LEFT JOIN concepto_cobro cc ON cc.id_concepto = df.concepto_cobro_id_concepto
WHERE df.factura_numero_factura = 1
ORDER BY df.numero_linea;

-- Total facturado vs total pagado por casa (control de morosidad)
SELECT f.casa_numero, f.casa_manzana,
       SUM(fc.total) AS total_facturado,
       COALESCE(SUM(p.valor), 0) AS total_pagado
FROM factura f
JOIN factura_con_total fc ON fc.numero_factura = f.numero_factura
LEFT JOIN pago p ON p.factura_numero_factura = f.numero_factura
GROUP BY f.casa_numero, f.casa_manzana;
