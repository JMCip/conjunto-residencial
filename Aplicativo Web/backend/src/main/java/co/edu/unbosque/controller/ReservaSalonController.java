package co.edu.unbosque.controller;

import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.model.ReservaSalon;
import co.edu.unbosque.service.ReservaSalonService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
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
	public void listar(Context ctx) throws SQLException {
		ctx.json(reservaSalonService.listar());
	}

	@OpenApi(
			path = "/api/reservas",
			methods = HttpMethod.POST,
			summary = "Crear reserva del salón",
			description = "Envía horaInicio, horaFinal (yyyy-MM-ddTHH:mm:ss), valorDeposito, valorPorHora, casaNumero y casaManzana. Queda CONFIRMADA.",
			tags = {"Salón comunal"},
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ReservaSalon.class)),
			responses = {
					@OpenApiResponse(status = "201", description = "Creada"),
					@OpenApiResponse(status = "400", description = "Datos inválidos"),
					@OpenApiResponse(status = "404", description = "Casa no encontrada"),
					@OpenApiResponse(status = "409", description = "Mora, sanción pendiente o franja ocupada")
			})
	public void crear(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		ReservaSalon r = ctx.bodyAsClass(ReservaSalon.class);
		int id = reservaSalonService.crear(r);
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Reserva creada", "idReserva", id));
	}

	@OpenApi(
			path = "/api/reservas/{id}",
			methods = HttpMethod.PUT,
			summary = "Cambiar el estado de una reserva del salón",
			description = "Cierra una reserva CONFIRMADA. Envía {\"estado\": \"CANCELADA\"}, {\"estado\": \"FINALIZADA\"} (salón usado, depósito devuelto) o {\"estado\": \"DEPOSITO_RETENIDO\"} (hubo daños, se retiene el depósito). Las horas y los valores no se pueden modificar: para otro horario, cancela y crea una reserva nueva.",
			tags = {"Salón comunal"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ReservaSalon.class)),
			responses = {
					@OpenApiResponse(status = "200", description = "Estado actualizado"),
					@OpenApiResponse(status = "400", description = "Estado inválido"),
					@OpenApiResponse(status = "404", description = "Reserva no encontrada"),
					@OpenApiResponse(status = "409", description = "La reserva no está CONFIRMADA")
			})
	public void actualizar(Context ctx) throws SQLException, DatoInvalidoException,
			RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		ReservaSalon r = ctx.bodyAsClass(ReservaSalon.class);
		reservaSalonService.actualizarEstado(id, r.getEstado());
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Estado de la reserva actualizado"));
	}

	@OpenApi(
			path = "/api/reservas/{id}",
			methods = HttpMethod.DELETE,
			summary = "Eliminar reserva del salón",
			description = "Solo se pueden eliminar reservas CANCELADAS. Para borrar una CONFIRMADA, cancélala primero con el PUT.",
			tags = {"Salón comunal"},
			pathParams = { @OpenApiParam(name = "id", type = Integer.class) },
			responses = {
					@OpenApiResponse(status = "200", description = "Eliminada"),
					@OpenApiResponse(status = "404", description = "Reserva no encontrada"),
					@OpenApiResponse(status = "409", description = "La reserva no está CANCELADA o ya fue facturada")
			})
	public void eliminar(Context ctx) throws SQLException, RecursoNoEncontradoException, ConflictoException {
		int id = Integer.parseInt(ctx.pathParam("id"));
		reservaSalonService.eliminar(id);
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Reserva eliminada"));
	}
}