package vista;

import modelo.Usuario;
import controlador.UsuarioController;
import util.EstiloManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class UserManagementFrame extends BaseFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JTable usuariosTable;
	private DefaultTableModel tableModel;
	private UsuarioController usuarioController;
	private JButton agregarButton;
	private JButton editarButton;
	private JButton eliminarButton;

	public UserManagementFrame() {
		super("Gestión de Usuarios");
		this.usuarioController = new UsuarioController();
		setupComponents();
		cargarUsuarios();
	}

	@Override
	protected void setupComponents() {
		JPanel contentPanel = createContentPanel();
		contentPanel.setLayout(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// tabla de usuarios
		String[] columnNames = { "ID", "Nombre", "Email", "Es Admin" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // hace la tabla no editable
			}
		};
		usuariosTable = new JTable(tableModel);
		EstiloManager.aplicarEstiloTabla(usuariosTable);

		// ScrollPane para la tabla
		JScrollPane scrollPane = new JScrollPane(usuariosTable);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		EstiloManager.aplicarColorBarraDesplazamiento(scrollPane);

		// panel de botones
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
		buttonPanel.setOpaque(false);

		agregarButton = new JButton("Agregar Usuario");
		editarButton = new JButton("Editar Usuario");
		eliminarButton = new JButton("Eliminar Usuario");

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
		agregarButton.addActionListener(e -> agregarUsuario());
		editarButton.addActionListener(e -> editarUsuario());
		eliminarButton.addActionListener(e -> eliminarUsuario());

		// selección de fila en la tabla
		usuariosTable.getSelectionModel().addListSelectionListener(e -> {
			boolean filaSeleccionada = usuariosTable.getSelectedRow() >= 0;
			editarButton.setEnabled(filaSeleccionada);
			eliminarButton.setEnabled(filaSeleccionada);
		});

		// inicialmente deshabilitar botones hasta que se seleccione un usuario
		editarButton.setEnabled(false);
		eliminarButton.setEnabled(false);
	}

	private void cargarUsuarios() {
		// limpiar tabla
		tableModel.setRowCount(0);

		try {
			List<Usuario> usuarios = usuarioController.obtenerTodosLosUsuarios();
			for (Usuario usuario : usuarios) {
				tableModel.addRow(
						new Object[] { usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.esAdmin() });
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// usa JDialog modal para agregar usuario
	private void agregarUsuario() {
		// Crear un JDialog modal con el estilo de la aplicación
		JDialog dialog = new JDialog(this, "Agregar Usuario", true);
		dialog.setIconImage(getIconImage());

		// panel con el formulario
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setBackground(EstiloManager.COLOR_FONDO);

		// componentes del formulario
		JTextField nombreField = new JTextField(20);
		JTextField emailField = new JTextField(20);
		JPasswordField passwordField = new JPasswordField(20);
		JCheckBox adminCheckBox = new JCheckBox("Es administrador");
		adminCheckBox.setBackground(EstiloManager.COLOR_FONDO);

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
		panel.add(new JLabel("Email:"), gbc);
		gbc.gridx = 1;
		panel.add(emailField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Contraseña:"), gbc);
		gbc.gridx = 1;
		panel.add(passwordField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		panel.add(adminCheckBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(guardarButton, gbc);

		// acción del botón guardar
		guardarButton.addActionListener(e -> {
			try {
				// valida campos
				String nombre = nombreField.getText().trim();
				String email = emailField.getText().trim();
				String password = new String(passwordField.getPassword());
				boolean esAdmin = adminCheckBox.isSelected();

				if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios", "Error de validación",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// verifica si ya existe un usuario con ese nombre
				Usuario usuarioExistente = usuarioController.obtenerUsuarioPorNombre(nombre);
				if (usuarioExistente != null) {
					JOptionPane.showMessageDialog(dialog, "Ya existe un usuario con ese nombre", "Error de validación",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// crea y guarda usuario
				Usuario nuevoUsuario = new Usuario();
				nuevoUsuario.setNombre(nombre);
				nuevoUsuario.setEmail(email);
				nuevoUsuario.setPassword(password);
				nuevoUsuario.setEsAdmin(esAdmin);

				usuarioController.actualizarUsuario(nuevoUsuario);

				dialog.dispose(); // cierra el diálogo
				cargarUsuarios(); // recarga la lista de usuarios

				JOptionPane.showMessageDialog(this, "Usuario agregado correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(dialog, "Error al guardar usuario: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		// configura y muestra el diálogo
		dialog.setContentPane(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	// usa JDialog modal para editar usuario
	private void editarUsuario() {
		int selectedRow = usuariosTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para editar", "Selección requerida",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int usuarioId = (int) tableModel.getValueAt(selectedRow, 0);

		try {
			Usuario usuario = usuarioController.obtenerUsuarioPorId(usuarioId);
			if (usuario == null) {
				JOptionPane.showMessageDialog(this, "No se pudo cargar la información del usuario", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// crea un JDialog modal con el estilo de la aplicación
			JDialog dialog = new JDialog(this, "Editar Usuario", true);
			dialog.setIconImage(getIconImage());

			// panel con el formulario
			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			panel.setBackground(EstiloManager.COLOR_FONDO);

			// componentes del formulario
			JTextField nombreField = new JTextField(usuario.getNombre(), 20);
			JTextField emailField = new JTextField(usuario.getEmail(), 20);
			JPasswordField passwordField = new JPasswordField(20);
			JCheckBox adminCheckBox = new JCheckBox("Es administrador", usuario.esAdmin());
			adminCheckBox.setBackground(EstiloManager.COLOR_FONDO);

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
			panel.add(new JLabel("Email:"), gbc);
			gbc.gridx = 1;
			panel.add(emailField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			panel.add(new JLabel("Contraseña:"), gbc);
			gbc.gridx = 1;
			panel.add(passwordField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			panel.add(adminCheckBox, gbc);

			gbc.gridx = 0;
			gbc.gridy = 4;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			panel.add(guardarButton, gbc);

			// acción del botón guardar
			guardarButton.addActionListener(e -> {
				try {
					// valida campos
					String nombre = nombreField.getText().trim();
					String email = emailField.getText().trim();
					String password = new String(passwordField.getPassword());
					boolean esAdmin = adminCheckBox.isSelected();

					if (nombre.isEmpty() || email.isEmpty()) {
						JOptionPane.showMessageDialog(dialog, "Nombre y email son obligatorios", "Error de validación",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					// verifica si el nombre ya existe (si se cambió)
					if (!nombre.equals(usuario.getNombre())) {
						Usuario usuarioExistente = usuarioController.obtenerUsuarioPorNombre(nombre);
						if (usuarioExistente != null) {
							JOptionPane.showMessageDialog(dialog, "Ya existe un usuario con ese nombre",
									"Error de validación", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}

					// actualiza usuario
					usuario.setNombre(nombre);
					usuario.setEmail(email);
					if (!password.isEmpty()) {
						usuario.setPassword(password);
					}
					usuario.setEsAdmin(esAdmin);

					usuarioController.actualizarUsuario(usuario);

					dialog.dispose(); // cierra el diálogo
					cargarUsuarios(); // recarga la lista de usuarios

					JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente", "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(dialog, "Error al actualizar usuario: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});

			// configura y muestra el diálogo
			dialog.setContentPane(panel);
			dialog.pack();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al cargar usuario: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void eliminarUsuario() {
		int selectedRow = usuariosTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para eliminar", "Selección requerida",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		int usuarioId = (int) tableModel.getValueAt(selectedRow, 0);
		String nombreUsuario = (String) tableModel.getValueAt(selectedRow, 1);

		// confirma eliminación
		int option = JOptionPane.showConfirmDialog(this,
				"¿Está seguro de que desea eliminar al usuario '" + nombreUsuario + "'?", "Confirmar eliminación",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
			try {
				usuarioController.eliminarUsuario(usuarioId);
				cargarUsuarios(); // Recargar la lista de usuarios

				JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente", "Éxito",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Error al eliminar usuario: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void onBackButtonPressed() {
		// vuelve a la pantalla de administración
		AdminFrame frame = new AdminFrame();
		dispose();
		frame.setVisible(true);
	}
}