# Changchun Mahjong Rules

This document outlines the rules for the Changchun Mahjong implementation in this application.

## 1. Game Components

### Tile Set
The game uses a standard set of 136 Mahjong tiles:
- **Suited Tiles (108 tiles):** 
    - **Wan (Characters):** 1-9 (4 of each)
    - **Tong (Dots):** 1-9 (4 of each)
    - **Tiao (Bamboo):** 1-9 (4 of each)
- **Honor Tiles (28 tiles):**
    - **Zi (Winds & Dragons):** 1-7 (4 of each)
        - 1-4: East, South, West, North
        - 5-7: Middle (Zhong), Green (Fa), White (Bai)

## 2. Winning Conditions (Hu)

To win (Hu), a player's hand must consist of 14 tiles that satisfy the following conditions:

### Basic Structure
- **Standard Hand:** 4 sets + 1 pair. 
    - A "set" can be a **Sequence** (Chi), **Triplet** (Peng/drawn), or **Quad** (Gang).
- **Seven Pairs (Qi Dui):** Exactly 7 pairs of identical tiles. This is only possible if the player has no exposed melds (0 Chi/Peng/Gang).

### Changchun Specific Constraints
1. **Three Suits:** The winning hand must contain at least one tile from all three suits (Wan, Tong, and Tiao).
2. **Yao Jiu:** The hand must contain at least one "Yao Jiu" tile. This includes any tile with rank 1 or 9, or any Honor tile (Zi). 
3. **At Least One Set:** The hand must contain at least one Triplet or Quad (either exposed as Peng/Gang or hidden in hand), **OR** the win must use a **Dragon Pair** (Zhong, Fa, or Bai). A hand consisting only of sequences and a regular pair is not eligible unless it is a "Seven Pairs" hand.

## 3. Player Actions

### Chi (Eat)
- Form a sequence of three consecutive tiles of the same suit (e.g., 2-3-4 Wan).
- Can only be taken from the player immediately to your left (upper house) when they discard.

### Peng (Bump)
- Form a triplet of three identical tiles.
- Can be taken from any player when they discard.

### Gang (Kong)
- Form a quad of four identical tiles.
- **Ming Gang (Exposed):** Taking a discarded tile to complete a quad.
- **An Gang (Hidden):** Collecting all four tiles in your hand. This can be done with a starting hand quad or after self-drawing the 4th tile.
- **Bu Gang (Add-up):** Adding a self-drawn 4th tile to a previously exposed Peng.
- After any Gang, the player must draw a replacement tile from the wall.

### Hu (Win)
- **Dian Pao:** Winning from another player's discard.
- **Zi Mo:** Winning by drawing the final tile from the wall yourself.

## 4. Game Flow

### Dealing
- The **Banker (East)** starts the game with 14 tiles.
- The other three players start with 13 tiles.
- Dealing is typically determined by two dice rolls to find the wall owner and the breach point.

### Turn Sequence
- Turns proceed counter-clockwise: **East → South → West → North**.
- On each turn, a player draws a tile from the wall and then discards one.
- Discards can be "claimed" by other players for Chi, Peng, Gang, or Hu.

### Interruption Priority
If multiple players want to claim a discarded tile:
1. **Hu (Win)** has the highest priority.
2. **Peng** or **Gang** has the second priority.
3. **Chi** has the lowest priority and can only be claimed by the next player in turn.

### Limitations
- **Exposed Melds:** In this implementation, a player is limited to a maximum of **3 exposed melds** (Chi/Peng/Gang) to prevent "dead hands" and maintain strategic complexity.

## 5. Game End
- The game ends when a player calls **Hu**.
- If the wall is exhausted (0 tiles remaining) and no one has won, the round ends in a **Draw (Huang Zhuang)**.
