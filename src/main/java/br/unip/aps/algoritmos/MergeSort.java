package br.unip.aps.algoritmos;

public class MergeSort implements AlgoritmoOrdenacao {
    @Override
    public String nome() {
        return "Merge Sort";
    }

    @Override
    public String complexidade() {
        return "O(n log n)";
    }

    @Override
    public void ordenar(int[] vetor, Contador contador) {
        if (vetor.length < 2) {
            return;
        }
        int[] auxiliar = new int[vetor.length];
        dividir(vetor, auxiliar, 0, vetor.length - 1, contador);
    }

    private void dividir(int[] vetor, int[] auxiliar, int inicio, int fim, Contador contador) {
        if (inicio >= fim) {
            return;
        }
        int meio = (inicio + fim) >>> 1;
        dividir(vetor, auxiliar, inicio, meio, contador);
        dividir(vetor, auxiliar, meio + 1, fim, contador);
        intercalar(vetor, auxiliar, inicio, meio, fim, contador);
    }

    private void intercalar(int[] vetor, int[] auxiliar, int inicio, int meio, int fim, Contador contador) {
        System.arraycopy(vetor, inicio, auxiliar, inicio, fim - inicio + 1);
        contador.movimentacoes(fim - inicio + 1);

        int esquerda = inicio;
        int direita = meio + 1;
        int destino = inicio;

        while (esquerda <= meio && direita <= fim) {
            contador.comparacao();
            if (auxiliar[esquerda] <= auxiliar[direita]) {
                vetor[destino++] = auxiliar[esquerda++];
            } else {
                vetor[destino++] = auxiliar[direita++];
            }
            contador.movimentacao();
        }
        while (esquerda <= meio) {
            vetor[destino++] = auxiliar[esquerda++];
            contador.movimentacao();
        }
    }
}
