import { parseExpression } from './searchExpressionParser'
import { toReactQueryBuilderState } from './searchExpressionUtils'

describe('to react query builder', () => {
  it('converts basic expression', () => {
    const expression = parseExpression(`{profile.name} = 'John' and {age} > 30 or {age} < 20`)

    const result = toReactQueryBuilderState(expression)

    expect(result).toEqual({
      'combinator': 'and',
      'id': '1',
      'rules': [
        {
          'combinator': 'or',
          'rules': [
            {
              'combinator': 'and',
              'rules': [
                {
                  'field': 'profile.name',
                  'operator': '=',
                  'value': 'John'
                },
                {
                  'field': 'age',
                  'operator': '>',
                  'value': 30
                }
              ]
            },
            {
              'field': 'age',
              'operator': '<',
              'value': 20
            }
          ]
        }
      ]
    })
  })
  it('flattens chained ands', () => {
    const expression = parseExpression(
      `{profile.name} = 'John' and {age} > 30 and {age} < 40 and {age} > 20 or {age} < 20`
    )

    const result = toReactQueryBuilderState(expression)

    expect(result).toEqual({
      'combinator': 'and',
      'id': '1',
      'rules': [
        {
          'combinator': 'or',
          'rules': [
            {
              'combinator': 'and',
              'rules': [
                {
                  'field': 'profile.name',
                  'operator': '=',
                  'value': 'John'
                },
                {
                  'field': 'age',
                  'operator': '>',
                  'value': 30
                },
                {
                  'field': 'age',
                  'operator': '<',
                  'value': 40
                },
                {
                  'field': 'age',
                  'operator': '>',
                  'value': 20
                }
              ]
            },
            {
              'field': 'age',
              'operator': '<',
              'value': 20
            }
          ]
        }
      ]
    })
  })

  it('handles parentheses', () => {
    const expression = parseExpression(
      `({profile.name} = 'John' and {age} > 30) or {age} < 20 or ({age} > 30 and {age} < 40)`
    )

    expect(toReactQueryBuilderState(expression)).toEqual({
      'combinator': 'and',
      'id': '1',
      'rules': [
        {
          'combinator': 'or',
          'rules': [
            {
              'combinator': 'and',
              'rules': [
                {
                  'field': 'profile.name',
                  'operator': '=',
                  'value': 'John'
                },
                {
                  'field': 'age',
                  'operator': '>',
                  'value': 30
                }
              ]
            },
            {
              'field': 'age',
              'operator': '<',
              'value': 20
            },
            {
              'combinator': 'and',
              'rules': [
                {
                  'field': 'age',
                  'operator': '>',
                  'value': 30
                },
                {
                  'field': 'age',
                  'operator': '<',
                  'value': 40
                }
              ]
            }
          ]
        }
      ]
    })
  })
})
