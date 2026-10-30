package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.ConceptoCobro;
import co.edu.unbosque.service.ConceptoCobroService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ConceptoCobroController {
	
	private final ConceptoCobroService conceptoCobroService = new ConceptoCobroService();
	
	@OpenApi(
			path = "/api/conceptos",
			methods = HttpMethod.GET,
			summary = "Listar conceptos de cobro",
			description = "Devuelve los conceptos que se pueden cobrar en una factura y su valor base.",
			tags = {"Finanzas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = ConceptoCobro[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(conceptoCobroService.listar());
	}
}
