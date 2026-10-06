# SQUISH OUT: UI/UX Design System & Master Screen Specification

> **Target Platform:** Mobile (iOS & Android via Compose Multiplatform)  
> **Target Tool:** Sketch / Stitch Design Agent  
> **Aspect Ratio:** Primary Canvas: 390 × 844 px (iPhone 15/16 baseline), Responsive to Tablet / Foldable  
> **Core Aesthetic:** Juicy Kawaii Tactile Jelly • Cozy Candy Meadow • High-Dopamine Casual Puzzle  
> **Core Genre:** 2D Directional Unblocking Puzzle (Evolution of "Arrow Puzzle")

---

## 1. Executive Summary & Design Vision

### 1.1 The Concept: "Arrow Puzzle" Reimagined
Traditional directional puzzle games (such as Easybrain's *Arrow Puzzle*) rely on sterile, monochromatic geometric arrows on flat backgrounds. While the core spatial unblocking mechanic is proven and globally viral, the visual design is cold, clinical, and impersonal.

**Squish Out** transforms this logic puzzle into an **irresistible, vibrant, character-driven experience**:
- Sterile arrows are replaced by **chubby, translucent, kawaii jelly creatures ("Squishies")**.
- Each creature has an inherent anatomical forward orientation (pointed cap, snail-like antenna, and forward-focused eyes).
- Trapped creatures **sleep peacefully**; unblocked creatures **wake up with wide, sparkling eyes**, intuitively communicating legal moves without visual clutter.
- Tapping an unblocked creature triggers an explosive **squish-and-stretch launch**, bouncing it free into the candy meadow with a juicy waterdrop bubble sound (`ploink!`).

### 1.2 The Three Design Pillars
1. **Juicy & Tactile (Candy Gloss):** Every button, tile, and creature feels physical, edible, and snackable. We use soft 3D bevels, spherical specular highlights, and springy press states.
2. **Instant Cognitive Clarity:** Despite the rich visual charm, board readability is paramount. The player must instantly recognize:
   - Which direction each jelly faces.
   - Which jellies are currently blocked vs. free.
   - The boundary of the escape paths.
3. **Cozy, Low-Stress Charm:** Soft pastel-candy gradients, friendly rounded typography, gentle idle breathing animations, and warm storybook borders that create a calming, mindful sanctuary.

---

## 2. Design System & Visual Tokens

### 2.1 Color Palette & Swatches

The color system is organized into **Character Types (Direction-bound)**, **Environmental Foundations**, and **Functional UI Elements**.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               CHARACTER DIRECTION PALETTES                             │
├───────────────┬──────────────────────┬───────────┬──────────────┬──────────────────────┤
│ Direction     │ Character Identity   │ Primary   │ Highlight    │ Shadow Rim           │
├───────────────┼──────────────────────┼───────────┼──────────────┼──────────────────────┤
│ NORTH (▲)     │ Strawberry Blobby    │ `#FF4D6D` │ `#FFA8BA`    │ `#C9184A`            │
│ EAST (▶)      │ Blueberry Drop       │ `#00B4D8` │ `#90E0EF`    │ `#0077B6`            │
│ SOUTH (▼)     │ Kiwi Gummy           │ `#06D6A0` │ `#A7F3D0`    │ `#059669`            │
│ WEST (◀)      │ Lemon Drop           │ `#FFB703` │ `#FDE68A`    │ `#D97706`            │
│ SPECIAL (1x2) │ Grape Slime (Eel)    │ `#9D4EDD` │ `#E0AAFF`    │ `#5A189A`            │
│ BOSS (2x2)    │ Honeycomb King Jelly │ `#FB8500` │ `#FED7AA`    │ `#C2410C`            │
└───────────────┴──────────────────────┴───────────┴──────────────┴──────────────────────┘
```

#### Environmental & Neutral Palettes (Day / Sweet Meadow Theme)
- **App Canvas Background:** Gradient from `#FDFBF7` (warm cream) to `#EBF7EE` (soft mint meadow).
- **Game Board Tray Background:** `#D8EEDC` with inner inset border `#B7E4C7`.
- **Empty Grid Tile Surface:** `#FFFFFF` with `40%` opacity and `1px` inner border `#FFFFFF` (`60%` opacity).
- **Meadow Trim & Flora:** Accents of `#52B788`, soft buttercup yellow `#FEE440`, and petal pink `#FFCCD5`.

#### Functional UI Colors
- **Mistake Hearts (Active):** Glossy Ruby Red `#E63946` with white specular heart highlight.
- **Mistake Hearts (Lost/Empty):** Translucent Charcoal `#6C757D` with `30%` opacity.
- **Success / Primary CTA Button:** Electric Mint `#2EC4B6` to `#06D6A0` gradient, shadow `#059669`.
- **Secondary CTA / Boosters:** Bubblegum Rose `#FF70A6` to `#FF4D6D`, shadow `#C9184A`.
- **Coin & Rewards Gold:** `#FFD166` to `#FFB703`, shadow `#D97706`.

---

### 2.2 Shading, Gloss & Specular Tokens

Every jelly character and interactive button uses a 3-layer glossy shader hierarchy:

1. **Base Body:** Solid radial gradient from `[Highlight Color]` (top-left 25%) to `[Primary Color]` to `[Shadow Rim]` (bottom edge).
2. **Top Specular Shine (The "Gelatin Glaze"):**
   - An elongated horizontal oval situated in the upper 30% of the creature body.
   - Fill: `#FFFFFF` at `55%` opacity, feathered blur `1.5px`.
   - Secondary micro-dot shine: `#FFFFFF` at `80%` opacity on the top-right cheek.
3. **Contact Shadow:**
   - Drop shadow offset: `Y = +6px`, `Blur = 8px`, Color: `rgba(20, 50, 30, 0.16)`.
   - Increases to `Y = +14px`, `Blur = 16px` when lifted or floating.

---

### 2.3 Typography Scale

Use rounded, friendly, high-legibility typefaces (e.g., **Fredoka**, **Nunito**, or **Quicksand**):

| Token | Family | Weight | Size | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `display-hero` | Fredoka / Nunito | Black (900) | 36 px | 44 px | Victory headlines ("STAGE CLEARED!") |
| `title-large` | Fredoka / Nunito | ExtraBold (800) | 24 px | 30 px | Modal titles, Stage banners |
| `title-medium` | Nunito | Bold (700) | 18 px | 24 px | Section headers, Level numbers |
| `body-bold` | Nunito | SemiBold (600) | 14 px | 20 px | Button labels, helper text |
| `hud-number` | Fredoka / Outfit | Black (900) | 16 px | 16 px | Mistake counter, remaining count, timer |
| `caption-micro`| Nunito | Bold (700) | 10 px | 14 px | Badge tags, sub-labels |

---

### 2.4 Component Shape Language
- **Border Radius:**
  - Standard Grid Tile: `20 px`
  - Floating Card / Tray: `28 px`
  - Action Buttons: `24 px` (Pill or soft squircle)
  - Dialog Modals: `32 px`
- **Button Depth:**
  - 3D Push Button style: A `4px` darker solid shadow lip at the bottom of the button.
  - Active/Pressed state: Translates down `+3px` with lip reduced to `1px`, delivering real tactile feedback.

---

## 3. Character Anatomy & Directional Cues (The Anti-Arrow Innovation)

```
                       [ Tapered Antenna / Snout ]
                         (Points North/Escape Vector)
                                  ▲
                             .─────────.
                            /    (o)    \    <── Top Specular Shine
                           /  (◕)   (◕)  \   <── Awake Sparkling Eyes (Unblocked!)
                          │       ▽       │  <── Cheerful Open Smile
                          │               │
                           \             /
                            '───────────'
                                 ───
                         [ Contact Shadow ]
```

### 3.1 Anatomical Parts
1. **The Directional Nose / Tip:**
   - Instead of drawing an arrow line, the jelly's body shape is an **organic tapered droplet**.
   - The tip of the droplet faces North, South, East, or West.
   - For extra clarity, a tiny matching candy antenna or fruit stem sits at the leading tip.
2. **The Living Face (Dynamic Eyelid States):**
   - **BLOCKED STATE (Trapped behind an obstacle):**
     - Eyes are cute closed sleepy arcs: `( ˘◡˘ )` or `( -_- )`.
     - Mouth is a soft closed smile or relaxed line.
     - Visual meaning: *"I'm resting, path is blocked, don't tap me yet!"*
   - **FREE STATE (Clear path to the screen edge):**
     - Eyes **pop wide open** with bright cartoon pupils and star glints: `( ◕‿◕ )`!
     - Surrounding body has a soft, pulsing golden aura ring (`ring-2 ring-white/80`).
     - Visual meaning: *"My path is clear! Tap me to launch!"*
   - **TAPPED ESCAPING STATE (Launching):**
     - Eyes squint with joyful glee: `( >‿< )` with open mouth `( ▽ )`.
     - Body deforms with dramatic squish-and-stretch.
   - **ERROR / BONK STATE (Player tapped when blocked):**
     - Eyes show dizzy spirals `( @ _ @ )` or squished wince `( >_< )`.
     - Body recoils horizontally with quick wobble shake.

### 3.2 Multi-Tile Creatures
- **1x1 Standard Jelly (Blobby):** The primary puzzle unit (70% of board). Quick, nimble, standard single-tile clearance.
- **1x2 Gummy Caterpillar / Moray Eel:** Occupies 2 contiguous cells. Both cells in its forward line must be clear for it to slide out.
- **2x2 King Jelly (Giant Slime):** Occupies 4 cells (2x2 square). Requires a full 2-lane wide corridor to escape. Clearing it provides a huge dopamine payoff with multi-colored confetti.

---

## 4. Master Screen Specifications (Sketch Artboard Blueprints)

### Artboard 1: `Game_Screen_Active` (390 × 844 px)

This is the core gameplay screen where 95% of user time is spent.

```
┌──────────────────────────────────────────────────────────┐  0 px
│ [⚙️]          STAGE 24 - SWEET MEADOW         [🔊] [📳]   │  StatusBar + Header
│                ❤️❤️❤️  (3 Lives)                          │  Top HUD (H: 88 px)
├──────────────────────────────────────────────────────────┤
│                                                          │
│                     [ Meadow Banner ]                    │
│                 "TAP UNBLOCKED JELLIES"                  │
│                                                          │
│     ┌──────────────────────────────────────────────┐     │
│     │  [ 6x6 Soft Enamelled Tray with Inset Glow ] │     │
│     │                                              │     │
│     │   [🌸]      [👀▲]     [😴▲]     [  ]     [🌸]  │     │
│     │                                              │     │
│     │   [😴▶]     [👀▶]     [🪨]      [😴▼]    [  ]  │     │  Game Board Area
│     │                                              │     │  (W: 340 px, H: 340 px)
│     │   [  ]      [😴◀]     [👀▲]     [  ]     [👀▶] │     │  Centered vertically
│     │                                              │     │
│     │   [👀▼]     [  ]      [😴◀]     [👀◀]    [  ]  │     │
│     │                                              │     │
│     └──────────────────────────────────────────────┘     │
│                                                          │
│              ✨ 4 Jellies Awake & Ready!                 │  Status Hint Line
│                                                          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│      [ 💡 Hint (3) ]   [ 🪄 Squish Wand ]   [ ↺ Reset ]   │  Bottom Action Dock
│                                                          │  (H: 96 px)
└──────────────────────────────────────────────────────────┘  844 px
```

#### Detailed Specs for Artboard 1:
1. **Header Region (`Y: 44px - 132px`):**
   - Background: Transparent with subtle soft-focus meadow foliage at top corners.
   - Left: Rounded circular icon button `40x40 px` (`#FFFFFF` with `#D8EEDC` border) containing Settings Gear.
   - Center: Level Badge pill (`#FFFFFF` background, `#2EC4B6` border `2px`, bold typography `STAGE 24`). Directly beneath: 3 glossy 3D heart sprites (`28x28 px` each).
   - Right: Sound toggle and Haptic toggle icon buttons (`36x36 px`).
2. **Game Board Region (`Y: 160px - 580px`):**
   - Centered container: `350 × 350 px`, corner radius `32 px`.
   - Tray material: Deep candy enamel `#C7E9C0` with soft inner shadow `Y = +3px, Blur = 6px, #A7D79E`.
   - Grid layout: 6 columns × 6 rows. Cell size: `48 × 48 px`, spacing: `6 px`.
   - Background grid tiles: `#FFFFFF` at `35%` opacity, corner radius `14 px`.
   - Environmental decorations: Tiny illustrated buttercups, four-leaf clovers, and daisies peeking out of the 4 tray corners.
3. **Dynamic Feedback Region (`Y: 590px - 640px`):**
   - Soft pill container: `#FFFFFF` with `70%` opacity, corner radius `16 px`.
   - Text: `✨ 4 Jellies Awake & Ready!` in `#059669` (bold 13px).
4. **Bottom Dock Region (`Y: 660px - 780px`):**
   - Container: Suspended floating white capsule (`350 × 80 px`), corner radius `28 px`, shadow `0 12px 32px rgba(0,0,0,0.08)`.
   - Button 1 (Hint): Golden lightbulb icon, label `Hint`, with red pill badge `3` at top right.
   - Button 2 (Squish Wand): Magic star wand icon, label `Wand`, with gold pill badge `1`.
   - Button 3 (Restart): Circular refresh icon, label `Retry`.

---

### Artboard 2: `Modal_Level_Complete` (390 × 844 px)

Overlay screen triggered upon clearing the final jelly from the board.

```
┌──────────────────────────────────────────────────────────┐
│              [ Dark Translucent Dimmer (60%) ]           │
│                                                          │
│        ┌────────────────────────────────────────┐        │
│        │          🎉 [ CONFETTI BURST ]         │        │
│        │                                        │        │
│        │             ⭐   ⭐   ⭐               │        │
│        │          (3 Gold Embossed Stars)       │        │
│        │                                        │        │
│        │             STAGE CLEARED!             │        │
│        │         "Flawless Sweet Run!"          │        │
│        │                                        │        │
│        │      ┌──────────────────────────┐      │        │
│        │      │ Score:       12,450 pts  │      │        │
│        │      │ Jelly Bonus: +300 🍬    │      │        │
│        │      └──────────────────────────┘      │        │
│        │                                        │        │
│        │        [ ➔ NEXT LEVEL (Huge) ]         │        │
│        │                                        │        │
│        │             [ ↺ Replay ]               │        │
│        └────────────────────────────────────────┘        │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

#### Detailed Specs for Artboard 2:
- **Modal Card:** `320 × 440 px`, corner radius `36 px`, fill `#FFFFFF`, border `3px` solid `#FFCCD5`.
- **Stars Arch:** 3 golden 3D cartoon stars hovering over the card. Center star is `56x56 px`, left and right stars are `44x44 px`. Emits warm yellow particle rays.
- **Mascot Blob:** Happy pink strawberry jelly at the center doing a celebratory jump with confetti.
- **Score Capsule:** Soft cream inset box `#FDF8F0`, rounded `18 px`. Shows score rolling animation counter.
- **Next Level Button:** `260 × 60 px`, corner radius `30 px`, gradient from `#2EC4B6` to `#06D6A0`, text in white `Fredoka 18px Bold`.
- **Replay Button:** Plain ghost text link in `#6C757D` `Nunito 14px SemiBold`.

---

### Artboard 3: `Modal_Game_Over_Lives` (390 × 844 px)

Triggered when the player taps 3 blocked jellies and exhausts all hearts.

```
┌──────────────────────────────────────────────────────────┐
│              [ Dark Translucent Dimmer (60%) ]           │
│                                                          │
│        ┌────────────────────────────────────────┐        │
│        │                  🥺                    │        │
│        │         (Sad Jelly with Bandaid)       │        │
│        │                                        │        │
│        │              OUT OF HEARTS!            │        │
│        │       "The jellies got squished!"      │        │
│        │                                        │        │
│        │   ┌────────────────────────────────┐   │        │
│        │   │  ▶️ WATCH AD: GET +3 HEARTS ❤️ │   │        │
│        │   └────────────────────────────────┘   │        │
│        │                                        │        │
│        │   ┌────────────────────────────────┐   │        │
│        │   │  🍬 SPEND 50 CANDIES TO REVIVE │   │        │
│        │   └────────────────────────────────┘   │        │
│        │                                        │        │
│        │             [ Give Up / Retry ]        │        │
│        └────────────────────────────────────────┘        │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

#### Detailed Specs for Artboard 3:
- **Modal Card:** `320 × 420 px`, fill `#FFFFFF`, corner radius `36 px`, border `3px` solid `#FFE5EC`.
- **Mascot Illustration:** Sad little blue jelly with teary anime eyes and a tiny criss-cross bandaid on its cheek.
- **Primary Revive Button (Rewarded Video):** Green gradient `#2EC4B6`, video play badge, white text: `CONTINUE WITH 3 HEARTS`.
- **Secondary Coin Revive Button:** Amber gradient `#FFB703`, candy coin icon, text: `50 GEMS`.
- **Decline Button:** Subtle link text `Give Up and Restart`.

---

### Artboard 4: `Saga_Progression_Map` (390 × 844 px)

Stage selection roadmap providing long-term progression motivation.

```
┌──────────────────────────────────────────────────────────┐
│ [Profile / Avatar]    🍬 1,420     ❤️ Full (5)    [Shop] │  Top Currency Header
├──────────────────────────────────────────────────────────┤
│                                                          │
│                      STAGE 28 🔒                         │
│                           \                              │
│                            STAGE 27 🔒                   │
│                            /                             │
│                     STAGE 26 (⭐ 3)                      │
│                          │                               │
│                     STAGE 25 (⭐ 2)                      │
│                           \                              │
│         [ 🍓 Current Mascot Pin: STAGE 24 ]              │  Active Level Node
│                           /                              │  (Bouncing Jelly Pin)
│                     STAGE 23 (⭐ 3)                      │
│                          │                               │
│                     STAGE 22 (⭐ 3)                      │
│                                                          │
├──────────────────────────────────────────────────────────┤
│  [ 🗺️ Map ]       [ 🏆 Daily Challenge ]     [ 👗 Skins ] │  Bottom Navigation Bar
└──────────────────────────────────────────────────────────┘
```

#### Detailed Specs for Artboard 4:
- **Map Path:** Winding cobblestone/stepping-stone trail through lush green grass, passing across wooden bridges, bubbling strawberry soda springs, and honeycomb trees.
- **Level Nodes:**
  - Completed: Glossy blue/green circle (`44x44 px`) with 1 to 3 gold mini-stars underneath.
  - Current Active: Enlarged glowing gold circle (`54x54 px`) with the player's selected avatar blob jumping cheerfully on top.
  - Locked: Stone-grey circle (`40x40 px`) with silver padlock icon.
- **Biome Transitions:** Every 20 levels shifts biomes:
  - Levels 1-20: *Sweet Meadow* (Lush green, daisies, buttercups).
  - Levels 21-40: *Soda Lagoon* (Turquoise water, water lilies, bubble streams).
  - Levels 41-60: *Honeycomb Orchard* (Warm amber, honeycombs, floral pollen).
  - Levels 61-80: *Cotton Candy Peak* (Pastel pink/lavender mountain clouds).

---

## 5. Animation Curves & Tactile Physics Guidelines

For the animation designers and CMP developers implementing the Sketch assets:

### 5.1 Idle Breathing (Loop)
- Duration: `2400 ms`, Ease: `easeInOutSine`.
- Transform: `scaleY: 1.0 -> 0.96 -> 1.04 -> 1.0`, `scaleX: 1.0 -> 1.04 -> 0.96 -> 1.0`.

### 5.2 Tap & Squish-Launch Sequence (Unblocked Move)
- **Phase 1: Pre-launch Compression (`0 - 80 ms`):**
  - Creature squishes down toward the board: `scaleY = 0.65`, `scaleX = 1.35`.
  - Color brightens `+15%` brightness.
- **Phase 2: Explosive Spring Release (`80 - 220 ms`):**
  - Creature stretches aggressively forward in travel direction: `scaleAlongVector = 1.55`, `scaleOrthogonal = 0.70`.
  - Audio trigger: High-pitched waterdrop pop (`650 Hz` rising to `1100 Hz`).
- **Phase 3: Ocean/Meadow Escape Flight (`220 - 450 ms`):**
  - Smooth linear translation with cubic overshoot: `cubic-bezier(0.25, 0.9, 0.3, 1.0)`.
  - Spawns 4-6 trailing mini-droplet particles that fade over `200 ms`.
  - Alpha fades from `1.0` to `0.0` as it crosses the canvas margin.

### 5.3 Collision & Wobble Sequence (Blocked Move)
- **Phase 1: Forward Bonk (`0 - 60 ms`):**
  - Moves forward `8 px` into the blocking jelly, squishing its leading face flat (`scaleAlongVector = 0.85`).
  - Audio trigger: Dull rubbery thud (`120 Hz` falling to `50 Hz`).
  - Haptic trigger: Dual-pulse heavy vibration (`50 ms` on, `40 ms` off, `50 ms` on).
- **Phase 2: Damped Harmonic Rebound (`60 - 320 ms`):**
  - Oscillates back to rest: `-6 px -> +4 px -> -2 px -> 0 px`.
  - Eyes display wince expression `( >_< )` for `350 ms` before returning to sleep state.

---

## 6. Asset Export Checklist for Stitch / Sketch

When generating Sketch artboards and slice assets, ensure the following exports are provided:

### 6.1 Vector Sprite Assets (SVG + PDF Vector + @2x/@3x PNG)
- [ ] `jelly_strawberry_n.svg` (Facing North, Awake & Asleep states)
- [ ] `jelly_strawberry_s.svg` (Facing South, Awake & Asleep states)
- [ ] `jelly_strawberry_e.svg` (Facing East, Awake & Asleep states)
- [ ] `jelly_strawberry_w.svg` (Facing West, Awake & Asleep states)
- [ ] `jelly_blueberry_e.svg` (Facing East, Awake & Asleep states)
- [ ] `jelly_kiwi_s.svg` (Facing South, Awake & Asleep states)
- [ ] `jelly_lemon_w.svg` (Facing West, Awake & Asleep states)
- [ ] `jelly_grape_1x2.svg` (Elongated vertical and horizontal multi-tile)
- [ ] `jelly_king_2x2.svg` (Giant 4-cell boss slime)
- [ ] `obstacle_rock.svg` (Indestructible mossy meadow stone)
- [ ] `obstacle_candycane.svg` (Indestructible sweet barrier)

### 6.2 UI Sprites & Badges
- [ ] `heart_full.svg` (Glossy 3D red heart with shine)
- [ ] `heart_empty.svg` (Translucent grey outline heart)
- [ ] `button_primary_mint.svg` (3D pushable button with base & lip states)
- [ ] `button_secondary_rose.svg`
- [ ] `icon_hint_lightbulb.svg`
- [ ] `icon_squish_wand.svg`
- [ ] `icon_refresh.svg`
- [ ] `star_gold_3d.svg` (Full, half, and empty states for level ratings)
- [ ] `dialog_card_bg.svg` (32px rounded modal background with drop shadow)

---

## 7. Hand-off Instructions for the Stitch Agent

When feeding this document to the Stitch agent, provide the following instruction prompt:

```text
Please create the complete mobile UI/UX design in Sketch for "Squish Out" according to the attached SQUISH_OUT_DESIGN_SPEC.md specification. 

Focus on creating 4 primary artboards (iPhone 15/16 - 390x844px):
1. Game_Screen_Active: The live gameplay board featuring the 6x6 candy tray, kawaii jelly creatures with awake/asleep eye states, top HUD with hearts, and bottom action dock.
2. Modal_Level_Complete: Celebratory victory popup with 3 golden stars, jumping mascot blob, score rollup, and glossy green "Next Level" button.
3. Modal_Game_Over_Lives: Out-of-lives dialog featuring the cute sad jelly with a bandaid, rewarded video revive button, and coin retry.
4. Saga_Progression_Map: Winding meadow stepping-stone roadmap with numbered level nodes, stars, and avatar pin.

Ensure every component adheres to the "Juicy Kawaii Tactile Jelly" aesthetic with soft 3D bevels, spherical specular highlights, and pastel-candy color tokens specified in Section 2.
```
