package co.edu.unbosque;

import co.edu.unbosque.config.ConexionBD;
import co.edu.unbosque.controller.CasaController;
import co.edu.unbosque.controller.PersonaController;
import co.edu.unbosque.controller.RolController;
import co.edu.unbosque.controller.TelefonoController;
import co.edu.unbosque.controller.VehiculoController;
import co.edu.unbosque.controller.PosesionController;
import co.edu.unbosque.controller.ArriendoController;
import co.edu.unbosque.controller.ResidenciaController;
import co.edu.unbosque.controller.VisitaController;
import co.edu.unbosque.controller.CeldaParqueaderoController;
import co.edu.unbosque.controller.AsignacionCeldaController;
import co.edu.unbosque.controller.ParqueoController;
import co.edu.unbosque.controller.ReservaSalonController;
import co.edu.unbosque.controller.SancionController;
import co.edu.unbosque.controller.ConceptoCobroController;
import co.edu.unbosque.controller.FacturaController;
import co.edu.unbosque.controller.DetalleFacturaController;
import co.edu.unbosque.controller.PagoController;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

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

            //------------------------------ Casa ----------------------------
            config.routes.get("/api/casas", ctx -> casaController.listar(ctx));
            config.routes.exception(SQLException.class, (e, conx) ->
                    conx.status(500).json(Map.of(
                            "error", "Error en la consulta de la BD",
                            "Descripcion", e.getMessage())));
            
            //----------------------------- Persona --------------------------
            config.routes.get("/api/personas", ctx -> pController.listar(ctx));
            config.routes.exception(SQLException.class, (e, conx)->
            		conx.status(500).json(Map.of(
            				"error", "Error en la consulta de la BD", "Descripcion", e.getMessage()))); 
            
            //----------------------------- Roles de persona --------------------------
            config.routes.get("/api/propietarios", ctx -> rolController.listarPropietarios(ctx));
            config.routes.get("/api/arrendatarios", ctx -> rolController.listarArrendatarios(ctx));
            config.routes.get("/api/residentes", ctx -> rolController.listarResidentes(ctx));
            config.routes.get("/api/visitantes", ctx -> rolController.listarVisitantes(ctx));

            //----------------------------- Personas --------------------------
            config.routes.get("/api/telefonos", ctx -> telefonoController.listar(ctx));

            //----------------------------- Parqueadero --------------------------
            config.routes.get("/api/vehiculos", ctx -> vehiculoController.listar(ctx));
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

            //----------------------------- Sanciones --------------------------
            config.routes.get("/api/sanciones", ctx -> sancionController.listar(ctx));

            //----------------------------- Finanzas --------------------------
            config.routes.get("/api/conceptos", ctx -> conceptoCobroController.listar(ctx));
            config.routes.get("/api/facturas", ctx -> facturaController.listar(ctx));
            config.routes.get("/api/detalles-factura", ctx -> detalleFacturaController.listar(ctx));
            config.routes.get("/api/pagos", ctx -> pagoController.listar(ctx));
        }).start(8080);
    }
}