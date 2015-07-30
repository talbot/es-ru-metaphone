package im.tretyakov.elasticsearch.rumetaphone;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.phonetic.PhoneticFilter;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;
import im.tretyakov.elasticsearch.rumetaphone.phonetic.RuMetaphoneEncoder;

/**
 * Factory which creates russian metaphone phonetic token filter
 *
 * @author Dmitry Tretyakov <dmitry@tretyakov.im>
 */
public class PhoneticTokenFilterFactory extends AbstractTokenFilterFactory {

    private final boolean replace;

    @Inject
    public PhoneticTokenFilterFactory(
        final Index index,
        @IndexSettings final Settings indexSettings,
        @Assisted final String name,
        @Assisted final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        this.replace = settings.getAsBoolean("replace", true);
    }

    @Override
    public TokenStream create(final TokenStream tokenStream) {
        return new PhoneticFilter(tokenStream, new RuMetaphoneEncoder(), !this.replace);
    }
}
