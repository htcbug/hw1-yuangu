package ner.CasConsumer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.base_cpm.CasObjectProcessor;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import ner.TypeSystem.BaseAnnotation;
import ner.TypeSystem.SourceSentenceInformation;

/**
 * AnnotationEvaluator evaluates the CPE's output with true answers and calculate the recall and
 * precision. Parameters needed by the AnnotationEvaluator are:
 * <ul>
 * <li><code>trueAnswerFile</code> - where the true answer file locates.</li>
 * </ul>
 * <br>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class AnnotationEvaluator extends CasConsumer_ImplBase implements CasObjectProcessor {

  /**
   * Name of configuration parameter which set the path of the true answer file.
   */
  public static final String PARAM_TRUEANSWERFILE = "TrueAnswerFile";

  /**
   * The true answers will be stored here.
   */
  HashSet<String> mTrueAnswers;

  /**
   * The system output will be stored here.
   */
  HashSet<String> mSystemOutputs;

  /**
   * The count of TruePositive annotations.
   */
  int mTruePositiveCount;

  /**
   * The count of FalsePositive annotations.
   */
  int mFalsePositiveCount;

  /**
   * Initializes this CAS Consumer with the parameters specified in the descriptor.
   * 
   * @throws ResourceInitializationException
   *           if there is error in initializing the resources
   */
  public void initialize() throws ResourceInitializationException {

    // extract configuration parameter settings
    String oPath = (String) getUimaContext().getConfigParameterValue(PARAM_TRUEANSWERFILE);

    if (oPath == null) {
      throw new ResourceInitializationException(
              ResourceInitializationException.CONFIG_SETTING_ABSENT,
              new Object[] { "trueAnswerFile" });
    }

    File file = new File(oPath.trim());
    BufferedReader br;
    String line;

    mTrueAnswers = new HashSet<String>();
    mSystemOutputs = new HashSet<String>();
    mTruePositiveCount = 0;
    mFalsePositiveCount = 0;

    // load the true answers into memory
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

      while ((line = br.readLine()) != null) {
        mTrueAnswers.add(line);
      }

      br.close();

    } catch (FileNotFoundException e) {
      throw new ResourceInitializationException(
              ResourceConfigurationException.RESOURCE_DATA_NOT_VALID, new Object[] {
                  PARAM_TRUEANSWERFILE, this.getMetaData().getName(), file.getPath() });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Iterates over selected annotations and compares them with true answers.
   * 
   * @param aCAS
   *          CasContainer which has been populated by the TAEs
   * 
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * 
   * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(CAS)
   */
  public synchronized void processCas(CAS aCAS) throws ResourceProcessException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    String identifier = null;
    Iterator it = jcas.getAnnotationIndex(SourceSentenceInformation.type).iterator();
    if (it.hasNext()) {
      SourceSentenceInformation srcDocInfo = (SourceSentenceInformation) it.next();
      identifier = srcDocInfo.getIdentifier();
    }

    // iterate and evaluate annotations
    Iterator annotationIter = jcas.getAnnotationIndex(BaseAnnotation.type).iterator();
    // Iterator annotationIter = jcas.getAnnotationIndex().iterator();
    while (annotationIter.hasNext()) {
      BaseAnnotation annot = (BaseAnnotation) annotationIter.next();

      // get the number of spaces in the prefix string of this entity
      int prefixSpaceNum = 0;
      String prefixString = jcas.getDocumentText().substring(0, annot.getBegin());
      for (int i = 0; i < prefixString.length(); i++) {
        if (prefixString.charAt(i) == ' ')
          prefixSpaceNum++;
      }

      // get the number of spaces in the content string of this entity
      int contentSpaceNum = 0;
      String contentString = jcas.getDocumentText().substring(annot.getBegin(), annot.getEnd());
      for (int i = 0; i < contentString.length(); i++) {
        if (contentString.charAt(i) == ' ')
          contentSpaceNum++;
      }

      // calculate the non-space version beg and end position and form the output string
      String outputString = identifier + "|" + (annot.getBegin() - prefixSpaceNum) + " "
              + (annot.getEnd() - prefixSpaceNum - contentSpaceNum - 1) + "|"
              + annot.getCoveredText();

      // store the output string
      mSystemOutputs.add(outputString);

      // compare the result with true answer
      if (mTrueAnswers.contains(outputString))
        mTruePositiveCount++;
      else {
        mFalsePositiveCount++;
      }
    }
  }

  /**
   * Called when the entire collection is completed.
   * 
   * @param aTrace
   *          ProcessTrace object that will log events in this method.
   * @throws ResourceProcessException
   *           if there is an error in processing the Resource
   * @throws IOException
   *           if there is an IO Error
   * @see org.apache.uima.collection.CasConsumer#collectionProcessComplete(ProcessTrace)
   */
  public void collectionProcessComplete(ProcessTrace aTrace) throws ResourceProcessException,
          IOException {
    System.out.println("Finished with ");
    float recall = (float) mTruePositiveCount / mTrueAnswers.size();
    float precision = (float) mTruePositiveCount / (mTruePositiveCount + mFalsePositiveCount);
    float fmeasure = 2 * recall * precision / (recall + precision);
    System.out.println("Recall = " + recall);
    System.out.println("Precision = " + precision);
    System.out.println("F-measure= " + fmeasure);
  }

  /**
   * Called if clean up is needed in case of exit under error conditions.
   * 
   * @see org.apache.uima.resource.Resource#destroy()
   */
  public void destroy() {
  }
}
