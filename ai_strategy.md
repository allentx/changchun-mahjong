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
- **Protection**: If the AI is missing a suit and holds the last remaining tile of that suit, it receives a +200 point protection bonus.

### D. General Sets and Pairs
- **Triplets/Quads**: Valued at +100 pts.
- **Dragon Pairs**: Valued at +150 pts as they also satisfy terminal requirements.

### E. Sequences (Numbered Tiles)
- **Completed Sequences**: Valued at +80 pts.
- **Side Waits**: Base value of +25 pts.
- **Gap Waits**: Base value of +12 pts.

### F. Penalty for Single Honors (Non-Terminals)
- Single honor tiles that don't satisfy the "only Yao Jiu" protection receive a penalty (-20 pts).

## 2. Dynamic Availability Modifier
Base scores for "Waits" (Side/Gap) are multiplied by an availability factor (0.0 to 1.0):
- The AI counts how many copies of the "needed" tiles (the tiles that would complete the sequence) are already visible on the table (discards and exposed melds).
- If all 4 copies of a needed tile are visible, the availability becomes 0, and the value of keeping that partial sequence drops to zero.

## 3. Global Visibility Check
To prevent the AI from chasing "dead tiles," a global penalty is applied to every tile:
- **Visibility Penalty**: -5 pts for every copy of that specific tile already visible on the table.
- This ensures the AI prefers keeping "fresh" tiles that have a higher mathematical probability of being drawn from the wall.

## 4. Randomization
If multiple tiles have the same minimum "Keep Value," the AI shuffles the candidates and picks one randomly to avoid predictable behavior.
