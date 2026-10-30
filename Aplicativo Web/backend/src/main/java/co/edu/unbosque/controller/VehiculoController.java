package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.Vehiculo;
import co.edu.unbosque.service.VehiculoService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class VehiculoController {
	
	private final VehiculoService vehiculoService = new VehiculoService();
	
	@OpenApi(
			path = "/api/vehiculos",
			methods = HttpMethod.GET,
			summary = "Listar vehículos",
			description = "Devuelve los vehículos registrados con el documento de su dueño.",
			tags = {"Parqueadero"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = Vehiculo[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(vehiculoService.listar());
	}
}
