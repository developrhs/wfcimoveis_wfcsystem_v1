# WFCSystem v1 — documentação operacional

## Objetivo

O WFCSystem v1 é uma aplicação desktop Java Swing para a WFC Imóveis. A versão 0.2.0 acrescenta banco local SQLite, funcionamento offline, cofre local criptografado e sincronização automática com a API HTTPS.

## Build e execução

Requisitos: Java 17+ e Maven 3.9+.

```bash
mvn clean test package
java -jar target/wfcsystem-v1-0.2.0-standalone.jar
```

O artefato `standalone` já inclui o driver SQLite. O JAR comum não é o pacote recomendado para distribuição.

## Banco local

O banco é criado automaticamente em:

```text
~/.wfcsystem/wfcsystem.db
```

As tabelas locais incluem `app_meta`, `local_records` e `sync_queue`. O modo offline permite abrir a aplicação, registrar alterações locais e mantê-las em fila até que a API esteja disponível.

O banco local não armazena senhas de cPanel, FTP, phpMyAdmin ou MySQL.

## Usuários locais

Na primeira inicialização, o SQLite cadastra os quatro usuários operacionais fornecidos para o sistema, preservando nome, CPF, e-mail, WhatsApp, perfil e CRECI. As senhas iniciais são gravadas apenas como hashes PBKDF2 com salt individual; não há senha em texto aberto no código, no JAR ou no log.

O modo offline não é um atalho: username vazio, usuário inexistente, usuário inativo ou senha incorreta são rejeitados. O primeiro username cadastrado aparece preenchido apenas como conveniência visual, enquanto o campo de senha permanece vazio. A autenticação online continua sendo preferencial; o fallback local só ocorre quando a API está indisponível.

## Cofre de credenciais

A tela **Configurações > Credenciais criptografadas** permite cadastrar dados operacionais no computador autorizado. O arquivo é salvo em:

```text
~/.wfcsystem/credentials.vault
```

O conteúdo é protegido com PBKDF2-HMAC-SHA256 e AES-256-GCM. A senha-mestra não é salva. As credenciais não são exibidas em logs, não entram no Git e não são incluídas no JAR ou no ZIP de distribuição.

A tela contempla campos para cPanel, FTP, phpMyAdmin e conexão administrativa MySQL, mas a sincronização do sistema usa a API HTTPS, não uma conexão direta do desktop ao MySQL.

## Sincronização automática

Após o login ou abertura do modo offline, o aplicativo tenta sincronizar a cada 60 segundos e também possui o botão **Sincronizar agora**.

O contrato esperado da API é:

```text
POST /sync/push
Content-Type: application/json
{"items":[{"queueId":1,"entityType":"imovel","entityId":"123","operation":"UPSERT","payload":{},"baseVersion":0}]}
```

Para o recebimento incremental:

```text
GET /sync/pull?since=<ISO-8601>
{"items":[{"entityType":"imovel","entityId":"123","version":4,"payload":{}}]}
```

Se esses endpoints ainda não existirem na API PHP, o aplicativo preserva as alterações no SQLite e informa que o envio ou recebimento precisa ser habilitado no servidor. A implementação do servidor deve validar sessão, permissões, versão base, conflitos e payload antes de alterar o MySQL.

## Login e banco online

O aplicativo verifica `/health` antes de enviar o login. Se a API retornar `DB_CONNECTION_FAILED` ou `DB_CONFIG_MISSING`, a correção deve ser feita no servidor, em `config/local.php` ou nas variáveis privadas da API: `WFC_DB_HOST`, `WFC_DB_NAME`, `WFC_DB_USER` e `WFC_DB_PASS`.

O desktop continua acessando o sistema por HTTPS. As credenciais administrativas salvas no cofre são configurações locais para uso autorizado e não substituem a autenticação da API.
