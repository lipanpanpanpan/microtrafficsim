package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class RandomRouteScenario extends BasicScenario {
    private static Logger logger = new EasyMarkableLogger(RandomRouteScenario.class);

    private Random random;
    // scout factory
    private final float fastestWayProbability = 1.0f;
    private final ShortestPathAlgorithm fastestWayBidirectionalAStar, linearDistanceBidirectionalAStar;

    /**
     * Default constructor calling {@link #RandomRouteScenario(SimulationConfig, StreetGraph, VehicleContainer)} using
     * {@link ConcurrentVehicleContainer} as {@code VehicleContainer}
     */
    public RandomRouteScenario(SimulationConfig config,
                                  StreetGraph graph,
                                  Supplier<VisualizationVehicleEntity> vehicleFactory) {
        this(config, graph, new ConcurrentVehicleContainer(vehicleFactory));
    }

    /**
     * Calls {@link #next()} to initialize the origin-destination-matrix.
     */
    public RandomRouteScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        random = new Random(config.seedGenerator.next()); // TODO reset

        // scout factory
        fastestWayBidirectionalAStar = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        linearDistanceBidirectionalAStar = new LinearDistanceBidirectionalAStar(config.metersPerCell);

        // init
        next();
    }

    /**
     * <p>
     * Referring to {@code Random.nextAnything()}, this method calculates the next origin-destination-matrix based on
     * the seed of this class.
     *
     * <p>
     * Until enough vehicles (defined in {@link SimulationConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull get random destination <br>
     * &bull increase the route count for the found origin-destination-pair
     */
    public void next() {
        logger.info("BUILDING ODMatrix started");

        ArrayList<Node> nodes = new ArrayList<>(getGraph().getNodes());
        odMatrix.clear();

        // TODO can the runtime be improved by mathematical magic?
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            int rdmOrig = random.nextInt(nodes.size());
            int rdmDest = random.nextInt(nodes.size());
            odMatrix.inc(nodes.get(rdmOrig), nodes.get(rdmDest));
        }

        logger.info("BUILDING ODMatrix finished");
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () ->
                (random.nextFloat() < fastestWayProbability)
                ? fastestWayBidirectionalAStar
                : linearDistanceBidirectionalAStar;
    }
}