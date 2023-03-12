# CliHats

CliHats is a java library to easily extend your java-program with a command-line interface in a functional, declarative style.

CliHats offers
- Gathering multiple commands under one command-line interface
- Selecting commands based on user inputs
- Option parsing
- Exception handling and displaying helpful error messages
- Generating and displaying help pages

# Quickstart example

In this example we will use CliHats to make multiple existing methods available through a command-line interface.

Assume the below "Hello, World!" example.

```java
/**
 * A simple "Hello, World!" program to demonstrate the capabilities of CliHats.
 */
public class HelloWorldCliHats {

    /**
     * Simply prints "Hello, World!"
     */
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name the name to greet.
     * @param polite if true, additionally prints "Nice to meet you!".
     */
    public static void sayHelloToPerson(String name, Boolean polite) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
```

We declare our class as a command-line interface by annotating it with `@CommandLineInterface`.

```
@CommandLineInterface
public class HelloWorldCliHats {
    // ...
}
```

Next, we add the first command to our command-line interface by annotating the method `sayHello` with `@Command`.

```
@Command
public static void sayHello() {
    System.out.println("Hello, World!");
}
```

By default, CliHats will automatically group commands to the first command-line interface it finds in the upwards package tree. In our case this is `HelloWorldCliHats`.

We proceed in a similar fashion with the second method `sayHelloToPerson`. This time we need CliHats to also set method parameters. We accomplish this by adding `@Option` to each parameter.

```
@Command
public static void sayHelloToPerson(
        @Option(necessity = OptionNecessity.REQUIRED) String name,
        @Option(flagValue = "true", defaultValue = "false") Boolean polite
) {
    System.out.printf("Hello, %s!\n", name);
    if (polite)
        System.out.println("Nice to meet you!");
}
```

Finally, it is left to add a java entrypoint to our program. In order to let CliHats choose commands and parse options, we need to pass the received String arguments to the command-line interface we want to invoke.
For this, we load our previously declared command-line interface with `CliHats.get(HelloWorldCliHats.class)` and run it by calling `execute` supplying the specified String arguments.

```
public static void main(String[] args) {
    CliHats.get(HelloWorldCliHats.class).execute(args);
}
```

The complete class definition now looks like this:

```java
/**
 * A simple "Hello, World!" program to demonstrate the capabilities of CliHats.
 */
@CommandLineInterface
public class HelloWorldCliHats {

    public static void main(String[] args) {
        CliHats.get(HelloWorldCliHats.class).execute(args);
    }

    /**
     * Simply prints "Hello, World!"
     */
    @Command
    public static void sayHello() {
        System.out.println("Hello, World!");
    }

    /**
     * Prints a greeting to the specified name.
     * @param name the name to greet.
     * @param polite if true, additionally prints "Nice to meet you!".
     */
    @Command
    public static void sayHelloToPerson(
            @Option(necessity = OptionNecessity.REQUIRED) String name,
            @Option(flagValue = "true", defaultValue = "false") Boolean polite
    ) {
        System.out.printf("Hello, %s!\n", name);
        if (polite)
            System.out.println("Nice to meet you!");
    }

}
```

Executing the above program without arguments results in a message similar to the following:

```
Help for hello-world-clihats
A simple "Hello, World!" program to demonstrate the capabilities of CliHats.                        

Commands:
say-hello           Simply prints "Hello, World!".          
say-hello-to-person Prints a greeting to the specified name.
```

If we run our program with arguments `{"say-hello-to-person", "--help"}` we receive a help message like this:

```
Help for say-hello-to-person                                                    
Prints a greeting to the specified name.                                        

Options:                                                                        
-n --name   (required) the name to greet.                               
-p --polite (flag)     if true, additionally prints "Nice to meet you!".
```

Running `{"say-hello-to-person", "-n", "Bob", "--polite"}` yields

```
Hello, Bob!
Nice to meet you!
```

But if we do not pass option `-n` and run `{"say-hello-to-person", "--polite"}` we receive

```
Error running hello-world-clihats: Exception during invocation of command say-hello-to-person: Invalid input arguments:
> Missing required arguments:
    -n
```

The above program is a tiny example mostly using default behaviour. There are options to configure parameter names and aliases, value mapping, parsing behaviour and exception handling.

