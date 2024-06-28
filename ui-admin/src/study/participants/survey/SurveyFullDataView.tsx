import React, { useEffect, useState } from 'react'
import { CalculatedValue, Question, SurveyModel } from 'survey-core'

import {
  createAddressValidator, Enrollee, instantToDefaultString,
  makeSurveyJsData,
  PortalEnvironment,
  PortalEnvironmentLanguage,
  surveyJSModelFromForm
} from '@juniper/ui-core'
import Api, { Answer, DataChangeRecord, Survey } from 'api/api'
import InfoPopup from 'components/forms/InfoPopup'
import PrintFormModal from './PrintFormModal'
import { Route, Routes } from 'react-router-dom'
import { renderTruncatedText } from 'util/pageUtils'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight, faHistory, faPencil } from '@fortawesome/free-solid-svg-icons'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { AdminUser } from 'api/adminUser'

type SurveyFullDataViewProps = {
  responseId?: string,
  answers: Answer[],
  survey: Survey,
  resumeData?: string,
  enrollee?: Enrollee,
  studyEnvContext: StudyEnvContextT
}

/** renders every item in a survey response */
export default function SurveyFullDataView({
  responseId, answers, resumeData, survey, enrollee, studyEnvContext
}: SurveyFullDataViewProps) {
  const [showAllQuestions, setShowAllQuestions] = useState(true)
  const [showFullQuestions, setShowFullQuestions] = useState(false)
  const [changeRecords, setChangeRecords] = useState<DataChangeRecord[]>([])
  const surveyJsData = makeSurveyJsData(resumeData, answers, enrollee?.participantUserId)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.onServerValidateQuestions.add(createAddressValidator(addr => Api.validateAddress(addr)))
  surveyJsModel.data = surveyJsData!.data
  const answerMap: Record<string, Answer> = {}
  answers.forEach(answer => {
    answerMap[answer.questionStableId] = answer
  })
  let questions = getQuestionsWithComputedValues(surveyJsModel)
  if (!showAllQuestions) {
    questions = questions.filter(q => !!answerMap[q.name])
  }

  const portalEnv = studyEnvContext.portal.portalEnvironments.find((env: PortalEnvironment) =>
    env.environmentName === studyEnvContext.currentEnv.environmentName)
  const supportedLanguages = portalEnv?.supportedLanguages ?? []

  useEffect(() => {
    if (responseId && enrollee) {
      Api.fetchEnrolleeChangeRecords(
        studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode, survey.stableId).then(changeRecords => {
        setChangeRecords(changeRecords)
      })
    }
  }, [responseId])

  return <div>
    <div className="d-flex d-print-none">
      <div className="d-flex align-items-center">
        <label>
          <input type="checkbox" className="me-2"
            checked={showAllQuestions} onChange={() => setShowAllQuestions(!showAllQuestions)}/>
          Show all questions
        </label>
        <InfoPopup content="Show all questions in the survey, or only questions answered by the participant"/>
      </div>
      <div className="d-flex align-items-center">
        <label className="ms-4">
          <input type="checkbox" className="me-2"
            checked={showFullQuestions} onChange={() => setShowFullQuestions(!showFullQuestions)}/>
          Show full question text
        </label>
        <InfoPopup content="Show full question text vs. truncated to first 100 characters"/>
      </div>
    </div>
    <hr/>
    <Routes>
      <Route path="print" element={<PrintFormModal answers={answers}
        resumeData={resumeData}
        survey={survey}/>
      }/>
      <Route index element={<dl>
        {questions.map((question, index) =>
          <ItemDisplay key={index} question={question} answerMap={answerMap} supportedLanguages={supportedLanguages}
            surveyVersion={survey.version} showFullQuestions={showFullQuestions} editHistory={changeRecords}/>)}
      </dl>}/>
    </Routes>
  </div>
}

type ItemDisplayProps = {
  question: Question | CalculatedValue,
  answerMap: Record<string, Answer>,
  surveyVersion: number,
  showFullQuestions: boolean,
  supportedLanguages: PortalEnvironmentLanguage[],
  editHistory?: DataChangeRecord[]
}

