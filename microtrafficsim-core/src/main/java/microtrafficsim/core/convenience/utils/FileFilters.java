package microtrafficsim.core.convenience.utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;


/**
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public class FileFilters {

    public static final String MAP_OSM_XML_POSTFIX = "osm";
    public static final String MAP_EXFMT_POSTFIX   = "mtsm";
    public static final String SCENARIO_POSTFIX    = "mtss";
    public static final String CONFIG_POSTFIX      = "mtscfg";


    private FileFilters() {}


    public static final FileFilter MAP_OSM_XML = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case MAP_OSM_XML_POSTFIX: return true;
                default:                  return false;
            }
        }

        @Override
        public String getDescription() {
            return "OpenStreetMap XML Files (*." + MAP_OSM_XML_POSTFIX + ")";
        }
    };

    public static final FileFilter MAP_EXFMT = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case MAP_EXFMT_POSTFIX: return true;
                default:     return false;
            }
        }

        @Override
        public String getDescription() {
            return "MTS ExchangeFormat Map Files (*." + MAP_EXFMT_POSTFIX + ")";
        }
    };

    public static final FileFilter MAP_ALL = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case MAP_EXFMT_POSTFIX:    return true;
                case MAP_OSM_XML_POSTFIX:  return true;
                default:                   return false;
            }
        }

        @Override
        public String getDescription() {
            return "All Map Files (*." + MAP_EXFMT_POSTFIX + " *." + MAP_OSM_XML_POSTFIX + ")";
        }
    };


    public static final FileFilter SCENARIO = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case SCENARIO_POSTFIX:  return true;
                default:                return false;
            }
        }

        @Override
        public String getDescription() {
            return "Scenario File (*" + SCENARIO_POSTFIX + ")";
        }
    };

    public static final FileFilter SCENARIO_ALL = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case SCENARIO_POSTFIX:  return true;
                default:                return false;
            }
        }

        @Override
        public String getDescription() {
            return "All Scenario Files (*." + SCENARIO_POSTFIX + ")";
        }
    };


    public static final FileFilter CONFIG = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) return true;

            switch (extension(file)) {
                case CONFIG_POSTFIX:  return true;
                default:              return false;
            }
        }

        @Override
        public String getDescription() {
            return "MTS config file (*" + CONFIG_POSTFIX + ")";
        }
    };


    private static String extension(File file) {
        String filename = file.getName();
        int i = filename.lastIndexOf('.');

        if (i > 0 && i < filename.length() - 1)
            return filename.substring(i + 1).toLowerCase();
        else
            return "";
    }
}