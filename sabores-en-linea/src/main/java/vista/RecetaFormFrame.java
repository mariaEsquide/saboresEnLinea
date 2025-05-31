package vista;

import controlador.CategoriaController;
import controlador.RecetaController;
import modelo.Categoria;
import modelo.FotoReceta;
import modelo.Receta;
import modelo.Usuario;
import modelo.VideoReceta;
import util.EstiloManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RecetaFormFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private final RecetaController recetaController;
	private final CategoriaController categoriaController;
	private Receta receta;
	private final boolean esNueva;
	private final Usuario usuarioActual;
	private JFrame parentFrame;

	// componentes UI
	private JTextField txtTitulo;
	private JTextArea txtDescripcion;
	private JTextArea txtInstrucciones;
	private JSpinner spnTiempoPreparacion;
	private JComboBox<String> cbxDificultad;
	private JSpinner spnPorciones;
	private JComboBox<Categoria> cbxCategoria;
	private JPanel ingredientesPanel;
	private JTextField txtNuevoIngrediente;
	private JButton btnAgregarIngrediente;
	private List<String> ingredientes = new ArrayList<>();
	private JPanel fotosPanel;
	private List<FotoReceta> fotosTemporales = new ArrayList<>();
	private JButton btnAgregarFoto;

	// componentes para videos
	private JPanel videosPanel;
	private JButton btnAgregarVideoArchivo;
	private JButton btnAgregarVideoURL;
	private List<VideoReceta> videosTemporales = new ArrayList<>();

	private JButton btnGuardar;

	public RecetaFormFrame(Receta receta, Usuario usuarioActual) {
		super(receta == null ? "Nueva Receta" : "Editar Receta: " + receta.getTitulo());
		this.recetaController = new RecetaController();
		this.categoriaController = new CategoriaController();
		this.usuarioActual = usuarioActual;

		if (receta == null) {
			this.receta = new Receta();
			this.esNueva = true;
			setupComponents();
		} else {
			this.receta = receta;
			this.esNueva = false;
			if (receta.getIngredientes() != null) {
				this.ingredientes = new ArrayList<>(receta.getIngredientes());
				setupComponents();
			}
		}
	}

	// constructor que acepta el frame padre
	public RecetaFormFrame(Receta receta, Usuario usuarioActual, JFrame parentFrame) {
		super(receta == null ? "Nueva Receta" : "Editar Receta: " + receta.getTitulo());
		this.recetaController = new RecetaController();
		this.categoriaController = new CategoriaController();
		this.usuarioActual = usuarioActual;
		this.parentFrame = parentFrame;

		// +listener : maneja el cierre de la ventana
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				if (parentFrame != null) {
					parentFrame.setVisible(true);
				}
			}
		});

		if (receta == null) {
			this.receta = new Receta();
			this.esNueva = true;
			setupComponents();
		} else {
			this.receta = receta;
			this.esNueva = false;
			if (receta.getIngredientes() != null) {
				this.ingredientes = new ArrayList<>(receta.getIngredientes());
				setupComponents();
			}
		}
	}

	@Override
	protected void setupComponents() {
		// panel de contenido con scroll
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// formulario
		JPanel formPanel = new JPanel(new GridBagLayout());
		formPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		// título
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel lblTitulo = new JLabel("Título:");
		formPanel.add(lblTitulo, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtTitulo = new JTextField(20);
		formPanel.add(txtTitulo, gbc);

		// categoría
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		JLabel lblCategoria = new JLabel("Categoría:");
		formPanel.add(lblCategoria, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		cbxCategoria = new JComboBox<>();
		for (Categoria categoria : categoriaController.obtenerTodasLasCategorias()) {
			cbxCategoria.addItem(categoria);
		}
		EstiloManager.aplicarEstiloComboBox(cbxCategoria);
		formPanel.add(cbxCategoria, gbc);

		// tiempo de preparación
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		JLabel lblTiempo = new JLabel("Tiempo (min):");
		formPanel.add(lblTiempo, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		spnTiempoPreparacion = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 5));
		formPanel.add(spnTiempoPreparacion, gbc);

		// dificultad
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0.0;
		JLabel lblDificultad = new JLabel("Dificultad:");
		formPanel.add(lblDificultad, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		cbxDificultad = new JComboBox<>(new String[] { "Fácil", "Media", "Difícil" });
		EstiloManager.aplicarEstiloComboBox(cbxDificultad);
		formPanel.add(cbxDificultad, gbc);

		// porciones
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0.0;
		JLabel lblPorciones = new JLabel("Porciones:");
		formPanel.add(lblPorciones, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		spnPorciones = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
		formPanel.add(spnPorciones, gbc);

		// descripción
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 0.0;
		JLabel lblDescripcion = new JLabel("Descripción:");
		formPanel.add(lblDescripcion, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtDescripcion = new JTextArea(4, 20);
		txtDescripcion.setLineWrap(true);
		txtDescripcion.setWrapStyleWord(true);
		JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
		formPanel.add(scrollDescripcion, gbc);
		EstiloManager.aplicarColorBarraDesplazamiento(scrollDescripcion);

		// instrucciones
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.weightx = 0.0;
		JLabel lblInstrucciones = new JLabel("Instrucciones:");
		formPanel.add(lblInstrucciones, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		txtInstrucciones = new JTextArea(6, 20);
		txtInstrucciones.setLineWrap(true);
		txtInstrucciones.setWrapStyleWord(true);
		JScrollPane scrollInstrucciones = new JScrollPane(txtInstrucciones);
		formPanel.add(scrollInstrucciones, gbc);
		EstiloManager.aplicarColorBarraDesplazamiento(scrollInstrucciones);

		contentPanel.add(formPanel);

		// panel de ingredientes
		JPanel ingredientesContainer = new JPanel(new BorderLayout());
		ingredientesContainer.setOpaque(false);
		ingredientesContainer.setBorder(BorderFactory.createTitledBorder("Ingredientes"));

		ingredientesPanel = new JPanel();
		ingredientesPanel.setLayout(new BoxLayout(ingredientesPanel, BoxLayout.Y_AXIS));
		ingredientesPanel.setOpaque(false);

		JScrollPane scrollIngredientes = new JScrollPane(ingredientesPanel);
		scrollIngredientes.setOpaque(false);
		scrollIngredientes.getViewport().setOpaque(false);
		scrollIngredientes.setPreferredSize(new Dimension(getWidth() - 50, 100));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollIngredientes);
		
		JPanel addIngredientePanel = new JPanel(new BorderLayout());
		addIngredientePanel.setOpaque(false);
		txtNuevoIngrediente = new JTextField();
		btnAgregarIngrediente = new JButton("Agregar");
		EstiloManager.aplicarEstiloBoton(btnAgregarIngrediente);
		btnAgregarIngrediente.addActionListener(e -> agregarIngrediente());

		addIngredientePanel.add(txtNuevoIngrediente, BorderLayout.CENTER);
		addIngredientePanel.add(btnAgregarIngrediente, BorderLayout.EAST);

		ingredientesContainer.add(scrollIngredientes, BorderLayout.CENTER);
		ingredientesContainer.add(addIngredientePanel, BorderLayout.SOUTH);

		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(ingredientesContainer);

		// panel de fotos
		JPanel fotosContainer = new JPanel(new BorderLayout());
		fotosContainer.setOpaque(false);
		fotosContainer.setBorder(BorderFactory.createTitledBorder("Fotos"));

		fotosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fotosPanel.setOpaque(false);

		JScrollPane scrollFotos = new JScrollPane(fotosPanel);
		scrollFotos.setOpaque(false);
		scrollFotos.getViewport().setOpaque(false);
		scrollFotos.setPreferredSize(new Dimension(getWidth() - 50, 150));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollFotos); 
		
		btnAgregarFoto = new JButton("Agregar Foto");
		EstiloManager.aplicarEstiloBoton(btnAgregarFoto);
		btnAgregarFoto.addActionListener(e -> seleccionarFoto());

		fotosContainer.add(scrollFotos, BorderLayout.CENTER);
		fotosContainer.add(btnAgregarFoto, BorderLayout.SOUTH);

		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(fotosContainer);

		// panel de videos
		JPanel videosContainer = new JPanel(new BorderLayout());
		videosContainer.setOpaque(false);
		videosContainer.setBorder(BorderFactory.createTitledBorder("Videos"));

		videosPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		videosPanel.setOpaque(false);

		JScrollPane scrollVideos = new JScrollPane(videosPanel);
		scrollVideos.setOpaque(false);
		scrollVideos.getViewport().setOpaque(false);
		scrollVideos.setPreferredSize(new Dimension(getWidth() - 50, 150));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollVideos); 
		
		JPanel botonesVideoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		botonesVideoPanel.setOpaque(false);

		btnAgregarVideoArchivo = new JButton("Subir Video");
		EstiloManager.aplicarEstiloBoton(btnAgregarVideoArchivo);
		btnAgregarVideoArchivo.addActionListener(e -> seleccionarVideoArchivo());

		btnAgregarVideoURL = new JButton("Agregar URL de Video");
		EstiloManager.aplicarEstiloBoton(btnAgregarVideoURL);
		btnAgregarVideoURL.addActionListener(e -> agregarVideoURL());

		botonesVideoPanel.add(btnAgregarVideoArchivo);
		botonesVideoPanel.add(btnAgregarVideoURL);

		videosContainer.add(scrollVideos, BorderLayout.CENTER);
		videosContainer.add(botonesVideoPanel, BorderLayout.SOUTH);

		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(videosContainer);

		// botón guardar
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setOpaque(false);
		btnGuardar = new JButton("Guardar Receta");
		EstiloManager.aplicarEstiloBoton(btnGuardar);
		btnGuardar.setBackground(new Color(76, 175, 80));
		btnGuardar.addActionListener(e -> guardarReceta());
		buttonPanel.add(btnGuardar);

		contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		contentPanel.add(buttonPanel);

		JScrollPane scrollContent = new JScrollPane(contentPanel);
		scrollContent.setOpaque(false);
		scrollContent.getViewport().setOpaque(false);
		scrollContent.setBorder(null);
		mainPanel.add(scrollContent, BorderLayout.CENTER);
		EstiloManager.aplicarColorBarraDesplazamiento(scrollContent); 

		cargarDatosReceta();
	}

	private void cargarDatosReceta() {
		if (!esNueva) {
			txtTitulo.setText(receta.getTitulo());
			txtDescripcion.setText(receta.getDescripcion());
			txtInstrucciones.setText(receta.getInstrucciones());
			spnTiempoPreparacion.setValue(receta.getTiempoPreparacion());
			cbxDificultad.setSelectedItem(receta.getDificultad());
			spnPorciones.setValue(receta.getPorciones());

			if (receta.getCategoria() != null) {
				for (int i = 0; i < cbxCategoria.getItemCount(); i++) {
					Categoria cat = cbxCategoria.getItemAt(i);
					if (cat.getId() == receta.getCategoria().getId()) {
						cbxCategoria.setSelectedIndex(i);
						break;
					}
				}
			}

			// carga ingredientes
			actualizarListaIngredientes();

			// carga fotos
			if (receta.getFotos() != null) {
				for (FotoReceta foto : receta.getFotos()) {
					agregarFotoAPanel(foto);
				}
			}

			// carga videos
			if (receta.getVideos() != null) {
				for (VideoReceta video : receta.getVideos()) {
					agregarVideoAPanel(video);
				}
			}
		}
	}

	private void agregarIngrediente() {
		String ingrediente = txtNuevoIngrediente.getText().trim();
		if (!ingrediente.isEmpty()) {
			ingredientes.add(ingrediente);
			txtNuevoIngrediente.setText("");
			actualizarListaIngredientes();
		}
	}

	private void actualizarListaIngredientes() {
		ingredientesPanel.removeAll();

		for (int i = 0; i < ingredientes.size(); i++) {
			final int index = i;
			JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(false);

			JLabel label = new JLabel(ingredientes.get(i));
			label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

			JButton btnEliminar = new JButton("×");
			btnEliminar.setForeground(Color.RED);
			btnEliminar.setBorderPainted(false);
			btnEliminar.setContentAreaFilled(false);
			btnEliminar.setFocusPainted(false);
			btnEliminar.addActionListener(e -> {
				ingredientes.remove(index);
				actualizarListaIngredientes();
			});

			panel.add(label, BorderLayout.CENTER);
			panel.add(btnEliminar, BorderLayout.EAST);
			ingredientesPanel.add(panel);
		}

		ingredientesPanel.revalidate();
		ingredientesPanel.repaint();
	}

	private void seleccionarFoto() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Seleccionar Foto");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				// lee el archivo como bytes
				byte[] contenido = Files.readAllBytes(selectedFile.toPath());

				// determina el tipo MIME
				String tipoMime = Files.probeContentType(selectedFile.toPath());
				if (tipoMime == null) {
					// Si no se puede determinar, usa un valor predeterminado basado en la extensión
					String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1)
							.toLowerCase();
					switch (extension) {
					case "jpg":
					case "jpeg":
						tipoMime = "image/jpeg";
						break;
					case "png":
						tipoMime = "image/png";
						break;
					case "gif":
						tipoMime = "image/gif";
						break;
					default:
						tipoMime = "application/octet-stream";
					}
				}

				// crea una foto temporal para mostrar en la interfaz
				String nombreArchivo = selectedFile.getAbsolutePath();
				FotoReceta foto = new FotoReceta(nombreArchivo, "", true, contenido, tipoMime);

				// agrega a la lista de fotos temporales
				fotosTemporales.add(foto);

				// muestra en la interfaz
				agregarFotoAPanel(foto);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void agregarFotoAPanel(FotoReceta foto) {
		try {
			BufferedImage img = null;

			// intenta cargar desde contenido binario
			if (foto.getContenido() != null && foto.getContenido().length > 0) {
				ByteArrayInputStream bis = new ByteArrayInputStream(foto.getContenido());
				img = ImageIO.read(bis);
				System.out.println("Cargando imagen desde contenido binario");
			}
			// intenta cargar desde URL si el nombre de archivo es una URL
			else if (foto.getNombreArchivo() != null && foto.getNombreArchivo().startsWith("http")) {
				try {
					System.out.println("Cargando imagen desde URL: " + foto.getNombreArchivo());
					// Crear URI primero y luego convertir a URL
					URI uri = new URI(foto.getNombreArchivo());
					URL url = uri.toURL();
					
					img = ImageIO.read(url);
					System.out.println("Cargando imagen desde URL: " + foto.getNombreArchivo());
				} catch (Exception e) {
					System.err.println("Error al cargar imagen desde URL: " + e.getMessage());
				}
			}

			// Si no se pudo cargar, usa una imagen por defecto
			if (img == null) {
				System.out.println("Usando imagen por defecto");
				// Crear una imagen en blanco como alternativa
				img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = img.createGraphics();
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.fillRect(0, 0, 100, 100);
				g2d.setColor(Color.BLACK);
				g2d.drawString("No disponible", 10, 50);
				g2d.dispose();
			}

			// escala imagen
			Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaledImg);

			JPanel fotoPanel = new JPanel(new BorderLayout());
			fotoPanel.setOpaque(false);
			fotoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			JLabel imgLabel = new JLabel(icon);
			imgLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

			JButton btnEliminar = new JButton("×");
			btnEliminar.setForeground(Color.RED);
			btnEliminar.setBorderPainted(false);
			btnEliminar.setContentAreaFilled(false);
			btnEliminar.setFocusPainted(false);

			final FotoReceta fotoFinal = foto;

			btnEliminar.addActionListener(e -> {
				fotosTemporales.remove(fotoFinal);
				fotosPanel.remove(fotoPanel);
				fotosPanel.revalidate();
				fotosPanel.repaint();
			});

			fotoPanel.add(imgLabel, BorderLayout.CENTER);
			fotoPanel.add(btnEliminar, BorderLayout.NORTH);

			fotosPanel.add(fotoPanel);
			fotosPanel.revalidate();
			fotosPanel.repaint();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void seleccionarVideoArchivo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Seleccionar Video");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Videos", "mp4", "avi", "mov", "wmv"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			try {
				// lee el archivo como bytes
				byte[] contenido = Files.readAllBytes(selectedFile.toPath());

				// determina el tipo MIME
				String tipoMime = Files.probeContentType(selectedFile.toPath());
				if (tipoMime == null) {
					// Si no se puede determinar, usa un valor predeterminado basado en la extensión
					String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1)
							.toLowerCase();
					switch (extension) {
					case "mp4":
						tipoMime = "video/mp4";
						break;
					case "avi":
						tipoMime = "video/x-msvideo";
						break;
					case "mov":
						tipoMime = "video/quicktime";
						break;
					case "wmv":
						tipoMime = "video/x-ms-wmv";
						break;
					default:
						tipoMime = "application/octet-stream";
					}
				}

				// solicita descripción
				String descripcion = JOptionPane.showInputDialog(this, "Ingrese una descripción para el video:",
						"Descripción del Video", JOptionPane.QUESTION_MESSAGE);

				if (descripcion != null) {
					// crea un video temporal para mostrar en la interfaz
					String nombreArchivo = selectedFile.getName();
					VideoReceta video = new VideoReceta(nombreArchivo, descripcion, null, contenido, tipoMime);

					// agrega a la lista de videos temporales
					videosTemporales.add(video);

					// muestra en la interfaz
					agregarVideoAPanel(video);
				}

			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void agregarVideoURL() {
		String url = JOptionPane.showInputDialog(this, "Ingrese la URL del video:", "Agregar Video por URL",
				JOptionPane.QUESTION_MESSAGE);

		if (url != null && !url.trim().isEmpty()) {
			try {
				new URI(url); // valida URL

				String descripcion = JOptionPane.showInputDialog(this, "Ingrese una descripción para el video:");

				if (descripcion != null) {
					VideoReceta video = new VideoReceta();
					video.setUrlVideo(url);
					video.setDescripcion(descripcion);

					videosTemporales.add(video);
					agregarVideoAPanel(video);
				}
			} catch (URISyntaxException e) {
				JOptionPane.showMessageDialog(this, "URL inválida. Por favor, ingrese una URL válida.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void agregarVideoAPanel(VideoReceta video) {
		JPanel videoPanel = new JPanel(new BorderLayout());
		videoPanel.setOpaque(false);
		videoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Icono de video
		JLabel iconLabel = new JLabel("🎬");
		iconLabel.setFont(new Font("Arial", Font.PLAIN, 32));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// panel de información
		JPanel infoPanel = new JPanel(new GridLayout(2, 1));
		infoPanel.setOpaque(false);

		JLabel nombreLabel = new JLabel(
				video.getUrlVideo() != null ? "URL: " + video.getUrlVideo() : "Archivo: " + video.getNombreArchivo());
		JLabel descLabel = new JLabel(video.getDescripcion());

		infoPanel.add(nombreLabel);
		infoPanel.add(descLabel);

		// botón eliminar
		JButton btnEliminar = new JButton("×");
		btnEliminar.setForeground(Color.RED);
		btnEliminar.setBorderPainted(false);
		btnEliminar.setContentAreaFilled(false);
		btnEliminar.setFocusPainted(false);

		btnEliminar.addActionListener(e -> {
			videosTemporales.remove(video);
			videosPanel.remove(videoPanel);
			videosPanel.revalidate();
			videosPanel.repaint();
		});

		videoPanel.add(iconLabel, BorderLayout.WEST);
		videoPanel.add(infoPanel, BorderLayout.CENTER);
		videoPanel.add(btnEliminar, BorderLayout.EAST);

		videosPanel.add(videoPanel);
		videosPanel.revalidate();
		videosPanel.repaint();
	}

	private void guardarReceta() {
		// valida campos requeridos
		if (txtTitulo.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "El título es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// actualiza datos de la receta
			receta.setTitulo(txtTitulo.getText().trim());
			receta.setDescripcion(txtDescripcion.getText().trim());
			receta.setInstrucciones(txtInstrucciones.getText().trim());
			receta.setTiempoPreparacion((int) spnTiempoPreparacion.getValue());
			receta.setDificultad((String) cbxDificultad.getSelectedItem());
			receta.setPorciones((int) spnPorciones.getValue());
			receta.setCategoria((Categoria) cbxCategoria.getSelectedItem());
			receta.setIngredientes(new ArrayList<>(ingredientes));

			if (esNueva) {
				receta.setUsuario(usuarioActual);
				recetaController.agregarReceta(receta);
			} else {
				recetaController.actualizarReceta(receta);
			}

			// procesa fotos
			for (FotoReceta foto : fotosTemporales) {
				foto.setReceta(receta);
				
				// asegura que el valor de esPrincipal esté establecido
				// si no se ha marcado, al menos que no quede en null
				
				if (foto.isEsPrincipal() != false) {
			        foto.setEsPrincipal(true);
					//if (foto.isEsPrincipal() != true && foto.isEsPrincipal() != false) {
					//	foto.setEsPrincipal(false); // por defecto
				}

				
				recetaController.agregarFotoAReceta(foto);

			}

			// procesa videos
			for (VideoReceta video : videosTemporales) {
				video.setReceta(receta);
				recetaController.agregarVideoAReceta(video);

			}
			// asegurarse de que al guardar se vuelva a la ventana padre
			dispose();
			if (parentFrame != null) {
				parentFrame.setVisible(true);
			}
			JOptionPane.showMessageDialog(this, "Receta guardada correctamente", "Éxito",
					JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al guardar la receta: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	@Override
	protected void onBackButtonPressed() {
		try {
			if (hayDatosSinGuardar()) {
				int opcion = JOptionPane.showConfirmDialog(this, "¿Desea guardar los cambios antes de salir?",
						"Guardar Cambios", JOptionPane.YES_NO_CANCEL_OPTION);

				if (opcion == JOptionPane.YES_OPTION) {
					guardarReceta();
					return; // La función guardarReceta ya maneja el cierre y la visibilidad del padre
				} else if (opcion == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
		} catch (NullPointerException e) {
			// Si hay un NullPointerException, simplemente cerramos la ventana
			System.out.println("Advertencia: Algunos componentes no están inicializados. Cerrando ventana.");
		}
		dispose();
		// restaura ventana padre al cerrar
		if (parentFrame != null) {
			parentFrame.setVisible(true);
		}
	}

	private boolean hayDatosSinGuardar() {
		// verifica que los componentes estén inicializados
		if (txtTitulo == null || txtDescripcion == null || txtInstrucciones == null) {
			return false;
		}
		if (esNueva) {
			return !txtTitulo.getText().trim().isEmpty() || !txtDescripcion.getText().trim().isEmpty()
					|| !txtInstrucciones.getText().trim().isEmpty() || !ingredientes.isEmpty()
					|| !fotosTemporales.isEmpty() || !videosTemporales.isEmpty();
		} else {
			return !txtTitulo.getText().equals(receta.getTitulo())
					|| !txtDescripcion.getText().equals(receta.getDescripcion())
					|| !txtInstrucciones.getText().equals(receta.getInstrucciones())
					|| (int) spnTiempoPreparacion.getValue() != receta.getTiempoPreparacion()
					|| !cbxDificultad.getSelectedItem().equals(receta.getDificultad())
					|| (int) spnPorciones.getValue() != receta.getPorciones()
					|| !ingredientes.equals(receta.getIngredientes()) || !fotosTemporales.isEmpty()
					|| !videosTemporales.isEmpty();
		}
	}
}