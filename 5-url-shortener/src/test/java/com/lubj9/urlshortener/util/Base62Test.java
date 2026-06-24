package com.lubj9.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testa o algoritmo de Base62 — o coração do encurtador.
 *
 * Cobre:
 *  - Valores de fronteira (0, 1, BASE, BASE-1)
 *  - Round-trip (encode → decode deve voltar ao mesmo valor)
 *  - Casos de erro (negativos, caracteres inválidos)
 */
class Base62Test {

    @Test
    @DisplayName("encode(0) deve retornar o primeiro caractere do alfabeto")
    void encodeZero() {
        assertThat(Base62.encode(0)).isEqualTo("0");
    }

    @Test
    @DisplayName("encode(1) deve retornar '1'")
    void encodeOne() {
        assertThat(Base62.encode(1)).isEqualTo("1");
    }

    @Test
    @DisplayName("encode(61) deve retornar o último caractere do alfabeto (Z)")
    void encodeUltimoCaractere() {
        assertThat(Base62.encode(61)).isEqualTo("Z");
    }

    @Test
    @DisplayName("encode(62) deve retornar '10' (transbordo do alfabeto)")
    void encodeTransbordo() {
        assertThat(Base62.encode(62)).isEqualTo("10");
    }

    @Test
    @DisplayName("encode/decode deve ser uma operação reversível (round-trip)")
    void roundTrip() {
        long[] valores = {0, 1, 42, 100, 1_000, 1_000_000, 1_000_000_000L, Long.MAX_VALUE / 2};
        for (long valor : valores) {
            String encoded = Base62.encode(valor);
            long decoded = Base62.decode(encoded);
            assertThat(decoded)
                    .as("round-trip falhou para %d → '%s'", valor, encoded)
                    .isEqualTo(valor);
        }
    }

    @Test
    @DisplayName("encode de valores negativos deve lançar IllegalArgumentException")
    void encodeNegativo() {
        assertThatThrownBy(() -> Base62.encode(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativos");
    }

    @Test
    @DisplayName("decode com caractere fora do alfabeto deve lançar IllegalArgumentException")
    void decodeCaractereInvalido() {
        assertThatThrownBy(() -> Base62.decode("abc!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("decode de string vazia ou nula deve lançar IllegalArgumentException")
    void decodeVazio() {
        assertThatThrownBy(() -> Base62.decode(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Base62.decode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Códigos curtos devem ter tamanho gerenciável mesmo para IDs altos")
    void tamanhoCodigos() {
        // 1 bilhão de URLs ainda cabe em 6 caracteres
        assertThat(Base62.encode(1_000_000_000L)).hasSizeLessThanOrEqualTo(6);
    }
}
