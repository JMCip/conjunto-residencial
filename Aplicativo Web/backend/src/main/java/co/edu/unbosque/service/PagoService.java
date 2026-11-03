package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.dao.PagoDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Factura;
import co.edu.unbosque.model.Pago;

public class PagoService {

	
	private static final Set<String> METODOS = Set.of("EFECTIVO", "TRANSFERENCIA", "PSE", "TARJETA");

	private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

	private final PagoDAO pagoDao = new PagoDAO();
	private final FacturaDAO facturaDao = new FacturaDAO();

	
	public record Registro(int idPago, String estadoFactura, BigDecimal saldo) {
	}

	public List<Pago> listar() throws SQLException {
		return pagoDao.listar();
	}

	
	public Registro registrar(Pago p) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		Factura factura = facturaDao.buscarPorId(p.getNumeroFactura());
		if (factura == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}

		String tipo = limpiar(p.getPersonaTipoDocumento());
		String numero = limpiar(p.getPersonaNumeroDocumento());
		if (tipo == null || numero == null) {
			throw new DatoInvalidoException("personaTipoDocumento y personaNumeroDocumento son obligatorios");
		}
		tipo = tipo.toUpperCase();

		String metodo = limpiar(p.getMetodoPago());
		if (metodo == null) {
			throw new DatoInvalidoException("metodoPago es obligatorio");
		}
		metodo = metodo.toUpperCase();
		if (!METODOS.contains(metodo)) {
			throw new DatoInvalidoException("metodoPago debe ser uno de: EFECTIVO, TRANSFERENCIA, PSE, TARJETA");
		}

		LocalDate fecha = parsearFecha(p.getFecha());
		if (fecha.isBefore(LocalDate.parse(factura.getFechaEmision()))) {
			throw new DatoInvalidoException("fecha no puede ser anterior a la emisión de la factura ("
					+ factura.getFechaEmision() + ")");
		}
		if (fecha.isAfter(LocalDate.now())) {
			throw new DatoInvalidoException("fecha no puede ser futura");
		}

		BigDecimal valor = validarValor(p.getValor());

		if (!pagoDao.existePersona(tipo, numero)) {
			throw new RecursoNoEncontradoException("La persona que paga no está registrada");
		}

		if ("PAGADA".equals(factura.getEstado())) {
			throw new ConflictoException("La factura ya está PAGADA");
		}
		if (factura.getSaldo().signum() <= 0) {
			throw new ConflictoException("La factura no tiene saldo por pagar");
		}
		if (valor.compareTo(factura.getSaldo()) > 0) {
			throw new ConflictoException("El pago supera el saldo de la factura (saldo: " + factura.getSaldo() + ")");
		}

		p.setPersonaTipoDocumento(tipo);
		p.setPersonaNumeroDocumento(numero);
		int id = pagoDao.registrar(p, fecha, valor, metodo);
		if (id == 0) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (id < 0) {
			throw new ConflictoException("El pago supera el saldo actual de la factura");
		}

		Factura despues = facturaDao.buscarPorId(p.getNumeroFactura());
		return new Registro(id, despues.getEstado(), despues.getSaldo());
	}

	private String limpiar(String texto) {
		return (texto == null || texto.isBlank()) ? null : texto.trim();
	}

	private LocalDate parsearFecha(String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException("fecha es obligatoria");
		}
		try {
			return LocalDate.parse(texto.trim());
		} catch (Exception e) {
			throw new DatoInvalidoException("fecha debe tener formato yyyy-MM-dd, por ejemplo 2026-11-02");
		}
	}

	private BigDecimal validarValor(BigDecimal valor) throws DatoInvalidoException {
		if (valor == null || valor.signum() <= 0) {
			throw new DatoInvalidoException("valor debe ser mayor que cero");
		}
		BigDecimal v;
		try {
			v = valor.setScale(2, java.math.RoundingMode.UNNECESSARY);
		} catch (ArithmeticException e) {
			throw new DatoInvalidoException("valor admite máximo 2 decimales");
		}
		if (v.compareTo(MAXIMO) > 0) {
			throw new DatoInvalidoException("valor es demasiado grande");
		}
		return v;
	}
}