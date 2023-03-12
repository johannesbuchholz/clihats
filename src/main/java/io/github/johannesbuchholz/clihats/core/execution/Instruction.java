package io.github.johannesbuchholz.clihats.core.execution;

/**
 * Implementing classes are able to receive a list of arbitrary input arguments and perform some action based on these
 * inputs.
 * <p>
 *     This interface serves as a bridge between client code and clihats: clihats handles command selection,
 *     option parsing and instruction invocation whereas client code logic is run via {@link #execute(Object[])}.
 * </p>
 * An example implementation could look like this:
 * <pre>
 *      public class ContextService {
 *           // business logic
 *           public static void loadContext(String contextName) {
 *               contextLoader.load(contextName);
 *           }
 *
 *           // instruction usable for clihats
 *           public Instruction loadContextInstruction = args{@literal ->} {
 *              if (args.length != 1)
 *                  throw new InstructionExecutionException("Illegal number of arguments");
 *              loadContext((String) args[0]);
 *           };
 *       }
 * </pre>
 * <p>
 *     Implementing classes are allowed to throw {@link Exception}.
 * </p>
 * @see Command
 */
@FunctionalInterface
public interface Instruction {

    /**
     * @param args the list of arguments.
     * @throws Exception if the execution fails.
     */
    void execute(Object[] args) throws Exception;

    /**
     * Creates the instruction that does nothing.
     * @return the empty Instruction.
     */
    static Instruction empty() {
        return args -> {};
    }

}
