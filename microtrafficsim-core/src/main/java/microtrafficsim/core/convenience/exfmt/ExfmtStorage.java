package microtrafficsim.core.convenience.exfmt;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.RouteContainerExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.SimulationConfigExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.exfmt.injector.simulation.ProjectedAreasInjector;
import microtrafficsim.core.exfmt.injector.simulation.RouteContainerInjector;
import microtrafficsim.core.exfmt.injector.simulation.SimulationConfigInjector;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.map.*;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Should simplify loading/saving simulation files
 *
 * @author Dominic Parga Cacheiro
 */
public class ExfmtStorage {
    private static final Logger logger = new EasyMarkableLogger(ExfmtStorage.class);


    private OSMParser parser;
    private ExchangeFormat exfmt;
    private ExchangeFormatSerializer serializer;

    private boolean mapLoadingHasBeenSet;


    public ExfmtStorage() {
        serializer = ExchangeFormatSerializer.create();
        exfmt = ExchangeFormat.getDefault();

        mapLoadingHasBeenSet = false;
    }


    public ExfmtStorage(SimulationConfig config, TilingScheme tilingScheme, int tileGridLevel) {
        this();
        setupMapLoading(config, tilingScheme, tileGridLevel);
    }

    /**
     * @param config This reference has to be stable for parser
     * @param viewer
     */
    public ExfmtStorage(SimulationConfig config, TileBasedMapViewer viewer) {
        this();
        setupMapLoading(config, viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel());
    }


    public void setupMapLoading(SimulationConfig config, TileBasedMapViewer viewer) {
        setupMapLoading(config, viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel());
    }

    public void setupMapLoading(SimulationConfig config, TilingScheme tilingScheme, int tileGridLevel) {
        parser = DefaultParserConfig.get(config).build();

        exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(tilingScheme, tileGridLevel));
        exfmt.getConfig().set(new StreetGraphExtractor.Config(config));

        mapLoadingHasBeenSet = true;
    }


    /*
    |=====|
    | map |
    |=====|
    */
    public Tuple<Graph, MapProvider> loadMap(File file) throws IOException, InterruptedException {
        return loadMap(file, true);
    }

    /**
     * Loads the given file depending on its map type (OSM or MTSM)
     *
     * @param priorityToTheRight Needed for visualization purpose; doesn't matter if no osm file
     */
    public Tuple<Graph, MapProvider> loadMap(File file, boolean priorityToTheRight) throws InterruptedException, IOException {
        if (!mapLoadingHasBeenSet)
            throw new IOException("You have to setup some map loading attributes, e.g. the parser.");

        try {
            if (MTSFileChooser.Filters.MAP_OSM_XML.accept(file)) {
                OSMParser.Result result = parser.parse(file, new MapProperties(priorityToTheRight));
                return new Tuple<>(result.streetgraph, result.segment);
            } else if (MTSFileChooser.Filters.MAP_EXFMT.accept(file)) {
                ExchangeFormat.Manipulator manipulator = exfmt.manipulator(serializer.read(file));

                MapProvider provider;
                try {
                    provider = manipulator.extract(QuadTreeTiledMapSegment.class);
                } catch (NotAvailableException e) { // thrown when no TileGrid available
                    provider = manipulator.extract(MapSegment.class);
                }

                return new Tuple<>(manipulator.extract(StreetGraph.class), provider);
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean saveMap(File file, Tuple<Graph, MapProvider> tuple) throws IOException {
        return saveMap(file, tuple.obj0, tuple.obj1);
    }

    public boolean saveMap(File file, Graph graph, MapProvider provider) throws IOException {
        if (!mapLoadingHasBeenSet)
            throw new IOException("You have to setup some map loading attributes, e.g. the parser.");

        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(provider)
                    .inject(graph)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    |========|
    | config |
    |========|
    */
    /**
     * @param file
     * @param draft see {@link SimulationConfigExtractor.Config#setConfig(SimulationConfig) Exfmt.Config.setConfig(...)}
     * @return new config reference with attributes set to the values in the given file
     */
    public SimulationConfig loadConfig(File file, SimulationConfig draft) {
        /* prepare exfmt config */
        SimulationConfigExtractor.Config cfg = new SimulationConfigExtractor.Config();
        cfg.setConfig(draft);
        exfmt.getConfig().set(cfg);


        /* prepare extractor */
        ExchangeFormat.Manipulator manipulator = null;
        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* load config */
        SimulationConfig newConfig = null;
        if (manipulator != null) {
            try {
                newConfig = manipulator.extract(SimulationConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newConfig;
    }

    /**
     * @param file
     * @param config see
     * {@link SimulationConfigInjector#inject(ExchangeFormat, ExchangeFormat.Context, Container, SimulationConfig)}
     *               for more detailed information
     * @return
     */
    public boolean saveConfig(File file, SimulationConfig config) {
        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(config)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    |========|
    | routes |
    |========|
    */
    public Triple<GraphGUID, RouteContainer, UnprojectedAreas> loadRoutes(File file, Graph graph) {
        RouteContainerExtractor.Config cfg = new RouteContainerExtractor.Config();
        cfg.setGraph(graph);
        exfmt.getConfig().set(cfg);


        /* prepare extractor */
        ExchangeFormat.Manipulator manipulator = null;
        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* load routes */
        RouteContainer routes = null;
        UnprojectedAreas areas = null;
        if (manipulator != null) {
            try {
                routes = manipulator.extract(RouteContainer.class);
                areas = manipulator.extract(UnprojectedAreas.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Triple<>(cfg.getLoadedGraphGUID(), routes, areas);
        }

        return null;
    }

    public boolean saveRoutes(File file, GraphGUID graphGUID, RouteContainer routes, UnprojectedAreas areas) {
        RouteContainerInjector.Config cfg = new RouteContainerInjector.Config();
        cfg.graphGUID = graphGUID;
        exfmt.getConfig().set(cfg);


        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(routes)
                    .inject(areas)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    |=======|
    | areas |
    |=======|
    */
    public UnprojectedAreas loadAreas(File file) {
        /* prepare extractor */
        ExchangeFormat.Manipulator manipulator = null;
        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* load areas */
        UnprojectedAreas areas = null;
        if (manipulator != null) {
            try {
                areas = manipulator.extract(UnprojectedAreas.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return areas;
    }

    public boolean saveAreas(File file, ProjectedAreas areas, Projection projection) {
        ProjectedAreasInjector.Config cfg = new ProjectedAreasInjector.Config();
        cfg.projection = projection;
        exfmt.getConfig().set(cfg);

        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(areas)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveAreas(File file, UnprojectedAreas areas) {
        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(areas)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
