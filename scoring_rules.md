# Mahjong Scoring Rules (Formal Rule Book)

## 1. General Rules

1. This game is played by **four players**.
2. Exactly **one player is designated as the Banker** at any given time.
3. Each player begins the game with **100 points**.
4. All scoring is **zero-sum**: any points gained by one player are lost by others.
5. All point values are **whole numbers**.
6. The game will end when a player reaches below **0 points**.

---

## 2. Definitions and Terminology

**Banker**  
The designated player who carries a scoring advantage in hands involving them.

**Winner**  
The player who completes a legal winning hand.

**Discarder**  
The player who discards the tile that allows another player to win.

**Self-Draw**  
A win in which the winner draws the winning tile themselves.

**Discard Win**  
A win in which the winner claims a tile discarded by another player.

**Standup (Fully Concealed Hand)**  
A winning hand that contains **no public/open melds**.

**Private In-Between (Kanchan)**  
A situation where:
- The winning tile completes a **sequence**,
- The winning tile is the **middle tile** of that sequence (for example, winning on 3 to complete 2-3-4),
- The completed sequence is **private/concealed**.

---

## 3. Bonus Multipliers

### 3.1 Standup Multiplier
- Applied if the winning hand is fully concealed.
- Multiplier: **×2**

### 3.2 Private In-Between Multiplier
- Applied if the winning tile is a private in-between tile.
- Multiplier: **×2**

### 3.3 Combined Bonus Multiplier

The combined bonus multiplier **G** is defined as:

G = Standup × Private In-Between


Possible values of **G** are:
- **1** (no bonuses apply),
- **2** (one bonus applies),
- **4** (both bonuses apply).

All point payments in a hand are multiplied by **G**.

---

## 4. Banker Multiplier Rule (Pairwise)

The banker advantage is applied **pairwise**, not globally.

For any payment from a losing player to the winner:
- If **either the payer or the winner is the banker**, that payment is **doubled (×2)**.
- If **neither player is the banker**, that payment is **not doubled (×1)**.

Each losing player’s payment is evaluated independently under this rule.

---

## 5. Win Types and Scoring

## 5.1 Self-Draw Win

When the winner draws the winning tile themselves:

1. **All three non-winning players pay the winner.**
2. Each payment is:
   - Doubled for self-draw,
   - Doubled again if the banker is involved in that payer–winner pair,
   - Multiplied by **G**.

### 5.1.1 Winner Is Banker
- Each of the three other players pays **4G**.
- The winner gains **12G**.

Point change pattern:

[ +12G, −4G, −4G, −4G ]


### 5.1.2 Winner Is Not Banker
- The banker pays **4G**.
- Each other non-banker pays **2G**.
- The winner gains **8G**.

Point change pattern:

[ +8G, −4G, −2G, −2G ]


(Role order: Winner, Banker, Other Player 1, Other Player 2)

---

## 5.2 Discard Win

When the winner claims a discarded tile:

1. **Only the discarding player pays points.**
2. The discarder:
   - Pays **their own share doubled** (discard penalty),
   - Covers the normal shares of the other two non-winners.
3. The other two non-winning players pay **nothing**.
4. All payments are multiplied by **G**.

---

## 6. Discard Win Scenarios

### 6.1 Winner Is Banker
- The discarder is a non-banker.
- The discarder pays **8G**.
- The winner gains **8G**.

Point change pattern:

[ +8G, −8G, 0, 0 ]


---

### 6.2 Winner Is Not Banker, Discarder Is Banker
- The banker’s own share is doubled.
- The banker covers the two other players’ shares.
- The banker pays **6G**.
- The winner gains **6G**.

Point change pattern:


[ +6G, −6G, 0, 0 ]


---

### 6.3 Winner Is Not Banker, Discarder Is Not Banker
(Both the winner and the discarder are non-bankers.)

- The discarder’s own share is doubled.
- The discarder covers:
  - The banker’s share,
  - One other non-banker’s share.
- The discarder pays **5G**.
- The winner gains **5G**.

Point change pattern:

[ +5G, 0, −5G, 0 ]


(Role order: Winner, Banker, Discarder, Other Player)

---

## 7. Summary Table (No Bonuses Applied, G = 1)

| Situation | Winner Gains | Discarder Pays |
|----------|--------------|----------------|
| Banker self-draw | 12 | — |
| Non-banker self-draw | 8 | — |
| Banker wins by discard | 8 | 8 |
| Non-banker wins, banker discards | 6 | 6 |
| Non-banker wins, non-banker discards | 5 | 5 |

When bonuses apply, multiply all values by **G**.

---

## 8. Rule Guarantees

1. All scoring outcomes are zero-sum.
2. Banker advantage applies **only when the banker is directly involved**.
3. Discard penalties are proportional and predictable.
4. Standup and private in-between bonuses stack cleanly.
5. All payer responsibilities and point transfers are explicitly defined.

---

## End of Rule Book



