const main = document.querySelector('main');
const form = main.querySelector('form');
const feedback = document.getElementById('feedback');
const resultados = document.getElementById('resultados');
let consulta;

async function lerJson(url, signal) {
    const resposta = await fetch(url, { signal, cache: 'no-store' });
    if (resposta.redirected || resposta.status === 401 || resposta.status === 403) {
        throw new Error('A sessão expirou. Entre novamente para acessar o dashboard.');
    }
    if (!resposta.ok) throw new Error('Não foi possível consultar os dados. Tente novamente.');
    return resposta.json();
}

function preencherLista(id, itens, barras = false) {
    const lista = document.getElementById(id);
    lista.replaceChildren();
    const maior = itens.reduce((max, [, total]) => Math.max(max, total), 1);
    for (const [rotulo, total] of itens) {
        const item = document.createElement('li');
        item.className = 'mb-2';
        item.textContent = rotulo + ' · ' + total;
        if (barras) {
            const barra = document.createElement('progress');
            barra.className = 'd-block w-100';
            barra.max = maior;
            barra.value = total;
            barra.setAttribute('aria-label', rotulo);
            item.append(barra);
        }
        lista.append(item);
    }
    if (!itens.length) {
        const vazio = document.createElement('li');
        vazio.textContent = 'Sem dados para os filtros selecionados.';
        lista.append(vazio);
    }
}

async function atualizar() {
    consulta?.abort();
    consulta = new AbortController();
    const { signal } = consulta;
    resultados.hidden = true;
    const dados = new FormData(form);
    const inicio = dados.get('inicio');
    const fim = dados.get('fim');
    if (inicio && fim && inicio > fim) {
        feedback.textContent = 'A data inicial deve anteceder ou coincidir com a final.';
        return;
    }
    feedback.textContent = 'Carregando chamados…';
    const filtros = new URLSearchParams([...dados].filter(([, valor]) => valor));
    try {
        const [resumo, opcoes] = await Promise.all([
            lerJson(main.dataset.api + '?' + filtros, signal),
            lerJson(main.dataset.api + '/filtros', signal)
        ]);
        if (signal.aborted) return;
        for (const [campo, chave] of [['status', 'status'], ['tipo', 'tipos'], ['tecnico', 'tecnicos']]) {
            const select = form.elements.namedItem(campo);
            const atual = dados.get(campo);
            const valores = new Set(opcoes[chave]);
            if (atual) valores.add(atual);
            select.replaceChildren(new Option('Todos', ''), ...[...valores].map(v => new Option(v, v)));
            select.value = atual;
        }
        for (const id of ['total', 'atendidos', 'atendidosSemData']) {
            document.getElementById(id).textContent = resumo[id];
        }
        preencherLista('dias', Object.entries(resumo.atendidosPorDia)
            .map(([data, total]) => [data.split('-').reverse().join('/'), total]), true);
        preencherLista('situacoes', Object.entries(resumo.situacoes), true);
        preencherLista('tipos', resumo.tiposAtendidos.map(item => [item.tipo, item.total]));
        resultados.hidden = false;
        feedback.textContent = resumo.total ? '' : 'Nenhum chamado encontrado.';
    } catch (erro) {
        if (!signal.aborted) feedback.textContent = erro.message;
    }
}

form.addEventListener('submit', evento => {
    evento.preventDefault();
    atualizar();
});
form.addEventListener('reset', () => setTimeout(atualizar, 0));
atualizar();
