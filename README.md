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

O aplicativo envia um `POST` JSON para:

```text
{api.baseUrl}/auth/login
```

com o corpo:

```json
{"username":"...","password":"..."}
```

A API deverá responder HTTP 200 com JSON contendo pelo menos `token`. O cliente não acessa o MySQL diretamente e não contém senha de banco.

## FTP e imagens

As pastas são configuradas em `config.properties`. Use um usuário FTP dedicado, restrito a `wfc_storage`. Não coloque senha FTP neste repositório ou no ZIP público; o campo deverá ser preenchido localmente no próximo módulo.

- Imóveis: `/public_html/wfc_storage/wfc_imoveis`
- Prova social: `/public_html/wfc_storage/nossos_clientes/prova_social`

## Próximas extensões

Depois de validar o login, podem ser adicionados os módulos de imóveis, clientes, agentes, prova social, vendas e usuários. O papel retornado pela API deve ser `Administrador`, `Corretor` ou `Atendimento`.

## Suporte

As informações exibidas no aviso são apenas nome, e-mail e telefone configurados em `config.properties`. Não há CPF, senha ou dado pessoal sensível no aplicativo.
