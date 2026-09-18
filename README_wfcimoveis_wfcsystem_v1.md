# Estado factual do WFCSystem v1 — aplicativo desktop Java

**Projeto:** `developrhs/wfcimoveis_wfcsystem_v1`  
**Aplicação:** WFCSystem v1 para desktop  
**Tecnologia constatada:** Java Swing, Java 17, SQLite, HTTP/HTTPS e Apache Commons Net FTP  
**Data da constatação:** 18 de setembro de 2026  
**Escopo:** inspeção do repositório, fonte Java, configuração de exemplo, artefatos, histórico Git e build local. Nenhum servidor, banco remoto ou FTP foi alterado durante esta análise.

## 1. Conclusão objetiva

O repositório do WFCSystem v1 contém uma aplicação Java desktop com fonte versionada e uma arquitetura offline-first. O aplicativo mantém dados locais em SQLite, autentica no servidor através da API PHP por HTTPS e envia imagens pendentes por FTP.

A configuração padrão do cliente aponta para:

```text
https://wfcimoveis.com/sistema/api/v1
```

O aplicativo não acessa o MySQL remoto diretamente. O acesso ao banco online é feito pela API PHP.

O estado atual do repositório é compilável com JDK 17 e Maven. O build e os testes Maven disponíveis foram executados com sucesso nesta constatação.

## 2. Repositório e versão atual

Repositório analisado:

```text
developrhs/wfcimoveis_wfcsystem_v1
```

Commit atual:

```text
6993a89 feat: tratar erros HTTP na interface desktop
```

Últimos commits relevantes constatados:

```text
6993a89 feat: tratar erros HTTP na interface desktop
00214f1 fix: preservar conflitos parciais na sincronizacao
3491bf7 feat: implementar frontend operacional do WFCSystem
56cbb81 docs: define permanent system architecture
7a946ea feat: sync desktop records bidirectionally with server
97a3363 fix: enforce local user authentication and seed profiles
62d1113 feat: add encrypted credentials vault local sqlite and sync
53b05d3 build: release wfcsystem v1 0.1.2
1b544ea release: wfcsystem v1.0.2 with database diagnostics
6f1caea fix: diagnose API database connection before desktop login
5387919 feat: connect desktop client to PHP session API and FTP
```

O repositório estava limpo antes da auditoria. Depois da compilação, os arquivos gerados permaneceram ignorados pelo `.gitignore` e não foram incluídos em alteração de fonte.

## 3. Arquivos versionados

Os principais arquivos versionados são:

```text
README.md
README_wfcsystem.md
config.properties.example
pom.xml
start-wfcsystem.bat
wfcsystem-v1-0.1.0.jar
wfcsystem-v1-0.1.2.jar
wfcsystem-v1-0.2.0-standalone.jar
wfcsystem_log.txt
wfcsystem_v1.zip
src/main/java/com/wfcimoveis/wfcsystem/AppDatabase.java
src/main/java/com/wfcimoveis/wfcsystem/CredentialVault.java
src/main/java/com/wfcimoveis/wfcsystem/ImageStaging.java
src/main/java/com/wfcimoveis/wfcsystem/Main.java
src/main/java/com/wfcimoveis/wfcsystem/ManagementPanel.java
```

O código-fonte Java possui 334 linhas distribuídas em cinco classes principais:

| Arquivo | Responsabilidade constatada |
|---|---|
| `Main.java` | Inicialização da interface, login, sessão HTTP, sincronização e chamadas principais |
| `AppDatabase.java` | SQLite local, usuários, registros pendentes, versões, conflitos e fila de imagens |
| `CredentialVault.java` | Cofre local criptografado para credenciais operacionais |
| `ImageStaging.java` | Seleção, armazenamento temporário e envio de imagens por FTP |
| `ManagementPanel.java` | Interface de gestão de imóveis, clientes, prova social, vendas e usuários |

## 4. Dependências e requisito de execução

O `pom.xml` define:

```text
Java release: 17
sqlite-jdbc: 3.46.1.0
commons-net: 3.11.1
Gson: 2.11.0
```

O projeto utiliza Maven Compiler Plugin, Maven JAR Plugin e Maven Shade Plugin.

O JAR `standalone` é gerado com as dependências incorporadas. O manifesto define:

```text
Main-Class: com.wfcimoveis.wfcsystem.Main
Build-Jdk-Spec: 17
```

A execução do aplicativo requer um ambiente gráfico Swing. A auditoria foi feita em ambiente de compilação; não foi iniciada a interface gráfica em uma sessão desktop do usuário.

## 5. Build constatado

Foi utilizado explicitamente o JDK 17 instalado no ambiente:

```text
javac 17.0.20
Apache Maven 3.8.7
```

Os comandos executados foram:

```bash
mvn -q clean package
mvn -q test
git diff --check
```

Resultado:

```text
BUILD_AND_TEST_OK
```

Não existem testes automatizados Java em `src/test` no estado analisado. O comando `mvn test` passou porque o projeto não possui uma suíte de testes registrada.

