package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Persona;
import co.edu.unbosque.service.RolService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class RolController {
	
	private final RolService rolService = new RolService();
	
	@OpenApi(
			path = "/api/propietarios",
			methods = HttpMethod.GET,
			summary = "Listar propietarios",
			description = "Devuelve las personas que son propietarias de alguna casa.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listarPropietarios(Context conx) throws SQLException {
		conx.json(rolService.listarPropietarios());
	}
	
	@OpenApi(
			path = "/api/arrendatarios",
			methods = HttpMethod.GET,
			summary = "Listar arrendatarios",
			description = "Devuelve las personas registradas como arrendatarias.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listarArrendatarios(Context conx) throws SQLException {
		conx.json(rolService.listarArrendatarios());
	}
	
	@OpenApi(
			path = "/api/residentes",
			methods = HttpMethod.GET,
			summary = "Listar residentes",
			description = "Devuelve las personas registradas como residentes.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listarResidentes(Context conx) throws SQLException {
		conx.json(rolService.listarResidentes());
	}
	
	@OpenApi(
			path = "/api/visitantes",
			methods = HttpMethod.GET,
			summary = "Listar visitantes",
			description = "Devuelve las personas registradas como visitantes.",
			tags = {"Personas"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Persona[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listarVisitantes(Context conx) throws SQLException {
		conx.json(rolService.listarVisitantes());
	}
}
