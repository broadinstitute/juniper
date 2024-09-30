import { CalculatedValue, Question } from 'survey-core'
import { Answer, instantToDefaultString, PortalEnvironmentLanguage } from '@juniper/ui-core'
import { DataChangeRecord } from 'api/api'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight, faChevronDown, faHistory, faPencil } from '@fortawesome/free-solid-svg-icons'
import { AdminUser } from 'api/adminUser'
import React from 'react'
import { getDisplayValue } from './SurveyFullDataView'
import { sortBy } from 'lodash'

/**
 * Renders a dropdown with the edit history for a question response
 */
export const AnswerEditHistory = ({ question, answer, editHistory, supportedLanguages }: {
    question: Question | CalculatedValue, answer: Answer,
  editHistory: DataChangeRecord[], supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const { users } = useAdminUserContext()
  const answerLanguage = supportedLanguages?.find(lang => lang.languageCode === answer?.viewedLanguage)

  return <>
    <div
      data-bs-toggle='dropdown'
      role='button'
      className="btn btn-light p-0"
      id="viewHistory"
      aria-label="View history"
      aria-haspopup="true"
      aria-expanded="false"
    >
      <div className="d-flex align-items-center">
        <pre className="fw-bold my-0 text-break text-start" style={{ whiteSpace: 'pre-wrap' }}>
          {getDisplayValue(answer, question)}
        </pre>
        { editHistory.length > 0 ?
          <FontAwesomeIcon icon={faHistory} className="fa-sm ms-2 text-muted"/> :
          <FontAwesomeIcon icon={faChevronDown} className="fa-sm ms-2 text-muted"/>}
      </div>
    </div>
    <div className="dropdown-menu" aria-labelledby="viewHistory">
      {answerLanguage && <span className="ms-2 text-muted fst-italic float-end me-3" style={{ fontSize: '0.75em' }}>
          answered in {answerLanguage.languageName}
      </span>}
      {editHistory.map((changeRecord, index) =>
        <div key={index} className="dropdown-item d-flex align-items-center" style={{ pointerEvents: 'none' }}>
          <FontAwesomeIcon icon={faPencil} className="me-2"/>
          <div>
            {renderChangeRecordSimple(changeRecord)}
            <div className="text-muted" style={{ fontSize: '0.75em' }}>
                            Edited on {instantToDefaultString(changeRecord.createdAt)} by <span className='fw-semibold'>
                {users.find(user =>
                  user.id === changeRecord.responsibleAdminUserId)?.username ?? 'Participant'}
              </span>
            </div>
          </div>
        </div>
      )}
      {renderOriginalAnswer(question, answer, editHistory, users)}
    </div>
  </>
}

const renderChangeRecordSimple = (changeRecord: DataChangeRecord) => {
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
const renderOriginalAnswer = (
  question: Question | CalculatedValue, answer: Answer, changeRecords: DataChangeRecord[], users: AdminUser[]
) => {
  const originalChangeRecord = sortBy(changeRecords, 'createdAt')[0]
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
