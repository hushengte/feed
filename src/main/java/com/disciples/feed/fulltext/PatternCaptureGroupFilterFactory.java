package com.disciples.feed.fulltext;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 * Factory for {@link PatternCaptureGroupTokenFilter}.
 * 
 * <pre class="prettyprint">
 * &lt;fieldType name="text_ptncapturegroup" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.KeywordTokenizerFactory"/&gt;
 *     &lt;filter class="solr.PatternCaptureGroupFilterFactory" pattern="([^a-z])" preserve_original="true"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;
 * </pre>
 *
 * @see com.disciples.feed.fulltext.PatternCaptureGroupTokenFilter
 */
public class PatternCaptureGroupFilterFactory extends TokenFilterFactory {
	
	private Pattern pattern;
	private boolean preserveOriginal = true;

	public PatternCaptureGroupFilterFactory(Map<String, String> args) {
		super(args);
		pattern = getPattern(args, "pattern");
		preserveOriginal = args.containsKey("preserve_original") ? Boolean.parseBoolean(args.get("preserve_original")) : true;
	}

	@Override
	public PatternCaptureGroupTokenFilter create(TokenStream input) {
		return new PatternCaptureGroupTokenFilter(input, preserveOriginal, pattern);
	}
	
}
