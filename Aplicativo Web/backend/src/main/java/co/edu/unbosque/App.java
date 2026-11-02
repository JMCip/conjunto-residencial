package co.edu.unbosque;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.controller.ArriendoController;
import co.edu.unbosque.controller.AsignacionCeldaController;
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
import co.edu.unbosque.exception.RecursoNoEncontradoException;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

public class App {

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

	    Javalin.create(config -> {
	        config.registerPlugin(new OpenApiPlugin(openApi -> {}));
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
	        //------------------------------ Manejo de errores de BD ----------------------------
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
	        //------------------------------ Casa ----------------------------
	        config.routes.get("/api/casas", ctx -> casaController.listar(ctx));
	        config.routes.post("/api/casas", ctx -> casaController.crear(ctx));
	        config.routes.put("/api/casas/{manzana}/{numero}", ctx -> casaController.actualizar(ctx));
	        config.routes.delete("/api/casas/{manzana}/{numero}", ctx -> casaController.eliminar(ctx));

	        //----------------------------- Persona --------------------------
	        config.routes.get("/api/personas", ctx -> pController.listar(ctx));
	        config.routes.get("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.consultar(ctx));
	        config.routes.post("/api/personas", ctx -> pController.crear(ctx));
	        config.routes.put("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.actualizar(ctx));
	        config.routes.delete("/api/personas/{tipoDocumento}/{numeroDocumento}", ctx -> pController.eliminar(ctx));

	        //----------------------------- Roles de persona --------------------------
	        config.routes.get("/api/propietarios", ctx -> rolController.listarPropietarios(ctx));
	        config.routes.get("/api/arrendatarios", ctx -> rolController.listarArrendatarios(ctx));
	        config.routes.get("/api/residentes", ctx -> rolController.listarResidentes(ctx));
	        config.routes.get("/api/visitantes", ctx -> rolController.listarVisitantes(ctx));

	        //----------------------------- Telefono --------------------------
	        config.routes.get("/api/telefonos", ctx -> telefonoController.listar(ctx));
	        config.routes.get("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.consultar(ctx));
	        config.routes.post("/api/telefonos", ctx -> telefonoController.crear(ctx));
	        config.routes.put("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.actualizar(ctx));
	        config.routes.delete("/api/telefonos/{tipoDocumento}/{numeroDocumento}/{telefono}", ctx -> telefonoController.eliminar(ctx));

	        //----------------------------- Parqueadero --------------------------
	        config.routes.get("/api/vehiculos", ctx -> vehiculoController.listar(ctx));
	        config.routes.get("/api/vehiculos", ctx -> vehiculoController.listar(ctx));
	        config.routes.get("/api/vehiculos/{placa}", ctx -> vehiculoController.consultar(ctx));
	        config.routes.post("/api/vehiculos", ctx -> vehiculoController.crear(ctx));
	        config.routes.put("/api/vehiculos/{placa}", ctx -> vehiculoController.actualizar(ctx));
	        config.routes.delete("/api/vehiculos/{placa}", ctx -> vehiculoController.eliminar(ctx));
	        config.routes.get("/api/celdas", ctx -> celdaParqueaderoController.listar(ctx));
	        config.routes.get("/api/asignaciones-celda", ctx -> asignacionCeldaController.listar(ctx));
	        config.routes.get("/api/parqueos", ctx -> parqueoController.listar(ctx));

	        //----------------------------- Casas --------------------------
	        config.routes.get("/api/posesiones", ctx -> posesionController.listar(ctx));
	        config.routes.get("/api/arriendos", ctx -> arriendoController.listar(ctx));
	        config.routes.get("/api/residencias", ctx -> residenciaController.listar(ctx));
	        config.routes.get("/api/visitas", ctx -> visitaController.listar(ctx));

	        //----------------------------- Salón comunal --------------------------
	        config.routes.get("/api/reservas", ctx -> reservaSalonController.listar(ctx));
	        config.routes.post("/api/reservas", ctx -> reservaSalonController.crear(ctx));
	        config.routes.put("/api/reservas/{id}", ctx -> reservaSalonController.actualizar(ctx));
	        config.routes.delete("/api/reservas/{id}", ctx -> reservaSalonController.eliminar(ctx));

	        //----------------------------- Sanciones --------------------------
	        config.routes.get("/api/sanciones", ctx -> sancionController.listar(ctx));
	        config.routes.post("/api/sanciones", ctx -> sancionController.crear(ctx));
	        config.routes.put("/api/sanciones/{id}", ctx -> sancionController.actualizar(ctx));
	        config.routes.delete("/api/sanciones/{id}", ctx -> sancionController.eliminar(ctx));

	        //----------------------------- Finanzas --------------------------
	        config.routes.get("/api/conceptos", ctx -> conceptoCobroController.listar(ctx));
	        config.routes.get("/api/facturas", ctx -> facturaController.listar(ctx));
	        config.routes.get("/api/detalles-factura", ctx -> detalleFacturaController.listar(ctx));
	        config.routes.get("/api/pagos", ctx -> pagoController.listar(ctx));
	    }).start(8080);
	}
}