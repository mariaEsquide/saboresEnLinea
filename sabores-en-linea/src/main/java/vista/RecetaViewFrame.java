package vista;

import modelo.Receta;
import modelo.SessionManager;
import modelo.Valoracion;
import util.EstiloManager;
import util.DatabaseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;

public class RecetaViewFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private Receta receta;
	private JButton valorarButton;
	private JPanel valoracionesPanel;
	private JFrame parentFrame;

	public RecetaViewFrame(Receta receta) {
		super("Receta: " + receta.getTitulo());
		// carga completamente la receta con sus colecciones
		this.receta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
		this.parentFrame = null;
		setupComponents();
	}

	// constructor que acepta el frame padre
	public RecetaViewFrame(Receta receta, JFrame parentFrame) {
		super("Receta: " + receta.getTitulo());
		// carga completamente la receta con sus colecciones
		this.receta = DatabaseUtil.obtenerRecetaCompleta(receta.getId());
		this.parentFrame = parentFrame;

		// listener : maneja el cierre de la ventana
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				if (parentFrame != null) {
					parentFrame.setVisible(true);
				}
			}
		});

		setupComponents();
	}

	@Override
	protected void setupComponents() {
		// panel principal con scroll
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// panel de información
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setOpaque(false);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

		// título
		JLabel titleLabel = new JLabel(receta.getTitulo());
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(titleLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		// categoría y dificultad
		JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		metaPanel.setOpaque(false);

		JLabel categoriaLabel = new JLabel(
				"Categoría: " + (receta.getCategoria() != null ? receta.getCategoria().getNombre() : "Sin categoría"));
		categoriaLabel.setFont(new Font("Arial", Font.ITALIC, 14));

		JLabel dificultadLabel = new JLabel(" | Dificultad: " + receta.getDificultad());
		dificultadLabel.setFont(new Font("Arial", Font.ITALIC, 14));

		JLabel tiempoLabel = new JLabel(" | Tiempo: " + receta.getTiempoPreparacion() + " min");
		tiempoLabel.setFont(new Font("Arial", Font.ITALIC, 14));

		JLabel porcionesLabel = new JLabel(" | Porciones: " + receta.getPorciones());
		porcionesLabel.setFont(new Font("Arial", Font.ITALIC, 14));

		metaPanel.add(categoriaLabel);
		metaPanel.add(dificultadLabel);
		metaPanel.add(tiempoLabel);
		metaPanel.add(porcionesLabel);
		metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// valoración promedio con estrellas Unicode
		JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ratingPanel.setOpaque(false);

		// caracteres Unicode para estrellas
		String starFilled = "\u2605"; // estrella rellena
		String starEmpty = "\u2606"; // estrella vacía

		StringBuilder estrellas = new StringBuilder();
		int valoracionPromedio = (int) Math.round(receta.getValoracionPromedio());
		for (int i = 0; i < 5; i++) {
			estrellas.append(i < valoracionPromedio ? starFilled : starEmpty);
		}

		JLabel valoracionPromedioLabel = new JLabel("Valoración: ");
		valoracionPromedioLabel.setFont(new Font("Arial", Font.BOLD, 16));

		JLabel starsLabel = new JLabel(estrellas.toString());
		starsLabel.setFont(new Font("Dialog", Font.PLAIN, 18));
		starsLabel.setForeground(new Color(255, 215, 0)); // dorado

		JLabel valoracionNumLabel = new JLabel(" (" + String.format("%.1f", receta.getValoracionPromedio()) + ")");
		valoracionNumLabel.setFont(new Font("Arial", Font.BOLD, 14));

		ratingPanel.add(valoracionPromedioLabel);
		ratingPanel.add(starsLabel);
		ratingPanel.add(valoracionNumLabel);
		ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		infoPanel.add(metaPanel);
		infoPanel.add(ratingPanel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// descripción
		JLabel descTitleLabel = new JLabel("Descripción:");
		descTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		descTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(descTitleLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JTextArea descTextArea = new JTextArea(receta.getDescripcion());
		descTextArea.setEditable(false);
		descTextArea.setLineWrap(true);
		descTextArea.setWrapStyleWord(true);
		descTextArea.setOpaque(false);
		descTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
		descTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(descTextArea);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// ingredientes
		JLabel ingredientesTitleLabel = new JLabel("Ingredientes:");
		ingredientesTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		ingredientesTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(ingredientesTitleLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JPanel ingredientesPanel = new JPanel();
		ingredientesPanel.setLayout(new BoxLayout(ingredientesPanel, BoxLayout.Y_AXIS));
		ingredientesPanel.setOpaque(false);

		if (receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()) {
			for (String ingrediente : receta.getIngredientes()) {
				JLabel ingredienteLabel = new JLabel("• " + ingrediente);
				ingredienteLabel.setFont(new Font("Arial", Font.PLAIN, 14));
				ingredientesPanel.add(ingredienteLabel);
			}
		} else {
			JLabel noIngredientesLabel = new JLabel("No hay ingredientes disponibles");
			ingredientesPanel.add(noIngredientesLabel);
		}

		ingredientesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(ingredientesPanel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// instrucciones
		JLabel instruccionesTitleLabel = new JLabel("Instrucciones:");
		instruccionesTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		instruccionesTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(instruccionesTitleLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		JTextArea instruccionesTextArea = new JTextArea(receta.getInstrucciones());
		instruccionesTextArea.setEditable(false);
		instruccionesTextArea.setLineWrap(true);
		instruccionesTextArea.setWrapStyleWord(true);
		instruccionesTextArea.setOpaque(false);
		instruccionesTextArea.setFont(new Font("Arial", Font.PLAIN, 14));

		JScrollPane instruccionesScrollPane = new JScrollPane(instruccionesTextArea);
		instruccionesScrollPane.setOpaque(false);
		instruccionesScrollPane.getViewport().setOpaque(false);
		instruccionesScrollPane.setPreferredSize(new Dimension(600, 200));
		instruccionesScrollPane.setBorder(null);
		instruccionesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		infoPanel.add(instruccionesScrollPane);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// valoraciones
		JLabel valoracionesTitleLabel = new JLabel("Valoraciones:");
		valoracionesTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
		valoracionesTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(valoracionesTitleLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// panel de valoraciones
		valoracionesPanel = new JPanel();
		valoracionesPanel.setLayout(new BoxLayout(valoracionesPanel, BoxLayout.Y_AXIS));
		valoracionesPanel.setOpaque(false);

		// carga valoraciones
		List<Valoracion> valoraciones = DatabaseUtil.obtenerValoracionesPorReceta(receta.getId());

		if (valoraciones != null && !valoraciones.isEmpty()) {
			for (Valoracion valoracion : valoraciones) {
				JPanel valoracionPanel = new JPanel(new BorderLayout(10, 5));
				valoracionPanel.setOpaque(false);
				valoracionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

				// información del usuario y puntuación
				JPanel infoValoracionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				infoValoracionPanel.setOpaque(false);

				JLabel usuarioLabel = new JLabel(valoracion.getUsuario().getNombre());
				usuarioLabel.setFont(new Font("Arial", Font.BOLD, 14));

				// crea estrellas para la puntuación con Unicode
				StringBuilder estrellasVal = new StringBuilder();
				for (int i = 0; i < 5; i++) {
					estrellasVal.append(i < valoracion.getPuntuacion() ? starFilled : starEmpty);
				}

				JLabel puntuacionLabel = new JLabel(" - " + estrellasVal.toString());
				puntuacionLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
				puntuacionLabel.setForeground(new Color(255, 215, 0)); // dorado

				// formatea fecha usando SimpleDateFormat
				String fechaFormateada = "Sin fecha";
				if (valoracion.getFecha() != null) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						fechaFormateada = sdf.format(valoracion.getFecha());
					} catch (Exception e) {
						fechaFormateada = "Fecha no disponible";
						System.err.println("Error al formatear fecha: " + e.getMessage());
					}
				}

				JLabel fechaLabel = new JLabel(" - " + fechaFormateada);
				fechaLabel.setFont(new Font("Arial", Font.ITALIC, 12));

				infoValoracionPanel.add(usuarioLabel);
				infoValoracionPanel.add(puntuacionLabel);
				infoValoracionPanel.add(fechaLabel);

				// comentario
				JTextArea comentarioArea = new JTextArea(valoracion.getComentario());
				comentarioArea.setEditable(false);
				comentarioArea.setLineWrap(true);
				comentarioArea.setWrapStyleWord(true);
				comentarioArea.setOpaque(false);
				comentarioArea.setFont(new Font("Arial", Font.PLAIN, 14));
				comentarioArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

				valoracionPanel.add(infoValoracionPanel, BorderLayout.NORTH);
				valoracionPanel.add(comentarioArea, BorderLayout.CENTER);

				valoracionesPanel.add(valoracionPanel);
				valoracionesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		} else {
			JLabel noValoracionesLabel = new JLabel("No hay valoraciones para esta receta");
			noValoracionesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
			valoracionesPanel.add(noValoracionesLabel);
		}

		JScrollPane valoracionesScrollPane = new JScrollPane(valoracionesPanel);
		valoracionesScrollPane.setOpaque(false);
		valoracionesScrollPane.getViewport().setOpaque(false);
		valoracionesScrollPane.setPreferredSize(new Dimension(600, 200));
		valoracionesScrollPane.setBorder(null);
		valoracionesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		infoPanel.add(valoracionesScrollPane);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// panel de botones - solo para valorar (sin botón de videos)
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setOpaque(false);

		// botón para valorar la receta (solo para usuarios no administradores y que no
		// hayan valorado ya)
		if (SessionManager.getUsuarioActual() != null && !SessionManager.isAdmin()) {
			// verifica si el usuario ya ha valorado esta receta
			boolean yaValorada = DatabaseUtil.usuarioYaValoroReceta(SessionManager.getUsuarioActual().getId(),
					receta.getId());

			if (!yaValorada) {
				valorarButton = new JButton("Valorar Receta");
				EstiloManager.aplicarEstiloBoton(valorarButton);
				valorarButton.setBackground(new Color(76, 175, 80));
				valorarButton.addActionListener(e -> mostrarFormularioValoracion());
				buttonPanel.add(valorarButton);
			} else {
				JLabel yaValoradaLabel = new JLabel("Ya has valorado esta receta");
				yaValoradaLabel.setFont(new Font("Arial", Font.ITALIC, 12));
				yaValoradaLabel.setForeground(new Color(100, 100, 100));
				buttonPanel.add(yaValoradaLabel);
			}
		}

		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(buttonPanel);

		// Scroll para todo el contenido
		JScrollPane scrollPane = new JScrollPane(infoPanel);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(null);

		contentPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
	}

	// formulario para valorar recetas
	private void mostrarFormularioValoracion() {
		// crea un diálogo para la valoración
		JDialog valoracionDialog = new JDialog(this, "Valorar Receta", true);
		valoracionDialog.setSize(400, 300);
		valoracionDialog.setLocationRelativeTo(this);

		JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// puntuación
		JPanel puntuacionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		puntuacionPanel.add(new JLabel("Puntuación:"));
		JSpinner puntuacionSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
		puntuacionPanel.add(puntuacionSpinner);

		// comentario
		JLabel comentarioLabel = new JLabel("Comentario:");
		JTextArea comentarioTextArea = new JTextArea(5, 20);
		comentarioTextArea.setLineWrap(true);
		comentarioTextArea.setWrapStyleWord(true);
		JScrollPane comentarioScrollPane = new JScrollPane(comentarioTextArea);

		// botón de guardar
		JButton guardarButton = new JButton("Guardar Valoración");
		EstiloManager.aplicarEstiloBoton(guardarButton);
		guardarButton.addActionListener(e -> {
			try {
				// crea y guarda valoración
				Valoracion valoracion = new Valoracion();
				valoracion.setReceta(receta);
				valoracion.setUsuario(SessionManager.getUsuarioActual());
				valoracion.setPuntuacion((int) puntuacionSpinner.getValue());
				valoracion.setComentario(comentarioTextArea.getText());

				// Usa java.util.Date en lugar de LocalDateTime
				valoracion.setFecha(new Date());

				DatabaseUtil.agregarValoracion(valoracion);

				// actualiza valoración promedio de la receta
				actualizarValoracionPromedio();

				// actualiza la vista de valoraciones
				actualizarVistaValoraciones();

				JOptionPane.showMessageDialog(valoracionDialog, "Valoración guardada correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);

				valoracionDialog.dispose();

				// dshabilita el botón de valorar después de valorar
				if (valorarButton != null) {
					valorarButton.setEnabled(false);
					valorarButton.setText("Ya valorada");
				}

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(valoracionDialog, "Error al guardar la valoración: " + ex.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		panel.add(puntuacionPanel);
		panel.add(comentarioLabel);
		panel.add(comentarioScrollPane);
		panel.add(guardarButton);

		valoracionDialog.add(panel);
		valoracionDialog.setVisible(true);
	}

	// actualiza valoracion promedio
	private void actualizarValoracionPromedio() {
		try {
			// obtiene todas las valoraciones de la receta
			List<Valoracion> valoraciones = DatabaseUtil.obtenerValoracionesPorReceta(receta.getId());

			if (valoraciones != null && !valoraciones.isEmpty()) {
				// calcula promedio
				double suma = 0;
				for (Valoracion v : valoraciones) {
					suma += v.getPuntuacion();
				}
				double promedio = suma / valoraciones.size();

				// actualiza receta
				receta.setValoracionPromedio(promedio);
				DatabaseUtil.actualizarReceta(receta);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// actualiza la vista de valoraciones
	private void actualizarVistaValoraciones() {
		// limpia panel valoraciones
		valoracionesPanel.removeAll();

		// caracteres Unicode para estrellas
		String starFilled = "\u2605"; // estrella rellena
		String starEmpty = "\u2606"; // estrella vacía

		// carga valoraciones actualizadas
		List<Valoracion> valoraciones = DatabaseUtil.obtenerValoracionesPorReceta(receta.getId());

		if (valoraciones != null && !valoraciones.isEmpty()) {
			for (Valoracion valoracion : valoraciones) {
				JPanel valoracionPanel = new JPanel(new BorderLayout(10, 5));
				valoracionPanel.setOpaque(false);
				valoracionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

				// información del usuario y puntuación
				JPanel infoValoracionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				infoValoracionPanel.setOpaque(false);

				JLabel usuarioLabel = new JLabel(valoracion.getUsuario().getNombre());
				usuarioLabel.setFont(new Font("Arial", Font.BOLD, 14));

				// crea estrellas para la puntuación con Unicode
				StringBuilder estrellas = new StringBuilder();
				for (int i = 0; i < 5; i++) {
					estrellas.append(i < valoracion.getPuntuacion() ? starFilled : starEmpty);
				}

				JLabel puntuacionLabel = new JLabel(" - " + estrellas.toString());
				puntuacionLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
				puntuacionLabel.setForeground(new Color(255, 215, 0)); // dorado

				// formatea fecha usando SimpleDateFormat
				String fechaFormateada = "Sin fecha";
				if (valoracion.getFecha() != null) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						fechaFormateada = sdf.format(valoracion.getFecha());
					} catch (Exception e) {
						fechaFormateada = "Fecha no disponible";
						System.err.println("Error al formatear fecha: " + e.getMessage());
					}
				}

				JLabel fechaLabel = new JLabel(" - " + fechaFormateada);
				fechaLabel.setFont(new Font("Arial", Font.ITALIC, 12));

				infoValoracionPanel.add(usuarioLabel);
				infoValoracionPanel.add(puntuacionLabel);
				infoValoracionPanel.add(fechaLabel);

				// comentario
				JTextArea comentarioArea = new JTextArea(valoracion.getComentario());
				comentarioArea.setEditable(false);
				comentarioArea.setLineWrap(true);
				comentarioArea.setWrapStyleWord(true);
				comentarioArea.setOpaque(false);
				comentarioArea.setFont(new Font("Arial", Font.PLAIN, 14));
				comentarioArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

				valoracionPanel.add(infoValoracionPanel, BorderLayout.NORTH);
				valoracionPanel.add(comentarioArea, BorderLayout.CENTER);

				valoracionesPanel.add(valoracionPanel);
				valoracionesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		} else {
			JLabel noValoracionesLabel = new JLabel("No hay valoraciones para esta receta");
			noValoracionesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
			valoracionesPanel.add(noValoracionesLabel);
		}

		valoracionesPanel.revalidate();
		valoracionesPanel.repaint();
	}

	@Override
	protected void onBackButtonPressed() {
		// vuelve a la ventana anterior
		dispose();
		if (parentFrame != null) {
			parentFrame.setVisible(true);
		}
	}
}