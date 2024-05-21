import { numericRegex, parseNumber, RuleProcessor, toArray, trimIfString } from 'react-querybuilder'

/**
 * Heavily lifted from react-querybuilder's SpEL rule processor, but with some modifications
 * to make it work with our search format. Not all of these features are supported in our
 * search format, but they are here for completeness.
 */
export const ruleProcessorEnrolleeSearchExpression: RuleProcessor = (
  { field, operator, value, valueSource },
  { escapeQuotes, parseNumbers } = {}
) => {
  const valueIsField = valueSource === 'field'
  const operatorTL = operator.replace(/^=$/, '==')
  const useBareValue =
    typeof value === 'number' ||
    typeof value === 'boolean' ||
    typeof value === 'bigint' ||
    shouldRenderAsNumber(value, true) ||
    value === 'true' ||
    value === 'false'


  switch (operatorTL) {
    case '<':
    case '<=':
    case '==':
    case '!=':
    case '>':
    case '>=': {
      // i do not know why it forces equality to be ==
      const operator = operatorTL === '==' ? '=' : operatorTL

      let processedValue

      if (valueIsField) {
        processedValue = `{${trimIfString(value)}}`
      } else if (useBareValue) {
        processedValue = trimIfString(value)
      } else {
        processedValue = `'${escapeSingleQuotes(value, escapeQuotes)}'`
      }

      return `{${field}} ${operator} ${processedValue}`
    }
    case 'contains':
    case 'doesNotContain':
      return wrapInNegation(
        `${field} matches ${
          valueIsField || useBareValue
            ? trimIfString(value)
            : `'${escapeSingleQuotes(value, escapeQuotes)}'`
        }`,
        shouldNegate(operatorTL)
      )

    case 'beginsWith':
    case 'doesNotBeginWith': {
      const valueTL = valueIsField
        ? `'^'.concat(${trimIfString(value)})`
        : `'${
          (typeof value === 'string' && !value.startsWith('^')) || useBareValue ? '^' : ''
        }${escapeSingleQuotes(value, escapeQuotes)}'`
      return wrapInNegation(`${field} matches ${valueTL}`, shouldNegate(operatorTL))
    }

    case 'endsWith':
    case 'doesNotEndWith': {
      const valueTL = valueIsField
        ? `${trimIfString(value)}.concat('$')`
        : `'${escapeSingleQuotes(value, escapeQuotes)}${
          (typeof value === 'string' && !value.endsWith('$')) || useBareValue ? '$' : ''
        }'`
      return wrapInNegation(`${field} matches ${valueTL}`, shouldNegate(operatorTL))
    }

    case 'null':
      return `${field} == null`

    case 'notNull':
      return `${field} != null`

    case 'in':
    case 'notIn': {
      const negate = shouldNegate(operatorTL) ? '!' : ''
      const valueAsArray = toArray(value)
      if (valueAsArray.length > 0) {
        return `${negate}(${valueAsArray
          .map(
            val =>
              `${field} == ${
                valueIsField || shouldRenderAsNumber(val, parseNumbers)
                  ? `${trimIfString(val)}`
                  : `'${escapeSingleQuotes(val, escapeQuotes)}'`
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
        const firstNum = shouldRenderAsNumber(first, true)
          ? parseNumber(first, { parseNumbers: true })
          : NaN
        const secondNum = shouldRenderAsNumber(second, true)
          ? parseNumber(second, { parseNumbers: true })
          : NaN
        let firstValue = isNaN(firstNum)
          ? valueIsField
            ? `${first}`
            : `'${escapeSingleQuotes(first, escapeQuotes)}'`
          : firstNum
        let secondValue = isNaN(secondNum)
          ? valueIsField
            ? `${second}`
            : `'${escapeSingleQuotes(second, escapeQuotes)}'`
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

const shouldNegate = (op: string) => /^(does)?not/i.test(op)

const wrapInNegation = (clause: string, negate: boolean) => (negate ? `!(${clause})` : `${clause}`)


const escapeSingleQuotes = (
  v: string | number | boolean | object | null,
  escapeQuotes?: boolean
) => (typeof v !== 'string' || !escapeQuotes ? v : v.replaceAll(`'`, `\\'`))

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const shouldRenderAsNumber = (v: any, parseNumbers?: boolean) =>
  parseNumbers &&
  (typeof v === 'number' ||
    typeof v === 'bigint' ||
    (typeof v === 'string' && numericRegex.test(v)))
