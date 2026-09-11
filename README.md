# WFCSystem v1

Primeiro corpo de uma aplicação desktop Java para a WFC Imóveis. O programa possui uma tela de login, aviso de suporte e uma janela inicial com menu superior. Ele foi feito sem bibliotecas externas para facilitar a manutenção por quem conhece Java.

## Requisitos

Java 17 ou superior e Maven 3.9 ou superior para compilar. Para iniciar apenas o JAR, basta ter Java 17 instalado.

## Compilar

```bash
cp config.properties.example config.properties
# edite config.properties
mvn clean package
java -jar target/wfcsystem-v1-0.1.0.jar
```

Também existe um `start-wfcsystem.bat` para Windows.

## Login

O arquivo `config.properties.example` já aponta para a API PHP publicada:

```text
https://wfcimoveis.com/sistema/api/v1
```

O aplicativo envia um `POST` JSON para:

```text
{api.baseUrl}/auth/login
```

com o corpo:

```json
{"identity":"...","password":"..."}
```

A API atual consulta a tabela de usuários no servidor, valida a senha com `password_verify`, cria uma sessão HTTP e retorna o objeto `user`. O aplicativo mantém o cookie de sessão e possui o botão **Verificar sessão**. O cliente não acessa o MySQL diretamente e não contém senha de banco.

### Pré-requisito no HostGator

O backend PHP precisa ter `deploy/wfc_sistema/api/config/local.php` criado no servidor, fora do Git, com `WFC_DB_HOST`, `WFC_DB_NAME`, `WFC_DB_USER` e `WFC_DB_PASS` correspondentes ao banco. Sem esse arquivo ou variáveis equivalentes, a rota `/api/v1/health` retorna indisponibilidade e o login não poderá funcionar. O endpoint de saúde foi testado durante esta atualização e o servidor respondeu `503`, indicando que essa configuração ainda precisa ser conferida no HostGator.

## FTP e imagens

As pastas são configuradas em `config.properties`. Use um usuário FTP dedicado, restrito a `wfc_storage`. Não coloque senha FTP neste repositório ou no ZIP público; os campos `ftp.username` e `ftp.password` devem ser preenchidos somente no arquivo local `config.properties`.

- Imóveis: `/public_html/wfc_storage/wfc_imoveis`
- Prova social: `/public_html/wfc_storage/nossos_clientes/prova_social`

Depois do login, o botão **Testar FTP** autentica no servidor em modo passivo e lista as duas pastas determinadas. Nesta versão ele não envia, altera ou remove arquivos.

## Próximas extensões

Depois de validar o login, podem ser adicionados os módulos de imóveis, clientes, agentes, prova social, vendas e usuários. O papel retornado pela API deve ser `Administrador`, `Corretor` ou `Atendimento`.

## Suporte

As informações exibidas no aviso são apenas nome, e-mail e telefone configurados em `config.properties`. Não há CPF, senha ou dado pessoal sensível no aplicativo.
