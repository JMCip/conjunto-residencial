package co.edu.unbosque;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.controller.ArriendoController;
import co.edu.unbosque.controller.AsignacionCeldaController;
import co.edu.unbosque.controller.AuthController;
import co.edu.unbosque.controller.CasaController;
import co.edu.unbosque.controller.CeldaParqueaderoController;
import co.edu.unbosque.controller.ConceptoCobroController;
import co.edu.unbosque.controller.DetalleFacturaController;
import co.edu.unbosque.controller.FacturaController;
import co.edu.unbosque.controller.PagoController;
import co.edu.unbosque.controller.ParqueoController;
import co.edu.unbosque.controller.PersonaController;
import co.edu.unbosque.controller.PosesionController;
import co.edu.unbosque.controller.ReservaSalonController;
import co.edu.unbosque.controller.ResidenciaController;
import co.edu.unbosque.controller.RolController;
import co.edu.unbosque.controller.SancionController;
import co.edu.unbosque.controller.TelefonoController;
import co.edu.unbosque.controller.VehiculoController;
import co.edu.unbosque.controller.VisitaController;
import co.edu.unbosque.exception.ConflictoException;
import co.edu.unbosque.exception.DatoInvalidoException;
import co.edu.unbosque.exception.NoAutorizadoException;
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import co.edu.unbosque.security.JwtAuthenticationFilter;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

/**
 * Punto de entrada del backend del Conjunto Residencial.
 * <p>
 * Crea el servidor Javalin en el puerto {@code 8080}, registra los plugins de OpenAPI y Swagger
 * (con autenticación Bearer global), traduce las excepciones a códigos HTTP, declara las rutas REST
 * de todos los módulos y aplica el filtro JWT antes de cada ruta.
 */
public class App {

