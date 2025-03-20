## Documentação Técnica sobre Intermediate Link Throw e Catch Events no AWS Step Functions

### Conceitos Gerais (Camunda)
No Camunda, os eventos intermediários do tipo Link Throw e Link Catch permitem que um fluxo BPMN faça saltos diretos entre diferentes pontos de execução dentro do mesmo processo, garantindo flexibilidade e simplificação visual.

- **Intermediate Link Throw Event:** Representa um ponto no processo que lança ("throw") um sinal interno para que outro ponto específico ("catch") no mesmo processo continue sua execução.
- **Intermediate Link Catch Event:** Representa o ponto no processo que recebe ("catch") o sinal lançado e inicia ou continua a execução a partir dali.

Esses eventos têm as seguintes características:
- Comunicação interna dentro de um mesmo fluxo de execução.
- Não são visíveis externamente (não são triggers externos).
- Não carregam payload adicional (apenas redirecionam o fluxo).

### AWS Step Functions e o conceito equivalente

O AWS Step Functions não possui explicitamente o conceito de Intermediate Link Throw e Catch Event nativo. Entretanto, é possível simular o mesmo comportamento utilizando algumas estratégias:

#### Estratégia Recomendada: Uso de `Choice` e `Pass` States

A melhor forma de simular o comportamento de Link Throw e Catch é usando uma combinação dos estados `Choice` e `Pass`, com redirecionamento explícito usando nomes de estado no Step Functions.

### Exemplo prático de simulação no AWS Step Functions:

Imagine o seguinte cenário:
- Um fluxo começa, executa várias tarefas, e em determinada condição, precisa "lançar um sinal" para reiniciar a execução a partir de um ponto intermediário já executado anteriormente.

```json
{
  "StartAt": "InitialTask",
  "States": {
    "InitialTask": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:initial-task",
      "Next": "CheckCondition"
    },
    "CheckCondition": {
      "Type": "Choice",
      "Choices": [
        {
          "Variable": "$.needsReExecution",
          "BooleanEquals": true,
          "Next": "LinkThrow"
        }
      ],
      "Default": "FinalTask"
    },
    "LinkThrow": {
      "Type": "Pass",
      "Next": "IntermediateLinkCatch"
    },
    "IntermediateLinkCatch": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:intermediate-task",
      "Next": "FinalTask"
    },
    "FinalTask": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:final-task",
      "End": true
    }
  }
}
```

### Características dessa implementação:
- **Flexibilidade:** Facilmente extensível adicionando novos estados ou condições.
- **Visibilidade:** O redirecionamento fica explícito no fluxo, o que facilita o entendimento.
- **Controle:** O fluxo pode avaliar variáveis do estado e fazer o desvio condicional claramente.

### Diferenças em relação ao Camunda:
- **Explicitamente declarativo:** No Step Functions, o redirecionamento é feito explicitamente pelo nome do próximo estado.
- **Payload e Contexto:** Diferente do Camunda, o Step Functions permite transportar dados (payload) durante esses desvios, agregando flexibilidade adicional.
- **Auditoria e Rastreamento:** Step Functions oferece rastreamento integrado e logging dos saltos, o que é vantajoso para auditorias e troubleshooting.

### Configuração e Propriedades no Step Functions:

- **Choice State:** Permite múltiplas condições para direcionar o fluxo.
- **Pass State:** Estado simples que redireciona para outro estado, podendo opcionalmente transformar os dados.
- **InputPath/OutputPath:** Permitem controlar explicitamente os dados transmitidos entre estados.

### Conclusão e Recomendações

Embora o AWS Step Functions não ofereça diretamente os eventos "Link Throw" e "Link Catch", a flexibilidade do serviço permite replicar de forma clara e eficiente esse comportamento utilizando estados condicionais e de passagem explícita, proporcionando um controle robusto do fluxo e facilidade de manutenção.

