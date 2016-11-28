import org.apache.commons.cli.*;

import java.util.HashMap;

public class SchedulerCliConfigurationParser {

    public SchedulerProcessConfiguration parseConfigurationFromArgs(String[] args) {
        SchedulerProcessConfiguration config = new SchedulerProcessConfiguration();
        CommandLine cli = parseArgs(args);

        double weight = Double.parseDouble(cli.getOptionValue("weight"));
        int priority = Integer.parseInt(cli.getOptionValue("priority"));

        config.setDefaultProcessWeight(weight);
        config.setDefaultProcessPriority(priority);
        config.setFiles(parseFilesWithGroups(cli.getOptionValues("files")));

        return config;
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Integer>[] parseFilesWithGroups(String[] files) {
        HashMap<String, Integer>[] parsedFiles = new HashMap[files.length];

        int x = 0;
        for(String file : files) {
            String[] parts = file.split(":");

            if(parts.length != 2) {
                throw new IllegalArgumentException("Invalid file name \"" + file + "\" given. Expected  {filename}:{group #}.");
            }

            HashMap<String, Integer> map = new HashMap<>();
            map.put(parts[0], Integer.parseInt(parts[1]));
            parsedFiles[x] = map;
            x++;
        }

        return parsedFiles;
    }

    private CommandLine parseArgs(String[] args) {
        Options options = initializeParserOptions();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        return null;
    }

    private Options initializeParserOptions() {
        Options options = new Options();

        //Default process weight argument
        Option weightArg = new Option("w", "weight", true, "The default weight for all processes.");
        weightArg.setRequired(true);
        options.addOption(weightArg);

        //Default process priority argument
        Option priorityArg = new Option("p", "priority", true, "The default priority for all processes.");
        priorityArg.setRequired(true);
        options.addOption(priorityArg);

        //Processes to be executed.
        Option processesArg = new Option("f", "files", true, "A list of processes (pexe files) to be executed, followed by " +
                "the group id. (i.e filename.pexe:5 filename2:3 ...).");
        processesArg.setRequired(true);
        processesArg.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(processesArg);

        return options;
    }

}
