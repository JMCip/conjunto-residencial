package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.CeldaParqueadero;
import co.edu.unbosque.service.CeldaParqueaderoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class CeldaParqueaderoController {
	
	private final CeldaParqueaderoService celdaParqueaderoService = new CeldaParqueaderoService();
	
	@OpenApi(
			path = "/api/celdas",
			methods = HttpMethod.GET,
			summary = "Listar celdas de parqueadero",
			description = "Devuelve las celdas del parqueadero con su tipo y estado.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = CeldaParqueadero[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(celdaParqueaderoService.listar());
	}
}
