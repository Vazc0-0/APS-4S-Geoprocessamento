package br.unip.aps.ferramentas;

import br.unip.aps.algoritmos.Contador;
import br.unip.aps.algoritmos.Metodo;
import br.unip.aps.benchmark.Benchmark;

import java.util.Arrays;
import java.util.Random;

public class VerificarAlgoritmos {
    public static void main(String[] args) {
        Random aleatorio = new Random(7);
        int[][] casos = {
                {},
                {5},
                {2, 1},
                {3, 3, 3, 3},
                {5, 4, 3, 2, 1},
                {1, 2, 3, 4, 5},
                {0, 9_999_999, 0, 9_999_999, 42},
                aleatorio.ints(1_000, 0, 100).toArray(),
                aleatorio.ints(5_000, 0, 500_000).toArray(),
        };

        int falhas = 0;
        for (Metodo metodo : Metodo.values()) {
            for (int[] caso : casos) {
                int[] esperado = caso.clone();
                Arrays.sort(esperado);
                int[] obtido = caso.clone();
                metodo.algoritmo().ordenar(obtido, new Contador());
                if (!Arrays.equals(esperado, obtido) || !Benchmark.estaOrdenado(obtido)) {
                    falhas++;
                    System.out.println("FALHOU: " + metodo.algoritmo().nome() + " com " + caso.length + " elementos");
                }
            }
            System.out.println("OK: " + metodo.algoritmo().nome());
        }
        System.out.println(falhas == 0 ? "Todos os algoritmos ordenaram corretamente." : falhas + " falha(s).");
        if (falhas > 0) {
            System.exit(1);
        }
    }
}
