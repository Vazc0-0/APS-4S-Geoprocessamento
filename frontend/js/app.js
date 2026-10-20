const RAMPA = ['#7A2E1F', '#B4472A', '#DB7B33', '#E9B949', '#D9DB6A', '#93C557', '#4E9E45', '#1F6B3A'];
const CORES_METODO = { insertion: '#DB7B33', merge: '#E9B949', bucket: '#93C557' };

const estado = { metodo: 'insertion', tamanho: '1k', cenario: 'ideal', opcoes: null, ultimo: null };
const $ = (id) => document.getElementById(id);
const numero = (n) => Number(n).toLocaleString('pt-BR');
const ms = (n) => Number(n).toLocaleString('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 }) + ' ms';

async function api(caminho) {
  const resposta = await fetch(caminho);
  const dados = await resposta.json();
  if (!resposta.ok) throw new Error(dados.erro || 'Erro ' + resposta.status);
  return dados;
}

function criarSegmentos(container, itens, chave) {
  container.innerHTML = '';
  for (const item of itens) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.textContent = item.nome;
    if (item.dica) botao.title = item.dica;
    botao.setAttribute('aria-pressed', String(estado[chave] === item.codigo));
    botao.addEventListener('click', () => {
      estado[chave] = item.codigo;
      atualizarBotoes();
      atualizarAviso();
    });
    botao.dataset.codigo = item.codigo;
    container.appendChild(botao);
  }
}

function atualizarBotoes() {
  for (const [id, chave] of [['grupo-metodo', 'metodo'], ['grupo-tamanho', 'tamanho'], ['grupo-cenario', 'cenario']]) {
    for (const b of $(id).querySelectorAll('button')) {
      b.setAttribute('aria-pressed', String(b.dataset.codigo === estado[chave]));
    }
  }
  const metodo = estado.opcoes.metodos.find((m) => m.codigo === estado.metodo);
  for (const b of $('grupo-cenario').querySelectorAll('button')) {
    const c = metodo.cenarios.find((x) => x.codigo === b.dataset.codigo);
    b.title = 'Arquivo: ' + c.arquivo;
  }
}

function atualizarAviso() {
  const grande = ['50k', '100k'].includes(estado.tamanho);
  const lento = estado.metodo === 'insertion' && estado.cenario !== 'ideal' && grande;
  $('aviso').hidden = !lento;
  $('aviso').textContent = lento
    ? 'Atenção: o Insertion Sort neste tamanho e cenário é O(n²) e pode levar vários segundos. Para tamanhos grandes o sistema mede 3 amostras em vez de 10.'
    : '';
}

async function executar() {
  carregando(true, 'Ordenando no Java…');
  try {
    const q = new URLSearchParams({ metodo: estado.metodo, tamanho: estado.tamanho, cenario: estado.cenario });
    const r = await api('/api/ordenar?' + q);
    estado.ultimo = r;
    mostrarResultado(r);
  } catch (e) {
    alert('Não foi possível executar: ' + e.message);
  } finally {
    carregando(false);
  }
}

function mostrarResultado(r) {
  $('titulo').textContent = r.relatorio.titulo;
  $('pasta').textContent = r.pasta;
  $('titulo-faixas').textContent = r.metodo === 'bucket' ? '2 · Baldes' : '2 · Distribuição';
  $('rotulo-entrada').textContent = r.arquivo.split(' · ')[0].toLowerCase();

  desenharMapa($('mapa-antes'), r.mapaAntes);
  desenharMapa($('mapa-depois'), r.mapaDepois);
  desenharFaixas(r.mapaAntes);

  $('m-tempo').textContent = ms(r.mediaMs);
  $('m-complexidade').textContent = r.complexidade;
  $('m-comparacoes').textContent = numero(r.comparacoes);
  $('m-movimentacoes').textContent = numero(r.movimentacoes);

  const selo = $('selo');
  selo.hidden = false;
  selo.className = 'selo ' + r.relatorio.nivel;
  $('veredito').textContent = r.relatorio.veredito;
  $('relatorio-titulo').textContent = 'Por que foi assim';
  $('relatorio-texto').innerHTML = '';
  for (const paragrafo of r.relatorio.paragrafos) {
    const p = document.createElement('p');
    p.textContent = paragrafo;
    $('relatorio-texto').appendChild(p);
  }
  $('baixar-pdf').disabled = false;

  $('lista-antes').textContent = r.antes.join(', ');
  $('lista-depois').textContent = r.depois.join(', ');
}

