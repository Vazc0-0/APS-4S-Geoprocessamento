package br.unip.aps.relatorio;

import br.unip.aps.algoritmos.Metodo;
import br.unip.aps.benchmark.ResultadoBenchmark;
import br.unip.aps.cenarios.ArquivoAlvo;
import br.unip.aps.cenarios.Cenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeradorRelatorio {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public Relatorio gerar(Metodo metodo, Cenario cenario, ArquivoAlvo alvo, ResultadoBenchmark r) {
        String nome = metodo.algoritmo().nome();
        String titulo = nome + " · " + formatarTamanho(r.tamanho()) + " registros · " + cenario.rotulo();

        List<String> p = new ArrayList<>();
        String nivel;
        String veredito;

        switch (metodo) {
            case INSERTION -> {
                switch (cenario) {
                    case IDEAL -> {
                        nivel = "bom";
                        p.add("No arquivo crescente, cada registro já está no lugar certo. O Insertion Sort compara cada "
                                + "elemento só com o vizinho da esquerda e não precisa deslocar nada.");
                        p.add(String.format(PT_BR, "Foram %,d comparações para %,d registros: o trabalho cresce em linha "
                                + "reta com o tamanho, ou seja, O(n).", r.comparacoes(), r.tamanho()));
                    }
                    case ALEATORIO -> {
                        nivel = "medio";
                        p.add("Com os registros embaralhados, cada elemento precisa andar em média até a metade da parte "
                                + "já ordenada para achar seu lugar.");
                        p.add(String.format(PT_BR, "Foram %,d comparações e %,d movimentações: o trabalho cresce com o "
                                + "quadrado do tamanho, O(n²). Dobrar os dados quadruplica o tempo.",
                                r.comparacoes(), r.movimentacoes()));
                    }
                    default -> {
                        nivel = "ruim";
                        p.add("No arquivo decrescente, cada registro precisa ser levado até o início da lista. O Insertion "
                                + "Sort desloca o elemento uma posição por vez, então este é exatamente o seu pior caso.");
                        p.add(String.format(PT_BR, "Foram %,d movimentações, praticamente n²/2. É o cenário em que o "
                                + "método mais sofre.", r.movimentacoes()));
                    }
                }
            }
            case MERGE -> {
                nivel = cenario == Cenario.IDEAL ? "bom" : "medio";
                p.add("O Merge Sort divide os registros ao meio até sobrarem elementos isolados e depois intercala as "
                        + "metades em ordem. Esse trabalho é o mesmo em qualquer arranjo de entrada.");
                if (cenario == Cenario.IDEAL) {
                    p.add(String.format(PT_BR, "No arquivo crescente a intercalação termina mais cedo: foram %,d "
                            + "comparações. A vantagem existe, mas é pequena perto do aleatório.", r.comparacoes()));
                } else {
                    p.add(String.format(PT_BR, "Foram %,d comparações e %,d movimentações, dentro do esperado para "
                            + "O(n log n). Para o Merge não existe um arquivo realmente ruim: por isso o pior caso dele "
                            + "é o próprio aleatório.", r.comparacoes(), r.movimentacoes()));
                }
            }
            default -> {
                switch (cenario) {
                    case IDEAL -> {
                        nivel = "bom";
                        p.add("Os valores estão bem espalhados e já chegam em ordem. Cada balde recebe poucos registros e "
                                + "a ordenação dentro dele é imediata.");
                    }
                    case ALEATORIO -> {
                        nivel = "bom";
                        p.add("Os valores da cena estão espalhados de forma uniforme. Cada balde recebe uma fatia parecida "
                                + "dos registros, então a ordenação dentro de cada balde é curta.");
                    }
                    default -> {
                        nivel = "medio";
                        p.add("No arquivo decrescente com duplicidade, os registros chegam na ordem inversa e alguns "
                                + "valores se repetem, o que deixa os baldes um pouco mais cheios e a ordenação interna "
                                + "mais trabalhosa.");
                        p.add("O verdadeiro pior caso do Bucket (quase tudo caindo em um balde só) não existe nos "
                                + "arquivos do professor, porque os valores deles são bem espalhados.");
                    }
                }
                p.add(String.format(PT_BR, "Foram %,d comparações e %,d movimentações, perto de O(n + k).",
                        r.comparacoes(), r.movimentacoes()));
            }
        }

        veredito = switch (nivel) {
            case "bom" -> "Ótimo para este método";
            case "ruim" -> "Ruim para este método";
            default -> "Desempenho médio";
        };

        p.add(String.format(PT_BR, "Arquivo usado: %s. Tempo médio de %.3f ms em %d amostra(s) "
                + "(menor %.3f ms, maior %.3f ms).", alvo.descricao(), r.mediaMs(), r.temposMs().size(),
                r.menorMs(), r.maiorMs()));

        return new Relatorio(nivel, veredito, titulo, p);
    }

    private static String formatarTamanho(int n) {
        return n >= 1000 && n % 1000 == 0 ? (n / 1000) + "k" : String.valueOf(n);
    }
}
