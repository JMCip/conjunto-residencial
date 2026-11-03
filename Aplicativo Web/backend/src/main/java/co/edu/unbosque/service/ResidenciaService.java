package co.edu.unbosque.service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import co.edu.unbosque.dao.RolDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.ResidenciaDAO;
import co.edu.unbosque.model.Residencia;

public class ResidenciaService {
	
	private final ResidenciaDAO residenciaDao = new ResidenciaDAO();
	private final RolDAO rolDao = new RolDAO();
	
	public List<Residencia> listar() throws SQLException {
		return residenciaDao.listar();
	}
	
	public Residencia consultar(int id) throws SQLException, RecursoNoEncontradoException {
		Residencia r = residenciaDao.buscar(id);
		if (r == null) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
		return r;
	}

	public int crear(Residencia r) throws SQLException, DatoInvalidoException, ConflictoException {
		if (r.getCasaNumero() <= 0 || r.getCasaManzana() <= 0
				|| r.getResidenteTipoDocumento() == null || r.getResidenteTipoDocumento().isBlank()
				|| r.getResidenteNumeroDocumento() == null || r.getResidenteNumeroDocumento().isBlank()) {
			throw new DatoInvalidoException(
					"casaNumero, casaManzana, residenteTipoDocumento y residenteNumeroDocumento son obligatorios");
		}
		validarFechas(r);
		
		if (!rolDao.tieneRol("residente", r.getResidenteTipoDocumento(), r.getResidenteNumeroDocumento())) {
			throw new ConflictoException("La persona no tiene el rol de residente");
		}
		
		if (r.getFechaSalida() == null && residenciaDao.existeVigente(r.getResidenteTipoDocumento(),
				r.getResidenteNumeroDocumento(), r.getCasaNumero(), r.getCasaManzana(), 0)) {
			throw new ConflictoException("La persona ya figura como residente vigente de esa casa");
		}
		return residenciaDao.insertar(r);
	}

	public void actualizar(Residencia r)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		Residencia actual = consultar(r.getIdResidencia());
		validarFechas(r);
		if (r.getFechaSalida() == null && residenciaDao.existeVigente(actual.getResidenteTipoDocumento(),
				actual.getResidenteNumeroDocumento(), actual.getCasaNumero(), actual.getCasaManzana(),
				r.getIdResidencia())) {
			throw new ConflictoException("La persona ya figura como residente vigente de esa casa");
		}
		if (!residenciaDao.actualizar(r)) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
	}

	public void eliminar(int id) throws SQLException, RecursoNoEncontradoException {
		if (!residenciaDao.eliminar(id)) {
			throw new RecursoNoEncontradoException("Residencia no encontrada");
		}
	}

	private void validarFechas(Residencia r) throws DatoInvalidoException {
		if (r.getFechaSalida() != null && r.getFechaSalida().isBlank()) {
			r.setFechaSalida(null);
		}
		if (r.getFechaIngreso() == null || r.getFechaIngreso().isBlank()) {
			throw new DatoInvalidoException("fechaIngreso es obligatoria (formato AAAA-MM-DD)");
		}
		try {
			LocalDate ingreso = LocalDate.parse(r.getFechaIngreso());
			if (r.getFechaSalida() != null && LocalDate.parse(r.getFechaSalida()).isBefore(ingreso)) {
				throw new DatoInvalidoException("fechaSalida no puede ser anterior a fechaIngreso");
			}
		} catch (DateTimeParseException e) {
			throw new DatoInvalidoException("Las fechas deben tener el formato AAAA-MM-DD");
		}
	}
	
}
