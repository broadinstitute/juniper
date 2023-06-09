import { useCallback, useState } from 'react'

/**
 * Force a component to re-render.
 * @returns A function that forces the component to re-render.
 * */
export const useForceUpdate = (): () => void => {
  const [, setKey] = useState(0)
  return useCallback(() => { setKey(k => k + 1) }, [])
}
