# Casos de Teste - Batalha Naval (console)

## Regras gerais do jogo

- Tabuleiro **10 x 10**: colunas **A-J**, linhas **1-10**. Cada jogador (voce e o computador) tem um tabuleiro.
- Frota de cada jogador:

  | Navio | Tamanho | Letra |
  |---|---|---|
  | Porta-avioes | 5 | P |
  | Encouracado | 4 | E |
  | Submarino | 3 | S |
  | Destroier | 2 | D |

- Posicionamento manual: `<casa inicial> <direcao>`, onde `h` = horizontal (para a direita) e `v` = vertical (para baixo). Ex: `C4 h`. Os navios sao pedidos na ordem da tabela acima.
- Tiro: coordenada letra + numero. Ex: `E7`.
- Comandos: `frota` (mostra a situacao dos navios, sem gastar turno) e `sair` (encerra o jogo).
- Entradas nao diferenciam maiusculas de minusculas (`c4 H` = `C4 h`).
- O jogador atira primeiro; depois o computador atira. Os turnos se alternam ate uma frota ser destruida.
- Simbolos: `~` agua | `P E S D` seu navio | `X` acerto | `o` tiro na agua | `.` casa ainda nao atacada.
- Navios podem encostar uns nos outros, mas nao podem se sobrepor.

---

## Casos de teste

### CT-01 - Posicionamento manual horizontal valido
- **Pre-condicao:** jogo iniciado, opcao `1` (manual) escolhida, tabuleiro do jogador vazio.
- **Entrada:** `C4 h` para o Porta-avioes (5 casas).
- **Resultado esperado:** o navio e aceito e ocupa C4, D4, E4, F4 e G4. O tabuleiro e redesenhado mostrando `P` nessas 5 casas e o jogo pede o proximo navio (Encouracado).

### CT-02 - Posicionamento manual vertical valido
- **Pre-condicao:** igual ao CT-01.
- **Entrada:** `C4 v` para o Porta-avioes.
- **Resultado esperado:** o navio ocupa C4, C5, C6, C7 e C8 (para baixo). O tabuleiro mostra `P` nessas casas e o jogo pede o proximo navio.

### CT-03 - Navio que ultrapassa a borda do tabuleiro
- **Pre-condicao:** posicionamento manual do Porta-avioes (5 casas).
- **Entrada:** `H1 h` (ocuparia H1 ate L1) e depois `A8 v` (ocuparia A8 ate A12).
- **Resultado esperado:** em ambos os casos aparece "Nao cabe ai (fora do tabuleiro ou sobrepondo outro navio)." Nenhum navio e colocado e o jogo pede novamente a posicao do mesmo navio.

### CT-04 - Sobreposicao de navios
- **Pre-condicao:** Porta-avioes ja posicionado em `C4 h` (C4-G4). Jogo pede o Encouracado (4 casas).
- **Entrada:** `E3 v` (ocuparia E3, E4, E5, E6, cruzando o Porta-avioes em E4).
- **Resultado esperado:** mensagem "Nao cabe ai (fora do tabuleiro ou sobrepondo outro navio)." O Encouracado nao e colocado e a posicao e pedida de novo. Em seguida, `C5 h` e aceito (navios podem encostar, mas nao sobrepor).

### CT-05 - Formato ou direcao invalidos no posicionamento
- **Pre-condicao:** posicionamento manual em andamento.
- **Entrada (uma de cada vez):** `C4` (sem direcao), `C4 x` (direcao invalida), `K1 h` (coluna inexistente), `A11 v` (linha inexistente), `C4 h v` (parametros demais).
- **Resultado esperado:** para `C4` e `C4 h v` aparece "Formato invalido. Exemplo: C4 h"; para `C4 x`, `K1 h` e `A11 v` aparece "Coordenada ou direcao invalida. Exemplo: C4 h". Nenhum navio e colocado e o mesmo navio e pedido de novo.

### CT-06 - Tiro na agua
- **Pre-condicao:** batalha em andamento; a casa E7 do inimigo nao tem navio e ainda nao foi atacada (aparece `.`).
- **Entrada:** `E7`
- **Resultado esperado:** mensagem "Seu tiro em E7: Agua!". No "MAR INIMIGO" a casa E7 passa a mostrar `o`. Em seguida o computador faz sua jogada.

