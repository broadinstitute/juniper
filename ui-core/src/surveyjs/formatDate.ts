import { FunctionFactory } from 'survey-core'

const formatDate = (params: unknown[]) => {
  if (params.length !== 1) {
    return null
  }

  const date = params[0]
  if (!(date instanceof Date)) {
    return null
  }

  return date.toLocaleDateString()
}

FunctionFactory.Instance.register('formatDate', formatDate)
