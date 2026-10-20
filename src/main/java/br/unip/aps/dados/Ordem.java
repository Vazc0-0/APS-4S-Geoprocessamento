package br.unip.aps.dados;

public enum Ordem {
    ALEATORIO("aleatorio", "Aleatório", "aleat"),
    CRESCENTE("crescente", "Crescente", "crescente"),
    DECRESCENTE("decrescente", "Decrescente", "descrescente", "decrescente"),
    CONCAVO("concavo", "Côncavo (desce e sobe)", "concavo"),
    CONVEXO("convexo", "Convexo (sobe e desce)", "convexo");

    private final String codigo;
    private final String rotulo;
    private final String[] prefixosPasta;

    Ordem(String codigo, String rotulo, String... prefixosPasta) {
        this.codigo = codigo;
        this.rotulo = rotulo;
        this.prefixosPasta = prefixosPasta;
    }

    public String codigo() {
        return codigo;
    }

    public String rotulo() {
        return rotulo;
    }

    public boolean correspondePasta(String nomeNormalizado) {
        for (String prefixo : prefixosPasta) {
            if (nomeNormalizado.startsWith(prefixo)) {
                return true;
            }
        }
        return false;
    }

    public static Ordem porCodigo(String codigo) {
        for (Ordem o : values()) {
            if (o.codigo.equalsIgnoreCase(codigo)) {
                return o;
            }
        }
        throw new IllegalArgumentException("Ordem desconhecida: " + codigo);
    }
}
