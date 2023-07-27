# Welcome to CliHats

![Maven Central](https://img.shields.io/maven-central/v/io.github.johannesbuchholz/clihats)

<!-- tag::readme-intro[] -->
CliHats is a java library that lets you easily expose functionality of your java code to the command line. Building a command-line interface becomes as simple as putting on a hat.
<!--  end::readme-intro[] -->

CliHats offers
<!-- tag::readme-offers[] -->
- Declarative and functional API
- Automatic selection of commands based on user inputs
- Option parsing
- Automatically generated help pages and error messages
<!-- end::readme-offers[] -->

## Quickstart
Create an entry point mapping to your command line interface.
```java
/**
 * The Quickstart example of CliHats.
 */
@CommandLineInterface
public class CliHatsQuickstart {

    public static void main(String[] args) {
        CliHats.get(CliHatsQuickstart.class).execute(args);
    }

    /**
     * Prints "Hello {name}" multiple times.
     * @param name The name to greet.
     * @param times The number of greetings.
     */
    @Command
    public static void sayHello(@Option String name, @Option(position = 0, necessity = OptionNecessity.REQUIRED) Integer times) {
        for (int i = 0; i < times; i++)
            System.out.println("Hello, " + name + "!");
    }

}
```

Compile and run your command by running the above entrypoint with `say-hello --name John 4`
```
Hello, John!
Hello, John!
Hello, John!
Hello, John!
```

Alternatively, get the command help page by running `say-hello --help`
```
Help for say-hello                                                              

Synopsis:
say-hello [-n <value>] [OPERAND0]

Prints "Hello {name}" multiple times.                                           

Parameters:                                                                     
-n       --name The name to greet.      
OPERAND0        The number of greetings.
```

## Why CliHats?

In contrary to other parameter parsers like [JCommander](https://github.com/cbeust/jcommander), CliHats directly links java methods to the command-line.

This is advantageous when orchestrating multiple functionalities of your code neatly behind one command line interface while the api is deduced from your java methods.
Let CliHats care for argument parsing, choosing the right command and generating help pages while you design your command access points: API first becomes a breeze.

### Is CliHats the right tool for me?
If you want to offer your users a toolset of methods behind one single java API, CliHats will come in handy. This is especially true, if the methods posses relatively simple signatures with few parameters and are easily deducible from String values.

To make a long story short:
- CliHats provides a functional view on building a command-line interface.
- CliHats is a cli-builder and you declare the api while CliHats links input arguments to suiting commands.
- CliHats is slim, uses plain java and is basically reflection-free.
- CliHats lets you adopt a command-line interface in no time, potentially not even requiring you to touch your current code's logic.

## Get CliHats
CliHats is available on [Maven Central](https://mvnrepository.com/artifact/io.github.johannesbuchholz/clihats).

## Documentation
The [documentation](https://johannesbuchholz.github.io/clihats/doc.html) contains a quickstart example and details on how to use and configure CliHats to your needs. 