# Core concepts
The general concept follows a rather functional style in which a command-line interface is represented by a set of methods. Each method models one task like printing the program version, loading data from a file or sending a web-request. A task may require additional input like a filename, a web-address or credentials. These are provided by the user via options.

A CliHats command-line interface is represented by one `Commander`. One commander possesses a collection of `Command` objects. Each command consists of one `Instruction` representing a task and a list of `AbstractOptionParser` objects. Option parsers are responsible for extracting values from the command-line arguments and make them available to the command being executed.

Although CliHats offers these structures as public classes which may be constructed "by hand", CliHats also comes with an annotation processor, automatically creating commanders, commands and option parsers based on annotations. One may fall back to manually defining a commander when more control over the configuration is required.

As an example, the generated implementation of the "Quickstart" example "Hello, World!" commander looks like this

```
Commander.forName("hello-world-clihats")
        .withDescription("A simple \"Hello, World!\" program to demonstrate the capabilities of CliHats.")
        .withCommands(
                Command.forName("say-hello")
                        .withInstruction(args -> HelloWorldCliHats.sayHello())
                        .withDescription("Simply prints \"Hello, World!\""),
                Command.forName("say-hello-to-person")
                        .withInstruction(args -> HelloWorldCliHats.sayHelloToPerson((String) args[0], (Boolean) args[1]))
                        .withDescription("Prints a greeting to the specified name.")
                        .withParsers(
                                OptionParsers.valued("-n").withAliases("--name").isRequired(true).withDescription("the name to greet."),
                                OptionParsers.flag("-p").withAliases("--polite").withFlagValue("true").withDefault("false").withMapper(new BooleanMapper()).withDescription("if true, additionally prints \"Nice to meet you!\".")));

```

## Defining command-line interfaces
Any class annotated with `@CommandLineInterface` defines a command-line interface serving as an anchor for attaching commands. Per default, the name is set to the hyphenated class name.

## Defining commands
Any `public static void` method annotated with `@Command` defines a command. Each command needs to be attached to one command line interface in order to be executable by CliHats.
CliHats automatically attaches commands to the first command-line interface within the upwards package tree of the command containing class. Alternatively, explicit attaching is possible via parameter `cli`.
A command method must not define any primitive parameter.  
Per default, commands are named by their hyphenated method name. 

## Defining options
Any method parameter annotated with `@Option` defines an option. Options only take effect on methods annotated with `@Command`. It is not required to declare every method parameter as an option. Parameters not declared to be an option are set to `null` whenever the respective command is invoked by CliHats.
If a command is invoked with a list of String input arguments, all registered option parsers scan the provided input arguments during possibly multiple parsing rounds. Then, the command instruction is executed with parameters set according to the parsed option values.

### Option types
Currently, there are three different option types available: valued options, flag options and positional options.
- Valued option: Provides a specific user defined value like `--name "John"` or `--numbers=1,2,3,4`.
- Flag option: Acts like a switch providing up to two predefined values depending on whether the flag is present or not. Think of something like `--dry-run` or `--verbose`.
- Positional option: Provides a specific user defined value without requiring a prefix. Takes its value from the remaining input arguments at a specific index. In this sense, the command `mkdir ./tmp` takes `./tmp` as a positional argument.

The parser type is automatically deduced from the option configuration. A positional options is created if the parameter `position` is set to a non-negative integer. A flag option is assumed if the parameter `flagValue` is set to a non-empty string. Otherwise, a valued option is created.

### Option names and aliases
Valued and flag options may be named individually via parameter `name`. This parameter takes an array of String values, where the first entry denotes the name. All following names are used as aliases.
If `name` is not set, the option name is automatically set to `-` plus the first character of the parameter name. In that case, also the alias name is automatically set to the hyphened parameter name prefixed with `--`.
Positional options possess an internal name only.

### Option necessity
Each option possesses a necessity determining CliHats behaviour in case the respective option is not among the provided input arguments. By default, each option is optional resulting in the parameter value `null` if the option is missing. Alternatively one can configure CliHats to stop with an error or request user input over the command line. 

If manual user input from a console is desired, CliHats will try to obtain a `java.io.Console` object by calling `System.console()` which may fail if there is no console available to the running jvm.

### Value mapping
By default, option parsers provide their values as `java.lang.String`. If a command method requires other parameter types, a `Mapper` can be specified via the `mapper` parameter. There is a collection of default mappers for commonly used types. CliHats is able to recognizes these standard types and automatically configures a suitable mapper. Currently implemented standard mappers are available for types `Path`, `Boolean`, `Integer`, `Double`, `Float`, `LocalDate`, `LocalDateTime`, `BigDecimal`.
The usage a standard mapper is overridden by explicitly setting a mapper.

