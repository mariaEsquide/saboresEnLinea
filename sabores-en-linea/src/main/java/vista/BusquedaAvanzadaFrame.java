package vista;

import controlador.RecetaController;
import modelo.Receta;
import modelo.SessionManager;
import modelo.Usuario;
import util.EstiloManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.List;

public class BusquedaAvanzadaFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JTextField busquedaField;
	private JComboBox<String> categoriaComboBox;
	private JComboBox<String> dificultadComboBox;
	private JButton buscarButton;
	private JTable resultadosTable;
	private DefaultTableModel tableModel;
	private JButton verRecetaButton;

	private RecetaController recetaController;
	private Usuario usuarioActual;
	private JFrame parentFrame;

	public BusquedaAvanzadaFrame() {
		super("Búsqueda Avanzada de Recetas");
		this.recetaController = new RecetaController();
		this.usuarioActual = SessionManager.getUsuarioActual();
		setupComponents();

		// manejar cierre de ventana
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
		categoriaComboBox = new JComboBox<>(new String[] { "Todas", "Entrantes", "Platos principales", "Postres" });
		dificultadComboBox = new JComboBox<>(new String[] { "Todas", "Fácil", "Media", "Difícil" });
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
		String[] columnNames = { "ID", "Título", "Categoría", "Tiempo de Preparación", "Dificultad" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		resultadosTable = new JTable(tableModel);
		EstiloManager.aplicarEstiloTabla(resultadosTable);

		// configurar ancho de columnas
		resultadosTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
		resultadosTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Título
		resultadosTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Categoría
		resultadosTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Tiempo
		resultadosTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Dificultad

		// ScrollPane para la tabla
		JScrollPane scrollPane = new JScrollPane(resultadosTable);
		scrollPane.setPreferredSize(new Dimension(700, 400));

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
	}

	private void buscarRecetas() {
		String busqueda = busquedaField.getText();
		String categoria = (String) categoriaComboBox.getSelectedItem();
		String dificultad = (String) dificultadComboBox.getSelectedItem();

		// limpia tabla
		tableModel.setRowCount(0);

		try {
			List<Receta> resultados = recetaController.buscarRecetas(busqueda, categoria, dificultad);
			if (resultados.isEmpty()) {
				JOptionPane.showMessageDialog(this, "No se encontraron recetas con los criterios especificados.",
						"Sin resultados", JOptionPane.INFORMATION_MESSAGE);
			} else {
				for (Receta receta : resultados) {
					tableModel.addRow(new Object[] { receta.getId(), receta.getTitulo(),
							receta.getCategoria() != null ? receta.getCategoria().getNombre() : "",
							receta.getTiempoPreparacion() + " min", receta.getDificultad() });
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al buscar recetas: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
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
		if (usuarioActual != null) {
			if (usuarioActual.esAdmin()) {
				new AdminFrame().setVisible(true);
			} else {
				new UserFrame().setVisible(true);
			}
		} else {
			new LoginFrame().setVisible(true);
		}
	}

	@Override
	protected void onBackButtonPressed() {
		volverAFramePadre();
	}

	public JFrame getParentFrame() {
		return parentFrame;
	}

	public void setParentFrame(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}
}