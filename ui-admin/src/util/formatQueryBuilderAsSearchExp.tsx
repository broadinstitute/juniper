import {
  parseNumber,
  RuleProcessor,
  toArray,
  trimIfString
} from 'react-querybuilder'
import {
  ExpressionSearchFacets,
  SearchValueTypeDefinition
} from 'api/api'
import {
  get,
  has,
  isEmpty,
  isString
} from 'lodash'

/**
 * Heavily lifted from react-querybuilder's SpEL rule processor, but with some modifications
 * to make it work with our search format. Not all of these features are supported in our
 * search format, but they are here for completeness.
 */
export const createEnrolleeSearchExpressionRuleProcessor = (facets: ExpressionSearchFacets): RuleProcessor => {
  return (
    { field, operator, value, valueSource },
    { escapeQuotes } = {}
  ) => {
    const valueIsField = valueSource === 'field'


    const defaultTypeDefinition: SearchValueTypeDefinition = {
      type: 'STRING', allowMultiple: false, allowOtherDescription: false
    }
    const typeDefinition = get(facets, field, defaultTypeDefinition)

    const useBareValue = typeDefinition.type === 'NUMBER' ||
      typeDefinition.type === 'BOOLEAN'

    const processedField = has(facets, field) ? `{${field}}` : field

    switch (operator) {
      case '<':
      case '<=':
      case '=':
      case '!=':
      case '>':
      case '>=': {
        let processedValue

        if (valueIsField) {
          processedValue = `{${trimIfString(value)}}`
        } else if (useBareValue) {
          if (isEmpty(value) && typeDefinition.type === 'NUMBER') {
            processedValue = 0
          } else if (typeDefinition.type === 'BOOLEAN') {
            processedValue = isString(value) ? value.toLowerCase() === 'true' : false
          } else {
            processedValue = trimIfString(value)
          }
        } else {
          processedValue = `'${escape(value, escapeQuotes)}'`
        }

        return `${processedField} ${operator} ${processedValue}`
      }
      case 'contains':
      case 'doesNotContain':
        return wrapInNegation(
          `${processedField} contains ${
            valueIsField || useBareValue
              ? trimIfString(value)
              : `'${escape(value, escapeQuotes)}'`
          }`,
          shouldNegate(operator)
        )

      case 'beginsWith':
      case 'doesNotBeginWith': {
        const valueTL = valueIsField
          ? `'^'.concat(${trimIfString(value)})`
          : `'${
            (typeof value === 'string' && !value.startsWith('^')) || useBareValue ? '^' : ''
          }${escape(value, escapeQuotes)}'`
        return wrapInNegation(`${field} matches ${valueTL}`, shouldNegate(operator))
      }

      case 'endsWith':
      case 'doesNotEndWith': {
        const valueTL = valueIsField
          ? `${trimIfString(value)}.concat('$')`
          : `'${escape(value, escapeQuotes)}${
            (typeof value === 'string' && !value.endsWith('$')) || useBareValue ? '$' : ''
          }'`
        return wrapInNegation(`${field} matches ${valueTL}`, shouldNegate(operator))
      }

      case 'null':
        return `${field} = null`

      case 'notNull':
        return `${field} != null`

      case 'in':
      case 'notIn': {
        const negate = shouldNegate(operator) ? '!' : ''
        const valueAsArray = toArray(value)
        if (valueAsArray.length > 0) {
          return `${negate}(${valueAsArray
            .map(
              val =>
                `${field} = ${
                  valueIsField || typeDefinition.type === 'NUMBER'
                    ? `${trimIfString(val)}`
                    : `'${escape(val, escapeQuotes)}'`
                }`
            )
            .join(' or ')})`
        } else {
          return ''
        }
      }

      case 'between':
      case 'notBetween': {
        const valueAsArray = toArray(value)
        if (valueAsArray.length >= 2 && !!valueAsArray[0] && !!valueAsArray[1]) {
          const [first, second] = valueAsArray
          const firstNum = typeDefinition.type === 'NUMBER'
            ? parseNumber(first, { parseNumbers: true })
            : NaN
          const secondNum = typeDefinition.type === 'NUMBER'
            ? parseNumber(second, { parseNumbers: true })
            : NaN
          let firstValue = isNaN(firstNum)
            ? valueIsField
              ? `${first}`
              : `'${escape(first, escapeQuotes)}'`
            : firstNum
          let secondValue = isNaN(secondNum)
            ? valueIsField
              ? `${second}`
              : `'${escape(second, escapeQuotes)}'`
            : secondNum
          if (firstValue === firstNum && secondValue === secondNum && secondNum < firstNum) {
            const tempNum = secondNum
            secondValue = firstNum
            firstValue = tempNum
          }
          if (operator === 'between') {
            return `(${processedField} >= ${firstValue} and ${processedField} <= ${secondValue})`
          } else {
            return `(${processedField} < ${firstValue} or ${processedField} > ${secondValue})`
          }
        } else {
          return ''
        }
      }
    }

    return ''
  }
}

const shouldNegate = (op: string) => /^(does)?not/i.test(op)

const wrapInNegation = (clause: string, negate: boolean) => (negate ? `!(${clause})` : `${clause}`)


const escape = (
  v: string | number | boolean | object | null,
  escapeQuotes?: boolean
) => (typeof v !== 'string' || !escapeQuotes
  ? v
  : v.replaceAll(`'`, ``).replaceAll('\\', ''))
