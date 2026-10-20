package br.unip.aps.algoritmos;

public interface AlgoritmoOrdenacao {
    String nome();

    String complexidade();

    void ordenar(int[] vetor, Contador contador);
}
