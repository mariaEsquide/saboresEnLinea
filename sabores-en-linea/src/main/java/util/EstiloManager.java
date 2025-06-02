package util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.io.Serializable;

// Clase de utilidad para manejar los estilos de la interfaz gráfica

public class EstiloManager implements Serializable {
    private static final long serialVersionUID = 1L;
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
	private static JButton crearBotonInvisible() {
	    JButton boton = new JButton();
	    boton.setPreferredSize(new Dimension(0, 0));
	    boton.setMinimumSize(new Dimension(0, 0));
	    boton.setMaximumSize(new Dimension(0, 0));
	    boton.setVisible(false);
	    return boton;
	}

	// aplicacion del estilo a la barra
	public static void aplicarColorBarraDesplazamiento(JScrollPane scrollPane) {
	    // Scrollbar vertical
	    scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
	        private final int THUMB_RADIUS = 8;

	        @Override
	        protected void configureScrollBarColors() {
	            // No usar thumbColor directamente porque pintamos custom
	            this.trackColor = COLOR_FONDO;
	        }

	        @Override
	        protected JButton createDecreaseButton(int orientation) {
	            return crearBotonInvisible();
	        }

	        @Override
	        protected JButton createIncreaseButton(int orientation) {
	            return crearBotonInvisible();
	        }

	        @Override
	        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
	            if (!c.isEnabled() || thumbBounds.width > c.getWidth() || thumbBounds.height > c.getHeight()) {
	                return;
	            }

	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	            // degradado marrón claro a marrón oscuro (color corporativo)
	            Color colorStart = new Color(205, 133, 63);  // marrón claro (COLOR_BOTON)
	            Color colorEnd = new Color(139, 69, 19);     // marrón oscuro (COLOR_SECUNDARIO)

	            GradientPaint gradient = new GradientPaint(
	                thumbBounds.x, thumbBounds.y, colorStart,
	                thumbBounds.x, thumbBounds.y + thumbBounds.height, colorEnd);

	            // relleno con degradado redondeado
	            g2.setPaint(gradient);
	            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, THUMB_RADIUS, THUMB_RADIUS);

	            // borde brillante arriba para efecto 3D
	            g2.setPaint(new GradientPaint(
	                thumbBounds.x, thumbBounds.y,
	                new Color(255, 230, 180, 180), // brillo dorado claro semi-transparente
	                thumbBounds.x, thumbBounds.y + thumbBounds.height / 2,
	                new Color(0, 0, 0, 0) // transparente
	            ));
	            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height / 2, THUMB_RADIUS, THUMB_RADIUS);

	            // sombra suave abajo para volumen
	            g2.setColor(new Color(0, 0, 0, 60));
	            g2.drawRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1, THUMB_RADIUS, THUMB_RADIUS);

	            g2.dispose();
	        }

	        @Override
	        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setColor(trackColor);
	            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
	            g2.dispose();
	        }

	        @Override
	        protected Dimension getMinimumThumbSize() {
	            return new Dimension(8, 30); // barra más estrecha y con alto mínimo
	        }
	    });

	    // Scrollbar horizontal (igual que vertical)
	    scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
	        private final int THUMB_RADIUS = 8;

	        @Override
	        protected void configureScrollBarColors() {
	            this.trackColor = COLOR_FONDO;
	        }

	        @Override
	        protected JButton createDecreaseButton(int orientation) {
	            return crearBotonInvisible();
	        }

	        @Override
	        protected JButton createIncreaseButton(int orientation) {
	            return crearBotonInvisible();
	        }

	        @Override
	        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
	            if (!c.isEnabled() || thumbBounds.width > c.getWidth() || thumbBounds.height > c.getHeight()) {
	                return;
	            }

	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	            Color colorStart = new Color(205, 133, 63);
	            Color colorEnd = new Color(139, 69, 19);

	            GradientPaint gradient = new GradientPaint(
	                thumbBounds.x, thumbBounds.y, colorStart,
	                thumbBounds.x, thumbBounds.y + thumbBounds.height, colorEnd);

	            g2.setPaint(gradient);
	            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, THUMB_RADIUS, THUMB_RADIUS);

	            g2.setPaint(new GradientPaint(
	                thumbBounds.x, thumbBounds.y,
	                new Color(255, 230, 180, 180),
	                thumbBounds.x, thumbBounds.y + thumbBounds.height / 2,
	                new Color(0, 0, 0, 0)
	            ));
	            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height / 2, THUMB_RADIUS, THUMB_RADIUS);

	            g2.setColor(new Color(0, 0, 0, 60));
	            g2.drawRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1, THUMB_RADIUS, THUMB_RADIUS);

	            g2.dispose();
	        }

	        @Override
	        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setColor(trackColor);
	            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
	            g2.dispose();
	        }

	        @Override
	        protected Dimension getMinimumThumbSize() {
	            return new Dimension(30, 8);
	        }
	    });

	    // ajuste para suavizar el scroll
	    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
	}
	
	// aplicacion del estilo al combo
	public static void aplicarEstiloComboBox(JComboBox<?> combo) {
	    combo.setFont(FUENTE_BOTON);
	    combo.setForeground(COLOR_TEXTO);
	    combo.setBackground(COLOR_FONDO);
	    combo.setOpaque(true);
	    combo.setFocusable(false);
	    combo.setBorder(BorderFactory.createLineBorder(COLOR_PRINCIPAL, 1)); // borde dorado

	    // Estilo del despliegue
	    combo.setRenderer(new DefaultListCellRenderer() {
	        
			private static final long serialVersionUID = 1L;

			@Override
	        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
	                boolean isSelected, boolean cellHasFocus) {
	            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	            c.setFont(FUENTE_BOTON);

	            if (isSelected) {
	                c.setBackground(COLOR_TABLA_HEADER); // beige para seleccionado
	                c.setForeground(COLOR_TEXTO);        // marrón
	            } else {
	                c.setBackground(COLOR_FONDO);        // blanco hueso
	                c.setForeground(COLOR_TEXTO);        // marrón
	            }

	            return c;
	        }
	    });

	    // Cambiar flecha del combo
	    combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
	        @Override
	        protected JButton createArrowButton() {
	            JButton button = new JButton();
	            button.setBorder(BorderFactory.createEmptyBorder());
	            button.setBackground(COLOR_FONDO);
	            button.setOpaque(true);
	            button.setContentAreaFilled(false);
	            button.setIcon(new javax.swing.plaf.metal.MetalComboBoxIcon() {
	              
					private static final long serialVersionUID = 1L;

					@Override
	                public void paintIcon(Component c, Graphics g, int x, int y) {
	                    g.setColor(COLOR_TEXTO); // flecha marrón
	                    int mid = getIconWidth() / 2;
	                    int height = getIconHeight() / 2;
	                    g.fillPolygon(new int[] {x, x + getIconWidth(), x + mid}, new int[] {y, y, y + height}, 3);
	                }
	            });
	            return button;
	        }
	    });
	}



}