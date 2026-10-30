package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Parqueo;
import co.edu.unbosque.service.ParqueoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ParqueoController {
	
	private final ParqueoService parqueoService = new ParqueoService();
	
	@OpenApi(
			path = "/api/parqueos",
			methods = HttpMethod.GET,
			summary = "Listar parqueos",
			description = "Devuelve las entradas y salidas de vehículos, de la más reciente a la más antigua. Si fechaHoraSalida es null, el vehículo sigue parqueado.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Parqueo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(parqueoService.listar());
	}
}
