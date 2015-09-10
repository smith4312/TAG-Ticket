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

Database migrations are stored as `.sql` files in `migrations/src/main/resources/db/migration`. The filename should be `Vnn__a_description.sql` where _nn_ is the version number and _a_description_ is the description of what the migration does, with underscores in place of spaces. The filename must start with a `V` and the version number must be separated from the description with two underscores.

If your database is already populated up to some version, you need to _baseline_ it, to tell Flyway what version it's already up to. To do so, at the activator/sbt prompt, enter

```
set flywayBaselineVersion in migrations := "<version>"
migrations/flywayBaseline
```

where _version_ is the version that Flyway should skip up to and including.


To run whatever migrations are pending, at the activator/sbt prompt, enter

```
migrations/flywayMigrate
```

To see the current status of all migrations, run

```
migrations/flywayInfo
```

Wipe out the entire database:

```
migrations/flywayClean
```

Partial migration:

```
set flywayTarget in migrations := "<version>"
migrations/flywayMigrate
```

For a full list of available commands and their documentation see http://flywaydb.org/documentation/sbt/ (commands must be prefixed with `migrations`
as above, since the flyway plugin is enabled within the `migrations` subproject).
