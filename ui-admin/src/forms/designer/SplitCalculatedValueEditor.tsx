import {
  FormContent,
  FormElement,
  FormPanel,
  Question,
  surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, {
  useEffect,
  useMemo,
  useState
} from 'react'
import { IconButton } from 'components/forms/Button'
import { faCode } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { Textarea } from 'components/forms/Textarea'
import { toString } from 'lodash/fp'
/* Note that this component is memoized using React.memo
 * Since survey pages can contain many elements, we need to be mindful of
 * how many times we re-render these components. Since the parent component state
 * is updated with every keystroke, we memoize this to minimize the number
 * of re-renders that take place. The SurveyComponent from SurveyJS in particular
 * is sluggish when undergoing many simultaneous re-renders.
 */

type CalculatedValue = {
  name: string,
  expression: string,
  includeIntoResult: boolean
}

export const SplitCalculatedValueEditor = ({
  editedContent, onChange, calculatedValueIndex
}: {
  calculatedValueIndex: number,
  editedContent: FormContent,
  onChange: (newContent: FormContent) => void
}) => {
  const [showJsonEditor, setShowJsonEditor] = useState(false)

  const calculatedValue = editedContent?.calculatedValues
    ? editedContent.calculatedValues[calculatedValueIndex]
    : createNewCalculatedValue()

  const updateCalculatedValue = (newCalculatedValue: CalculatedValue) => {
    const newContent = { ...editedContent }
    if (!newContent.calculatedValues) {
      newContent.calculatedValues = []
    }

    if (newContent.calculatedValues.length <= calculatedValueIndex) {
      newContent.calculatedValues = newContent
        .calculatedValues
        .concat(new Array(calculatedValueIndex - newContent.calculatedValues.length + 1)
          .fill(createNewCalculatedValue()))
    }

    newContent.calculatedValues[calculatedValueIndex] = newCalculatedValue
    onChange(newContent)
  }


  const copyCalculatedValue = () => {
    const newValue = createNewCalculatedValue()

    newValue.expression = calculatedValue.expression
    newValue.name = calculatedValue.name

    return newValue
  }

  const updateExpression = (expression: string) => {
    const newCalculatedValue = copyCalculatedValue()
    newCalculatedValue.expression = expression

    updateCalculatedValue(newCalculatedValue)
  }

  const updateName = (name: string) => {
    const newCalculatedValue = copyCalculatedValue()
    newCalculatedValue.name = name

    updateCalculatedValue(newCalculatedValue)
  }

  const extractQuestionNames = (expression: string) => {
    // expression example: {question1} + {question2} + funciton({question3})

    const regex = /{([^}]+)}/g

    const matches = []

    let match
    while ((match = regex.exec(expression)) !== null) {
      matches.push(match[1])
    }

    return matches
  }

  const isQuestion = (element: FormElement): element is Question => {
    return 'type' in element && element.type !== 'panel' && element.type !== 'html'
  }

  const isPanel = (element: FormElement): element is FormPanel => {
    return 'type' in element && element.type === 'panel'
  }

  const findQuestionsWithNames = (element: FormElement, names: string[]): Question[] => {
    if (isQuestion(element)) {
      if (names.includes(element.name)) {
        return [element]
      }
    } else if (isPanel(element)) {
      return element.elements.flatMap(elem => findQuestionsWithNames(elem, names))
    }
    return []
  }

  const questionsUsedInCalculatedValue: FormElement[] = useMemo(() => {
    const questionNames = extractQuestionNames(calculatedValue.expression)

    return editedContent.pages.flatMap(page =>
      page.elements.flatMap(element => findQuestionsWithNames(element, questionNames))
    )
  }, [calculatedValue.expression, editedContent.pages])

  // Chop the survey down to just the specific question that we're editing, so we can display
  // a preview using the SurveyJS survey component.
  const surveyFromQuestion = {
    title: 'Question Preview',
    pages: [{ elements: questionsUsedInCalculatedValue }],
    questionTemplates: editedContent.questionTemplates,
    calculatedValues: [calculatedValue]
  }

  const [previewResult, setPreviewResult] = useState('')

  const surveyModel = useMemo(() => {
    const surveyModel = surveyJSModelFromFormContent(surveyFromQuestion)
    surveyModel.onVariableChanged.add((_, options) => {
      if (options.name === calculatedValue.name) {
        setPreviewResult(toString(options.value))
      }
    })

    surveyModel.showInvisibleElements = true
    surveyModel.showQuestionNumbers = false
    return surveyModel
  }, [editedContent, calculatedValue, setPreviewResult])

  useEffect(() => {
    console.log('expression')
    console.log(calculatedValue.expression)
    console.log('questionsUsedInCalculatedValue')
    console.log(questionsUsedInCalculatedValue)
    console.log('previewResult')
    console.log(previewResult)
  }, [calculatedValue.expression, questionsUsedInCalculatedValue])

  return <div key={calculatedValueIndex} className="row">
    <div className="col-md-6 p-3 rounded-start-3"
      style={{ backgroundColor: '#f3f3f3', borderRight: '1px solid #fff' }}>
      <div className="d-flex justify-content-between">
        <span className="h5">Edit calculated value</span>
        <div className="d-flex justify-content-end">
          <IconButton icon={faCode}
            aria-label={showJsonEditor ? 'Switch to designer' : 'Switch to JSON editor'}
            className="ms-2"
            onClick={() => setShowJsonEditor(!showJsonEditor)}
          />
          <ListElementController
            index={calculatedValueIndex}
            items={editedContent.calculatedValues || []}
            updateItems={newItems => {
              const newContent = { ...editedContent }
              newContent.calculatedValues = newItems
              onChange(newContent)
            }}
          />
        </div>
      </div>
      <div className="mb-3">
        <Textarea
          label="Name"
          rows={2}
          value={calculatedValue.name}
          onChange={updateName}
        />
      </div>

      <div className="mb-3">
        <Textarea
          label="Expression"
          rows={2}
          value={calculatedValue.expression}
          onChange={updateExpression}
        />
      </div>
    </div>

    <div className="col-md-6 p-3 rounded-end-3 survey-hide-complete"
      style={{ backgroundColor: '#f3f3f3', borderLeft: '1px solid #fff' }}>
      {questionsUsedInCalculatedValue.length > 0
        ? <SurveyComponent
          model={surveyModel}
          readOnly={false}
        />
        : <p>Any questions used in the calculated value will appear here.
          If you provide answers, you will be able to preview the result
          below.</p>}
      <span data-testid={`result-${calculatedValueIndex}`}>
        <span className="fw-bold">Result:</span><span>{previewResult}</span>
      </span>
    </div>
  </div>
}

SplitCalculatedValueEditor.displayName = 'SplitCalculatedValueEditor'


const createNewCalculatedValue = () => {
  return {
    name: '',
    expression: '',
    includeIntoResult: true
  }
}
