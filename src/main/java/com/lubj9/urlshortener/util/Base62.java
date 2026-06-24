package com.lubj9.urlshortener.util;

/**
 * Conversor entre números inteiros e strings Base62.
 *
 * Base62 usa 62 caracteres alfanuméricos (0-9, a-z, A-Z) para representar
 * números, gerando strings curtas e URL-safe.
 *
 * Trade-offs vs alternativas:
 * <ul>
 *   <li><b>Base62 vs UUID</b>: UUIDs (~36 chars) são muito longos para URLs amigáveis.
 *       Base62 dá ~7 chars para 1 trilhão de combinações.</li>
 *   <li><b>Base62 vs Base64</b>: Base64 inclui '+', '/' e '=', que precisam de URL-encoding.
 *       Base62 evita esse problema usando apenas alfanuméricos.</li>
 *   <li><b>Base62 vs Hash aleatório</b>: derivar do ID auto-incremental garante unicidade
 *       sem precisar de retry em caso de colisão.</li>
 * </ul>
 *
 * Capacidade:
 * <ul>
 *   <li>62^6 ≈ 56 bilhões de combinações com 6 caracteres</li>
 *   <li>62^7 ≈ 3,5 trilhões com 7 caracteres</li>
 * </ul>
 */
public final class Base62 {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private Base62() {
        // utility class — não instanciável
    }

    /**
     * Converte um número inteiro positivo em string Base62.
     *
     * @param value valor a ser convertido (deve ser >= 0)
     * @return representação Base62 (ex: 125 → "21", 100_000 → "Q0u")
     * @throws IllegalArgumentException se value for negativo
     */
    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Base62 não suporta números negativos: " + value);
        }
        if (value == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(ALPHABET.charAt(remainder));
            value /= BASE;
        }
        // O algoritmo monta o número do dígito menos significativo para o mais.
        // Reverter no final dá a string na ordem natural.
        return sb.reverse().toString();
    }

    /**
     * Converte uma string Base62 de volta em número inteiro.
     * Útil para localizar a entidade no banco a partir do código curto.
     */
    public static long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("código não pode ser vazio");
        }
        long value = 0;
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            int digit = ALPHABET.indexOf(c);
            if (digit < 0) {
                throw new IllegalArgumentException(
                        "caractere inválido em código Base62: '" + c + "'");
            }
            value = value * BASE + digit;
        }
        return value;
    }
}
