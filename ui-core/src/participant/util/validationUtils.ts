import { isPlainObject as _isPlainObject } from 'lodash'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const isPlainObject = (config: unknown): config is Record<string, unknown> => {
  return _isPlainObject(config)
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requirePlainObject = (value: unknown, prefix = ''): Record<string, unknown> => {
  if (!isPlainObject(value)) {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}expected an object`)
  }
  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireString = (
  config: Record<string, unknown>,
  key: string,
  prefix = ''
): string => {
  const value = config[key]
  if (typeof value !== 'string') {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}a string value is required for "${key}"`)
  }

  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireOptionalString = (
  config: Record<string, unknown>,
  key: string,
  prefix = ''
): string | undefined => {
  const value = config[key]
  if (!(value === undefined || typeof value === 'string')) {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}if provided, "${key}" must be a string`)
  }

  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireNumber = (
  config: Record<string, unknown>,
  key: string,
  prefix = ''
): number => {
  const value = config[key]
  if (typeof value !== 'number') {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}a number value is required for "${key}"`)
  }

  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireOptionalNumber = (
  config: Record<string, unknown>,
  key: string,
  prefix = ''
): number | undefined => {
  const value = config[key]
  if (!(value === undefined || typeof value === 'number')) {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}if provided, "${key}" must be a number`)
  }

  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireOptionalBoolean = (
  config: Record<string, unknown>,
  key: string,
  prefix = ''
): boolean | undefined => {
  const value = config[key]
  if (!(value === undefined || typeof value === 'boolean')) {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}if provided, "${key}" must be a boolean`)
  }
  return value
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const requireOptionalArray = <ElementType>(
  config: Record<string, unknown>,
  key: string,
  validateElement: (el: unknown) => ElementType,
  prefix = ''
): ElementType[] => {
  const value = config[key]
  if (!(value === undefined || Array.isArray(value))) {
    const messagePrefix = prefix ? `${prefix}: ` : ''
    throw new Error(`${messagePrefix}if provided, "${key}" must be an array`)
  }

  return (value || []).map(validateElement)
}
