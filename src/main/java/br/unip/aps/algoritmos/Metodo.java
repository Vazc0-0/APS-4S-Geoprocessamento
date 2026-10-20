package br.unip.aps.algoritmos;

public enum Metodo {
    INSERTION("insertion", new InsertionSort()),
    MERGE("merge", new MergeSort()),
    BUCKET("bucket", new BucketSort());

    private final String codigo;
    private final AlgoritmoOrdenacao algoritmo;

    Metodo(String codigo, AlgoritmoOrdenacao algoritmo) {
        this.codigo = codigo;
        this.algoritmo = algoritmo;
    }

    public String codigo() {
        return codigo;
    }

    public AlgoritmoOrdenacao algoritmo() {
        return algoritmo;
    }

    public static Metodo porCodigo(String codigo) {
        for (Metodo m : values()) {
            if (m.codigo.equalsIgnoreCase(codigo)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Método desconhecido: " + codigo + ". Use insertion, merge ou bucket.");
    }
}
