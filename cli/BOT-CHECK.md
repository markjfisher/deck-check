Bot Check is a discord bot for displaying information about a deck
directly in a discord channel. Also supports tournament creating and checking.

To enable, set the following value

    deck-check.bot.token=your-token-here

This can be done using one of:

1. Environment variable
    ```
    DECK_CHECK_BOT_TOKEN=your-token-here
    ```
2. System Property
    ```
    -Ddeck-check.bot.token=your-token-here
    ```
3. Via gradle if running by source.
   Edit your ~/.gradle/gradle.properties file and add a property
   ```properties
   deck-check.bot.token=your-token-here
   ```

# Building

To build a distribution release run:

    ./gradlew distZip -Pbot-check

and copy the zip from `cli/build/distributions/bot-check-<VERSION>.zip`