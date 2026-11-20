import java.util.*;

/**
 * BATALHA NAVAL 10 x 10 - voce contra o computador (console).
 * (Arquivo 100% ASCII: sem acentos e sem simbolos especiais.)
 *
 * Frota de cada jogador:
 *   1 Porta-avioes (5 casas)  - letra P
 *   1 Encouracado  (4 casas)  - letra E
 *   1 Submarino    (3 casas)  - letra S
 *   1 Destroier    (2 casas)  - letra D
 *
 * Colunas A-J, linhas 1-10.
 *
 * Posicionamento manual: digite a casa inicial e a direcao, ex:  C4 h  (horizontal, para a direita)
 *                                                                C4 v  (vertical, para baixo)
 * Durante o jogo: digite a coordenada do tiro, ex: E7  | 'frota' mostra a situacao | 'sair' encerra.
 *
 * Simbolos: ~ agua | P E S D seu navio | X acerto | o tiro na agua | . casa ainda nao atacada
 *
 * Compilar: javac BatalhaNaval.java
 * Executar: java BatalhaNaval
 */
public class BatalhaNaval {

    static final int N = 10;
    static final String[] NOMES = {"Porta-avioes", "Encouracado", "Submarino", "Destroier"};
    static final int[] TAM = {5, 4, 3, 2};
    static final char[] SIGLA = {'P', 'E', 'S', 'D'};

    static final Scanner sc = new Scanner(System.in);
    static final Random rnd = new Random();

    // ===================== Tabuleiro =====================
    static class Tabuleiro {
        final int[][] id = new int[N][N];          // -1 = agua, 0..3 = indice do navio
        final boolean[][] tiro = new boolean[N][N];
        final int[] restante = TAM.clone();        // casas ainda nao atingidas de cada navio
        int ultimo;                                // navio atingido no ultimo tiro

        Tabuleiro() {
            for (int[] linha : id) Arrays.fill(linha, -1);
        }

        boolean cabe(int r, int c, int len, boolean horizontal) {
            for (int i = 0; i < len; i++) {
                int rr = r + (horizontal ? 0 : i);
                int cc = c + (horizontal ? i : 0);
                if (rr < 0 || rr >= N || cc < 0 || cc >= N || id[rr][cc] != -1) return false;
            }
            return true;
        }

        void colocar(int navio, int r, int c, boolean horizontal) {
            for (int i = 0; i < TAM[navio]; i++) {
                int rr = r + (horizontal ? 0 : i);
                int cc = c + (horizontal ? i : 0);
                id[rr][cc] = navio;
            }
        }

        void aleatorio() {
            for (int n = 0; n < TAM.length; n++) {
                int r, c;
                boolean h;
                do {
                    r = rnd.nextInt(N);
                    c = rnd.nextInt(N);
                    h = rnd.nextBoolean();
                } while (!cabe(r, c, TAM[n], h));
                colocar(n, r, c, h);
            }
        }

        /** Retorna -1 = agua, 0 = acertou, 1 = afundou. */
        int atirar(int r, int c) {
            tiro[r][c] = true;
            if (id[r][c] == -1) return -1;
            ultimo = id[r][c];
            restante[ultimo]--;
            return restante[ultimo] == 0 ? 1 : 0;
        }

        boolean derrotado() {
            for (int x : restante) if (x > 0) return false;
            return true;
        }
    }

    // ===================== IA do computador =====================
    static final Deque<int[]> alvos = new ArrayDeque<>();

