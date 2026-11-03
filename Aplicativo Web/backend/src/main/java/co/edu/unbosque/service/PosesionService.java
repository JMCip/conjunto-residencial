package co.edu.unbosque.service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.PosesionDAO;
import co.edu.unbosque.model.Posesion;

public class PosesionService {
	
	private final PosesionDAO posesionDao = new PosesionDAO();
	private final RolDAO rolDao = new RolDAO();
	
	public List<Posesion> listar() throws SQLException {
		return posesionDao.listar();
	}
	
	private static final BigDecimal CIEN = new BigDecimal("100");

	public Posesion consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Posesion p = posesionDao.buscar(id);
		if (p == null) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
		return p;
	}

	public int crear(Posesion p) throws SQLException, DatoInvalidoException, ConflictoException {
		if (p.getCasaNumero() <= 0 || p.getCasaManzana() <= 0
				|| p.getPropietarioTipoDocumento() == null || p.getPropietarioTipoDocumento().isBlank()
				|| p.getPropietarioNumeroDocumento() == null || p.getPropietarioNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, propietarioTipoDocumento y propietarioNumeroDocumento son obligatorios");
		}
		validarDatos(p);
		if (!rolDao.tieneRol("propietario", p.getPropietarioTipoDocumento(), p.getPropietarioNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de propietario");
		}
		validarSuma(p.getCasaNumero(), p.getCasaManzana(), p, 0);
		return posesionDao.insertar(p);
	}

	public void actualizar(Posesion p)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Posesion actual = consultar(p.getIdPosesion());
		validarDatos(p);
		validarSuma(actual.getCasaNumero(), actual.getCasaManzana(), p, p.getIdPosesion());
		if (!posesionDao.actualizar(p)) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!posesionDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Posesión no encontrada");
		}
	}

	private void validarDatos(Posesion p) throws DatoInvalidoException {
		if (p.getPorcentaje() == null || p.getPorcentaje().signum() <= 0 || p.getPorcentaje().compareTo(CIEN) > 0) {
			throw new DatoInvalidoException("porcentaje debe ser mayor que 0 y máximo 100");
		}
		if (p.getFechaVenta() != null && p.getFechaVenta().isBlank()) {
			p.setFechaVenta(null);
		}
		try {
			if (p.getFechaCompra() == null) {
				throw new DatoInvalidoException("fechaCompra es obligatoria (formato AAAA-MM-DD)");
			}
			LocalDate compra = LocalDate.parse(p.getFechaCompra());
			if (p.getFechaVenta() != null && LocalDate.parse(p.getFechaVenta()).isBefore(compra)) {
				throw new DatoInvalidoException("fechaVenta no puede ser anterior a fechaCompra");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}

	// S-3: la suma de los porcentajes vigentes de una casa no puede pasar de 100.
	private void validarSuma(int numero, int manzana, Posesion p, int excluirId)
			throws SQLException, ConflictoException {
		if (p.getFechaVenta() != null) {
			return;
		}
		BigDecimal otros = posesionDao.sumaPorcentajeVigente(numero, manzana, excluirId);
		if (otros.add(p.getPorcentaje()).compareTo(CIEN) > 0) {
			throw new ConflictoException("Los porcentajes vigentes de la casa sumarían más de 100 (hoy: " + otros + ")");
		}
	}
}
