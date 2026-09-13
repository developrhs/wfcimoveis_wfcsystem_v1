package com.wfcimoveis.wfcsystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.UUID;

/** Interface operacional offline-first para cadastros, usuários e fila de imagens. */
final class ManagementPanel extends JPanel {
  private static final Color WINE = new Color(101, 31, 37);
  private static final Color SAND = new Color(248, 245, 239);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final AppDatabase db;
  private final ImageStaging images;
  private final JTabbedPane tabs = new JTabbedPane();

  ManagementPanel(AppDatabase db, ImageStaging images) {
    this.db = db;
    this.images = images;
    setLayout(new BorderLayout());
    setBackground(SAND);
    setBorder(new EmptyBorder(10, 10, 10, 10));
    add(header(), BorderLayout.NORTH);
    add(tabs, BorderLayout.CENTER);
    rebuildTabs();
  }

  private JComponent header() {
    JPanel panel = new JPanel(new BorderLayout(12, 4));
    panel.setOpaque(false);
    JLabel title = new JLabel("Operação WFCSystem");
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    title.setForeground(WINE);
    JLabel subtitle = new JLabel("Cadastros locais preservados e prontos para sincronização");
    subtitle.setForeground(new Color(90, 80, 75));
    JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
    copy.add(title); copy.add(subtitle);
    JButton refresh = new JButton("Atualizar módulos");
    refresh.addActionListener(e -> rebuildTabs());
    panel.add(copy, BorderLayout.WEST); panel.add(refresh, BorderLayout.EAST);
    panel.setBorder(new EmptyBorder(2, 2, 12, 2));
    return panel;
  }

  private void rebuildTabs() {
    tabs.removeAll();
    tabs.addTab("Imóveis", recordTab("imovel", new String[]{"Título", "Localização", "Preço", "Status"}, true));
    tabs.addTab("Clientes", recordTab("cliente", new String[]{"Nome", "Telefone", "E-mail", "CPF"}, false));
    tabs.addTab("Agentes", recordTab("agente", new String[]{"Nome", "E-mail", "Telefone", "CRECI"}, false));
    tabs.addTab("Vendas", recordTab("venda", new String[]{"Imóvel", "Cliente", "Valor", "Data"}, false));
    tabs.addTab("Usuários", usersTab());
    tabs.revalidate(); tabs.repaint();
  }

