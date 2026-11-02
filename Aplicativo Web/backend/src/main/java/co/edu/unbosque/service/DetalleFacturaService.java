package co.edu.unbosque.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

import co.edu.unbosque.dao.DetalleFacturaDAO;
import co.edu.unbosque.dao.DetalleFacturaDAO.Origen;
import co.edu.unbosque.dao.FacturaDAO;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.model.DetalleFactura;
import co.edu.unbosque.model.Factura;

public class DetalleFacturaService {

	
	private static final String CONCEPTO_SANCION = "Sanción";
	private static final String CONCEPTO_RESERVA = "Salón comunal";
	private static final String CONCEPTO_PARQUEADERO = "Parqueadero adicional";


	private static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

	private final DetalleFacturaDAO detalleFacturaDao = new DetalleFacturaDAO();
	private final FacturaDAO facturaDao = new FacturaDAO();

	public List<DetalleFactura> listar() throws SQLException {
		return detalleFacturaDao.listar();
	}

	
	public int crear(DetalleFactura d) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		Factura factura = obtenerFacturaAbierta(d.getNumeroFactura());

		ConceptoCobro concepto = detalleFacturaDao.buscarConcepto(d.getIdConcepto());
		if (concepto == null) {
			throw new RecursoNoEncontradoException("Concepto de cobro no encontrado");
		}

		int origenes = (d.getIdSancion() != null ? 1 : 0)
				+ (d.getIdReserva() != null ? 1 : 0)
				+ (d.getIdAsignacion() != null ? 1 : 0);
		if (origenes > 1) {
			throw new DatoInvalidoException(
					"La línea solo puede ligarse a una sanción, una reserva o una asignación, no a varias");
		}

		BigDecimal valor;
		if (d.getIdSancion() != null) {
			exigirConcepto(concepto, CONCEPTO_SANCION);
			Origen s = detalleFacturaDao.buscarSancion(d.getIdSancion());
			if (s == null) {
				throw new RecursoNoEncontradoException("Sanción no encontrada");
			}
			exigirMismaCasa(factura, s, "La sanción");
			if (!"CONFIRMADA".equals(s.estado())) {
				throw new ConflictoException(
						"Solo se cobra una sanción CONFIRMADA (estado actual: " + s.estado() + ")");
			}
			if (s.valor() == null || s.valor().signum() <= 0) {
				throw new ConflictoException("La sanción no tiene una multa para cobrar");
			}
			if (detalleFacturaDao.sancionFacturada(d.getIdSancion())) {
				throw new ConflictoException("La sanción ya fue cobrada en otra factura");
			}
			valor = s.valor();

		} else if (d.getIdReserva() != null) {
			exigirConcepto(concepto, CONCEPTO_RESERVA);
			Origen r = detalleFacturaDao.buscarReserva(d.getIdReserva());
			if (r == null) {
				throw new RecursoNoEncontradoException("Reserva no encontrada");
			}
			exigirMismaCasa(factura, r, "La reserva");
			if ("CANCELADA".equals(r.estado())) {
				throw new ConflictoException("Una reserva CANCELADA no se cobra");
			}
			if (detalleFacturaDao.reservaFacturada(d.getIdReserva())) {
				throw new ConflictoException("La reserva ya fue cobrada en otra factura");
			}
			valor = r.valor();

		} else if (d.getIdAsignacion() != null) {
			exigirConcepto(concepto, CONCEPTO_PARQUEADERO);
			Origen a = detalleFacturaDao.buscarAsignacion(d.getIdAsignacion());
			if (a == null) {
				throw new RecursoNoEncontradoException("Asignación de celda no encontrada");
			}
			exigirMismaCasa(factura, a, "La asignación");
			if (detalleFacturaDao.asignacionEnFactura(d.getIdAsignacion(), d.getNumeroFactura())) {
				throw new ConflictoException("La asignación ya está cobrada en esta factura");
			}
			valor = concepto.getValorBase();

		} else {
			
			if (esConceptoLigado(concepto)) {
				throw new DatoInvalidoException("El concepto '" + concepto.getNombre()
						+ "' exige indicar la sanción, reserva o asignación que se cobra");
			}
			valor = d.getValor() != null ? d.getValor() : concepto.getValorBase();
		}

