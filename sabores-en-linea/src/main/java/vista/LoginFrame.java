package vista;

import modelo.SessionManager;
import modelo.Usuario;

import org.hibernate.Session;
import org.hibernate.query.Query;
import util.EstiloManager;
import util.HibernateUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

// Pantalla inicio sesión

public class LoginFrame extends BaseFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginFrame() {
        super("Sabores en Línea - Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // no ver "volver"
        setupComponents();
    }

    @Override
    protected void setupComponents() {
        // oculta el botón de volver (pantalla de inicio)
        backButton.setVisible(false);

        // panel de contenido semitransparente
        JPanel contentPanel = createContentPanel();
        contentPanel.setLayout(new GridBagLayout());

        // configuración de GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // crea componentes
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userField = new JTextField(15);

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passField = new JPasswordField(15);

        loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(EstiloManager.COLOR_BOTON);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        // añade componentes al panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(loginButton, gbc);

        // añade panel de contenido al panel principal
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // configura acción del botón de login
        loginButton.addActionListener(e -> handleLogin());
    }

    // inicio sesion
    private void handleLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Usuario WHERE nombre = :username AND password = :password";
            Query<Usuario> query = session.createQuery(hql, Usuario.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            Usuario usuario = query.uniqueResult();

            if (usuario != null) {
                // guarda el usuario en SessionManager
                SessionManager.setUsuarioActual(usuario);
                // redirige según el rol
                if (usuario.esAdmin()) {
                    new AdminFrame().setVisible(true);
                } else {
                    new UserFrame().setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error de inicio de sesión",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + ex.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    @Override
    protected void onBackButtonPressed() {
        // no es necesario implementar ya que el botón está oculto
    }
}
