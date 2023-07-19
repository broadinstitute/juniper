import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { faXmark } from '@fortawesome/free-solid-svg-icons/faXmark'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Collapse } from 'bootstrap'
import classNames from 'classnames'
import _ from 'lodash'
import React, { useCallback, useEffect, useRef, useState } from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { requireOptionalBoolean, requireOptionalString, requirePlainObject, requireString }
  from '../../util/validationUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'

import { Markdown } from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../../participant/ApiProvider'

const idFor = (question: string): string => {
  return _.kebabCase(question)
}

const targetFor = (question: string): string => {
  return `#${idFor(question)}`
}

type FaqQuestion = {
  question: string,
  answer: string
}

type FrequentlyAskedQuestionsConfig = {
  blurb?: string, //  text below the title
  questions: FaqQuestion[], // the questions
  showToggleAllButton?: boolean, // whether or not the show the expand/collapse button
  title?: string, // large heading text
}

const validateFaqQuestion = (questionConfig: unknown): FaqQuestion => {
  const message = 'Invalid FrequentlyAskedQuestionsConfig: Invalid question'
  const config = requirePlainObject(questionConfig, message)
  const question = requireString(config, 'question', message)
  const answer = requireString(config, 'answer', message)
  return { question, answer }
}

/** Validate that a section configuration object conforms to FrequentlyAskedQuestionsConfig */
const validateFrequentlyAskedQuestionsConfig = (config: SectionConfig): FrequentlyAskedQuestionsConfig => {
  const message = 'Invalid FrequentlyAskedQuestionsConfig'
  const title = requireOptionalString(config, 'title', message)
  const blurb = requireOptionalString(config, 'blurb', message)
  const showToggleAllButton = requireOptionalBoolean(config, 'showToggleAllButton', message)

  const questions = config.questions
  if (!Array.isArray(questions)) {
    throw new Error(`${message}: a list of questions is required`)
  }

  return {
    blurb,
    questions: questions.map(validateFaqQuestion),
    showToggleAllButton,
    title
  }
}

type FrequentlyAskedQuestionProps = {
  question: string
  answer: string
  onToggle: (isExpanded: boolean) => void
}

const FrequentlyAskedQuestion = (props: FrequentlyAskedQuestionProps) => {
  const { question, answer, onToggle } = props

  const collapseRef = useRef<HTMLDivElement>(null)
  useEffect(() => {
    collapseRef.current?.addEventListener('show.bs.collapse', () => { onToggle(true) })
    collapseRef.current?.addEventListener('hide.bs.collapse', () => { onToggle(false) })
  }, [onToggle])

  return (
    <>
      <button
        aria-controls={targetFor(question)}
        aria-expanded="false"
        className={classNames(
          'btn btn-link btn-lg',
          'w-100 py-3 px-0 px-sm-2',
          'd-flex',
          'text-black fw-bold text-start text-decoration-none'
        )}
        data-bs-target={targetFor(question)}
        data-bs-toggle="collapse"
      >
        <span className="me-2 text-center" style={{ width: 20 }}>
          <FontAwesomeIcon className="hidden-when-expanded" icon={faPlus} />
          <FontAwesomeIcon className="hidden-when-collapsed" icon={faXmark} />
        </span>
        {question}
      </button>
      <div ref={collapseRef} className="collapse px-0 px-sm-2 ms-2" id={idFor(question)}>
        <Markdown className="mb-3 fs-5" style={{ marginLeft: 20 }}>
          {answer}
        </Markdown>
      </div>
    </>
  )
}

type FrequentlyAskedQuestionsProps = TemplateComponentProps<FrequentlyAskedQuestionsConfig>

/**
 * Template for rendering a Frequently Asked Questions block.
 */
function FrequentlyAskedQuestionsTemplate(props: FrequentlyAskedQuestionsProps) {
  const { anchorRef, config } = props
  const {
    blurb,
    questions,
    showToggleAllButton = true,
    title = 'Frequently Asked Questions'
  } = config

  const { getImageUrl } = useApiContext()

  const [numExpandedQuestions, setNumExpandedQuestions] = useState(0)
  const allQuestionsAreExpanded = numExpandedQuestions === questions.length

  const onToggleQuestion = useCallback((isExpanded: boolean) => {
    setNumExpandedQuestions(isExpanded ? n => n + 1 : n => n - 1)
  }, [])

  const questionsListRef = useRef<HTMLUListElement>(null)

  const expandAll = useCallback(() => {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const collapses = Array.from(questionsListRef.current!.querySelectorAll('.collapse'))
    collapses.forEach(el => { Collapse.getOrCreateInstance(el).show() })
  }, [])

  const collapseAll = useCallback(() => {
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const collapses = Array.from(questionsListRef.current!.querySelectorAll('.collapse'))
    collapses.forEach(el => { Collapse.getOrCreateInstance(el).hide() })
  }, [])

  return <div id={anchorRef} className="row mx-0 justify-content-center" style={getSectionStyle(config, getImageUrl)}>
    <div className="col-12 col-sm-8 col-lg-6">
      {!!title && (
        <h2 className="fs-1 fw-normal lh-sm mb-4 text-center">{title}</h2>
      )}
      {!!blurb && (
        <Markdown className="fs-4 mb-4 text-center">
          {blurb}
        </Markdown>
      )}
      {showToggleAllButton && (
        <div className="mb-4">
          <button
            className="btn btn-outline-dark btn-rounded text-uppercase"
            onClick={allQuestionsAreExpanded ? collapseAll : expandAll}
          >
            <span className="d-inline-block me-2 text-center" style={{ width: 20 }}>
              <FontAwesomeIcon icon={allQuestionsAreExpanded ? faXmark : faPlus} />
            </span>
            {allQuestionsAreExpanded ? 'Collapse' : 'Expand'} all
          </button>
        </div>
      )}
      <ul ref={questionsListRef} className="list-unstyled mb-0 border-top">
        {
          questions.map(({ question, answer }, i) => {
            return (
              <li key={i} className="border-bottom">
                <FrequentlyAskedQuestion question={question} answer={answer} onToggle={onToggleQuestion} />
              </li>
            )
          })
        }
      </ul>
    </div>
  </div>
}

export default withValidatedSectionConfig(validateFrequentlyAskedQuestionsConfig, FrequentlyAskedQuestionsTemplate)
