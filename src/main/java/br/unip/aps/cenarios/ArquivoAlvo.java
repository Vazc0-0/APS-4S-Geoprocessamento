package br.unip.aps.cenarios;

import br.unip.aps.dados.Ordem;

public record ArquivoAlvo(Ordem ordem, boolean comDuplicidade) {
    public String descricao() {
        return ordem.rotulo() + (comDuplicidade ? " · com duplicidade" : " · sem duplicidade");
    }
}
