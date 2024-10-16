import { FaqQuestion, HtmlSection, SectionConfig } from '@juniper/ui-core'
import React, { useId } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { TextInput } from 'components/forms/TextInput'
import { Textarea } from 'components/forms/Textarea'
import { Button } from 'components/forms/Button'
import { ListElementController } from '../components/ListElementController'
import { CollapsibleSectionButton } from '../components/CollapsibleSectionButton'

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
    <CollapsibleSectionButton targetSelector={faqTargetSelector} sectionLabel={`Questions (${questions.length})`}/>
    <div className="collapse hide rounded-3 mb-2" id={faqContentId}
      style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
      {questions.map((question, i) => {
        return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
          <div className="d-flex justify-content-between align-items-center">
            <span className="h5">Edit question</span>
            <ListElementController<FaqQuestion>
              index={i}
              items={questions}
              updateItems={newQuestions => {
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
              }}/>
          </div>
          <TextInput label="Question" className="mb-2" placeholder={'Enter a question'}
            value={question.question} onChange={value => {
              const newQuestions = [...questions]
              newQuestions[i].question = value
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
            }}/>
          <Textarea rows={3} label="Answer" className="mb-2" placeholder={'Enter an answer'}
            value={question.answer} onChange={value => {
              const newQuestions = [...questions]
              newQuestions[i].answer = value
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
            }}/>
        </div>
      })}
      <Button onClick={() => {
        const newQuestions = [...questions]
        newQuestions.push({ question: '', answer: '' })
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, questions: newQuestions }) })
      }}><FontAwesomeIcon icon={faPlus}/> Add Question</Button>
    </div>
  </div>
  )
}