Artefatos gerados pelo build:

```text
target/wfcsystem-v1-0.3.0.jar
target/wfcsystem-v1-0.3.0-standalone.jar
```

Tamanhos constatados:

```text
target/wfcsystem-v1-0.3.0.jar              41 KB
target/wfcsystem-v1-0.3.0-standalone.jar   15 MB
```

O JAR standalone é o artefato adequado para execução sem instalação manual das dependências Maven.

## 6. Artefatos antigos mantidos no repositório

Além do código atual, o Git contém artefatos de versões anteriores:

```text
wfcsystem-v1-0.1.0.jar
wfcsystem-v1-0.1.2.jar
wfcsystem-v1-0.2.0-standalone.jar
wfcsystem_v1.zip
wfcsystem_log.txt
```

Os manifests dos JARs antigos e do JAR atual apontam para a classe principal:

```text
com.wfcimoveis.wfcsystem.Main
```

Os hashes constatados durante a auditoria foram:

| Artefato | SHA-256 constatado |
|---|---|
| `wfcsystem-v1-0.1.0.jar` | `a306d302e53f5662b1e13e29b9cf01d7c09851686bf1b1f48d2384a10822e0e7` |
| `wfcsystem-v1-0.1.2.jar` | `4fa0165abd498129d3eae7a9414e95ba6033fae11dff82abc1f643b6e74a2fc8` |
| `wfcsystem-v1-0.2.0-standalone.jar` | `64f180cb6da995d39eb0a123bde21576515d465a6bd249453877f11034f28693` |
| `target/wfcsystem-v1-0.3.0-standalone.jar` | `d9196e2403f7bdfeb1239f2c466c8100957d3b482aec31253c361231b393020c` |

Esses arquivos não foram removidos porque esta etapa foi de constatação. A coexistência de versões pode causar dúvida sobre qual JAR deve ser distribuído. O nome do artefato atual é definido pelo `pom.xml` como versão `0.3.0`.

## 7. API online usada pelo Java

O arquivo `config.properties.example` define:

```properties
api.baseUrl=https://wfcimoveis.com/sistema/api/v1
```

O fluxo de autenticação utiliza sessão HTTP por cookie. O cliente envia um `POST` para:

```text
{api.baseUrl}/auth/login
```

Com corpo JSON:

```json
{"identity":"...","password":"..."}
```

O cliente mantém um `CookieManager` e usa o cookie da sessão nas chamadas posteriores.

As rotas usadas no código são:

```text
GET  /health
POST /auth/login
POST /auth/logout
GET  /auth/me
POST /sync/push
GET  /sync/pull?since=...
```

A URL pública de saúde foi validada separadamente e respondeu:

```text
HTTP 200
{"ok":true,"service":"wfc-api"}
```

Na etapa anterior de validação real, a chamada Java ao login chegou corretamente à API, enviou JSON e recebeu resposta JSON `401 INVALID_CREDENTIALS`. Isso comprovou transporte, rota, formato da requisição e processamento inicial de cookies. A credencial testada não foi aceita pelo servidor.

## 8. Banco local SQLite

O banco local é criado automaticamente em:

```text
~/.wfcsystem/wfcsystem.db
```

O código cria tabelas locais para:

- metadados de sincronização;
- usuários locais;
- registros de gestão;
- fila de imagens;
- estado de sincronização;
- versões e conflitos.

A aplicação usa o SQLite para permitir operação offline. Quando a API está indisponível, os registros permanecem localmente e são marcados para sincronização posterior.

O cliente não deve receber nem armazenar credenciais do MySQL remoto. O MySQL é responsabilidade da API PHP.

## 9. Usuários locais e dados sensíveis no código

`AppDatabase.java` contém uma rotina de seed com perfis locais, incluindo nome, CPF, e-mail, telefone, perfil, CRECI, salt e hash PBKDF2.

O código não contém as senhas em texto claro, mas contém dados pessoais e material criptográfico de usuários dentro da fonte versionada. Isso é um fato relevante de segurança e governança.

A autenticação local utiliza:

```text
PBKDF2WithHmacSHA256
150000 iterações
chave de 256 bits
```

O banco local também possui campo para indicar troca obrigatória de senha. A auditoria não alterou os dados nem tentou autenticar usuários locais.

## 10. Cofre local de credenciais

`CredentialVault.java` implementa um cofre local criptografado com:

```text
AES/GCM/NoPadding
PBKDF2WithHmacSHA256
150000 iterações
salt e IV aleatórios
```

O cofre é salvo localmente em:

```text
~/.wfcsystem/credentials.vault
```

O cofre é usado para guardar credenciais operacionais, como os dados de FTP. O arquivo de exemplo não contém senha FTP.

A senha-mestra não é enviada para a API nem para o banco remoto. Ela protege o arquivo local do cofre.

## 11. FTP e imagens

O projeto usa Apache Commons Net `FTPClient` para imagens.

A implementação observada:

