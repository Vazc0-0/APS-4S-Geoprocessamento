package br.unip.aps.servidor;

import br.unip.aps.api.OrdenacaoApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class Servidor {
    private final HttpServer http;
    private final OrdenacaoApi api;
    private final Path pastaFrontend;

    public Servidor(int porta, OrdenacaoApi api, Path pastaFrontend) throws IOException {
        this.api = api;
        this.pastaFrontend = pastaFrontend.toAbsolutePath().normalize();
        this.http = HttpServer.create(new InetSocketAddress("localhost", porta), 0);
        this.http.setExecutor(Executors.newFixedThreadPool(4));
        this.http.createContext("/api/", this::tratarApi);
        this.http.createContext("/", this::tratarArquivo);
    }

    public void iniciar() {
        http.start();
    }

    private void tratarApi(HttpExchange troca) throws IOException {
        Map<String, String> parametros = lerParametros(troca.getRequestURI().getRawQuery());
        String rota = troca.getRequestURI().getPath();
        try {
            Json resposta = switch (rota) {
                case "/api/opcoes" -> api.opcoes();
                case "/api/ordenar" -> api.ordenar(parametros);
                case "/api/comparar" -> api.comparar(parametros);
                default -> null;
            };
            if (resposta == null) {
                responder(troca, 404, "application/json", erro("Rota não encontrada: " + rota));
            } else {
                responder(troca, 200, "application/json", resposta.toString());
            }
        } catch (IllegalArgumentException e) {
            responder(troca, 400, "application/json", erro(e.getMessage()));
        } catch (IOException e) {
            responder(troca, 404, "application/json", erro(e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            responder(troca, 500, "application/json", erro("Erro interno: " + e.getMessage()));
        }
    }

    private void tratarArquivo(HttpExchange troca) throws IOException {
        String caminho = troca.getRequestURI().getPath();
        if (caminho.equals("/")) {
            caminho = "/index.html";
        }
        Path arquivo = pastaFrontend.resolve(caminho.substring(1)).normalize();
        if (!arquivo.startsWith(pastaFrontend) || !Files.isRegularFile(arquivo)) {
            responder(troca, 404, "text/plain", "Arquivo não encontrado: " + caminho);
            return;
        }
        byte[] conteudo = Files.readAllBytes(arquivo);
        troca.getResponseHeaders().set("Content-Type", tipoConteudo(arquivo.getFileName().toString()));
        troca.sendResponseHeaders(200, conteudo.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(conteudo);
        }
    }

    private static String tipoConteudo(String nome) {
        String n = nome.toLowerCase();
        if (n.endsWith(".html")) return "text/html; charset=utf-8";
        if (n.endsWith(".css")) return "text/css; charset=utf-8";
        if (n.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (n.endsWith(".svg")) return "image/svg+xml";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static void responder(HttpExchange troca, int status, String tipo, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", tipo.contains("charset") ? tipo : tipo + "; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static String erro(String mensagem) {
        return Json.objeto().com("erro", mensagem).toString();
    }

    private static Map<String, String> lerParametros(String query) {
        Map<String, String> parametros = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return parametros;
        }
        for (String par : query.split("&")) {
            int igual = par.indexOf('=');
            String chave = igual >= 0 ? par.substring(0, igual) : par;
            String valor = igual >= 0 ? par.substring(igual + 1) : "";
            parametros.put(URLDecoder.decode(chave, StandardCharsets.UTF_8),
                           URLDecoder.decode(valor, StandardCharsets.UTF_8));
        }
        return parametros;
    }
}
