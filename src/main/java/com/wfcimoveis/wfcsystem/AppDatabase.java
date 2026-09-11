package com.wfcimoveis.wfcsystem;

import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Arrays;
import java.security.MessageDigest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** SQLite local: nunca contém senhas nem credenciais de infraestrutura. */
final class AppDatabase implements AutoCloseable {
  private final Connection connection;
  private final Path path;

  AppDatabase() throws SQLException {
    try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException e) { throw new SQLException("Driver SQLite ausente", e); }
    try {
      Path dir = Path.of(System.getProperty("user.home"), ".wfcsystem");
      Files.createDirectories(dir);
      path = dir.resolve("wfcsystem.db");
      connection = DriverManager.getConnection("jdbc:sqlite:" + path);
      connection.setAutoCommit(true);
      try (Statement s = connection.createStatement()) {
        s.execute("PRAGMA foreign_keys=ON");
        s.execute("PRAGMA journal_mode=WAL");
        s.execute("CREATE TABLE IF NOT EXISTS app_meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
        s.execute("CREATE TABLE IF NOT EXISTS local_records (entity_type TEXT NOT NULL, entity_id TEXT NOT NULL, payload_json TEXT NOT NULL, version INTEGER NOT NULL DEFAULT 0, updated_at TEXT NOT NULL, deleted INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(entity_type, entity_id))");
        s.execute("CREATE TABLE IF NOT EXISTS sync_queue (id INTEGER PRIMARY KEY AUTOINCREMENT, entity_type TEXT NOT NULL, entity_id TEXT NOT NULL, operation TEXT NOT NULL, payload_json TEXT NOT NULL, base_version INTEGER NOT NULL DEFAULT 0, status TEXT NOT NULL DEFAULT 'PENDING', error_message TEXT, created_at TEXT NOT NULL, synced_at TEXT)");
        s.execute("CREATE INDEX IF NOT EXISTS idx_sync_queue_status ON sync_queue(status, id)");
        s.execute("CREATE TABLE IF NOT EXISTS app_users (username TEXT PRIMARY KEY, full_name TEXT NOT NULL, cpf TEXT NOT NULL, email TEXT NOT NULL, whatsapp TEXT NOT NULL, profile TEXT NOT NULL, creci TEXT, salt_b64 TEXT NOT NULL, hash_b64 TEXT NOT NULL, iterations INTEGER NOT NULL, active INTEGER NOT NULL DEFAULT 1, must_change_password INTEGER NOT NULL DEFAULT 1)");
      }
      seedUsers();
    } catch (Exception e) { throw new SQLException("Não foi possível abrir o banco local", e); }
  }

