package util;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// Clase de utilidad para generar informes utilizando BIRT a partir de archivos .rptdesign (creados con BIRT Designer)
 
public class ReportUtil implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = Logger.getLogger(ReportUtil.class.getName());
    private static IReportEngine engine = null;
    
    // ruta unificada para los informes
    public static final String REPORT_PATH = "src/main/java/birt/";
    
    // verifica que el entorno BIRT esté correctamente configurado
     
    public static boolean verificarEntornoBIRT() {
        try {
            // verifica que las clases de BIRT estén disponibles
            Class.forName("org.eclipse.birt.report.engine.api.EngineConfig");
            
            // verifica que las carpetas necesarias existan
            File[] dirs = {
                new File(REPORT_PATH),
                new File("output"),
                new File("output/images")
            };
            
            for (File dir : dirs) {
                if (!dir.exists()) {
                    dir.mkdirs();
                    System.out.println("Carpeta creada en: " + dir.getAbsolutePath());
                }
            }
            
            System.out.println("Entorno BIRT verificado correctamente");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se encontraron las clases de BIRT. Verifica tus dependencias.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("ERROR al verificar el entorno BIRT: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // genera un informe en formato PDF
   
    @SuppressWarnings("unchecked")
	public static void generateReport(String reportName, String outputFileName, Map<String, Object> parameters) {
        IReportEngine engine = null;

        try {
            // verifica que el archivo de informe existe
            File reportFile = new File(REPORT_PATH + reportName);
            if (!reportFile.exists()) {
                logger.severe("El archivo de informe no existe: " + REPORT_PATH + reportName);
                throw new RuntimeException("El archivo de informe no existe: " + REPORT_PATH + reportName);
            }
            
            // verifica y ajusta la versión del informe si es necesario
            if (!BirtVersionUtil.checkAndFixReportVersion(REPORT_PATH + reportName)) {
                throw new RuntimeException("No se pudo verificar o ajustar la versión del informe");
            }
            
            logger.info("Generando informe desde: " + REPORT_PATH + reportName);
            
            // configura el motor de BIRT
            EngineConfig config = new EngineConfig();
            config.setResourcePath(new File(REPORT_PATH).getAbsolutePath());
            
            // inicializa la plataforma BIRT
            Platform.startup(config);

            // crea la fábrica del motor de informes
            IReportEngineFactory factory = (IReportEngineFactory) Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            // crea el motor de informes
            engine = factory.createReportEngine(config);

            // registra excepciones
            engine.getConfig().setLogConfig(null, Level.WARNING);

            // abre el diseño del informe
            IReportRunnable design = engine.openReportDesign(REPORT_PATH + reportName);

            // crea opciones de renderizado
            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");
            options.setOutputFileName(outputFileName);
            
            // configura opciones de PDF
            options.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
            
            // crea tarea de ejecución y renderizado
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            // configura la conexión JDBC directamente
            Connection conn = null;
            try {
                conn = DatabaseUtil.getConnection();
                if (conn != null) {
                    task.getAppContext().put("OdaJDBCDriverPassInConnection", conn);
                    System.out.println("Conexión JDBC configurada para el informe");
                }
            } catch (Exception e) {
                System.err.println("Error al configurar conexión JDBC para el informe: " + e.getMessage());
                e.printStackTrace();
                throw e; // relanza la excepción para detener el proceso
            }

            // establece parámetros
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    task.setParameterValue(entry.getKey(), entry.getValue());
                }
            }

            // valida parámetros
            task.validateParameters();

            // establece opciones de renderizado
            task.setRenderOption(options);

            // ejecuta tarea
            task.run();

            // cierra tarea
            task.close();

            // cierra la conexión JDBC
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }

            logger.info("Informe generado correctamente: " + outputFileName);

            // verifica que el archivo se ha creado correctamente
            File outputFile = new File(outputFileName);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new RuntimeException("El archivo de salida no se ha creado correctamente o está vacío: " + outputFileName);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar el informe", e);
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe: " + e.getMessage(), e);
        } finally {
            // cierra el motor y apagar la plataforma
            if (engine != null) {
                engine.destroy();
                Platform.shutdown();
            }
        }
    }

    // genera un informe en formato HTML
    
    @SuppressWarnings("unchecked")
	public static void generateHTMLReport(String reportName, String outputFileName, Map<String, Object> parameters) {
        IReportEngine engine = null;

        try {
            // verifica que el archivo de informe existe
            File reportFile = new File(REPORT_PATH + reportName);
            if (!reportFile.exists()) {
                logger.severe("El archivo de informe no existe: " + REPORT_PATH + reportName);
                throw new RuntimeException("El archivo de informe no existe: " + REPORT_PATH + reportName);
            }
            
            // verifica y ajusta la versión del informe si es necesario
            if (!BirtVersionUtil.checkAndFixReportVersion(REPORT_PATH + reportName)) {
                throw new RuntimeException("No se pudo verificar o ajustar la versión del informe");
            }
            
            logger.info("Generando informe HTML desde: " + REPORT_PATH + reportName);
            
            // configura el motor de BIRT
            EngineConfig config = new EngineConfig();
            config.setResourcePath(new File(REPORT_PATH).getAbsolutePath());
            
            // inicializa la plataforma BIRT
            Platform.startup(config);

            // crea la fábrica del motor de informes
            IReportEngineFactory factory = (IReportEngineFactory) Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            // crea el motor de informes
            engine = factory.createReportEngine(config);

            // abre el diseño del informe
            IReportRunnable design = engine.openReportDesign(REPORT_PATH + reportName);

            // crea opciones de renderizado HTML
            HTMLRenderOption options = new HTMLRenderOption();
            options.setOutputFormat("html");
            options.setOutputFileName(outputFileName);
            options.setEmbeddable(false);
            options.setHtmlPagination(false);
            
            // configura directorio de imágenes
            File imagesDir = new File("output/images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            options.setImageDirectory(imagesDir.getAbsolutePath());
            options.setBaseImageURL("images");
            options.setImageHandler(new HTMLCompleteImageHandler());
            
            // crea tarea de ejecución y renderizado
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            // configura la conexión JDBC directamente
            Connection conn = null;
            try {
                conn = DatabaseUtil.getConnection();
                if (conn != null) {
                    task.getAppContext().put("OdaJDBCDriverPassInConnection", conn);
                    System.out.println("Conexión JDBC configurada para el informe HTML");
                }
            } catch (Exception e) {
                System.err.println("Error al configurar conexión JDBC para el informe HTML: " + e.getMessage());
                e.printStackTrace();
                throw e; // relanza la excepción para detener el proceso
            }

            // establece parámetros
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    task.setParameterValue(entry.getKey(), entry.getValue());
                }
            }

            task.validateParameters();
            task.setRenderOption(options);
            task.run();
            task.close();

            // cierra la conexión JDBC
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }

            logger.info("Informe HTML generado correctamente: " + outputFileName);

            // verifica que el archivo se ha creado correctamente
            File outputFile = new File(outputFileName);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new RuntimeException("El archivo de salida HTML no se ha creado correctamente o está vacío: " + outputFileName);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar el informe HTML", e);
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe HTML: " + e.getMessage(), e);
        } finally {
            // cierra el motor y apaga la plataforma
            if (engine != null) {
                engine.destroy();
                Platform.shutdown();
            }
        }
    }
    
    // genera un informe en formato Excel
   
    @SuppressWarnings("unchecked")
	public static void generateExcelReport(String reportName, String outputFileName, Map<String, Object> parameters) {
        IReportEngine engine = null;

        try {
            // verifica que el archivo de informe existe
            File reportFile = new File(REPORT_PATH + reportName);
            if (!reportFile.exists()) {
                logger.severe("El archivo de informe no existe: " + REPORT_PATH + reportName);
                throw new RuntimeException("El archivo de informe no existe: " + REPORT_PATH + reportName);
            }
            
            // verifica y ajusta la versión del informe si es necesario
            if (!BirtVersionUtil.checkAndFixReportVersion(REPORT_PATH + reportName)) {
                throw new RuntimeException("No se pudo verificar o ajustar la versión del informe");
            }
            
            logger.info("Generando informe Excel desde: " + REPORT_PATH + reportName);
            
            // configurar el motor de BIRT
            EngineConfig config = new EngineConfig();
            config.setResourcePath(new File(REPORT_PATH).getAbsolutePath());
            
            // inicializa la plataforma BIRT
            Platform.startup(config);

            // crea la fábrica del motor de informes
            IReportEngineFactory factory = (IReportEngineFactory) Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            // crea el motor de informes
            engine = factory.createReportEngine(config);

            // abre el diseño del informe
            IReportRunnable design = engine.openReportDesign(REPORT_PATH + reportName);

            // crea opciones de renderizado Excel
            RenderOption options = new RenderOption();
            options.setOutputFormat("xls");  // Usar "xls" en lugar de "xlsx"
            options.setOutputFileName(outputFileName);
            
            // crea tarea de ejecución y renderizado
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            // configura la conexión JDBC directamente
            Connection conn = null;
            try {
                conn = DatabaseUtil.getConnection();
                if (conn != null) {
                    task.getAppContext().put("OdaJDBCDriverPassInConnection", conn);
                    System.out.println("Conexión JDBC configurada para el informe Excel");
                }
            } catch (Exception e) {
                System.err.println("Error al configurar conexión JDBC para el informe Excel: " + e.getMessage());
                e.printStackTrace();
                throw e; // relanza la excepción para detener el proceso
            }

            // establece parámetros
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    task.setParameterValue(entry.getKey(), entry.getValue());
                }
            }

            task.validateParameters();

            task.setRenderOption(options);

            task.run();

            task.close();

            // cierra la conexión JDBC
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }

            logger.info("Informe Excel generado correctamente: " + outputFileName);

            // verifica que el archivo se ha creado correctamente
            File outputFile = new File(outputFileName);
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new RuntimeException("El archivo de salida Excel no se ha creado correctamente o está vacío: " + outputFileName);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al generar el informe Excel", e);
            e.printStackTrace();
            throw new RuntimeException("Error al generar el informe Excel: " + e.getMessage(), e);
        } finally {
            // cierra el motor y apagar la plataforma
            if (engine != null) {
                engine.destroy();
                Platform.shutdown();
            }
        }
    }
    
    // verifica si una consulta SQL devuelve resultados.

    public static boolean verificarConsultaSQL(String sqlQuery) {
        Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlQuery);
            
            boolean tieneResultados = rs.next();
            
            if (tieneResultados) {
                System.out.println("La consulta SQL devuelve resultados");
                
                // muestra la primera fila como ejemplo
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                System.out.println("Primera fila de resultados:");
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    System.out.println(columnName + ": " + value);
                }
            } else {
                System.out.println("La consulta SQL NO devuelve resultados");
            }
            
            return tieneResultados;
        } catch (Exception e) {
            System.err.println("Error al verificar la consulta SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
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

	public static IReportEngine getEngine() {
		return engine;
	}

	public static void setEngine(IReportEngine engine) {
		ReportUtil.engine = engine;
	}
}