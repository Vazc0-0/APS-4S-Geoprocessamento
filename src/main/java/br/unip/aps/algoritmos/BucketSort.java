package br.unip.aps.algoritmos;

public class BucketSort implements AlgoritmoOrdenacao {
    @Override
    public String nome() {
        return "Bucket Sort";
    }

    @Override
    public String complexidade() {
        return "O(n + k)";
    }

    @Override
    public void ordenar(int[] vetor, Contador contador) {
        int n = vetor.length;
        if (n < 2) {
            return;
        }

        int menor = vetor[0];
        int maior = vetor[0];
        for (int valor : vetor) {
            if (valor < menor) menor = valor;
            if (valor > maior) maior = valor;
        }
        if (menor == maior) {
            return;
        }

        int baldes = n;
        long faixa = (long) maior - menor + 1;

        int[] inicioBalde = new int[baldes + 1];
        for (int valor : vetor) {
            inicioBalde[indiceBalde(valor, menor, faixa, baldes) + 1]++;
        }

        for (int b = 0; b < baldes; b++) {
            inicioBalde[b + 1] += inicioBalde[b];
        }

        int[] saida = new int[n];
        int[] proximaPosicao = inicioBalde.clone();
        for (int valor : vetor) {
            int b = indiceBalde(valor, menor, faixa, baldes);
            saida[proximaPosicao[b]++] = valor;
        }
        contador.movimentacoes(n);

        for (int b = 0; b < baldes; b++) {
            if (inicioBalde[b + 1] - inicioBalde[b] > 1) {
                InsertionSort.ordenarTrecho(saida, inicioBalde[b], inicioBalde[b + 1], contador);
            }
        }

        System.arraycopy(saida, 0, vetor, 0, n);
        contador.movimentacoes(n);
    }

    private static int indiceBalde(int valor, int menor, long faixa, int baldes) {
        return (int) (((long) valor - menor) * baldes / faixa);
    }
}
