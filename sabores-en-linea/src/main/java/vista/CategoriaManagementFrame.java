package vista;

import controlador.CategoriaController;
import modelo.Categoria;
import util.EstiloManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class CategoriaManagementFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JTable categoriasTable;
	private DefaultTableModel tableModel;
	private CategoriaController categoriaController;
	private JButton agregarButton;
	private JButton editarButton;
	private JButton eliminarButton;

	public CategoriaManagementFrame() {
		super("Gestión de Categorías");
		this.categoriaController = new CategoriaController();
		setupComponents();
		cargarCategorias();
	}

	@Override
	protected void setupComponents() {
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// tabla de categorías
		String[] columnNames = { "ID", "Nombre", "Descripción" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // hace la tabla no editable
			}
		};
		categoriasTable = new JTable(tableModel);
		EstiloManager.aplicarEstiloTabla(categoriasTable);

		// ScrollPane para la tabla
		JScrollPane scrollPane = new JScrollPane(categoriasTable);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollPane);
		

		// panel de botones
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
		buttonPanel.setOpaque(false);

		agregarButton = new JButton("Agregar Categoría");
		editarButton = new JButton("Editar Categoría");
		eliminarButton = new JButton("Eliminar Categoría");

		EstiloManager.aplicarEstiloBoton(agregarButton);
		EstiloManager.aplicarEstiloBoton(editarButton);
		EstiloManager.aplicarEstiloBoton(eliminarButton);

		buttonPanel.add(agregarButton);
		buttonPanel.add(editarButton);
		buttonPanel.add(eliminarButton);

		// añade componentes al panel principal
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		// añade el panel principal
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// configura acciones de botones
		agregarButton.addActionListener(e -> agregarCategoria());
		editarButton.addActionListener(e -> editarCategoria());
		eliminarButton.addActionListener(e -> eliminarCategoria());

		// selección de fila en la tabla
		categoriasTable.getSelectionModel().addListSelectionListener(e -> {
			boolean filaSeleccionada = categoriasTable.getSelectedRow() >= 0;
			editarButton.setEnabled(filaSeleccionada);
			eliminarButton.setEnabled(filaSeleccionada);
		});

		// inicialmente deshabilitar botones hasta que se seleccione una categoría
		editarButton.setEnabled(false);
		eliminarButton.setEnabled(false);
	}

	private void cargarCategorias() {
		// limpia tabla
		tableModel.setRowCount(0);

		try {
			List<Categoria> categorias = categoriaController.obtenerTodasLasCategorias();
			for (Categoria categoria : categorias) {
				tableModel
						.addRow(new Object[] { categoria.getId(), categoria.getNombre(), categoria.getDescripcion() });
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// usa JDialog modal para agregar categoría
	private void agregarCategoria() {
		// crea un JDialog modal con el estilo de la aplicación
		JDialog dialog = new JDialog(this, "Agregar Categoría", true);
		dialog.setIconImage(getIconImage());

		// panel con el formulario
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(EstiloManager.COLOR_FONDO);

		// componentes del formulario
		JTextField nombreField = new JTextField(20);
		JTextArea descripcionArea = new JTextArea(5, 20);
		descripcionArea.setLineWrap(true);
		descripcionArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(descripcionArea);

		JButton guardarButton = new JButton("Guardar");
		EstiloManager.aplicarEstiloBoton(guardarButton);

		// configura layout
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// agrega componentes
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Nombre:"), gbc);
		gbc.gridx = 1;
		panel.add(nombreField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Descripción:"), gbc);
		gbc.gridx = 1;
		panel.add(scrollPane, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(guardarButton, gbc);

		// acción del botón guardar
		guardarButton.addActionListener(e -> {
			try {
				// valida campos
				String nombre = nombreField.getText().trim();
				String descripcion = descripcionArea.getText().trim();

				if (nombre.isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "El nombre de la categoría es obligatorio",
							"Error de validación", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// crea y guarda categoría
				Categoria nuevaCategoria = new Categoria();
				nuevaCategoria.setNombre(nombre);
				nuevaCategoria.setDescripcion(descripcion);

				categoriaController.actualizarCategoria(nuevaCategoria);

				dialog.dispose(); // cierra el diálogo
				cargarCategorias(); // recarga la lista de categorías

				JOptionPane.showMessageDialog(this, "Categoría agregada correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Error al guardar categoría: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		// configura y muestra el diálogo
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	// usa JDialog modal para editar categoría
	private void editarCategoria() {
		int selectedRow = categoriasTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione una categoría para editar",
					"Selección requerida", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int categoriaId = (int) tableModel.getValueAt(selectedRow, 0);

		try {
			Categoria categoria = categoriaController.obtenerCategoriaPorId(categoriaId);
			if (categoria == null) {
				JOptionPane.showMessageDialog(this, "No se pudo cargar la información de la categoría", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// crea un JDialog modal con el estilo de la aplicación
			JDialog dialog = new JDialog(this, "Editar Categoría", true);
			dialog.setIconImage(getIconImage());

			// panel con el formulario
			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			panel.setBackground(EstiloManager.COLOR_FONDO);

			// componentes del formulario
			JTextField nombreField = new JTextField(categoria.getNombre(), 20);
			JTextArea descripcionArea = new JTextArea(categoria.getDescripcion(), 5, 20);
			descripcionArea.setLineWrap(true);
			descripcionArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(descripcionArea);

			JButton guardarButton = new JButton("Guardar");
			EstiloManager.aplicarEstiloBoton(guardarButton);

			// configura layout
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// agrega componentes
			gbc.gridx = 0;
			gbc.gridy = 0;
			panel.add(new JLabel("Nombre:"), gbc);
			gbc.gridx = 1;
			panel.add(nombreField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			panel.add(new JLabel("Descripción:"), gbc);
			gbc.gridx = 1;
			panel.add(scrollPane, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			panel.add(guardarButton, gbc);

			// acción del botón guardar
			guardarButton.addActionListener(e -> {
				try {
					// valida campos
					String nombre = nombreField.getText().trim();
					String descripcion = descripcionArea.getText().trim();

					if (nombre.isEmpty()) {
						JOptionPane.showMessageDialog(dialog, "El nombre de la categoría es obligatorio",
								"Error de validación", JOptionPane.ERROR_MESSAGE);
						return;
					}

					// actualiza categoría
					categoria.setNombre(nombre);
					categoria.setDescripcion(descripcion);

					categoriaController.actualizarCategoria(categoria);

					dialog.dispose(); // cierra el diálogo
					cargarCategorias(); // recarga la lista de categorías

					JOptionPane.showMessageDialog(this, "Categoría actualizada correctamente", "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, "Error al actualizar categoría: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});

			// configura y muestra el diálogo
			dialog.setContentPane(panel);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al cargar categoría: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void eliminarCategoria() {
		int selectedRow = categoriasTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione una categoría para eliminar",
					"Selección requerida", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int categoriaId = (int) tableModel.getValueAt(selectedRow, 0);
		String nombreCategoria = (String) tableModel.getValueAt(selectedRow, 1);

		// confirma eliminación
		int option = JOptionPane.showConfirmDialog(this,
				"¿Está seguro de que desea eliminar la categoría '" + nombreCategoria + "'?", "Confirmar eliminación",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
			try {
				categoriaController.eliminarCategoria(categoriaId);
				cargarCategorias(); // recarga la lista de categorías

				JOptionPane.showMessageDialog(this, "Categoría eliminada correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error al eliminar categoría: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void onBackButtonPressed() {
		// vuelve a la pantalla de administración
		// AdminFrame frame = new AdminFrame();
		// dispose();
		// frame.setVisible(true);

		// vuelve a la pantalla de administración sin verificación adicional
		openNewFrame(new AdminFrame());
	}
}