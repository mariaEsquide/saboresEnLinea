package util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

// Utilidad simplificada para verificar archivos BIRT

public class BirtVersionUtil {
    
    private static final Logger logger = Logger.getLogger(BirtVersionUtil.class.getName());
    
    // verifica si el archivo de informe existe
    
    public static boolean checkAndFixReportVersion(String reportPath) {
        try {
            File reportFile = new File(reportPath);
            if (!reportFile.exists()) {
                logger.severe("El archivo de informe no existe: " + reportPath);
                return false;
            }
            
            logger.info("Archivo de informe verificado: " + reportPath);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al verificar el informe", e);
            return false;
        }
    }
    
    // metodo simplificado : compatibilidad
     
    public static void printBirtVersionInfo() {
        logger.info("Información de versión de BIRT no disponible en esta versión simplificada.");
    }
}