import { isPlainObject as _isPlainObject } from 'lodash'

export const isPlainObject = (config: unknown): config is Record<string, unknown> => {
  return _isPlainObject(config)
}
