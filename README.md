# Welcome to CliHats

![Maven Central](https://img.shields.io/maven-central/v/io.github.johannesbuchholz/clihats?style=for-the-badge)

CliHats is a java library that lets you easily expose aspects of your java code to the command line. The command-line interface becomes a hat you simply put on your program.

CliHats offers
- Declarative and functional API
- Automatic selection of commands based on user inputs
- Option parsing
- Effortless help and error messages

## Why CliHats?

In contrary to other parameter *parsers* like [JCommander](https://github.com/cbeust/jcommander), CliHats is a command-line interface *builder*. That means, CliHats is not about handing out an object containing command-line arguments but about creating the command-line interface program itself.

### Is CliHats the right tool for me?
If you want to offer your users a toolset of methods behind one single access point, CliHats will come in handy. This is especially true if the methods possess relatively simple signatures and a handful of parameters.

If you only want to invoke one method with one complex parameter type, you might be better off using a parameter parser like JCommander.

To make a long story short:
- CliHats provides a functional view on building a command-line interface - because a cli is about providing functionality.
- CliHats is a cli-builder and you *declare* what to build.
- CliHats is slim, uses plain java and is basically reflection-free.
- CliHats lets you adopt a command-line interface in no time, potentially not even requiring you to touch your current code's logic.

## Get CliHats
CliHats is available on [Maven Central](https://mvnrepository.com/artifact/io.github.johannesbuchholz/clihats).

## Documentation
The [documentation](https://johannesbuchholz.github.io/clihats/doc.html) contains a quickstart example and details on how to use and configure CliHats to your needs.
