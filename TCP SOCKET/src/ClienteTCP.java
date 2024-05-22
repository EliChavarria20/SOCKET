
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ClienteTCP extends JFrame {
    private JTextArea logTextArea;
    private JTextField mensajeField;
    private JButton enviarButton;
    private JButton salirButton;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    public ClienteTCP() {
        setTitle("Cliente TCP");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Componentes de la interfaz
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mensajeField = new JTextField(20);
        enviarButton = new JButton("Enviar Mensaje");
        salirButton = new JButton("Salir");

        // Panel para el campo de mensaje y botones
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Mensaje:"));
        inputPanel.add(mensajeField);
        panel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 5, 5));
        buttonPanel.add(enviarButton);
        buttonPanel.add(salirButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Conectar al servidor
        conectarAlServidor();

        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarMensaje();
            }
        });

        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir();
            }
        });
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 8888);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            logTextArea.append("Conectado al servidor.\n");

            // Hilo para recibir mensajes del servidor
            Thread recibirThread = new Thread(new RecibirMensajes());
            recibirThread.start();
        } catch (IOException e) {
            logTextArea.append("Error al conectar con el servidor: " + e.getMessage() + "\n");
        }
    }

    private class RecibirMensajes implements Runnable {
        public void run() {
            try {
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    logTextArea.append("Mensaje recibido: " + mensaje + "\n");
                }
            } catch (IOException e) {
                logTextArea.append("Error al recibir mensaje: " + e.getMessage() + "\n");
            } finally {
                logTextArea.append("Desconectado del servidor.\n");
            }
        }
    }

    private void enviarMensaje() {
        String mensaje = mensajeField.getText();
        if (salida != null) {
            salida.println(mensaje);
            logTextArea.append("Mensaje enviado: " + mensaje + "\n");
            mensajeField.setText("");
        } else {
            logTextArea.append("No conectado al servidor.\n");
        }
    }

    private void salir() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            logTextArea.append("Error al cerrar el cliente: " + e.getMessage() + "\n");
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ClienteTCP cliente = new ClienteTCP();
                cliente.setVisible(true);
            }
        });
    }
}
