import { FunctionFactory } from 'survey-core'

/** formats a receive Date or date string in the users current locale */
export const formatDate = (params: unknown[]) => {
  if (params.length !== 1) {
    return null
  }

  let date = params[0]
  if ((typeof(date) === 'string' || date instanceof String) && date.length) {
    date = new Date(date as string)
  } else if (!(date instanceof Date)) {
    return null
  }

  return (date as Date).toLocaleDateString()
}

FunctionFactory.Instance.register('formatDate', formatDate)
