package co.edu.unbosque.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.Factura;

public class FacturaService {

	private final FacturaDAO facturaDao = new FacturaDAO();

	public List<Factura> listar() throws SQLException {
		return facturaDao.listar();
	}

	public int crear(Factura f) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		YearMonth periodo = parsearPeriodo(f.getPeriodo());
		LocalDate emision = parsearFecha("fechaEmision", f.getFechaEmision());
		LocalDate vencimiento = parsearFecha("fechaVencimiento", f.getFechaVencimiento());
		if (vencimiento.isBefore(emision)) {
			throw new DatoInvalidoException("fechaVencimiento no puede ser anterior a fechaEmision");
		}

		if (!facturaDao.existeCasa(f.getCasaNumero(), f.getCasaManzana())) {
			throw new RecursoNoEncontradoException("Casa no encontrada");
		}
		
		if (facturaDao.existePeriodo(f.getCasaNumero(), f.getCasaManzana(), periodo.toString())) {
			throw new ConflictoException("La casa ya tiene una factura del periodo " + periodo);
		}

		f.setPeriodo(periodo.toString());
		f.setEstado("PENDIENTE");
		return facturaDao.insertar(f, emision, vencimiento);
	}

	
	public void actualizar(int id, Factura cambios) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		LocalDate vencimiento = parsearFecha("fechaVencimiento", cambios.getFechaVencimiento());

		Factura actual = facturaDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(actual.getEstado()) && !"ABONADA".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede cambiar el vencimiento de una factura PENDIENTE o ABONADA (estado actual: "
							+ actual.getEstado() + ")");
		}
		if (vencimiento.isBefore(LocalDate.parse(actual.getFechaEmision()))) {
			throw new DatoInvalidoException("fechaVencimiento no puede ser anterior a fechaEmision");
		}

		if (!facturaDao.actualizarVencimiento(id, vencimiento)) {
			throw new ConflictoException("La factura ya no está PENDIENTE ni ABONADA");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		Factura actual = facturaDao.buscarPorId(id);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(actual.getEstado())) {
			throw new ConflictoException(
					"Solo se puede eliminar una factura PENDIENTE (estado actual: " + actual.getEstado() + ")");
		}
		try {
			if (!facturaDao.eliminar(id)) {
				throw new RecursoNoEncontradoException("Factura no encontrada");
			}
		} catch (SQLException e) {
			
			if ("23503".equals(e.getSQLState())) {
				throw new ConflictoException(
						"La factura tiene líneas de detalle o pagos asociados y no se puede eliminar");
			}
			throw e;
		}
	}

	/** Pasa a VENCIDA las facturas PENDIENTE o ABONADA cuya fecha de vencimiento ya pasó. */
	public int marcarVencidas() throws SQLException {
		return facturaDao.marcarVencidas(LocalDate.now());
	}

	private YearMonth parsearPeriodo(String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException("periodo es obligatorio");
		}
		try {
			return YearMonth.parse(texto);
		} catch (Exception e) {
			throw new DatoInvalidoException("periodo debe tener formato yyyy-MM, por ejemplo 2026-11");
		}
	}

	private LocalDate parsearFecha(String campo, String texto) throws DatoInvalidoException {
		if (texto == null || texto.isBlank()) {
			throw new DatoInvalidoException(campo + " es obligatoria");
		}
		try {
			return LocalDate.parse(texto);
		} catch (Exception e) {
			throw new DatoInvalidoException(campo + " debe tener formato yyyy-MM-dd, por ejemplo 2026-11-30");
		}
	}
}