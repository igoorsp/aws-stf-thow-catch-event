# 📌 Step Functions Link Events Handler

Este projeto oferece uma implementação prática para simular o comportamento dos **Intermediate Link Throw e Catch Events** do Camunda usando **AWS Step Functions**, AWS Lambda e filas SQS.

---

## 🚩 Visão Geral

Em processos BPM (Business Process Management) utilizando Camunda, é comum existir eventos intermediários do tipo **throw** (lançar) e **catch** (capturar), permitindo redirecionamentos e loops internos em um mesmo fluxo.

Este projeto replica o mesmo comportamento no ambiente da AWS, especialmente utilizando AWS Step Functions. O Lambda desenvolvido nesta implementação processa mensagens vindas de uma fila SQS e interage diretamente com AWS Step Functions utilizando **Task Tokens**.

---

## 📚 Tecnologias Utilizadas

- **Java 17**
- **AWS SDK v2**
- **AWS Lambda**
- **AWS Step Functions (Task Tokens)**
- **Amazon SQS**
- **Quarkus Framework com CDI**
- **Jackson (serialização JSON)**

---

## 🚀 Como Funciona

### Fluxo principal:

1. **AWS Step Functions** inicia o fluxo e, em determinado ponto, envia uma mensagem para uma fila **SQS** com um **Task Token** para aguardar interação externa (human task ou processamento externo).

2. Este projeto, implementado como uma função **AWS Lambda**, consome a mensagem SQS contendo o Task Token enviado pela Step Functions.

3. A lógica interna do Lambda realiza validações, extração e processamento dos dados recebidos.

4. Após processar a mensagem, o Lambda responde ao AWS Step Functions utilizando o Task Token recebido para informar o resultado do processamento (`sendTaskSuccess` ou `sendTaskFailure`).

---

## 📌 Exemplo de Funcionamento (Business Rule)

Uma regra prática exemplificada no projeto é a reexecução condicional com base no valor da `businessKey`:

- Se o número ao final da `businessKey` for **par**, o Lambda devolve para a Step Functions com o resultado:

```json
{"reexecucao": true}
```

- Caso seja **ímpar**:

```json
{"reexecucao": false}
```

Isso permite fluxos condicionais dinâmicos, simulando exatamente o comportamento dos Link Events intermediários do Camunda.

**Exemplos:**

| BusinessKey        | Saída Lambda               | Comportamento Step Function |
|--------------------|----------------------------|-----------------------------|
| my-process-01      | `{ "reexecucao": false }`  | Continua fluxo normalmente  |
| my-process-02      | `{ "reexecucao": true }`   | Executa ponto intermediário novamente |

---

## 🧩 Estrutura do Projeto

```text
src/main/java/com/example/sfn/lambda/
├── LambdaSqsHandler.java    → Handler principal da Lambda
├── dto
│   └── SqsMessage.java      → Objeto DTO das mensagens recebidas do SQS
└── exception
    └── InvalidMessageException.java → Exceção customizada para validações
```

---

## 🔨 Explicação Técnica dos Componentes

- **LambdaSqsHandler**: Handler principal, processa mensagens do SQS e comunica-se com Step Functions.
- **SqsMessage**: Data Transfer Object (DTO), representa o JSON enviado pela Step Functions via SQS.
- **InvalidMessageException**: Tratamento elegante para erros relacionados a validações incorretas ou JSON inválido.

### ⚙️ Comunicação com Step Functions

O Lambda utiliza AWS SDK v2 para responder à Step Function:

- **SendTaskSuccessRequest:** Retorna sucesso com uma saída JSON informando se deve ou não realizar uma reexecução.
- **SendTaskFailureRequest:** Comunica falhas explícitas com mensagens claras, facilitando debug e tratamento de erros posteriores.

---

## 📌 Pré-requisitos

- **Java 17 instalado**
- Conta AWS com permissões:
   - AWS Lambda
   - AWS Step Functions
   - Amazon SQS
- AWS CLI configurado localmente para deploy e testes

---

## 🚀 Como implantar

**1. Construa e empacote o projeto (exemplo com Maven):**

```shell
mvn clean package
```

**2. Faça upload da função Lambda para AWS:**

- Utilizando AWS CLI ou console AWS, carregue o `.jar` gerado no Lambda.

**3. Configure permissões e triggers:**

- Configure a Lambda para ser disparada automaticamente ao receber mensagens na fila SQS desejada.
- Garanta que a Lambda possua permissão adequada para acessar AWS Step Functions e SQS.

---

## 🎯 Exemplo prático de entrada SQS:

```json
{
  "taskToken": "TASK_TOKEN_GERADO_STEP_FUNCTIONS",
  "executionId": "12345-exec",
  "businessKey": "my-process-02"
}
```

---

## 📈 Logs e monitoramento

- Logs detalhados no CloudWatch, permitindo rastreabilidade completa.
- Métricas integradas AWS Lambda e Step Functions fornecem monitoramento robusto.

---

## 🧑‍💻 Boas Práticas Utilizadas

- Clean Code e boas práticas de OOP.
- Uso adequado de logs estruturados para troubleshooting.
- Tratamento explícito e consistente de exceções.
- Código preparado para testes unitários e integração contínua.

---

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

📢 **Pronto!** Agora seu projeto tem uma documentação completa, clara e estruturada. Caso precise ajustar ou complementar algum ponto, estou à disposição.