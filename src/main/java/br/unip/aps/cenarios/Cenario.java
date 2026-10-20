package br.unip.aps.cenarios;

public enum Cenario {
    IDEAL("ideal", "Arquivo ideal"),
    ALEATORIO("aleatorio", "Arquivo aleatório"),
    PIOR("pior", "Pior caso");

    private final String codigo;
    private final String rotulo;

    Cenario(String codigo, String rotulo) {
        this.codigo = codigo;
        this.rotulo = rotulo;
    }

    public String codigo() {
        return codigo;
    }

    public String rotulo() {
        return rotulo;
    }

    public static Cenario porCodigo(String codigo) {
        for (Cenario c : values()) {
            if (c.codigo.equalsIgnoreCase(codigo)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Cenário desconhecido: " + codigo + ". Use ideal, aleatorio ou pior.");
    }
}
