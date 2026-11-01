package co.edu.unbosque.dao;
import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.model.Factura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {
	
	private static final String LISTAR = """
			SELECT f.numero_factura, f.periodo, f.fecha_emision, f.fecha_vencimiento, f.estado,
			       COALESCE(t.total, 0) AS total,
			       f.casa_numero, f.casa_manzana,
			       COALESCE(p.pagado, 0) AS total_pagado,
			       COALESCE(t.total, 0) - COALESCE(p.pagado, 0) AS saldo
			FROM factura f
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS total
			           FROM detalle_factura
			           GROUP BY factura_numero_factura) t
			       ON t.factura_numero_factura = f.numero_factura
			LEFT JOIN (SELECT factura_numero_factura, SUM(valor) AS pagado
			           FROM pago
			           GROUP BY factura_numero_factura) p
			       ON p.factura_numero_factura = f.numero_factura
			ORDER BY f.periodo, f.casa_manzana, f.casa_numero
			""";
	
	public List<Factura> listar() throws SQLException {
		List<Factura> facturas = new ArrayList<>();
		try (	Connection c = ConexionBD.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(LISTAR);
				ResultSet rs = ps.executeQuery()) {
			
			while (rs.next()) {
				facturas.add(mapear(rs));
			}
		}
		return facturas;
	}
	
	private Factura mapear(ResultSet rs) throws SQLException {
		return new Factura(
				rs.getInt("numero_factura"),
				rs.getString("periodo"),
				rs.getString("fecha_emision"),
				rs.getString("fecha_vencimiento"),
				rs.getString("estado"),
				rs.getBigDecimal("total"),
				rs.getInt("casa_numero"),
				rs.getInt("casa_manzana"),
				rs.getBigDecimal("total_pagado"),
				rs.getBigDecimal("saldo")
				);
	}
}