/**
 * Renders a single survey question and its answer,
 * with stableId and the viewed language (if applicable)
 */
export const ItemDisplay = ({
  question, answerMap, surveyVersion, showFullQuestions, supportedLanguages, editHistory = []
}: ItemDisplayProps) => {
  const answer = answerMap[question.name]
  const answerLanguage = supportedLanguages.find(lang => lang.languageCode === answer?.viewedLanguage)
  const editHistoryForQuestion = editHistory
    .filter(changeRecord => changeRecord.fieldName === question.name)
    .sort((a, b) => b.createdAt - a.createdAt)
  const displayValue = getDisplayValue(answer, question)
  let stableIdText = question.name
  if (answer && answer.surveyVersion !== surveyVersion) {
    stableIdText = `${answer.questionStableId} v${answer.surveyVersion}`
  }
  if ((question as CalculatedValue).expression) {
    stableIdText += ' -- derived'
  }

  return <>
    <dt className="fw-normal">
      <div className="d-flex align-items-center">
        {renderQuestionText(answer, question, showFullQuestions)}
        <span className="ms-2 fst-italic text-muted">
        ({stableIdText}) {answerLanguage && ` (Answered in ${answerLanguage.languageName})`}
        </span>
        { answer && <ResponseEditHistory question={question} answer={answer} editHistory={editHistoryForQuestion}/> }
      </div>
    </dt>
    <dl>
      <pre className="fw-bold">{displayValue}</pre>
    </dl>
  </>
}

/**
 * Renders a dropdown with the edit history for a question response
 */
export const ResponseEditHistory = ({ question, answer, editHistory }: {
  question: Question | CalculatedValue, answer: Answer, editHistory: DataChangeRecord[]
}) => {
  const { users } = useAdminUserContext()

  return <>
    <div
      data-bs-toggle='dropdown'
      role='button'
      className="btn btn-light dropdown-toggle fst-italic ms-2 rounded-3 p-1 border-1"
      id="viewHistory"
      aria-label="View history"
      aria-haspopup="true"
      aria-expanded="false"
    ><FontAwesomeIcon icon={faHistory} className="fa-sm"/> View history</div>
    <div className="dropdown-menu" aria-labelledby="viewHistory">
      {editHistory.map((changeRecord, index) =>
        <div key={index} className="dropdown-item d-flex align-items-center" style={{ pointerEvents: 'none' }}>
          <FontAwesomeIcon icon={faPencil} className="me-2"/>
          <div>
            {getBeforeAndAfterAnswer(changeRecord)}
            <div className="text-muted" style={{ fontSize: '0.75em' }}>
                Edited on {instantToDefaultString(changeRecord.createdAt)} by <span className='fw-semibold'>
                {users.find(user =>
                  user.id === changeRecord.responsibleAdminUserId)?.username ?? 'Participant'}
              </span>
            </div>
          </div>
        </div>
      )}
      {getOriginalAnswer(question, answer, editHistory, users)}
    </div>
  </>
}

const getBeforeAndAfterAnswer = (changeRecord: DataChangeRecord) => {
  return <div className="d-flex align-items-center">
    <div className="bg-danger-subtle fw-medium">{changeRecord.oldValue}</div>
    <FontAwesomeIcon icon={faArrowRight} className="mx-1"/>
    <div className="bg-success-subtle fw-medium">{changeRecord.newValue}</div>
  </div>
}

/*
 * Displays the original answer value and the entity responsible for answering it. If changes have
 * been made to the answer, we backtrack through the change records to find the original answer.
 */
