package util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;

// Clase de utilidad para manejar los estilos de la interfaz gráfica

public class EstiloManager {
	// colores corporativos
	public static final Color COLOR_PRINCIPAL = new Color(153, 101, 21); // marron dorado
	public static final Color COLOR_SECUNDARIO = new Color(139, 69, 19); // marron oscuro
	public static final Color COLOR_FONDO = new Color(255, 250, 240); // blanco hueso
	public static final Color COLOR_TEXTO = new Color(70, 40, 10); // marron texto
	public static final Color COLOR_BOTON = new Color(205, 133, 63); // marron claro para botones
	public static final Color COLOR_TABLA_HEADER = new Color(222, 184, 135); // beige para headers

	// estilos de fuente
	public static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 24);
	public static final Font FUENTE_BOTON = new Font("Arial", Font.BOLD, 14);
	public static final Font FUENTE_TABLA = new Font("Arial", Font.PLAIN, 12);

	// aplicacion del estilo a ventana

	public static void aplicarEstiloVentana(JFrame ventana, String titulo) {
		ventana.setTitle(titulo);
		ventana.getContentPane().setBackground(COLOR_FONDO);

		JPanel header = new JPanel();
		header.setBackground(COLOR_PRINCIPAL);
		header.setPreferredSize(new Dimension(ventana.getWidth(), 60));

		JLabel titleLabel = new JLabel(titulo);
		titleLabel.setFont(FUENTE_TITULO);
		titleLabel.setForeground(Color.WHITE);
		header.add(titleLabel);

		ventana.add(header, BorderLayout.NORTH);

		JPanel footer = new JPanel();
		footer.setBackground(COLOR_PRINCIPAL);
		footer.setPreferredSize(new Dimension(ventana.getWidth(), 30));

		JLabel copyrightLabel = new JLabel("© 2025 Sabores en Línea");
		copyrightLabel.setForeground(Color.WHITE);
		footer.add(copyrightLabel);

		ventana.add(footer, BorderLayout.SOUTH);
	}

	// aplicacion del estilo a boton

	public static void aplicarEstiloBoton(JButton boton) {
		boton.setBackground(COLOR_BOTON);
		boton.setForeground(Color.WHITE);
		boton.setFont(FUENTE_BOTON);
		boton.setFocusPainted(false);
		boton.setBorderPainted(false);
		boton.setPreferredSize(new Dimension(200, 40));

		boton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				boton.setBackground(COLOR_BOTON.darker());
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				boton.setBackground(COLOR_BOTON);
			}
		});
	}

	// aplicacion del estilo a tabla

	public static void aplicarEstiloTabla(JTable tabla) {
		tabla.setFont(FUENTE_TABLA);
		tabla.setRowHeight(25);
		tabla.setShowGrid(true);
		tabla.setGridColor(COLOR_TABLA_HEADER.darker());

		JTableHeader header = tabla.getTableHeader();
		header.setBackground(COLOR_TABLA_HEADER);
		header.setForeground(COLOR_TEXTO);
		header.setFont(FUENTE_TABLA.deriveFont(Font.BOLD));

		tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (isSelected) {
					c.setBackground(COLOR_BOTON.brighter());
					c.setForeground(Color.WHITE);
				} else {
					c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
					c.setForeground(COLOR_TEXTO);
				}

				return c;
			}
		});
	}

	// aplicacion del estilo a etiqueta

	public static void aplicarEstiloLabel(JLabel label) {
		label.setFont(FUENTE_BOTON);
		label.setForeground(COLOR_TEXTO);
	}

	// creacion de panel de botones con el estilo corporativo

	public static JPanel crearPanelBotones(JButton... botones) {
		JPanel panel = new JPanel();
		panel.setBackground(COLOR_FONDO);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		for (JButton boton : botones) {
			aplicarEstiloBoton(boton);
			panel.add(boton);
		}

		return panel;
	}
	// NUEVO: Aplica color corporativo a las barras de desplazamiento
		public static void aplicarColorBarraDesplazamiento(JScrollPane scrollPane) {
			// Personalizar solo el color de la barra de desplazamiento
			scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
				@Override
				protected void configureScrollBarColors() {
					// Color del "thumb" (la parte móvil de la barra)
					this.thumbColor = COLOR_BOTON;
					
					// Mantener el resto de colores por defecto
					this.trackColor = UIManager.getColor("ScrollBar.track");
					this.trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
				}
			});
			
			scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
				@Override
				protected void configureScrollBarColors() {
					// Color del "thumb" (la parte móvil de la barra)
					this.thumbColor = COLOR_BOTON;
					
					// Mantener el resto de colores por defecto
					this.trackColor = UIManager.getColor("ScrollBar.track");
					this.trackHighlightColor = UIManager.getColor("ScrollBar.trackHighlight");
				}
			});
		}
	}

	
