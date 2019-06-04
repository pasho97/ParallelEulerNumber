package fmi.spo.project;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final String CURRENT_RUN_TOTAL_TIME_MSG_TEMPLATE = "Total execution time for current run (millis): %d";

    private static final String OUTPUT_FILE_OPTION = "o";
    private static final String OUTPUT_FILE_OPTION_DESCRIPTION = "Output file name";
    private static final String NUMBER_OF_TASKS_TO_RUN = "Number of tasks to run";
    private static final String PRECISION_OPTION_DESCRIPTION = "Precision value";
    private static final String HELP_DESCRIPTION = "HELP";
    private static final String HELP_OPTION = "h";
    private static final String HELP_OPTION_FULL = "help";
    private static final String TASKS_OPTION = "t";
    private static final String TASKS_OPTION_FULL = "tasks";
    private static final String PRECISION_OPTION = "p";
    private static final String QUIET_MODE = "Quiet mode";
    private static final String QUIET_OPTION = "q";
    //Defaults
    private static int numberOfThreads = 1;
    private static int precision = 10000;
    private static String fileOutput = "result.txt";

    public static void main(String[] args) throws ExecutionException, InterruptedException, ParseException, IOException {
        long startMillis = System.currentTimeMillis();
        CommandLineParser commandLineParser = new DefaultParser();
        Options options = buildOptions();

        CommandLine commandLine = commandLineParser.parse(options, args);
        if (commandLine.hasOption(HELP_OPTION)) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("parallel-euler-number", options);
            return;
        }
        if (commandLine.hasOption(TASKS_OPTION)) {
            numberOfThreads = Integer.valueOf(commandLine.getOptionValue(TASKS_OPTION));
        }
        if (commandLine.hasOption(OUTPUT_FILE_OPTION)) {
            fileOutput = commandLine.getOptionValue(OUTPUT_FILE_OPTION);
        }
        if (commandLine.hasOption(QUIET_OPTION)) {
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            }));
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
        long endMillis = System.currentTimeMillis();
        executor.shutdown();
        System.out.println(String.format(CURRENT_RUN_TOTAL_TIME_MSG_TEMPLATE, endMillis - startMillis));
        Files.write(Paths.get(fileOutput), result.toPlainString().getBytes());
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(Option.builder(TASKS_OPTION)
                .longOpt(TASKS_OPTION_FULL)
                .hasArg(true)
                .desc(NUMBER_OF_TASKS_TO_RUN)
                .build());
        options.addOption(PRECISION_OPTION, true, PRECISION_OPTION_DESCRIPTION);
        options.addOption(HELP_OPTION, HELP_OPTION_FULL, false, HELP_DESCRIPTION);
        options.addOption(QUIET_OPTION, false, QUIET_MODE);
        options.addOption(OUTPUT_FILE_OPTION, true, OUTPUT_FILE_OPTION_DESCRIPTION);
        return options;
    }
}
