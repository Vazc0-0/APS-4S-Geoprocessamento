package br.unip.aps.servidor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Json {
    private final Map<String, Object> campos = new LinkedHashMap<>();

    private Json() { }

    public static Json objeto() {
        return new Json();
    }

    public Json com(String chave, Object valor) {
        campos.put(chave, valor);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        escrever(this, sb);
        return sb.toString();
    }

    private static void escrever(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof Json json) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<String, Object> e : json.campos.entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escreverTexto(e.getKey(), sb);
                sb.append(':');
                escrever(e.getValue(), sb);
            }
            sb.append('}');
        } else if (valor instanceof String texto) {
            escreverTexto(texto, sb);
        } else if (valor instanceof Double d) {
            sb.append(Double.isFinite(d) ? String.valueOf(d) : "null");
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor);
        } else if (valor instanceof int[] vetor) {
            sb.append('[');
            for (int i = 0; i < vetor.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(vetor[i]);
            }
            sb.append(']');
        } else if (valor instanceof Collection<?> lista) {
            sb.append('[');
            boolean primeiro = true;
            for (Object item : lista) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                escrever(item, sb);
            }
            sb.append(']');
        } else {
            escreverTexto(valor.toString(), sb);
        }
    }

    private static void escreverTexto(String texto, StringBuilder sb) {
        sb.append('"');
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
