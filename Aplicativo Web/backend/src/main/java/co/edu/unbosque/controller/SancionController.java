package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Sancion;
import co.edu.unbosque.service.SancionService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class SancionController {
	
	private final SancionService sancionService = new SancionService();
	
	@OpenApi(
			path = "/api/sanciones",
			methods = HttpMethod.GET,
			summary = "Listar sanciones",
			description = "Devuelve las sanciones impuestas a las casas, de la más reciente a la más antigua.",
			tags = {"Sanciones"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Sancion[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(sancionService.listar());
	}
}
