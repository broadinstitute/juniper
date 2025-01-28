import { KeyedSearchValueTypeDefinition } from 'api/api'
import { parseAnswerFacet } from './columnUtils'

test('filters legacy prefixes from col names', async () => {
  const facet: KeyedSearchValueTypeDefinition = {
    key: 'answer.surveyA.oh_oh_question1',
    type: 'STRING',
    allowMultiple: false,
    allowOtherDescription: false
  }
  const { surveyStableId, questionStableId, header } = parseAnswerFacet(facet)
  expect(surveyStableId).toBe('surveyA')
  expect(questionStableId).toBe('oh_oh_question1')
  expect(header).toBe('Question 1')
})
