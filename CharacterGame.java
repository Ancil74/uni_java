import java.util.*;
import java.util.stream.*;
import java.text.DecimalFormat;
import rpg.abilities.SpecAb;

class InvalidStatException extends Exception {
    InvalidStatException(String msg) { super(msg); }
}

class InsufficientManaException extends RuntimeException {
    InsufficientManaException(String msg) { super(msg); }
}

class GameCharacter implements Comparable<GameCharacter> {
    String name;
    int hp, def, atk;
    boolean isEnemy = false;
    static final DecimalFormat df = new DecimalFormat("#.##");

    GameCharacter(String n, int h, int d, int a) throws InvalidStatException {
        if (n == null || n.isBlank()) throw new InvalidStatException("Name cannot be blank.");
        if (h <= 0) throw new InvalidStatException("HP must be positive.");
        if (d < 0 || a < 0) throw new InvalidStatException("Defense and attack cannot be negative.");
        this.name = n;
        this.hp = h;
        this.def = d;
        this.atk = a;
    }

    GameCharacter(String n) throws InvalidStatException {
        this(n, 50, 5, 5);
    }

    synchronized void damage(int amt) {
        hp -= amt;
    }

    synchronized void heal(int amt) {
        hp += amt;
    }

    synchronized int getHp() {
        return hp;
    }

    int threat() {
        return atk + def;
    }

    @Override
    public int compareTo(GameCharacter other) {
        return Integer.compare(this.hp, other.hp);
    }

    public int attack() {
        return attack(1, 0);
    }

    public int attack(int strikes, int targetDef) {
        System.out.println(name + " strikes " + strikes + " time(s)");
        int totdmg = 0;
        for (int i = 1; i <= strikes; i++) {
            double raw = (atk * 1.15) + (i * 5);
            double mitigated = Math.max(2, raw - (targetDef * 0.5));
            totdmg += mitigated;
            System.out.println("Strike " + i + " dealt " + df.format(mitigated) + " damage");
        }
        return totdmg;
    }

    public int enemyatk(int strikes, double dmgMult) {
        System.out.println("The enemy is about to strike " + name + " " + strikes + " times");
        Random r = new Random();
        Scanner s = new Scanner(System.in);
        int max = 5, min = 1;
        int totdmg = 0;
        for (int i = 1; i <= strikes; i++) {
            double dmg = (atk * 1.4) - (def * 0.6);
            int d = r.nextInt(max - min + 1) + min;
            dmg += (i * d);
            dmg *= dmgMult;

            System.out.println("Guess the right number from 1-5 to defend yourself");
            int guess = -1;
            boolean valid = false;
            while (!valid) {
                try {
                    guess = s.nextInt();
                    valid = true;
                } catch (InputMismatchException e) {
                    System.out.println("That's not a whole number. Enter 1-5:");
                    s.next();
                }
            }

            if (guess == d) {
                System.out.println("You guessed right! You parried the enemy's strike");
            } else {
                totdmg += Math.max(1, (int) dmg);
                System.out.println("Strike " + i + " dealt " + df.format(dmg) + " damage");
            }
        }
        return totdmg;
    }

    public synchronized void status() {
        String title = isEnemy ? "Enemy Status" : "Character Status";
        System.out.println("=== " + title + " ===");
        System.out.println("Name: " + name);
        System.out.println("Attack: " + atk);
        System.out.println("HP: " + hp);
        System.out.println("Defense: " + def);
        System.out.println("========================\n");
    }
}

class Player extends GameCharacter {
    int level;
    int exp;
    int maxHp;

    Player(String n, int h, int d, int a, int level) throws InvalidStatException {
        super(n, h, d, a);
        this.level = level;
        this.exp = 0;
        this.maxHp = h;
    }

    @Override
    synchronized void heal(int amt) {
        hp = Math.min(maxHp, hp + amt);
    }

    void gainExp(int amount) {
        exp += amount;
        System.out.println(name + " gained " + amount + " EXP");
        while (exp >= level * 100) {
            exp -= level * 100;
            level++;
            atk += 4;
            def += 3;
            maxHp += 20;
            heal(maxHp / 2);
            System.out.println(name + " leveled up! Now level " + level);
            System.out.println(name + " restored 50% of maximum HP.");
        }
    }

    @Override
    public void status() {
        super.status();
        System.out.println("Level: " + level);
        System.out.println("EXP: " + exp + "/" + (level * 100));
        System.out.println("========================\n");
    }
}

abstract class SpecialistPlayer extends Player implements SpecAb {
    int Mana, maxMana;
    final String ManaName;
    final int abilityCost;

