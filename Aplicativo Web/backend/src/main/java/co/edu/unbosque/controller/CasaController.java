package co.edu.unbosque.controller;

import co.edu.unbosque.model.Casa;
import co.edu.unbosque.service.CasaService;
import io.javalin.http.Context;

import java.sql.SQLException;

import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class CasaController {
	
	private final CasaService casaService = new CasaService();
	
	@OpenApi(
            path = "/api/casas",
            methods = HttpMethod.GET,
            summary = "Listar casas",
            description = "Devuelve todas las casas del conjunto, ordenadas por manzana y número.",
            tags = {"Casas"},
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Casa[].class)),
                    @OpenApiResponse(status = "500", description = "Error consultando la base de datos")
            })
	
	public void listar(Context conx) throws SQLException {
		conx.json(casaService.listar());
	}
}
