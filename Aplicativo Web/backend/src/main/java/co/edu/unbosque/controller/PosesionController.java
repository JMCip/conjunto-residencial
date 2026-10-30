package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Posesion;
import co.edu.unbosque.service.PosesionService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class PosesionController {
	
	private final PosesionService posesionService = new PosesionService();
	
	@OpenApi(
			path = "/api/posesiones",
			methods = HttpMethod.GET,
			summary = "Listar posesiones",
			description = "Devuelve el historial de propietarios de cada casa. Si fechaVenta es null, es el dueño actual.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Posesion[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(posesionService.listar());
	}
}