- usa modo passivo;
- usa transferência binária;
- define timeout de conexão e dados;
- copia a imagem para uma área temporária local;
- registra a fila no SQLite;
- envia cada arquivo pendente;
- remove o arquivo local somente após confirmação do `storeFile`;
- preserva a fila quando ocorre falha.

O diretório temporário local é:

```text
<java.io.tmpdir>/wfcsystem-images
```

Os caminhos padrão configurados são:

```text
/public_html/wfc_storage/wfc_imoveis
/public_html/wfc_storage/nossos_clientes/prova_social
```

O host FTP no exemplo é:

```text
br968.hostgator.com.br
```

A conexão FTP real não foi executada nesta auditoria. Portanto, ainda não está confirmado se o host, usuário, senha e permissões do FTP estão corretos para o ambiente do usuário.

## 12. Sincronização

Depois do login ou da abertura em modo offline, o aplicativo agenda sincronização periódica a cada 60 segundos e disponibiliza o botão de sincronização manual.

O fluxo constatado é:

1. Marcar registros locais como em sincronização.
2. Montar o lote pendente.
3. Enviar registros para `POST /sync/push`.
4. Marcar itens aceitos pelo servidor.
5. Preservar conflitos parciais.
6. Consultar alterações com `GET /sync/pull?since=...`.
7. Aplicar atualizações no SQLite.
8. Enviar imagens pendentes pelo FTP.
9. Atualizar o estado visual de sincronização.

O código trata respostas HTTP específicas:

```text
401 Sessão expirada
403 Perfil sem permissão
413 Solicitação excede o limite
422 Dados inválidos
500/503 Serviço indisponível
```

Os endpoints de sincronização não foram executados com dados reais nesta auditoria.

## 13. Interface desktop constatada

`ManagementPanel.java` implementa telas locais para:

- imóveis;
- clientes;
- agentes;
- prova social;
- vendas;
- usuários.

Os cadastros são gravados primeiro no SQLite local. A sincronização remota depende do contrato da API PHP.

A alteração de perfil/status de usuário observada no painel é local. A sincronização administrativa correspondente depende de endpoint compatível no servidor.

## 14. Configuração local

O projeto fornece:

```text
config.properties.example
```

O arquivo local esperado é:

```text
config.properties
```

Esse arquivo é ignorado pelo Git. O exemplo contém a URL da API e os caminhos FTP, mas deixa usuário e senha FTP vazios.

O `.gitignore` também exclui:

```text
config.properties
*.local
.env
.env.*
target/
*.class
```

Nenhum `config.properties` local foi encontrado no repositório durante a auditoria.

## 15. Estado confirmado

Foram confirmados os seguintes fatos:

- o repositório contém fonte Java versionada;
- o commit atual é `6993a89`;
- o projeto usa Java 17;
- o projeto compila com Maven usando JDK 17;
- o JAR standalone `0.3.0` é gerado com sucesso;
- não existem testes Java automatizados no repositório;
- o cliente usa SQLite local;
- o cliente usa API PHP por HTTPS;
- o cliente não acessa o MySQL remoto diretamente;
- o cliente possui sincronização bidirecional preparada;
- o cliente possui fila local de imagens;
- o cliente possui integração FTP preparada;
- o repositório contém JARs e ZIPs de versões anteriores;
- o código versionado contém dados pessoais e hashes de usuários locais;
- a API de saúde online está acessível;
- o login Java foi chamado anteriormente e recebeu `401 INVALID_CREDENTIALS` para a credencial testada;
- o FTP real ainda não foi validado.

## 16. Estado não confirmado

Ainda não foi confirmado:

- login válido de um usuário no servidor;
- funcionamento dos endpoints de sincronização em produção;
- compatibilidade completa entre o payload Java e a API PHP publicada;
- conexão FTP com credencial dedicada;
- permissão de escrita nas pastas de imagens;
- instalação do JAR em um computador Windows real;
- funcionamento visual da interface em um desktop com Java 17;
- existência ou conteúdo de `config.properties` nos computadores dos usuários;
- estratégia de distribuição e atualização do JAR;
- correspondência entre os JARs antigos e as versões dos commits que os geraram.

## 17. Estado desta etapa

Este documento registra o estado do repositório e dos arquivos do aplicativo desktop Java em 18 de setembro de 2026. Nenhum código de produto foi alterado nesta etapa. A compilação apenas regenerou arquivos em `target/`, que são ignorados pelo Git.

A próxima validação técnica independente deve ser feita nesta ordem:

```text
1. Confirmar uma credencial válida na API PHP.
2. Testar auth/login e auth/me pelo Java.
3. Testar sincronização em ambiente controlado.
4. Confirmar host e credencial FTP dedicada.
5. Fazer upload de uma imagem de teste controlada.
6. Criar testes automatizados para API, SQLite e fila FTP.
```

## Referências

[1]: https://github.com/developrhs/wfcimoveis_wfcsystem_v1 "Repositório do WFCSystem v1"
[2]: https://wfcimoveis.com/sistema/api/v1/health "Endpoint de saúde da API PHP usada pelo desktop"
