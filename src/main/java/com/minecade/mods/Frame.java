package com.minecade.mods;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Frame extends JFrame implements ActionListener {

    private static Frame instance;
    JPanel buttons;
    Font font;
    final BufferedImage finalMyImage;

    public Frame() {
        super("OlimpoCraft Mod Instalador");

        this.setSize(350, 240);
        this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            this.setIconImage(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage myImage = null;
        try {
            myImage = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel panel = new JLabel();
        panel.setIcon(new ImageIcon(myImage));
        panel.setLayout(new BorderLayout());

        final JLabel stage = new JLabel(String.format("Etapa: %s | MC Versión: %s | OS: %s | Creador: %s", ModInstaller.stage, ModInstaller.version, ModInstaller.getOS(), "PaulBGD.me"), SwingConstants.CENTER);
        stage.setForeground(new Color(54, 54, 54));
        stage.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI("http://paulbgd.me/modinstaller"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JLabel title = new JLabel("", SwingConstants.CENTER);
        try {
            title.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("minecade-logo.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        title.setLayout(new BorderLayout());
        title.setBorder(new LineBorder(new Color(0, 255, 0, 0), 10));

        buttons = new JPanel();
        JButton install = new JButton("Instalar Mod");
        install.addActionListener(this);
        font = new Font(install.getFont().getName(), 0, 20);
        install.setFont(font);
        JButton close = new JButton("Cerrar");
        close.addActionListener(this);
        close.setFont(font);
        buttons.setBackground(new Color(0, 255, 0, 0));

        final JComboBox<String> options = new JComboBox<String>(new String[]{"OlimpoCraft Mod", "OlimpoCraft Mod + Optifine", "OlimpoCraft + Optifine + Camstudio"});
        finalMyImage = myImage;
        options.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((JLabel) Frame.this.getContentPane()).setIcon(new ImageIcon(finalMyImage));
                ModInstaller.installation = options.getItemAt(options.getSelectedIndex());
            }
        });

        buttons.add(options, BorderLayout.NORTH);
        buttons.add(install, BorderLayout.WEST);
        buttons.add(close, BorderLayout.EAST);
        buttons.add(stage, BorderLayout.SOUTH);

        panel.add(title, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
        panel.setBorder(new LineBorder(Color.DARK_GRAY, 10));
        this.setContentPane(panel);
        instance = this;
    }

    public static Frame getInstance() {
        return instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("Cerrar")) {
            System.exit(0);
        } else if (s.equals("Instalar Mod")) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        ModInstaller.install();
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(Frame.getInstance(), "IOException!", "Excepción", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                }
            }.start();
            update();
        }
    }

    public void update() {
        buttons.removeAll();
        JLabel installing = new JLabel();
        try {
            installing.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("installing.png"))));
        } catch (IOException e1) {
            e1.printStackTrace();
            installing.setText("Installing...");
        }
        buttons.add(installing);
        this.pack();
        ((JLabel) Frame.this.getContentPane()).setIcon(new ImageIcon(finalMyImage));
}

    public void download() {
        buttons.removeAll();
        JLabel installing = new JLabel();
        try {
            installing.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("downloading.png"))));
        } catch (IOException e1) {
            e1.printStackTrace();
            installing.setText("Downloading...");
        }
        buttons.add(installing);
        this.pack();
        ((JLabel) Frame.this.getContentPane()).setIcon(new ImageIcon(finalMyImage));
    }
}
