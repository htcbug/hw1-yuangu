package ner.AnalysisEngine;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import ner.TypeSystem.BaseAnnotation;
import ner.util.PosTagNamedEntityRecognizer;

/**
 * Annotator that wraps StanfordNLP NE Recognizer.
 */

/**
 * The GeneStanfordNLPAnnotator class wraps the StanfordNLP NE Recognizer provided in the
 * assignment.
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GeneStanfordNLPAnnotator extends JCasAnnotator_ImplBase {

  private PosTagNamedEntityRecognizer mNERecognizer;

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {

    // get document text
    String docText = aJCas.getDocumentText();

    // process via StanfordNLP NE recognizer
    Map<Integer, Integer> geneSpans = mNERecognizer.getGeneSpans(docText);

    Iterator it = geneSpans.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Integer, Integer> pairs = (Entry<Integer, Integer>) it.next();

      // output the found spans
      BaseAnnotation annotation = new BaseAnnotation(aJCas);
      annotation.setBegin(pairs.getKey());
      annotation.setEnd(pairs.getValue());
      annotation.setSource(getClass().getName());
      annotation.setConfidence(1.0f);
      annotation.addToIndexes();
    }
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // initialize NE recognizer
    mNERecognizer = new PosTagNamedEntityRecognizer();

  }
}
