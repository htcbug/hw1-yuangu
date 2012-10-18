package ner.CollectionReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import ner.TypeSystem.SourceSentenceInformation;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

/**
 * The LineBreakCollectionReader class processes the input document by line. It reads one document
 * from the filesystem and outputs each line as a CAS. It can be configured with the following
 * parameters:
 * 
 * <ul>
 * <li><code>InputFile</code> - path to the input file</li>
 * </ul>
 * 
 * @author <a href="mailto:yuangu@andrew.cmu.edu">Yuan Gu</a>
 */

public class LineBreakCollectionReader extends CollectionReader_ImplBase {
  /**
   * Name of configuration parameter that must be set to the path of the input file.
   */
  public static final String PARAM_INPUTFILE = "InputFile";

  /**
   * The input file.
   */
  private File mFile;

  /**
   * BufferedReader to the input file.
   */
  private BufferedReader mBufferedReader;

  /**
   * Line number of the input file.
   */
  private int mLineNumber;

  /**
   * Index of the next line to be processed.
   */
  private int mNextLine;

  /**
   * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
   */
  public void initialize() throws ResourceInitializationException {
    File file = new File(((String) getConfigParameterValue(PARAM_INPUTFILE)).trim());
    if (!file.exists() || !file.isFile()) {
      throw new ResourceInitializationException(
              ResourceConfigurationException.RESOURCE_DATA_NOT_VALID, new Object[] {
                  PARAM_INPUTFILE, this.getMetaData().getName(), file.getPath() });
    }

    mFile = file;
    mLineNumber = getLineNumber(mFile);
    mNextLine = 0;

    // open the file
    try {
      mBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
    } catch (FileNotFoundException e) {
      throw new ResourceInitializationException(
              ResourceConfigurationException.RESOURCE_DATA_NOT_VALID, new Object[] {
                  PARAM_INPUTFILE, this.getMetaData().getName(), file.getPath() });
    }
  }

  /**
   * Get the line number of the file.
   * 
   * @param file
   */
  private int getLineNumber(File file) {

    LineNumberReader lnr;
    try {
      lnr = new LineNumberReader(new FileReader(file));
      lnr.skip(Long.MAX_VALUE);
    } catch (FileNotFoundException e) {
      return 0;
    } catch (IOException e) {
      return 0;
    }
    return lnr.getLineNumber();
  }

  /**
   * @see org.apache.uima.collection.CollectionReader#hasNext()
   */
  public boolean hasNext() {
    return mNextLine < mLineNumber;
  }

  /**
   * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
   */
  public void getNext(CAS aCAS) throws IOException, CollectionException {

    // update the line index
    mNextLine++;

    String line;
    String identifier;
    String content;

    line = mBufferedReader.readLine();
    if (line == null) {
      return;
    }

    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }

    // break the sentence into identifier and content
    String strArray[] = line.split(" ", 2);

    if (strArray.length < 2) {
      return;
    }

    identifier = strArray[0];
    content = strArray[1];

    // store the content of the line in CAS
    jcas.setDocumentText(content);

    // also store identifier of the line in the source document in CAS.
    SourceSentenceInformation srcSentenceInfo = new SourceSentenceInformation(jcas);
    srcSentenceInfo.setIdentifier(identifier);

    srcSentenceInfo.addToIndexes();
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
   */
  public void close() throws IOException {
    mBufferedReader.close();
  }

  /**
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
   */
  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(mNextLine, mLineNumber, Progress.ENTITIES) };
  }

  /**
   * Gets the total number of documents that will be returned by this collection reader. This is not
   * part of the general collection reader interface.
   * 
   * @return the number of documents in the collection
   */
  public int getNumberOfDocuments() {
    return mLineNumber;
  }
}
