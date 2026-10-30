package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Telefono;
import co.edu.unbosque.service.TelefonoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class TelefonoController {
	
	private final TelefonoService telefonoService = new TelefonoService();
	
	@OpenApi(
			path = "/api/telefonos",
			methods = HttpMethod.GET,
			summary = "Listar teléfonos",
			description = "Devuelve los teléfonos registrados de cada persona.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Telefono[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(telefonoService.listar());
	}
}
