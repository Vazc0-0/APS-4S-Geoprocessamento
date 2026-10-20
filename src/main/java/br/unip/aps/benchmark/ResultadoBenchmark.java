package br.unip.aps.benchmark;

import java.util.List;

public record ResultadoBenchmark(
        List<Double> temposMs,
        double mediaMs,
        double menorMs,
        double maiorMs,
        long comparacoes,
        long movimentacoes,
        int tamanho,
        int[] amostraAntes,
        int[] amostraDepois,
        int[] mapaAntes,
        int[] mapaDepois) {
}
