package vista;

import modelo.FotoReceta;
import modelo.Receta;
import modelo.SessionManager;
import modelo.VideoReceta;
import util.DatabaseUtil;
import util.EstiloManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Componente para mostrar una tarjeta de receta :extends JPanel: porque no es ventana completa

public class RecetaCard extends JPanel implements Serializable {
	private static final long serialVersionUID = 1L;
	// caché de imágenes para mejorar rendimiento
	private static Map<String, ImageIcon> imagenesCache = new HashMap<>();

	// proporción de la imagen respecto a la altura total de la tarjeta
	private static final int IMAGE_HEIGHT_RATIO = 60; // 60% de la altura para la imagen

	private final Color CARD_BACKGROUND = new Color(255, 255, 255);
	private final Color CARD_BORDER = new Color(200, 200, 200);
	private final Color HOVER_BACKGROUND = new Color(240, 240, 240);

	private Receta receta;
	private JFrame parentFrame;
	private JLabel imageLabel;
	private JLabel titleLabel;
	private JPanel ratingPanel;
	private JLabel categoryLabel;
	private JButton btnVerReceta;
	private JButton btnVerVideos;
	private JButton btnEditar;
	private JPopupMenu videoPopupMenu;

	public RecetaCard(Receta receta) {
		super();
		this.receta = receta;
		setupCard();
	}

	public RecetaCard(Receta receta, JFrame parentFrame) {
		super();
		this.receta = receta;
		this.parentFrame = parentFrame;
		setupCard();
	}

	// configura la tarjeta

