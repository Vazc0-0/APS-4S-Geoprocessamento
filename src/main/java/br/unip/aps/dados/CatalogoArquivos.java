package br.unip.aps.dados;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CatalogoArquivos {
    private final Path raiz;

    public CatalogoArquivos(Path pastaDados) {
        Path comSubpasta = pastaDados.resolve("DadosAPS");
        this.raiz = Files.isDirectory(comSubpasta) ? comSubpasta : pastaDados;
    }

    public Path raiz() {
        return raiz;
    }

    public List<Path> amostras(String tamanho, boolean comDuplicidade, Ordem ordem) throws IOException {
        Path pastaTamanho = raiz.resolve(Tamanho.validar(tamanho));
        if (!Files.isDirectory(pastaTamanho)) {
            throw new IOException("Pasta não encontrada: " + pastaTamanho.toAbsolutePath()
                    + ". Extraia o DadosAPS.zip dentro da pasta 'dados' do projeto.");
        }

        Path pastaDuplicidade = acharSubpasta(pastaTamanho,
                nome -> nome.startsWith(comDuplicidade ? "com" : "sem"),
                comDuplicidade ? "Com Duplicidade" : "Sem Duplicidade");
        Path pastaOrdem = acharSubpasta(pastaDuplicidade, ordem::correspondePasta, ordem.rotulo());

        List<Path> arquivos = new ArrayList<>();
        try (Stream<Path> itens = Files.list(pastaOrdem)) {
            itens.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".txt"))
                 .sorted()
                 .forEach(arquivos::add);
        }
        if (arquivos.isEmpty()) {
            throw new IOException("Nenhum .txt em " + pastaOrdem.toAbsolutePath());
        }
        return arquivos;
    }

    private Path acharSubpasta(Path pai, java.util.function.Predicate<String> regra, String descricao)
            throws IOException {
        try (Stream<Path> itens = Files.list(pai)) {
            return itens.filter(Files::isDirectory)
                        .filter(p -> regra.test(normalizar(p.getFileName().toString())))
                        .findFirst()
                        .orElseThrow(() -> new IOException(
                                "Pasta '" + descricao + "' não encontrada em " + pai.toAbsolutePath()));
        }
    }

    static String normalizar(String nome) {
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
