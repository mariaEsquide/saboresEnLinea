package vista;

import controlador.ReporteController;
import modelo.SessionManager;
import util.EstiloManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.Map;

// Ventana para la generación de informes BIRT.
 
public class ReportGenerationFrame extends BaseFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private JButton recetasPopularesButton;
    private JButton usuariosActivosButton;
    private JButton categoriasButton;
    private JPanel estadisticasPanel;
    private ReporteController reporteController;
    
    // constantes para los nombres de informes
    private static final String INFORME_RECETAS = "InformeRecetasPopulares.rptdesign";
    private static final String INFORME_USUARIOS = "InformeUsuariosActivos.rptdesign";
    private static final String INFORME_CATEGORIAS = "InformeCategorias.rptdesign";
    
    public ReportGenerationFrame() {
        super("Generación de Informes");
        this.reporteController = new ReporteController();
        
        // verifica si el usuario es administrador
        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(this,
                "Solo los administradores pueden acceder a esta función",
                "Acceso denegado",
                JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }
        
        // verifica si existe la carpeta de informes
        File reportDir = new File("src/main/java/birt");
        if (!reportDir.exists() || !reportDir.isDirectory()) {
            reportDir.mkdirs();
            System.out.println("Carpeta de informes creada: " + reportDir.getAbsolutePath());
        }
        
        // verifica si existe la carpeta de salida
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        
        setupComponents();
    }

    // configura los componentes de la interfaz gráfica.
    
    @Override
    protected void setupComponents() {
        JPanel contentPanel = createContentPanel();
        contentPanel.setLayout(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de estadísticas
        estadisticasPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        estadisticasPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        cargarEstadisticas();
        
        // Panel principal que contiene los informes y botones
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel de informes disponibles
        JPanel informesPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        informesPanel.setBorder(BorderFactory.createTitledBorder("Informes Disponibles"));
        
        // crea botones para cada informe
        recetasPopularesButton = new JButton("Informe de Recetas Populares");
        EstiloManager.aplicarEstiloBoton(recetasPopularesButton);
        recetasPopularesButton.addActionListener(e -> mostrarOpcionesFormato(INFORME_RECETAS, "Recetas Populares"));
        
        usuariosActivosButton = new JButton("Informe de Usuarios Activos");
        EstiloManager.aplicarEstiloBoton(usuariosActivosButton);
        usuariosActivosButton.addActionListener(e -> mostrarOpcionesFormato(INFORME_USUARIOS, "Usuarios Activos"));
        
        categoriasButton = new JButton("Informe de Categorías");
        EstiloManager.aplicarEstiloBoton(categoriasButton);
        categoriasButton.addActionListener(e -> mostrarOpcionesFormato(INFORME_CATEGORIAS, "Categorías"));
        
        // añade botones al panel
        informesPanel.add(recetasPopularesButton);
        informesPanel.add(usuariosActivosButton);
        informesPanel.add(categoriasButton);
        
        mainContentPanel.add(informesPanel, BorderLayout.CENTER);
        
        contentPanel.add(estadisticasPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    // muestra un diálogo para elegir el formato del informe
 
    private void mostrarOpcionesFormato(String nombreInforme, String titulo) {
        // verifica si el archivo de informe existe
        File reportFile = new File("src/main/java/birt/" + nombreInforme);
        if (!reportFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "El archivo de informe '" + nombreInforme + "' no existe.\n" +
                "Debes crear primero el informe con BIRT Designer y guardarlo en la carpeta 'src/main/java/birt'.",
                "Archivo no encontrado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // crea un JDialog modal con el estilo de la aplicación
        JDialog dialog = new JDialog(this, "Seleccionar Formato", true);
        dialog.setIconImage(getIconImage());
        
        // Panel con el formulario
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(EstiloManager.COLOR_FONDO);
        
        // componentes del formulario
        JLabel formatoLabel = new JLabel("Formato:");
        formatoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // grupo de radio buttons para el formato
        JRadioButton pdfRadio = new JRadioButton("PDF");
        pdfRadio.setBackground(EstiloManager.COLOR_FONDO);
        pdfRadio.setSelected(true);
        
        JRadioButton htmlRadio = new JRadioButton("HTML");
        htmlRadio.setBackground(EstiloManager.COLOR_FONDO);
        
        JRadioButton excelRadio = new JRadioButton("Excel");
        excelRadio.setBackground(EstiloManager.COLOR_FONDO);
        
        ButtonGroup formatoGroup = new ButtonGroup();
        formatoGroup.add(pdfRadio);
        formatoGroup.add(htmlRadio);
        formatoGroup.add(excelRadio);
        
        JButton guardarButton = new JButton("Generar");
        EstiloManager.aplicarEstiloBoton(guardarButton);
        
        // configura layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // agrega componentes
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(formatoLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(pdfRadio, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(htmlRadio, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(excelRadio, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(guardarButton, gbc);
        
        // acción del botón guardar
        guardarButton.addActionListener(e -> {
            dialog.dispose();
            
            if (pdfRadio.isSelected()) {
                generarInforme(nombreInforme, titulo, "pdf");
            } else if (htmlRadio.isSelected()) {
                generarInforme(nombreInforme, titulo, "html");
            } else if (excelRadio.isSelected()) {
                generarInforme(nombreInforme, titulo, "excel");
            }
        });
        
        // configura y muestra el diálogo
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    // genera un informe en el formato especificado (pdf, html, excel)
     
    private void generarInforme(String nombreInforme, String titulo, String formato) {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // sms según el formato
            String mensaje = "Generando informe en formato " + formato.toUpperCase() + "...";
            
            // muestra diálogo de progreso
            final JDialog progressDialog = new JDialog(this, "Procesando", true);
            progressDialog.setLayout(new BorderLayout());
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);
            
            JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
            
            JLabel mensajeLabel = new JLabel(mensaje);
            mensajeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(mensajeLabel, BorderLayout.CENTER);
            panel.add(progressBar, BorderLayout.SOUTH);
            
            progressDialog.add(panel);
            progressDialog.setSize(300, 150);
            progressDialog.setLocationRelativeTo(this);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            
            // crea un hilo para generar el informe
            Thread generarThread = new Thread(() -> {
                try {
                    // genera el informe según el formato
                    if ("pdf".equals(formato)) {
                        reporteController.generarInforme(nombreInforme, titulo);
                    } else if ("html".equals(formato)) {
                        reporteController.generarInformeHTML(nombreInforme, titulo);
                    } else if ("excel".equals(formato)) {
                        reporteController.generarInformeExcel(nombreInforme, titulo);
                    }
                    
                    // cierra el diálogo de progreso
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        setCursor(Cursor.getDefaultCursor());
                        
                        // muestra sms de éxito
                        JOptionPane.showMessageDialog(
                            ReportGenerationFrame.this,
                            "El informe se ha generado correctamente en la carpeta 'output'.",
                            "Informe Generado",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    });
                } catch (Exception ex) {
                    // cierra el diálogo de progreso y mostrar error
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        setCursor(Cursor.getDefaultCursor());
                        
                        JOptionPane.showMessageDialog(
                            ReportGenerationFrame.this,
                            "Error al generar el informe: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    });
                }
            });
            
            // inicia el hilo y mostrar el diálogo
            generarThread.start();
            progressDialog.setVisible(true);
            
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                this,
                "Error al generar el informe: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }
    
    // carga las estadísticas básicas en el panel de estadísticas

    private void cargarEstadisticas() {
        try {
            Map<String, Object> estadisticas = reporteController.obtenerEstadisticasBasicas();
            
            estadisticasPanel.removeAll();
            
            // crea etiquetas con estilo
            JLabel recetasLabel = new JLabel("Total de Recetas:");
            JLabel recetasValor = new JLabel(estadisticas.getOrDefault("totalRecetas", 0).toString());
            estilizarEtiquetaEstadistica(recetasLabel, recetasValor);
            
            JLabel usuariosLabel = new JLabel("Total de Usuarios:");
            JLabel usuariosValor = new JLabel(estadisticas.getOrDefault("totalUsuarios", 0).toString());
            estilizarEtiquetaEstadistica(usuariosLabel, usuariosValor);
            
            JLabel categoriasLabel = new JLabel("Total de Categorías:");
            JLabel categoriasValor = new JLabel(estadisticas.getOrDefault("totalCategorias", 0).toString());
            estilizarEtiquetaEstadistica(categoriasLabel, categoriasValor);
            
            estadisticasPanel.add(recetasLabel);
            estadisticasPanel.add(recetasValor);
            estadisticasPanel.add(usuariosLabel);
            estadisticasPanel.add(usuariosValor);
            estadisticasPanel.add(categoriasLabel);
            estadisticasPanel.add(categoriasValor);
            
            estadisticasPanel.revalidate();
            estadisticasPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error al cargar estadísticas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    // aplica estilo a las etiquetas de estadísticas
   
    
    private void estilizarEtiquetaEstadistica(JLabel label, JLabel valor) {
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(EstiloManager.COLOR_TEXTO);
        
        valor.setFont(new Font("Arial", Font.PLAIN, 14));
        valor.setForeground(EstiloManager.COLOR_PRINCIPAL);
        valor.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    // acción al presionar el botón de volver
     
    @Override
    protected void onBackButtonPressed() {
        // vuelve a la pantalla de administración
        AdminFrame frame = new AdminFrame();
        dispose();
        frame.setVisible(true);
    }
}