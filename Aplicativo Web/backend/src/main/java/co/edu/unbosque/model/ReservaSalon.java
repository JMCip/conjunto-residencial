package co.edu.unbosque.model;

import java.math.BigDecimal;

public class ReservaSalon {
	
	private int idReserva;
	private String fechaReserva;
	private String horaInicio;
	private String horaFinal;
	private BigDecimal valorDeposito;
	private BigDecimal valorPorHora;
	private String estado;
	private int casaNumero;
	private int casaManzana;
	private BigDecimal horasReservadas;
	private BigDecimal valorTotal;
	
	public ReservaSalon() {
		
	}

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

	public int getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(int idReserva) {
		this.idReserva = idReserva;
	}

	public String getFechaReserva() {
		return fechaReserva;
	}

	public void setFechaReserva(String fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	public String getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(String horaInicio) {
		this.horaInicio = horaInicio;
	}

	public String getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(String horaFinal) {
		this.horaFinal = horaFinal;
	}

	public BigDecimal getValorDeposito() {
		return valorDeposito;
	}

	public void setValorDeposito(BigDecimal valorDeposito) {
		this.valorDeposito = valorDeposito;
	}

	public BigDecimal getValorPorHora() {
		return valorPorHora;
	}

	public void setValorPorHora(BigDecimal valorPorHora) {
		this.valorPorHora = valorPorHora;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getCasaNumero() {
		return casaNumero;
	}

	public void setCasaNumero(int casaNumero) {
		this.casaNumero = casaNumero;
	}

	public int getCasaManzana() {
		return casaManzana;
	}

	public void setCasaManzana(int casaManzana) {
		this.casaManzana = casaManzana;
	}

	public BigDecimal getHorasReservadas() {
		return horasReservadas;
	}

	public void setHorasReservadas(BigDecimal horasReservadas) {
		this.horasReservadas = horasReservadas;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

}
