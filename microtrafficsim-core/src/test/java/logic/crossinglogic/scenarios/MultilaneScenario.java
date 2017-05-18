package logic.crossinglogic.scenarios;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.Car;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class MultilaneScenario extends QueueScenarioSmall {
    private Route slowRoute;


    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph)
     */
    public MultilaneScenario(SimulationConfig config,
                             Graph graph,
                             VisVehicleFactory visVehicleFactory) {
        super(config, graph);
        VehicleScenarioBuilder scenarioBuilder = new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle;
                    if (metaRoute == slowRoute)
                        vehicle = new Car(id, 1, scenario.getConfig().visualization.style);
                    else
                        vehicle = new Car(id, scenario.getConfig().visualization.style);
                    Driver driver = new BasicDriver(seed, 0, metaRoute.getSpawnDelay());
                    driver.setRoute(metaRoute.clone());
                    driver.setVehicle(vehicle);
                    vehicle.setDriver(driver);

                    vehicle.addStateListener(scenario.getVehicleContainer());
                    vehicle.addStateListener(v -> {
                        System.err.println("id = " + v.getId());
                        System.err.println("state = " + v.getState());
                        System.err.println("v = " + v.getVelocity());
                        System.err.println(v.getDirectedEdge().getLane(0).getLastVehicle().getId());
                        System.err.println(v.getDirectedEdge());
                    });
                    return vehicle;
                },
                visVehicleFactory
        );
        setScenarioBuilder(scenarioBuilder);



        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> {
            Coordinate c1 = n1.getCoordinate();
            Coordinate c2 = n2.getCoordinate();
            if (c1.lat == c2.lat)
                return c1.lon > c2.lon ? 1 : -1;
            else
                return c1.lat > c2.lat ? 1 : -1;
        });

        /*
        Sorted by (lat, lon)
        Node id = 6 at microtrafficsim.core.map.Coordinate { 35.625, 139.7425}
        Node id = 8 at microtrafficsim.core.map.Coordinate { 35.626, 139.7432}
        Node id = 4 at microtrafficsim.core.map.Coordinate { 35.627, 139.7415}
        Node id = 3 at microtrafficsim.core.map.Coordinate { 35.627, 139.7445}
        Node id = 0 at microtrafficsim.core.map.Coordinate { 35.628, 139.743}
        Node id = 2 at microtrafficsim.core.map.Coordinate { 35.629, 139.7415}
        Node id = 1 at microtrafficsim.core.map.Coordinate { 35.629, 139.7445}
        Node id = 7 at microtrafficsim.core.map.Coordinate { 35.63, 139.7433}
        Node id = 5 at microtrafficsim.core.map.Coordinate { 35.631, 139.7435}
        */

        Node bl = sortedNodes.get(0); // 3 <-> 3 lanes
        Node cb = sortedNodes.get(1); // 3 <-> 3 lanes
        Node xbl = sortedNodes.get(2); // 2 <-> 3 lanes
        Node xbr = sortedNodes.get(3); // 2 <-> 3 lanes
        Node x = sortedNodes.get(4); // 2 <-> 2 lanes
        Node xtl = sortedNodes.get(5); // 2 <-> 3 lanes
        Node xtr = sortedNodes.get(6); // 2 <-> 3 lanes
        Node ct = sortedNodes.get(7); // 3 <-> 3 lanes
        Node tr = sortedNodes.get(8); // 3 <-> 3 lanes

        RouteContainer routeContainer = new SortedRouteContainer();
        slowRoute = new MetaRoute(tr, bl);
        routeContainer.add(slowRoute);
        routeContainer.add(tr, bl, 4);
        addSubScenario(routeContainer);
    }


    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        config.maxVehicleCount                            = 100;
        config.speedup                                    = 5;
        config.seed                                       = 42;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;

        return config;
    }
}