    SpecialistPlayer(String n, int h, int d, int a, int level, int Mana, String ManaName, int abilityCost)
            throws InvalidStatException {
        super(n, h, d, a, level);
        this.Mana = Mana;
        this.maxMana = Mana;
        this.ManaName = ManaName;
        this.abilityCost = abilityCost;
    }

    @Override
    public int getAbilityCost() {
        return abilityCost;
    }

    @Override
    public boolean canUseAbility() {
        return Mana >= abilityCost;
    }

    void regenMana(int amount) {
        Mana = Math.min(maxMana, Mana + amount);
    }

    @Override
    public void status() {
        super.status();
        System.out.println(ManaName + ": " + Mana + "/" + maxMana);
        System.out.println("========================\n");
    }
}

class Hero extends SpecialistPlayer {
    Hero(String n, int h, int d, int a, int level, int mana) throws InvalidStatException {
        super(n, h, d, a, level, mana, "Mana", 15);
    }

    @Override
    public int useAbility(int targetDef) {
        if (!canUseAbility()) {
            throw new InsufficientManaException(name + " doesn't have enough mana for a burst attack (needs "
                + getAbilityCost() + ", has " + Mana + ")");
        }
        Mana -= getAbilityCost();
        double raw = (atk * 2.0) + (level * 6);
        double mitigated = Math.max(5, raw - (targetDef * 0.4));
        System.out.println(name + " channels mana for a burst strike, dealing " + df.format(mitigated) + " bonus damage!");
        return (int) mitigated;
    }
}

class Mage extends SpecialistPlayer {
    Mage(String n, int h, int d, int a, int level, int mana) throws InvalidStatException {
        super(n, h, d, a, level, mana, "Mana", 20);
    }

    @Override
    public int useAbility(int targetDef) {
        if (!canUseAbility()) {
            throw new InsufficientManaException(name + " doesn't have enough mana for an arcane bolt (needs "
                + getAbilityCost() + ", has " + Mana + ")");
        }
        Mana -= getAbilityCost();
        double raw = (atk * 2.6) + (level * 4);
        double mitigated = Math.max(5, raw - (targetDef * 0.2));
        System.out.println(name + " unleashes an arcane bolt, dealing " + df.format(mitigated) + " bonus damage!");
        return (int) mitigated;
    }
}

class Rogue extends SpecialistPlayer {
    Rogue(String n, int h, int d, int a, int level) throws InvalidStatException {
        super(n, h, d, a, level, 0, "Combo Points", 5);
        this.maxMana = 5;
    }

    @Override
    public int attack(int strikes, int targetDef) {
        int dmg = super.attack(strikes, targetDef);
        regenMana(strikes);
        System.out.println(name + " builds combo points (" + Mana + "/" + maxMana + ")");
        return dmg;
    }

    @Override
    public int useAbility(int targetDef) {
        if (!canUseAbility()) {
            throw new InsufficientManaException(name + " doesn't have enough combo points for a finisher (needs "
                + getAbilityCost() + ", has " + Mana + ")");
        }
        int points = Mana;
        Mana = 0;
        double raw = (atk * 0.7) * points;
        double mitigated = Math.max(5, raw - (targetDef * 0.3));
        System.out.println(name + " unleashes a " + points + "-point finisher, dealing " + df.format(mitigated) + " bonus damage!");
        return (int) mitigated;
    }
}

class Tank extends Player {
    Tank(String n, int h, int d, int a, int level) throws InvalidStatException {
        super(n, h, d, a, level);
    }

    @Override
    synchronized void damage(int amt) {
        super.damage((int) Math.round(amt * 0.8));
    }
}

class StatusEffect<T extends GameCharacter> implements Runnable {
    private final T target;
    private final int ticks;
    private final int dmgPerTick;
    private final String label;
    private volatile boolean cancelled = false;

    StatusEffect(T target, int ticks, int dmgPerTick, String label) {
        this.target = target;
        this.ticks = ticks;
        this.dmgPerTick = dmgPerTick;
        this.label = label;
    }

    void cancel() {
        cancelled = true;
    }

    @Override
    public void run() {
        for (int i = 0; i < ticks && !cancelled; i++) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (cancelled) return;
            target.damage(dmgPerTick);
            System.out.println("\n[" + label + "] " + target.name + " takes " + dmgPerTick
                + " damage (" + Math.max(target.getHp(), 0) + " HP left)");
        }
    }
}

class Inventory<T> {
    private final Map<T, Integer> items = new HashMap<>();

    void add(T item, int qty) {
        items.merge(item, qty, Integer::sum);
    }

    boolean use(T item) {
        Integer count = items.get(item);
        if (count == null || count <= 0) return false;
        items.put(item, count - 1);
        return true;
    }

    int count(T item) {
        return items.getOrDefault(item, 0);
    }
}

enum Item { HEALTH_POTION, MANA_POTION }

