package ner.AnalysisEngine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.tutorial.RoomNumber;
import org.apache.uima.util.Level;

import ner.TypeSystem.BaseAnnotation;

/**
 * The GeneRegexAnnotator class is a simple, regular expression based gene mention annotator. It
 * will set all annotations' confidence to 1.0f as regular expressions are hand coded with high
 * confidence. It can be configured with the following parameters:
 * 
 * <ul>
 * <li><code>GeneRegularExpressions</code> - regular expression strings to match annotations.</li>
 * </ul>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class GeneRegexAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Name of configuration parameter which set the regular expression.
   */
  public static final String PARAM_GENEREGULAREXPRESSIONS = "GeneRegularExpressions";

  /**
   * Compiled regular expressions
   */
  private Pattern[] mPatterns;

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {

    // get document text
    String docText = aJCas.getDocumentText();

    if (mPatterns == null)
      return;

    // loop over patterns
    for (int i = 0; i < mPatterns.length; i++) {
      Matcher matcher = mPatterns[i].matcher(docText);
      while (matcher.find()) {
        // found one - create annotation
        BaseAnnotation annotation = new BaseAnnotation(aJCas);
        annotation.setBegin(matcher.start());
        annotation.setEnd(matcher.end());
        annotation.setSource(getClass().getName());
        annotation.setConfidence(1.0f);
        annotation.addToIndexes();
      }
    }
  }

  /**
   * @see AnalysisComponent#initialize(UimaContext)
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // Get regular expressions strings
    String[] patternStrings = (String[]) aContext
            .getConfigParameterValue(PARAM_GENEREGULAREXPRESSIONS);

    // compile regular expressions
    if (patternStrings != null) {
      mPatterns = new Pattern[patternStrings.length];
      for (int i = 0; i < patternStrings.length; i++) {
        mPatterns[i] = Pattern.compile(patternStrings[i]);
      }
    } else
      mPatterns = null;
  }
}