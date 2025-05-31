package vista;

import util.EstiloManager;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

public abstract class BaseFrame extends JFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	protected JPanel mainPanel;
	protected JButton backButton;
	public static BufferedImage backgroundImage; // "accesible RecetaCard"
	protected BufferedImage blurredImage;

	public BaseFrame(String titulo) {
		setTitle(titulo);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null); // centra ventana
		
	    // establece el icono de la aplicación
        try {
            
            ImageIcon icono = new ImageIcon(getClass().getResource("/img/icono_64x64.png"));
            setIconImage(icono.getImage());
         
        } catch (Exception e) {
            System.err.println("Error al cargar el icono: " + e.getMessage());
        }

		loadAndBlurBackground();
		setupFrame();
	}

	private void loadAndBlurBackground() {
		try {
			// carga la imagen desde la carpeta IMG
			backgroundImage = ImageIO.read(getClass().getResourceAsStream("/img/background.jpeg"));

			// crea el kernel para el efecto de desenfoque
			float[] matrix = new float[25];// antes [400]
			for (int i = 0; i < 25; i++) {// < 400
				matrix[i] = 1.0f / 25.0f;// 400.0f
			}

			Kernel kernel = new Kernel(5, 5, matrix);// 20,20 mas optimo y opaco (lo se)
			ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

			// aplica el desenfoque
			blurredImage = op.filter(backgroundImage, null);
		} catch (IOException e) {
			e.printStackTrace();
			// Si hay error, continua sin imagen de fondo
		}
	}

	private void setupFrame() {
		// panel principal con imagen de fondo
		mainPanel = new JPanel(new BorderLayout()) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (blurredImage != null) {
					g.drawImage(blurredImage, 0, 0, getWidth(), getHeight(), this);
				}
			}
		};
		setContentPane(mainPanel);

		// panel superior con título y botón de volver
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(EstiloManager.COLOR_PRINCIPAL);
		headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

		// título
		JLabel titleLabel = new JLabel(getTitle());
		titleLabel.setFont(EstiloManager.FUENTE_TITULO);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(titleLabel, BorderLayout.CENTER);

		// botón de volver
		backButton = new JButton("← Volver");
		backButton.setFont(EstiloManager.FUENTE_BOTON);
		backButton.setForeground(Color.WHITE);
		backButton.setBackground(EstiloManager.COLOR_PRINCIPAL);
		backButton.setBorderPainted(false);
		backButton.setFocusPainted(false);
		backButton.addActionListener(e -> onBackButtonPressed());
		headerPanel.add(backButton, BorderLayout.WEST);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// panel inferior con copyright
		JPanel footerPanel = new JPanel();
		footerPanel.setBackground(EstiloManager.COLOR_PRINCIPAL);
		footerPanel.setPreferredSize(new Dimension(getWidth(), 30));

		JLabel copyrightLabel = new JLabel("© 2025 Sabores en Línea");
		copyrightLabel.setForeground(Color.WHITE);
		footerPanel.add(copyrightLabel);

		mainPanel.add(footerPanel, BorderLayout.SOUTH);
	}

	// método : crea panel de contenido semitransparente
	protected JPanel createContentPanel() {
		JPanel contentPanel = new JPanel() {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(new Color(255, 255, 255, 200));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		contentPanel.setOpaque(false);
		return contentPanel;
	}

	// método : abre nueva ventana y cierra la actual(evitando parpadeo)
	protected void openNewFrame(JFrame frame) {

		this.setVisible(false); // primero: oculata la ventana actual

		frame.setVisible(true); // segundo: hace visible la nueva

		this.dispose(); // tercero: cierra la actual
	}

	// métodos abstractos que deben implementar las clases hijas
	protected abstract void setupComponents();

	protected abstract void onBackButtonPressed();
}
