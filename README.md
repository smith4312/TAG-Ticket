TAG Ticketing System
=====


Run the webapp
-----

At the terminal in the project directory, type

```
activator run
```

When you change the code and refresh the page, it will be compiled and reloaded automatically.
If there is an error it will be displayed in the browser.



Database migrations
-----

Database migrations are stored as `.sql` files in `conf/db/migration`. The filename should be `Vnn__a_description.sql` where _nn_ is the version number and _a_description_ is the description of what the migration does, with underscores in place of spaces. The filename must start with a `V` and the version number must be separated from the description with two underscores.

If your database is already populated up to some version, you need to _baseline_ it, to tell Flyway what version it's already up to. To do so, at the activator/sbt prompt, enter

```
runMain RunFlyway baseline <version>
```

where _version_ is the version that Flyway should skip up to and including.


To run whatever migrations are pending, at the activator/sbt prompt, enter

```
runMain RunFlyway migrate
```


For a full list of available commands do

```
runMain Flyway
```

without arguments.
