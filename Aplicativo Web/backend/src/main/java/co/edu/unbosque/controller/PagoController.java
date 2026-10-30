package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Pago;
import co.edu.unbosque.service.PagoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class PagoController {
	
	private final PagoService pagoService = new PagoService();
	
	@OpenApi(
			path = "/api/pagos",
			methods = HttpMethod.GET,
			summary = "Listar pagos",
			description = "Devuelve los pagos realizados, con la factura que pagan y quién pagó.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Pago[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(pagoService.listar());
	}
}
