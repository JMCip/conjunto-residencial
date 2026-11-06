-- Reservas confirmadas del salon comunal, ordenadas por fecha
SELECT id_reserva, fecha_reserva, hora_inicio, hora_final, casa_numero, casa_manzana
FROM reserva_salon
WHERE estado = 'CONFIRMADA'
ORDER BY fecha_reserva;

-- Reservas de una fecha especifica (cambiar la fecha segun necesidad)
SELECT id_reserva, hora_inicio, hora_final, estado, casa_numero, casa_manzana
FROM reserva_salon
WHERE fecha_reserva = CURRENT_DATE;