  synchronized Path path() { return path; }
  private void seedUsers() throws SQLException { String sql="INSERT OR IGNORE INTO app_users(username,full_name,cpf,email,whatsapp,profile,creci,salt_b64,hash_b64,iterations) VALUES(?,?,?,?,?,?,?,?,?,?)"; Object[][] users={
    {"wellington.wfc","Wellington Flavio Oliveira de Carvalho","975.886.101-82","wellington.corretor@wfcimoveis.com","(62) 99309-9184","Corretor","21496","G4kJ07OcUwOwi6/Li0eL+Q==","3ZT/65I5Id7CZ5wlHjZAxkMhKM8cP4SIBB6X/vlUH4g=",150000},
    {"wesley.wfc","Wesley Oliveira de Carvalho","007.359.671-05","wesley.corretor@wfcimoveis.com","(62) 99234-5819","Corretor","42716","3b3LW+lVyNeqd84Gk6Mykw==","cCL6tlI+R65OxmD92Ps9YowJyJ1RWXJpJIH9sE3Ko1c=",150000},
    {"eduarda.wfc","Eduarda Oliveira da Silva","078.875.521-84","duda.atendimento@wfcimoveis.com","(61) 99922-7042","Atendimento","","pozhgZvFnSbL/x3kcJ3T4w==","yp+TWu0qOhVB4TFRsPAKFEGa35AU8oq3//M3SippI9I=",150000},
    {"rodrigo.wfc","Rodrigo Honório da Silva","002.442.211-89","rodrigo.suporte@wfcimoveis.com","(64) 99272-0965","Administrador","","TJ4Lsm4embFkb5G7ioACtA==","nN/OpAxAmQbByw8f1oOr+egEz2DJHzS6RbXUX07sEjw=",150000}
  }; try(PreparedStatement p=connection.prepareStatement(sql)){for(Object[] u:users){for(int i=0;i<u.length;i++)p.setObject(i+1,u[i]);p.executeUpdate();}}
  }
  synchronized String authenticateLocal(String username, char[] password) throws Exception { String sql="SELECT full_name,profile,salt_b64,hash_b64,iterations,active FROM app_users WHERE username=?";try(PreparedStatement p=connection.prepareStatement(sql)){p.setString(1,username);try(ResultSet r=p.executeQuery()){if(!r.next()||r.getInt(6)!=1)return null;byte[] salt=Base64.getDecoder().decode(r.getString(3));byte[] expected=Base64.getDecoder().decode(r.getString(4));PBEKeySpec spec=new PBEKeySpec(password,salt,r.getInt(5),256);byte[] actual=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();return MessageDigest.isEqual(actual,expected)?r.getString(1)+"|"+r.getString(2):null;}}}
  synchronized int pendingCount() throws SQLException { try (PreparedStatement p = connection.prepareStatement("SELECT COUNT(*) FROM sync_queue WHERE status='PENDING'")) { try (ResultSet r=p.executeQuery()) { return r.next()?r.getInt(1):0; } } }
  synchronized void upsert(String type, String id, String json) throws SQLException {
    String now=Instant.now().toString();
    try (PreparedStatement p=connection.prepareStatement("INSERT INTO local_records(entity_type,entity_id,payload_json,version,updated_at,deleted) VALUES(?,?,?,0,?,0) ON CONFLICT(entity_type,entity_id) DO UPDATE SET payload_json=excluded.payload_json,updated_at=excluded.updated_at,deleted=0")) { p.setString(1,type);p.setString(2,id);p.setString(3,json);p.setString(4,now);p.executeUpdate(); }
    try (PreparedStatement p=connection.prepareStatement("INSERT INTO sync_queue(entity_type,entity_id,operation,payload_json,created_at) VALUES(?,?,?,?,?)")) { p.setString(1,type);p.setString(2,id);p.setString(3,"UPSERT");p.setString(4,json);p.setString(5,now);p.executeUpdate(); }
  }
  synchronized void applyRemote(String type, String id, String json, long version) throws SQLException { String now=Instant.now().toString(); try (PreparedStatement p=connection.prepareStatement("INSERT INTO local_records(entity_type,entity_id,payload_json,version,updated_at,deleted) VALUES(?,?,?,?,?,0) ON CONFLICT(entity_type,entity_id) DO UPDATE SET payload_json=excluded.payload_json,version=excluded.version,updated_at=excluded.updated_at,deleted=0")) { p.setString(1,type);p.setString(2,id);p.setString(3,json);p.setLong(4,version);p.setString(5,now);p.executeUpdate(); } }
  synchronized void markSyncing() throws SQLException { try (Statement s=connection.createStatement()) { s.executeUpdate("UPDATE sync_queue SET status='PENDING' WHERE status='SYNCING'"); s.executeUpdate("UPDATE sync_queue SET status='SYNCING' WHERE status='PENDING' ORDER BY id LIMIT 50"); } }
  synchronized String pendingJson() throws SQLException { StringBuilder b=new StringBuilder("["); boolean first=true; try (PreparedStatement p=connection.prepareStatement("SELECT id,entity_type,entity_id,operation,payload_json,base_version FROM sync_queue WHERE status='SYNCING' ORDER BY id LIMIT 50"); ResultSet r=p.executeQuery()) { while(r.next()){ if(!first)b.append(',');first=false; b.append("{\"queueId\":").append(r.getLong(1)).append(",\"entityType\":\"").append(Main.esc(r.getString(2))).append("\",\"entityId\":\"").append(Main.esc(r.getString(3))).append("\",\"operation\":\"").append(Main.esc(r.getString(4))).append("\",\"payload\":").append(r.getString(5)).append(",\"baseVersion\":").append(r.getLong(6)).append('}'); } } return b.append(']').toString(); }
  synchronized void markSynced() throws SQLException { try (PreparedStatement p=connection.prepareStatement("UPDATE sync_queue SET status='SYNCED',synced_at=? WHERE status='SYNCING'")) { p.setString(1,Instant.now().toString());p.executeUpdate(); } }
  synchronized void markFailed(String reason) throws SQLException { try (PreparedStatement p=connection.prepareStatement("UPDATE sync_queue SET status='PENDING',error_message=? WHERE status='SYNCING'")) { p.setString(1,reason);p.executeUpdate(); } }
  synchronized String meta(String key) throws SQLException { try(PreparedStatement p=connection.prepareStatement("SELECT value FROM app_meta WHERE key=?")){p.setString(1,key);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}} }
  synchronized void setMeta(String key,String value) throws SQLException { try(PreparedStatement p=connection.prepareStatement("INSERT INTO app_meta(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")){p.setString(1,key);p.setString(2,value);p.executeUpdate();} }
  @Override public synchronized void close() throws SQLException { connection.close(); }
}