	/**
	 * Arranca el servidor HTTP del backend en el puerto {@code 8080} con toda su configuración.
	 *
	 * @param args argumentos de la línea de comandos (no se usan)
	 */
	public static void main(String[] args) {
	    CasaController casaController = new CasaController();
	    PersonaController pController = new PersonaController();
	    RolController rolController = new RolController();
	    TelefonoController telefonoController = new TelefonoController();
	    VehiculoController vehiculoController = new VehiculoController();
	    CeldaParqueaderoController celdaParqueaderoController = new CeldaParqueaderoController();
	    AsignacionCeldaController asignacionCeldaController = new AsignacionCeldaController();
	    ParqueoController parqueoController = new ParqueoController();
	    PosesionController posesionController = new PosesionController();
	    ArriendoController arriendoController = new ArriendoController();
	    ResidenciaController residenciaController = new ResidenciaController();
	    VisitaController visitaController = new VisitaController();
	    ReservaSalonController reservaSalonController = new ReservaSalonController();
	    SancionController sancionController = new SancionController();
	    ConceptoCobroController conceptoCobroController = new ConceptoCobroController();
	    FacturaController facturaController = new FacturaController();
	    DetalleFacturaController detalleFacturaController = new DetalleFacturaController();
	    PagoController pagoController = new PagoController();
	    AuthController authController = new AuthController();
	    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter();

	    Javalin.create(config -> {
	    	config.registerPlugin(new OpenApiPlugin(openApi ->
	        openApi.withDefinitionConfiguration((version, definicion) ->
	                definicion
	                        .info(info -> {
	                            info.title("API Conjunto Residencial");
	                            info.version("1.0");
	                            info.description("Backend del proyecto final de Bases de Datos 1. "
	                                    + "Para usar los endpoints protegidos: haz login en /api/auth/login, "
	                                    + "copia el token y pégalo en el botón Authorize.");
	                        })
	                        .withBearerAuth()
	                        .withGlobalSecurity("BearerAuth"))));
	        config.registerPlugin(new SwaggerPlugin());

	        config.routes.get("/api/estado", ctx -> ctx.result("Backend del Conjunto Residencial funcionando"));

	        config.routes.get("/api/bd/prueba", ctx -> {
	            String sql = "SELECT current_user, current_database(), version()";
	            try (Connection cox = ConexionBD.obtenerConexion();
	                 Statement st = cox.createStatement();
	                 ResultSet rs = st.executeQuery(sql)) {
	                rs.next();
	                ctx.json(Map.of(
	                        "usuario", rs.getString(1),
	                        "base", rs.getString(2),
	                        "version", rs.getString(3)));
	            }
	        });

	        config.routes.exception(DatoInvalidoException.class, (e, conx) ->
	            conx.status(400).json(Map.of("error", e.getMessage())));
	        config.routes.exception(RecursoNoEncontradoException.class, (e, conx) ->
	            conx.status(404).json(Map.of("error", e.getMessage())));
	        config.routes.exception(ConflictoException.class, (e, conx) ->
	            conx.status(409).json(Map.of("error", e.getMessage())));
	        config.routes.exception(com.fasterxml.jackson.core.JsonProcessingException.class, (e, conx) ->
	            conx.status(400).json(Map.of("error", "El cuerpo de la petición falta o no es un JSON válido")));
	        config.routes.exception(NoAutorizadoException.class, (e, conx) ->
	        conx.status(401).json(Map.of("error", e.getMessage())));
	        
	        config.routes.exception(SQLException.class, (e, conx) -> {
	            String estado = e.getSQLState();
	            if ("23505".equals(estado)) {
	                conx.status(409).json(Map.of("error", "Ya existe un registro con esa llave"));
	            } else if ("23503".equals(estado)) {
	                conx.status(409).json(Map.of("error", "Operación no permitida: hay datos relacionados"));
	            } else if ("23514".equals(estado)) {
	                conx.status(400).json(Map.of("error", "El dato no cumple una regla de la base de datos"));
	            } else {
	                conx.status(500).json(Map.of(
	                        "error", "Error en la consulta de la BD",
	                        "Descripcion", e.getMessage()));
	            }
	        });

	        config.routes.get("/api/casas", ctx -> casaController.listar(ctx));
	        config.routes.post("/api/casas", ctx -> casaController.crear(ctx));
	        config.routes.put("/api/casas/{manzana}/{numero}", ctx -> casaController.actualizar(ctx));
	        config.routes.delete("/api/casas/{manzana}/{numero}", ctx -> casaController.eliminar(ctx));

	        config.routes.get("/api/personas", ctx -> pController.listar(ctx));
	        config.routes.get("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.consultar(ctx));
	        config.routes.post("/api/personas", ctx -> pController.crear(ctx));
	        config.routes.put("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.actualizar(ctx));
	        config.routes.delete("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.eliminar(ctx));

	        config.routes.get("/api/propietarios", ctx -> rolController.listarPropietarios(ctx));
	        config.routes.get("/api/arrendatarios", ctx -> rolController.listarArrendatarios(ctx));
	        config.routes.get("/api/residentes", ctx -> rolController.listarResidentes(ctx));
	        config.routes.get("/api/visitantes", ctx -> rolController.listarVisitantes(ctx));
	        config.routes.post("/api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}", ctx -> rolController.asignar(ctx));
	        config.routes.delete("/api/personas/{tipoDocumento}/{numeroDocumento}/roles/{rol}", ctx -> rolController.quitar(ctx));

	        config.routes.get("/api/telefonos", ctx -> telefonoController.listar(ctx));
	        config.routes.get("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.consultar(ctx));
	        config.routes.post("/api/telefonos", ctx -> telefonoController.crear(ctx));
	        config.routes.put("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.actualizar(ctx));
	        config.routes.delete("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.eliminar(ctx));

	        config.routes.get("/api/vehiculos", ctx -> vehiculoController.listar(ctx));
	        config.routes.get("/api/vehiculos/{placa}", ctx -> vehiculoController.consultar(ctx));
	        config.routes.post("/api/vehiculos", ctx -> vehiculoController.crear(ctx));
	        config.routes.put("/api/vehiculos/{placa}", ctx -> vehiculoController.actualizar(ctx));
	        config.routes.delete("/api/vehiculos/{placa}", ctx -> vehiculoController.eliminar(ctx));
	        config.routes.get("/api/celdas", ctx -> celdaParqueaderoController.listar(ctx));
	        config.routes.get("/api/celdas/{id}", ctx -> celdaParqueaderoController.consultar(ctx));
	        config.routes.post("/api/celdas", ctx -> celdaParqueaderoController.crear(ctx));
	        config.routes.put("/api/celdas/{id}", ctx -> celdaParqueaderoController.actualizar(ctx));
	        config.routes.delete("/api/celdas/{id}", ctx -> celdaParqueaderoController.eliminar(ctx));
	        config.routes.get("/api/asignaciones-celda", ctx -> asignacionCeldaController.listar(ctx));
	        config.routes.get("/api/asignaciones-celda/{id}", ctx -> asignacionCeldaController.consultar(ctx));
	        config.routes.post("/api/asignaciones-celda", ctx -> asignacionCeldaController.crear(ctx));
	        config.routes.put("/api/asignaciones-celda/{id}", ctx -> asignacionCeldaController.actualizar(ctx));
	        config.routes.delete("/api/asignaciones-celda/{id}", ctx -> asignacionCeldaController.eliminar(ctx));
	        config.routes.get("/api/parqueos", ctx -> parqueoController.listar(ctx));
	        config.routes.get("/api/parqueos/{id}", ctx -> parqueoController.consultar(ctx));
	        config.routes.post("/api/parqueos", ctx -> parqueoController.crear(ctx));
	        config.routes.put("/api/parqueos/{id}", ctx -> parqueoController.actualizar(ctx));
	        config.routes.delete("/api/parqueos/{id}", ctx -> parqueoController.eliminar(ctx));

	        config.routes.get("/api/posesiones", ctx -> posesionController.listar(ctx));
	        config.routes.get("/api/posesiones/{id}", ctx -> posesionController.consultar(ctx));
	        config.routes.post("/api/posesiones", ctx -> posesionController.crear(ctx));
	        config.routes.put("/api/posesiones/{id}", ctx -> posesionController.actualizar(ctx));
	        config.routes.delete("/api/posesiones/{id}", ctx -> posesionController.eliminar(ctx));
	        config.routes.get("/api/arriendos", ctx -> arriendoController.listar(ctx));
	        config.routes.get("/api/arriendos/{id}", ctx -> arriendoController.consultar(ctx));
	        config.routes.post("/api/arriendos", ctx -> arriendoController.crear(ctx));
	        config.routes.put("/api/arriendos/{id}", ctx -> arriendoController.actualizar(ctx));
	        config.routes.delete("/api/arriendos/{id}", ctx -> arriendoController.eliminar(ctx));
	        config.routes.get("/api/residencias", ctx -> residenciaController.listar(ctx));
	        config.routes.get("/api/residencias/{id}", ctx -> residenciaController.consultar(ctx));
	        config.routes.post("/api/residencias", ctx -> residenciaController.crear(ctx));
	        config.routes.put("/api/residencias/{id}", ctx -> residenciaController.actualizar(ctx));
	        config.routes.delete("/api/residencias/{id}", ctx -> residenciaController.eliminar(ctx));
	        config.routes.get("/api/visitas", ctx -> visitaController.listar(ctx));
	        config.routes.get("/api/visitas/{id}", ctx -> visitaController.consultar(ctx));
	        config.routes.post("/api/visitas", ctx -> visitaController.crear(ctx));
	        config.routes.put("/api/visitas/{id}", ctx -> visitaController.actualizar(ctx));
	        config.routes.delete("/api/visitas/{id}", ctx -> visitaController.eliminar(ctx));

	        config.routes.get("/api/reservas", ctx -> reservaSalonController.listar(ctx));
	        config.routes.post("/api/reservas", ctx -> reservaSalonController.crear(ctx));
	        config.routes.put("/api/reservas/{id}", ctx -> reservaSalonController.actualizar(ctx));
	        config.routes.delete("/api/reservas/{id}", ctx -> reservaSalonController.eliminar(ctx));

	        config.routes.get("/api/sanciones", ctx -> sancionController.listar(ctx));
	        config.routes.post("/api/sanciones", ctx -> sancionController.crear(ctx));
	        config.routes.put("/api/sanciones/{id}", ctx -> sancionController.actualizar(ctx));
	        config.routes.delete("/api/sanciones/{id}", ctx -> sancionController.eliminar(ctx));

	        config.routes.get("/api/conceptos", ctx -> conceptoCobroController.listar(ctx));
	        config.routes.post("/api/conceptos", ctx -> conceptoCobroController.crear(ctx));
	        config.routes.put("/api/conceptos/{id}", ctx -> conceptoCobroController.actualizar(ctx));
	        config.routes.delete("/api/conceptos/{id}", ctx -> conceptoCobroController.eliminar(ctx));

	        config.routes.get("/api/facturas", ctx -> facturaController.listar(ctx));
	        config.routes.post("/api/facturas", ctx -> facturaController.crear(ctx));
	        config.routes.post("/api/facturas/marcar-vencidas", ctx -> facturaController.marcarVencidas(ctx));
	        config.routes.put("/api/facturas/{id}", ctx -> facturaController.actualizar(ctx));
	        config.routes.delete("/api/facturas/{id}", ctx -> facturaController.eliminar(ctx));

	        config.routes.get("/api/detalles-factura", ctx -> detalleFacturaController.listar(ctx));
	        config.routes.post("/api/detalles-factura", ctx -> detalleFacturaController.crear(ctx));
	        config.routes.put("/api/detalles-factura/{factura}/{linea}", ctx -> detalleFacturaController.actualizar(ctx));
	        config.routes.delete("/api/detalles-factura/{factura}/{linea}", ctx -> detalleFacturaController.eliminar(ctx));
	        
	        config.routes.get("/api/pagos", ctx -> pagoController.listar(ctx));
	        config.routes.post("/api/pagos", ctx -> pagoController.crear(ctx));
	        
	        config.routes.post("/api/auth/login", ctx -> authController.login(ctx));

	        config.routes.beforeMatched(ctx -> jwtFilter.filtrar(ctx));
	        
	    }).start(8080);
	}
}