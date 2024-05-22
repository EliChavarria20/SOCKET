import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorTCPInterfaz extends JFrame {
    private JTextArea logTextArea;
    private JTextField mensajeField;
    private JButton enviarButton;
    private JButton salirButton;
    private ServerSocket serverSocket;
    private List<PrintWriter> clientesConectados = new ArrayList<>();
    private String usuario;

    public ServidorTCPInterfaz() {
        setTitle("Servidor TCP");
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

        // Autenticación de usuarios
        autenticarUsuario();

        Thread serverThread = new Thread(new ServerThread());
        serverThread.start();

        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensaje = mensajeField.getText().trim();
                if (!mensaje.isEmpty()) { // Validación básica del mensaje
                    enviarMensajeATodos("Servidor: " + mensaje);
                    mensajeField.setText("");
                } else {
                    JOptionPane.showMessageDialog(ServidorTCPInterfaz.this, "El mensaje está vacío.");
                }
            }
        });

        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salir();
            }
        });
    }

    private void autenticarUsuario() {
        usuario = JOptionPane.showInputDialog("Ingrese su nombre de usuario:");
        logTextArea.append("Usuario autenticado: " + usuario + "\n");
    }

    private class ServerThread implements Runnable {
        public void run() {
            try {
                int puerto = 6666;
                serverSocket = new ServerSocket(puerto);
                logTextArea.append("Servidor TCP iniciado en el puerto " + puerto + "...\n");

                while (true) {
                    logTextArea.append("Esperando conexión...\n");
                    Socket clienteSocket = serverSocket.accept();
                    logTextArea.append("Cliente conectado desde " + clienteSocket.getInetAddress().getHostAddress() + "...\n");

                    PrintWriter salida = new PrintWriter(clienteSocket.getOutputStream(), true);
                    clientesConectados.add(salida);

                    Thread recibirThread = new Thread(new RecibirMensajes(clienteSocket, salida));
                    recibirThread.start();
                }
            } catch (IOException e) {
                logTextArea.append("Error al iniciar el servidor: " + e.getMessage() + "\n");
            }
        }
    }

    private class RecibirMensajes implements Runnable {
        private Socket socket;
        private PrintWriter salida;

        public RecibirMensajes(Socket socket, PrintWriter salida) {
            this.socket = socket;
            this.salida = salida;
        }

        @Override
        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    logTextArea.append("Mensaje recibido de " + socket.getInetAddress().getHostAddress() + ": " + mensaje + "\n");
                    if (mensaje.equalsIgnoreCase("disconnect")) {
                        logTextArea.append("Cliente " + socket.getInetAddress().getHostAddress() + " se desconectó.\n");
                        socket.close();
                        clientesConectados.remove(salida);
                        break;
                    }
                }
            } catch (IOException e) {
                logTextArea.append("Error al recibir mensaje: " + e.getMessage() + "\n");
            } finally {
                logTextArea.append("Cliente desconectado.\n");
            }
        }
    }

    private void enviarMensajeATodos(String mensaje) {
        for (PrintWriter cliente : clientesConectados) {
            cliente.println(mensaje);
        }
    }

    private void salir() {
        try {
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