public class CharacterGame {

    static Player createHero(Scanner s) {
        Player hero = null;
        while (hero == null) {
            System.out.println("Enter your hero's name:");
            String heroName = s.nextLine();

            System.out.println("Choose a starting class:");
            System.out.println("1. Warrior (high HP/DEF, mana feuled strike)");
            System.out.println("2. Battlemage (balanced, eXpLoSiOn)");
            System.out.println("3. Mage (low HP, high mana, precision bolt)");
            System.out.println("4. Rogue (combo-point finisher, gains points on attack)");
            System.out.println("5. Tank (very high HP/DEF, no special ability, mitigates all damage)");
            int choice = readInt(s, 1, 5);

            try {
                switch (choice) {
                    case 2 -> hero = new Hero(heroName, 95, 15, 21, 1, 45);
                    case 3 -> hero = new Mage(heroName, 70, 8, 18, 1, 60);
                    case 4 -> hero = new Rogue(heroName, 100, 12, 20, 1);
                    case 5 -> hero = new Tank(heroName, 170, 30, 12, 1);
                    default -> hero = new Hero(heroName, 130, 24, 17, 1, 20);
                }
            } catch (InvalidStatException e) {
                System.out.println("Couldn't create hero: " + e.getMessage() + " Try again.\n");
            }
        }
        return hero;
    }

    static int readInt(Scanner s, int min, int max) {
        while (true) {
            if (s.hasNextInt()) {
                int val = s.nextInt();
                s.nextLine();
                if (val >= min && val <= max) return val;
            } else {
                s.nextLine();
            }
            System.out.println("Enter a number between " + min + " and " + max + ":");
        }
    }

    static boolean readYesNo(Scanner s) {
        while (true) {
            String line = s.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("Please enter y or n:");
        }
    }

    static <T extends GameCharacter> void printRoster(List<T> roster) {
        for (T c : roster) {
            System.out.println("- " + c.name + " (" + Math.max(c.getHp(), 0) + " HP)");
        }
    }

    static void analyzeEnemyRoster(List<GameCharacter> enemies) {
        System.out.println("--- Enemy Roster Analytics ---");

        enemies.forEach(e -> System.out.println("Scouted: " + e.name + " (threat " + e.threat() + ")"));

        List<String> topThreats = enemies.stream()
            .filter(e -> e.threat() > 40)
            .sorted(Comparator.comparingInt(GameCharacter::threat).reversed())
            .limit(3)
            .map(e -> e.name)
            .collect(Collectors.toList());
        System.out.println("Top threats: " + topThreats);

        int dangerousCount = (int) enemies.stream().filter(e -> e.threat() > 30).count();
        double avgHp = enemies.stream().mapToInt(e -> e.hp).average().orElse(0);
        Optional<GameCharacter> deadliest = enemies.stream()
            .max(Comparator.comparingInt(GameCharacter::threat));

        System.out.println("Dangerous enemies: " + dangerousCount);
        System.out.printf("Average enemy HP: %.2f%n", avgHp);
        deadliest.ifPresent(e -> System.out.println("Deadliest enemy: " + e.name + " (threat " + e.threat() + ")"));
        System.out.println("-----------------------------------------\n");
    }

    public static void main(String[] args) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        Scanner input = new Scanner(System.in);
        Player hero = createHero(input);
        hero.status();

        Inventory<Item> bag = new Inventory<>();
        bag.add(Item.HEALTH_POTION, 2);
        bag.add(Item.MANA_POTION, 1);

        List<GameCharacter> enemies;
        try {
            enemies = new ArrayList<>(List.of(
                new GameCharacter("Goblin", 75, 8, 13),
                new GameCharacter("Skeleton", 60, 8, 11),
                new GameCharacter("Orc", 160, 13, 19),
                new GameCharacter("Troll", 190, 13, 22),
                new GameCharacter("Dragon", 280, 20, 29),
                new GameCharacter("Vampire", 140, 18, 25)
            ));
        } catch (Exception e) {
            System.out.println("Fatal error setting up enemies: " + e.getMessage());
            return;
        }
        enemies.sort(Comparator.comparingInt(GameCharacter::threat));

        System.out.println("Enemy roster ahead:");
        printRoster(enemies);
        System.out.println();

        analyzeEnemyRoster(enemies);

        Map<String, Integer> killCount = new LinkedHashMap<>();

        Thread statusThread = null;
        StatusEffect<Player> burnEffect = null;