    static int[] jogadaIA(Tabuleiro alvo) {
        // Modo "alvo": ataca vizinhas de um acerto anterior
        while (!alvos.isEmpty()) {
            int[] p = alvos.removeFirst();
            if (!alvo.tiro[p[0]][p[1]]) return p;
        }
        // Modo "cacada": casas em xadrez (o menor navio tem 2 casas)
        List<int[]> xadrez = new ArrayList<>();
        List<int[]> todas = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (alvo.tiro[r][c]) continue;
                todas.add(new int[]{r, c});
                if ((r + c) % 2 == 0) xadrez.add(new int[]{r, c});
            }
        }
        List<int[]> lista = xadrez.isEmpty() ? todas : xadrez;
        return lista.get(rnd.nextInt(lista.size()));
    }

    static void adicionarVizinhas(Tabuleiro alvo, int r, int c) {
        int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<int[]> vizinhas = new ArrayList<>();
        for (int[] x : d) {
            int rr = r + x[0], cc = c + x[1];
            if (rr >= 0 && rr < N && cc >= 0 && cc < N && !alvo.tiro[rr][cc]) vizinhas.add(new int[]{rr, cc});
        }
        Collections.shuffle(vizinhas, rnd);
        for (int[] v : vizinhas) alvos.addLast(v);
    }

    /** Apos afundar um navio, mantem so os alvos vizinhos de acertos em navios ainda vivos. */
    static void limparAlvosDoAfundado(Tabuleiro alvo) {
        int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        alvos.removeIf(p -> {
            for (int[] x : d) {
                int rr = p[0] + x[0], cc = p[1] + x[1];
                if (rr >= 0 && rr < N && cc >= 0 && cc < N && alvo.tiro[rr][cc]
                        && alvo.id[rr][cc] != -1 && alvo.restante[alvo.id[rr][cc]] > 0) return false;
            }
            return true;
        });
    }

    // ===================== Main =====================
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("      BATALHA NAVAL 10 x 10");
        System.out.println("=====================================");
        System.out.println("Frota: Porta-avioes (5), Encouracado (4), Submarino (3), Destroier (2)");

        boolean continuar = true;
        while (continuar) {
            if (!jogar()) break; // jogador digitou 'sair'
            System.out.print("\nJogar novamente? (s/n): ");
            String resp = ler();
            continuar = resp.equals("s") || resp.equals("sim");
        }
        System.out.println("\nAte a proxima!");
    }

    /** Retorna false se o jogador pediu para sair. */
    static boolean jogar() {
        Tabuleiro eu = new Tabuleiro();
        Tabuleiro pc = new Tabuleiro();
        alvos.clear();
        pc.aleatorio();

        // Posicionamento da frota do jogador
        System.out.println("\nComo deseja posicionar sua frota?");
        System.out.println(" [1] Manualmente   [2] Automaticamente");
        while (true) {
            System.out.print("Opcao: ");
            String s = ler();
            if (s.equals("sair")) return false;
            if (s.equals("1")) {
                posicionarManual(eu);
                break;
            }
            if (s.equals("2")) {
                eu.aleatorio();
                break;
            }
            System.out.println("Opcao invalida.");
        }

        System.out.println("\nFrotas posicionadas! A batalha vai comecar. Voce atira primeiro.");
        int turnos = 0;

        while (true) {
            desenhar(eu, pc);
            System.out.print("Seu tiro (ex: E7): ");
            String s = ler();
            if (s.equals("sair")) return false;
            if (s.equals("frota")) {
                mostrarFrota(eu, pc);
                continue;
            }
            int[] pos = lerCoordenada(s);
            if (pos == null) {
                System.out.println("Coordenada invalida. Use letra A-J e numero 1-10 (ex: E7).");
                continue;
            }
            if (pc.tiro[pos[0]][pos[1]]) {
                System.out.println("Voce ja atirou nessa casa. Escolha outra.");
                continue;
            }

            turnos++;
            int res = pc.atirar(pos[0], pos[1]);
            System.out.println("\n>> Seu tiro em " + nomeCasa(pos[0], pos[1]) + ": " + mensagem(res, pc, true));
            if (pc.derrotado()) {
                desenhar(eu, pc);
                System.out.println("VITORIA! Voce afundou toda a frota inimiga em " + turnos + " tiros!");
                return true;
            }

            // Turno do computador
            int[] t = jogadaIA(eu);
            int r2 = eu.atirar(t[0], t[1]);
            System.out.println(">> Computador atirou em " + nomeCasa(t[0], t[1]) + ": " + mensagem(r2, eu, false));
            if (r2 == 0) {
                adicionarVizinhas(eu, t[0], t[1]);
            } else if (r2 == 1) {
                limparAlvosDoAfundado(eu);
            }
            if (eu.derrotado()) {
                desenhar(eu, pc);
                System.out.println("DERROTA! O computador afundou toda a sua frota.");
                return true;
            }
        }
    }

    static String mensagem(int res, Tabuleiro t, boolean meuTiro) {
        if (res == -1) return "Agua!";
        if (res == 0) return "ACERTOU um navio!";
        return "ACERTOU e AFUNDOU " + (meuTiro ? "o " : "o seu ") + NOMES[t.ultimo] + "!";
    }

    // ===================== Posicionamento manual =====================
    static void posicionarManual(Tabuleiro eu) {
        System.out.println("\nFormato: casa inicial + direcao (h = horizontal, v = vertical). Ex: C4 h");
        for (int n = 0; n < TAM.length; n++) {
            while (true) {
                desenharUm(eu, "SEU MAR");
                System.out.print("Posicione o " + NOMES[n] + " (" + TAM[n] + " casas): ");
                String s = ler();
                String[] p = s.split("\\s+");
                if (p.length != 2) {
                    System.out.println("Formato invalido. Exemplo: C4 h");
                    continue;
                }
                int[] pos = lerCoordenada(p[0]);
                if (pos == null || !(p[1].equals("h") || p[1].equals("v"))) {
                    System.out.println("Coordenada ou direcao invalida. Exemplo: C4 h");
                    continue;
                }
                boolean h = p[1].equals("h");
                if (!eu.cabe(pos[0], pos[1], TAM[n], h)) {
                    System.out.println("Nao cabe ai (fora do tabuleiro ou sobrepondo outro navio).");
                    continue;
                }
                eu.colocar(n, pos[0], pos[1], h);
                break;
            }
        }
    }

    // ===================== Tela =====================
    static char simboloMeu(Tabuleiro t, int r, int c) {
        if (t.tiro[r][c]) return t.id[r][c] == -1 ? 'o' : 'X';
        return t.id[r][c] == -1 ? '~' : SIGLA[t.id[r][c]];
    }

    static char simboloInimigo(Tabuleiro t, int r, int c) {
        if (!t.tiro[r][c]) return '.';
        return t.id[r][c] == -1 ? 'o' : 'X';
    }

    static String linha(Tabuleiro t, int r, boolean inimigo) {
        StringBuilder sb = new StringBuilder(String.format("%2d  ", r + 1));
        for (int c = 0; c < N; c++) {
            sb.append(inimigo ? simboloInimigo(t, r, c) : simboloMeu(t, r, c));
            if (c < N - 1) sb.append(' ');
        }
        return sb.toString();
    }

    static String cabecalho() {
        StringBuilder sb = new StringBuilder("    ");
        for (int c = 0; c < N; c++) {
            sb.append((char) ('A' + c));
            if (c < N - 1) sb.append(' ');
        }
        return sb.toString();
    }

    static void desenhar(Tabuleiro eu, Tabuleiro pc) {
        System.out.println();
        System.out.println(String.format("%-29s", "     === SEU MAR ===") + "=== MAR INIMIGO ===");
        System.out.println(cabecalho() + "      " + cabecalho());
        for (int r = 0; r < N; r++) {
            System.out.println(linha(eu, r, false) + "      " + linha(pc, r, true));
        }
        System.out.println("\nLegenda: ~ agua | P E S D seus navios | X acerto | o agua atingida | . nao atacada");
        System.out.println("Navios inimigos restantes: " + restantes(pc) + " | Seus navios restantes: " + restantes(eu));
        System.out.println("(digite 'frota' para ver detalhes)");
    }

    static void desenharUm(Tabuleiro t, String titulo) {
        System.out.println("\n=== " + titulo + " ===");
        System.out.println(cabecalho());
        for (int r = 0; r < N; r++) System.out.println(linha(t, r, false));
        System.out.println();
    }

    static int restantes(Tabuleiro t) {
        int n = 0;
        for (int x : t.restante) if (x > 0) n++;
        return n;
    }

    static void mostrarFrota(Tabuleiro eu, Tabuleiro pc) {
        System.out.println("\nSua frota:");
        for (int n = 0; n < TAM.length; n++) {
            System.out.println("  " + NOMES[n] + " (" + TAM[n] + "): "
                    + (eu.restante[n] == 0 ? "AFUNDADO" : eu.restante[n] + " casa(s) intacta(s)"));
        }
        System.out.println("Frota inimiga:");
        for (int n = 0; n < TAM.length; n++) {
            System.out.println("  " + NOMES[n] + " (" + TAM[n] + "): "
                    + (pc.restante[n] == 0 ? "AFUNDADO" : "ainda flutuando"));
        }
    }

    // ===================== Utilidades =====================
    static int[] lerCoordenada(String s) {
        if (s.length() < 2 || s.length() > 3) return null;
        int c = s.charAt(0) - 'a';
        String num = s.substring(1);
        if (!num.matches("[1-9][0-9]?")) return null;
        int r = Integer.parseInt(num) - 1;
        if (r < 0 || r >= N || c < 0 || c >= N) return null;
        return new int[]{r, c};
    }

    static String nomeCasa(int r, int c) {
        return "" + (char) ('A' + c) + (r + 1);
    }

    static String ler() {
        if (!sc.hasNextLine()) {
            System.out.println("\nEntrada encerrada.");
            System.exit(0);
        }
        return sc.nextLine().trim().toLowerCase();
    }
}