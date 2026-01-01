# Computer Discard Strategy (AI)

This document outlines the heuristic-based strategy used by computer players to decide which tile to discard. The strategy focuses on maximizing the potential for forming winning sets (Triplets and Sequences) while considering tile availability.

## 1. Keep Value Calculation
Every tile in the AI's hand is assigned a "Keep Value" score. The tile with the **lowest** score is chosen for discard.

### A. Triplet Requirement
- **Changchun Rule**: Must have at least one triplet (Peng/Gang) to win.
- **Priority**: If the AI doesn't have a triplet yet, pairs are assigned a much higher value (+150 to +180 pts) to encourage hitting that first triplet.

### B. "Yao Jiu" (Terminal/Honor) Requirement
- **Changchun Rule**: Hand must contain at least one terminal (1 or 9) or honor tile (Zhong, Fa, Bai, or Winds).
- **Protection**: If a tile is the ONLY terminal/honor tile in the hand, it receives a +100 point safety bonus to ensure it isn't discarded.

### C. Three Suits Requirement
- **Changchun Rule**: Hand must contain at least one tile from all three suits (Wan, Tiao, Tong).
- **Endangered Suit Protection**: If the AI holds 2 or fewer tiles of a suit and has no exposed melds for that suit, these tiles receive a heavy safety bonus (+150 to +250 pts). This ensures the AI doesn't accidentally discard its only connection to a required suit early in the game.

### D. General Sets and Pairs
- **Triplets/Quads**: Valued at +100 pts.
- **Dragon Pairs**: Valued at +150 pts as they also satisfy terminal requirements.

### E. Sequences (Numbered Tiles)
- **Completed Sequences**: Valued at +80 pts.
- **Side Waits**: Base value of +25 pts.
- **Gap Waits**: Base value of +12 pts.

### F. Penalty for Single Honors (Non-Terminals)
- Single honor tiles that don't satisfy the "only Yao Jiu" protection receive a penalty (-20 pts).

### G. "Hu-Oriented" Bonus (Ready State)
- **Tenpai (Ready)**: If discarding a tile makes the hand "Tenpai" (waiting for exactly 1 tile to win), the remaining tiles receive a massive "Tenpai Protection" bonus (+500 pts).
- **Outs Count**: The more tiles that can complete the hand (the "outs"), the higher the bonus for the remaining tiles. This ensures the AI chooses discards that leave the widest possible winning opportunities.

## 2. Hu Progress Simulation
Instead of just evaluating tiles in isolation, the AI now simulates the outcome of each possible discard:
- It checks if any discard leads to a "Tenpai" state.
- It prioritizes discards that satisfy all Changchun requirements (Triplet, 3-Suits, Yao Jiu) simultaneously.

## 3. Dynamic Availability Modifier
Base scores for "Waits" (Side/Gap) are multiplied by an availability factor (0.0 to 1.0):
- The AI counts how many copies of the "needed" tiles (the tiles that would complete the sequence) are already visible on the table (discards and exposed melds).
- If all 4 copies of a needed tile are visible, the availability becomes 0, and the value of keeping that partial sequence drops to zero.

## 4. Global Visibility Check
To prevent the AI from chasing "dead tiles," a global penalty is applied to every tile:
- **Visibility Penalty**: -5 pts for every copy of that specific tile already visible on the table.
- This ensures the AI prefers keeping "fresh" tiles that have a higher mathematical probability of being drawn from the wall.

## 5. Randomization
If multiple tiles have the same minimum "Keep Value," the AI shuffles the candidates and picks one randomly to avoid predictable behavior.
