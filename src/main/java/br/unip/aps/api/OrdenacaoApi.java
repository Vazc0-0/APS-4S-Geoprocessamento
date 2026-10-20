package br.unip.aps.api;

import br.unip.aps.algoritmos.Metodo;
import br.unip.aps.benchmark.Benchmark;
import br.unip.aps.benchmark.ResultadoBenchmark;
import br.unip.aps.cenarios.ArquivoAlvo;
import br.unip.aps.cenarios.Cenario;
import br.unip.aps.cenarios.MapaCenarios;
import br.unip.aps.dados.CatalogoArquivos;
import br.unip.aps.dados.LeitorArquivo;
import br.unip.aps.dados.Ordem;
import br.unip.aps.dados.Tamanho;
import br.unip.aps.relatorio.GeradorRelatorio;
import br.unip.aps.relatorio.Relatorio;
import br.unip.aps.servidor.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdenacaoApi {
    private final CatalogoArquivos catalogo;
    private final Benchmark benchmark = new Benchmark();
    private final GeradorRelatorio gerador = new GeradorRelatorio();

    public OrdenacaoApi(CatalogoArquivos catalogo) {
        this.catalogo = catalogo;
    }

    public Json opcoes() {
        List<Json> metodos = new ArrayList<>();
        for (Metodo m : Metodo.values()) {
            List<Json> cenarios = new ArrayList<>();
            for (Cenario c : Cenario.values()) {
                ArquivoAlvo alvo = MapaCenarios.alvo(m, c);
                cenarios.add(Json.objeto().com("codigo", c.codigo()).com("nome", c.rotulo())
                        .com("arquivo", alvo.descricao()));
            }
            metodos.add(Json.objeto().com("codigo", m.codigo()).com("nome", m.algoritmo().nome())
                    .com("complexidade", m.algoritmo().complexidade()).com("cenarios", cenarios));
        }
        List<Json> ordens = new ArrayList<>();
        for (Ordem o : Ordem.values()) {
            ordens.add(Json.objeto().com("codigo", o.codigo()).com("nome", o.rotulo()));
        }
        return Json.objeto().com("metodos", metodos).com("tamanhos", Tamanho.TODOS).com("ordens", ordens);
    }

    public Json ordenar(Map<String, String> parametros) throws IOException {
        Metodo metodo = Metodo.porCodigo(obrigatorio(parametros, "metodo"));
        String tamanho = Tamanho.validar(obrigatorio(parametros, "tamanho"));
        Cenario cenario = Cenario.porCodigo(obrigatorio(parametros, "cenario"));

        ArquivoAlvo alvo = MapaCenarios.alvo(metodo, cenario);
        List<Path> arquivos = catalogo.amostras(tamanho, alvo.comDuplicidade(), alvo.ordem());
        ResultadoBenchmark r = benchmark.executar(metodo.algoritmo(), carregar(arquivos));
        Relatorio relatorio = gerador.gerar(metodo, cenario, alvo, r);

        return resultadoJson(metodo, r)
                .com("cenario", cenario.codigo())
                .com("arquivo", alvo.descricao())
                .com("pasta", tamanho + " / " + alvo.descricao() + " / " + arquivos.size() + " arquivos")
                .com("relatorio", Json.objeto()
                        .com("nivel", relatorio.nivel())
                        .com("veredito", relatorio.veredito())
                        .com("titulo", relatorio.titulo())
                        .com("paragrafos", relatorio.paragrafos()));
    }

    public Json comparar(Map<String, String> parametros) throws IOException {
        String tamanho = Tamanho.validar(obrigatorio(parametros, "tamanho"));
        Ordem ordem = Ordem.porCodigo(parametros.getOrDefault("ordem", "aleatorio"));
        boolean comDuplicidade = "com".equalsIgnoreCase(parametros.getOrDefault("duplicidade", "sem"));

        List<int[]> amostras = carregar(catalogo.amostras(tamanho, comDuplicidade, ordem));
        List<Json> resultados = new ArrayList<>();
        for (Metodo m : Metodo.values()) {
            resultados.add(resultadoJson(m, benchmark.executar(m.algoritmo(), amostras)));
        }
        return Json.objeto()
                .com("tamanho", tamanho)
                .com("ordem", ordem.rotulo())
                .com("duplicidade", comDuplicidade ? "com" : "sem")
                .com("resultados", resultados);
    }

    private Json resultadoJson(Metodo metodo, ResultadoBenchmark r) {
        return Json.objeto()
                .com("metodo", metodo.codigo())
                .com("nome", metodo.algoritmo().nome())
                .com("complexidade", metodo.algoritmo().complexidade())
                .com("tamanho", r.tamanho())
                .com("amostras", r.temposMs().size())
                .com("temposMs", r.temposMs())
                .com("mediaMs", r.mediaMs())
                .com("menorMs", r.menorMs())
                .com("maiorMs", r.maiorMs())
                .com("comparacoes", r.comparacoes())
                .com("movimentacoes", r.movimentacoes())
                .com("antes", r.amostraAntes())
                .com("depois", r.amostraDepois())
                .com("mapaAntes", r.mapaAntes())
                .com("mapaDepois", r.mapaDepois());
    }

    private List<int[]> carregar(List<Path> arquivos) throws IOException {
        List<int[]> amostras = new ArrayList<>();
        for (Path arquivo : arquivos) {
            amostras.add(LeitorArquivo.ler(arquivo));
        }
        return amostras;
    }

    private static String obrigatorio(Map<String, String> parametros, String nome) {
        String valor = parametros.get(nome);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Parâmetro obrigatório ausente: " + nome);
        }
        return valor;
    }
}