function faixaDoValor(v, menor, maior) {
  if (maior === menor) return 0;
  return Math.min(7, Math.floor(((v - menor) / (maior - menor + 1)) * 8));
}

function desenharMapa(container, valores) {
  const menor = Math.min(...valores);
  const maior = Math.max(...valores);
  container.innerHTML = '';
  for (const v of valores) {
    const pixel = document.createElement('span');
    pixel.style.background = RAMPA[faixaDoValor(v, menor, maior)];
    pixel.title = numero(v);
    container.appendChild(pixel);
  }
}

function desenharFaixas(valores) {
  const menor = Math.min(...valores);
  const maior = Math.max(...valores);
  const contagem = new Array(8).fill(0);
  for (const v of valores) contagem[faixaDoValor(v, menor, maior)]++;
  const max = Math.max(...contagem, 1);
  const container = $('faixas');
  container.innerHTML = '';
  contagem.forEach((n, i) => {
    const coluna = document.createElement('div');
    coluna.className = 'faixa';
    coluna.innerHTML = `<small>${n}</small><div style="height:${Math.round((n / max) * 88)}%;background:${RAMPA[i]}"></div>`;
    container.appendChild(coluna);
  });
}

async function comparar() {
  const grande = ['50k', '100k'].includes(estado.tamanho);
  if (grande && !confirm('Comparar com ' + estado.tamanho + ' inclui o Insertion Sort em O(n²) e pode demorar. Continuar?')) return;
  carregando(true, 'Rodando os 3 métodos…');
  try {
    const r = await api('/api/comparar?' + new URLSearchParams({ tamanho: estado.tamanho, ordem: 'aleatorio', duplicidade: 'sem' }));
    $('info-comparar').textContent = `${r.ordem} · ${r.tamanho} registros · sem duplicidade · comparações em escala logarítmica`;
    const maiorLog = Math.max(...r.resultados.map((x) => Math.log10(Math.max(x.comparacoes, 1))));
    const area = $('barras-comparar');
    area.innerHTML = '';
    for (const x of r.resultados) {
      const largura = maiorLog > 0 ? (Math.log10(Math.max(x.comparacoes, 1)) / maiorLog) * 100 : 0;
      const linha = document.createElement('div');
      linha.className = 'linha-barra';
      linha.innerHTML = `<strong>${x.nome.replace(' Sort', '')}</strong>
        <div class="trilho"><div style="width:${largura.toFixed(1)}%;background:${CORES_METODO[x.metodo]}"></div></div>
        <span class="mono">${numero(x.comparacoes)} · ${ms(x.mediaMs)}</span>`;
      area.appendChild(linha);
    }
  } catch (e) {
    alert('Não foi possível comparar: ' + e.message);
  } finally {
    carregando(false);
  }
}

function carregando(ligado, texto) {
  $('carregando').hidden = !ligado;
  $('executar').disabled = ligado;
  $('botao-comparar').disabled = ligado;
  if (texto) $('carregando-texto').textContent = texto;
}

async function iniciar() {
  $('rampa').innerHTML = RAMPA.map((c) => `<span style="background:${c}"></span>`).join('');
  try {
    estado.opcoes = await api('/api/opcoes');
  } catch (e) {
    $('titulo').textContent = 'Servidor Java não respondeu. Rode a classe App e recarregue a página.';
    return;
  }
  criarSegmentos($('grupo-metodo'), estado.opcoes.metodos.map((m) => ({ codigo: m.codigo, nome: m.nome.replace(' Sort', ''), dica: m.complexidade })), 'metodo');
  $('grupo-tamanho').classList.add('tamanhos');
  criarSegmentos($('grupo-tamanho'), estado.opcoes.tamanhos.map((t) => ({ codigo: t, nome: t })), 'tamanho');
  const maiuscula = (t) => t.charAt(0).toUpperCase() + t.slice(1);
  criarSegmentos($('grupo-cenario'), estado.opcoes.metodos[0].cenarios.map((c) => ({ codigo: c.codigo, nome: maiuscula(c.nome.replace('Arquivo ', '')) })), 'cenario');
  atualizarBotoes();
  atualizarAviso();
  $('executar').addEventListener('click', executar);
  $('botao-comparar').addEventListener('click', comparar);
  $('baixar-pdf').addEventListener('click', () => window.print());
}

iniciar();
