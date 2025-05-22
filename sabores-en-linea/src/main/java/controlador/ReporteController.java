package controlador;

import org.hibernate.Session;
import org.hibernate.query.Query;

import util.DatabaseUtil;
import util.HibernateUtil;
import util.ReportUtil;
import util.BirtVersionUtil;

import java.awt.Desktop;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Controlador : generación de informes BIRT
 
public class ReporteController implements Serializable {
    private static final long serialVersionUID = 1L;

    // metodo : verifica la conexión a la BBDD
    
    public boolean verificarConexionBD() {
        Connection conn = null;
        try {
            System.out.println("Verificando conexión a la base de datos...");
            conn = DatabaseUtil.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión a la base de datos establecida correctamente");
                
                // intenta ejecutar una consulta simple
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categorias")) {
                    
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("Número de categorías en la base de datos: " + count);
                    }
                }
                
                return true;
            } else {
                System.err.println("No se pudo establecer una conexión válida a la base de datos");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al verificar la conexión a la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    //metodo : genera informe BIRT (PDF: a partir de un archivo .rptdesign existente)
     
    public void generarInforme(String nombreInforme, String titulo) {
        try {
            // verifica la conexión a la BBDD
            if (!verificarConexionBD()) {
                throw new RuntimeException("No se pudo establecer conexión con la base de datos");
            }
            
            // verifica que el archivo de informe exista
            File reportFile = new File("src/main/java/birt/" + nombreInforme);
            if (!reportFile.exists()) {
                throw new RuntimeException("El archivo de informe no existe: " + nombreInforme);
            }
            
            // crea directorio de salida si no existe
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            
            // configura parámetros del informe
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TITULO", titulo);
            parameters.put("FECHA_GENERACION", new Date());
            
            // Genera el informe en formato PDF
            String outputFileName = "output/" + nombreInforme.replace(".rptdesign", "") + "_" + System.currentTimeMillis() + ".pdf";
            
            // Usa el método existente para generar el informe
            ReportUtil.generateReport(nombreInforme, outputFileName, parameters);
            
            System.out.println("Informe generado: " + outputFileName);
            
         // Abre el archivo PDF automáticamente
            try {
                File pdfFile = new File(outputFileName);
                if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                    System.out.println("Archivo Excel abierto automáticamente: " + outputFileName);
                }
            } catch (Exception e) {
                System.err.println("Error al abrir el archivo PDF: " + e.getMessage());
                // No lanza la excepción para que no interrumpa el flujo si no se puede abrir
            }        
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe: " + e.getMessage(), e);
        }
    }
    
    //genera un informe en formato HTML y lo abre en el navegador
     
    public void generarInformeHTML(String nombreInforme, String titulo) {
        try {
            // verifica la conexión a la base de datos
            if (!verificarConexionBD()) {
                throw new RuntimeException("No se pudo establecer conexión con la base de datos");
            }
            
            // verifica que el archivo de informe exista
            File reportFile = new File("src/main/java/birt/" + nombreInforme);
            if (!reportFile.exists()) {
                throw new RuntimeException("El archivo de informe no existe: " + nombreInforme);
            }
            
            // crea directorio de salida si no existe
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            
            // crea directorio para imágenes si no existe
            File imagesDir = new File("output/images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }
            
            // configura parámetros del informe
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TITULO", titulo);
            parameters.put("FECHA_GENERACION", new Date());
            
            // genera el informe en formato HTML
            String outputFileName = "output/" + nombreInforme.replace(".rptdesign", "") + "_" + System.currentTimeMillis() + ".html";
            ReportUtil.generateHTMLReport(nombreInforme, outputFileName, parameters);
            
            System.out.println("Informe HTML generado: " + outputFileName);
            
            // abre el archivo HTML automáticamente
            try {
                File htmlFile = new File(outputFileName);
                if (htmlFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(htmlFile.toURI());
                    System.out.println("Archivo HTML abierto automáticamente: " + outputFileName);
                }
            } catch (Exception e) {
                System.err.println("Error al abrir el archivo HTML: " + e.getMessage());
                // no lanza la excepción para que no interrumpa el flujo si no se puede abrir
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe HTML: " + e.getMessage(), e);
        }
    }
    
    //genera un informe en formato Excel
    public void generarInformeExcel(String nombreInforme, String titulo) {
        try {
            // verifica la conexión a la base de datos
            if (!verificarConexionBD()) {
                throw new RuntimeException("No se pudo establecer conexión con la base de datos");
            }
            
            // verifica que el archivo de informe exista
            File reportFile = new File("src/main/java/birt/" + nombreInforme);
            if (!reportFile.exists()) {
                throw new RuntimeException("El archivo de informe no existe: " + nombreInforme);
            }
            
            // crea directorio de salida si no existe
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            
            // configura parámetros del informe
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TITULO", titulo);
            parameters.put("FECHA_GENERACION", new Date());
            
            // genera el informe en formato Excel
            String outputFileName = "output/" + nombreInforme.replace(".rptdesign", "") + "_" + System.currentTimeMillis() + ".xls";
            ReportUtil.generateExcelReport(nombreInforme, outputFileName, parameters);
            
            System.out.println("Informe Excel generado: " + outputFileName);
            
            // Abre el archivo HTML automáticamente
            try {
                File htmlFile = new File(outputFileName);
                if (htmlFile.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(htmlFile.toURI());
                    System.out.println("Archivo HTML abierto automáticamente: " + outputFileName);
                }
            } catch (Exception e) {
                System.err.println("Error al abrir el archivo HTML: " + e.getMessage());
                // no lanza la excepción para que no interrumpa el flujo si no se puede abrir
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe Excel: " + e.getMessage(), e);
        }
    }

    // verifica la estructura de una tabla en la BBDD
    
    public void verificarEstructuraTabla(String tableName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM " + tableName + " LIMIT 1");
            rs = stmt.executeQuery();
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            System.out.println("Estructura de la tabla " + tableName + ":");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Columna " + i + ": " + metaData.getColumnName(i) + 
                                  " (" + metaData.getColumnTypeName(i) + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar la estructura de la tabla: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // obtiene estadísticas básicas de la BBDD
     
    public Map<String, Object> obtenerEstadisticasBasicas() {
        Map<String, Object> estadisticas = new HashMap<>();
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // cuenta total de recetas
            Query<Long> queryRecetas = session.createQuery("SELECT COUNT(r) FROM Receta r", Long.class);
            Long totalRecetas = queryRecetas.uniqueResult();
            estadisticas.put("totalRecetas", totalRecetas);
            System.out.println("Total de recetas encontradas: " + totalRecetas);
            
            // cuenta total de usuarios
            Query<Long> queryUsuarios = session.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class);
            Long totalUsuarios = queryUsuarios.uniqueResult();
            estadisticas.put("totalUsuarios", totalUsuarios);
            System.out.println("Total de usuarios encontrados: " + totalUsuarios);
            
            // cuenta total de categorías
            Query<Long> queryCategorias = session.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class);
            Long totalCategorias = queryCategorias.uniqueResult();
            estadisticas.put("totalCategorias", totalCategorias);
            System.out.println("Total de categorías encontradas: " + totalCategorias);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return estadisticas;
    }
    
    // muestra información sobre la versión de BIRT en uso
    
    public void mostrarVersionBIRT() {
        BirtVersionUtil.printBirtVersionInfo();
    }
}