        for (GameCharacter enemy : enemies) {
            enemy.isEnemy = true;
            System.out.println("A wild " + enemy.name + " appears!");
            enemy.status();

            if (enemy.name.equals("Dragon")) {
                System.out.println(enemy.name + " engulfs the battlefield in flame! "
                    + hero.name + " is now burning.\n");
                burnEffect = new StatusEffect<>(hero, 5, 6, "Burn");
                statusThread = new Thread(burnEffect, "Status-Effect-Thread");
                statusThread.setPriority(Thread.MIN_PRIORITY);
                statusThread.setDaemon(true);
                statusThread.start();
            }

            while (hero.getHp() > 0 && enemy.hp > 0) {
                boolean defending = false;
                boolean turnTaken = false;

                while (!turnTaken) {
                    System.out.println("What will " + hero.name + " do?");
                    System.out.println("1. Attack");
                    System.out.println("2. Defend (halve incoming damage, regen Mana)");
                    System.out.println("3. Check status");
                    System.out.println("4. Use item (Health Potions: " + bag.count(Item.HEALTH_POTION)
                        + ", Mana Potions: " + bag.count(Item.MANA_POTION) + ")");
                    int choice = readInt(input, 1, 4);

                    if (choice == 1) {
                        int atkdmg = hero.attack(3, enemy.def);

                        if (hero instanceof SpecAb ability) {
                            System.out.println("Use special ability for " + ability.getAbilityCost() + " Mana? (y/n)");
                            if (readYesNo(input)) {
                                try {
                                    atkdmg += ability.useAbility(enemy.def);
                                } catch (InsufficientManaException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }

                        enemy.hp -= atkdmg;
                        System.out.println("Total damage dealt: " + atkdmg);
                        System.out.println(enemy.name + " has " + Math.max(enemy.hp, 0) + " HP left\n");
                        turnTaken = true;
                    } else if (choice == 2) {
                        defending = true;
                        if (hero instanceof SpecialistPlayer sp) sp.regenMana(10);
                        System.out.println(hero.name + " braces for impact and regains some Mana.\n");
                        turnTaken = true;
                    } else if (choice == 3) {
                        hero.status();
                    } else {
                        System.out.println("1. Health Potion (heal 30 HP)");
                        System.out.println("2. Mana Potion (restore 15 Mana)");
                        int itemChoice = readInt(input, 1, 2);
                        if (itemChoice == 1) {
                            if (bag.use(Item.HEALTH_POTION)) {
                                hero.heal(30);
                                System.out.println(hero.name + " drinks a health potion and heals 30 HP.\n");
                            } else {
                                System.out.println("No health potions left!\n");
                            }
                        } else {
                            if (hero instanceof SpecialistPlayer sp) {
                                if (bag.use(Item.MANA_POTION)) {
                                    sp.regenMana(15);
                                    System.out.println(hero.name + " drinks a mana potion and restores 15 Mana.\n");
                                } else {
                                    System.out.println("No mana potions left!\n");
                                }
                            } else {
                                System.out.println(hero.name + " has no Mana pool to restore!\n");
                            }
                        }
                    }
                }

                if (enemy.hp <= 0) {
                    System.out.println(enemy.name + " has been defeated!\n");
                    killCount.merge(enemy.name, 1, Integer::sum);
                    int healAmount = (int) (hero.getHp() * 0.25);
                    int hpBefore = hero.getHp();
                    hero.heal(healAmount);
                    int hpRestored = hero.getHp() - hpBefore;
                    if (hpRestored > 0) {
                        System.out.println(hero.name + " regained " + hpRestored
                            + " HP after the victory!\n");
                    }
                    if (hero instanceof SpecialistPlayer sp) {
                        int y = sp.Mana;
                        sp.regenMana(5);
                        int x = sp.Mana - y;
                        if (x > 0) {
                            System.out.println(hero.name + " restored 5 Mana after the victory.\n");
                        }
                    }
                    hero.gainExp(70);
                    break;
                }

                double mitigation = defending ? 0.5 : 1.0;
                int losthp = hero.enemyatk(3, mitigation);
                hero.damage(losthp);
                System.out.println("Total damage taken: " + losthp);
                System.out.println(hero.name + " has " + Math.max(hero.getHp(), 0) + " HP left\n");

                if (hero.getHp() <= 0) {
                    System.out.println(hero.name + " has been defeated!");
                    break;
                }
            }

            if (burnEffect != null) {
                burnEffect.cancel();
                statusThread.interrupt();
                try {
                    statusThread.join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                statusThread = null;
                burnEffect = null;
            }

            if (hero.getHp() <= 0) {
                break;
            }
        }

        System.out.println("Kills:");
        for (Map.Entry<String, Integer> entry : killCount.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue());
        }

        if (hero.getHp() > 0) {
            System.out.println(hero.name + " survived the battles with " + hero.getHp() + " HP left!");
            hero.status();
        } else {
            System.out.println(hero.name + " failed to clear the dungeon.");
        }
    }
}