		valor = validarValor(valor);
		return detalleFacturaDao.insertar(d, valor);
	}

	public void actualizar(int numeroFactura, int numeroLinea, DetalleFactura cambios)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		DetalleFactura actual = detalleFacturaDao.buscar(numeroFactura, numeroLinea);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
		Factura factura = obtenerFacturaAbierta(numeroFactura);

		if (actual.getIdSancion() != null || actual.getIdReserva() != null || actual.getIdAsignacion() != null) {
			throw new ConflictoException(
					"El valor de una línea ligada a sanción, reserva o asignación sale de su origen; "
							+ "elimina la línea y créala de nuevo");
		}
		if (cambios.getValor() == null) {
			throw new DatoInvalidoException("valor es obligatorio");
		}
		BigDecimal nuevo = validarValor(cambios.getValor());

		BigDecimal totalNuevo = factura.getTotal().subtract(actual.getValor()).add(nuevo);
		exigirNoQuedarBajoLoPagado(factura, totalNuevo);

		if (!detalleFacturaDao.actualizarValor(numeroFactura, numeroLinea, nuevo)) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
	}

	public void eliminar(int numeroFactura, int numeroLinea)
			throws SQLException, RecursoNoEncontradoException, ConflictoException {
		DetalleFactura actual = detalleFacturaDao.buscar(numeroFactura, numeroLinea);
		if (actual == null) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
		Factura factura = obtenerFacturaAbierta(numeroFactura);

		BigDecimal totalNuevo = factura.getTotal().subtract(actual.getValor());
		exigirNoQuedarBajoLoPagado(factura, totalNuevo);

		if (!detalleFacturaDao.eliminar(numeroFactura, numeroLinea)) {
			throw new RecursoNoEncontradoException("Línea de factura no encontrada");
		}
	}

	private Factura obtenerFacturaAbierta(int numeroFactura)
			throws SQLException, RecursoNoEncontradoException, ConflictoException {
		Factura factura = facturaDao.buscarPorId(numeroFactura);
		if (factura == null) {
			throw new RecursoNoEncontradoException("Factura no encontrada");
		}
		if (!"PENDIENTE".equals(factura.getEstado()) && !"ABONADA".equals(factura.getEstado())) {
			throw new ConflictoException(
					"Solo se pueden cambiar las líneas de una factura PENDIENTE o ABONADA (estado actual: "
							+ factura.getEstado() + "); cóbralo en la factura del siguiente periodo");
		}
		return factura;
	}

	private void exigirConcepto(ConceptoCobro concepto, String esperado) throws DatoInvalidoException {
		if (!esperado.equalsIgnoreCase(concepto.getNombre())) {
			throw new DatoInvalidoException("Esta línea debe usar el concepto '" + esperado
					+ "' (se envió '" + concepto.getNombre() + "')");
		}
	}

	private boolean esConceptoLigado(ConceptoCobro c) {
		String n = c.getNombre();
		return CONCEPTO_SANCION.equalsIgnoreCase(n) || CONCEPTO_RESERVA.equalsIgnoreCase(n)
				|| CONCEPTO_PARQUEADERO.equalsIgnoreCase(n);
	}

	
	private void exigirMismaCasa(Factura f, Origen o, String que) throws ConflictoException {
		if (o.casaNumero() != f.getCasaNumero() || o.casaManzana() != f.getCasaManzana()) {
			throw new ConflictoException(que + " pertenece a otra casa que la de la factura");
		}
	}


	private void exigirNoQuedarBajoLoPagado(Factura f, BigDecimal totalNuevo) throws ConflictoException {
		if (totalNuevo.compareTo(f.getTotalPagado()) < 0) {
			throw new ConflictoException("El total de la factura quedaría por debajo de lo ya pagado ("
					+ f.getTotalPagado() + ")");
		}
	}

	private BigDecimal validarValor(BigDecimal valor) throws DatoInvalidoException {
		if (valor == null || valor.signum() <= 0) {
			throw new DatoInvalidoException("valor debe ser mayor que cero");
		}
		BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
		if (v.compareTo(MAXIMO) > 0) {
			throw new DatoInvalidoException("valor es demasiado grande");
		}
		return v;
	}
}