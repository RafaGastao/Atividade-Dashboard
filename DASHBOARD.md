# Dashboard administrativo do Thindesk

| Requisito | Implementação |
| --- | --- |
| RF01 | Total de chamados, atendidos e atendidos sem data |
| RF02 | Gráfico de barras com atendidos agrupados por dia de conclusão |
| RF03 | Distribuição de chamados por situação |
| RF04 | Ranking de tipos dos chamados atendidos |
| RF05 | Filtros combináveis por período, status, tipo e técnico |
| RF06 | Interface Thymeleaf consulta a API JSON, que usa a coleção chamados do MongoDB |
| RF07 | Atualização sob demanda e mensagens de carregamento, erro e ausência de dados |

## Requisitos não funcionais

- A página e os endpoints exigem a sessão autenticada do Thindesk.
- A API entrega contagens agregadas, sem descrições ou nomes de usuários dos chamados.
- A página adapta os cartões à largura disponível e oferece rótulos nos filtros e valores textuais nos gráficos.
- A contagem usa a data de conclusão em America/Sao_Paulo. A filtragem e a agregação são feitas em memória nesta versão.

## API

Abra /dashboard após entrar no sistema. O menu lateral tem um link para o painel.

### GET /api/dashboard

Os parâmetros são opcionais. Os limites inicio e fim usam AAAA-MM-DD e incluem as próprias datas. status, tipo e tecnico aceitam os valores retornados pelo endpoint de filtros. Ao informar qualquer limite de data, entram no recorte apenas chamados com conclusão datada. Registros históricos concluídos sem data entram nos totais e ranking nas consultas sem período. Reabrir um chamado remove a data de conclusão. Uma nova conclusão registra a data atual.

Exemplo de requisição autenticada no navegador

    GET /api/dashboard?inicio=2026-11-01&fim=2026-11-30&tipo=Manutencao

Exemplo de resposta HTTP 200

    {
      "total": 2,
      "atendidos": 2,
      "atendidosSemData": 0,
      "situacoes": {"Concluído": 2},
      "atendidosPorDia": {"2026-11-14": 2},
      "tiposAtendidos": [{"tipo": "Manutenção", "total": 2}]
    }

### GET /api/dashboard/filtros

Sem parâmetros. Exemplo de resposta HTTP 200

    {"status":["Aberto","Concluído"],"tipos":["Manutenção"],"tecnicos":["João"]}

Datas inválidas ou invertidas retornam HTTP 400. Sem sessão, o fluxo de autenticação atual redireciona para /login. Uma falha de acesso ao banco pode retornar HTTP 500. Uma consulta válida sem correspondências retorna HTTP 200 com contagens zeradas e coleções vazias.

## Roteiro de verificação

1. Com MongoDB disponível, inicie o Thindesk com o Maven Wrapper e entre no sistema.
2. Abra Dashboard pelo menu. Compare os totais com a lista de chamados.
3. Conclua um chamado e atualize o painel. Confirme o incremento no dia atual, no status e no ranking.
4. Filtre pelo dia da conclusão e por tipo. Confirme que os resultados obedecem aos dois filtros.
5. Consulte um período sem conclusões e confira a mensagem de ausência de dados.
6. Com sessão válida, consulte /api/dashboard?inicio=2026-11-30&fim=2026-11-01 e confira HTTP 400.

## Roteiro de vídeo

- 0:00–0:30. Apresente o objetivo e mostre os três indicadores.
- 0:30–1:20. Mostre os gráficos, a situação dos chamados e o ranking.
- 1:20–2:10. Combine filtros e mostre a atualização após concluir um chamado.
- 2:10–2:55. Mostre os dois endpoints no navegador e explique o uso do MongoDB e o tratamento dos chamados antigos sem data de conclusão.