	private void setupCard() {
		// BorderLayout : mejor adaptabilidad
		setLayout(new BorderLayout(5, 5));
		setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true));
		setBackground(CARD_BACKGROUND);

		// panel para la imagen
		JPanel imagePanel = new JPanel(new BorderLayout());
		imagePanel.setBackground(CARD_BACKGROUND);

		// carga imagen
		imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		cargarImagen();
		imagePanel.add(imageLabel, BorderLayout.CENTER);

		// panel para el texto
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(CARD_BACKGROUND);

		// título
		titleLabel = new JLabel(receta.getTitulo());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// valoración
		ratingPanel = createStarRatingPanel(receta.getValoracionPromedio());
		ratingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// categoría
		categoryLabel = new JLabel(receta.getCategoria() != null ? receta.getCategoria().getNombre() : "Sin categoría");
		categoryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
		categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
		categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		textPanel.add(titleLabel);
		textPanel.add(Box.createVerticalStrut(5));
		textPanel.add(ratingPanel);
		textPanel.add(Box.createVerticalStrut(5));
		textPanel.add(categoryLabel);
		textPanel.add(Box.createVerticalStrut(10));

		// panel de botones - Usa GridLayout (organizar los botones)
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
		buttonPanel.setOpaque(false);
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		// botón ver receta (siempre presente)
		btnVerReceta = new JButton("Ver Receta");
		EstiloManager.aplicarEstiloBoton(btnVerReceta);
		btnVerReceta.addActionListener(e -> verReceta());
		buttonPanel.add(btnVerReceta);

		// botón ver videos (solo si hay videos)
		if (receta.getVideos() != null && !receta.getVideos().isEmpty()) {
			btnVerVideos = new JButton("Ver Videos");
			EstiloManager.aplicarEstiloBoton(btnVerVideos);

			// menú emergente para videos
			videoPopupMenu = new JPopupMenu();

			// carga videos de la receta
			List<VideoReceta> videos = DatabaseUtil.obtenerVideosPorReceta(receta.getId());
			if (videos != null && !videos.isEmpty()) {
				for (VideoReceta video : videos) {
					String videoName = video.getDescripcion() != null ? video.getDescripcion()
							: (video.getNombreArchivo() != null ? video.getNombreArchivo() : "Video");
					JMenuItem videoItem = new JMenuItem(videoName);
					videoItem.addActionListener(e -> reproducirVideo(video));
					videoPopupMenu.add(videoItem);
				}
			} else {
				JMenuItem noVideosItem = new JMenuItem("No hay videos disponibles");
				noVideosItem.setEnabled(false);
				videoPopupMenu.add(noVideosItem);
			}

			// muestra menú emergente al hacer clic en el botón
			btnVerVideos.addActionListener(e -> {
				videoPopupMenu.show(btnVerVideos, 0, btnVerVideos.getHeight());
			});

			buttonPanel.add(btnVerVideos);
		}

		// botón editar (solo para administradores)
		if (SessionManager.isAdmin()) {
			btnEditar = new JButton("Editar");
			EstiloManager.aplicarEstiloBoton(btnEditar);
			btnEditar.addActionListener(e -> editarReceta());
			buttonPanel.add(btnEditar);
		}

		// añade panel de botones al panel de texto
		textPanel.add(buttonPanel);

		add(imagePanel, BorderLayout.CENTER);
		add(textPanel, BorderLayout.SOUTH);
	}

	private void cargarImagen() {
		try {
			// obtiene fotos directamente de la BBDD
			List<FotoReceta> fotos = DatabaseUtil.obtenerFotosPorReceta(receta.getId());

			if (fotos != null && !fotos.isEmpty()) {
				// obtiene la foto principal o la primera disponible
				FotoReceta foto = fotos.stream().filter(FotoReceta::isEsPrincipal).findFirst().orElse(fotos.get(0));

				// clave para la caché
				String cacheKey = "";

				// intenta cargar desde contenido binario
				if (foto.getContenido() != null && foto.getContenido().length > 0) {
					// usa el ID de la foto como clave de caché
					cacheKey = "foto_" + foto.getId();

					// verifica si ya está en caché
					if (imagenesCache.containsKey(cacheKey)) {
						imageLabel.setIcon(imagenesCache.get(cacheKey));
						return;
					}

					ByteArrayInputStream bis = new ByteArrayInputStream(foto.getContenido());
					BufferedImage img = ImageIO.read(bis);

					if (img != null) {
						// tamaño : se ajustará en paintComponent
						ImageIcon icon = new ImageIcon(img);
						imageLabel.setIcon(icon);

						// guarda en caché
						imagenesCache.put(cacheKey, icon);
						return;
					}
				}
				// intenta cargar desde URL si el nombre de archivo es una URL
				else if (foto.getNombreArchivo() != null && (foto.getNombreArchivo().startsWith("http") || foto.getNombreArchivo().startsWith("file"))) {
					//else if (foto.getNombreArchivo() != null && foto.getNombreArchivo().startsWith("http")) {
					// usa la URL como clave de caché
					cacheKey = foto.getNombreArchivo();

					// verifica si ya está en caché
					if (imagenesCache.containsKey(cacheKey)) {
						imageLabel.setIcon(imagenesCache.get(cacheKey));
						return;
					}

					try {
						System.out.println("Intentando cargar imagen desde URL: " + foto.getNombreArchivo());
						// crea URI primero y luego convierte a URL
						URI uri = new URI(foto.getNombreArchivo());
						URL url = uri.toURL();
						
						BufferedImage img = ImageIO.read(url);
						if (img != null) {
							// tamaño : se ajustará en paintComponent
							ImageIcon icon = new ImageIcon(img);
							imageLabel.setIcon(icon);

							// guarda en caché
							imagenesCache.put(cacheKey, icon);
							return;
						}
					} catch (Exception e) {
						System.err.println("Error al cargar imagen desde URL: " + e.getMessage());
					}
				}
				
				
				
				// intenta cargar desde archivo local si no es una URL
				else if (foto.getNombreArchivo() != null) {
					cacheKey = foto.getNombreArchivo();

					if (imagenesCache.containsKey(cacheKey)) {
						imageLabel.setIcon(imagenesCache.get(cacheKey));
						return;
					}

					File file = new File(foto.getNombreArchivo());
					if (file.exists()) {
						try {
							BufferedImage img = ImageIO.read(file);
							if (img != null) {
								ImageIcon icon = new ImageIcon(img);
								imageLabel.setIcon(icon);
								imagenesCache.put(cacheKey, icon);
								return;
							}
						} catch (IOException ex) {
							System.err.println("Error al cargar imagen desde archivo local: " + ex.getMessage());
						}
					} else {
						System.err.println("Archivo local no encontrado: " + file.getAbsolutePath());
					}
				}
			}

			// Si no hay foto o error : imagen de fondo
			mostrarImagenPorDefecto();

		} catch (Exception e) {
			e.printStackTrace();
			mostrarImagenPorDefecto();
		}
	}


	private void mostrarImagenPorDefecto() {
		try {
			// usa la imagen de fondo
			if (BaseFrame.backgroundImage != null) {
				// La escala se ajustará en paintComponent
				imageLabel.setIcon(new ImageIcon(BaseFrame.backgroundImage));
			} else {
				// Si no se puede cargar la imagen, usa un color de fondo simple
				imageLabel.setIcon(null);
				imageLabel.setOpaque(true);
				imageLabel.setBackground(new Color(240, 230, 210)); // Color beige para fondo por defecto
				imageLabel.setText("Sin imagen");
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// en caso de error, usa un color de fondo simple
			imageLabel.setIcon(null);
			imageLabel.setOpaque(true);
			imageLabel.setBackground(new Color(240, 230, 210));
			imageLabel.setText("Sin imagen");
			imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
	}

	// panel con estrellas para mostrar la valoración:

	private JPanel createStarRatingPanel(double rating) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel.setOpaque(false);

		int numStars = (int) Math.round(rating);

		// caracteres Unicode para estrellas
		String starFilled = "\u2605"; // estrella rellena
		String starEmpty = "\u2606"; // estrella vacía

		for (int i = 0; i < 5; i++) {
			JLabel estrella = new JLabel(i < numStars ? starFilled : starEmpty);
			estrella.setFont(new Font("Dialog", Font.PLAIN, 16)); // usa fuente Dialog que soporta Unicode
			estrella.setForeground(new Color(255, 215, 0)); // dorado
			panel.add(estrella);
		}

		return panel;
	}

	private void reproducirVideo(VideoReceta video) {
		try {
			if (video.getUrlVideo() != null && !video.getUrlVideo().isEmpty()) {
				// limpia la URL de comillas adicionales si existen
				String urlVideo = video.getUrlVideo().replace("'", "").replace("\\", "");
				System.out.println("Intentando reproducir video desde URL: " + urlVideo);

				// verifica si la URL es válida
				try {
					// abre URL en el navegador
					Desktop.getDesktop().browse(new URI(urlVideo));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "La URL del video no es válida: " + urlVideo, "Error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			} else if (video.getContenido() != null && video.getContenido().length > 0) {
				// guarda el video en un archivo temporal y reproducirlo
				File tempFile = File.createTempFile("video_", "." + getExtensionFromMimeType(video.getTipoMime()));
				tempFile.deleteOnExit();

				try (FileOutputStream fos = new FileOutputStream(tempFile)) {
					fos.write(video.getContenido());
				}

				Desktop.getDesktop().open(tempFile);
			} else {
				JOptionPane.showMessageDialog(this, "No hay contenido de video disponible", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al reproducir el video: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// obtiene la extensión de archivo basada en el tipo MIME
	private String getExtensionFromMimeType(String mimeType) {
		if (mimeType == null)
			return "mp4";

		switch (mimeType) {
		case "video/mp4":
			return "mp4";
		case "video/x-msvideo":
			return "avi";
		case "video/quicktime":
			return "mov";
		case "video/x-ms-wmv":
			return "wmv";
		default:
			return "mp4"; // Extensión por defecto
		}
	}

	// usa JDialog modal para editar la receta
	private void editarReceta() {
		try {
			// Asegura : receta cargada completamente
			Receta recetaCompleta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
			if (recetaCompleta == null) {
				JOptionPane.showMessageDialog(this, "No se pudo cargar la información completa de la receta.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (parentFrame != null) {
				// crea un JDialog modal
				JDialog dialog = new JDialog(parentFrame, "Editar Receta: " + recetaCompleta.getTitulo(), true);
				dialog.setIconImage(parentFrame.getIconImage());

				// crea panel de edición de receta
				JPanel editPanel = new JPanel(new BorderLayout());
				editPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				editPanel.setBackground(EstiloManager.COLOR_FONDO);

				// componentes para editar la receta
				JTextField titleField = new JTextField(recetaCompleta.getTitulo(), 30);
				JTextArea descriptionArea = new JTextArea(recetaCompleta.getDescripcion(), 10, 30);
				descriptionArea.setLineWrap(true);
				descriptionArea.setWrapStyleWord(true);

				// componentes adicionales para editar más campos
				JComboBox<String> dificultadComboBox = new JComboBox<>(new String[] { "Fácil", "Media", "Difícil" });
				dificultadComboBox.setSelectedItem(recetaCompleta.getDificultad());

				JSpinner tiempoSpinner = new JSpinner(
						new SpinnerNumberModel(recetaCompleta.getTiempoPreparacion(), 1, 1000, 5));

				JSpinner porcionesSpinner = new JSpinner(
						new SpinnerNumberModel(recetaCompleta.getPorciones(), 1, 100, 1));

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				buttonPanel.setOpaque(false);

				JButton saveButton = new JButton("Guardar");
				JButton cancelButton = new JButton("Cancelar");
				EstiloManager.aplicarEstiloBoton(saveButton);
				EstiloManager.aplicarEstiloBoton(cancelButton);

				// implementación correcta del botón guardar
				saveButton.addActionListener(e -> {
					try {
						// actualiza los datos de la receta con los valores de los campos
						recetaCompleta.setTitulo(titleField.getText().trim());
						recetaCompleta.setDescripcion(descriptionArea.getText().trim());
						recetaCompleta.setDificultad((String) dificultadComboBox.getSelectedItem());
						recetaCompleta.setTiempoPreparacion((Integer) tiempoSpinner.getValue());
						recetaCompleta.setPorciones((Integer) porcionesSpinner.getValue());

						// guarda los cambios en la base de datos
						DatabaseUtil.actualizarReceta(recetaCompleta);

						// actualiza la tarjeta con los nuevos datos
						titleLabel.setText(recetaCompleta.getTitulo());
						categoryLabel.setText(
								recetaCompleta.getCategoria() != null ? recetaCompleta.getCategoria().getNombre()
										: "Sin categoría");

						JOptionPane.showMessageDialog(dialog, "Receta actualizada correctamente", "Éxito",
								JOptionPane.INFORMATION_MESSAGE);

						dialog.dispose();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(dialog, "Error al guardar los cambios: " + ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				});

				cancelButton.addActionListener(e -> dialog.dispose());

				buttonPanel.add(saveButton);
				buttonPanel.add(cancelButton);

				// panel de formulario
				JPanel formPanel = new JPanel(new GridBagLayout());
				formPanel.setOpaque(false);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(5, 5, 5, 5);
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.HORIZONTAL;

				gbc.gridx = 0;
				gbc.gridy = 0;
				formPanel.add(new JLabel("Título:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.weightx = 1.0;
				formPanel.add(titleField, gbc);

				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.weightx = 0.0;
				formPanel.add(new JLabel("Descripción:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 1;
				gbc.weightx = 1.0;
				JScrollPane scrollPane = new JScrollPane(descriptionArea);
				formPanel.add(new JScrollPane(descriptionArea), gbc);
				EstiloManager.aplicarColorBarraDesplazamiento(scrollPane);
				
				gbc.gridx = 0;
				gbc.gridy = 2;
				gbc.weightx = 0.0;
				formPanel.add(new JLabel("Dificultad:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 2;
				gbc.weightx = 1.0;
				formPanel.add(dificultadComboBox, gbc);

				gbc.gridx = 0;
				gbc.gridy = 3;
				gbc.weightx = 0.0;
				formPanel.add(new JLabel("Tiempo (min):"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 3;
				gbc.weightx = 1.0;
				formPanel.add(tiempoSpinner, gbc);

				gbc.gridx = 0;
				gbc.gridy = 4;
				gbc.weightx = 0.0;
				formPanel.add(new JLabel("Porciones:"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 4;
				gbc.weightx = 1.0;
				formPanel.add(porcionesSpinner, gbc);

				editPanel.add(formPanel, BorderLayout.CENTER);
				editPanel.add(buttonPanel, BorderLayout.SOUTH);

				// configura y muestra el diálogo
				dialog.setContentPane(editPanel);
				dialog.pack();
				dialog.setLocationRelativeTo(parentFrame);
				dialog.setVisible(true);
			} else {
				// Si no hay parentFrame, crea un JFrame normal
				RecetaFormFrame formFrame = new RecetaFormFrame(recetaCompleta, SessionManager.getUsuarioActual());
				formFrame.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al abrir el editor de recetas: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// usa JDialog modal para ver la receta
	private void verReceta() {
		try {
			// asegura : receta cargada completamente
			Receta recetaCompleta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
			if (recetaCompleta == null) {
				JOptionPane.showMessageDialog(this, "No se pudo cargar la información completa de la receta.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (parentFrame != null) {
				// crea un JDialog modal
				JDialog dialog = new JDialog(parentFrame, "Ver Receta: " + recetaCompleta.getTitulo(), true);
				dialog.setIconImage(parentFrame.getIconImage());

				// crea panel de vista de receta
				JPanel viewPanel = new JPanel(new BorderLayout());
				viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				viewPanel.setBackground(EstiloManager.COLOR_FONDO);

				// componentes para mostrar la receta de forma más completa
				JPanel infoPanel = new JPanel();
				infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
				infoPanel.setOpaque(false);

				// título
				JLabel titleLabel = new JLabel(recetaCompleta.getTitulo());
				titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
				titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

				// información básica
				JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				metaPanel.setOpaque(false);

				JLabel categoriaLabel = new JLabel("Categoría: "
						+ (recetaCompleta.getCategoria() != null ? recetaCompleta.getCategoria().getNombre()
								: "Sin categoría"));
				JLabel dificultadLabel = new JLabel(" | Dificultad: " + recetaCompleta.getDificultad());
				JLabel tiempoLabel = new JLabel(" | Tiempo: " + recetaCompleta.getTiempoPreparacion() + " min");
				JLabel porcionesLabel = new JLabel(" | Porciones: " + recetaCompleta.getPorciones());

				metaPanel.add(categoriaLabel);
				metaPanel.add(dificultadLabel);
				metaPanel.add(tiempoLabel);
				metaPanel.add(porcionesLabel);
				metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				// descripción
				JLabel descLabel = new JLabel("Descripción:");
				descLabel.setFont(new Font("Arial", Font.BOLD, 14));
				descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

				JTextArea descArea = new JTextArea(recetaCompleta.getDescripcion());
				descArea.setEditable(false);
				descArea.setLineWrap(true);
				descArea.setWrapStyleWord(true);
				descArea.setOpaque(false);
				JScrollPane descScroll = new JScrollPane(descArea);
				descScroll.setOpaque(false);
				descScroll.getViewport().setOpaque(false);
				descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
				EstiloManager.aplicarColorBarraDesplazamiento(descScroll);

				// Instrucciones
				JLabel instrLabel = new JLabel("Instrucciones:");
				instrLabel.setFont(new Font("Arial", Font.BOLD, 14));
				instrLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

				JTextArea instrArea = new JTextArea(recetaCompleta.getInstrucciones());
				instrArea.setEditable(false);
				instrArea.setLineWrap(true);
				instrArea.setWrapStyleWord(true);
				instrArea.setOpaque(false);
				JScrollPane instrScroll = new JScrollPane(instrArea);
				instrScroll.setOpaque(false);
				instrScroll.getViewport().setOpaque(false);
				instrScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
				EstiloManager.aplicarColorBarraDesplazamiento(descScroll);

				// agrega componentes al panel de información
				infoPanel.add(titleLabel);
				infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
				infoPanel.add(metaPanel);
				infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
				infoPanel.add(descLabel);
				infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
				infoPanel.add(descScroll);
				infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
				infoPanel.add(instrLabel);
				infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
				infoPanel.add(instrScroll);

				JButton closeButton = new JButton("Cerrar");
				EstiloManager.aplicarEstiloBoton(closeButton);
				closeButton.addActionListener(e -> dialog.dispose());

				viewPanel.add(new JScrollPane(infoPanel), BorderLayout.CENTER);
				viewPanel.add(closeButton, BorderLayout.SOUTH);
				EstiloManager.aplicarColorBarraDesplazamiento(descScroll);

				// configura y muestra el diálogo
				dialog.setContentPane(viewPanel);
				dialog.setSize(600, 500);
				dialog.setLocationRelativeTo(parentFrame);
				dialog.setVisible(true);
			} else {
				// Si no hay parentFrame, crear un JFrame normal
				RecetaViewFrame viewFrame = new RecetaViewFrame(recetaCompleta);
				viewFrame.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al abrir la vista de receta: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// sobrescribe para ajustar la imagen al tamaño actual
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// ajusta tamaño imagen
		if (imageLabel.getIcon() != null) {
			int imageHeight = (int) (getHeight() * IMAGE_HEIGHT_RATIO / 100.0);
			imageLabel.setPreferredSize(new Dimension(getWidth(), imageHeight));
		}
	}

	public Receta getReceta() {
		return receta;
	}

	public static void limpiarCache() {
		imagenesCache.clear();
	}

	public Color getHOVER_BACKGROUND() {
		return HOVER_BACKGROUND;
	}
}