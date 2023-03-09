import React, { useState } from 'react'
import _cloneDeep from 'lodash/cloneDeep'
import { questionFromRawText, QuestionObj, panelObjsToJson, questionToJson, PanelObj } from 'util/pearlSurveyUtils'

/** Component for helping transform plaintext survey questions (such as from a word doc) to survey definition
 * json.
 */
export default function QuestionScratchbox() {
  const [rawText, setRawText] = useState(' ')
  const [questionObj, setQuestionObj] = useState<QuestionObj>({
    namePrefix: 'oh_oh_medList_', nameSuffix: '', type: 'radiogroup', isRequired: true,
    title: '', choices: [], otherText: '', otherPlaceholder: 'Please specify'
  })
  const [showSubPanels, setShowSubPanels] = useState(false)
  /** update the question as a whole, in response to new raw text being pasted in */
  function updateQuestion(newText: string) {
    const newQuestionObj = questionFromRawText(newText)
    newQuestionObj.namePrefix = questionObj.namePrefix
    setQuestionObj(newQuestionObj)
    setRawText(newText)
  }

  /** update a single property of the question */
  function updateQuestionSimpleProp(prop: string, value: string | boolean) {
    const newQuestionObj = _cloneDeep(questionObj)
    // eslint-disable-next-line
    // @ts-ignore
    newQuestionObj[prop] = value
    setQuestionObj(newQuestionObj)
  }

  /** update the value (stableId) of a given choice */
  function updateChoiceValue(index: number, value: string) {
    const newQuestionObj = _cloneDeep(questionObj)
    if (newQuestionObj.choices && questionObj.choices) {
      newQuestionObj.choices[index] = {
        ...questionObj.choices[index],
        value
      }
    }
    setQuestionObj(newQuestionObj)
  }

  const subPanelsJson = panelObjsToJson(subPanelsForQuestion(questionObj))

  /** copy the json text to the clipboard */
  function copyText() {
    navigator.clipboard.writeText(questionToJson(questionObj))
  }


  const showCopy = questionObj.nameSuffix.length > 1

  return <div className="row">
    <div className="col-md-12 d-flex ms-5 mt-5">
      <div>
        <h6>Question and choices</h6>
        <textarea rows={50} cols={50} value={rawText} onChange={e => updateQuestion(e.target.value)} />
      </div>
      <div className="ms-4">
        <h6>
          JSON translation
          {showCopy && <button className="btn btn-secondary" onClick={copyText}>Copy</button> }
        </h6>
        <div style={{ fontFamily: 'monospace' }}>
          <label>name: {questionObj.namePrefix}
            <input type="text" size={30} value={questionObj.nameSuffix}
              onChange={e => updateQuestionSimpleProp('nameSuffix', e.target.value)}/></label>
          <br/>

          <label>type:
            <select value={questionObj.type} onChange={e => updateQuestionSimpleProp('type', e.target.value)}>
              <option>radiogroup</option>
              <option>checkbox</option>
              <option>text</option>
            </select></label>
          <br/>

          <label>title: <textarea rows={2} cols={80} value={questionObj.title}
            onChange={e => updateQuestionSimpleProp('title', e.target.value)}/>
          </label><br/>
          <label>is Required:
            <input type="checkbox" checked={questionObj.isRequired}
              onChange={() => updateQuestionSimpleProp('isRequired', !questionObj.isRequired)}/>
          </label><br/>

          <label>visibleIf: <input type="text" size={80} value={questionObj.visibleIf}
            onChange={e => updateQuestionSimpleProp('visibleIf', e.target.value)}/>
          </label><br/>

          <label>otherText:
            <input type="text" size={80} value={questionObj.otherText}
              onChange={e => updateQuestionSimpleProp('otherText', e.target.value)}/></label>
          <br/>
          <label>otherPlaceholder:
            <input type="text" size={80} value={questionObj.otherPlaceholder}
              onChange={e => updateQuestionSimpleProp('otherPlaceholder', e.target.value)}/></label>
          <br/>

          choices:
          <div className="ms-3">
            { questionObj.choices && questionObj.choices.map((choice, index) => {
              return <div key={index} className="mt-1">
                <label>{choice.text}:
                  <input type="text" size={30} value={choice.value}
                    onChange={e => updateChoiceValue(index, e.target.value)}/>
                </label>
              </div>
            })}
          </div>
        </div>
      </div>
      <div className="ms-2">
        <button onClick={() => setShowSubPanels(!showSubPanels)}>Toggle subpanels</button>
        <button className="ms-3" onClick={() => navigator.clipboard.writeText(subPanelsJson)}>Copy</button>
        {showSubPanels && <pre>
          {subPanelsJson}
        </pre> }

      </div>
    </div>
  </div>
}

/**
 * iterate over each choice in a question and generate a follow-up panel.  the utility of this function might
 * be exclusively limited to OurHealth medical history survey
 * https://docs.google.com/document/d/1jmsfFFJQ1Hkz84Qc2Cxvdvo23mS4MIiH/edit#
 * so for now it is extremely hardcoded for that case.  As other follow-up question patterns become
 * apparent, this should either be extended or removed
 * @param questionObj
 */
function subPanelsForQuestion(questionObj: QuestionObj): PanelObj[] {
  return (questionObj.choices || []).map(choice => {
    return {
      type: 'panel',
      title: choice.text,
      visibleIf: `{${questionObj.namePrefix}${questionObj.nameSuffix}} contains '${choice.value}'`,
      elements: [
        {
          namePrefix: questionObj.namePrefix,
          nameSuffix: `${choice.value}InTreatment`,
          type: 'template',
          questionTemplateName: 'oh_oh_medHx_stillSeeingProvider'
        },
        {
          namePrefix: questionObj.namePrefix,
          nameSuffix: `${choice.value}AgeAtDx`,
          type: 'template',
          questionTemplateName: 'oh_oh_medHx_ageAboutAtDiagnosis'
        }
      ]
    }
  })
}

