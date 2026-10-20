# APS-4S-Geoprocessamento — Sistema de Análise de Ordenação

Sistema web que ordena os arquivos de dados do professor com **Insertion Sort**, **Merge Sort** e **Bucket Sort**, mede **só o tempo da ordenação** e gera um relatório explicando por que cada cenário foi bom ou ruim para o método escolhido.

A tela segue o tema de geoprocessamento: cada arquivo é tratado como uma "cena" de satélite e cada número vira um pixel colorido numa rampa estilo NDVI (do vermelho, valores baixos, ao verde, valores altos). Assim dá para ver a cena "bagunçada" antes e "organizada" depois da ordenação.

- **Backend:** Java puro (JDK 17+). Sem Maven, Spring ou bibliotecas externas: o servidor HTTP é o que já vem no JDK (`com.sun.net.httpserver`) e o JSON é gerado à mão.
- **Frontend:** HTML, CSS e JavaScript puros, na pasta `frontend`.

---

## Sumário

1. [Como rodar](#1-como-rodar)
2. [Visão geral da arquitetura](#2-visão-geral-da-arquitetura)
3. [Estrutura de pastas](#3-estrutura-de-pastas)
4. [Os algoritmos](#4-os-algoritmos)
5. [Cenários: ideal, aleatório e pior caso](#5-cenários-ideal-aleatório-e-pior-caso)
6. [Leitura dos dados](#6-leitura-dos-dados)
7. [Como o tempo é medido (Benchmark)](#7-como-o-tempo-é-medido-benchmark)
8. [Relatório explicativo](#8-relatório-explicativo)
9. [Servidor e API](#9-servidor-e-api)
10. [Frontend](#10-frontend)
11. [Autoteste dos algoritmos](#11-autoteste-dos-algoritmos)
12. [Problemas comuns](#12-problemas-comuns)

---

## 1. Como rodar

### O que instalar (uma vez)

1. **JDK 17 ou mais novo.** Sugestão: Eclipse Temurin 21 ([adoptium.net](https://adoptium.net)). Na instalação, marque a opção de configurar o `JAVA_HOME`.
2. **VS Code** com a extensão **Extension Pack for Java** (Microsoft). Ao abrir o projeto, o VS Code já sugere instalar.

### Preparar o projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/Vazc0-0/APS-4S-Geoprocessamento.git
   ```
2. No VS Code: **Arquivo → Abrir Pasta…** e escolha a pasta do projeto (a raiz, onde estão `src`, `frontend` e `dados`).
3. Extraia o **DadosAPS.zip** do professor dentro da pasta `dados` (detalhes em [`dados/LEIA-ME.md`](dados/LEIA-ME.md)). Os dados têm 158 MB e por isso **não vão para o GitHub**.
4. Espere a barra de status do VS Code terminar de carregar o projeto Java.

### Executar

1. Aba **Executar e Depurar** (`Ctrl+Shift+D`) → escolha **Iniciar sistema** → `F5`.
2. Abra **http://localhost:8080** no navegador.
3. Escolha método, tamanho e cenário e clique em **Executar análise**.

Para conferir se os algoritmos estão corretos, rode a configuração **Verificar algoritmos**.

### Sem VS Code (linha de comando)

Na raiz do projeto:

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin br.unip.aps.App
```

---

## 2. Visão geral da arquitetura

```
Navegador (frontend/)                         Java (src/main/java)
─────────────────────                         ────────────────────
index.html + app.js  ── GET /api/ordenar ──▶  Servidor ─▶ OrdenacaoApi
                                                             │
                                                             ├─ MapaCenarios    (qual pasta usar)
                                                             ├─ CatalogoArquivos (acha os .txt)
                                                             ├─ LeitorArquivo    (lê os números)
                                                             ├─ Benchmark        (ordena e mede)
                                                             └─ GeradorRelatorio (texto explicativo)
                     ◀────── JSON ───────────  Json
```

Fluxo de uma análise:

1. O usuário escolhe **método**, **tamanho** e **cenário** e clica em *Executar análise*.
2. O `app.js` chama `/api/ordenar?metodo=...&tamanho=...&cenario=...`.
3. O `Servidor` repassa para a `OrdenacaoApi`, que:
   - usa o `MapaCenarios` para descobrir qual pasta corresponde ao cenário daquele método;
   - usa o `CatalogoArquivos` para listar os 10 arquivos `.txt` (amostras 0 a 9) dessa pasta;
   - lê todos com o `LeitorArquivo` **antes** de começar a medir;
   - passa as amostras ao `Benchmark`, que ordena, mede e confere o resultado;
   - pede ao `GeradorRelatorio` o texto explicativo.
4. Tudo volta em JSON e o frontend desenha os mapas de pixels, métricas e relatório.

---

## 3. Estrutura de pastas

```
.
├── .vscode/                         configurações para rodar com F5
│   ├── launch.json                  "Iniciar sistema" e "Verificar algoritmos"
│   ├── settings.json                pasta de código-fonte (src/main/java) e de saída (bin)
│   └── extensions.json              recomenda o Extension Pack for Java
├── dados/                           extraia aqui o DadosAPS.zip (fora do Git)
│   └── LEIA-ME.md
├── frontend/
│   ├── index.html                   a tela
│   ├── css/estilo.css               visual (tema escuro "Raster NDVI")
│   ├── js/app.js                    chama a API e desenha os resultados
│   └── favicon.svg
└── src/main/java/br/unip/aps/
    ├── App.java                     ponto de entrada: sobe o servidor na porta 8080
    ├── algoritmos/
    │   ├── AlgoritmoOrdenacao.java  interface comum dos 3 métodos
    │   ├── InsertionSort.java
    │   ├── MergeSort.java
    │   ├── BucketSort.java          usa o Insertion dentro de cada balde
    │   ├── Contador.java            conta comparações e movimentações
    │   └── Metodo.java              enum com os 3 métodos
    ├── dados/
    │   ├── CatalogoArquivos.java    acha a pasta e os arquivos certos em dados/
    │   ├── LeitorArquivo.java       lê o .txt e devolve um int[]
    │   ├── Ordem.java               aleatório, crescente, decrescente, côncavo, convexo
    │   └── Tamanho.java             10, 50, 100, 500, 1k, 5k, 10k, 50k, 100k
    ├── cenarios/
    │   ├── Cenario.java             ideal, aleatório, pior caso
    │   ├── ArquivoAlvo.java         ordem + com/sem duplicidade
    │   └── MapaCenarios.java        qual pasta é o ideal/aleatório/pior de cada método
    ├── benchmark/
    │   ├── Benchmark.java           aquecimento, cronômetro, conferência e contagem
    │   └── ResultadoBenchmark.java  dados de uma medição
    ├── relatorio/
    │   ├── GeradorRelatorio.java    os 9 textos explicativos (3 métodos × 3 cenários)
    │   └── Relatorio.java
    ├── api/OrdenacaoApi.java        regras de cada rota da API
    ├── servidor/
    │   ├── Servidor.java            servidor HTTP + entrega dos arquivos do frontend
    │   └── Json.java                gerador de JSON simples
    └── ferramentas/
        └── VerificarAlgoritmos.java autoteste dos algoritmos
```

---

## 4. Os algoritmos

Todos implementam a interface **`AlgoritmoOrdenacao`**, que tem três métodos:

| Método | O que faz |
| --- | --- |
| `nome()` | nome exibido na tela, ex.: `"Merge Sort"` |
| `complexidade()` | complexidade de referência, ex.: `"O(n log n)"` |
| `ordenar(int[] vetor, Contador contador)` | ordena o vetor em ordem crescente, **no próprio vetor** |

Graças à interface, o `Benchmark` e o relatório não precisam saber qual algoritmo estão usando. Para adicionar outro método (ex.: Quick Sort), basta criar uma classe que implemente a interface e registrá-la no enum **`Metodo`**, que liga o código usado na URL (`insertion`, `merge`, `bucket`) ao objeto do algoritmo.

### Insertion Sort — `O(n²)`

Funciona como organizar cartas na mão: pega um elemento por vez e o empurra para a esquerda até achar o lugar certo dele.

- **Melhor caso (crescente):** cada elemento compara só com o vizinho e já para → `O(n)`.
- **Pior caso (decrescente):** todo elemento precisa andar até o começo → `O(n²)`.

O método `ordenarTrecho(vetor, inicio, fim, contador)` é `public static` porque o Bucket Sort o reaproveita para ordenar cada balde.

### Merge Sort — `O(n log n)`

"Dividir para conquistar": divide o vetor ao meio recursivamente até sobrarem pedaços de 1 elemento e depois **intercala** as metades de volta, sempre em ordem.

- Faz praticamente o mesmo trabalho em qualquer entrada, por isso é `O(n log n)` sempre.
- Usa um vetor auxiliar do mesmo tamanho, criado **uma única vez** no início (e não a cada intercalação).
- O ponto médio é calculado com `(inicio + fim) >>> 1`, que evita estouro de `int`.
- Na intercalação, se sobrar algo na metade direita não é preciso copiar: já está no lugar certo.

### Bucket Sort — `O(n + k)`

Separa os números em "baldes" de acordo com o valor (os menores no primeiro balde, os maiores no último), ordena cada balde e junta tudo na ordem dos baldes.

- Usa **um balde por elemento** (`k = n`). Como os dados do professor são bem espalhados, cada balde fica com 1 ou 2 números, e o custo fica perto de `O(n + k)`.
- Se muitos números caíssem no mesmo balde, ele viraria praticamente um Insertion Sort, `O(n²)`.
- Dentro de cada balde usa o **Insertion Sort**, que é ótimo para listas pequenas.
- Não usa lista de listas (`ArrayList<ArrayList>`), que seria pesada com 100 mil baldes. Em vez disso faz:
  1. acha o menor e o maior valor (se forem iguais, já está ordenado);
  2. **conta** quantos números vão para cada balde;
  3. faz a **soma acumulada**, que diz em que posição cada balde começa;
  4. coloca cada número direto no seu balde, num vetor de saída;
  5. ordena com Insertion só os baldes com 2 ou mais elementos;
  6. copia o resultado de volta para o vetor original.
- O índice do balde é uma regra de três: `(valor - menor) * baldes / faixa`, calculada em `long` para não estourar com valores grandes (os arquivos de 100k chegam a ~10 milhões).

### Contador

O **`Contador`** soma as **comparações** e **movimentações** feitas pelo algoritmo. Os campos são `long` porque o Insertion com 100k passa de 4 bilhões de operações.

Existe também o `Contador.DESLIGADO`, uma versão que não conta nada. Ele é usado enquanto o tempo está sendo medido, para que a contagem não deixe o tempo maior do que o real.

---

## 5. Cenários: ideal, aleatório e pior caso

O usuário escolhe um **cenário** (`Cenario`: ideal, aleatório ou pior caso), mas qual pasta de dados cada cenário usa depende do método. Essa decisão fica toda em **`MapaCenarios`**:

| Método | Ideal | Aleatório | Pior caso |
| --- | --- | --- | --- |
| Insertion | Crescente, sem duplicidade | Aleatório, sem duplicidade | Decrescente, sem duplicidade |
| Merge | Crescente, sem duplicidade | Aleatório, sem duplicidade | Aleatório, com duplicidade |
| Bucket | Crescente, sem duplicidade | Aleatório, sem duplicidade | Decrescente, com duplicidade |

Justificativas:

- **Insertion:** crescente já está ordenado (só confere e termina); decrescente obriga todos a andarem até o começo.
- **Merge:** faz o mesmo trabalho em qualquer ordem, então o "pior caso" dele acaba sendo o próprio aleatório.
- **Bucket:** decrescente com duplicidade deixa os baldes um pouco mais cheios e dá mais trabalho ao Insertion interno.

Cada combinação é um **`ArquivoAlvo`** (ordem + com/sem duplicidade), que também gera o texto exibido na tela, ex.: `Crescente · sem duplicidade`. Para mudar a regra, basta editar `MapaCenarios.java`: tela e relatório acompanham automaticamente.

---

## 6. Leitura dos dados

Depois de extrair o zip, os arquivos ficam assim:

```
dados/DadosAPS/10k/Sem Duplicidade/Aleatório/dtaleat10kuni0.txt ... uni9.txt
```

Para achar um arquivo são necessárias três informações: **tamanho**, **duplicidade** e **ordem**.

- **`Tamanho`** guarda os tamanhos válidos (`10` até `100k`, os nomes das pastas), valida o valor recebido da tela e converte `"10k"` em `10000`.
- **`Ordem`** lista as 5 ordenações das pastas (aleatório, crescente, decrescente, côncavo e convexo). Cada uma tem prefixos de nome de pasta (`"aleat"`, `"crescente"`...). A comparação é feita pelo **começo** do nome porque no zip o acento de "Aleatório" às vezes vem corrompido, e o professor escreveu "Descrescente" com S — por isso os dois jeitos são aceitos.
- **`CatalogoArquivos`** monta o caminho:
  - funciona tanto com `dados/DadosAPS/...` quanto com `dados/...` direto;
  - normaliza os nomes de pasta (remove acentos, espaços e deixa minúsculo: `"Com Duplicidade"` → `"comduplicidade"`);
  - devolve os `.txt` da pasta **ordenados por nome**, garantindo a sequência 0, 1, 2… 9;
  - lança uma mensagem clara se a pasta não existir.
- **`LeitorArquivo`** lê o `.txt` (um número por linha, codificação ISO-8859-1) e devolve um `int[]`.
  - Aplica `trim()` em cada linha: os arquivos foram gerados no Windows e, a partir da 2ª linha, têm um espaço antes do número (ex.: `" 421"`). Sem o `trim()` o `parseInt` falha.
  - Linhas em branco (normalmente a última) são ignoradas.

---

## 7. Como o tempo é medido (Benchmark)

O enunciado pede para medir **apenas o tempo de ordenação**. O `Benchmark` faz isso assim:

1. **Leitura fora do cronômetro:** os arquivos já chegam carregados na memória pela `OrdenacaoApi`.
2. **Aquecimento da JVM:** na primeira execução, o Java ainda não otimizou o código (compilador JIT), então a primeira amostra sairia bem mais lenta e distorceria a média. Antes de medir, o algoritmo roda 20 vezes em vetores aleatórios de 2.000 números (semente fixa 42) e, para tamanhos menores que 50k, mais 3 vezes com a própria amostra. Esses resultados são descartados.
3. **Medição de cada amostra:** o vetor é **copiado** (senão a próxima rodada pegaria um vetor já ordenado), e só a chamada `ordenar()` fica entre dois `System.nanoTime()`. O resultado é convertido para milissegundos.
4. **Conferência:** depois de cada amostra, `estaOrdenado()` verifica se cada elemento é menor ou igual ao seguinte. Se não for, gera erro.
5. **Amostras grandes:** a partir de **50k** registros são medidas **3 amostras** em vez de 10, porque o Insertion leva vários segundos em cada.
6. **Contagem separada:** comparações e movimentações são contadas numa rodada extra, cujo tempo é ignorado.
7. **Resultado:** média, menor e maior tempo, contagens, os 20 primeiros números antes/depois e 200 valores **espalhados** pelo vetor inteiro (não só os primeiros) para o mapa de pixels da tela.

Tudo isso vai num **`ResultadoBenchmark`** (um `record` Java, classe só de dados).

---

## 8. Relatório explicativo

O **`GeradorRelatorio`** monta o texto que explica **por que** o resultado foi bom ou ruim. São 9 combinações (3 métodos × 3 cenários), cada uma com um texto pronto onde são encaixados os números reais da medição (comparações, movimentações, tempos). Os números são formatados no padrão brasileiro (`49.995.000`).

Cada relatório (`Relatorio`) tem:

- **nível:** `bom`, `medio` ou `ruim` — define a cor do selo na tela (verde, amarelo ou laranja);
- **veredito:** "Ótimo para este método", "Desempenho médio" ou "Ruim para este método";
- **título:** ex.: `Merge Sort · 10k registros · Pior caso`;
- **parágrafos:** a explicação, terminando sempre com o arquivo usado e os tempos (médio, menor e maior).

---

## 9. Servidor e API

### Servidor

O **`Servidor`** usa o `HttpServer` do próprio JDK, escutando em `localhost:8080` com até 4 requisições simultâneas. Ele faz duas coisas:

- URLs que começam com **`/api/`** → chama a `OrdenacaoApi` e responde em JSON;
- qualquer outra URL → entrega o arquivo correspondente da pasta `frontend` (`/` vira `/index.html`), com o `Content-Type` certo para HTML, CSS, JS, SVG, PNG e JPG.

Por segurança, ele recusa qualquer caminho que tente sair da pasta `frontend` (ex.: `/../src/...`).

Erros viram respostas JSON `{"erro": "..."}` com o status adequado:

| Situação | Status |
| --- | --- |
| Rota inexistente | 404 |
| Parâmetro inválido (ex.: `metodo=xyz`) | 400 |
| Pasta/arquivo de dados não encontrado | 404 |
| Qualquer outro erro (stack trace no terminal) | 500 |

### Json

A classe **`Json`** gera JSON sem bibliotecas externas, de forma encadeada:

```java
Json.objeto().com("nome", "Merge").com("tempo", 1.5).toString()
// {"nome":"Merge","tempo":1.5}
```

Suporta objetos aninhados, textos (com escape de aspas, barras e quebras de linha), números, booleanos, `int[]` e listas. Mantém a ordem dos campos (`LinkedHashMap`).

### Rotas

| Rota | O que faz | Exemplo |
| --- | --- | --- |
| `GET /api/opcoes` | métodos, tamanhos, ordens e o arquivo de cada cenário, para montar os botões da tela | `/api/opcoes` |
| `GET /api/ordenar` | roda **1 método** em **1 cenário** e devolve tempos, contagens, mapas e relatório | `/api/ordenar?metodo=insertion&tamanho=10k&cenario=pior` |
| `GET /api/comparar` | roda **os 3 métodos no mesmo arquivo** para comparação direta | `/api/comparar?tamanho=10k&ordem=aleatorio&duplicidade=sem` |

Parâmetros aceitos:

- `metodo`: `insertion`, `merge`, `bucket`
- `tamanho`: `10`, `50`, `100`, `500`, `1k`, `5k`, `10k`, `50k`, `100k`
- `cenario`: `ideal`, `aleatorio`, `pior`
- `ordem` (só em `/comparar`, padrão `aleatorio`): `aleatorio`, `crescente`, `decrescente`, `concavo`, `convexo`
- `duplicidade` (só em `/comparar`, padrão `sem`): `com`, `sem`

Dá para testar direto no navegador colando as URLs depois de `http://localhost:8080`.

---

## 10. Frontend

O frontend só pede dados à API e os desenha; **toda ordenação e medição acontece no Java**.

- **`index.html`:** barra de parâmetros (método, tamanho, cenário), três painéis (entrada, distribuição, saída), legenda de cores, comparação dos 3 métodos, métricas, relatório e lista "antes e depois".
- **`js/app.js`:**
  - ao abrir, chama `/api/opcoes` e cria os botões dinamicamente (nada fica fixo no HTML); o *tooltip* de cada cenário mostra qual arquivo será usado;
  - mostra um aviso quando o Insertion vai rodar em 50k/100k fora do cenário ideal, porque pode demorar;
  - **Executar análise** chama `/api/ordenar` e desenha:
    - **mapas de pixels** (antes e depois): 200 valores, cada um pintado numa rampa de 8 cores do vermelho ao verde, conforme sua faixa de valor;
    - **distribuição/baldes:** histograma de quantos valores caem em cada uma das 8 faixas;
    - **métricas:** tempo médio, complexidade, comparações e movimentações;
    - **relatório** com selo colorido e os 20 primeiros números antes/depois;
  - **Comparar no arquivo aleatório** chama `/api/comparar` e desenha barras em **escala logarítmica** (senão o Insertion esmagaria os outros dois no gráfico);
  - mostra uma tela de "carregando" enquanto espera o Java.
- **`css/estilo.css`:** tema escuro "Raster NDVI", layout responsivo (funciona no celular) e um modo de impressão que mostra só o relatório.
- **Baixar PDF:** abre a impressão do navegador; escolha "Salvar como PDF".

---

## 11. Autoteste dos algoritmos

A classe **`VerificarAlgoritmos`** testa os 3 algoritmos com vários vetores: vazio, 1 elemento, 2 elementos invertidos, todos iguais, decrescente, crescente, valores extremos (0 e 9.999.999) e vetores aleatórios de 1.000 e 5.000 números. O resultado de cada um é comparado com o `Arrays.sort` do Java (usado só como gabarito).

Rode com a configuração **Verificar algoritmos** no VS Code, ou:

```bash
java -cp bin br.unip.aps.ferramentas.VerificarAlgoritmos
```

Saída esperada:

```
OK: Insertion Sort
OK: Merge Sort
OK: Bucket Sort
Todos os algoritmos ordenaram corretamente.
```

Sempre rode depois de alterar algum algoritmo.

---

## 12. Problemas comuns

- **"Pasta não encontrada":** o zip não foi extraído dentro de `dados`, ou o programa está rodando a partir de outra pasta. Abra no VS Code a pasta raiz do projeto, não a pasta acima dela.
- **"Pasta 'frontend' não encontrada":** mesmo motivo — o programa precisa rodar a partir da raiz do projeto.
- **Porta 8080 ocupada:** mude a constante `PORTA` em `App.java` (ex.: `8081`).
- **Insertion demorando em 50k/100k:** é esperado, é `O(n²)`. O sistema já reduz para 3 amostras nesses tamanhos.
- **"Servidor Java não respondeu":** a classe `App` não está rodando. Inicie com `F5` e recarregue a página.
