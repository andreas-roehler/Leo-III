Leo-III
=======

Higher-order Theorem Prover

Common SBT tasks
----------------

[SBT](http://www.scala-sbt.org/) manages the build process of Leo-III. Download SBT [here](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt).

To start an interactive REPL use:

    > sbt console

The package `datastructures.tptp.Commons` and all commands from the `LeoShell` object are included. See the API documentation for more informations. How to generate the API see next SBT task.

Generate the API documentation with:

    > sbt doc

The API is placed in target/scala-2.10/api/index.html.

Tests could be executed with:

    > sbt test

Some other, may useful tasks:

 Task          | Effect
:--------------|:-------------------------------------
 clean         | Deletes all files generated by SBT
 compile       | Compiles all sources and prints all errors and warnings
 eclipse       | Creates Eclipse project files
 gen-idea      | Creates Idea project file
 publish-local | Publish Leo-III to the local repository

SBT without any tasks opens a console where multiply task could be requested.

An additional feature of SBT are the continues tasks. With the prefix `~` executes the task and waits for any updates of the source code.

