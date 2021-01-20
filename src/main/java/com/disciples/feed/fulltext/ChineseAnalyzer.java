package com.disciples.feed.fulltext;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cn.smart.HMMChineseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * This analyzer does not use PorterStemFilter to process english word,
 * which is a feature of SmartChineseAnalyzer.
 * 
 * @see org.apache.lucene.analysis.en.PorterStemFilter
 * @see org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
 */
public class ChineseAnalyzer extends Analyzer {

    private final CharArraySet stopWords;

    public ChineseAnalyzer() {
        this(null);
    }

    public ChineseAnalyzer(CharArraySet stopWords) {
        this.stopWords = stopWords == null ? CharArraySet.EMPTY_SET : stopWords;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new HMMChineseTokenizer();
        TokenStream result = tokenizer;
        if (!stopWords.isEmpty()) {
            result = new StopFilter(result, stopWords);
        }
        return new TokenStreamComponents(tokenizer, result);
    }

}
