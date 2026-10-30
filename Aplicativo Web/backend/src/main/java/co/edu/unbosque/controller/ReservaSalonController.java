package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;

import co.edu.unbosque.model.ReservaSalon;
import co.edu.unbosque.service.ReservaSalonService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

public class ReservaSalonController {
	
	private final ReservaSalonService reservaSalonService = new ReservaSalonService();
	
	@OpenApi(
			path = "/api/reservas",
			methods = HttpMethod.GET,
			summary = "Listar reservas del salón comunal",
			description = "Devuelve las reservas del salón con los atributos derivados horasReservadas y valorTotal, calculados en la consulta.",
			tags = {"Salón comunal"},
			responses = {
					@OpenApiResponse(status = "200", content = @OpenApiContent(from = ReservaSalon[].class)),
					@OpenApiResponse(status = "500", description = "Error consultando la base de datos")
			})
	public void listar(Context conx) throws SQLException {
		conx.json(reservaSalonService.listar());
	}
}
