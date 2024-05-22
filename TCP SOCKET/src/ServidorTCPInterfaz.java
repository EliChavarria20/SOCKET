
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ServidorTCPInterfaz extends JFrame {
    private JTextArea logTextArea;
    private JTextField mensajeField;
    private JButton enviarButton;
    private JButton salirButton;
    private ServerSocket serverSocket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private Socket clienteSocket;

    public ServidorTCPInterfaz() {
        setTitle("Servidor TCP");
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

        // Iniciar el servidor en un hilo aparte
        Thread serverThread = new Thread(new ServerThread());
        serverThread.start();

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

    private class ServerThread implements Runnable {
        public void run() {
            try {
                // Establecer el puerto para escuchar
                int puerto = 8888;
                serverSocket = new ServerSocket(puerto);
                logTextArea.append("Servidor TCP iniciado...\n");

                // Bucle para escuchar las conexiones entrantes
                while (true) {
                    logTextArea.append("Esperando conexión...\n");
                    clienteSocket = serverSocket.accept();
                    logTextArea.append("Cliente conectado...\n");

                    // Inicializar streams de entrada y salida
                    entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                    salida = new PrintWriter(clienteSocket.getOutputStream(), true);

                    // Hilo para recibir mensajes del cliente
                    Thread recibirThread = new Thread(new RecibirMensajes());
                    recibirThread.start();
                }
            } catch (IOException e) {
                logTextArea.append("Error: " + e.getMessage() + "\n");
            }
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
                logTextArea.append("Cliente desconectado.\n");
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
            logTextArea.append("No hay cliente conectado.\n");
        }
    }

    private void salir() {
        try {
            if (clienteSocket != null) {
                clienteSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logTextArea.append("Error al cerrar el servidor: " + e.getMessage() + "\n");
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ServidorTCPInterfaz servidor = new ServidorTCPInterfaz();
                servidor.setVisible(true);
            }
        });
    }
}
