package br.unip.aps.cenarios;

import br.unip.aps.algoritmos.Metodo;
import br.unip.aps.dados.Ordem;

public final class MapaCenarios {
    private MapaCenarios() { }

    public static ArquivoAlvo alvo(Metodo metodo, Cenario cenario) {
        switch (metodo) {
            case INSERTION:
                switch (cenario) {
                    case IDEAL:     return new ArquivoAlvo(Ordem.CRESCENTE, false);
                    case ALEATORIO: return new ArquivoAlvo(Ordem.ALEATORIO, false);
                    case PIOR:      return new ArquivoAlvo(Ordem.DECRESCENTE, false);
                }
                break;
            case MERGE:
                switch (cenario) {
                    case IDEAL:     return new ArquivoAlvo(Ordem.CRESCENTE, false);
                    case ALEATORIO: return new ArquivoAlvo(Ordem.ALEATORIO, false);
                    case PIOR:      return new ArquivoAlvo(Ordem.ALEATORIO, true);
                }
                break;
            case BUCKET:
                switch (cenario) {
                    case IDEAL:     return new ArquivoAlvo(Ordem.CRESCENTE, false);
                    case ALEATORIO: return new ArquivoAlvo(Ordem.ALEATORIO, false);
                    case PIOR:      return new ArquivoAlvo(Ordem.DECRESCENTE, true);
                }
                break;
        }
        throw new IllegalStateException("Combinação sem regra: " + metodo + " / " + cenario);
    }
}
