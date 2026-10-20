package br.unip.aps.relatorio;

import java.util.List;

public record Relatorio(String nivel, String veredito, String titulo, List<String> paragrafos) {
}
