package im.tretyakov.elasticsearch.rumetaphone;

import org.elasticsearch.index.analysis.AnalysisModule;

/**
 * Phonetic analysis processor for russian metaphone filter
 *
 * @author Dmitry Tretyakov <dmitry@tretyakov.im>
 */
public class PhoneticAnalysisProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenFilters(final TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("ru_phonetic", PhoneticTokenFilterFactory.class);
    }
}
