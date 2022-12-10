import _ from 'lodash'
import React from 'react'
import ReactMarkdown from 'react-markdown'

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

type FrequentlyAskedQuestionsProps = {
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  questions?: FaqQuestion[], // the questions
  title?: string, // large heading text
  color?: string // foreground text color
}

/**
 * Template for rendering a Frequently Asked Questions block.
 */
function FrequentlyAskedQuestionsTemplate({
  config: {
    backgroundColor,
    blurb,
    color,
    questions,
    title = 'Frequently Asked Questions'
  }
}: {config: FrequentlyAskedQuestionsProps}) {
  return <div className="d-flex justify-content-center py-5" style={{ backgroundColor, color }}>
    <div className="col-lg-6">
      <h1 className="fs-1 fw-normal lh-sm mt-5 mb-4 text-center">Frequently Asked Questions</h1>
      <div className='fs-5 fw-normal mb-4 text-center'>
        {blurb && <ReactMarkdown>{blurb}</ReactMarkdown> }
      </div>
      <div className="border-bottom"></div>
      <div className="fs-5 fw-normal">
        {
          _.map(questions, ({ question, answer }) => {
            return <div className="border-bottom p-3">
              <button type="button" className="btn btn-lg btn-link text-white text-decoration-none"
                data-bs-toggle="collapse" data-bs-target={targetFor(question)}>
                + {question}
              </button>
              <div className="collapse" id={idFor(question)}>
                <p>
                  {answer}
                </p>
              </div>
            </div>
          })
        }
      </div>
    </div>
  </div>
}

export default FrequentlyAskedQuestionsTemplate
