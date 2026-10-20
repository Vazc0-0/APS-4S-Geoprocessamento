package br.unip.aps.dados;

import java.util.List;

public final class Tamanho {
    public static final List<String> TODOS =
            List.of("10", "50", "100", "500", "1k", "5k", "10k", "50k", "100k");

    private Tamanho() { }

    public static String validar(String tamanho) {
        if (tamanho == null || !TODOS.contains(tamanho.toLowerCase())) {
            throw new IllegalArgumentException("Tamanho inválido: " + tamanho + ". Use um destes: " + TODOS);
        }
        return tamanho.toLowerCase();
    }

    public static int emNumero(String tamanho) {
        String t = validar(tamanho);
        return t.endsWith("k")
                ? Integer.parseInt(t.substring(0, t.length() - 1)) * 1000
                : Integer.parseInt(t);
    }
}
