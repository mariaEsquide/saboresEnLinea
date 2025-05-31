package main;

import javax.swing.*;
import vista.LoginFrame;
import util.HibernateUtil;

// Clase principal que inicia la aplicación Sabores en Línea
 
public class Main {

    // método principal : inicia la aplicación
    
    public static void main(String[] args) {
        System.out.println("Iniciando Sabores en Línea...");

        try {
            // configura el Look and Feel personalizado
            configurarLookAndFeel();

            // inicializa Hibernate (conexión a la BBDD)
            System.out.println("Inicializando Hibernate...");
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate inicializado correctamente");

            // asegura que la interfaz se ejecute en el Event Dispatch Thread
            System.out.println("Iniciando interfaz gráfica...");
            SwingUtilities.invokeLater(() -> {
                try {
                    // crea y muestra la ventana de login
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                    System.out.println("Aplicación iniciada correctamente");
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarErrorInicializacion("Error al iniciar la interfaz: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErrorInicializacion("Error al inicializar la aplicación: " + e.getMessage());
        }
    }

    //configura el Look and Feel del sistema compatible con estilos personalizados
   
    private static void configurarLookAndFeel() {
        try {
            
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            System.out.println("Look and Feel configurado: Metal (para mejor compatibilidad con estilos personalizados)");
        } catch (Exception e) {
            System.err.println("No se pudo configurar el Look and Feel: " + e.getMessage());
        }
    }

   
    private static void mostrarErrorInicializacion(String mensaje) {
        System.err.println(mensaje);
        JOptionPane.showMessageDialog(null,
            mensaje + "\nLa aplicación se cerrará.",
            "Error de Inicialización",
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
