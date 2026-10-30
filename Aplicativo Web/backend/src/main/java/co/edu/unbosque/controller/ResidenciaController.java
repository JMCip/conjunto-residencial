package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Residencia;
import co.edu.unbosque.service.ResidenciaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ResidenciaController {
	
	private final ResidenciaService residenciaService = new ResidenciaService();
	
	@OpenApi(
			path = "/api/residencias",
			methods = HttpMethod.GET,
			summary = "Listar residencias",
			description = "Devuelve quién vive o ha vivido en cada casa. Si fechaSalida es null, vive ahí actualmente.",
			tags = {"Casas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Residencia[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(residenciaService.listar());
	}
}
