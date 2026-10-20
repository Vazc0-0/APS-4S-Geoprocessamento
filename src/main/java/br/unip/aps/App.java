package br.unip.aps;

import br.unip.aps.api.OrdenacaoApi;
import br.unip.aps.dados.CatalogoArquivos;
import br.unip.aps.servidor.Servidor;

import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static final int PORTA = 8080;

    public static void main(String[] args) throws Exception {
        Path pastaDados = Path.of("dados");
        Path pastaFrontend = Path.of("frontend");

        if (!Files.isDirectory(pastaFrontend)) {
            System.err.println("Pasta 'frontend' não encontrada em " + Path.of("").toAbsolutePath()
                    + ". Rode o programa a partir da pasta raiz do projeto.");
            return;
        }

        CatalogoArquivos catalogo = new CatalogoArquivos(pastaDados);
        Servidor servidor = new Servidor(PORTA, new OrdenacaoApi(catalogo), pastaFrontend);
        servidor.iniciar();

        System.out.println("Sistema de Análise de Ordenação rodando.");
        System.out.println("Abra no navegador: http://localhost:" + PORTA);
        System.out.println("Dados lidos de: " + catalogo.raiz().toAbsolutePath());
        System.out.println("Para parar: Ctrl+C no terminal (ou o botão de parar no VS Code).");
    }
}
