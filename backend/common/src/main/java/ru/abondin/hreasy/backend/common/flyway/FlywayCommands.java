package ru.abondin.hreasy.backend.common.flyway;

import org.flywaydb.core.Flyway;

import java.util.List;
import java.util.function.Consumer;

/**
 * Executes configured Flyway maintenance commands shared by backend services.
 */
public final class FlywayCommands {

    private FlywayCommands() {
    }

    /**
     * Runs Flyway commands in the configured order.
     *
     * @param flyway configured Flyway instance
     * @param commands commands to execute, for example {@code migrate} or {@code repair}
     * @param emptyCommandsHandler callback invoked when command list is empty
     */
    public static void run(Flyway flyway, List<String> commands, Consumer<String> emptyCommandsHandler) {
        if (commands.isEmpty()) {
            emptyCommandsHandler.accept("No Flyway migration commands passed. Allowed values: baseline, migrate, clean, repair, undo");
        }
        for (var command : commands) {
            switch (command) {
                case "baseline":
                    flyway.baseline();
                    break;
                case "migrate":
                    flyway.migrate();
                    break;
                case "clean":
                    flyway.clean();
                    break;
                case "repair":
                    flyway.repair();
                    break;
                case "undo":
                    flyway.undo();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported flyway command " + command);
            }
        }
    }
}
