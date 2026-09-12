# WFCSystem v1 — documentação operacional

## Objetivo

O WFCSystem v1 é uma aplicação desktop Java Swing para a WFC Imóveis. A versão 0.3.0 consolida o banco local SQLite, o funcionamento offline, o cofre local criptografado, as tabelas de gestão e a fila local de imagens antes do envio ao FTP.

## Build e execução

Requisitos: Java 17+ e Maven 3.9+.

```bash
mvn clean test package
java -jar target/wfcsystem-v1-0.3.0-standalone.jar
```

O artefato `standalone` inclui SQLite e a biblioteca Apache Commons Net para FTP.

## Banco local e tabelas

O banco é criado automaticamente em `~/.wfcsystem/wfcsystem.db`. As principais tabelas são `app_meta`, `local_records`, `sync_queue`, `app_users` e `image_queue`.

As abas **Imóveis**, **Clientes**, **Agentes** e **Vendas** permitem salvar, atualizar, excluir e recarregar registros locais em formato JSON. As alterações entram na fila de sincronização. A aba **Usuários** lista os usuários locais e permite que um operador autorizado altere o perfil entre `Administrador`, `Corretor` e `Atendimento`, além do status ativo/inativo.

O banco local não armazena senhas de cPanel, FTP, phpMyAdmin ou MySQL. A alteração de perfil/status é local nesta etapa e deverá ser sincronizada com endpoint administrativo próprio quando essa API for disponibilizada.

## Fila local de imagens

Ao iniciar, o aplicativo cria a pasta temporária:

```text
<java.io.tmpdir>/wfcsystem-images
```

As imagens selecionadas nas telas são copiadas para subpastas temporárias e registradas na tabela `image_queue`. Apenas JPG, JPEG, PNG, WEBP e GIF são aceitos. A fila preserva o arquivo em caso de falha e só o remove depois que o servidor FTP confirmar o recebimento.

A sincronização FTP usa modo passivo e transferência binária. As pastas padrão são:

```text
/public_html/wfc_storage/wfc_imoveis
/public_html/wfc_storage/nossos_clientes/prova_social
```

O caminho da imagem deverá ser associado ao JSON do registro quando o contrato da API de mídia estiver definido. Nesta primeira consolidação, a fila garante o armazenamento local e o envio seguro da imagem.

## Credenciais

As credenciais operacionais devem ser cadastradas em **Configurações > Credenciais criptografadas**. Nenhuma senha deve ser colocada no repositório, no JAR, no ZIP público ou no log. A senha do cPanel não deve ser presumida como senha FTP.

## Sincronização

Após o login ou abertura do modo offline, o aplicativo tenta sincronizar a cada 60 segundos e também possui o botão **Sincronizar agora**. Os registros usam `POST /sync/push` e `GET /sync/pull?since=...`. As imagens pendentes são enviadas ao FTP durante a sincronização.

Se os endpoints ainda não existirem na API PHP, os registros permanecem no SQLite. O envio de arquivos ao FTP exige host, usuário, senha e caminhos válidos no cofre local.

## Arquitetura oficial e operação permanente

O produto é composto por três camadas separadas. `https://wfcimoveis.com/` é o site público destinado aos clientes e não deve ser substituído pelo painel. `https://wfcimoveis.com/sistema/` é o sistema web permanente, com login e gestão de imóveis, clientes, prova social e usuários. O **WfcSystem** é o aplicativo Java local, usado para trabalhar sem internet e sincronizar posteriormente.

O desktop usa como base padrão `https://wfcimoveis.com/sistema/api/v1`. Quando a internet está indisponível, os cadastros permanecem no SQLite e as imagens permanecem em `<java.io.tmpdir>/wfcsystem-images`; quando a conexão retorna, o desktop envia registros pela API HTTPS e imagens pela fila FTP. A raiz pública do site nunca é destino de upload do desktop.

A publicação permanente do painel deve manter os arquivos em `/home3/cwcimo17/public_html/wfc_sistema/`, expostos pelo endereço `/sistema/`. Uma atualização do painel não deve apagar nem substituir arquivos do site público em `public_html`.
