package vista;

import javax.swing.*;
import modelo.SessionManager;
import modelo.Usuario;
import util.EstiloManager;

import java.awt.*;
import java.io.Serializable;

public class UserFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JButton buscarRecetasButton;
	private JButton verRecetasButton;
	private JButton misValoracionesButton;

	public UserFrame() {
		super("Panel de Usuario: "
				+ (SessionManager.getUsuarioActual() != null ? SessionManager.getUsuarioActual().getNombre()
						: "Invitado"));
		setupComponents();
	}

	@Override
	protected void setupComponents() {
		Usuario usuarioActual = SessionManager.getUsuarioActual();

		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new GridLayout(3, 1, 10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		buscarRecetasButton = new JButton("Buscar Recetas");
		verRecetasButton = new JButton("Ver Todas las Recetas");
		misValoracionesButton = new JButton("Mis Valoraciones");

		EstiloManager.aplicarEstiloBoton(buscarRecetasButton);
		EstiloManager.aplicarEstiloBoton(verRecetasButton);
		EstiloManager.aplicarEstiloBoton(misValoracionesButton);

		contentPanel.add(buscarRecetasButton);
		contentPanel.add(verRecetasButton);

		// "Mis Valoraciones" solo : usuarios regulares
		if (usuarioActual != null && !usuarioActual.esAdmin()) {
			contentPanel.add(misValoracionesButton);
		}

		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// configura acciones de botones
		buscarRecetasButton.addActionListener(e -> {
			BusquedaAvanzadaFrame frame = new BusquedaAvanzadaFrame();
			openNewFrame(frame);
		});

		verRecetasButton.addActionListener(e -> {
			RecetaListFrame frame = new RecetaListFrame(false);
			openNewFrame(frame);
		});

		misValoracionesButton.addActionListener(e -> {
			ValoracionManagementFrame frame = new ValoracionManagementFrame(usuarioActual, this);
			openNewFrame(frame);
		});
	}

	@Override
	protected void onBackButtonPressed() {
		SessionManager.cerrarSesion();
		LoginFrame loginFrame = new LoginFrame();
		dispose();
		loginFrame.setVisible(true);
	}
}
