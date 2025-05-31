package vista;

import controlador.RecetaController;
import modelo.FotoReceta;
import modelo.VideoReceta;
import modelo.Receta;
import modelo.SessionManager;
import util.EstiloManager;
import util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MultimediaFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JPanel contentPanel;
	private JPanel fotosPanel;
	private JPanel videosPanel;
	private final RecetaController recetaController;
	private List<FotoReceta> fotosSeleccionadas;
	private List<VideoReceta> videosSeleccionados;
	private Receta recetaActual;

	public MultimediaFrame() {
		super("Gestión de Multimedia");
		this.recetaController = new RecetaController();
		this.fotosSeleccionadas = new ArrayList<>();
		this.videosSeleccionados = new ArrayList<>();

		// verifica si el usuario es administrador
		if (!SessionManager.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Solo los administradores pueden acceder a esta función",
					"Acceso denegado", JOptionPane.WARNING_MESSAGE);
			dispose();
			return;
		}

		setupComponents();
	}

	@Override
	protected void setupComponents() {
		contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout(10, 10));

		// panel de pestañas
		JTabbedPane tabbedPane = new JTabbedPane();

		// panel de Fotos
		JPanel fotosContainer = new JPanel(new BorderLayout(10, 10));
		fotosContainer.setOpaque(false);

		fotosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		fotosPanel.setOpaque(false);

		JScrollPane scrollFotos = new JScrollPane(fotosPanel);
		scrollFotos.setOpaque(false);
		scrollFotos.getViewport().setOpaque(false);

		JButton btnAgregarFotos = new JButton("Agregar Fotos");
		EstiloManager.aplicarEstiloBoton(btnAgregarFotos);
		btnAgregarFotos.addActionListener(e -> procesarFotos());

		fotosContainer.add(scrollFotos, BorderLayout.CENTER);
		fotosContainer.add(btnAgregarFotos, BorderLayout.SOUTH);

		// panel de Videos
		JPanel videosContainer = new JPanel(new BorderLayout(10, 10));
		videosContainer.setOpaque(false);

		videosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		videosPanel.setOpaque(false);

		JScrollPane scrollVideos = new JScrollPane(videosPanel);
		scrollVideos.setOpaque(false);
		scrollVideos.getViewport().setOpaque(false);

		JPanel botonesVideo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		botonesVideo.setOpaque(false);

		JButton btnAgregarVideos = new JButton("Agregar Videos");
		JButton btnAgregarURL = new JButton("Agregar URL de Video");

		EstiloManager.aplicarEstiloBoton(btnAgregarVideos);
		EstiloManager.aplicarEstiloBoton(btnAgregarURL);

		btnAgregarVideos.addActionListener(e -> procesarVideos());
		btnAgregarURL.addActionListener(e -> procesarVideoURL());

		botonesVideo.add(btnAgregarVideos);
		botonesVideo.add(btnAgregarURL);

		videosContainer.add(scrollVideos, BorderLayout.CENTER);
		videosContainer.add(botonesVideo, BorderLayout.SOUTH);

		// agrega pestañas
		tabbedPane.addTab("Fotos", null, fotosContainer);
		tabbedPane.addTab("Videos", null, videosContainer);

		contentPanel.add(tabbedPane, BorderLayout.CENTER);

		// panel de botones inferior
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setOpaque(false);

		JButton btnGuardar = new JButton("Guardar Cambios");
		EstiloManager.aplicarEstiloBoton(btnGuardar);
		btnGuardar.setBackground(new Color(76, 175, 80));
		btnGuardar.addActionListener(e -> guardarCambios());

		buttonPanel.add(btnGuardar);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainPanel.add(contentPanel, BorderLayout.CENTER);
	}

	private void procesarFotos() {
		// verifica si el usuario esAdmin
		if (!SessionManager.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Solo los administradores pueden modificar multimedia",
					"Acceso denegado", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(
				new FileNameExtensionFilter("Imágenes (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();

			for (File file : files) {
				try {
					// lee archivo
					byte[] contenido = Files.readAllBytes(file.toPath());

					// determina tipo MIME
					String tipoMime = Files.probeContentType(file.toPath());
					if (tipoMime == null) {
						tipoMime = "image/" + getFileExtension(file);
					}

					// crea miniatura
					ImageIcon originalIcon = new ImageIcon(contenido);
					Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

					// crea panel para la foto
					JPanel photoPanel = new JPanel(new BorderLayout(5, 5));
					photoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

					// mostra miniatura
					JLabel photoLabel = new JLabel(new ImageIcon(scaledImage));
					photoPanel.add(photoLabel, BorderLayout.CENTER);

					// botón para eliminar
					JButton deleteButton = new JButton("×");
					deleteButton.setForeground(Color.RED);
					deleteButton.addActionListener(e -> {
						fotosPanel.remove(photoPanel);
						fotosPanel.revalidate();
						fotosPanel.repaint();
						// También eliminar de la lista de fotos seleccionadas
						fotosSeleccionadas.removeIf(f -> f.getNombreArchivo().equals(file.getName()));
					});
					photoPanel.add(deleteButton, BorderLayout.NORTH);

					// agrega a la interfaz
					fotosPanel.add(photoPanel);
					fotosPanel.revalidate();
					fotosPanel.repaint();

					// crea objeto FotoReceta
					FotoReceta foto = new FotoReceta();
					foto.setNombreArchivo(file.getAbsolutePath());
					foto.setContenido(contenido);
					foto.setTipoMime(tipoMime);
					// Por defecto, la primera foto es la principal si no hay otras
					foto.setEsPrincipal(fotosSeleccionadas.isEmpty());
					fotosSeleccionadas.add(foto);

				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "Error al procesar la imagen: " + file.getName(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void procesarVideos() {
		// verifica si el usuario esAdmin
		if (!SessionManager.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Solo los administradores pueden modificar multimedia",
					"Acceso denegado", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new FileNameExtensionFilter("Videos (*.mp4, *.avi, *.mov)", "mp4", "avi", "mov"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();

			for (File file : files) {
				try {
					// lee archivo
					byte[] contenido = Files.readAllBytes(file.toPath());

					// determina tipo MIME
					String tipoMime = Files.probeContentType(file.toPath());
					if (tipoMime == null) {
						tipoMime = "video/" + getFileExtension(file);
					}

					// solicita descripción
					String descripcion = JOptionPane.showInputDialog(this,
							"Ingrese una descripción para el video: " + file.getName());

					if (descripcion != null) {
						// crea panel para el video
						JPanel videoPanel = new JPanel(new BorderLayout(5, 5));
						videoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

						// muestra información
						JLabel videoLabel = new JLabel(file.getName());
						videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
						videoPanel.add(videoLabel, BorderLayout.CENTER);

						// botón para eliminar
						JButton deleteButton = new JButton("×");
						deleteButton.setForeground(Color.RED);
						deleteButton.addActionListener(e -> {
							videosPanel.remove(videoPanel);
							videosPanel.revalidate();
							videosPanel.repaint();
							// También elimina de la lista de videos seleccionados
							videosSeleccionados.removeIf(
									v -> v.getNombreArchivo() != null && v.getNombreArchivo().equals(file.getName()));
						});
						videoPanel.add(deleteButton, BorderLayout.NORTH);

						// agrega a la interfaz:
						videosPanel.add(videoPanel);
						videosPanel.revalidate();
						videosPanel.repaint();

						// crea objeto VideoReceta
						VideoReceta video = new VideoReceta();
						video.setNombreArchivo(file.getName());
						video.setDescripcion(descripcion);
						video.setContenido(contenido);
						video.setTipoMime(tipoMime);
						videosSeleccionados.add(video);
					}

				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "Error al procesar el video: " + file.getName(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void procesarVideoURL() {
		// verifica si el usuario esAdmin
		if (!SessionManager.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Solo los administradores pueden modificar multimedia",
					"Acceso denegado", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String url = JOptionPane.showInputDialog(this, "Ingrese la URL del video:", "Agregar Video por URL",
				JOptionPane.QUESTION_MESSAGE);

		if (url != null && !url.trim().isEmpty()) {
			String descripcion = JOptionPane.showInputDialog(this, "Ingrese una descripción para el video:");

			if (descripcion != null) {
				// crea panel para el video
				JPanel videoPanel = new JPanel(new BorderLayout(5, 5));
				videoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

				// muestra información
				JLabel videoLabel = new JLabel("Video URL: " + url);
				videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
				videoPanel.add(videoLabel, BorderLayout.CENTER);

				// botón para eliminar
				JButton deleteButton = new JButton("×");
				deleteButton.setForeground(Color.RED);
				deleteButton.addActionListener(e -> {
					videosPanel.remove(videoPanel);
					videosPanel.revalidate();
					videosPanel.repaint();
					// También elimina de la lista de videos seleccionados
					videosSeleccionados.removeIf(v -> v.getUrlVideo() != null && v.getUrlVideo().equals(url));
				});
				videoPanel.add(deleteButton, BorderLayout.NORTH);

				// agrega a la interfaz
				videosPanel.add(videoPanel);
				videosPanel.revalidate();
				videosPanel.repaint();

				// crea objeto VideoReceta
				VideoReceta video = new VideoReceta();
				video.setUrlVideo(url);
				video.setDescripcion(descripcion);
				videosSeleccionados.add(video);
			}
		}
	}

	private void guardarCambios() {
		// Verifica si el usuario esAdmin
		if (!SessionManager.isAdmin()) {
			JOptionPane.showMessageDialog(this, "Solo los administradores pueden modificar multimedia",
					"Acceso denegado", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			if (recetaActual != null) {
				// Asegurarse de que la receta esté completamente cargada
				recetaActual = DatabaseUtil.obtenerRecetaCompleta(recetaActual.getId());

				if (recetaActual == null) {
					throw new Exception("No se pudo cargar la receta completa");
				}

				// guarda fotos
				for (FotoReceta foto : fotosSeleccionadas) {
					foto.setReceta(recetaActual);
					DatabaseUtil.agregarFotoAReceta(foto, recetaActual.getId());
				}

				// guarda videos
				for (VideoReceta video : videosSeleccionados) {
					video.setReceta(recetaActual);
					DatabaseUtil.agregarVideoAReceta(video, recetaActual.getId());
				}

				JOptionPane.showMessageDialog(this, "Cambios guardados correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);

				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "No hay receta seleccionada para guardar multimedia", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al guardar los cambios: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf + 1);
	}

	public void setReceta(Receta receta) {
		this.recetaActual = receta;
		// carga multimedia existente
		if (receta != null) {
			// Asegurarse de que la receta esté completamente cargada
			try {
				this.recetaActual = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
				if (this.recetaActual != null) {
					cargarMultimediaExistente();
				} else {
					JOptionPane.showMessageDialog(this, "Error al cargar la receta completa", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error al cargar la receta: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void cargarMultimediaExistente() {
		try {
			// limpia paneles
			fotosPanel.removeAll();
			videosPanel.removeAll();

			// carga fotos existentes
			List<FotoReceta> fotos = DatabaseUtil.obtenerFotosPorReceta(recetaActual.getId());
			if (fotos != null) {
				for (FotoReceta foto : fotos) {
					mostrarFotoExistente(foto);
				}
			}

			// carga videos existentes
			List<VideoReceta> videos = DatabaseUtil.obtenerVideosPorReceta(recetaActual.getId());
			if (videos != null) {
				for (VideoReceta video : videos) {
					mostrarVideoExistente(video);
				}
			}

			fotosPanel.revalidate();
			fotosPanel.repaint();
			videosPanel.revalidate();
			videosPanel.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al cargar multimedia existente: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void mostrarFotoExistente(FotoReceta foto) {
		try {
			if (foto == null || foto.getContenido() == null) {
				return;
			}

			// crea miniatura
			ImageIcon originalIcon = new ImageIcon(foto.getContenido());
			Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

			// crea panel para la foto
			JPanel photoPanel = new JPanel(new BorderLayout(5, 5));
			photoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

			// muestra miniatura
			JLabel photoLabel = new JLabel(new ImageIcon(scaledImage));
			photoPanel.add(photoLabel, BorderLayout.CENTER);

			// botón para eliminar
			JButton deleteButton = new JButton("×");
			deleteButton.setForeground(Color.RED);
			deleteButton.addActionListener(e -> {
				try {
					DatabaseUtil.eliminarFoto(foto.getId());
					fotosPanel.remove(photoPanel);
					fotosPanel.revalidate();
					fotosPanel.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error al eliminar la foto: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			photoPanel.add(deleteButton, BorderLayout.NORTH);

			// agrega a la interfaz
			fotosPanel.add(photoPanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mostrarVideoExistente(VideoReceta video) {
		try {
			if (video == null) {
				return;
			}

			// crea panel para el video
			JPanel videoPanel = new JPanel(new BorderLayout(5, 5));
			videoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

			// muestra información
			String displayText = video.getUrlVideo() != null ? "Video URL: " + video.getUrlVideo()
					: "Video: " + video.getNombreArchivo();

			JLabel videoLabel = new JLabel(displayText);
			videoLabel.setHorizontalAlignment(SwingConstants.CENTER);
			videoPanel.add(videoLabel, BorderLayout.CENTER);

			// botón eliminar
			JButton deleteButton = new JButton("×");
			deleteButton.setForeground(Color.RED);
			deleteButton.addActionListener(e -> {
				try {
					DatabaseUtil.eliminarVideo(video.getId());
					videosPanel.remove(videoPanel);
					videosPanel.revalidate();
					videosPanel.repaint();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Error al eliminar el video: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			videoPanel.add(deleteButton, BorderLayout.NORTH);

			// agrega a la interfaz
			videosPanel.add(videoPanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onBackButtonPressed() {
		if (recetaActual != null) {
			int option = JOptionPane.showConfirmDialog(this, "¿Desea guardar los cambios antes de salir?",
					"Guardar Cambios", JOptionPane.YES_NO_CANCEL_OPTION);

			if (option == JOptionPane.YES_OPTION) {
				guardarCambios();
			} else if (option == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
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
}