In case of a missing non-required option, value mapping is not applied and the respective parameter is directly set to `null`.

### Default values
Any option may declare a default value which should be used whenever the respective option is missing. Such a default value is treated as if the user had specified it. In particular, this means that value mapping applies. This differs from the behaviour in case of not declaring a default value, resulting in the value `null` and not applying value mapping.

Default values only apply for non-required options.

## Configuring help
In general there are two ways of defining the content of these descriptions: by javadoc comments or by the provided annotation parameter `description`. In general, text provided by the description parameter overrides the description from javadoc comments.
The javadoc of a command-annotated method may also contain `@param` tags to conveniently provide descriptions for each option.

Help may be displayed for a commander, hence for the whole command-line interface, or for a single command. For now, CliHats triggers help printing if the last option passed to CliHats is equal to `--help`. Alternatively, passing zero arguments to the command-line interface or a command with at least one option will also display help.

## Exception handling
Using the method `execute(String[] args)` on the `Cli` object returned from `CliHats.get(Class<?> commandLineInterface)` includes exception handling by CliHats. That is, the cli catches any occurring exceptions, prints an appropriate message to `System.err` and exiting the jvm with a non-zero exit code. If custom exception handling is desired, use `executeWithThrows(String[] args)` and handle thrown exceptions manually.

It is worth mentioning that help-calls also result in an exception. Unlike error-induced exceptions.In that case, CliHats exception handling prints help prints to `System.out` and exits the jvm normally with code `0`. Modelling help calls as an exception enables custom processing of help-calls.

## Use case examples
This section aims to provide common use case examples when defining options.

### Receive a username
Receiving a username from the command-line may be done by declaring an option as follows:
```
@Option(necessity = OptionNecessity.PROMPT) String name
```
One now may declare the name by providing `-n <name>` or `--name <name>`. If not providing the above option, CliHats will ask the user for manual input.

### Receive a password
Receiving a password from the command-line and not showing already typed characters, one may declare an option as follows:
```
@Option(necessity = OptionNecessity.MASKED_PROMPT) String password
```
One now may declare the password by providing `-p <name>` or `--password <name>`. If one does not provide the above option, CliHats will ask for manual input, hiding typed characters.

Note: Be aware that CliHats handles user inputs from command-line arguments and from console input as String objects. There are discussions about how to handle sensitive data like passwords regarding immutability of String objects and them remaining accessible in memory until garbage collection is performed. It is encouraged to use `char[]` for password handling in order to minimize the duration in which sensitive data remains in memory by emptying said array. For now, CliHats still uses String objects for transporting user input data.

### Receive a boolean flag
A simple switch-like option may be defined as follows
```
@Option(flagValue = "true", defaultValue = "false") Boolean flag
```
Dropping `defaultValue` results in passing `null` to the parameter whenever the flag is not present.

Note: CliHats will automatically use a value mapper for some commonly used types like `java.lang.Boolean`. The specified String values from this example are then transformed using said mapper.

### Load file contents
Obtaining some file content as `InputStream` might look like this:
First define a public mapper class extending `AbstractValueMapper`.
```java
public class InputStreamMapper extends AbstractValueMapper<InputStream> {

        @Override
        public InputStream map(String filePath) {
            Path p = Path.of(filePath);
            if (!Files.isRegularFile(p)) {
                throw new IllegalArgumentException("Path does not point to a regular file " + p);
            }
            try {
                return Files.newInputStream(p);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create input stream: " + e.getMessage(), e);
            }
        }
        
    }
```

Then, declare an option using the above mapper.
```
@Option(mapper = InputStreamMapper.class) InputStream file
```

Make sure to close the opened InputStream when processing is done.

### Receive a list of String
Collecting multiple values into a list argument is possible by defining a suiting mapper.

A mapper returning multiple `String` objects might be defined as follows.

```java
public static class ListMapper extends AbstractValueMapper<List<String>> {
        
        private static final String separator = ",";

        @Override
        public List<String> map(String str) {
            return Arrays.stream(str.split(separator))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

}
```

Then, declare an option using the above mapper.
```
@Option(mapper = ListMapper.class) List<String> list
```
