package br.unip.aps.algoritmos;

public class Contador {
    public static final Contador DESLIGADO = new Contador() {
        @Override public void comparacao() { }
        @Override public void movimentacao() { }
        @Override public void movimentacoes(long quantidade) { }
    };

    private long comparacoes;
    private long movimentacoes;

    public void comparacao() {
        comparacoes++;
    }

    public void movimentacao() {
        movimentacoes++;
    }

    public void movimentacoes(long quantidade) {
        movimentacoes += quantidade;
    }

    public long getComparacoes() {
        return comparacoes;
    }

    public long getMovimentacoes() {
        return movimentacoes;
    }
}
