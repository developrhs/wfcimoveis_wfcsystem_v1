package com.wfcimoveis.wfcsystem;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;

final class CredentialVault {
  private static final String MAGIC="WFCVAULT1";
  private final Path file=Path.of(System.getProperty("user.home"),".wfcsystem","credentials.vault");
  private final Map<String,String> values=new LinkedHashMap<>();
  boolean exists(){return Files.exists(file);}
  Set<String> keys(){return values.keySet();}
  String get(String k){return values.getOrDefault(k,"");}
  void put(String k,String v){if(v==null)v="";values.put(k,v);}
  void clear(){values.clear();}
  void load(char[] password) throws Exception { byte[] all=Files.readAllBytes(file); byte[] magic=MAGIC.getBytes(StandardCharsets.UTF_8); for(int i=0;i<magic.length;i++)if(all[i]!=magic[i])throw new SecurityException("Cofre inválido"); int saltLen=16,ivLen=12; byte[] salt=Arrays.copyOfRange(all,magic.length,magic.length+saltLen),iv=Arrays.copyOfRange(all,magic.length+saltLen,magic.length+saltLen+ivLen),cipher=Arrays.copyOfRange(all,magic.length+saltLen+ivLen,all.length); Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(password,salt),new GCMParameterSpec(128,iv));String text=new String(c.doFinal(cipher),StandardCharsets.UTF_8);values.clear();for(String line:text.split("\\R")){int n=line.indexOf('=');if(n>0)values.put(line.substring(0,n),line.substring(n+1));} }
  void save(char[] password) throws Exception { Files.createDirectories(file.getParent());byte[] salt=new byte[16],iv=new byte[12];SecureRandom r=new SecureRandom();r.nextBytes(salt);r.nextBytes(iv);StringBuilder text=new StringBuilder();for(var e:values.entrySet())text.append(e.getKey()).append('=').append(e.getValue().replace("\\","\\\\").replace("\n","\\n")).append('\n');Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(password,salt),new GCMParameterSpec(128,iv));byte[] out=new byte[MAGIC.length()+salt.length+iv.length+c.doFinal(text.toString().getBytes(StandardCharsets.UTF_8)).length];System.arraycopy(MAGIC.getBytes(StandardCharsets.UTF_8),0,out,0,MAGIC.length());System.arraycopy(salt,0,out,MAGIC.length(),salt.length);System.arraycopy(iv,0,out,MAGIC.length()+salt.length,iv.length);byte[] enc=c.doFinal(text.toString().getBytes(StandardCharsets.UTF_8));System.arraycopy(enc,0,out,MAGIC.length()+salt.length+iv.length,enc.length);Files.write(file,out,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);}
  private static SecretKey key(char[] password,byte[] salt)throws Exception{SecretKeyFactory f=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");KeySpec s=new javax.crypto.spec.PBEKeySpec(password,salt,150000,256);return new SecretKeySpec(f.generateSecret(s).getEncoded(),"AES");}
}
