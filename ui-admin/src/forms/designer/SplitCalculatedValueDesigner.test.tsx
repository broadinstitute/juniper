import React, {
  useEffect,
  useState
} from 'react'
import {
  act,
  render,
  screen,
  waitFor
} from '@testing-library/react'
import { SplitCalculatedValueDesigner } from 'forms/designer/SplitCalculatedValueDesigner'
import { FormContent } from '@juniper/ui-core'
import { userEvent } from '@testing-library/user-event'

const TestCalculatedValueDesignerWrapper = ({
  initialContent, onUpdate
}: {
  initialContent: FormContent, onUpdate?: (content: FormContent) => void
}) => {
  const [content, setContent] = useState(initialContent)

  useEffect(() => {
    if (onUpdate) {
      onUpdate(content)
    }
  }, [])

  return <SplitCalculatedValueDesigner content={content} onChange={setContent}/>
}

describe('SplitCalculatedValueDesigner', () => {
  it('should render', () => {
    const content: FormContent = {
      title: 'title',
      pages: [],
      questionTemplates: [],
      calculatedValues: []
    }

    render(<TestCalculatedValueDesignerWrapper initialContent={content}/>)

    expect(screen.getByText('Insert derived value')).toBeInTheDocument()
  })
  it('should render with calculated values', () => {
    const content: FormContent = {
      title: 'title',
      pages: [],
      questionTemplates: [],
      calculatedValues: [
        { name: 'cv1', expression: '{test} = 1', includeIntoResult: true },
        { name: 'cv2', expression: '{asdf} = 3', includeIntoResult: true }
      ]
    }

    render(<TestCalculatedValueDesignerWrapper initialContent={content}/>)

    expect(screen.getAllByText('Insert derived value')).toHaveLength(3)
    expect(screen.getByText('cv1')).toBeInTheDocument()
    expect(screen.getByText('cv2')).toBeInTheDocument()
  })
  it('should add a new calculated value', () => {
    const content: FormContent = {
      title: 'title',
      pages: [],
      questionTemplates: [],
      calculatedValues: []
    }

    render(<TestCalculatedValueDesignerWrapper initialContent={content}/>)

    expect(screen.getAllByText('Insert derived value')).toHaveLength(1)

    act(() => screen.getAllByText('Insert derived value')[0].click())

    expect(screen.getAllByText('Insert derived value')).toHaveLength(2)

    act(() => screen.getAllByText('Insert derived value')[0].click())

    expect(screen.getAllByText('Insert derived value')).toHaveLength(3)
  })
  it('has question preview for calculated values', async () => {
    const content: FormContent = {
      title: 'title',
      pages: [{
        elements: [
          {
            name: 'my_question',
            title: 'My Question',
            type: 'text',
            inputType: 'number',
            placeholder: 'Question 1'
          }, {
            name: 'unused_question',
            title: 'Unused Question',
            type: 'text',
            inputType: 'text',
            placeholder: 'Question 3'
          }
        ]
      }],
      questionTemplates: [],
      calculatedValues: [
        {
          name: 'cv1',
          expression: '',
          includeIntoResult: true
        }
      ]
    }

    render(<TestCalculatedValueDesignerWrapper initialContent={content}/>)

    expect(screen.queryByText('My Question')).not.toBeInTheDocument()
    expect(screen.queryByText('Unused Question')).not.toBeInTheDocument()

    const expressionInput = screen.getByLabelText('Expression')

    await act(async () => {
      await userEvent.type(
        expressionInput,
        // note: curly braces must be escaped by doubling them
        '{{my_question} +  5')
    })

    await waitFor(() => {
      expect(screen.getByText('My Question')).toBeInTheDocument()
      expect(screen.queryByText('Unused Question')).not.toBeInTheDocument()
    })

    await act(async () => {
      await userEvent.type(screen.getByPlaceholderText('Question 1'), '10')
      await userEvent.click(screen.getByLabelText('Name'))
    })


    // await screen.findByText('Result:15')

    await waitFor(() => {
      expect(screen.queryByTestId('result-0')).toHaveTextContent('Result: 15')
    })
  })
})
