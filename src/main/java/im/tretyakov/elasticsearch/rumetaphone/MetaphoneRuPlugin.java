package im.tretyakov.elasticsearch.rumetaphone;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

/**
 * Elasticsearch plugin which enables russian metaphone filter
 *
 * @author Dmitry Tretyakov <dmitry@tretyakov.im>
 */
public class MetaphoneRuPlugin extends AbstractPlugin {

    public static final String PLUGIN_NAME = "elasticsearch-analysis-metaphone-ru";

    /**
     * The name of the plugin.
     */
    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    /**
     * The description of the plugin.
     */
    @Override
    public String description() {
        return "Russian metaphone plugin for Elasticsearch";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new PhoneticAnalysisProcessor());
    }
}
