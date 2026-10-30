package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.AsignacionCelda;
import co.edu.unbosque.service.AsignacionCeldaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class AsignacionCeldaController {
	
	private final AsignacionCeldaService asignacionCeldaService = new AsignacionCeldaService();
	
	@OpenApi(
			path = "/api/asignaciones-celda",
			methods = HttpMethod.GET,
			summary = "Listar asignaciones de celda",
			description = "Devuelve qué celda tiene asignada cada casa. Si fechaFin es null, la asignación está vigente.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = AsignacionCelda[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(asignacionCeldaService.listar());
	}
}
