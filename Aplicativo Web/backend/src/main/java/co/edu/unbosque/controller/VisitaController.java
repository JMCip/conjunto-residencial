package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Visita;
import co.edu.unbosque.service.VisitaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class VisitaController {
	
	private final VisitaService visitaService = new VisitaService();
	
	@OpenApi(
			path = "/api/visitas",
			methods = HttpMethod.GET,
			summary = "Listar visitas",
			description = "Devuelve las visitas registradas, de la más reciente a la más antigua. Si fechaSalida es null, el visitante sigue en el conjunto.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Visita[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(visitaService.listar());
	}
}
