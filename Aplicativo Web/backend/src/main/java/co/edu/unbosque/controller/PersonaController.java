package co.edu.unbosque.controller;



import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Casa;
import co.edu.unbosque.service.PersonaService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;


public class PersonaController {
	private final PersonaService pService = new PersonaService();
	
	@OpenApi (
			path = "/api/personas",
			methods = HttpMethod.GET,
			summary = "Listar personas",
			description = "Devuelve todas las personas del conjunto, ordenadas por el primer nombre y apellido.",
			tags = {"Personas"},
			responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(from = Casa[].class)),
                    @OpenApiResponse(status = "500", description = "Error consultando la base de datos")
            })
	
	public void listar(Context conx) throws SQLException {
		conx.json(pService.listar());
	}
}
