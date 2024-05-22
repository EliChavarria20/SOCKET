import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

// CLASE CLIENTE 
public class ClienteTCP extends JFrame {
    private JTextArea logTextArea;
    private JTextField mensajeField;
    private JButton enviarButton;
    private JButton salirButton;
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;

    // Constructor
    public ClienteTCP() {
        setTitle("Cliente TCP");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mensajeField = new JTextField(20);
        enviarButton = new JButton("Enviar Mensaje");
        salirButton = new JButton("Salir");

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

    // Método para conectarse al servidor
    private void conectarAlServidor() {
        try {
            
            socket = new Socket("179.50.138.247", 6666); // IP PÚBLICA Y PUERTO 6666 
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            logTextArea.append("Conectado al servidor.\n");

            Thread recibirThread = new Thread(new RecibirMensajes());
            recibirThread.start();
        } catch (UnknownHostException e) {
            logTextArea.append("No se pudo encontrar el host: " + e.getMessage() + "\n");
        } catch (IOException e) {
            logTextArea.append("Error de entrada/salida: " + e.getMessage() + "\n");
        }
    }

    // Método para recibir mensajes del servidor
    private class RecibirMensajes implements Runnable {
        public void run() {
            try {
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    logTextArea.append("Mensaje del servidor: " + mensaje + "\n");
                }
            } catch (IOException e) {
                logTextArea.append("Error al recibir mensaje: " + e.getMessage() + "\n");
            } finally {
                logTextArea.append("Conexión con el servidor perdida.\n");
                salir(); // Cerrar la conexión cuando se pierda la conexión con el servidor
            }
        }
    }

    // Método para enviar mensajes al servidor
    private void enviarMensaje() {
        String mensaje = mensajeField.getText();
        salida.println(mensaje);
        logTextArea.append("Mensaje enviado: " + mensaje + "\n");
        mensajeField.setText("");
    }

    // Método para cerrar la conexión con el servidor
    private void salir() {
        try {
            if (socket != null && !socket.isClosed()) {
                salida.println("disconnect");
                salida.close();
                entrada.close();
                socket.close();
            }
        } catch (IOException e) {
            logTextArea.append("Error al cerrar la conexión: " + e.getMessage() + "\n");
        }
        System.exit(0);
    }

    // Método main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ClienteTCP cliente = new ClienteTCP();
                cliente.setVisible(true);
            }
        });
    }
}
