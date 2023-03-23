import { useRef } from 'react'

let nextId = 1

export const useUniqueId = (prefix = 'element'): string => {
  const idRef = useRef<number>()
  if (idRef.current === undefined) {
    idRef.current = nextId
    nextId += 1
  }
  return `${prefix}-${idRef.current}`
}