  private JPanel recordTab(String type, String[] fields, boolean withImages) {
    JPanel panel = new JPanel(new BorderLayout(8, 8)); panel.setBorder(new EmptyBorder(8, 2, 2, 2)); panel.setBackground(SAND);
    DefaultTableModel model = new DefaultTableModel(new Object[]{"ID"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    for (String field : fields) model.addColumn(field);
    JTable table = new JTable(model); table.setRowHeight(28); table.setAutoCreateRowSorter(true);
    loadRecords(type, fields, model);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
    JButton add = new JButton("Novo cadastro");
    JButton edit = new JButton("Editar selecionado");
    JButton delete = new JButton("Excluir localmente");
    JButton media = new JButton("Adicionar imagens");
    add.addActionListener(e -> editRecord(type, fields, null, model));
    edit.addActionListener(e -> { int row = table.getSelectedRow(); if (row >= 0) editRecord(type, fields, model.getValueAt(table.convertRowIndexToModel(row), 0).toString(), model); else info("Selecione um registro para editar."); });
    delete.addActionListener(e -> { int row = table.getSelectedRow(); if (row < 0) { info("Selecione um registro para excluir."); return; } int answer = JOptionPane.showConfirmDialog(this, "Excluir este registro localmente? Ele será enviado como DELETE na próxima sincronização.", "Confirmar exclusão", JOptionPane.OK_CANCEL_OPTION); if (answer == JOptionPane.OK_OPTION) try { db.deleteRecord(type, model.getValueAt(table.convertRowIndexToModel(row), 0).toString()); loadRecords(type, fields, model); } catch (Exception ex) { error(ex); } });
    media.addActionListener(e -> { int row = table.getSelectedRow(); if (row < 0) { info("Selecione um imóvel ou registro antes de adicionar imagens."); return; } try { String id = model.getValueAt(table.convertRowIndexToModel(row), 0).toString(); images.chooseAndStage(this, "imovel".equals(type) ? "imoveis" : "prova_social", id); info("Imagem(ns) adicionada(s) à fila local."); } catch (Exception ex) { error(ex); } });
    actions.add(add); actions.add(edit); actions.add(delete); if (withImages) actions.add(media);
    panel.add(actions, BorderLayout.NORTH); panel.add(new JScrollPane(table), BorderLayout.CENTER);
    return panel;
  }

  private void loadRecords(String type, String[] fields, DefaultTableModel model) {
    model.setRowCount(0);
    try { for (AppDatabase.RecordRow row : db.records(type)) { JsonObject o; try { o = JsonParser.parseString(row.payload()).getAsJsonObject(); } catch (Exception ignored) { o = new JsonObject(); } Object[] values = new Object[fields.length + 1]; values[0] = row.id(); for (int i = 0; i < fields.length; i++) values[i + 1] = first(o, fieldKey(fields[i])); model.addRow(values); } }
    catch (Exception ex) { error(ex); }
  }

  private void editRecord(String type, String[] fields, String existingId, DefaultTableModel model) {
    JTextField[] inputs = new JTextField[fields.length]; JPanel form = new JPanel(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(4, 4, 4, 4); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
    JsonObject current = new JsonObject();
    if (existingId != null) try { for (AppDatabase.RecordRow row : db.records(type)) if (row.id().equals(existingId)) current = JsonParser.parseString(row.payload()).getAsJsonObject(); } catch (Exception ex) { error(ex); return; }
    for (int i = 0; i < fields.length; i++) { c.gridx = 0; c.gridy = i; c.weightx = 0; form.add(new JLabel(fields[i] + ":"), c); inputs[i] = new JTextField(first(current, fieldKey(fields[i])), 28); c.gridx = 1; c.weightx = 1; form.add(inputs[i], c); }
    int answer = JOptionPane.showConfirmDialog(this, form, existingId == null ? "Novo cadastro" : "Editar cadastro", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); if (answer != JOptionPane.OK_OPTION) return;
    String id = existingId == null ? UUID.randomUUID().toString().substring(0, 8) : existingId; JsonObject payload = new JsonObject(); for (int i = 0; i < fields.length; i++) payload.addProperty(fieldKey(fields[i]), inputs[i].getText().trim());
    try { db.upsert(type, id, GSON.toJson(payload)); loadRecords(type, fields, model); } catch (Exception ex) { error(ex); }
  }

  private JPanel usersTab() {
    JPanel panel = new JPanel(new BorderLayout(8, 8)); panel.setBorder(new EmptyBorder(8, 2, 2, 2)); panel.setBackground(SAND);
    DefaultTableModel model = new DefaultTableModel(new Object[]{"Username", "Nome", "Perfil", "Ativo", "E-mail", "CRECI"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    JTable table = new JTable(model); table.setRowHeight(28); loadUsers(model);
    JButton edit = new JButton("Alterar perfil/status"); edit.addActionListener(e -> { int row = table.getSelectedRow(); if (row < 0) { info("Selecione um usuário."); return; } String username = model.getValueAt(table.convertRowIndexToModel(row), 0).toString(); String profile = model.getValueAt(table.convertRowIndexToModel(row), 2).toString(); boolean active = Boolean.parseBoolean(model.getValueAt(table.convertRowIndexToModel(row), 3).toString()); JComboBox<String> profiles = new JComboBox<>(new String[]{"Administrador", "Corretor", "Atendimento"}); profiles.setSelectedItem(profile); JCheckBox enabled = new JCheckBox("Conta ativa", active); JPanel form = new JPanel(new GridLayout(0, 1)); form.add(new JLabel(username)); form.add(profiles); form.add(enabled); if (JOptionPane.showConfirmDialog(this, form, "Atualizar usuário local", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) try { db.updateUser(username, profiles.getSelectedItem().toString(), enabled.isSelected()); loadUsers(model); } catch (Exception ex) { error(ex); } });
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(edit); panel.add(actions, BorderLayout.NORTH); panel.add(new JScrollPane(table), BorderLayout.CENTER); return panel;
  }

  private void loadUsers(DefaultTableModel model) { model.setRowCount(0); try { for (AppDatabase.UserRow u : db.users()) model.addRow(new Object[]{u.username(), u.fullName(), u.profile(), u.active(), u.email(), u.creci()}); } catch (Exception ex) { error(ex); } }
  private String first(JsonObject object, String key) { return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : ""; }
  private String fieldKey(String label) { return switch (label) { case "Título" -> "title"; case "Localização" -> "location"; case "Preço" -> "price"; case "Status" -> "status"; case "Nome" -> "name"; case "Telefone" -> "phone"; case "E-mail" -> "email"; case "CPF" -> "cpf"; case "CRECI" -> "creci"; case "Imóvel" -> "propertyId"; case "Cliente" -> "clientId"; case "Valor" -> "value"; case "Data" -> "date"; default -> label.toLowerCase(); }; }
  private void info(String message) { JOptionPane.showMessageDialog(this, message, "WFCSystem", JOptionPane.INFORMATION_MESSAGE); }
  private void error(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage() == null ? "Operação não concluída." : ex.getMessage(), "WFCSystem", JOptionPane.ERROR_MESSAGE); }
}
