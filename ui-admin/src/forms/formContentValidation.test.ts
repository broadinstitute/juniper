import {
  getAllQuestions,
  validateFormContent,
  validateQuestionNames,
  validateQuestionTypes,
  validateTemplatedQuestions
} from './formContentValidation'
import { FormContent } from '@juniper/ui-core'

describe('validateFormContent', () => {
  it('getAllQuestions throws an exception if a form is missing a pages property', () => {
    const formContent: FormContent = {
      title: 'test'
    } as unknown as FormContent //cast in order to simulate invalid form content

    expect(() => getAllQuestions(formContent)).toThrowError(
      `Error parsing form. Please ensure that the form has a 'pages' property.`
    )
  })

  it('getAllQuestions throws an exception if a panel is missing `elements` property', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              type: 'panel',
              elementz: [ //error: simulate typo'd elements property
                {
                  name: 'test',
                  type: 'text',
                  title: 'test title'
                }
              ]
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    expect(() => getAllQuestions(formContent)).toThrowError(
      `Error parsing form. Please ensure that all panels have an 'elements' property.`
    )
  })

  it('getAllQuestions throws an exception if a page is missing `elements` property', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elementsz: [ //error: simulate typo'd elements property
            {
              type: 'panel',
              elements: [
                {
                  name: 'test',
                  type: 'text',
                  title: 'test title'
                }
              ]
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    expect(() => getAllQuestions(formContent)).toThrowError(
      `Error parsing form. Please ensure that all pages have an 'elements' property.`
    )
  })

  it('getAllQuestions returns all questions in the form, including those in panels', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              name: 'test',
              type: 'text',
              title: 'test title'
            },
            {
              type: 'panel',
              elements: [
                {
                  name: 'test2',
                  type: 'text',
                  title: 'test title 2'
                }
              ]
            }
          ]
        }
      ]
    }

    const questions = getAllQuestions(formContent)
    expect(questions.length).toBe(2)
    expect(questions[0].name).toBe('test')
    expect(questions[1].name).toBe('test2')
  })

  it('validateQuestionNames returns an error if a question is missing a name', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              type: 'text',
              title: 'test title'
            },
            {
              type: 'text',
              title: 'test title 2'
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    const questions = getAllQuestions(formContent)
    const errors = validateQuestionNames(questions)
    expect(errors.length).toBe(1)
    expect(errors[0]).toBe(`2 questions are missing a 'name' field.`)
  })

  it('validateQuestionNames returns an error if two questions have a duplicate name', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              name: 'test',
              type: 'text',
              title: 'test title'
            },
            {
              name: 'test',
              type: 'text',
              title: 'test title 2'
            }
          ]
        }
      ]
    } as FormContent

    const questions = getAllQuestions(formContent)
    const errors = validateQuestionNames(questions)
    expect(errors.length).toBe(1)
    expect(errors[0]).toBe(`Duplicate question name: test`)
  })

  it('validateQuestionTypes returns an error if a question is missing a type', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              name: 'oh_test',
              title: 'test title'
            },
            {
              name: 'oh_test2',
              title: 'test title 2'
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    const questions = getAllQuestions(formContent)
    const errors = validateQuestionTypes(questions)
    expect(errors.length).toBe(2)
    expect(errors[0]).toBe(`Question oh_test is missing a 'type' field.`)
    expect(errors[1]).toBe(`Question oh_test2 is missing a 'type' field.`)
  })

  it('validateQuestionTypes does not return an error if a TemplatedQuestion is missing a type', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              name: 'oh_test',
              title: 'test title',
              questionTemplateName: 'testTemplate'
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    const questions = getAllQuestions(formContent)
    const errors = validateQuestionTypes(questions)
    expect(errors.length).toBe(0)
  })

  it('validateTemplatedQuestions returns an error if a' +
    'TemplatedQuestion references a template that doesnt exist', () => {
    const formContent: FormContent = {
      title: 'test',
      pages: [
        {
          elements: [
            {
              name: 'oh_test',
              title: 'test title',
              questionTemplateName: 'testTemplate'
            }
          ]
        }
      ]
    } as unknown as FormContent //cast in order to simulate invalid form content

    const questions = getAllQuestions(formContent)
    const errors = validateTemplatedQuestions(formContent, questions)
    expect(errors.length).toBe(1)
    expect(errors[0]).toBe(`'oh_test' references non-existent template 'testTemplate'`)
  })
})
