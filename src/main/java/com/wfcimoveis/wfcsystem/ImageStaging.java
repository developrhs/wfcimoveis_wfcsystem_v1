package com.wfcimoveis.wfcsystem;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.List;

/** Estagia imagens localmente e só remove o arquivo após confirmação do FTP. */
final class ImageStaging {
  private final AppDatabase db;
  private final Path root;

  ImageStaging(AppDatabase db) throws IOException {
    this.db = db;
    root = Path.of(System.getProperty("java.io.tmpdir"), "wfcsystem-images");
    Files.createDirectories(root);
  }

  Path root() { return root; }
  List<AppDatabase.ImageRow> pending() throws Exception { return db.pendingImages(); }

  void chooseAndStage(Component parent, String category, String entityId) throws Exception {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(true);
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
        "Imagens (JPG, JPEG, PNG, WEBP, GIF)", "jpg", "jpeg", "png", "webp", "gif"));
    if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;
    for (java.io.File selected : chooser.getSelectedFiles()) stage(category, entityId, selected.toPath());
  }

  private void stage(String category, String entityId, Path source) throws Exception {
    String name = source.getFileName().toString();
    String lower = name.toLowerCase(java.util.Locale.ROOT);
    if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif"))) {
      throw new IllegalArgumentException("Formato de imagem não permitido: " + name);
    }
    String safeCategory = category.replaceAll("[^a-zA-Z0-9_-]", "_");
    String safeEntity = entityId.replaceAll("[^a-zA-Z0-9_-]", "_");
    Path folder = root.resolve(safeCategory).resolve(safeEntity);
    Files.createDirectories(folder);
    Path target = uniqueTarget(folder, name);
    Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
    db.enqueueImage(category, entityId, name, target.toString());
  }

  private Path uniqueTarget(Path folder, String name) {
    Path candidate = folder.resolve(name);
    if (!Files.exists(candidate)) return candidate;
    String base = name;
    String extension = "";
    int dot = name.lastIndexOf('.');
    if (dot > 0) { base = name.substring(0, dot); extension = name.substring(dot); }
    int count = 2;
    do { candidate = folder.resolve(base + "-" + count++ + extension); } while (Files.exists(candidate));
    return candidate;
  }

  void uploadPending(String host, int port, String username, String password,
                     String propertiesPath, String testimonialsPath) throws Exception {
    List<AppDatabase.ImageRow> rows = db.pendingImages();
    if (rows.isEmpty()) return;
    if (host == null || host.isBlank() || username == null || username.isBlank()) {
      throw new IllegalStateException("Configure host, usuário e senha FTP no cofre local.");
    }
    FTPClient ftp = new FTPClient();
    try {
      ftp.setConnectTimeout(15000);
      ftp.setDataTimeout(30000);
      ftp.connect(host, port);
      if (!ftp.login(username, password == null ? "" : password)) throw new IOException("Login FTP recusado");
      ftp.enterLocalPassiveMode();
      ftp.setFileType(FTP.BINARY_FILE_TYPE);
      for (AppDatabase.ImageRow row : rows) {
        Path file = Path.of(row.stagedPath());
        if (!Files.isRegularFile(file)) { db.markImageFailed(row.id(), "Arquivo temporário não encontrado"); continue; }
        String directory = "prova_social".equalsIgnoreCase(row.category()) ? testimonialsPath : propertiesPath;
        String remoteName = sanitizeRemoteName(row.originalName());
        String remote = directory.replaceAll("/+$", "") + "/" + row.entityId() + "-" + row.id() + "-" + remoteName;
        try (var input = Files.newInputStream(file)) {
          if (!ftp.storeFile(remote, input)) throw new IOException("FTP recusou o arquivo");
          db.markImageUploaded(row.id());
          Files.deleteIfExists(file);
        } catch (Exception error) {
          db.markImageFailed(row.id(), error.getMessage() == null ? "Falha no upload" : error.getMessage());
        }
      }
      if (!ftp.logout()) throw new IOException("Não foi possível encerrar a sessão FTP");
    } finally {
      if (ftp.isConnected()) try { ftp.disconnect(); } catch (IOException ignored) { }
    }
  }

  private String sanitizeRemoteName(String value) {
    return value.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
