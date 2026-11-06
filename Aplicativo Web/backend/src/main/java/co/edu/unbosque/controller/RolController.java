package co.edu.unbosque.controller;

import io.javalin.http.Context;
import java.sql.SQLException;
import java.util.Map;

import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.OpenApiParam;
import co.edu.unbosque.model.Persona;
import co.edu.unbosque.service.RolService;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;

/**
 * Controlador REST de los roles de las personas (rutas {@code /api/propietarios},
 * {@code /api/arrendatarios}, {@code /api/residentes}, {@code /api/visitantes} y
 * {@code /api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}}).
 * <p>
 * Lista las personas por rol y permite asignar o quitar un rol. Las excepciones se traducen a
 * códigos HTTP en {@link co.edu.unbosque.App}.
 */
public class RolController {
	
	/** Servicio con la lógica de negocio de los roles de las personas. */
	private final RolService rolService = new RolService();
	
	/**
	 * Atiende {@code GET /api/propietarios} (listar propietarios). Devuelve las personas que son
	 * propietarias de alguna casa.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
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
	
	/**
	 * Atiende {@code GET /api/arrendatarios} (listar arrendatarios). Devuelve las personas registradas
	 * como arrendatarias.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
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
	
	/**
	 * Atiende {@code GET /api/residentes} (listar residentes). Devuelve las personas registradas como
	 * residentes.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
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
	
	/**
	 * Atiende {@code GET /api/visitantes} (listar visitantes). Devuelve las personas registradas como
	 * visitantes.
	 *
	 * @param conx contexto de la petición y la respuesta HTTP
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 */
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
	
	/**
	 * Atiende {@code POST /api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}} (asignar rol a
	 * una persona). rol: propietario, arrendatario, residente o visitante. La persona debe existir.
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code rol}, {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 * @throws ConflictoException si la operación viola una regla de negocio (HTTP 409)
	 */
	@OpenApi(
			path = "/api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}",
			methods = HttpMethod.POST,
			summary = "Asignar rol a una persona",
			description = "rol: propietario, arrendatario, residente o visitante. La persona debe existir.",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class),
					@OpenApiParam(name = "rol", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "201", description = "Rol asignado"),
					@OpenApiResponse(status = "400", description = "Rol inválido"),
					@OpenApiResponse(status = "404", description = "Persona no encontrada"),
					@OpenApiResponse(status = "409", description = "La persona ya tiene ese rol")
			})
	public void asignar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException, ConflictoException {
		rolService.asignar(ctx.pathParam("rol"), ctx.pathParam("tipoDocumento"), ctx.pathParam("numeroDocumento"));
		ctx.status(HttpStatus.CREATED).json(Map.of("mensaje", "Rol asignado"));
	}

	/**
	 * Atiende {@code DELETE /api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}} (quitar rol a
	 * una persona).
	 *
	 * @param ctx contexto de la petición y la respuesta HTTP; de él se leen los parámetros de ruta
	 *         {@code rol}, {@code tipoDocumento} y {@code numeroDocumento}
	 * @throws SQLException si falla el acceso a la base de datos; se traduce a HTTP según su SQLState
	 * @throws DatoInvalidoException si el cuerpo o los parámetros no son válidos (HTTP 400)
	 * @throws RecursoNoEncontradoException si el registro no existe (HTTP 404)
	 */
	@OpenApi(
			path = "/api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}",
			methods = HttpMethod.DELETE,
			summary = "Quitar rol a una persona",
			tags = {"Personas"},
			pathParams = {
					@OpenApiParam(name = "tipoDocumento", type = String.class),
					@OpenApiParam(name = "numeroDocumento", type = String.class),
					@OpenApiParam(name = "rol", type = String.class)
			},
			responses = {
					@OpenApiResponse(status = "200", description = "Rol quitado"),
					@OpenApiResponse(status = "400", description = "Rol inválido"),
					@OpenApiResponse(status = "404", description = "La persona no tiene ese rol"),
					@OpenApiResponse(status = "409", description = "El rol tiene datos asociados (posesiones, arriendos, visitas...)")
			})
	public void quitar(Context ctx)
			throws SQLException, DatoInvalidoException, RecursoNoEncontradoException {
		rolService.quitar(ctx.pathParam("rol"), ctx.pathParam("tipoDocumento"), ctx.pathParam("numeroDocumento"));
		ctx.status(HttpStatus.OK).json(Map.of("mensaje", "Rol quitado"));
	}
	
}
