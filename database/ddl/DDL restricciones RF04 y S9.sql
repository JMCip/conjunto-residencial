-- =====================================================================
--RF-04: Registrar contratos de arriendo (casa, propietario que lo firma,
--arrendatario que lo asume, cuota mensual, fechas y estado) y validar
--que exista un solo contrato vigente por casa.
--S-9:El salón comunal se reserva por franjas de horario (hora de inicio y hora final) y solo puede
--existir una reserva por franja. La reserva se asocia a la casa, no a una persona, y exige un
--depósito de garantía cuyo valor se registra en la reserva. Las horas reservadas se calculan a
--partir de la hora de inicio y la hora final, y el total a pagar es el resultado de multiplicar esas
--horas por el valor por hora de la reserva; ninguno de los dos se almacena.--
-- =====================================================================


-- RF-04:
ALTER TABLE arriendo
    ADD CONSTRAINT arriendo_estado_check
    CHECK (estado_contrato IN ('VIGENTE', 'FINALIZADO'));

CREATE UNIQUE INDEX arriendo_un_vigente_por_casa
    ON arriendo (casa_numero, casa_manzana)
    WHERE estado_contrato = 'VIGENTE';


-- S-9
ALTER TABLE reserva_salon
    ADD CONSTRAINT reserva_salon_estado_check
    CHECK (estado IN ('CONFIRMADA', 'FINALIZADA', 'CANCELADA'));


ALTER TABLE reserva_salon
    ADD CONSTRAINT reserva_salon_horario_check
    CHECK (hora_final > hora_inicio);

ALTER TABLE reserva_salon
    ADD CONSTRAINT reserva_salon_sin_cruce
    EXCLUDE USING gist (tsrange(hora_inicio, hora_final) WITH &&)
    WHERE (estado <> 'CANCELADA');


