# WFCSystem v1 — documentação operacional

## Objetivo

Aplicação desktop Java Swing para a WFC Imóveis. O desktop autentica pela API PHP publicada em `https://wfcimoveis.com/sistema/api/v1`, mantém a sessão HTTP e não acessa o MySQL diretamente.

## Build

Requisitos: Java 17+ e Maven 3.9+.

```bash
cp config.properties.example config.properties
mvn clean package
java -jar target/wfcsystem-v1-0.1.1.jar
```

## Login e banco

A aplicação verifica `/health` antes de enviar o login. Se a API retornar `DB_CONNECTION_FAILED`, a correção deve ser feita no servidor, em `config/local.php` ou nas variáveis de ambiente da API: `WFC_DB_HOST`, `WFC_DB_NAME`, `WFC_DB_USER` e `WFC_DB_PASS`.

Para a API PHP hospedada no mesmo ambiente, usar o host MySQL indicado no cPanel (em muitos planos, `localhost`), o nome completo do banco e usuário com o prefixo da conta, a senha correta e a porta `3306`. O MySQL Workbench, quando acessa externamente, também exige que o IP público seja liberado em **Remote MySQL**; essa regra é do acesso externo e não deve levar o desktop a conectar diretamente no banco.

## FTP

As credenciais FTP ficam somente no `config.properties` local. Não usar senha de cPanel no repositório e preferir usuário FTP dedicado, limitado ao armazenamento de mídia.

## Diagnóstico atual

Consulte `wfcsystem_log.txt`. Na última validação pública, `/health` retornou HTTP 503 com `DB_CONNECTION_FAILED`, confirmando que a aplicação desktop precisa aguardar a correção da configuração/conectividade do banco na API.
