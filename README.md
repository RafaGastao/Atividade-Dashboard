# Como executar o Thindesk

## Pré-requisitos

- JDK 23
- MongoDB local ou um cluster no MongoDB Atlas
- Git Bash

## 1. Abra a pasta do projeto

Selecione a branch `develop` e abra o Git Bash na pasta que contém o arquivo `pom.xml`.

## 2. Configure o banco e a senha de acesso

Execute os comandos abaixo. O terminal vai pedir a URI do MongoDB e a senha que você deseja usar para entrar no Thindesk.

```bash
read -rsp 'URI do MongoDB: ' MONGODB_URI; echo
read -rsp 'Senha do Thindesk: ' THINDESK_PASSWORD; echo
export MONGODB_URI THINDESK_PASSWORD
```

Se estiver usando o MongoDB local na porta padrão, informe `mongodb://localhost:27017/thindesk`. Se estiver usando o Atlas, informe a URI do seu cluster e confira se o IP da sua máquina está autorizado.

## 3. Inicie a aplicação

No mesmo terminal, execute:

```bash
./mvnw spring-boot:run
```

Aguarde aparecer `Started ThindeskApplication` e mantenha o terminal aberto.

## 4. Acesse o dashboard

No navegador, digite `http://localhost:8080/dashboard`. Entre com o usuário `admin` e a senha definida na etapa 2.

Para testar o painel, vá em **Chamados → Novo chamado** e cadastre um chamado com o status **Concluído**. Volte ao dashboard e clique em **Consultar / atualizar** para ver o registro nos indicadores e no gráfico do dia.
