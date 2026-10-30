package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Arriendo;
import co.edu.unbosque.service.ArriendoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ArriendoController {
	
	private final ArriendoService arriendoService = new ArriendoService();
	
	@OpenApi(
			path = "/api/arriendos",
			methods = HttpMethod.GET,
			summary = "Listar arriendos",
			description = "Devuelve los contratos de arriendo, vigentes y finalizados.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Arriendo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(arriendoService.listar());
	}
}
