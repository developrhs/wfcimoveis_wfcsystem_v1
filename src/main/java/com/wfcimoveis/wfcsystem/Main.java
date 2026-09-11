package com.wfcimoveis.wfcsystem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

public final class Main {
    private static final Properties CONFIG = new Properties();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { loadConfig(); showLogin(); });
    }

    private static void loadConfig() {
        Path file = Path.of("config.properties");
        try (InputStream in = Files.exists(file) ? Files.newInputStream(file) : Main.class.getResourceAsStream("/config.properties.example")) {
            if (in != null) CONFIG.load(in);
        } catch (IOException ignored) { }
    }

    private static void showLogin() {
        frame = new JFrame(value("app.name", "WFCSystem v1") + " — Login");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(430, 310); frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(10, 10)); root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel title = new JLabel("Acesso ao sistema", SwingConstants.CENTER); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20)); root.add(title, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(5, 1, 6, 6));
        JTextField username = new JTextField(); JPasswordField password = new JPasswordField();
        form.add(new JLabel("Usuário ou e-mail")); form.add(username); form.add(new JLabel("Senha")); form.add(password);
        JButton login = new JButton("Entrar"); form.add(login); root.add(form, BorderLayout.CENTER);
        JLabel support = new JLabel("Suporte: " + value("support.name", "Suporte WFC Imóveis"), SwingConstants.CENTER); root.add(support, BorderLayout.SOUTH);
        login.addActionListener(e -> login(username.getText().trim(), new String(password.getPassword())));
        password.addActionListener(e -> login.doClick()); frame.setContentPane(root); frame.setVisible(true); username.requestFocusInWindow();
    }

    private static void login(String username, String password) {
        if (username.isBlank() || password.isBlank()) { message("Informe usuário e senha.", JOptionPane.WARNING_MESSAGE); return; }
        String base = value("api.baseUrl", "").replaceAll("/$", "");
        if (base.isBlank() || base.contains("(") || base.contains("SEU")) { message("Configure api.baseUrl no arquivo config.properties antes de entrar.", JOptionPane.WARNING_MESSAGE); return; }
        try {
            String json = "{\"username\":\"" + jsonEscape(username) + "\",\"password\":\"" + jsonEscape(password) + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/auth/login")).timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body().contains("token")) { showHome(username, response.body()); }
            else message("Login não autorizado ou API ainda não configurada. HTTP " + response.statusCode(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { message("Não foi possível conectar à API: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE); }
    }

    private static void showHome(String username, String response) {
        frame.getContentPane().removeAll(); frame.setTitle(value("app.name", "WFCSystem v1"));
        JMenuBar menu = new JMenuBar(); for (String item : new String[]{"Início", "Imóveis", "Clientes", "Agentes", "Prova social", "Vendas", "Usuários", "Configurações"}) menu.add(new JMenu(item));
        JMenu sair = new JMenu("Sair"); sair.addMenuListener(new javax.swing.event.MenuListener() { public void menuSelected(javax.swing.event.MenuEvent e) { frame.dispose(); showLogin(); } public void menuDeselected(javax.swing.event.MenuEvent e) {} public void menuCanceled(javax.swing.event.MenuEvent e) {} }); menu.add(sair); frame.setJMenuBar(menu);
        JPanel panel = new JPanel(new BorderLayout(12, 12)); panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel welcome = new JLabel("Login realizado: " + username); welcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18)); panel.add(welcome, BorderLayout.NORTH);
        JTextArea info = new JTextArea("WFCSystem v1\n\nEste é o primeiro corpo do aplicativo.\nUse o menu superior para os próximos módulos.\n\nSUPORTE\n" + value("support.name", "Suporte WFC Imóveis") + "\n" + value("support.email", "(definir e-mail)") + "\n" + value("support.phone", "(definir telefone)")); info.setEditable(false); info.setOpaque(false); info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15)); panel.add(info, BorderLayout.CENTER);
        frame.setContentPane(panel); frame.setSize(780, 480); frame.setLocationRelativeTo(null); frame.revalidate(); frame.repaint();
    }

    private static String value(String key, String fallback) { return CONFIG.getProperty(key, fallback); }
    private static String jsonEscape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
    private static void message(String text, int type) { JOptionPane.showMessageDialog(frame, text, value("app.name", "WFCSystem v1"), type); }
}
