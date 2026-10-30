package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Factura;
import co.edu.unbosque.service.FacturaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class FacturaController {
	
	private final FacturaService facturaService = new FacturaService();
	
	@OpenApi(
			path = "/api/facturas",
			methods = HttpMethod.GET,
			summary = "Listar facturas",
			description = "Devuelve las facturas con lo pagado y el saldo pendiente, calculados a partir de los pagos.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Factura[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(facturaService.listar());
	}
}