### CT-07 - Tiro que acerta um navio
- **Pre-condicao:** batalha em andamento; existe um navio inimigo de 2 ou mais casas na casa B2, ainda nao atacada.
- **Entrada:** `B2`
- **Resultado esperado:** mensagem "Seu tiro em B2: ACERTOU um navio!" (sem dizer que afundou, pois ainda restam casas do navio). No "MAR INIMIGO" a casa B2 passa a mostrar `X`. O navio inimigo continua contando como "restante" e o computador joga em seguida.

### CT-08 - Afundar um navio
- **Pre-condicao:** o Destroier inimigo (2 casas) ocupa B2 e B3; B2 ja foi atingida.
- **Entrada:** `B3`
- **Resultado esperado:** mensagem "Seu tiro em B3: ACERTOU e AFUNDOU o Destroier!". As duas casas aparecem como `X` e o contador "Navios inimigos restantes" diminui em 1. O comando `frota` passa a mostrar o Destroier inimigo como "AFUNDADO".

### CT-09 - Tiro repetido na mesma casa
- **Pre-condicao:** o jogador ja atirou em E7 em um turno anterior.
- **Entrada:** `E7`
- **Resultado esperado:** mensagem "Voce ja atirou nessa casa. Escolha outra." O tiro nao conta, o computador **nao** joga e o jogo pede nova coordenada. O contador de tiros da vitoria nao aumenta.

### CT-10 - Coordenada de tiro invalida
- **Pre-condicao:** batalha em andamento.
- **Entrada (uma de cada vez):** `Z9`, `K5`, `E0`, `E11`, `E`, `7E`, `e+5`, `e07`, `abc`.
- **Resultado esperado:** para todas aparece "Coordenada invalida. Use letra A-J e numero 1-10 (ex: E7)." Nenhum tiro e feito, o turno nao e consumido e o computador nao joga. Ja `e7` (minuscula) e `E10` sao validas.

### CT-11 - Vitoria do jogador
- **Pre-condicao:** o inimigo tem apenas 1 casa de navio ainda intacta (por exemplo J10, ultima casa do Destroier), todos os outros navios afundados. O jogador ja deu 37 tiros validos.
- **Entrada:** `J10`
- **Resultado esperado:** o tiro afunda o ultimo navio, o tabuleiro e redesenhado e aparece "VITORIA! Voce afundou toda a frota inimiga em 38 tiros!". O computador **nao** faz jogada depois da vitoria. Em seguida o jogo pergunta "Jogar novamente? (s/n):".

### CT-12 - Derrota do jogador
- **Pre-condicao:** a frota do jogador tem apenas 1 casa intacta; o jogador ainda nao venceu.
- **Entrada:** qualquer tiro valido que nao encerre a partida (ex: uma casa de agua ainda nao atacada).
- **Resultado esperado:** depois do tiro do jogador, o computador atira exatamente na ultima casa restante. O tabuleiro e redesenhado e aparece "DERROTA! O computador afundou toda a sua frota." Em seguida o jogo pergunta "Jogar novamente? (s/n):".

### CT-13 - Comando `frota`
- **Pre-condicao:** batalha em andamento, com o Destroier do jogador atingido em 1 das 2 casas.
- **Entrada:** `frota`
- **Resultado esperado:** o jogo lista cada navio do jogador com as casas intactas (ex: "Destroier (2): 1 casa(s) intacta(s)") e, para o inimigo, "ainda flutuando" ou "AFUNDADO". Nenhum tiro e feito, o turno nao e consumido e o computador nao joga.

### CT-14 - Jogar novamente e sair
- **Pre-condicao:** uma partida acabou e aparece "Jogar novamente? (s/n):".
- **Entrada:** `s` ou `sim` -> nova partida; `n`, `nao`, `sair` ou qualquer outra coisa -> encerra.
- **Resultado esperado:** com `s`/`sim` o jogo volta a perguntar como posicionar a frota, com tabuleiros novos e zerados e o contador de tiros recomeçando. Com qualquer outra resposta aparece "Ate a proxima!" e o programa termina. Alem disso, digitar `sair` durante a batalha ou no posicionamento tambem encerra o jogo (no menu de posicionamento "Opcao:" e durante os tiros).

---

## Casos da dupla (CT-15 em diante)

<!-- A dupla continua a partir daqui, mantendo o mesmo formato: Pre-condicao / Entrada / Resultado esperado. -->
