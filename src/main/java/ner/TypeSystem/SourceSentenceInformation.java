

/* First created by JCasGen Sat Oct 13 00:17:17 EDT 2012 */
package ner.TypeSystem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Oct 13 02:16:23 EDT 2012
 * XML source: /Users/htcbug/Documents/Develop/workspace/hw1-yuangu/src/main/resources/data/ner/descriptors/type_system/SourceSentenceInformation.xml
 * @generated */
public class SourceSentenceInformation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SourceSentenceInformation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SourceSentenceInformation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SourceSentenceInformation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SourceSentenceInformation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SourceSentenceInformation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: identifier

  /** getter for identifier - gets identifier represents the identifier of the input sentence
   * @generated */
  public String getIdentifier() {
    if (SourceSentenceInformation_Type.featOkTst && ((SourceSentenceInformation_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "ner.TypeSystem.SourceSentenceInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceSentenceInformation_Type)jcasType).casFeatCode_identifier);}
    
  /** setter for identifier - sets identifier represents the identifier of the input sentence 
   * @generated */
  public void setIdentifier(String v) {
    if (SourceSentenceInformation_Type.featOkTst && ((SourceSentenceInformation_Type)jcasType).casFeat_identifier == null)
      jcasType.jcas.throwFeatMissing("identifier", "ner.TypeSystem.SourceSentenceInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceSentenceInformation_Type)jcasType).casFeatCode_identifier, v);}    
  }

    