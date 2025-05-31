package vista;

import controlador.RecetaController;
import modelo.Receta;
import modelo.Usuario;
import modelo.Valoracion;
import util.DatabaseUtil;
import util.EstiloManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.List;

public class ValoracionManagementFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JTable resultadosTable;
	private DefaultTableModel tableModel;
	private JTextField busquedaField;
	private JComboBox<String> categoriaComboBox;
	private JComboBox<String> dificultadComboBox;
	private JButton buscarButton;
	private JButton verRecetaButton;

	private RecetaController recetaController;
	private Usuario usuarioActual;
	private JFrame parentFrame;

	// constructor único
	public ValoracionManagementFrame(Usuario usuario, JFrame parentFrame) {
		super("Mis Valoraciones");
		this.recetaController = new RecetaController();
		this.usuarioActual = usuario;
		this.parentFrame = parentFrame;
		setupComponents();

		// maneja cierre de ventana
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				volverAFramePadre();
			}
		});
	}

	@Override
	protected void setupComponents() {
		// panel principal
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// panel de búsqueda
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// componentes de búsqueda
		busquedaField = new JTextField(20);
		categoriaComboBox = new JComboBox<>(new String[] {"Todas", "Entrantes", "Platos principales", "Postres" });
		EstiloManager.aplicarEstiloComboBox(categoriaComboBox);
		dificultadComboBox = new JComboBox<>(new String[] {"Todas", "Fácil", "Media", "Difícil" });
		EstiloManager.aplicarEstiloComboBox(dificultadComboBox);
		buscarButton = new JButton("Buscar");
		EstiloManager.aplicarEstiloBoton(buscarButton);

		// añade componentes al panel de búsqueda
		gbc.gridx = 0;
		gbc.gridy = 0;
		searchPanel.add(new JLabel("Búsqueda:"), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		searchPanel.add(busquedaField, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		searchPanel.add(new JLabel("Categoría:"), gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		searchPanel.add(categoriaComboBox, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		searchPanel.add(new JLabel("Dificultad:"), gbc);

		gbc.gridx = 5;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		searchPanel.add(dificultadComboBox, gbc);

		gbc.gridx = 6;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(5, 15, 5, 5);
		searchPanel.add(buscarButton, gbc);

		// tabla de resultados
		String[] columnNames = { "ID", "Título", "Categoría", "Tiempo de Preparación", "Dificultad", "Puntuación" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		resultadosTable = new JTable(tableModel);
		EstiloManager.aplicarEstiloTabla(resultadosTable);

		// configura ancho de columnas
		resultadosTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
		resultadosTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Título
		resultadosTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
		resultadosTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Tiempo
		resultadosTable.getColumnModel().getColumn(4).setPreferredWidth(80); // Dificultad
		resultadosTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Puntuación

		// renderer 
		resultadosTable.getColumnModel().getColumn(5).setCellRenderer(new StarRatingTableCellRenderer());
		
		// ScrollPane para la tabla
		JScrollPane scrollPane = new JScrollPane(resultadosTable);
		scrollPane.setPreferredSize(new Dimension(700, 400));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollPane);
		

		// panel para botón de ver receta
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setOpaque(false);

		verRecetaButton = new JButton("Ver Receta Seleccionada");
		EstiloManager.aplicarEstiloBoton(verRecetaButton);
		verRecetaButton.addActionListener(e -> verRecetaSeleccionada());
		buttonPanel.add(verRecetaButton);

		// añade componentes al panel principal
		contentPanel.add(searchPanel, BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// configura acción del botón buscar
		buscarButton.addActionListener(e -> buscarRecetas());

		// configura selección de fila en la tabla
		resultadosTable.getSelectionModel().addListSelectionListener(e -> {
			verRecetaButton.setEnabled(resultadosTable.getSelectedRow() >= 0);
		});

		// inicialmente deshabilita el botón hasta que se seleccione una receta
		verRecetaButton.setEnabled(false);

		// carga las recetas valoradas al iniciar
		SwingUtilities.invokeLater(this::buscarRecetas);
	}
	
	// renderer 
	private class StarRatingTableCellRenderer implements TableCellRenderer {
	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, 
	            boolean isSelected, boolean hasFocus, int row, int column) {
	        
	        // Crea un panel con FlowLayout centrado
	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
	        panel.setOpaque(true);
	        
	        // establece colores de fondo según selección
	        if (isSelected) {
	            panel.setBackground(table.getSelectionBackground());
	        } else {
	            panel.setBackground(table.getBackground());
	        }
	        
	        // obtiene la puntuación como cadena de estrellas
	        String starsText = value != null ? value.toString() : "";
	        
	        // caracteres Unicode para estrellas 
	        String starFilled = "\u2605"; // estrella rellena
	        String starEmpty = "\u2606"; // estrella vacía
	        
	        // determina cuántas estrellas mostrar
	        int numStars = 0;
	        if (starsText.contains(starFilled)) {
	            // Contar estrellas rellenas en el texto
	            for (char c : starsText.toCharArray()) {
	                if (c == '\u2605') numStars++;
	            }
	        } else {
	            try {
	                // intenta interpretar como número
	                numStars = Integer.parseInt(starsText.trim());
	            } catch (NumberFormatException e) {
	                numStars = 0;
	            }
	        }
	        
	        // limita entre 0 y 5
	        numStars = Math.max(0, Math.min(numStars, 5));
	        
	       
	        for (int i = 0; i < 5; i++) {
	            JLabel estrella = new JLabel(i < numStars ? starFilled : starEmpty);
	            estrella.setFont(new Font("Dialog", Font.PLAIN, 16)); // Exactamente igual que en RecetaCard
	            estrella.setForeground(new Color(255, 215, 0)); // dorado, igual que en RecetaCard
	            panel.add(estrella);
	        }
	        
	        return panel;
	    }
	}
	
	// método : convierte puntuación a estrellas Unicode
	@SuppressWarnings("unused")
	private String convertirPuntuacionAEstrellas(int puntuacion) {
	    // caracteres Unicode para estrellas
	    String starFilled = "\u2605"; // estrella rellena
	    String starEmpty = "\u2606"; // estrella vacía
	    
	    // limita puntuación entre 0 y 5
	    puntuacion = Math.max(0, Math.min(puntuacion, 5));
	    
	    // construye la cadena de estrellas
	    StringBuilder stars = new StringBuilder();
	    for (int i = 0; i < 5; i++) {
	        stars.append(i < puntuacion ? starFilled : starEmpty);
	    }
	    
	    return stars.toString();
	}

	private void buscarRecetas() {
		String busqueda = busquedaField.getText();
		String categoria = (String) categoriaComboBox.getSelectedItem();
		String dificultad = (String) dificultadComboBox.getSelectedItem();

		// limpia tabla
		tableModel.setRowCount(0);

		try {
			// obtiene las recetas valoradas por el usuario actual
			List<Receta> recetasValoradas = DatabaseUtil.obtenerRecetasValoradasPorUsuario(usuarioActual.getId());

			// filtra según criterios
			List<Receta> resultados = new java.util.ArrayList<>();

			for (Receta receta : recetasValoradas) {
				boolean cumpleBusqueda = busqueda.isEmpty()
						|| receta.getTitulo().toLowerCase().contains(busqueda.toLowerCase());

				boolean cumpleCategoria = categoria.equals("Todas")
						|| (receta.getCategoria() != null && receta.getCategoria().getNombre().equals(categoria));

				boolean cumpleDificultad = dificultad.equals("Todas") || receta.getDificultad().equals(dificultad);

				if (cumpleBusqueda && cumpleCategoria && cumpleDificultad) {
					resultados.add(receta);
				}
			}

			if (resultados.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"No se encontraron recetas valoradas con los criterios especificados.", "Sin resultados",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				for (Receta receta : resultados) {
					// obtiene la puntuación del usuario para esta receta
					int puntuacion = obtenerPuntuacionUsuario(receta.getId(), usuarioActual.getId());

					
					// El renderer se encargará de convertirlo a estrellas
					tableModel.addRow(new Object[] { 
					    receta.getId(), 
					    receta.getTitulo(),
					    receta.getCategoria() != null ? receta.getCategoria().getNombre() : "",
					    receta.getTiempoPreparacion() + " min", 
					    receta.getDificultad(), 
					    puntuacion  // Pasar el número directamente
					});
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al buscar recetas valoradas: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	private int obtenerPuntuacionUsuario(int recetaId, int usuarioId) {
		try {
			List<Valoracion> valoraciones = DatabaseUtil.obtenerValoracionesPorReceta(recetaId);
			for (Valoracion v : valoraciones) {
				if (v.getUsuario().getId() == usuarioId) {
					return v.getPuntuacion();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void verRecetaSeleccionada() {
		int selectedRow = resultadosTable.getSelectedRow();
		if (selectedRow >= 0) {
			int recetaId = (int) tableModel.getValueAt(selectedRow, 0);
			try {
				Receta receta = recetaController.obtenerRecetaPorId(recetaId);
				if (receta != null) {
					setVisible(false);

					// abre la vista de receta
					RecetaViewFrame vistaReceta = new RecetaViewFrame(receta, this);
					vistaReceta.setVisible(true);
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error al cargar la receta: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para ver.", "Selección requerida",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void volverAFramePadre() {
		dispose();
		if (parentFrame != null) {
			parentFrame.setVisible(true);
		} else {
			new UserFrame().setVisible(true);
		}
	}

	@Override
	protected void onBackButtonPressed() {
		volverAFramePadre();
	}
}
