package com.disciples.feed;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

import com.disciples.feed.fulltext.PatternCaptureGroupTokenFilter;

public class AuthorAnalyzer extends StopwordAnalyzerBase {

	private static final CharArraySet CUSTOM_STOP_WORDS_SET = new CharArraySet(Arrays.asList("names", "level"), false);
	private static final Pattern CHINESE_NAME_SUFFIX_RULE = Pattern.compile("(^.{2,})(等$)");
	  
	public AuthorAnalyzer() {
		this(CUSTOM_STOP_WORDS_SET);
	}
	
	public AuthorAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	public AuthorAnalyzer(File stopwordsFile) throws IOException {
	    this(loadStopwordSet(stopwordsFile));
	}

	public AuthorAnalyzer(Reader stopwords) throws IOException {
	    this(loadStopwordSet(stopwords));
	}

	/**
	 * Creates {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} used to tokenize all the text in the provided {@link Reader}.
	 * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} built from a {@link LowerCaseTokenizer} filtered with {@link StopFilter}
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer source = new LowerCaseTokenizer(reader);
		StopFilter stopFilter = new StopFilter(source, CUSTOM_STOP_WORDS_SET);
		return new TokenStreamComponents(source, new PatternCaptureGroupTokenFilter(stopFilter, false, CHINESE_NAME_SUFFIX_RULE));
	}

}
