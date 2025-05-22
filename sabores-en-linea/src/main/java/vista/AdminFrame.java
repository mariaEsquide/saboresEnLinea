package vista;

import util.EstiloManager;
import modelo.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class AdminFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JButton gestionUsuariosButton;
	private JButton gestionRecetasButton;
	private JButton gestionCategoriasButton;
	private JButton generarReportesButton;

	public AdminFrame() {
		super("Panel de Administración");
		setupComponents();
	}

	@Override
	protected void setupComponents() {
		// depuración : log del estado del usuario actual
		SessionManager.logUserStatus("AdminFrame setup");

		// crea panel de contenido y lo configuramos
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new GridLayout(4, 1, 10, 10)); // diseño de la cuadrícula (4 botones en columna)
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // espaciado

		// inicializa los botones para las diferentes funcionalidades del panel de
		// administración
		gestionUsuariosButton = new JButton("Gestión de Usuarios");
		gestionRecetasButton = new JButton("Gestión de Recetas");
		gestionCategoriasButton = new JButton("Gestión de Categorías");
		generarReportesButton = new JButton("Generar Reportes");

		// aplica el estilo a cada uno de los botones
		EstiloManager.aplicarEstiloBoton(gestionUsuariosButton);
		EstiloManager.aplicarEstiloBoton(gestionRecetasButton);
		EstiloManager.aplicarEstiloBoton(gestionCategoriasButton);
		EstiloManager.aplicarEstiloBoton(generarReportesButton);

		// añade los botones al panel de contenido
		contentPanel.add(gestionUsuariosButton);
		contentPanel.add(gestionRecetasButton);
		contentPanel.add(gestionCategoriasButton);
		contentPanel.add(generarReportesButton);

		// añade el panel de contenido al panel principal
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// configura las acciones para los botones
		gestionUsuariosButton.addActionListener(e -> {
			openNewFrame(new UserManagementFrame());
		});

		gestionRecetasButton.addActionListener(e -> {
			RecetaManagementFrame frame = new RecetaManagementFrame();
			dispose();
			frame.setVisible(true);
		});

		gestionCategoriasButton.addActionListener(e -> {
			openNewFrame(new CategoriaManagementFrame());
		});

		generarReportesButton.addActionListener(e -> {
			ReportGenerationFrame frame = new ReportGenerationFrame();
			dispose();
			frame.setVisible(true);
		});
	}

	@Override
	protected void onBackButtonPressed() {
		SessionManager.cerrarSesion();
		LoginFrame loginFrame = new LoginFrame();
		dispose();
		loginFrame.setVisible(true); // login
	}
}
