package br.unip.aps.benchmark;

import br.unip.aps.algoritmos.AlgoritmoOrdenacao;
import br.unip.aps.algoritmos.Contador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Benchmark {
    public static final int LIMITE_GRANDE = 50_000;
    public static final int AMOSTRAS_GRANDES = 3;

    private static final int TAMANHO_EXIBICAO = 20;
    private static final int PIXELS_MAPA = 200;

    public ResultadoBenchmark executar(AlgoritmoOrdenacao algoritmo, List<int[]> amostras) {
        if (amostras.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma amostra para medir.");
        }
        int tamanho = amostras.get(0).length;
        List<int[]> usadas = tamanho >= LIMITE_GRANDE && amostras.size() > AMOSTRAS_GRANDES
                ? amostras.subList(0, AMOSTRAS_GRANDES)
                : amostras;

        aquecer(algoritmo, usadas.get(0));

        List<Double> tempos = new ArrayList<>();
        for (int[] original : usadas) {
            int[] copia = Arrays.copyOf(original, original.length);

            long inicio = System.nanoTime();
            algoritmo.ordenar(copia, Contador.DESLIGADO);
            long fim = System.nanoTime();

            if (!estaOrdenado(copia)) {
                throw new IllegalStateException(algoritmo.nome() + " não ordenou corretamente.");
            }
            tempos.add((fim - inicio) / 1_000_000.0);
        }

        int[] paraContar = Arrays.copyOf(usadas.get(0), tamanho);
        Contador contador = new Contador();
        algoritmo.ordenar(paraContar, contador);

        double media = tempos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double menor = tempos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maior = tempos.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        return new ResultadoBenchmark(tempos, media, menor, maior,
                contador.getComparacoes(), contador.getMovimentacoes(), tamanho,
                Arrays.copyOf(usadas.get(0), Math.min(TAMANHO_EXIBICAO, tamanho)),
                Arrays.copyOf(paraContar, Math.min(TAMANHO_EXIBICAO, tamanho)),
                espalhar(usadas.get(0), PIXELS_MAPA),
                espalhar(paraContar, PIXELS_MAPA));
    }

    private static int[] espalhar(int[] vetor, int quantidade) {
        int q = Math.min(quantidade, vetor.length);
        int[] resultado = new int[q];
        for (int i = 0; i < q; i++) {
            resultado[i] = vetor[(int) ((long) i * vetor.length / q)];
        }
        return resultado;
    }

    private void aquecer(AlgoritmoOrdenacao algoritmo, int[] amostra) {
        Random aleatorio = new Random(42);
        for (int rodada = 0; rodada < 20; rodada++) {
            int[] vetor = aleatorio.ints(2_000, 0, 100_000).toArray();
            algoritmo.ordenar(vetor, Contador.DESLIGADO);
        }
        if (amostra.length < LIMITE_GRANDE) {
            for (int rodada = 0; rodada < 3; rodada++) {
                algoritmo.ordenar(Arrays.copyOf(amostra, amostra.length), Contador.DESLIGADO);
            }
        }
    }

    public static boolean estaOrdenado(int[] vetor) {
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i - 1] > vetor[i]) {
                return false;
            }
        }
        return true;
    }
}
