import { useCallback, useState } from 'react'

export const useForceUpdate = (): () => void => {
  const [, setKey] = useState(0)
  return useCallback(() => { setKey(k => k + 1) }, [])
}
