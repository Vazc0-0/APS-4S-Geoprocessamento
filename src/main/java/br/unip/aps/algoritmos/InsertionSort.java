package br.unip.aps.algoritmos;

public class InsertionSort implements AlgoritmoOrdenacao {
    @Override
    public String nome() {
        return "Insertion Sort";
    }

    @Override
    public String complexidade() {
        return "O(n²)";
    }

    @Override
    public void ordenar(int[] vetor, Contador contador) {
        ordenarTrecho(vetor, 0, vetor.length, contador);
    }

    public static void ordenarTrecho(int[] vetor, int inicio, int fim, Contador contador) {
        for (int i = inicio + 1; i < fim; i++) {
            int atual = vetor[i];
            int j = i - 1;

            while (j >= inicio) {
                contador.comparacao();
                if (vetor[j] <= atual) {
                    break;
                }
                vetor[j + 1] = vetor[j];
                contador.movimentacao();
                j--;
            }
            vetor[j + 1] = atual;
        }
    }
}
