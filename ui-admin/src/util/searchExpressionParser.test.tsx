import { parseExpression } from './searchExpressionParser'

describe('search expression parser', () => {
  it('parse basic expressions', () => {
    const result = parseExpression(`{profile.name} = 'John' and {age} > 30 or {age} < 20`)

    expect(result).toEqual({
      'booleanOperator': 'or',
      'left': {
        'booleanOperator': 'and',
        'left': {
          'comparisonOperator': '=',
          'left': {
            'field': [
              'name'
            ],
            'model': 'profile'
          },
          'right': 'John'
        },
        'right': {
          'comparisonOperator': '>',
          'left': {
            'field': [],
            'model': 'age'
          },
          'right': 30
        }
      },
      'right': {
        'comparisonOperator': '<',
        'left': {
          'field': [],
          'model': 'age'
        },
        'right': 20
      }
    })
  })

  it('parses expressions with parens', () => {
    const result = parseExpression(`{profile.name} = 'John' and ({age} > 30 or ({age} < 20 and {age} > 10))`)

    expect(result).toEqual({
      'booleanOperator': 'and',
      'left': {
        'comparisonOperator': '=',
        'left': {
          'field': [
            'name'
          ],
          'model': 'profile'
        },
        'right': 'John'
      },
      'right': {
        'booleanOperator': 'or',
        'left': {
          'comparisonOperator': '>',
          'left': {
            'field': [],
            'model': 'age'
          },
          'right': 30
        },
        'right': {
          'booleanOperator': 'and',
          'left': {
            'comparisonOperator': '<',
            'left': {
              'field': [],
              'model': 'age'
            },
            'right': 20
          },
          'right': {
            'comparisonOperator': '>',
            'left': {
              'field': [],
              'model': 'age'
            },
            'right': 10
          }
        }
      }
    })
  })

  it('parses variables with multiple fields', () => {
    const result = parseExpression(`{answer.hd_hd_preenroll.qualified} = true`)

    expect(result).toEqual({
      'comparisonOperator': '=',
      'left': {
        'field': [
          'hd_hd_preenroll',
          'qualified'
        ],
        'model': 'answer'
      },
      'right': true
    })
  })

  it('throws an error on invalid input', () => {
    expect(() => parseExpression(`{`)).toThrow()
  })
})
