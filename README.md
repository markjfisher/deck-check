# deck-check

A very simple command line application to check card decks out from import codes

Current Version: 

## example output

    ▶ ./deck-check SPACgOsjAAAQfsbSgUgpmToZpqfrfxkpmefqfBjiiuhO
    
    Rarity:
      Common, count: 24
      Rare, count: 9
      Epic, count: 15
      Legendary, count: 2
    
    Action, count: 3
      Edict of Azura, count: 3, [4/-/-], attr: [Willpower, Endurance]
    
    Item, count: 0
    
    Support, count: 3
      Halls of the Dwemer, count: 3, [6/-/-], attr: [Neutral]
    
    Total unique cards: 18
    Total cards: 50
    
    Set Types:
      Return to Clockwork City: 13
      Houses of Morrowind: 1
      Core Set: 24
      Heroes of Skyrim: 6
      Monthly Rewards: 6
    
    Creature, count: 44
      Blackreach Rebuilder, count: 3, [1/0/1], attr: [Neutral]
      Dwarven Dynamo, count: 3, [4/2/1], attr: [Neutral]
      Dwarven Sphere, count: 3, [3/2/3], attr: [Neutral]
      Dwarven Spider, count: 3, [0/0/3], attr: [Neutral]
      Eastmarch Crusader, count: 3, [3/4/2], attr: [Willpower]
      Fifth Legion Trainer, count: 3, [2/1/3], attr: [Willpower]
      Galyn the Shelterer, count: 1, [3/3/3], attr: [Endurance]
      Gearwork Spider, count: 3, [1/1/1], attr: [Neutral]
      Hulking Fabricant, count: 3, [5/5/5], attr: [Endurance]
      Kagouti Fabricant, count: 3, [4/3/3], attr: [Willpower]
      Mechanical Ally, count: 3, [3/3/3], attr: [Neutral]
      Pit Lion, count: 3, [3/5/5], attr: [Willpower]
      Reflective Automaton, count: 3, [2/2/3], attr: [Neutral]
      Spider Worker, count: 3, [2/0/1], attr: [Neutral]
      Steam Constructor, count: 3, [2/2/2], attr: [Neutral]
      Yagrum Bagarn, count: 1, [3/2/4], attr: [Neutral]
    
    Mana Curve
    
     0   | 1 ██████████
     1   | 2 ████████████████████
     2   | 4 ████████████████████████████████████████
     3   | 6 ████████████████████████████████████████████████████████████
     4   | 3 ██████████████████████████████
     5   | 1 ██████████
     6   | 1 ██████████
     7+  | 0 ▏


## Requirements

You need JAVA (8+) installed and available on the command line, or JAVA_HOME set as an environment variable.

## Usage from source

### linux/mac

    # From source
    ./gradlew run --args="SPAJsIuptdtrtDutsYsUsABBtLtmsvuAsTudsPtjsJsVsWtvsRuntqtHsFsLursQukuqsEtctytauvAEtttAtuts"
    
    ## OR
    
    # Create a distribution...
    ./gradlew distZip

    # ... copy it somewhere
    cp build/distributions/deck-check-1.0.0.zip /some/target/dir
    cd /some/target/dir
    unzip deck-check-1.0.0.zip

    # run it
    cd deck-check-1.0.0/bin
    ./bin/deck-check SPAJsIuptdtrtDutsYsUsABBtLtmsvuAsTudsPtjsJsVsWtvsRuntqtHsFsLursQukuqsEtctytauvAEtttAtuts

### windows from source

As this is a gradle based application, it should just be a case of replacing
all the ./gradlew commands above with gradle.bat in the above, and adjust
the commands for cd/unzip above to windows versions.

I haven't however tried it in windows.

## Usage from a release zip file

1. Get a copy of the zip file
2. Unzip it to a directory
3. (on command line) cd into the directory that contains the bin-check.bat file
4. (on the command line) run:
   ```bash
   deck-check.bat SPAJsIuptdtrtDutsYsUsABBtLtmsvuAsTudsPtjsJsVsWtvsRuntqtHsFsLursQukuqsEtctytauvAEtttAtuts
   ```
