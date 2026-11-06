package co.edu.unbosque.model;

import java.math.BigDecimal;

/**
 * Reserva del salón comunal hecha por una casa.
 * <p>
 * Corresponde a la tabla {@code reserva_salon}. Las horas reservadas y el valor total no se
 * guardan: se calculan en la consulta.
 */
public class ReservaSalon {
	
	/** Identificador de la reserva del salón comunal. */
	private int idReserva;
	/** Fecha de la reserva. */
	private String fechaReserva;
	/** Fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS}). */
	private String horaInicio;
	/** Fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS}). */
	private String horaFinal;
	/** Valor del depósito. */
	private BigDecimal valorDeposito;
	/** Valor por hora del salón. */
	private BigDecimal valorPorHora;
	/**
	 * Estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 * {@code DEPOSITO_RETENIDO}.
	 */
	private String estado;
	/** Número de la casa. */
	private int casaNumero;
	/** Manzana de la casa. */
	private int casaManzana;
	/** Horas reservadas (calculado). */
	private BigDecimal horasReservadas;
	/** Valor total de la reserva: horas por valor por hora (calculado). */
	private BigDecimal valorTotal;
	
	/**
	 * Crea una reserva del salón comunal vacía. Lo usa Jackson para convertir el JSON del cuerpo de la
	 * petición.
	 */
	public ReservaSalon() {
		
	}

	/**
	 * Crea una reserva del salón comunal con todos sus datos.
	 *
	 * @param idReserva identificador de la reserva del salón comunal
	 * @param fechaReserva fecha de la reserva
	 * @param horaInicio fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS})
	 * @param horaFinal fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS})
	 * @param valorDeposito valor del depósito
	 * @param valorPorHora valor por hora del salón
	 * @param estado estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 *         {@code DEPOSITO_RETENIDO}
	 * @param casaNumero número de la casa
	 * @param casaManzana manzana de la casa
	 * @param horasReservadas horas reservadas (calculado)
	 * @param valorTotal valor total de la reserva: horas por valor por hora (calculado)
	 */
	public ReservaSalon(int idReserva, String fechaReserva, String horaInicio, String horaFinal, BigDecimal valorDeposito, BigDecimal valorPorHora, String estado, int casaNumero, int casaManzana, BigDecimal horasReservadas, BigDecimal valorTotal) {
		super();
		this.idReserva = idReserva;
		this.fechaReserva = fechaReserva;
		this.horaInicio = horaInicio;
		this.horaFinal = horaFinal;
		this.valorDeposito = valorDeposito;
		this.valorPorHora = valorPorHora;
		this.estado = estado;
		this.casaNumero = casaNumero;
		this.casaManzana = casaManzana;
		this.horasReservadas = horasReservadas;
		this.valorTotal = valorTotal;
	}

	/**
	 * Devuelve el identificador de la reserva del salón comunal.
	 *
	 * @return identificador de la reserva del salón comunal
	 */
	public int getIdReserva() {
		return idReserva;
	}

	/**
	 * Asigna el identificador de la reserva del salón comunal.
	 *
	 * @param idReserva identificador de la reserva del salón comunal
	 */
	public void setIdReserva(int idReserva) {
		this.idReserva = idReserva;
	}

	/**
	 * Devuelve la fecha de la reserva.
	 *
	 * @return fecha de la reserva
	 */
	public String getFechaReserva() {
		return fechaReserva;
	}

	/**
	 * Asigna la fecha de la reserva.
	 *
	 * @param fechaReserva fecha de la reserva
	 */
	public void setFechaReserva(String fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	/**
	 * Devuelve la fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS}).
	 *
	 * @return fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS})
	 */
	public String getHoraInicio() {
		return horaInicio;
	}

	/**
	 * Asigna la fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS}).
	 *
	 * @param horaInicio fecha y hora de inicio ({@code AAAA-MM-DDTHH:MM:SS})
	 */
	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}

	/**
	 * Devuelve la fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS}).
	 *
	 * @return fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS})
	 */
	public String getHoraFinal() {
		return horaFinal;
	}

	/**
	 * Asigna la fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS}).
	 *
	 * @param horaFinal fecha y hora de fin ({@code AAAA-MM-DDTHH:MM:SS})
	 */
	public void setHoraFinal(String horaFinal) {
		this.horaFinal = horaFinal;
	}

	/**
	 * Devuelve el valor del depósito.
	 *
	 * @return valor del depósito
	 */
	public BigDecimal getValorDeposito() {
		return valorDeposito;
	}

	/**
	 * Asigna el valor del depósito.
	 *
	 * @param valorDeposito valor del depósito
	 */
	public void setValorDeposito(BigDecimal valorDeposito) {
		this.valorDeposito = valorDeposito;
	}

	/**
	 * Devuelve el valor por hora del salón.
	 *
	 * @return valor por hora del salón
	 */
	public BigDecimal getValorPorHora() {
		return valorPorHora;
	}

	/**
	 * Asigna el valor por hora del salón.
	 *
	 * @param valorPorHora valor por hora del salón
	 */
	public void setValorPorHora(BigDecimal valorPorHora) {
		this.valorPorHora = valorPorHora;
	}

	/**
	 * Devuelve el estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 * {@code DEPOSITO_RETENIDO}.
	 *
	 * @return estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 *         {@code DEPOSITO_RETENIDO}
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 * {@code DEPOSITO_RETENIDO}.
	 *
	 * @param estado estado de la reserva: {@code CONFIRMADA}, {@code CANCELADA}, {@code FINALIZADA} o
	 *         {@code DEPOSITO_RETENIDO}
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/**
	 * Devuelve el número de la casa.
	 *
	 * @return número de la casa
	 */
	public int getCasaNumero() {
		return casaNumero;
	}

	/**
	 * Asigna el número de la casa.
	 *
	 * @param casaNumero número de la casa
	 */
	public void setCasaNumero(int casaNumero) {
		this.casaNumero = casaNumero;
	}

	/**
	 * Devuelve la manzana de la casa.
	 *
	 * @return manzana de la casa
	 */
	public int getCasaManzana() {
		return casaManzana;
	}

	/**
	 * Asigna la manzana de la casa.
	 *
	 * @param casaManzana manzana de la casa
	 */
	public void setCasaManzana(int casaManzana) {
		this.casaManzana = casaManzana;
	}

	/**
	 * Devuelve las horas reservadas (calculado).
	 *
	 * @return horas reservadas (calculado)
	 */
	public BigDecimal getHorasReservadas() {
		return horasReservadas;
	}

	/**
	 * Asigna las horas reservadas (calculado).
	 *
	 * @param horasReservadas horas reservadas (calculado)
	 */
	public void setHorasReservadas(BigDecimal horasReservadas) {
		this.horasReservadas = horasReservadas;
	}

	/**
	 * Devuelve el valor total de la reserva: horas por valor por hora (calculado).
	 *
	 * @return valor total de la reserva: horas por valor por hora (calculado)
	 */
	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	/**
	 * Asigna el valor total de la reserva: horas por valor por hora (calculado).
	 *
	 * @param valorTotal valor total de la reserva: horas por valor por hora (calculado)
	 */
	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

}
