package br.unip.aps.dados;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class LeitorArquivo {
    private LeitorArquivo() { }

    public static int[] ler(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.ISO_8859_1);
        int[] valores = new int[linhas.size()];
        int quantidade = 0;

        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i).trim();
            if (linha.isEmpty()) {
                continue;
            }
            try {
                valores[quantidade++] = Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                throw new IOException("Linha " + (i + 1) + " de " + arquivo.getFileName()
                        + " não é um número: \"" + linha + "\"", e);
            }
        }

        return quantidade == valores.length ? valores : java.util.Arrays.copyOf(valores, quantidade);
    }
}
