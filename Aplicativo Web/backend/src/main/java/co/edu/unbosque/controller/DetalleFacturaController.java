package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.DetalleFactura;
import co.edu.unbosque.service.DetalleFacturaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class DetalleFacturaController {
	
	private final DetalleFacturaService detalleFacturaService = new DetalleFacturaService();
	
	@OpenApi(
			path = "/api/detalles-factura",
			methods = HttpMethod.GET,
			summary = "Listar detalles de factura",
			description = "Devuelve las líneas de cada factura. idAsignacion, idReserva o idSancion indican de dónde sale el cobro.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = DetalleFactura[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(detalleFacturaService.listar());
	}
}
