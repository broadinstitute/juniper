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
  isEmpty
} from 'lodash'

/**
 * Heavily lifted from react-querybuilder's SpEL rule processor, but with some modifications
 * to make it work with our search format. Not all of these features are supported in our
 * search format, but they are here for completeness.
 */
export const createEnrolleeSearchExpressionRuleProcessor = (facets: ExpressionSearchFacets): RuleProcessor => {
  return (
    { field, operator, value, valueSource },
    { escapeQuotes, parseNumbers } = {}
  ) => {
    const valueIsField = valueSource === 'field'


    const defaultTypeDefinition: SearchValueTypeDefinition = {
      type: 'STRING', allowMultiple: false, allowOtherDescription: false
    }
    const typeDefinition = get(facets, field, defaultTypeDefinition)

    const useBareValue =
      typeof value === 'number' ||
      typeof value === 'boolean' ||
      typeof value === 'bigint' ||
      shouldRenderAsNumber(value, typeDefinition, true) ||
      value === 'true' ||
      value === 'false'

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
          } else {
            processedValue = trimIfString(value)
          }
        } else {
          processedValue = `'${escape(value, escapeQuotes)}'`
        }

        return `{${field}} ${operator} ${processedValue}`
      }
      case 'contains':
      case 'doesNotContain':
        return wrapInNegation(
          `{${field}} contains ${
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
                  valueIsField || shouldRenderAsNumber(val, typeDefinition, parseNumbers)
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
          const firstNum = shouldRenderAsNumber(first, typeDefinition, true)
            ? parseNumber(first, { parseNumbers: true })
            : NaN
          const secondNum = shouldRenderAsNumber(second, typeDefinition, true)
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
            return `(${field} >= ${firstValue} and ${field} <= ${secondValue})`
          } else {
            return `(${field} < ${firstValue} or ${field} > ${secondValue})`
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

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const shouldRenderAsNumber = (v: any,
  typeDef: SearchValueTypeDefinition,
  parseNumbers?: boolean) =>
  parseNumbers &&
  (typeof v === 'number' ||
    typeof v === 'bigint' ||
    (typeof v === 'string' && typeDef.type === 'NUMBER'))
