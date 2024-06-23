import { FaqQuestion, HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Textarea } from 'components/forms/Textarea'
import { Button, IconButton } from 'components/forms/Button'

/**
 * Returns an editor for the FAQ element of a website section
 */
export const FrequentlyAskedQuestionEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const questions = config.questions as FaqQuestion[] || []
  const faqContentId = useId()
  const faqTargetSelector = `#${faqContentId}`
  return (<div>
    <div className="pb-1">
      <button
        aria-controls={faqTargetSelector}
        aria-expanded="true"
        className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
        data-bs-target={faqTargetSelector}
        data-bs-toggle="collapse"
      >
        <span className={'form-label fw-semibold mb-0'}>Questions ({questions.length})</span>
        <span className="text-center px-2">
          <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
          <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
        </span>
      </button>
    </div>
    <div className="collapse hide rounded-3 mb-2" id={faqContentId}
      style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
      {questions.map((question, i) => {
        return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
          <div className="d-flex justify-content-between align-items-center">
            <span className="h5">Edit question</span>
            <div className="d-flex justify-content-end">
              <IconButton icon={faChevronUp} aria-label={'Move Up'} disabled={i < 1}
                onClick={() => {
                  const newQuestions = [...config.questions as FaqQuestion[]]
                  const temp = newQuestions[i]
                  newQuestions[i] = newQuestions[i - 1]
                  newQuestions[i - 1] = temp
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
                }}/>
              <IconButton icon={faChevronDown} aria-label={'Move Down'} disabled={i > questions.length - 2}
                onClick={() => {
                  const newQuestions = [...config.questions as FaqQuestion[]]
                  const temp = newQuestions[i]
                  newQuestions[i] = newQuestions[i + 1]
                  newQuestions[i + 1] = temp
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
                }}/>
              <IconButton icon={faTimes} className={'text-danger'} aria-label={'Delete'} onClick={() => {
                const newQuestions = [...config.questions as FaqQuestion[]]
                newQuestions.splice(i, 1)
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
              }}/>
            </div>
          </div>
          <TextInput label="Question" placeholder={'Enter a question'} value={question.question} onChange={value => {
            const newQuestions = [...config.questions as FaqQuestion[]]
            newQuestions[i].question = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
          }}/>
          <Textarea rows={3} label="Answer" placeholder={'Enter an answer'} value={question.answer} onChange={value => {
            const newQuestions = [...config.questions as FaqQuestion[]]
            newQuestions[i].answer = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
          }}/>
        </div>
      })}
      <Button onClick={() => {
        const newQuestions = [...config.questions as FaqQuestion[]]
        newQuestions.push({ question: '', answer: '' })
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
      }}><FontAwesomeIcon icon={faPlus}/> Add Question</Button>
    </div>
  </div>
  )
}
