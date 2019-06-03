package fmi.spo.project;

import org.apache.commons.cli.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    private static final String NUMBER_OF_TASKS_TO_RUN = "Number of tasks to run";
    private static final String PRECISION_OPTION_DESCRIPTION = "Precision value";
    private static final String HELP_DESCRIPTION = "HELP";
    private static final String HELP_OPTION = "h";
    private static final String HELP_OPTION_FULL = "help";
    private static final String TASKS_OPTION = "t";
    private static final String TASKS_OPTION_FULL = "tasks";
    private static final String PRECISION_OPTION = "p";
    public static final String QUIET_MODE = "Quiet mode";
    public static final String QUIET_OPTION = "q";
    private static int numberOfThreads = 10;
    private static int precision = 1000;

    public static void main(String[] args) throws ExecutionException, InterruptedException, ParseException {
        CommandLineParser commandLineParser = new DefaultParser();
        Options options = buildOptions();

        CommandLine commandLine = commandLineParser.parse(options, args);
        if (commandLine.hasOption(HELP_OPTION)) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("parallel-euler-number", options);
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<BigDecimal>> futures = new ArrayList<>(numberOfThreads);
        int step = precision / numberOfThreads;
        BigDecimal result = BigDecimal.ZERO;
        for (int j = 0; j <= precision; j += step) {
            Future<BigDecimal> future = executor.submit(new EulerCallable(j, j + step, precision));
            futures.add(future);
        }
        for (Future<BigDecimal> future : futures) {
            result = result.add(future.get());
        }
        executor.shutdown();

    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(new Option(TASKS_OPTION, TASKS_OPTION_FULL, true, NUMBER_OF_TASKS_TO_RUN));
        options.addOption(PRECISION_OPTION, true, PRECISION_OPTION_DESCRIPTION);
        options.addOption(HELP_OPTION, HELP_OPTION_FULL, false, HELP_DESCRIPTION);
        options.addOption(QUIET_OPTION,false, QUIET_MODE);
        return options;
    }
}
