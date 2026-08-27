# HAUL YEA!

Authors:

* Ulziibayar Borokhul PRUA5W
* Dua Arshad CT9PHI
* Elene Rukhadze HN53H0
* Oyunbold Ganbold  DDJIJZ

## 1. Game Description

The game is a transportation and economic simulation played on a grid-based map. Players begin on a predefined map featuring cities, buildings, and existing roads, which they can expand by constructing new roads and establishing transport routes. Vehicles operate continuously on assigned circular routes, delivering goods and passengers. The central challenge revolves around designing profitable transport networks and managing resources effectively.

## 2. Subtasks

* Forests \<0.5 complexity\>
* Rivers and Lakes \<0.5 complexity\>
* Minimap \<0.5 complexity\>
* Traffic Lights \<1 complexity\>
* Continuous movement \<0.5 complexity\>
* 2.5D graphics \<0.5 complexity\>

## 3. Functional Requirements

### 3.1 Game Mechanics

The game uses a 2D grid-based map with fixed Cities (min. 3×3 tiles) and facilities (min. 2×2 tiles), where players can build roads, place stops. Facilities produce/consume goods; cities require passengers/products.

Vehicles operate only on connected roads, automatically loading/unloading goods and passengers, and follow rules such as one-vehicle-per-direction per tile and gradually changing demand for goods and passengers. Real-time gameplay with pause, normal, fast, very fast speeds.

### 3.2 Player Actions

Players can start a game, build/remove roads, place stops, purchase vehicles, create/modify routes, assign vehicles, adjust speed, and monitor the financial and network status.

### 3.3 Scoring and Economy

Economy includes starting capital, income from deliveries, expenses for construction, purchasing, and maintenance. Game ends when capital becomes negative (bankruptcy).

**Profit = income − operational costs.**

## 4. Non-Functional Requirements

### 4.1 Performance

The game must run smoothly on standard PCs, supporting maps of at least 50×50 tiles, around 15 vehicles, and multiple traffic lights, ensuring stable memory usage, responsive speed switching, and error-free saving/loading. It requires visually distinct elements (roads, terrain types, traffic lights), clear 2.5D readability, accurate mini map representation, and descriptive error messages.

### 4.2 Reliability and Maintainability

Reliability ensures no crashes during construction, saving, or speed switching, while save/load must restore all map states, vehicle positions, timers, and economic status consistently without creating invalid game states.

Maintainability requires modular architecture separating logic, rendering, UI, and persistence, along with consistent code formatting and version-controlled development.2.5D visual quality must preserve correct height/depth perception, smooth vehicle movement, accurate bridge rendering, and graphical behavior that does not affect gameplay logic timing.

### 4.3 Language and Tools

Java, JavaFX, VScode, IntelliJ.