const getOriginalAnswer = (
  question: Question | CalculatedValue, answer: Answer, changeRecords: DataChangeRecord[], users: AdminUser[]
) => {
  const originalChangeRecord = changeRecords.sort((a, b) => a.createdAt > b.createdAt ? 1 :
    a.createdAt < b.createdAt ? -1 : 0)[0]
  return <div className="dropdown-item d-flex align-items-center" style={{ pointerEvents: 'none' }}>
    <FontAwesomeIcon icon={faPencil} className="me-2"/>
    <div>
      <span className='fw-medium'>
        {originalChangeRecord ? originalChangeRecord.oldValue : getDisplayValue(answer, question)}
      </span>
      <div className="text-muted" style={{ fontSize: '0.75em' }}>
          Answered on {instantToDefaultString(answer.createdAt)} by <span className='fw-semibold'>
          {users.find(user =>
            user.id === answer.creatingAdminUserId)?.username ?? 'Participant'}
        </span>
      </div>
    </div>
  </div>
}

/** renders the value of the answer, either as plaintext, a matched choice, or an image for signatures */
export const getDisplayValue = (answer: Answer,
  question: Question | QuestionWithChoices | CalculatedValue): React.ReactNode => {
  const isCalculatedValue = !!(question as CalculatedValue).expression
  if (!answer) {
    if (!(question as Question).isVisible || isCalculatedValue) {
      return <span className="text-muted fst-italic fw-normal">n/a</span>
    } else {
      return <span className="text-muted fst-italic fw-normal">no answer</span>
    }
  }
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue ?? answer.booleanValue

  let displayValue: React.ReactNode = answerValue
  if ((question as Question).choices) {
    if (answer.objectValue) {
      const valueArray = JSON.parse(answer.objectValue)
      const textArray = valueArray.map((value: string | number) => getTextForChoice(value, question as Question))
      displayValue = JSON.stringify(textArray)
    } else {
      displayValue = getTextForChoice(answerValue, question as Question)
    }
  }
  if (answer.booleanValue !== undefined) {
    displayValue = answer.booleanValue ? 'True' : 'False'
  }
  if (question.getType() === 'signaturepad') {
    displayValue = <img src={answer.stringValue}/>
  }
  if (answer.otherDescription) {
    displayValue = `${displayValue} - ${answer.otherDescription}`
  }
  return displayValue
}

/**
 * matches the answer stableId with the choice of the question.  note that this is not yet version-safe
 * and so, e.g.,  answers to choices that no longer exist may not render correctly
 */
export const getTextForChoice = (value: string | number | boolean | undefined, question: Question) => {
  return question.choices.find((choice: ItemValue)  => choice.value === value)?.text ?? value
}

type QuestionWithChoices = Question & {
  choices: ItemValue[]
}
type ItemValue = { text: string, value: string }


/** gets the question text -- truncates it at 100 chars */
export const renderQuestionText = (answer: Answer,
  question: Question | CalculatedValue,
  showFullQuestions: boolean) => {
  if (!question) {
    return <span>-</span>
  }
  const questionText = (question as Question).title
  return renderTruncatedText(questionText, showFullQuestions ? 10000 : 100)
}

/**
 * returns the last stableId that this calculatedValue is dependent on, or null
 * if it is independent.
 * e.g. if the expression is "{heightInInches} * 2.54", this will return "heightInInches"
 * This should match the logic in SurveyParseUtils.java
 */
export function getUpstreamStableId(calculatedValue: CalculatedValue): string | undefined {
  const match = calculatedValue.expression.match(/.*\{(.+?)\}.*/)
  return match ? match[1] : undefined
}

/**
 * returns an array of the questions for display, which excludes html elements, and includes
 * calculatedValues that have includeIntoResult
 */
export function getQuestionsWithComputedValues(model: SurveyModel) {
  const questionsAndVals: (Question | CalculatedValue)[] = model
    .getAllQuestions().filter(q => q.getType() !== 'html')
  model.calculatedValues
    .filter(calculatedValue => calculatedValue.includeIntoResult)
    .forEach(calculatedValue => {
      // figure out where in the question list to insert this, based on which questions the computation uses.
      const upstreamStableId = getUpstreamStableId(calculatedValue)
      if (!upstreamStableId) {
        questionsAndVals.push(calculatedValue)
      } else {
        const spliceIndex = questionsAndVals.findIndex(question => question.name === upstreamStableId)
        questionsAndVals.splice(spliceIndex, 0, calculatedValue)
      }
    })
  return questionsAndVals
}

