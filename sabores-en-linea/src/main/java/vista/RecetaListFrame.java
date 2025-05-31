package vista;

import modelo.Receta;
import modelo.SessionManager;
import modelo.Usuario;
import util.EstiloManager;
import util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

// Pantalla para mostrar una lista de recetas con paginación y diseño responsive

public class RecetaListFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JPanel cardsPanel;
	private JScrollPane scrollPane;
	private boolean soloMisRecetas;
	private int paginaActual = 0;
	private final int RECETAS_POR_PAGINA = 6;
	private final int COLUMNAS = 3;
	private final int FILAS = 2;
	private JButton btnAnterior;
	private JButton btnSiguiente;
	private JLabel paginaLabel;

	public RecetaListFrame(boolean soloMisRecetas) {
		super(soloMisRecetas ? "Mis Valoraciones" : "Todas las Recetas");
		this.soloMisRecetas = soloMisRecetas;

		// ajusta tamaño para mostrar 3x2 tarjetas cómodamente
		setSize(800, 700);

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

		// panel para las tarjetas con GridLayout fijo de 3x2
		cardsPanel = new JPanel(new GridLayout(FILAS, COLUMNAS, 20, 20));
		cardsPanel.setOpaque(false);
		cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		scrollPane = new JScrollPane(cardsPanel);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(null);

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

	// carga las recetas según el filtro y la página actual
	private void cargarRecetas() {
		cardsPanel.removeAll();

		try {
			// obtiene recetas según el filtro, con fotos precargadas
			List<Receta> recetas = new ArrayList<>();
			Usuario usuarioActual = SessionManager.getUsuarioActual();

			if (soloMisRecetas && usuarioActual != null) {
				// obtiene solo las recetas valoradas por el usuario actual
				recetas = DatabaseUtil.obtenerRecetasValoradasPorUsuario(usuarioActual.getId());
			} else {
				// obtiene todas las recetas con sus fotos precargadas
				recetas = DatabaseUtil.obtenerRecetasConFotos();
			}

			// Si la lista está vacía, mostrar mensaje y salir
			if (recetas.isEmpty()) {
				JLabel noRecetasLabel = new JLabel(
						soloMisRecetas ? "No has valorado ninguna receta todavía." : "No hay recetas disponibles.");
				noRecetasLabel.setFont(new Font("Arial", Font.BOLD, 16));
				noRecetasLabel.setHorizontalAlignment(SwingConstants.CENTER);

				// BorderLayout para centrar el mensaje
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

			// asegurarse que la página actual sea válida
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

			if (inicio < recetas.size()) {
				List<Receta> recetasPagina = recetas.subList(inicio, fin);

				// crea tarjetas para cada receta
				for (Receta receta : recetasPagina) {
					try {
						// asegurarse que la receta tenga sus colecciones cargadas
						Receta recetaCompleta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
						if (recetaCompleta != null) {
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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// añade tarjetas vacías para completar la cuadrícula si es necesario
				int tarjetasFaltantes = RECETAS_POR_PAGINA - recetasPagina.size();
				for (int i = 0; i < tarjetasFaltantes; i++) {
					JPanel emptyCard = new JPanel();
					emptyCard.setOpaque(false);
					cardsPanel.add(emptyCard);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al cargar las recetas: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		// ajusta el tamaño de las tarjetas
		ajustarTarjetas();
	}

	// abre formulario para editar
	private void editarReceta(Receta receta) {
		RecetaFormFrame formulario = new RecetaFormFrame(receta, SessionManager.getUsuarioActual());
		openNewFrame(formulario);
	}

	// abre la ventana para ver detalles receta
	private void verReceta(Receta receta) {
		// Muestra una ventana de solo lectura con los detalles de la receta
		RecetaViewFrame vistaReceta = new RecetaViewFrame(receta);
		openNewFrame(vistaReceta);
	}

	// maneja el evento: boton volver
	@Override
	protected void onBackButtonPressed() {
		// vuelve al panel de usuario o administrador según el rol
		if (SessionManager.isAdmin()) {
			openNewFrame(new AdminFrame());
		} else {
			openNewFrame(new UserFrame());
		}
	}
}