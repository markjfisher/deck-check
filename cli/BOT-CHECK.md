Bot Check is a discord bot for displaying information about a deck
directly in a discord channel

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
