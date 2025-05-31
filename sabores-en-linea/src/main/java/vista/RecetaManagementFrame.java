package vista;

import controlador.RecetaController;
import modelo.Receta;
import modelo.SessionManager;
import util.EstiloManager;
import util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

// Ventana para la gestión de recetas (ver, crear, editar, eliminar)

public class RecetaManagementFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private RecetaController recetaController;
	private JPanel cardsPanel;
	private JScrollPane scrollPane;
	private JButton addButton;

	// variables para paginación
	private int paginaActual = 0;
	private final int RECETAS_POR_PAGINA = 6;
	private final int COLUMNAS = 3;
	private final int FILAS = 2;
	private JButton btnAnterior;
	private JButton btnSiguiente;
	private JLabel paginaLabel;

	public RecetaManagementFrame() {
		super("Gestión de Recetas");
		this.setRecetaController(new RecetaController());

		// ajusta tamaño para mostrar 3x2 tarjetas cómodamente
		setSize(900, 700);// (antes 800, 700)

		setupComponents();

		// añade listener para redimensionar
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				ajustarTarjetas();
			}
		});
	}

	@Override
	protected void setupComponents() {
		// panel de contenido
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout());

		// botón de agregar
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setOpaque(false);
		addButton = new JButton("+ Nueva Receta");
		EstiloManager.aplicarEstiloBoton(addButton);
		addButton.addActionListener(e -> mostrarFormularioReceta());

		// solo si es administrador: botón "agregar"
		if (SessionManager.isAdmin()) {
			topPanel.add(addButton);
		}
		contentPanel.add(topPanel, BorderLayout.NORTH);

		// panel de cards con GridLayout fijo de 3x2
		cardsPanel = new JPanel(new GridLayout(FILAS, COLUMNAS, 20, 20));
		cardsPanel.setOpaque(false);
		cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		scrollPane = new JScrollPane(cardsPanel);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(null);
		EstiloManager.aplicarColorBarraDesplazamiento(scrollPane);

		contentPanel.add(scrollPane, BorderLayout.CENTER);

		// panel de paginación
		JPanel paginacionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		paginacionPanel.setOpaque(false);

		btnAnterior = new JButton("« Anterior");
		EstiloManager.aplicarEstiloBoton(btnAnterior);
		btnAnterior.setEnabled(false); // Inicialmente deshabilitado

		paginaLabel = new JLabel("Página 1");
		paginaLabel.setFont(new Font("Arial", Font.BOLD, 14));
		paginaLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

		btnSiguiente = new JButton("Siguiente »");
		EstiloManager.aplicarEstiloBoton(btnSiguiente);

		btnAnterior.addActionListener(e -> {
			paginaActual--;
			cargarRecetas();
		});

		btnSiguiente.addActionListener(e -> {
			paginaActual++;
			cargarRecetas();
		});

		paginacionPanel.add(btnAnterior);
		paginacionPanel.add(paginaLabel);
		paginacionPanel.add(btnSiguiente);

		contentPanel.add(paginacionPanel, BorderLayout.SOUTH);

		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// carga recetas de forma segura
		SwingUtilities.invokeLater(this::cargarRecetas);
	}

	// método : ajusta el tamaño de las tarjetas cuando cambia el tamaño de la ventana
	private void ajustarTarjetas() {
		// obtiene el ancho disponible para cada tarjeta
		int anchoDisponible = scrollPane.getViewport().getWidth() - 40; // resta el padding
		int anchoPorTarjeta = (anchoDisponible - (COLUMNAS - 1) * 20) / COLUMNAS; // resta el espacio entre tarjetas

		// establece un tamaño preferido para cada tarjeta
		for (Component comp : cardsPanel.getComponents()) {
			if (comp instanceof RecetaCard) {
				comp.setPreferredSize(new Dimension(anchoPorTarjeta, 280));
			}
		}

		// fuerza la actualización del layout
		cardsPanel.revalidate();
		cardsPanel.repaint();
	}

	private void cargarRecetas() {
		cardsPanel.removeAll();

		try {
			// obtiene recetas con sus colecciones inicializadas
			List<Receta> recetas = DatabaseUtil.obtenerRecetasConFotos();

			// Si la lista está vacía, mostrar mensaje y salir
			if (recetas.isEmpty()) {
				JLabel noRecetasLabel = new JLabel("No hay recetas disponibles.");
				noRecetasLabel.setFont(new Font("Arial", Font.BOLD, 16));
				noRecetasLabel.setHorizontalAlignment(SwingConstants.CENTER);

				// usa BorderLayout para centrar el mensaje
				cardsPanel.setLayout(new BorderLayout());
				cardsPanel.add(noRecetasLabel, BorderLayout.CENTER);

				// deshabilita botones de paginación
				btnAnterior.setEnabled(false);
				btnSiguiente.setEnabled(false);
				paginaLabel.setText("Página 1 de 1");

				cardsPanel.revalidate();
				cardsPanel.repaint();
				return;
			}

			// restaura el GridLayout si se cambió
			cardsPanel.setLayout(new GridLayout(FILAS, COLUMNAS, 20, 20));

			// calcula total de páginas
			int totalPaginas = (int) Math.ceil((double) recetas.size() / RECETAS_POR_PAGINA);

			// asegurarse de que la página actual sea válida
			if (paginaActual >= totalPaginas) {
				paginaActual = totalPaginas - 1;
			}
			if (paginaActual < 0) {
				paginaActual = 0;
			}

			// actualiza estado de los botones de paginación
			btnAnterior.setEnabled(paginaActual > 0);
			btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);

			// actualiza etiqueta de página
			paginaLabel.setText("Página " + (paginaActual + 1) + " de " + Math.max(1, totalPaginas));

			// obtiene recetas para la página actual
			int inicio = paginaActual * RECETAS_POR_PAGINA;
			int fin = Math.min(inicio + RECETAS_POR_PAGINA, recetas.size());

			List<Receta> recetasPagina = recetas.subList(inicio, fin);

			for (Receta receta : recetasPagina) {
				// asegura de que la receta tenga sus colecciones cargadas
				Receta recetaCompleta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
				RecetaCard card = new RecetaCard(recetaCompleta);
				card.addMouseListener(new java.awt.event.MouseAdapter() {
					@Override
					public void mouseClicked(java.awt.event.MouseEvent evt) {
						if (SessionManager.isAdmin()) {
							editarReceta(recetaCompleta);
						} else {
							verReceta(recetaCompleta);
						}
					}
				});
				cardsPanel.add(card);
			}

			// añade tarjetas vacías para completar la cuadrícula si es necesario
			int tarjetasFaltantes = RECETAS_POR_PAGINA - recetasPagina.size();
			for (int i = 0; i < tarjetasFaltantes; i++) {
				JPanel emptyCard = new JPanel();
				emptyCard.setOpaque(false);
				cardsPanel.add(emptyCard);
			}

			// ajusta el tamaño de las tarjetas
			ajustarTarjetas();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al cargar las recetas: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void mostrarFormularioReceta() {
		RecetaFormFrame formulario = new RecetaFormFrame(null, SessionManager.getUsuarioActual(), this);
		formulario.setVisible(true);
		this.setVisible(false);
	}

	private void editarReceta(Receta receta) {
		RecetaFormFrame formulario = new RecetaFormFrame(receta, SessionManager.getUsuarioActual(), this);
		formulario.setVisible(true);
		this.setVisible(false);
	}

	private void verReceta(Receta receta) {
		RecetaViewFrame vistaReceta = new RecetaViewFrame(receta);
		openNewFrame(vistaReceta);
	}

	@Override
	protected void onBackButtonPressed() {
		// vuelve a la pantalla correspondiente según el rol
		if (SessionManager.isAdmin()) {
			openNewFrame(new AdminFrame());
		} else {
			openNewFrame(new UserFrame());
		}
	}

	public RecetaController getRecetaController() {
		return recetaController;
	}

	public void setRecetaController(RecetaController recetaController) {
		this.recetaController = recetaController;
	}
}