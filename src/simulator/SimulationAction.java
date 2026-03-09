package simulator;

/**
 * Enum defines the actions that Simulator can perform
 */
public enum SimulationAction {
    CREATE_ORDER("Create new order"),
    UPDATE_INVENTORY("Update stock quantity"),
    ADD_MEDICINE("Add new product");

    private final String description;

    SimulationAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Select random action
     */
    public static SimulationAction random() {
        SimulationAction[] actions = SimulationAction.values();
        return actions[(int) (Math.random() * actions.length)];
    }
}
